package com.fuckingdeveloper.lms.runtime;

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
    private static final Set<DataComponentType<?>> PROJECTED=Set.of(DataComponents.CUSTOM_DATA,DataComponents.DAMAGE,DataComponents.ENCHANTMENTS,DataComponents.STORED_ENCHANTMENTS,DataComponents.CUSTOM_NAME,DataComponents.REPAIR_COST,DataComponents.UNBREAKABLE);
    private static void requireProjection(ItemStack stack) {
        for(var type:stack.getComponentsPatch().keySet())if(!PROJECTED.contains(type))throw new UnsupportedOperationException("Source item wire component projection is unproven: "+type);
    }
    private static void requireCustomOnly(ItemStack stack){for(var type:stack.getComponentsPatch().keySet())if(type!=DataComponents.CUSTOM_DATA)throw new UnsupportedOperationException("Custom source share tag component projection is unproven: "+type);}
    private static CompoundTag projectTag(ItemStack stack) {
        CompoundTag source=Forge1192ItemBridge.getTag(stack);var tag=source==null?new CompoundTag():source.copy();
        if(stack.isDamageableItem())tag.putInt("Damage",stack.getDamageValue());
        if(stack.has(DataComponents.UNBREAKABLE))tag.putBoolean("Unbreakable",true);
        Integer repair=stack.get(DataComponents.REPAIR_COST);if(repair!=null && repair!=0)tag.putInt("RepairCost",repair);
        var name=stack.get(DataComponents.CUSTOM_NAME);if(name!=null){var display=tag.getCompoundOrEmpty("display").copy();display.putString("Name",Forge1192ComponentJsonBridge.toJson(name));tag.put("display",display);}
        putEnchantments(tag,"Enchantments",stack.get(DataComponents.ENCHANTMENTS));putEnchantments(tag,"StoredEnchantments",stack.get(DataComponents.STORED_ENCHANTMENTS));
        return source==null && tag.isEmpty()?null:tag;
    }
    private static void putEnchantments(CompoundTag tag,String key,ItemEnchantments values) {
        if(values==null || values.isEmpty())return;
        var list=new ListTag();for(var entry:values.entrySet()){var value=new CompoundTag();value.putString("id",entry.getKey().unwrapKey().orElseThrow().identifier().toString());value.putShort("lvl",(short)entry.getIntValue());list.add(value);}tag.put(key,list);
    }
    private static HolderLookup.Provider registries() {
        var active=Target263DataComponentBindingBridge.currentRegistries();if(active.isPresent())return active.get();
        var server=ServerLifecycleHooks.getCurrentServer();if(server!=null)return server.registryAccess();
        var client=net.minecraft.client.Minecraft.getInstance();if(client!=null && client.level!=null)return client.level.registryAccess();
        throw new IllegalStateException("Source enchantment wire requires authoritative registry lookup");
    }
    private static void restoreComponents(ItemStack stack,CompoundTag tag) {
        if(tag==null)return;
        if(tag.contains("Damage"))stack.setDamageValue(tag.getIntOr("Damage",0));
        if(tag.getBooleanOr("Unbreakable",false))stack.set(DataComponents.UNBREAKABLE,net.minecraft.util.Unit.INSTANCE);
        if(tag.contains("RepairCost"))stack.set(DataComponents.REPAIR_COST,tag.getIntOr("RepairCost",0));
        var display=tag.getCompoundOrEmpty("display");if(display.contains("Name"))stack.set(DataComponents.CUSTOM_NAME,Forge1192ComponentJsonBridge.fromJson(display.getStringOr("Name","")));
        for(var entry:Map.of("Enchantments",DataComponents.ENCHANTMENTS,"StoredEnchantments",DataComponents.STORED_ENCHANTMENTS).entrySet()) {
            if(!tag.contains(entry.getKey()))continue;
            var builder=new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            var lookup=registries().lookupOrThrow(Registries.ENCHANTMENT);
            for(var value:tag.getListOrEmpty(entry.getKey())){if(!(value instanceof CompoundTag enchant))throw new DecoderException("Invalid source enchantment wire entry");var key=ResourceKey.create(Registries.ENCHANTMENT,Identifier.parse(enchant.getStringOr("id","")));builder.set(lookup.getOrThrow(key),enchant.getShortOr("lvl",(short)0));}
            stack.set(entry.getValue(),builder.toImmutable());
        }
    }
}
