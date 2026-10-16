package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.render.Forge1192CustomItemRenderer;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.render.LegacyMultiBufferSource;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.render.LegacyRenderType;
import com.fuckingdeveloper.lms.target.neoforge263.render.Target263GroundItemExtraction;
import com.fuckingdeveloper.lms.target.neoforge263.render.Target263LegacyGuiItemModel;


import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.world.item.*;
import org.joml.*;
import java.lang.reflect.Proxy;
import java.util.*;

final class GroundCustomItemCheck {
    static void run() {
        NativeDefaultComponentFixture.run(() -> {
            var stack = com.fuckingdeveloper.lms.profile.forge1192.runtime.item.Forge1192EarlyItemStackBridge.create(Items.STICK);
            var state = new ItemStackRenderState();
            new ItemModelResolver(null) {
                @Override public void appendItemLayers(ItemStackRenderState output, ItemStack input, ItemDisplayContext display,
                        net.minecraft.world.level.Level level, net.minecraft.world.entity.ItemOwner owner, int seed) {}
            }.updateForTopItem(state, stack, ItemDisplayContext.GROUND, null, null, 0);
            var nested = new ItemStackRenderState();
            reject(() -> Target263GroundItemExtraction.requireLight(state, stack, null));
            Target263GroundItemExtraction.extract(state, stack, null, 0x123456, 0, () -> {
                check(Target263GroundItemExtraction.requireLight(state, stack, null) == 0x123456, "native light lost");
                reject(() -> Target263GroundItemExtraction.requireLight(nested, stack, null));
                reject(() -> Target263GroundItemExtraction.requireLight(state, stack.copy(), null));
                try {
                    Target263GroundItemExtraction.extract(nested, stack, null, 42, 0, () -> {
                        check(Target263GroundItemExtraction.requireLight(nested, stack, null) == 42, "nested scope lost");
                        throw new IllegalStateException("probe");
                    });
                } catch (IllegalStateException expected) {}
                check(Target263GroundItemExtraction.requireLight(state, stack, null) == 0x123456, "failed nested scope leaked");
            });
            reject(() -> Target263GroundItemExtraction.requireLight(state, stack, null));
            Target263GroundItemExtraction.extract(state, stack, null, 1, 1,
                    () -> check(Target263GroundItemExtraction.requireLight(state, stack, null) == 1, "outline lost native light"));

            Target263GroundItemExtraction.extractShelf(state,stack,null,0x234567,()->{
                check(Target263GroundItemExtraction.requireShelfLight(state,stack,null)==0x234567,"shelf light lost");
                reject(()->Target263GroundItemExtraction.requireLight(state,stack,null));
                reject(()->Target263GroundItemExtraction.requireShelfLight(nested,stack,null));
            });
            reject(()->Target263GroundItemExtraction.requireShelfLight(state,stack,null));
            net.minecraft.client.renderer.blockentity.ShelfRenderer.class.getDeclaredMethods();

            int[] calls = {0};
            var renderer = new Forge1192CustomItemRenderer(null, null) {
                public void m_108829_(ItemStack input, ItemDisplayContext display, PoseStack pose, LegacyMultiBufferSource buffers, int light, int overlay) {
                    calls[0]++;
                    check(display == ItemDisplayContext.GROUND && light == 0x123456 && overlay == OverlayTexture.NO_OVERLAY, "ground source inputs changed");
                    input.setCount(9);
                    pose.translate(2, 3, 4);
                    var vertex = buffers.getBuffer(LegacyRenderType.m_110452_(net.minecraft.resources.Identifier.parse("probe:ground")));
                    vertex.addVertex(pose.last(), -1, -2, -3).setUv2(light & 65535, light >>> 16);
                    vertex.addVertex(pose.last(), 5, 6, 7);
                }
            };
            var geometry = Target263LegacyGuiItemModel.recordGeometry(renderer, stack, ItemDisplayContext.GROUND, 0x123456, id -> null);
            check(stack.getCount() == 1 && calls[0] == 1, "recording mutated input or repeated callback");
            var unchanged = ItemTransform.NO_TRANSFORM;
            var ground = new ItemTransform(new Vector3f(), new Vector3f(3,4,5), new Vector3f(1,1,1));
            var transforms = new ItemTransforms(unchanged, unchanged, unchanged, unchanged, unchanged, unchanged, ground, unchanged, unchanged);
            var model = new Target263LegacyGuiItemModel(new ModelRenderProperties(false, null, transforms), new Matrix4f().scaling(2), null);
            model.installGeometry(state, stack, geometry, ItemDisplayContext.GROUND);
            var bounds = state.getModelBoundingBox();
            check(bounds.minX == 4.5 && bounds.minY == 5.5 && bounds.minZ == 6.5
                    && bounds.maxX == 16.5 && bounds.maxY == 21.5 && bounds.maxZ == 26.5, "actual geometry/model transformed bounds lost: " + bounds);
            var positions = new ArrayList<Vector3f>();
            var lightValues = new ArrayList<List<Integer>>();
            var colors = new ArrayList<Integer>();
            var types = new ArrayList<net.minecraft.client.renderer.rendertype.RenderType>();
            var consumer = (VertexConsumer) Proxy.newProxyInstance(GroundCustomItemCheck.class.getClassLoader(), new Class[]{VertexConsumer.class}, (p,m,a) -> {
                if(m.getName().equals("addVertex")) {
                    if(a.length == 4 && a[0] instanceof PoseStack.Pose vertexPose)
                        positions.add(new Vector3f((float)a[1], (float)a[2], (float)a[3]).mulPosition(vertexPose.pose()));
                    else positions.add(new Vector3f((float)a[0], (float)a[1], (float)a[2]));
                }
                if(m.getName().equals("setColor")) colors.add((int)a[0]);
                if(m.getName().equals("setUv2")) lightValues.add(List.of((int)a[0], (int)a[1]));
                return p;
            });
            var collector = (SubmitNodeCollector) Proxy.newProxyInstance(GroundCustomItemCheck.class.getClassLoader(), new Class[]{SubmitNodeCollector.class}, (p,m,a) -> {
                check(m.getName().equals("submitCustomGeometry"), "native geometry submission bypassed");
                types.add((net.minecraft.client.renderer.rendertype.RenderType)a[1]);
                ((SubmitNodeCollector.CustomGeometryRenderer)a[2]).render(((PoseStack)a[0]).last(), consumer);
                return null;
            });
            var pose = new PoseStack(); pose.translate(10, 20, 30);
            state.submit(pose, collector, 0x123456, OverlayTexture.NO_OVERLAY, 0);
            state.submit(pose, collector, 0x123456, OverlayTexture.NO_OVERLAY, 0);
            check(calls[0] == 1 && positions.size() == 4, "stack submission repeated source callback");
            check(positions.getFirst().equals(new Vector3f(14.5F, 25.5F, 36.5F)), "ground vertex transformed twice");
            check(lightValues.equals(List.of(List.of(0x3456, 0x12), List.of(0x3456, 0x12))), "recorded light lost");
            state.submit(pose, collector, 0x123456, OverlayTexture.NO_OVERLAY, 0xFF123456);
            check(calls[0] == 1 && positions.size() == 8, "outline repeated source extraction or omitted normal geometry");
            check(types.getLast() == types.getFirst().outline().orElseThrow(), "native outline type not selected");
            check(colors.equals(List.of(0xFF123456, 0xFF123456)), "outline color not enforced per vertex");
            var noOutline = LegacyMultiBufferSource.recording();
            noOutline.getBuffer(LegacyRenderType.m_110488_(net.minecraft.resources.Identifier.parse("probe:eyes")))
                    .addVertex(0,0,0).setColor(-1);
            int previousTypes = types.size();
            noOutline.finishRecording().submit(collector, pose, 0xFF123456);
            check(types.size() == previousTypes + 1 && !types.getLast().isOutline(), "non-outline material lost normal draw or participated in outline");
            check(pose.last().pose().equals(new Matrix4f().translation(10,20,30)), "native parent pose leaked");
        });
        System.out.println("PASS: GROUND native light scope, mismatch rejection, native outline materials/colors, nested failure cleanup, single source callback, isolated stack, actual transformed bounds and repeated native submission");
    }
    private static void reject(Runnable action) {
        try { action.run(); throw new AssertionError("unsupported ground extraction admitted"); }
        catch (UnsupportedOperationException expected) {}
    }
    private static void check(boolean value, String reason) { if (!value) throw new AssertionError(reason); }
}
