package com.fuckingdeveloper.lms.target.neoforge263.render;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2f;

/** Native extracted items/primitives snapshot the extractor pose; no GPU-global mutation occurs here. */
public final class Target263GuiModelViewState {
    private final GuiGraphicsExtractor graphics;
    private final Matrix3x2f baseline;
    public Target263GuiModelViewState(GuiGraphicsExtractor graphics){this.graphics=graphics;baseline=new Matrix3x2f(graphics.pose());}
    public void apply(Matrix3x2f relative){graphics.pose().set(baseline).mul(relative);}
    public void restore(){graphics.pose().set(baseline);}
}
