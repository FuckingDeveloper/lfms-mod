package com.fuckingdeveloper.lms.runtime;

import net.minecraft.resources.Identifier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

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

    public enum TargetKind {
        SERVER, PLAYER, ALL, DIMENSION, TRACKING_CHUNK, TRACKING_ENTITY_AND_SELF, NEAR
    }

    public record LegacyPacketTarget(TargetKind kind, Object argument) {
        public LegacyPacketTarget {
            Objects.requireNonNull(kind, "kind");
        }
    }

    public record LegacyTargetPoint(
            double x, double y, double z, double radius,
            net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension) {
        public LegacyTargetPoint {
            Objects.requireNonNull(dimension, "dimension");
        }
    }

    /** Source-shaped targeting tokens retained without claiming a modern send binding. */
    public static final class LegacyPacketDistributor {
        public static final LegacyPacketDistributor SERVER = new LegacyPacketDistributor(TargetKind.SERVER);
        public static final LegacyPacketDistributor PLAYER = new LegacyPacketDistributor(TargetKind.PLAYER);
        public static final LegacyPacketDistributor ALL = new LegacyPacketDistributor(TargetKind.ALL);
        public static final LegacyPacketDistributor DIMENSION = new LegacyPacketDistributor(TargetKind.DIMENSION);
        public static final LegacyPacketDistributor TRACKING_CHUNK =
                new LegacyPacketDistributor(TargetKind.TRACKING_CHUNK);
        public static final LegacyPacketDistributor TRACKING_ENTITY_AND_SELF =
                new LegacyPacketDistributor(TargetKind.TRACKING_ENTITY_AND_SELF);
        public static final LegacyPacketDistributor NEAR = new LegacyPacketDistributor(TargetKind.NEAR);

        private final TargetKind kind;

        private LegacyPacketDistributor(TargetKind kind) {
            this.kind = Objects.requireNonNull(kind, "kind");
        }

        public LegacyPacketTarget noArg() {
            return new LegacyPacketTarget(kind, null);
        }

        public LegacyPacketTarget with(Supplier<?> argument) {
            return new LegacyPacketTarget(kind, Objects.requireNonNull(argument, "argument").get());
        }
    }

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

    public record LegacyPayload(
            LegacyChannel channel, LegacyMessage registration, Object message)
            implements CustomPacketPayload {
        public LegacyPayload {
            Objects.requireNonNull(channel, "channel");
            Objects.requireNonNull(registration, "registration");
            Objects.requireNonNull(message, "message");
        }

        @Override
        public Type<LegacyPayload> type() {
            return channel.payloadType();
        }
    }

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
            Predicate<String> serverAcceptedVersions,
            CustomPacketPayload.Type<LegacyPayload> payloadType) {
        public LegacyChannel {
            Objects.requireNonNull(modId, "modId");
            Objects.requireNonNull(name, "name");
            Objects.requireNonNull(protocolVersion, "protocolVersion");
            Objects.requireNonNull(clientAcceptedVersions, "clientAcceptedVersions");
            Objects.requireNonNull(serverAcceptedVersions, "serverAcceptedVersions");
            Objects.requireNonNull(payloadType, "payloadType");
        }
    }

    public static Object newSimpleChannel(
            Identifier name,
            Supplier<String> protocolVersion,
            Predicate<String> clientAcceptedVersions,
            Predicate<String> serverAcceptedVersions) {
        String modId = Forge1192LifecycleBridge.requireActive().modId();
        LegacyChannel channel = new LegacyChannel(modId, name, protocolVersion,
                clientAcceptedVersions, serverAcceptedVersions,
                new CustomPacketPayload.Type<>(name));
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

    public static void send(Object token, LegacyPacketTarget target, Object message) {
        LegacyChannel channel = requireChannel(token);
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(message, "message");
        LegacyMessage registration = messages(channel).stream()
                .filter(candidate -> candidate.messageType() == message.getClass())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid legacy message "
                        + message.getClass().getName() + " on channel " + channel.name()));
        LegacyPayload payload = new LegacyPayload(channel, registration, message);
        switch (target.kind()) {
            case SERVER -> Target263ClientNetworkBridge.sendToServer(payload);
            case PLAYER -> PacketDistributor.sendToPlayer(
                    requireArgument(target, ServerPlayer.class), payload);
            case ALL -> PacketDistributor.sendToAllPlayers(payload);
            case DIMENSION -> {
                @SuppressWarnings("unchecked")
                var dimension = (net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level>)
                        requireArgument(target, net.minecraft.resources.ResourceKey.class);
                var server = Objects.requireNonNull(ServerLifecycleHooks.getCurrentServer(),
                        "Cannot send a dimension payload without an active server");
                ServerLevel level = Objects.requireNonNull(server.getLevel(dimension),
                        "Unknown target dimension " + dimension.identifier());
                PacketDistributor.sendToPlayersInDimension(level, payload);
            }
            case TRACKING_CHUNK -> {
                LevelChunk chunk = requireArgument(target, LevelChunk.class);
                if (!(chunk.getLevel() instanceof ServerLevel level)) {
                    throw new IllegalStateException("Cannot send a tracking-chunk payload from a client level");
                }
                PacketDistributor.sendToPlayersTrackingChunk(level, chunk.getPos(), payload);
            }
            case TRACKING_ENTITY_AND_SELF -> PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                    requireArgument(target, Entity.class), payload);
            case NEAR -> {
                LegacyTargetPoint point = requireArgument(target, LegacyTargetPoint.class);
                var server = Objects.requireNonNull(ServerLifecycleHooks.getCurrentServer(),
                        "Cannot send a nearby payload without an active server");
                ServerLevel level = Objects.requireNonNull(server.getLevel(point.dimension()),
                        "Unknown target dimension " + point.dimension().identifier());
                PacketDistributor.sendToPlayersNear(level, null, point.x(), point.y(), point.z(),
                        point.radius(), payload);
            }
        }
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        Objects.requireNonNull(event, "event");
        MESSAGES.keySet().stream()
                .sorted(java.util.Comparator.comparing(channel -> channel.name().toString()))
                .forEach(channel -> {
                    String version = Objects.requireNonNull(channel.protocolVersion().get(),
                            "Legacy network protocol version");
                    if (version.isBlank()) {
                        throw new IllegalStateException("Blank legacy network protocol version for "
                                + channel.name());
                    }
                    if (!channel.clientAcceptedVersions().test(version)
                            || !channel.serverAcceptedVersions().test(version)) {
                        throw new IllegalStateException("Legacy channel rejects its own protocol version: "
                                + channel.name() + " version=" + version);
                    }
                    var registrar = event.registrar(version);
                    String absent = "ABSENT \uD83E\uDD14";
                    if (channel.clientAcceptedVersions().test(absent)
                            && channel.serverAcceptedVersions().test(absent)) {
                        registrar = registrar.optional();
                    }
                    registrar.playBidirectional(channel.payloadType(), payloadCodec(channel),
                            Forge1192NetworkBridge::handlePayload,
                            Forge1192NetworkBridge::handlePayload);
                });
    }

    private static StreamCodec<RegistryFriendlyByteBuf, LegacyPayload> payloadCodec(
            LegacyChannel channel) {
        return new StreamCodec<>() {
            @Override
            public LegacyPayload decode(RegistryFriendlyByteBuf buffer) {
                int discriminator = buffer.readUnsignedByte();
                LegacyMessage registration = MESSAGES.get(channel).get(discriminator);
                if (registration == null) {
                    throw new IllegalArgumentException("Invalid legacy discriminator "
                            + discriminator + " on channel " + channel.name());
                }
                Object message = Objects.requireNonNull(registration.decoder().apply(buffer),
                        "Legacy packet decoder returned null for discriminator " + discriminator);
                if (!registration.messageType().isInstance(message)) {
                    throw new IllegalStateException("Legacy packet decoder returned "
                            + message.getClass().getName() + " instead of "
                            + registration.messageType().getName());
                }
                return new LegacyPayload(channel, registration, message);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, LegacyPayload payload) {
                if (payload.channel() != channel) {
                    throw new IllegalArgumentException("Legacy payload channel identity mismatch");
                }
                buffer.writeByte(payload.registration().discriminator() & 0xff);
                payload.registration().encoder().accept(payload.message(), buffer);
            }
        };
    }

    private static void handlePayload(LegacyPayload payload, IPayloadContext context) {
        ServerPlayer sender = context.flow() == PacketFlow.SERVERBOUND
                ? requirePlayer(context) : null;
        LegacyContext legacyContext = new LegacyContext(sender, context::enqueueWork);
        try (var ignored = Forge1192LifecycleBridge.enterRecorded(payload.channel().modId())) {
            payload.registration().consumer().accept(payload.message(), () -> legacyContext);
        } catch (RuntimeException | Error failure) {
            throw failure;
        } catch (Exception impossible) {
            throw new IllegalStateException("Failed to close legacy network lifecycle scope", impossible);
        }
    }

    private static ServerPlayer requirePlayer(IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) return player;
        throw new IllegalStateException("Serverbound legacy payload has no ServerPlayer sender");
    }

    private static <T> T requireArgument(LegacyPacketTarget target, Class<T> type) {
        if (type.isInstance(target.argument())) return type.cast(target.argument());
        throw new IllegalArgumentException("Legacy packet target " + target.kind()
                + " requires " + type.getName() + " but got "
                + (target.argument() == null ? "null" : target.argument().getClass().getName()));
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
