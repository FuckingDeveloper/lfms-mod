package com.fuckingdeveloper.lms.runtime;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.*;
import net.minecraft.world.item.ItemStack;

/** Exact source slot/stack NBT schema, independent of a particular container implementation. */
public final class Forge1192ContainerNbtBridge {
    private Forge1192ContainerNbtBridge(){}
    public static CompoundTag saveAllItems(CompoundTag destination,NonNullList<ItemStack> items){return saveAllItems(destination,items,true);}
    public static CompoundTag saveAllItems(CompoundTag destination,NonNullList<ItemStack> items,boolean alsoWhenEmpty) {
        var list=new ListTag();
        for(int slot=0;slot<items.size();slot++)if(!items.get(slot).isEmpty()) {
            var entry=new CompoundTag();entry.putByte("Slot",(byte)slot);
            Forge1192StackNbtBridge.save(items.get(slot),entry);list.add(entry);
        }
        // The source leaves an existing Items entry untouched when empty and alsoWhenEmpty=false.
        if(!list.isEmpty()||alsoWhenEmpty)destination.put("Items",list);
        return destination;
    }
    public static void loadAllItems(CompoundTag source,NonNullList<ItemStack> items) {
        if(!(source.get("Items") instanceof ListTag entries)||entries.stream().anyMatch(value->!(value instanceof CompoundTag)))return;
        for(var value:entries) {
            var entry=(CompoundTag)value;int slot=entry.getByteOr("Slot",(byte)0)&255;
            if(slot<items.size())items.set(slot,Forge1192ItemBridge.of(entry));
        }
    }
}
