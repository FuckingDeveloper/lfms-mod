package com.fuckingdeveloper.lms.runtime;

import net.minecraft.resources.Identifier;

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

    public record LegacyMessage(
            int discriminator,
            Class<?> messageType,
            BiConsumer<Object, Object> encoder,
            Function<Object, Object> decoder,
            BiConsumer<Object, Supplier<Object>> consumer) {}

    private static final ConcurrentHashMap<LegacyChannel, ConcurrentHashMap<Integer, LegacyMessage>> MESSAGES =
            new ConcurrentHashMap<>();

    public static java.util.List<LegacyMessage> messages(LegacyChannel channel) {
        var entries = MESSAGES.get(Objects.requireNonNull(channel, "channel"));
        if (entries == null) throw new IllegalStateException("Unknown legacy network channel");
        return entries.values().stream()
                .sorted(java.util.Comparator.comparingInt(LegacyMessage::discriminator))
                .toList();
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
        LegacyMessage message = new LegacyMessage(discriminator, messageType, encoder, decoder, consumer);
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
