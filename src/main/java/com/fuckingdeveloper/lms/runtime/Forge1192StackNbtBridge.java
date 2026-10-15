package com.fuckingdeveloper.lms.runtime;

import io.netty.handler.codec.DecoderException;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/** Proven source stack-tag schema shared by persistence and wire adapters. */
public final class Forge1192StackNbtBridge {
    private Forge1192StackNbtBridge(){}
    public static CompoundTag save(ItemStack stack,CompoundTag destination) {
        requireProjection(stack);
        var id=BuiltInRegistries.ITEM.getKey(stack.getItem());
        if(id==null)throw new IllegalStateException("Cannot persist an unregistered source item");
        destination.putString("id",id.toString());destination.putByte("Count",(byte)stack.getCount());
        var tag=projectTag(stack);if(tag!=null)destination.put("tag",tag);
        return destination;
    }
    private static final Set<DataComponentType<?>> PROJECTED=Set.of(DataComponents.CUSTOM_DATA,DataComponents.DAMAGE,DataComponents.ENCHANTMENTS,DataComponents.STORED_ENCHANTMENTS,DataComponents.CUSTOM_NAME,DataComponents.REPAIR_COST,DataComponents.UNBREAKABLE);
    public static void requireProjection(ItemStack stack) {
        for(var type:stack.getComponentsPatch().keySet())if(!PROJECTED.contains(type))throw new UnsupportedOperationException("Source stack NBT component projection is unproven: "+type);
    }
    public static void requireCustomOnly(ItemStack stack){for(var type:stack.getComponentsPatch().keySet())if(type!=DataComponents.CUSTOM_DATA)throw new UnsupportedOperationException("Custom source share tag component projection is unproven: "+type);}
    public static CompoundTag projectTag(ItemStack stack) {
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
    public static void restoreComponents(ItemStack stack,CompoundTag tag) {
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
