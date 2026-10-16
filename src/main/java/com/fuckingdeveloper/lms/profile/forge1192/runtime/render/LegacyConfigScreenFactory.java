package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import java.util.Objects;
import java.util.function.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/** Source function shape, with an explicit native extension implementation. */
public record LegacyConfigScreenFactory(BiFunction<Minecraft, Screen, Screen> screenFunction) implements IConfigScreenFactory {
    public LegacyConfigScreenFactory { Objects.requireNonNull(screenFunction); }
    public LegacyConfigScreenFactory(Function<Screen, Screen> function) { this((client, parent) -> function.apply(parent)); }
    @Override public Screen createScreen(ModContainer container, Screen parent) { return screenFunction.apply(Minecraft.getInstance(), parent); }
}
