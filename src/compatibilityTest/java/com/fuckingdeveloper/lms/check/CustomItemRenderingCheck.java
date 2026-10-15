package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.runtime.render.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.world.item.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

final class CustomItemRenderingCheck {
    static void run() throws Exception {
        var buffers = LegacyMultiBufferSource.recording();
        buffers.getBuffer(LegacyRenderType.m_110452_(net.minecraft.resources.Identifier.parse("probe:texture")))
                .addVertex(-2, 3, 4).addVertex(5, -6, 7);
        var geometry = buffers.finishRecording();
        var bounds = geometry.extents();
        check(bounds.length == 2 && bounds[0].x() == -2 && bounds[1].y() == -6, "actual source geometry bounds lost");
        ((org.joml.Vector3f) bounds[0]).set(100, 100, 100);
        check(geometry.extents()[0].x() == -2, "submitted geometry retained mutable bounds");
        NativeDefaultComponentFixture.run(() -> {
            var properties = new net.minecraft.client.renderer.item.ModelRenderProperties(false, null,
                    net.minecraft.client.resources.model.cuboid.ItemTransforms.NO_TRANSFORMS);
            var model = new Target263LegacyGuiItemModel(properties, new org.joml.Matrix4f(), null);
            var sameProperties = new Target263LegacyGuiItemModel(properties, new org.joml.Matrix4f(), null);
            check(model.equals(sameProperties), "regression fixture must reproduce equal display properties");
            var first = new net.minecraft.client.renderer.item.TrackingItemStackRenderState();
            var second = new net.minecraft.client.renderer.item.TrackingItemStackRenderState();
            var stack = com.fuckingdeveloper.lms.runtime.Forge1192EarlyItemStackBridge.create(Items.STICK);
            var otherBuffers = LegacyMultiBufferSource.recording();
            otherBuffers.getBuffer(LegacyRenderType.m_110452_(net.minecraft.resources.Identifier.parse("probe:other_texture"))).addVertex(9, 8, 7);
            model.installGeometry(first, stack, geometry);
            sameProperties.installGeometry(second, stack, otherBuffers.finishRecording());
            check(!first.getModelIdentity().equals(second.getModelIdentity()), "native GUI atlas aliases distinct extracted source geometry/materials");
            check(first.isAnimated() && second.isAnimated(), "source callback results cached across frames");
        });
        try { buffers.getBuffer(LegacyRenderType.m_110452_(net.minecraft.resources.Identifier.parse("probe:texture")));
            throw new AssertionError("finished geometry remained mutable"); } catch (IllegalStateException expected) {}

        Path artifact = Path.of("legacy-mods/ironchest-1.19.2-14.2.7.jar");
        if (Files.isRegularFile(artifact)) try (var loader = new ManagedLegacyClassLoader(artifact,
                CustomItemRenderingCheck.class.getClassLoader(), new Forge1192LegacyClassTransformer())) {
            Forge1192CustomItemBridge.verifyRegistration(loader, "com.progwml6.ironchest.common.item.IronChestBlockItem");
            var extension = loader.loadClass("com.progwml6.ironchest.common.item.IronChestBlockItem$1");
            check(Forge1192ItemExtensions.class.isAssignableFrom(extension), "source extension interface not normalized");
            check(extension.getMethod("getCustomRenderer").getReturnType() == Forge1192CustomItemRenderer.class,
                    "source extension return descriptor not normalized");
            String name = "com.progwml6.ironchest.client.model.inventory.IronChestItemStackRenderer";
            var renderer = loader.loadClass(name);
            check(renderer.getSuperclass() == Forge1192CustomItemRenderer.class, "source renderer superclass lost");
            renderer.getMethod("m_108829_", ItemStack.class, ItemDisplayContext.class,
                    com.mojang.blaze3d.vertex.PoseStack.class, LegacyMultiBufferSource.class, int.class, int.class);
            var node = new ClassNode(); new ClassReader(loader.definedClassBytes(name)).accept(node, 0);
            var calls = node.methods.stream().flatMap(m -> Arrays.stream(m.instructions.toArray()))
                    .filter(i -> i instanceof MethodInsnNode).map(i -> (MethodInsnNode)i).toList();
            check(calls.stream().anyMatch(c -> c.getOpcode() == Opcodes.INVOKESTATIC && c.owner.equals(
                    "com/fuckingdeveloper/lms/runtime/render/Forge1192CustomItemBridge") && c.name.equals("renderItem")),
                    "source dispatcher callback not migrated");
            check(calls.stream().noneMatch(c -> c.name.equals("m_112272_")), "removed dispatcher ABI retained");
            String ber = "com.progwml6.ironchest.client.render.IronChestRenderer";
            loader.loadClass(ber);
            var sourceBer = new ClassNode(); new ClassReader(loader.definedClassBytes(ber)).accept(sourceBer, 0);
            var operations = sourceBer.methods.stream().flatMap(m -> Arrays.stream(m.instructions.toArray())).toList();
            check(operations.stream().noneMatch(i -> i instanceof FieldInsnNode f && f.name.equals("f_112249_")),
                    "removed source dispatcher camera retained");
            check(operations.stream().anyMatch(i -> i instanceof MethodInsnNode c && c.name.equals("cameraPosition")
                    && c.owner.equals("com/fuckingdeveloper/lms/runtime/render/Forge1192CustomItemBridge")), "source camera position observation lost");
            check(operations.stream().noneMatch(i -> i instanceof MethodInsnNode c && (c.name.equals("m_91291_") || c.name.equals("m_174269_"))), "removed nested source renderer API retained");
            check(operations.stream().anyMatch(i -> i instanceof MethodInsnNode c && c.name.equals("renderStatic")
                    && c.owner.equals("com/fuckingdeveloper/lms/runtime/render/Forge1192NestedItemBridge")), "nested source items silently omitted");
            String itemClass="com.progwml6.ironchest.common.item.IronChestBlockItem";
            loader.loadClass(itemClass);var named=new ClassNode();new ClassReader(loader.definedClassBytes(itemClass)).accept(named,0);
            check(named.methods.stream().flatMap(m->Arrays.stream(m.instructions.toArray())).anyMatch(i->i instanceof MethodInsnNode c
                    && c.name.equals("withBlockItemDescription")),"ordinary source BlockItem subclass lost block name intent");
            var source = new ClassNode(); new ClassReader(loader.originalClassBytes(name)).accept(source, 0);
            var reference = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "probeHandle", "()Ljava/lang/invoke/MethodHandle;", null, null);
            reference.instructions.add(new LdcInsnNode(new Handle(Opcodes.H_INVOKEVIRTUAL,
                    "net/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher", "m_112272_",
                    "(Lnet/minecraft/world/level/block/entity/BlockEntity;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)Z", false)));
            reference.instructions.add(new InsnNode(Opcodes.ARETURN)); reference.maxStack = 1;
            source.methods.add(reference);
            var writer = new ClassWriter(0); source.accept(writer);
            var transformed = new Forge1192LegacyClassTransformer().transform(name, writer.toByteArray());
            var withHandle = new ClassNode(); new ClassReader(transformed.bytes()).accept(withHandle, 0);
            var handle = (Handle)((LdcInsnNode)withHandle.methods.stream().filter(m -> m.name.equals("probeHandle"))
                    .findFirst().orElseThrow().instructions.getFirst()).cst;
            check(handle.getTag() == Opcodes.H_INVOKESTATIC && handle.getName().equals("renderItem")
                    && handle.getDesc().contains("LegacyMultiBufferSource"), "dispatcher reference handle ABI lost");
            var render = source.methods.stream().filter(m -> m.name.equals("m_108829_")).findFirst().orElseThrow();
            render.instructions.insert(new MethodInsnNode(Opcodes.INVOKESPECIAL,
                    "net/minecraft/client/renderer/BlockEntityWithoutLevelRenderer", "unsupportedBase", "()V", false));
            writer = new ClassWriter(0); source.accept(writer);
            try { new Forge1192LegacyClassTransformer().transform(name, writer.toByteArray());
                throw new AssertionError("unproven vanilla fallback admitted"); } catch (UnsupportedOperationException expected) {}

            var outer = new ClassNode(); String outerName = "com.progwml6.ironchest.common.item.IronChestBlockItem";
            new ClassReader(loader.originalClassBytes(outerName)).accept(outer, 0);
            outer.methods.stream().filter(m -> m.name.equals("initializeClient")).findFirst().orElseThrow().instructions
                    .insert(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/System", "gc", "()V", false));
            writer = new ClassWriter(0); outer.accept(writer);
            Path impure = Files.createTempFile("lms-impure-item-", ".jar");
            try {
                try (var zip = new java.util.jar.JarOutputStream(Files.newOutputStream(impure))) {
                    zip.putNextEntry(new java.util.jar.JarEntry(outerName.replace('.', '/') + ".class"));
                    zip.write(writer.toByteArray()); zip.closeEntry();
                }
                try (var rejected = new ManagedLegacyClassLoader(impure, CustomItemRenderingCheck.class.getClassLoader(), new Forge1192LegacyClassTransformer())) {
                    try { Forge1192CustomItemBridge.verifyRegistration(rejected, outerName);
                        throw new AssertionError("observable lazy initialization admitted"); } catch (UnsupportedOperationException expected) {}
                }
            } finally { Files.deleteIfExists(impure); }
        }
        System.out.println("PASS: immutable source custom-renderer/extension linkage, original pure registration proof, dispatcher migration and immutable geometry bounds");
    }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
