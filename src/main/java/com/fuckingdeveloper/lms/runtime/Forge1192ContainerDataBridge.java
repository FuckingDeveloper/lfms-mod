package com.fuckingdeveloper.lms.runtime;

import com.fuckingdeveloper.lms.mixin.Target263ContainerStateAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;

/** Verified native-container portion of a source NBT superclass callback. */
public final class Forge1192ContainerDataBridge {
    private Forge1192ContainerDataBridge(){}
    public static void loadBase(BaseContainerBlockEntity entity,CompoundTag tag) {
        var access=(Target263ContainerStateAccess)entity;
        access.lms$lock(Target263SourceNameLock.lock(tag.getStringOr("Lock","")));
        // Source load leaves the previous name untouched when CustomName is absent.
        if(tag.get("CustomName") instanceof net.minecraft.nbt.StringTag)
            access.lms$customName(Forge1192ComponentJsonBridge.fromJson(tag.getStringOr("CustomName","")));
    }
    public static void saveBase(BaseContainerBlockEntity entity,CompoundTag tag) {
        var access=(Target263ContainerStateAccess)entity;
        String name=Target263SourceNameLock.sourceName(access.lms$lock());
        if(!name.isEmpty())tag.putString("Lock",name);
        if(access.lms$customName()!=null)tag.putString("CustomName",Forge1192ComponentJsonBridge.toJson(access.lms$customName()));
    }
    public static boolean tryLoadLootTable(RandomizableContainerBlockEntity entity,CompoundTag tag) {
        if(!(tag.get("LootTable") instanceof net.minecraft.nbt.StringTag))return false;
        entity.setLootTable(ResourceKey.create(Registries.LOOT_TABLE,Identifier.parse(tag.getStringOr("LootTable",""))));
        entity.setLootTableSeed(tag.getLongOr("LootTableSeed",0));return true;
    }
    public static boolean trySaveLootTable(RandomizableContainerBlockEntity entity,CompoundTag tag) {
        if(entity.getLootTable()==null)return false;
        tag.putString("LootTable",entity.getLootTable().identifier().toString());
        if(entity.getLootTableSeed()!=0)tag.putLong("LootTableSeed",entity.getLootTableSeed());
        return true;
    }
}
