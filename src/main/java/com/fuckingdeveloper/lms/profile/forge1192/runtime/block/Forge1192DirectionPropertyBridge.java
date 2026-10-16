package com.fuckingdeveloper.lms.profile.forge1192.runtime.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/** Preserves the removed Forge 1.19.2 DirectionProperty factory surface. */
public final class Forge1192DirectionPropertyBridge {
    private Forge1192DirectionPropertyBridge() {}

    public static EnumProperty<Direction> create(String name, Predicate<Direction> filter) {
        return EnumProperty.create(name, Direction.class, filter);
    }

    public static EnumProperty<Direction> create(String name, Collection<Direction> values) {
        return EnumProperty.create(name, Direction.class, List.copyOf(values));
    }
}
