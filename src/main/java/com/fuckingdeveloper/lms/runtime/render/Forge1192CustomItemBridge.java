package com.fuckingdeveloper.lms.runtime.render;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.mojang.blaze3d.vertex.PoseStack;
import java.lang.invoke.*;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public final class Forge1192CustomItemBridge {
    private Forge1192CustomItemBridge() {}
    public static Forge1192ItemExtensions extensions(Item item) {
        synchronized (item) {
            var carrier = (com.fuckingdeveloper.lms.runtime.Forge1192CustomItemExtensionCarrier) item;
            if (carrier.lms$getCustomItemExtension() == null) carrier.lms$setCustomItemExtension(initialize(item));
            return (Forge1192ItemExtensions) carrier.lms$getCustomItemExtension();
        }
    }

    private static Forge1192ItemExtensions initialize(Item item) {
        try {
            var method = item.getClass().getMethod("initializeClient", Consumer.class);
            Class<?> owner = method.getDeclaringClass();
            if (!(owner.getClassLoader() instanceof ManagedLegacyClassLoader loader))
                throw new UnsupportedOperationException("Custom source item has no managed initializeClient declaration");
            verifyRegistration(loader, owner.getName());
            var accepted = new ArrayList<Forge1192ItemExtensions>();
            Consumer<Object> consumer = value -> {
                if (!(value instanceof Forge1192ItemExtensions extension))
                    throw new IllegalStateException("Source initializeClient supplied a foreign extension");
                accepted.add(extension);
            };
            MethodHandles.privateLookupIn(owner, MethodHandles.lookup()).unreflect(method).invoke(item, consumer);
            if (accepted.size() != 1) throw new IllegalStateException("Source initializeClient must register exactly one item extension");
            return accepted.getFirst();
        } catch (RuntimeException | Error failure) { throw failure; }
        catch (Throwable failure) { throw new IllegalStateException("Cannot initialize source custom item extension", failure); }
    }

    /** Only an allocation/capture/accept declaration can safely materialize lazily. */
    public static void verifyRegistration(ManagedLegacyClassLoader loader, String owner) throws java.io.IOException {
        var node = new ClassNode(); new ClassReader(Objects.requireNonNull(loader.originalClassBytes(owner))).accept(node, 0);
        var method = node.methods.stream().filter(m -> m.name.equals("initializeClient")
                && m.desc.equals("(Ljava/util/function/Consumer;)V")).findFirst().orElseThrow();
        var code = Arrays.stream(method.instructions.toArray()).filter(i -> i.getOpcode() >= 0).toList();
        if (code.size() == 10 && code.get(0) instanceof VarInsnNode self && self.getOpcode() == Opcodes.ALOAD && self.var == 0
                && code.get(1) instanceof VarInsnNode arg && arg.getOpcode() == Opcodes.ALOAD && arg.var == 1
                && code.get(2) instanceof MethodInsnNode base && base.getOpcode() == Opcodes.INVOKESPECIAL
                && Set.of("net/minecraft/world/item/Item", "net/minecraft/world/item/BlockItem").contains(base.owner)
                && base.name.equals("initializeClient") && base.desc.equals("(Ljava/util/function/Consumer;)V"))
            code = code.subList(3, code.size());
        if (code.size() != 7 || !(code.get(0) instanceof VarInsnNode consumer) || consumer.getOpcode() != Opcodes.ALOAD || consumer.var != 1
                || !(code.get(1) instanceof TypeInsnNode allocation) || allocation.getOpcode() != Opcodes.NEW
                || code.get(2).getOpcode() != Opcodes.DUP || !(code.get(3) instanceof VarInsnNode receiver)
                || receiver.getOpcode() != Opcodes.ALOAD || receiver.var != 0
                || !(code.get(4) instanceof MethodInsnNode ctor) || ctor.getOpcode() != Opcodes.INVOKESPECIAL
                || !ctor.owner.equals(allocation.desc) || !ctor.name.equals("<init>") || !ctor.desc.equals("(L" + node.name + ";)V")
                || !(code.get(5) instanceof MethodInsnNode accept) || accept.getOpcode() != Opcodes.INVOKEINTERFACE
                || !accept.owner.equals("java/util/function/Consumer") || !accept.name.equals("accept")
                || !accept.desc.equals("(Ljava/lang/Object;)V") || code.get(6).getOpcode() != Opcodes.RETURN)
            throw new UnsupportedOperationException("Source custom item initialization has observable/unproven work: " + owner);
        var extension = new ClassNode();
        new ClassReader(Objects.requireNonNull(loader.originalClassBytes(allocation.desc.replace('/', '.')))).accept(extension, 0);
        if (extension.methods.stream().anyMatch(m -> m.name.equals("<clinit>")))
            throw new UnsupportedOperationException("Source item extension has unproven static initialization");
        var constructor = extension.methods.stream().filter(m -> m.name.equals("<init>") && m.desc.equals(ctor.desc)).findFirst().orElseThrow();
        for (var instruction : constructor.instructions) {
            if (instruction.getOpcode() < 0 || instruction instanceof VarInsnNode || instruction.getOpcode() == Opcodes.RETURN) continue;
            if (instruction instanceof FieldInsnNode field && field.getOpcode() == Opcodes.PUTFIELD && field.owner.equals(extension.name)) continue;
            if (instruction instanceof MethodInsnNode call && call.getOpcode() == Opcodes.INVOKESPECIAL
                    && call.owner.equals("java/lang/Object") && call.name.equals("<init>") && call.desc.equals("()V")) continue;
            throw new UnsupportedOperationException("Source item extension constructor has unproven work: " + extension.name);
        }
    }

    /** Forge 1.19.2 Item.initializeClient is exactly RETURN; BlockItem inherits it. */
    public static void sourceBaseInitialize(Item item, Consumer<?> consumer) {}

    /** Native source-world dispatcher observes the main camera; no substitute camera is created. */
    public static net.minecraft.world.phys.Vec3 cameraPosition(BlockEntityRenderDispatcher dispatcher) {
        var minecraft = net.minecraft.client.Minecraft.getInstance();
        if (dispatcher != minecraft.getBlockEntityRenderDispatcher())
            throw new UnsupportedOperationException("Source camera query on a non-native block entity dispatcher");
        return minecraft.gameRenderer.mainCamera().position();
    }

    public static boolean renderItem(BlockEntityRenderDispatcher dispatcher, BlockEntity entity, PoseStack pose,
            LegacyMultiBufferSource buffers, int light, int overlay) {
        Object renderer = dispatcher.getRenderer(entity);
        if (renderer == null) return true; // Source renderItem returns true precisely on absence.
        if (!(renderer instanceof Forge1192BlockEntityRendererBridge legacy))
            throw new UnsupportedOperationException("Source item dispatcher requires an adapted source BER: " + renderer.getClass().getName());
        legacy.lmsLegacyRender(entity, 0, pose, buffers, light, overlay);
        return false;
    }
}
