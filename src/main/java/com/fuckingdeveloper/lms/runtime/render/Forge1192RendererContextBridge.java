package com.fuckingdeveloper.lms.runtime.render;

import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;

/** Unchanged context service slots exposed by actual target record accessors. */
public final class Forge1192RendererContextBridge {
    private Forge1192RendererContextBridge(){}
    public static BlockEntityRenderDispatcher blockEntityRenderDispatcher(BlockEntityRendererProvider.Context context){return context.blockEntityRenderDispatcher();}
    public static EntityRenderDispatcher entityRenderer(BlockEntityRendererProvider.Context context){return context.entityRenderer();}
    public static EntityModelSet entityModelSet(BlockEntityRendererProvider.Context context){return context.entityModelSet();}
    public static Font font(BlockEntityRendererProvider.Context context){return context.font();}
}
