package com.fuckingdeveloper.lms.runtime;

import net.minecraft.resources.Identifier;

import java.util.Objects;
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
        return new LegacyChannel(modId, name, protocolVersion, clientAcceptedVersions, serverAcceptedVersions);
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
}
