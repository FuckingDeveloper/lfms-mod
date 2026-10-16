package com.fuckingdeveloper.lms.profile.forge1192.runtime.network;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.item.Forge1192EarlyItemStackBridge;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.item.Forge1192ItemBridge;

import static com.fuckingdeveloper.lms.profile.forge1192.runtime.data.Forge1192StackNbtBridge.*;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import io.netty.buffer.*;
import io.netty.handler.codec.*;
import net.minecraft.core.*;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.io.IOException;
import java.lang.invoke.*;
import java.util.*;

/** Paired source item/NBT wire operations. The native component stream codec is a different protocol. */
public final class Forge1192ItemWireBridge {
    private Forge1192ItemWireBridge() {}
    private record Callbacks(MethodHandle share, MethodHandle read, MethodHandle override, MethodHandle damageable) {}
    private static final ClassValue<Callbacks> CALLBACKS=new ClassValue<>() {
        @Override protected Callbacks computeValue(Class<?> type) {
            MethodHandle share=null,read=null,override=null,damageable=null;
            for(Class<?> current=type;current!=null;current=current.getSuperclass()) {
                if(!(current.getClassLoader() instanceof ManagedLegacyClassLoader loader))continue;
                try {
                    byte[] bytes=loader.originalClassBytes(current.getName());
                    if(bytes==null)throw new IllegalStateException("Missing source item callback evidence: "+current.getName());
                    var node=new ClassNode();new ClassReader(bytes).accept(node,ClassReader.SKIP_CODE|ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
                    var lookup=MethodHandles.privateLookupIn(current,MethodHandles.lookup());
                    for(var method:node.methods) {
                        if((method.access&Opcodes.ACC_STATIC)!=0)continue;
                        if(share==null && method.name.equals("getShareTag") && method.desc.equals("(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/nbt/CompoundTag;"))share=lookup.findVirtual(current,method.name,MethodType.methodType(CompoundTag.class,ItemStack.class));
                        if(read==null && method.name.equals("readShareTag") && method.desc.equals("(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/nbt/CompoundTag;)V"))read=lookup.findVirtual(current,method.name,MethodType.methodType(void.class,ItemStack.class,CompoundTag.class));
                        if(override==null && Set.of("m_41468_","shouldOverrideMultiplayerNbt").contains(method.name) && method.desc.equals("()Z"))override=lookup.findVirtual(current,method.name,MethodType.methodType(boolean.class));
                        if(damageable==null && method.name.equals("isDamageable") && method.desc.equals("(Lnet/minecraft/world/item/ItemStack;)Z"))damageable=lookup.findVirtual(current,method.name,MethodType.methodType(boolean.class,ItemStack.class));
                    }
                }catch(IOException|ReflectiveOperationException failure){throw new IllegalStateException("Source item wire callback linkage failed",failure);}
            }
            return new Callbacks(share,read,override,damageable);
        }
    };
    private static Object invoke(MethodHandle callback,Object... arguments) {
        try{return callback.invokeWithArguments(arguments);}
        catch(RuntimeException|Error failure){throw failure;}
        catch(Throwable failure){throw new IllegalStateException("Source item wire callback failed",failure);}
    }
    public static FriendlyByteBuf writeNbt(FriendlyByteBuf buffer,CompoundTag tag) {
        try{if(tag==null)buffer.writeByte(0);else NbtIo.writeUnnamedTag(tag,new ByteBufOutputStream(buffer));return buffer;}
        catch(IOException failure){throw new EncoderException(failure);}
    }
    public static CompoundTag readNbt(FriendlyByteBuf buffer) {
        if(buffer.getByte(buffer.readerIndex())==0){buffer.readByte();return null;}
        try{return NbtIo.read(new ByteBufInputStream(buffer),NbtAccounter.defaultQuota());}
        catch(IOException failure){throw new DecoderException(failure);}
    }
    public static FriendlyByteBuf writeItem(FriendlyByteBuf buffer,ItemStack stack){return writeItemStack(buffer,stack,true);}
    public static FriendlyByteBuf writeItemStack(FriendlyByteBuf buffer,ItemStack stack,boolean limited) {
        if(stack.isEmpty()){buffer.writeBoolean(false);return buffer;}
        requireProjection(stack);
        var item=stack.getItem();var callbacks=CALLBACKS.get(item.getClass());
        buffer.writeBoolean(true);buffer.writeVarInt(BuiltInRegistries.ITEM.getId(item));buffer.writeByte(stack.getCount());
        boolean damaged=callbacks.damageable==null?stack.isDamageableItem():(boolean)invoke(callbacks.damageable,item,stack);
        boolean override=callbacks.override==null || (boolean)invoke(callbacks.override,item);
        CompoundTag tag=null;
        if(damaged||override) {
            if(limited && callbacks.share!=null) {
                requireCustomOnly(stack);tag=(CompoundTag)invoke(callbacks.share,item,stack);
            }else tag=projectTag(stack);
        }
        return writeNbt(buffer,tag);
    }
    public static ItemStack readItem(FriendlyByteBuf buffer) {
        if(!buffer.readBoolean())return ItemStack.EMPTY;
        int id=buffer.readVarInt();Item item=BuiltInRegistries.ITEM.byId(id);
        if(item==null)throw new DecoderException("Unbound source item wire id: "+id);
        var stack=Forge1192EarlyItemStackBridge.create(item,buffer.readByte());
        CompoundTag tag=readNbt(buffer);var callbacks=CALLBACKS.get(item.getClass());
        if(callbacks.read!=null){
            if(tag!=null && Set.of("Damage","Enchantments","StoredEnchantments","Unbreakable","RepairCost","display").stream().anyMatch(tag::contains))throw new UnsupportedOperationException("Custom source share reader requires native component/NBT projection");
            invoke(callbacks.read,item,stack,tag);
        }else {Forge1192ItemBridge.setTag(stack,tag);restoreComponents(stack,tag);}
        return stack;
    }
}
