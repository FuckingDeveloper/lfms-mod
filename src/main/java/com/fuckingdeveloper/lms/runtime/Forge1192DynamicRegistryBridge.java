package com.fuckingdeveloper.lms.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * Resolves Forge 1.19.2 registry globals that became data-pack/world scoped.
 * The returned object is always a real target Registry from the active
 * registration, server, or client world; LMS never caches a counterfeit
 * process-global registry.
 */
public final class Forge1192DynamicRegistryBridge {
    private static final ThreadLocal<RegistryAccess> ACTIVE_ACCESS = new ThreadLocal<>();
    private static final Map<Object, Set<String>> DEFERRED =
            Collections.synchronizedMap(new IdentityHashMap<>());

    private Forge1192DynamicRegistryBridge() {}

    public static Registry<?> enchantments() {
        return resolve(Registries.ENCHANTMENT);
    }

    public static Registry<?> paintingVariants() {
        return resolve(Registries.PAINTING_VARIANT);
    }

    public static void populateSimpleTreeBlockMapping(Map<Block, Block> mapping) {
        mapping.clear();
        for (Object value : resolve(Registries.FEATURE)) {
            if (!(value instanceof TreeFeature tree)) continue;
            if (!(tree.trunkProvider().value() instanceof SimpleStateProvider trunk)) continue;
            if (!(tree.foliageProvider().value() instanceof SimpleStateProvider foliage)) continue;
            mapping.put(trunk.state().getBlock(), foliage.state().getBlock());
        }
    }

    public static void deferConfiguredFeatureInitialization(
            Object receiver, Class<?> owner, String methodName) {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            invokeScoped(Forge1192LifecycleBridge.requireActive().modId(), receiver,
                    owner, methodName, server.registryAccess());
            return;
        }
        String key = owner.getName() + '#' + methodName;
        synchronized (DEFERRED) {
            if (!DEFERRED.computeIfAbsent(receiver, ignored -> new HashSet<>()).add(key)) return;
        }
        String modId = Forge1192LifecycleBridge.requireActive().modId();
        NeoForge.EVENT_BUS.addListener((ServerAboutToStartEvent event) ->
                invokeScoped(modId, receiver, owner, methodName, event.getServer().registryAccess()));
    }

    private static void invokeScoped(
            String modId, Object receiver, Class<?> owner, String methodName,
            RegistryAccess registryAccess) {
        RegistryAccess previous = ACTIVE_ACCESS.get();
        ACTIVE_ACCESS.set(registryAccess);
        try (var lifecycle = Forge1192LifecycleBridge.enterRecorded(modId);
             var ignored = Forge1192RegistrationContext.enterMod(modId)) {
            Method method = owner.getDeclaredMethod(methodName);
            if (!method.canAccess(receiver)) method.setAccessible(true);
            method.invoke(receiver);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtime) throw runtime;
            if (cause instanceof Error error) throw error;
            throw new IllegalStateException("Deferred configured-feature initialization failed", cause);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot invoke configured-feature initializer "
                    + owner.getName() + '#' + methodName, e);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot close configured-feature initializer scope", e);
        } finally {
            if (previous == null) ACTIVE_ACCESS.remove();
            else ACTIVE_ACCESS.set(previous);
        }
    }

    private static Registry<?> resolve(ResourceKey<? extends Registry<?>> key) {
        RegistryAccess active = ACTIVE_ACCESS.get();
        if (active != null) return active.lookupOrThrow(key);
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
