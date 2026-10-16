package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.fluid.Forge1192EarlyFluidStackBridge;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.item.Forge1192EarlyItemStackBridge;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.item.Forge1192ItemBridge;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.network.Forge1192FluidPacketBridge;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.network.Forge1192ItemWireBridge;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.network.Forge1192NetworkBridge;


import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import net.minecraft.core.component.*;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.*;
import io.netty.buffer.Unpooled;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.util.*;

public final class ItemWireFamilyCheck {
    private static void check(boolean result,String message){if(!result)throw new AssertionError(message);}
    public static void run() { NativeDefaultComponentFixture.run(ItemWireFamilyCheck::checks); }
    private static void checks() {
        var tag=new CompoundTag();tag.putInt("charge",42);
        var wire=new FriendlyByteBuf(Unpooled.buffer());
        try {
            check(Forge1192ItemWireBridge.writeNbt(wire,tag)==wire,"source fluent receiver identity lost");
            byte[] bytes=new byte[wire.readableBytes()];wire.getBytes(0,bytes);
            check(java.util.HexFormat.of().formatHex(bytes).equals("0a00000300066368617267650000002a00"),"source named-root NBT header/payload changed");
            wire.writeInt(0x10203040);check(Forge1192ItemWireBridge.readNbt(wire).equals(tag)&&wire.readInt()==0x10203040,"source NBT framing consumed following field");wire.clear();
            Forge1192ItemWireBridge.writeNbt(wire,null);check(wire.readableBytes()==1 && Forge1192ItemWireBridge.readNbt(wire)==null,"source null NBT framing lost");wire.clear();
            Forge1192ItemWireBridge.writeItem(wire,ItemStack.EMPTY);check(wire.readableBytes()==1 && Forge1192ItemWireBridge.readItem(wire)==ItemStack.EMPTY,"empty item wire shape changed");wire.clear();
            Forge1192NetworkBridge.writeRegistryIdUnsafe(wire,net.minecraft.core.registries.BuiltInRegistries.FLUID,net.minecraft.world.level.material.Fluids.WATER);
            check(Forge1192NetworkBridge.readRegistryIdUnsafe(wire,net.minecraft.core.registries.BuiltInRegistries.FLUID)==net.minecraft.world.level.material.Fluids.WATER,"raw registry wire identity lost");wire.clear();
            Forge1192FluidPacketBridge.writeToPacket(Forge1192EarlyFluidStackBridge.create(net.minecraft.world.level.material.Fluids.WATER,321),wire);
            var fluid=Forge1192FluidPacketBridge.readFromPacket(wire);
            check(fluid.getFluid()==net.minecraft.world.level.material.Fluids.WATER && fluid.getAmount()==321 && wire.readableBytes()==0,"named registry/fluid amount/NBT framing lost");wire.clear();
            Forge1192FluidPacketBridge.writeFluidStack(wire,net.neoforged.neoforge.fluids.FluidStack.EMPTY);
            check(wire.readableBytes()==1 && Forge1192FluidPacketBridge.readFluidStack(wire)==net.neoforged.neoforge.fluids.FluidStack.EMPTY,"source buffer fluid presence/empty sentinel lost");wire.clear();
            Forge1192FluidPacketBridge.writeFluidStack(wire,Forge1192EarlyFluidStackBridge.create(net.minecraft.world.level.material.Fluids.WATER,19));
            check(wire.getByte(0)==1 && Forge1192FluidPacketBridge.readFluidStack(wire).getAmount()==19 && wire.readableBytes()==0,"buffer fluid wrapper omitted presence discriminator");wire.clear();
            var stack=Forge1192EarlyItemStackBridge.create(Items.DIAMOND_SWORD,1);Forge1192ItemBridge.setTag(stack,tag.copy());stack.setDamageValue(7);
            Forge1192ItemWireBridge.writeItem(wire,stack);var read=Forge1192ItemWireBridge.readItem(wire);
            check(read.getItem()==stack.getItem()&&read.getCount()==1&&read.getDamageValue()==7&&Forge1192ItemBridge.getTag(read).getIntOr("charge",0)==42,"source item identity/count/damage/custom NBT roundtrip lost");
            check(Forge1192ItemBridge.getTag(read)!=Forge1192ItemBridge.getTag(stack),"decoded tag aliases sender tag");wire.clear();
            stack.set(DataComponents.MAX_STACK_SIZE,17);
            try{Forge1192ItemWireBridge.writeItem(wire,stack);throw new AssertionError("unknown component silently dropped");}catch(UnsupportedOperationException expected){}
            check(wire.writerIndex()==0,"unsupported component wrote partial packet before projection check");
        }finally{wire.release();}
        var node=new ClassNode();node.name="fixture/ItemWire";node.superName="java/lang/Object";node.version=Opcodes.V17;node.access=Opcodes.ACC_PUBLIC;
        var method=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"write","(Lnet/minecraft/network/FriendlyByteBuf;Lnet/minecraft/world/item/ItemStack;)V",null,null);
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));method.instructions.add(new VarInsnNode(Opcodes.ALOAD,1));method.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,"net/minecraft/network/FriendlyByteBuf","m_130055_","(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/network/FriendlyByteBuf;",false));method.instructions.add(new InsnNode(Opcodes.POP));method.instructions.add(new InsnNode(Opcodes.RETURN));node.methods.add(method);
        var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);var mapped=new ClassNode();new ClassReader(new Forge1192LegacyClassTransformer().transform(node.name.replace('/','.'),writer.toByteArray()).bytes()).accept(mapped,0);
        var call=(MethodInsnNode)mapped.methods.getFirst().instructions.get(2);check(call.getOpcode()==Opcodes.INVOKESTATIC && call.owner.endsWith("Forge1192ItemWireBridge")&&call.desc.equals("(Lnet/minecraft/network/FriendlyByteBuf;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/network/FriendlyByteBuf;"),"source buffer receiver/descriptor not projected");
        var registryMethod=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"registry","(Lnet/minecraft/network/FriendlyByteBuf;Lnet/minecraftforge/registries/IForgeRegistry;Ljava/lang/Object;)V",null,null);
        for(int i=0;i<3;i++)registryMethod.instructions.add(new VarInsnNode(Opcodes.ALOAD,i));
        registryMethod.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,"net/minecraft/network/FriendlyByteBuf","writeRegistryIdUnsafe","(Lnet/minecraftforge/registries/IForgeRegistry;Ljava/lang/Object;)V",false));registryMethod.instructions.add(new InsnNode(Opcodes.RETURN));node.methods.add(registryMethod);
        writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);mapped=new ClassNode();new ClassReader(new Forge1192LegacyClassTransformer().transform(node.name.replace('/','.'),writer.toByteArray()).bytes()).accept(mapped,0);
        var registryCall=(MethodInsnNode)mapped.methods.get(1).instructions.get(3);
        check(registryCall.getOpcode()==Opcodes.INVOKESTATIC && registryCall.owner.endsWith("Forge1192NetworkBridge") && registryCall.desc.equals("(Lnet/minecraft/network/FriendlyByteBuf;Ljava/lang/Object;Ljava/lang/Object;)V"),"source registry wire operation missed before marker descriptor projection");
        System.out.println("PASS: pinned source item/NBT wire bytes, empty/null/framing, native item/count/damage/custom tag roundtrip and unknown component rejection; full custom share callbacks require runtime evidence");
    }
}
