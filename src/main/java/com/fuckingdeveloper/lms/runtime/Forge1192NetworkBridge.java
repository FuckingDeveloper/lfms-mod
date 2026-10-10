package com.fuckingdeveloper.lms.runtime;

import net.minecraft.resources.Identifier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Ownership boundary for Forge 1.19.2 SimpleChannel migration.
 *
 * <p>The old indexed-message protocol is not binary-compatible with current
 * NeoForge payload registration. This bridge deliberately models the legacy
 * channel before any target registration occurs: no packet is silently
 * registered against a guessed modern protocol.</p>
 */
public final class Forge1192NetworkBridge {
    private Forge1192NetworkBridge() {}

    /**
     * Forge 1.19.2 FriendlyByteBuf#writeRegistryIdUnsafe serialized the raw
     * numeric id from the supplied registry. LMS registry facade tokens are
     * concrete vanilla Registry instances, so preserve that wire contract.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void writeRegistryIdUnsafe(FriendlyByteBuf buffer, Object registryToken, Object value) {
        Objects.requireNonNull(buffer, "buffer");
        Objects.requireNonNull(value, "value");
        if (!(registryToken instanceof net.minecraft.core.Registry registry)) {
            throw new IllegalStateException("Legacy registry network token is not a vanilla Registry: "
                    + (registryToken == null ? "null" : registryToken.getClass().getName()));
        }
        int id = registry.getId(value);
        if (id < 0) {
            throw new IllegalArgumentException("Value is not present in legacy registry network token: " + value);
        }
        buffer.writeVarInt(id);
    }


    /**
     * Inverse of the Forge 1.19.2 raw registry-id writer: consume the varint
     * numeric id and resolve it against the explicit registry token.
     */
    @SuppressWarnings("rawtypes")
    public static Object readRegistryIdUnsafe(FriendlyByteBuf buffer, Object registryToken) {
        Objects.requireNonNull(buffer, "buffer");
        if (!(registryToken instanceof net.minecraft.core.Registry registry)) {
            throw new IllegalStateException("Legacy registry network token is not a vanilla Registry: "
                    + (registryToken == null ? "null" : registryToken.getClass().getName()));
        }
        int id = buffer.readVarInt();
        Object value = registry.byId(id);
        if (value == null) {
            throw new IllegalArgumentException("Unknown raw registry id " + id + " for legacy registry network token");
        }
        return value;
    }



    public enum Flow { CLIENTBOUND, SERVERBOUND, BIDIRECTIONAL }

    public record LegacyMessage(
            int discriminator,
            Class<?> messageType,
            BiConsumer<Object, Object> encoder,
            Function<Object, Object> decoder,
            BiConsumer<Object, Supplier<Object>> consumer,
            Flow flow) {
        public LegacyMessage {
            Objects.requireNonNull(messageType, "messageType");
            Objects.requireNonNull(encoder, "encoder");
            Objects.requireNonNull(decoder, "decoder");
            Objects.requireNonNull(consumer, "consumer");
        }

        public LegacyMessage withFlow(Flow value) {
            return new LegacyMessage(discriminator, messageType, encoder, decoder, consumer,
                    Objects.requireNonNull(value, "flow"));
        }
    }

    private static final ConcurrentHashMap<LegacyChannel, ConcurrentHashMap<Integer, LegacyMessage>> MESSAGES =
            new ConcurrentHashMap<>();

    public static java.util.List<LegacyMessage> messages(LegacyChannel channel) {
        var entries = MESSAGES.get(Objects.requireNonNull(channel, "channel"));
        if (entries == null) throw new IllegalStateException("Unknown legacy network channel");
        return entries.values().stream()
                .sorted(java.util.Comparator.comparingInt(LegacyMessage::discriminator))
                .toList();
    }

    /**
     * Preflight for the NeoForge 26.3 payload registration event. A legacy
     * SimpleChannel registration has no direction or connection phase, so
     * neither may be inferred from registerMessage alone.
     */
    public enum PayloadReadiness {
        EMPTY_CHANNEL, DIRECTION_UNRESOLVED, CODEC_UNVERIFIED
    }

    public record PayloadPreflight(String modId, Identifier channel, int messageCount,
                                   PayloadReadiness readiness, String reason) {}

    public static PayloadPreflight preflight(LegacyChannel channel) {
        var registered = messages(channel);
        if (registered.isEmpty())
            return new PayloadPreflight(channel.modId(), channel.name(), 0,
                    PayloadReadiness.EMPTY_CHANNEL, "No legacy messages registered");
        if (registered.stream().anyMatch(message -> message.flow() == null))
            return new PayloadPreflight(channel.modId(), channel.name(), registered.size(),
                    PayloadReadiness.DIRECTION_UNRESOLVED,
                    "One or more legacy messages have no proven packet direction");
        return new PayloadPreflight(channel.modId(), channel.name(), registered.size(),
                PayloadReadiness.CODEC_UNVERIFIED,
                "Legacy message flow is resolved; NeoForge payload type/phase binding remains unverified");
    }

    /**
     * Adapts the legacy encoder/decoder to a typed stream codec without
     * assuming packet direction or registering it. The old callbacks use
     * FriendlyByteBuf-compatible buffers; the original generic signatures
     * were erased when captured from the legacy registration.
     */
    public enum FlowEvidence { EXPLICIT_SEND_TO_SERVER, EXPLICIT_SEND_TO_CLIENT, EXPLICIT_BIDIRECTIONAL }

    public static Flow flowFromEvidence(FlowEvidence evidence) {
        return switch (Objects.requireNonNull(evidence, "evidence")) {
            case EXPLICIT_SEND_TO_SERVER -> Flow.SERVERBOUND;
            case EXPLICIT_SEND_TO_CLIENT -> Flow.CLIENTBOUND;
            case EXPLICIT_BIDIRECTIONAL -> Flow.BIDIRECTIONAL;
        };
    }

    public static void resolveFlow(Object token, int discriminator, Flow flow) {
        LegacyChannel channel = requireChannel(token);
        var registrations = MESSAGES.get(channel);
        LegacyMessage message = registrations.get(discriminator);
        if (message == null)
            throw new IllegalArgumentException("Unknown legacy network discriminator " + discriminator
                    + " on channel " + channel.name());
        Flow proven = Objects.requireNonNull(flow, "flow");
        if (message.flow() != null && message.flow() != proven)
            throw new IllegalStateException("Conflicting legacy packet flow evidence for discriminator "
                    + discriminator + " on channel " + channel.name() + ": "
                    + message.flow() + " vs " + proven);
        registrations.put(discriminator, message.withFlow(proven));
    }

    public static StreamCodec<FriendlyByteBuf, Object> codec(LegacyMessage message) {
        Objects.requireNonNull(message, "message");
        return new StreamCodec<>() {
            @Override
            public Object decode(FriendlyByteBuf buffer) {
                Object decoded = Objects.requireNonNull(
                        message.decoder().apply(buffer),
                        "Legacy packet decoder returned null for discriminator " + message.discriminator());
                if (!message.messageType().isInstance(decoded))
                    throw new IllegalStateException("Legacy packet decoder returned "
                            + decoded.getClass().getName() + " instead of "
                            + message.messageType().getName());
                return decoded;
            }

            @Override
            public void encode(FriendlyByteBuf buffer, Object value) {
                if (!message.messageType().isInstance(value))
                    throw new IllegalArgumentException("Legacy packet encoder expected "
                            + message.messageType().getName());
                message.encoder().accept(value, buffer);
            }
        };
    }

    public static void requirePayloadRegistrationReady(LegacyChannel channel) {
        requireChannel(channel);
        throw new UnsupportedOperationException(
                "Forge 1.19.2 SimpleChannel " + channel.name()
                + " cannot register payloads until legacy buffer codec, direction and "
                + "NeoForge 26.3 payload event bindings are verified");
    }

    public record LegacyChannel(
            String modId,
            Identifier name,
            Supplier<String> protocolVersion,
            Predicate<String> clientAcceptedVersions,
            Predicate<String> serverAcceptedVersions) {
        public LegacyChannel {
            Objects.requireNonNull(modId, "modId");
            Objects.requireNonNull(name, "name");
            Objects.requireNonNull(protocolVersion, "protocolVersion");
            Objects.requireNonNull(clientAcceptedVersions, "clientAcceptedVersions");
            Objects.requireNonNull(serverAcceptedVersions, "serverAcceptedVersions");
        }
    }

    public static Object newSimpleChannel(
            Identifier name,
            Supplier<String> protocolVersion,
            Predicate<String> clientAcceptedVersions,
            Predicate<String> serverAcceptedVersions) {
        String modId = Forge1192LifecycleBridge.requireActive().modId();
        LegacyChannel channel = new LegacyChannel(modId, name, protocolVersion, clientAcceptedVersions, serverAcceptedVersions);
        MESSAGES.put(channel, new ConcurrentHashMap<>());
        return channel;
    }

    public static LegacyChannel requireChannel(Object token) {
        if (!(token instanceof LegacyChannel channel))
            throw new IllegalStateException("Legacy SimpleChannel token escaped or has an invalid provenance");
        String activeMod = Forge1192LifecycleBridge.requireActive().modId();
        if (!channel.modId().equals(activeMod))
            throw new IllegalStateException("Legacy SimpleChannel belongs to " + channel.modId()
                    + " but active legacy mod is " + activeMod);
        return channel;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Object registerMessage(Object token, int discriminator, Class messageType,
                                         BiConsumer encoder, Function decoder, BiConsumer consumer) {
        LegacyChannel channel = requireChannel(token);
        Objects.requireNonNull(messageType, "messageType");
        Objects.requireNonNull(encoder, "encoder");
        Objects.requireNonNull(decoder, "decoder");
        Objects.requireNonNull(consumer, "consumer");
        LegacyMessage message = new LegacyMessage(discriminator, messageType, encoder, decoder, consumer, null);
        LegacyMessage previous = MESSAGES.get(channel).putIfAbsent(discriminator, message);
        if (previous != null)
            throw new IllegalStateException("Duplicate legacy network discriminator " + discriminator
                    + " on channel " + channel.name());
        // Forge returned an IndexedMessageCodec.MessageHandler. LMS deliberately
        // returns its own opaque registration token; no packet can execute until
        // the current NeoForge payload adapter has bound this descriptor.
        return message;
    }
    /**
     * LMS-owned replacement for the old Forge NetworkEvent.Context. The actual
     * NeoForge payload adapter will construct this object with explicit sender
     * and work scheduling semantics; legacy handlers never receive a guessed
     * modern context object.
     */
    public static final class LegacyContext {
        private final net.minecraft.server.level.ServerPlayer sender;
        private final Function<Runnable, CompletableFuture<Void>> scheduler;
        private volatile boolean packetHandled;

        public LegacyContext(net.minecraft.server.level.ServerPlayer sender,
                             Function<Runnable, CompletableFuture<Void>> scheduler) {
            this.sender = sender;
            this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        }

        public CompletableFuture<Void> enqueueWork(Runnable work) {
            return scheduler.apply(Objects.requireNonNull(work, "work"));
        }

        public net.minecraft.server.level.ServerPlayer sender() {
            return sender;
        }

        public void setPacketHandled(boolean handled) {
            packetHandled = handled;
        }

        public boolean packetHandled() {
            return packetHandled;
        }
    }

    private static LegacyContext requireContext(Object token) {
        if (!(token instanceof LegacyContext context))
            throw new IllegalStateException("Legacy NetworkEvent.Context token escaped or has invalid provenance");
        return context;
    }

    public static CompletableFuture<Void> enqueueWork(Object token, Runnable work) {
        return requireContext(token).enqueueWork(work);
    }

    public static net.minecraft.server.level.ServerPlayer getSender(Object token) {
        return requireContext(token).sender();
    }

    public static void setPacketHandled(Object token, boolean handled) {
        requireContext(token).setPacketHandled(handled);
    }

}
