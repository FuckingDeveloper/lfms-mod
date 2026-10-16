package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.item.Forge1192ToolModificationBridge;


import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.*;
import net.minecraft.core.component.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.*;
import net.neoforged.neoforge.event.level.BlockEvent.BlockToolModificationEvent;
import net.neoforged.bus.api.EventPriority;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import static com.fuckingdeveloper.lms.profile.forge1192.runtime.item.Forge1192ToolActions.*;

final class ToolActionFamilyCheck {
    private static final class PermittedItem extends Item {
        private PermittedItem() { super(new Item.Properties()); } // Never constructed or registered.
        @Override public boolean canPerformAction(ItemInstance stack, ItemAbility ability) { return true; }
    }
    static void run() throws Exception {
        var field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe"); field.setAccessible(true);
        var unsafe = (sun.misc.Unsafe)field.get(null);
        var item = (Item)unsafe.allocateInstance(PermittedItem.class);
        var stack = new ItemStack(Holder.direct(item), 1, new PatchedDataComponentMap(DataComponentMap.EMPTY));
        check(HOE_TILL == ItemAbility.get("till") && SHEARS_DIG == ItemAbilities.SHEARS_DIG, "action tokens lost interned identity");
        check(DEFAULT_HOE_ACTIONS.equals(Set.of(HOE_DIG, HOE_TILL)) && DEFAULT_AXE_ACTIONS.size() == 4
                && DEFAULT_SHEARS_ACTIONS.size() == 4, "source default sets changed");
        var custom = ItemAbility.get("probe:custom"); DEFAULT_HOE_ACTIONS.add(custom);
        check(DEFAULT_HOE_ACTIONS.remove(custom), "source action set became immutable");

        // An isolated target level exposes only getBlockState; any world mutation fails.
        var node = new ClassNode(); node.version=Opcodes.V17; node.access=Opcodes.ACC_PUBLIC;
        node.name="fixture/ToolQueryLevel"; node.superName="net/minecraft/server/level/ServerLevel";
        node.fields.add(new FieldNode(Opcodes.ACC_PUBLIC,"above","Lnet/minecraft/world/level/block/state/BlockState;",null,null));
        var getter = new MethodNode(Opcodes.ACC_PUBLIC,"getBlockState","(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",null,null);
        getter.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));
        getter.instructions.add(new FieldInsnNode(Opcodes.GETFIELD,node.name,"above","Lnet/minecraft/world/level/block/state/BlockState;"));
        getter.instructions.add(new InsnNode(Opcodes.ARETURN)); node.methods.add(getter);
        var writer = new ClassWriter(ClassWriter.COMPUTE_MAXS); node.accept(writer);
        var levelType = new ClassLoader(ToolActionFamilyCheck.class.getClassLoader()) {
            Class<?> define(byte[] bytes) { return defineClass(null,bytes,0,bytes.length); }
        }.define(writer.toByteArray());
        var level = (Level)unsafe.allocateInstance(levelType);
        levelType.getField("above").set(level,Blocks.AIR.defaultBlockState());
        var pos = new BlockPos(3,4,5);
        var context = new UseOnContext(level,null,InteractionHand.MAIN_HAND,stack,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));
        var dirt=Blocks.DIRT.defaultBlockState();
        check(dirt.getToolModifiedState(context,HOE_TILL,true)==null,"source defaults leaked into native target call");
        check(Forge1192ToolModificationBridge.query(dirt,context,HOE_TILL,true).is(Blocks.FARMLAND),"source simulated till result lost");
        check(Forge1192ToolModificationBridge.query(dirt,context,HOE_TILL,false).is(Blocks.FARMLAND),"source actual query lost result");
        check(!Forge1192ToolModificationBridge.active(dirt,context,HOE_TILL,false),"source query scope leaked");
        check(Forge1192ToolModificationBridge.query(Blocks.COARSE_DIRT.defaultBlockState(),context,HOE_TILL,true).is(Blocks.DIRT),"coarse dirt source rule lost");
        check(Forge1192ToolModificationBridge.query(Blocks.ROOTED_DIRT.defaultBlockState(),context,HOE_TILL,true).is(Blocks.DIRT),"root simulation attempted side effects");
        check(Forge1192ToolModificationBridge.query(dirt,context,SHOVEL_FLATTEN,true).is(Blocks.DIRT_PATH),"source pathing query lost");
        levelType.getField("above").set(level,Blocks.STONE.defaultBlockState());
        check(Forge1192ToolModificationBridge.query(dirt,context,HOE_TILL,true)==null,"blocked till incorrectly succeeded");
        try { Forge1192ToolModificationBridge.query(dirt,context,AXE_STRIP,true); throw new AssertionError("unknown source stripping adapter silently returned null"); }
        catch(UnsupportedOperationException expected) {}
        check(!Forge1192ToolModificationBridge.active(dirt,context,AXE_STRIP,true),"failed query scope leaked");

        int[] events={0};
        Consumer<BlockToolModificationEvent> cancel=event->{ events[0]++; event.setCanceled(true); };
        NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL,false,BlockToolModificationEvent.class,cancel);
        try { check(Forge1192ToolModificationBridge.query(dirt,context,HOE_TILL,true)==null && events[0]==1,"native cancellation/event count lost"); }
        finally { NeoForge.EVENT_BUS.unregister(cancel); }
        Consumer<BlockToolModificationEvent> replace=event->event.setFinalState(Blocks.GOLD_BLOCK.defaultBlockState());
        NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL,false,BlockToolModificationEvent.class,replace);
        try { check(Forge1192ToolModificationBridge.query(dirt,context,HOE_TILL,true).is(Blocks.GOLD_BLOCK),"native event result ignored"); }
        finally { NeoForge.EVENT_BUS.unregister(replace); }

        var callback = new ClassNode(); callback.version=Opcodes.V17; callback.access=Opcodes.ACC_PUBLIC;
        callback.name="fixture/SourceActionItem"; callback.superName="net/minecraft/world/item/Item";
        callback.fields.add(new FieldNode(Opcodes.ACC_PUBLIC,"seenStack","Lnet/minecraft/world/item/ItemStack;",null,null));
        callback.fields.add(new FieldNode(Opcodes.ACC_PUBLIC,"seenAction","Lnet/minecraftforge/common/ToolAction;",null,null));
        var action = new MethodNode(Opcodes.ACC_PUBLIC,"canPerformAction","(Lnet/minecraft/world/item/ItemStack;Lnet/minecraftforge/common/ToolAction;)Z",null,null);
        action.instructions.add(new VarInsnNode(Opcodes.ALOAD,0)); action.instructions.add(new VarInsnNode(Opcodes.ALOAD,1));
        action.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD,callback.name,"seenStack","Lnet/minecraft/world/item/ItemStack;"));
        action.instructions.add(new VarInsnNode(Opcodes.ALOAD,0)); action.instructions.add(new VarInsnNode(Opcodes.ALOAD,2));
        action.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD,callback.name,"seenAction","Lnet/minecraftforge/common/ToolAction;"));
        action.instructions.add(new InsnNode(Opcodes.ICONST_1)); action.instructions.add(new InsnNode(Opcodes.IRETURN)); callback.methods.add(action);
        var callbackWriter=new ClassWriter(ClassWriter.COMPUTE_MAXS); callback.accept(callbackWriter);
        Path synthetic=Files.createTempFile("lms-source-action", ".jar");
        try {
            try(var jar=new java.util.jar.JarOutputStream(Files.newOutputStream(synthetic))) {
                jar.putNextEntry(new java.util.jar.JarEntry(callback.name+".class"));jar.write(callbackWriter.toByteArray());jar.closeEntry();
            }
            try(var loader=new ManagedLegacyClassLoader(synthetic,ToolActionFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
                var sourceType=loader.loadClass("fixture.SourceActionItem");
                var sourceItem=(Item)unsafe.allocateInstance(sourceType);
                var sourceStack=new ItemStack(Holder.direct(sourceItem),1,new PatchedDataComponentMap(DataComponentMap.EMPTY));
                check(sourceStack.canPerformAction(HOE_TILL),"native ItemInstance dispatch missed source override");
                check(sourceType.getField("seenStack").get(sourceItem)==sourceStack
                        && sourceType.getField("seenAction").get(sourceItem)==HOE_TILL,"source callback lost exact live stack/action");
                var stackless=(ItemInstance)java.lang.reflect.Proxy.newProxyInstance(ToolActionFamilyCheck.class.getClassLoader(),new Class[]{ItemInstance.class},(p,m,a)->{throw new AssertionError("stackless source materialization");});
                try { sourceItem.canPerformAction(stackless,HOE_TILL); throw new AssertionError("stackless source callback admitted"); }
                catch(UnsupportedOperationException expected) {}
            }
        } finally { Files.deleteIfExists(synthetic); }

        Path fixture=Path.of("legacy-mods/IC2Classic-1.19.2-2.1.3.4.jar");
        if(Files.isRegularFile(fixture)) try(var loader=new ManagedLegacyClassLoader(fixture,ToolActionFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
            for(String name:List.of("ic2.core.block.resource.RubberwoodBarkBlock","ic2.core.block.resource.RubberwoodLogBlock",
                    "ic2.core.item.tool.electric.AdvancedHoe","ic2.core.item.tool.electric.ChainsawTool","ic2.core.item.tool.electric.MowingTool",
                    "ic2.core.item.tool.electric.NanoSaber","ic2.core.item.wearable.base.IC2ShieldBase")) {
                loader.loadClass(name);
                var transformed=new ClassNode();new ClassReader(loader.definedClassBytes(name)).accept(transformed,0);
                check(transformed.methods.stream().noneMatch(m->m.desc.contains("net/minecraftforge/common/ToolAction")),"source action descriptor escaped: "+name);
                check(transformed.methods.stream().flatMap(m->Arrays.stream(m.instructions.toArray())).noneMatch(i->i instanceof FieldInsnNode f && f.owner.equals("net/minecraftforge/common/ToolActions")),"source constants escaped: "+name);
                if(name.endsWith("AdvancedHoe")) check(transformed.methods.stream().flatMap(m->Arrays.stream(m.instructions.toArray())).anyMatch(i->i instanceof MethodInsnNode c && c.owner.equals("com/fuckingdeveloper/lms/profile/forge1192/runtime/item/Forge1192ToolModificationBridge") && c.name.equals("query")),"source preview query not migrated");
            }
        }
        System.out.println("PASS: tool-action identities/mutable source sets, seven immutable fixture users, native default isolation, till/path preview and query results, blocked soil, simulation without mutation, cancellation/result and failed-scope cleanup");
    }
    private static void check(boolean value,String reason) { if(!value)throw new AssertionError(reason); }
}
