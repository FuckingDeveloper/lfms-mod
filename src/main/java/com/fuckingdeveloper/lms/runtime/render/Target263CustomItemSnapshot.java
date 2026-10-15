package com.fuckingdeveloper.lms.runtime.render;

import net.minecraft.core.component.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Exclusively owned component snapshot; no input stack/renderer/block entity is retained. */
public final class Target263CustomItemSnapshot {
    private final Identifier item;
    private final int count;
    private final PatchedDataComponentMap components;

    private Target263CustomItemSnapshot(Identifier item, int count, PatchedDataComponentMap components) {
        this.item=item;this.count=count;this.components=components;
    }
    public static Target263CustomItemSnapshot capture(ItemStack input) {
        if(input.isEmpty())throw new IllegalArgumentException("Empty source custom item snapshot");
        var id=BuiltInRegistries.ITEM.getResourceKey(input.getItem()).orElseThrow(
                ()->new IllegalStateException("Source custom item is not registered")).identifier();
        var copy=input.copy();detachCustomData(copy);
        if(!(copy.getComponents() instanceof PatchedDataComponentMap map))
            throw new IllegalStateException("Native item does not expose its authoritative component map");
        return new Target263CustomItemSnapshot(id,copy.getCount(),map.copy());
    }
    public ItemStack materialize() {
        var holder=BuiltInRegistries.ITEM.get(item).orElseThrow(
                ()->new IllegalStateException("Captured source item disappeared: "+item));
        var result=new ItemStack(holder,count,components.copy());
        detachCustomData(result);
        return result;
    }
    private static void detachCustomData(ItemStack stack) {
        var data=stack.get(DataComponents.CUSTOM_DATA);
        if(data!=null)stack.set(DataComponents.CUSTOM_DATA,new CustomData(data.copyTag()));
    }
}
