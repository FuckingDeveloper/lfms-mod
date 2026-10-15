package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.runtime.*;
import com.fuckingdeveloper.lms.mixin.Target263ContainerStateAccess;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.*;

final class ContainerPersistenceFamilyCheck {
    static final class Container extends RandomizableContainerBlockEntity {
        private NonNullList<ItemStack> items=NonNullList.withSize(27,ItemStack.EMPTY);
        Container(){super(BlockDataFamilyCheck.fixtureType(),BlockPos.ZERO,Blocks.CHEST.defaultBlockState());}
        protected Component getDefaultName(){return Component.literal("fixture");}
        protected NonNullList<ItemStack> getItems(){return items;}
        protected void setItems(NonNullList<ItemStack> items){this.items=items;}
        public int getContainerSize(){return 27;}
        protected AbstractContainerMenu createMenu(int id,Inventory inventory){return ChestMenu.threeRows(id,inventory,this);}
    }
    static void run() { NativeDefaultComponentFixture.run(ContainerPersistenceFamilyCheck::checks); }
    private static void checks() {
        var entity=new Container();var tag=new CompoundTag();
        tag.putString("CustomName","{\"text\":\"Source name\",\"color\":\"red\"}");tag.putString("Lock","source key");
        tag.putString("LootTable","minecraft:chests/simple_dungeon");tag.putLong("LootTableSeed",123);
        Forge1192ContainerDataBridge.loadBase(entity,tag);
        check(entity.getCustomName().getString().equals("Source name"),"source JSON name not installed in native state");
        check(Forge1192ContainerDataBridge.tryLoadLootTable(entity,tag)&&entity.getLootTable().equals(ResourceKey.create(Registries.LOOT_TABLE,Identifier.parse("minecraft:chests/simple_dungeon")))&&entity.getLootTableSeed()==123,"source loot identity/seed");
        var key=Forge1192EarlyItemStackBridge.create(Items.APPLE);key.set(DataComponents.CUSTOM_NAME,Component.literal("source key").withStyle(net.minecraft.ChatFormatting.GOLD));
        var lock=((Target263ContainerStateAccess)(Object)entity).lms$lock();
        check(lock.unlocksWith(key),"source plain-text lock changed to formatted-component equality");
        key.set(DataComponents.CUSTOM_NAME,Component.literal("different"));check(!lock.unlocksWith(key)&&!lock.unlocksWith(ItemStack.EMPTY),"wrong/empty key admitted");
        var output=new CompoundTag();Forge1192ContainerDataBridge.saveBase(entity,output);
        check(Forge1192ContainerDataBridge.trySaveLootTable(entity,output),"source loot save lost presence");
        var loaded=new Container();Forge1192ContainerDataBridge.loadBase(loaded,output);Forge1192ContainerDataBridge.tryLoadLootTable(loaded,output);
        check(loaded.getCustomName().equals(entity.getCustomName())&&output.getStringOr("Lock","").equals("source key")&&loaded.getLootTableSeed()==123,"source container roundtrip lost native state");
        var empty=new CompoundTag();Forge1192ContainerDataBridge.loadBase(loaded,empty);
        check(!loaded.isLocked()&&loaded.getCustomName()!=null&&!Forge1192ContainerDataBridge.tryLoadLootTable(loaded,empty),"source absent-key/name/loot behavior changed");
        var namePredicate=new Target263SourceNameLock("source key");
        var encoded=Target263SourceNameLock.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE,namePredicate).getOrThrow();
        check(Target263SourceNameLock.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE,encoded).getOrThrow().equals(namePredicate),"native predicate codec lost lock name");
        System.out.println("PASS: native private container state/source JSON name, plain-text formatted-name lock and empty-key rejection, source loot presence/seed, NBT roundtrip and predicate codec");
    }
    private static void check(boolean value,String reason){if(!value)throw new AssertionError(reason);}
}
