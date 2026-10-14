package com.fuckingdeveloper.lms.runtime;

import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/** Preserves source-defined Forge 1.19.2 fluid client extension values. */
public final class Forge1192ClientFluidExtensionsBridge {
    private static final Map<net.neoforged.neoforge.fluids.FluidType, Object>
            LEGACY_EXTENSIONS = Collections.synchronizedMap(new WeakHashMap<>());
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
        Object extensions = sourceOrTargetExtensions(state);
        Method sourceAccessor = ACCESSORS.get(extensions.getClass()).still();
        if (sourceAccessor != null) return texture(extensions, sourceAccessor, "still");
        Identifier vanilla = vanillaTexture(state.getType(), true);
        if (vanilla != null) return vanilla;
        try {
            return Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(state)
                    .stillMaterial().sprite().contents().name();
        } catch (NullPointerException unavailable) {
            throw unavailableFluidModel("still", state, extensions, unavailable);
        }
    }

    public static Identifier getStillTexture(Fluid fluid) {
        return getStillTexture(fluid.defaultFluidState());
    }

    public static Identifier getFlowingTexture(FluidState state) {
        Object extensions = sourceOrTargetExtensions(state);
        Method sourceAccessor = ACCESSORS.get(extensions.getClass()).flowing();
        if (sourceAccessor != null) return texture(extensions, sourceAccessor, "flowing");
        Identifier vanilla = vanillaTexture(state.getType(), false);
        if (vanilla != null) return vanilla;
        try {
            return Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(state)
                    .flowingMaterial().sprite().contents().name();
        } catch (NullPointerException unavailable) {
            throw unavailableFluidModel("flowing", state, extensions, unavailable);
        }
    }

    public static Identifier getFlowingTexture(Fluid fluid) {
        return getFlowingTexture(fluid.defaultFluidState());
    }

    public static int getTintColor(FluidState state) {
        return getTintColor(sourceOrTargetExtensions(state));
    }

    public static int getTintColor(Fluid fluid) {
        return getTintColor(fluid.defaultFluidState());
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

    /** Recover the exact source initializeClient registration intent on demand. */
    private static Object sourceOrTargetExtensions(FluidState state) {
        var type = state.getType().getFluidType();
        Object cached = LEGACY_EXTENSIONS.get(type);
        if (cached != null) return cached;

        Method initializer = findInitializer(type.getClass());
        if (initializer == null) return IClientFluidTypeExtensions.of(state);

        AtomicReference<Object> registered = new AtomicReference<>();
        Consumer<Object> capture = value -> {
            if (value == null || !registered.compareAndSet(null, value)) {
                throw new IllegalStateException(
                        "Legacy fluid initializeClient must register exactly one extension");
            }
        };
        invoke(type, initializer, capture);
        Object extension = registered.get();
        if (extension == null) {
            throw new IllegalStateException("Legacy fluid initializeClient registered no extension: "
                    + type.getClass().getName());
        }
        LEGACY_EXTENSIONS.put(type, extension);
        return extension;
    }

    private static Method findInitializer(Class<?> type) {
        for (Class<?> current = type;
                current != null && current != net.neoforged.neoforge.fluids.FluidType.class;
                current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (!method.getName().equals("initializeClient")
                        || method.getReturnType() != void.class
                        || method.getParameterCount() != 1
                        || method.getParameterTypes()[0] != Consumer.class) continue;
                if (!method.trySetAccessible()) {
                    throw new IllegalStateException(
                            "Cannot access legacy fluid client initializer " + method);
                }
                return method;
            }
        }
        return null;
    }

    private static IllegalStateException unavailableFluidModel(
            String kind, FluidState state, Object extensions, NullPointerException cause) {
        return new IllegalStateException("Target fluid model is unavailable during legacy "
                + kind + " texture lookup: fluid="
                + net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(state.getType())
                + " fluidClass=" + state.getType().getClass().getName()
                + " fluidTypeClass=" + state.getType().getFluidType().getClass().getName()
                + " extensionsClass=" + extensions.getClass().getName(), cause);
    }

    private static Identifier vanillaTexture(Fluid fluid, boolean still) {
        if (fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER) {
            return Identifier.withDefaultNamespace(still ? "block/water_still" : "block/water_flow");
        }
        if (fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA) {
            return Identifier.withDefaultNamespace(still ? "block/lava_still" : "block/lava_flow");
        }
        return null;
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
