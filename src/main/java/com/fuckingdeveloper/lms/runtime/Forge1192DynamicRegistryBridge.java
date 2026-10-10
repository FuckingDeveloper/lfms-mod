package com.fuckingdeveloper.lms.runtime;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * Resolves Forge 1.19.2 registry globals that became data-pack/world scoped.
 * The returned object is always a real target Registry from the active
 * registration, server, or client world; LMS never caches a counterfeit
 * process-global registry.
 */
public final class Forge1192DynamicRegistryBridge {
    private Forge1192DynamicRegistryBridge() {}

    public static Registry<?> enchantments() {
        return resolve(Registries.ENCHANTMENT);
    }

    public static Registry<?> paintingVariants() {
        return resolve(Registries.PAINTING_VARIANT);
    }

    private static Registry<?> resolve(ResourceKey<? extends Registry<?>> key) {
        Registry<?> registration = Forge1192RegistrationContext.activeRegistry(key);
        if (registration != null) return registration;

        var server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) return server.registryAccess().lookupOrThrow(key);

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) return minecraft.level.registryAccess().lookupOrThrow(key);

        throw new IllegalStateException("Legacy access to data-driven registry "
                + key.identifier() + " requires an active registration or world context");
    }
}
