package com.fuckingdeveloper.lms.runtime.render;

import java.util.*;
import java.util.function.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.*;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforgespi.language.IModInfo;

/** Native container extensions keep their ownership; source factory tokens project to the native key. */
public final class Forge1192ConfigScreenBridge {
    private Forge1192ConfigScreenBridge() {}
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void registerExtensionPoint(ModContainer container, Class type, Supplier<?> supplier) {
        if (type == LegacyConfigScreenFactory.class) {
            container.registerExtensionPoint(IConfigScreenFactory.class, (Supplier<IConfigScreenFactory>) () -> {
                Object value = Objects.requireNonNull(supplier.get(), "Source config screen supplier returned null");
                if (!(value instanceof LegacyConfigScreenFactory factory)) throw new IllegalStateException("Invalid source config factory " + value);
                return factory;
            });
        } else container.registerExtensionPoint(type, (Supplier)supplier);
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Optional<?> getCustomExtension(ModContainer container, Class type) {
        if (type == LegacyConfigScreenFactory.class) return container.getCustomExtension(IConfigScreenFactory.class).map(factory ->
                factory instanceof LegacyConfigScreenFactory source ? source
                        : new LegacyConfigScreenFactory((client, parent) -> factory.createScreen(container, parent)));
        return container.getCustomExtension(type);
    }
    public static Optional<BiFunction<Minecraft, Screen, Screen>> getScreenFactoryFor(IModInfo info) {
        return ModList.get().getModContainerById(info.getModId()).flatMap(container ->
                getCustomExtension(container, LegacyConfigScreenFactory.class).map(factory -> ((LegacyConfigScreenFactory)factory).screenFunction()));
    }
}
