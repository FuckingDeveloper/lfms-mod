package com.fuckingdeveloper.lms.target.neoforge263.render;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.render.Forge1192CustomItemBridge;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.render.Forge1192CustomItemRenderer;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.render.LegacyMultiBufferSource;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.world.item.*;
import org.joml.Vector3fc;

/** Late source draw with actual target lighting; unsupported early bounds stay explicit. */
public final class Target263DeferredCustomItem implements SpecialModelRenderer<Target263DeferredCustomItem.Argument> {
    public static final Target263DeferredCustomItem INSTANCE=new Target263DeferredCustomItem();
    public record Argument(Target263CustomItemSnapshot snapshot,ItemDisplayContext display) {}
    private Target263DeferredCustomItem() {}
    public static boolean supports(ItemDisplayContext display) {
        return switch(display) {
            case NONE,FIRST_PERSON_LEFT_HAND,FIRST_PERSON_RIGHT_HAND,THIRD_PERSON_LEFT_HAND,THIRD_PERSON_RIGHT_HAND,HEAD,FIXED -> true;
            default -> false;
        };
    }
    @Override public void submit(Argument argument,PoseStack pose,SubmitNodeCollector output,int light,int overlay,boolean foil,int outline) {
        ItemStack stack=argument.snapshot().materialize();
        var renderer=Forge1192CustomItemBridge.extensions(stack.getItem()).getCustomRenderer();
        draw(renderer,stack,argument.display(),pose,output,light,overlay,outline,Minecraft.getInstance().getAtlasManager());
    }
    /** Executed only at native submission, after final pose/light/overlay are available. */
    public static void draw(Forge1192CustomItemRenderer renderer,ItemStack stack,ItemDisplayContext display,
            PoseStack pose,SubmitNodeCollector output,int light,int overlay,int outline,SpriteGetter sprites) {
        if(!supports(display))throw new UnsupportedOperationException("Deferred source custom item context: "+display);
        var buffers=new LegacyMultiBufferSource(output,pose,sprites);
        renderer.m_108829_(stack,display,pose,buffers,light,overlay);
        buffers.flush(outline);
    }
    public static Vector3fc[] unavailableBounds() {
        throw new UnsupportedOperationException("Deferred source custom item bounds require source geometry before final target lighting is available");
    }
    @Override public void getExtents(java.util.function.Consumer<Vector3fc> output) { unavailableBounds(); }
    @Override public Argument extractArgument(ItemStack stack) {
        throw new UnsupportedOperationException("Source custom item capture requires the native display context");
    }
}
