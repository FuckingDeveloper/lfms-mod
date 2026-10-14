package com.fuckingdeveloper.lms.runtime.render;

import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.dispatch.ModelState;

import java.util.Objects;

/** Source-shaped Forge 1.19.2 model transformation state. */
public final class LegacySimpleModelState implements ModelState {
    private final Transformation transformation;
    private final boolean uvLocked;

    public LegacySimpleModelState(Transformation transformation) {
        this(transformation, false);
    }

    public LegacySimpleModelState(Transformation transformation, boolean uvLocked) {
        this.transformation = Objects.requireNonNull(transformation, "transformation");
        this.uvLocked = uvLocked;
    }

    public Transformation getRotation() {
        return transformation;
    }

    @Override
    public Transformation transformation() {
        return transformation;
    }

    public boolean isUvLocked() {
        return uvLocked;
    }
}
