package com.fuckingdeveloper.lms.runtime.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

/** Extract source nested items now, submit their isolated native state later. */
public final class Forge1192NestedItemBridge {
    private Forge1192NestedItemBridge() {}
    public static ItemModelResolver resolver(Minecraft minecraft) { return minecraft.getItemModelResolver(); }
    public static void renderStatic(ItemModelResolver resolver, ItemStack stack, ItemDisplayContext display,
            int light, int overlay, PoseStack pose, LegacyMultiBufferSource buffers, int seed) {
        renderStatic(resolver,null,stack,display,false,pose,buffers,null,light,overlay,seed);
    }
    public static void renderStatic(ItemModelResolver resolver, LivingEntity owner, ItemStack stack, ItemDisplayContext display,
            boolean leftHand, PoseStack pose, LegacyMultiBufferSource buffers, Level level, int light, int overlay, int seed) {
        if(stack.isEmpty())return;
        if(leftHand!=display.leftHand())throw new UnsupportedOperationException("Source item handedness differs from native display context");
        buffers.recordItem(resolver,stack,display,pose,level,owner,light,overlay,seed);
    }
}
