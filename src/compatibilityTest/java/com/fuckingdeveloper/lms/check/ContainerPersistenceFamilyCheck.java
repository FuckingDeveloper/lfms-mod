package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.container.Forge1192ContainerDataBridge;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.container.Forge1192ContainerNbtBridge;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.container.Forge1192DoubleContainerBridge;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.item.Forge1192EarlyItemStackBridge;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.item.Forge1192ItemBridge;
import com.fuckingdeveloper.lms.target.neoforge263.Target263SourceNameLock;


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
        var state=Blocks.CHEST.defaultBlockState();
        var level=(net.minecraft.world.level.LevelAccessor)java.lang.reflect.Proxy.newProxyInstance(ContainerPersistenceFamilyCheck.class.getClassLoader(),new Class<?>[]{net.minecraft.world.level.LevelAccessor.class},(proxy,method,args)-> {
            return switch(method.getName()) {case "getBlockEntity"->entity;case "getBlockState"->state;default->throw new AssertionError(method);};
        });
        var callbacks=new java.util.concurrent.atomic.AtomicInteger();
        var combined=Forge1192DoubleContainerBridge.combine(BlockDataFamilyCheck.fixtureType(),s->{callbacks.incrementAndGet();return net.minecraft.world.level.block.DoubleBlockCombiner.BlockType.SINGLE;},s->net.minecraft.core.Direction.EAST,
                net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING,state,level,BlockPos.ZERO,(world,pos)->false);
        var result=combined.apply(new net.minecraft.world.level.block.DoubleBlockCombiner.Combiner<net.minecraft.world.level.block.entity.BlockEntity,Integer>() {
            public Integer acceptSingle(net.minecraft.world.level.block.entity.BlockEntity value){check(value==entity,"native combiner replaced source container identity");return 1;}
            public Integer acceptDouble(net.minecraft.world.level.block.entity.BlockEntity a,net.minecraft.world.level.block.entity.BlockEntity b){throw new AssertionError("single source state combined twice");}
            public Integer acceptNone(){return 0;}
        });
        check(result==1&&callbacks.get()==1,"native double-container callback ordering/result changed");

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
        var slots=NonNullList.withSize(256,ItemStack.EMPTY);
        var content=Forge1192EarlyItemStackBridge.create(Items.APPLE,13);
        content.set(DataComponents.CUSTOM_NAME,Component.literal("Stored name"));Forge1192ItemBridge.getOrCreateTag(content).putInt("source_energy",42);
        slots.set(255,content);var inventoryTag=new CompoundTag();inventoryTag.putString("unrelated","retained");
        check(Forge1192ContainerNbtBridge.saveAllItems(inventoryTag,slots)==inventoryTag,"source inventory save lost fluent destination identity");
        var entry=(CompoundTag)inventoryTag.getListOrEmpty("Items").get(0);
        check(entry.getByteOr("Slot",(byte)0)==(byte)255&&entry.getByteOr("Count",(byte)0)==13&&entry.getStringOr("id","").equals("minecraft:apple"),"source byte slot/count/item schema changed");
        check(entry.getCompoundOrEmpty("tag").getIntOr("source_energy",0)==42&&entry.getCompoundOrEmpty("tag").getCompoundOrEmpty("display").getStringOr("Name","").contains("Stored name"),"source stack data/native custom name dropped during container persistence");
        var originalItems=inventoryTag.get("Items");Forge1192ContainerNbtBridge.saveAllItems(inventoryTag,NonNullList.withSize(0,ItemStack.EMPTY),false);
        check(inventoryTag.get("Items")==originalItems&&inventoryTag.getStringOr("unrelated","").equals("retained"),"empty source optional save removed existing data");
        var small=NonNullList.withSize(2,content);Forge1192ContainerNbtBridge.loadAllItems(inventoryTag,small);
        check(small.get(0)==content&&small.get(1)==content,"out-of-range/unlisted source inventory slots changed");
        Forge1192ContainerNbtBridge.saveAllItems(inventoryTag,NonNullList.withSize(0,ItemStack.EMPTY),true);
        check(inventoryTag.getListOrEmpty("Items").isEmpty(),"explicit empty source inventory save omitted list");
        var namePredicate=new Target263SourceNameLock("source key");
        var encoded=Target263SourceNameLock.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE,namePredicate).getOrThrow();
        check(Target263SourceNameLock.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE,encoded).getOrThrow().equals(namePredicate),"native predicate codec lost lock name");
        System.out.println("PASS: native private container state/source JSON name, plain-text formatted-name lock and empty-key rejection, source loot presence/seed, NBT roundtrip and predicate codec");
    }
    private static void check(boolean value,String reason){if(!value)throw new AssertionError(reason);}
}
