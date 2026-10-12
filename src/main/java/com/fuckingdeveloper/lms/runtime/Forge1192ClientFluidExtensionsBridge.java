package com.fuckingdeveloper.lms.runtime;

import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/** Preserves source-defined Forge 1.19.2 fluid client extension values. */
public final class Forge1192ClientFluidExtensionsBridge {
    private static final ClassValue<Accessors> ACCESSORS = new ClassValue<>() {
        @Override
        protected Accessors computeValue(Class<?> type) {
            return new Accessors(
                    find(type, "getStillTexture", 0),
                    find(type, "getStillTexture", 1),
                    find(type, "getFlowingTexture", 0),
                    find(type, "getFlowingTexture", 1),
                    find(type, "getOverlayTexture", 0),
                    find(type, "getOverlayTexture", 1),
                    find(type, "getTintColor", 0),
                    find(type, "getTintColor", 1));
        }
    };

    private Forge1192ClientFluidExtensionsBridge() {}

    public static Identifier getStillTexture(FluidState state) {
        Object extensions = IClientFluidTypeExtensions.of(state);
        Method sourceAccessor = ACCESSORS.get(extensions.getClass()).still();
        if (sourceAccessor != null) return texture(extensions, sourceAccessor, "still");
        return Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(state)
                .stillMaterial().sprite().contents().name();
    }

    public static Identifier getStillTexture(Fluid fluid) {
        return getStillTexture(fluid.defaultFluidState());
    }

    public static Identifier getFlowingTexture(FluidState state) {
        Object extensions = IClientFluidTypeExtensions.of(state);
        Method sourceAccessor = ACCESSORS.get(extensions.getClass()).flowing();
        if (sourceAccessor != null) return texture(extensions, sourceAccessor, "flowing");
        return Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(state)
                .flowingMaterial().sprite().contents().name();
    }

    public static Identifier getFlowingTexture(Fluid fluid) {
        return getFlowingTexture(fluid.defaultFluidState());
    }

    public static Identifier getStillTexture(Object extensions) {
        return texture(extensions, ACCESSORS.get(extensions.getClass()).still(), "still");
    }

    public static Identifier getStillTexture(Object extensions, Object stack) {
        Accessors accessors = ACCESSORS.get(extensions.getClass());
        Method method = accessors.stillWithStack() != null
                ? accessors.stillWithStack() : accessors.still();
        return texture(extensions, method, "still", stack);
    }

    public static Identifier getFlowingTexture(Object extensions) {
        return texture(extensions, ACCESSORS.get(extensions.getClass()).flowing(), "flowing");
    }

    public static Identifier getFlowingTexture(Object extensions, Object stack) {
        Accessors accessors = ACCESSORS.get(extensions.getClass());
        Method method = accessors.flowingWithStack() != null
                ? accessors.flowingWithStack() : accessors.flowing();
        return texture(extensions, method, "flowing", stack);
    }

    public static Identifier getOverlayTexture(Object extensions) {
        return nullableTexture(extensions, ACCESSORS.get(extensions.getClass()).overlay());
    }

    public static Identifier getOverlayTexture(Object extensions, Object stack) {
        Accessors accessors = ACCESSORS.get(extensions.getClass());
        Method method = accessors.overlayWithStack() != null
                ? accessors.overlayWithStack() : accessors.overlay();
        return nullableTexture(extensions, method, stack);
    }

    public static int getTintColor(Object extensions) {
        Method method = ACCESSORS.get(extensions.getClass()).tint();
        return method == null ? 0xFFFFFFFF : (Integer) invoke(extensions, method);
    }

    public static int getTintColor(Object extensions, Object stack) {
        Accessors accessors = ACCESSORS.get(extensions.getClass());
        Method method = accessors.tintWithStack() != null
                ? accessors.tintWithStack() : accessors.tint();
        return method == null ? 0xFFFFFFFF : (Integer) invoke(extensions, method, stack);
    }

    private static Identifier texture(Object receiver, Method method, String kind, Object... argument) {
        Identifier result = nullableTexture(receiver, method, argument);
        if (result == null) {
            throw new IllegalStateException("Legacy fluid extension provides no " + kind
                    + " texture: " + receiver.getClass().getName());
        }
        return result;
    }

    private static Identifier nullableTexture(Object receiver, Method method, Object... argument) {
        if (method == null) return null;
        Object value = method.getParameterCount() == 0
                ? invoke(receiver, method) : invoke(receiver, method, argument);
        if (value == null) return null;
        if (value instanceof Identifier identifier) return identifier;
        throw new IllegalStateException("Legacy fluid texture has unexpected type: "
                + value.getClass().getName());
    }

    private static Object invoke(Object receiver, Method method, Object... arguments) {
        try {
            return method.invoke(receiver, arguments);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtime) throw runtime;
            if (cause instanceof Error error) throw error;
            throw new IllegalStateException("Legacy fluid client extension failed", cause);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot invoke legacy fluid client extension", e);
        }
    }

    private static Method find(Class<?> type, String name, int parameterCount) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (!method.getName().equals(name) || method.getParameterCount() != parameterCount) continue;
                if (!method.trySetAccessible()) {
                    throw new IllegalStateException("Cannot access legacy fluid extension method " + method);
                }
                return method;
            }
        }
        return null;
    }

    private record Accessors(
            Method still, Method stillWithStack,
            Method flowing, Method flowingWithStack,
            Method overlay, Method overlayWithStack,
            Method tint, Method tintWithStack) {}
}
