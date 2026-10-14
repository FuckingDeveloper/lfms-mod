package com.fuckingdeveloper.lms.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/** Runs source-era early client initialization when target client state exists. */
public final class Forge1192ClientLifecycleBridge {
    private static final Map<Object, Set<String>> PENDING =
            Collections.synchronizedMap(new IdentityHashMap<>());
    private static final Map<Object, Set<String>> PENDING_FIELDS =
            Collections.synchronizedMap(new IdentityHashMap<>());

    private Forge1192ClientLifecycleBridge() {}

    public static void runWhenReady(Object receiver, String helperName) {
        if (clientReady()) {
            invoke(receiver, helperName);
            return;
        }

        var lifecycle = Forge1192LifecycleBridge.requireActive();
        synchronized (PENDING) {
            if (!PENDING.computeIfAbsent(receiver, ignored -> new HashSet<>()).add(helperName)) return;
        }
        String modId = lifecycle.modId();
        lifecycle.modEventBus().addListener(
                EventPriority.HIGHEST, false, FMLClientSetupEvent.class,
                event -> invokeScoped(modId, receiver, helperName));
    }

    public static void populateMinecraftFieldWhenReady(Object receiver, String fieldName) {
        if (clientReady()) {
            populateField(receiver, fieldName);
            return;
        }

        var lifecycle = Forge1192LifecycleBridge.requireActive();
        synchronized (PENDING_FIELDS) {
            if (!PENDING_FIELDS.computeIfAbsent(receiver, ignored -> new HashSet<>()).add(fieldName)) return;
        }
        String modId = lifecycle.modId();
        lifecycle.modEventBus().addListener(
                EventPriority.HIGHEST, false, FMLClientSetupEvent.class,
                event -> populateFieldScoped(modId, receiver, fieldName));
    }

    private static boolean clientReady() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft != null && minecraft.getWindow() != null && minecraft.options != null;
    }

    /** Returns the active client player, or null while client construction is incomplete. */
    public static net.minecraft.client.player.LocalPlayer currentPlayer() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft == null ? null : minecraft.player;
    }

    /** Resolve a source-era cached singleton after target client construction. */
    public static Minecraft currentMinecraft(Minecraft cached) {
        if (cached != null) return cached;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            throw new IllegalStateException(
                    "Target Minecraft singleton is unavailable at legacy client callback");
        }
        return minecraft;
    }

    private static void invokeScoped(String modId, Object receiver, String helperName) {
        synchronized (PENDING) {
            Set<String> methods = PENDING.get(receiver);
            if (methods == null || !methods.remove(helperName)) return;
            if (methods.isEmpty()) PENDING.remove(receiver);
        }
        try (var lifecycle = Forge1192LifecycleBridge.enterRecorded(modId);
             var ignored = Forge1192RegistrationContext.enterMod(modId)) {
            if (!clientReady()) {
                throw new IllegalStateException(
                        "Target client state is unavailable during FMLClientSetupEvent");
            }
            invoke(receiver, helperName);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to close deferred legacy client scope", e);
        }
    }

    private static void populateFieldScoped(String modId, Object receiver, String fieldName) {
        synchronized (PENDING_FIELDS) {
            Set<String> fields = PENDING_FIELDS.get(receiver);
            if (fields == null || !fields.remove(fieldName)) return;
            if (fields.isEmpty()) PENDING_FIELDS.remove(receiver);
        }
        try (var lifecycle = Forge1192LifecycleBridge.enterRecorded(modId);
             var ignored = Forge1192RegistrationContext.enterMod(modId)) {
            if (!clientReady()) {
                throw new IllegalStateException(
                        "Target client state is unavailable during FMLClientSetupEvent");
            }
            populateField(receiver, fieldName);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to close deferred legacy client scope", e);
        }
    }

    private static void populateField(Object receiver, String fieldName) {
        try {
            Field field = receiver.getClass().getDeclaredField(fieldName);
            if (field.getType() != Minecraft.class) {
                throw new IllegalStateException("Deferred field is not a Minecraft field: " + field);
            }
            if (!field.canAccess(receiver)) field.setAccessible(true);
            field.set(receiver, Minecraft.getInstance());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot populate deferred Minecraft field " + fieldName, e);
        }
    }

    private static void invoke(Object receiver, String helperName) {
        try {
            Method method = receiver.getClass().getDeclaredMethod(helperName);
            if (!method.canAccess(receiver)) method.setAccessible(true);
            method.invoke(receiver);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtime) throw runtime;
            if (cause instanceof Error error) throw error;
            throw new IllegalStateException("Deferred legacy client initialization failed", cause);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Cannot invoke deferred legacy client initialization " + helperName, e);
        }
    }
}
