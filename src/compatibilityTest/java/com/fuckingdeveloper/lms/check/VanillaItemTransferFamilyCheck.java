package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.runtime.itemhandler.*;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.transfer.transaction.Transaction;

final class VanillaItemTransferFamilyCheck {
    private static class Handler implements LegacyIItemHandler {
        ItemStack stored=ItemStack.EMPTY;
        boolean insert=true,extract=true;
        int mutations;
        public int getSlots(){return 1;}
        public ItemStack getStackInSlot(int slot){return stored;}
        public int getSlotLimit(int slot){return 2;}
        public boolean isItemValid(int slot,ItemStack stack){return insert&&stack.is(Items.STONE);}
        public ItemStack insertItem(int slot,ItemStack stack,boolean simulate){
            if(!isItemValid(slot,stack)||stored.getCount()>=2)return stack;
            if(!simulate){stored=stack.copyWithCount(stored.getCount()+1);mutations++;}
            return ItemStack.EMPTY;
        }
        public ItemStack extractItem(int slot,int amount,boolean simulate){
            if(!extract||stored.isEmpty())return ItemStack.EMPTY;
            var result=stored.copyWithCount(1);
            if(!simulate){stored=stored.copyWithCount(stored.getCount()-1);mutations++;}
            return result;
        }
    }
    static void run() throws Exception {
        // Isolated item prototypes, as in the native probe; no datapack defaults loaded here.
        Items.DIRT.builtInRegistryHolder().bindComponents(net.minecraft.core.component.DataComponentMap.builder()
                .set(net.minecraft.core.component.DataComponents.MAX_STACK_SIZE,64).build());
        Items.COAL.builtInRegistryHolder().bindComponents(net.minecraft.core.component.DataComponentMap.builder()
                .set(net.minecraft.core.component.DataComponents.MAX_STACK_SIZE,64).build());
        var hopper=new SimpleContainer(new ItemStack(Items.STONE,3));var handler=new Handler();
        handler.insert=false;
        check(!Forge1192VanillaItemTransfer.insertOne(hopper,0,handler)&&handler.mutations==0&&hopper.getItem(0).getCount()==3,"source import filter bypassed");
        handler.insert=true;
        check(Forge1192VanillaItemTransfer.insertOne(hopper,0,handler)&&hopper.getItem(0).getCount()==2&&handler.stored.getCount()==1,"single import conservation");
        check(Forge1192VanillaItemTransfer.insertOne(hopper,0,handler)&&handler.stored.getCount()==2,"stacked source insertion");
        check(!Forge1192VanillaItemTransfer.insertOne(hopper,0,handler)&&hopper.getItem(0).getCount()==1&&handler.mutations==2,"full handler lost source item");
        var timingSource=new SimpleContainer(new ItemStack(Items.STONE,3));var seen=new java.util.ArrayList<Integer>();
        var timingHandler=new Handler(){@Override public ItemStack insertItem(int slot,ItemStack stack,boolean simulate){seen.add(timingSource.getItem(0).getCount());return super.insertItem(slot,stack,simulate);}};
        check(Forge1192VanillaItemTransfer.insertOne(timingSource,0,timingHandler)&&seen.equals(java.util.List.of(2,2)),"source callback timing changed: hopper removal must precede simulation/execution");
        var rejected=new SimpleContainer(new ItemStack(Items.DIRT));
        check(!Forge1192VanillaItemTransfer.insertOne(rejected,0,handler)&&rejected.getItem(0).is(Items.DIRT),"wrong item accepted");
        var destination=new SimpleContainer(1);
        handler.extract=false;
        check(!Forge1192VanillaItemTransfer.extractOne(handler,destination)&&destination.isEmpty(),"source export filter bypassed");
        handler.extract=true;
        check(Forge1192VanillaItemTransfer.extractOne(handler,destination)&&destination.getItem(0).getCount()==1&&handler.stored.getCount()==1,"single extraction conservation");
        check(Forge1192VanillaItemTransfer.extractOne(handler,destination)&&destination.getItem(0).getCount()==2&&handler.stored.isEmpty(),"stacked native destination");
        handler.stored=new ItemStack(Items.STONE);
        var noPlace=new SimpleContainer(1){@Override public boolean canPlaceItem(int slot,ItemStack stack){return false;}};
        int before=handler.mutations;
        check(!Forge1192VanillaItemTransfer.extractOne(handler,noPlace)&&handler.mutations==before,"destination placement rule bypassed");
        var full=new SimpleContainer(new ItemStack(Items.STONE,64));
        check(!Forge1192VanillaItemTransfer.extractOne(handler,full)&&handler.mutations==before,"full destination mutated source");
        try(var transaction=Transaction.openRoot()) {
            try{Forge1192VanillaItemTransfer.insertOne(hopper,0,handler);throw new AssertionError("source insert joined target transaction");}catch(IllegalStateException expected){}
            try{Forge1192VanillaItemTransfer.extractOne(handler,destination);throw new AssertionError("source extract joined target transaction");}catch(IllegalStateException expected){}
        }
        check(handler.mutations==before,"transaction boundary failed after source mutation");
        var invalid=new Handler(){@Override public ItemStack insertItem(int s,ItemStack value,boolean simulate){return value.copyWithCount(2);}};
        try{Forge1192VanillaItemTransfer.insertOne(hopper,0,invalid);throw new AssertionError("invalid source remainder accepted");}catch(IllegalStateException expected){}
        var dishonest=new Handler(){@Override public ItemStack extractItem(int s,int n,boolean simulate){return simulate?new ItemStack(Items.STONE):new ItemStack(Items.DIRT);}};
        try{Forge1192VanillaItemTransfer.extractOne(dishonest,new SimpleContainer(1));throw new AssertionError("source simulation disagreement accepted");}catch(IllegalStateException expected){}
        check(java.util.Arrays.stream(net.minecraft.world.level.block.entity.HopperBlockEntity.class.getDeclaredMethods()).anyMatch(m->m.getName().contains("lms$insert")),"hopper insertion mixin not applied");
        check(java.util.Arrays.stream(net.minecraft.world.level.block.DropperBlock.class.getDeclaredMethods()).anyMatch(m->m.getName().contains("lms$sourceInsert")),"dropper mixin not applied");
        helperChecks();
        System.out.println("PASS: source immediate item transfer import/export filters, one-item conservation, stacking/full/reject rules, simulation validation, transaction rejection before mutation and actual native automation mixins");
    }
    private static void helperChecks() throws Exception {
        var a=new ItemStack(Items.COAL,2);var b=new ItemStack(Items.COAL);
        check(LegacyItemHandlerHelper.canItemStacksStack(a,b),"source helper count-sensitive comparison");
        com.fuckingdeveloper.lms.runtime.Forge1192ItemBridge.getOrCreateTag(a).putInt("probe",4);
        check(!LegacyItemHandlerHelper.canItemStacksStack(a,b),"source helper ignored NBT");
        com.fuckingdeveloper.lms.runtime.Forge1192ItemBridge.getOrCreateTag(b).putInt("probe",4);
        check(LegacyItemHandlerHelper.canItemStacksStackRelaxed(a,b),"source helper lost equal source NBT");
        var copy=LegacyItemHandlerHelper.copyStackWithSize(a,5);
        check(copy!=a && copy.getCount()==5 && a.getCount()==2 && LegacyItemHandlerHelper.canItemStacksStack(copy,a),"source helper copy aliases or loses data");
        check(LegacyItemHandlerHelper.copyStackWithSize(a,0)==ItemStack.EMPTY,"zero-size copy identity");
        var history=new java.util.ArrayList<Integer>();
        LegacyIItemHandler multi=new LegacyIItemHandler(){
            public int getSlots(){return 2;}public int getSlotLimit(int slot){return 64;}
            public ItemStack getStackInSlot(int slot){return slot==0?ItemStack.EMPTY:b;}
            public boolean isItemValid(int slot,ItemStack stack){return true;}
            public ItemStack insertItem(int slot,ItemStack stack,boolean simulate){history.add(slot);return ItemStack.EMPTY;}
            public ItemStack extractItem(int slot,int amount,boolean simulate){throw new AssertionError("unexpected extraction");}
        };
        check(LegacyItemHandlerHelper.insertItemStacked(multi,a,true).isEmpty()&&history.equals(java.util.List.of(1)),"source helper did not prefer existing stack");
        history.clear();check(LegacyItemHandlerHelper.insertItem(multi,a,true).isEmpty()&&history.equals(java.util.List.of(0)),"source helper changed ordinary slot order");
        var handler=new Handler();handler.stored=new ItemStack(Items.STONE,2);
        check(LegacyItemHandlerHelper.calcRedstoneFromInventory(handler)==15,"source helper fullness formula");
        handler.stored=ItemStack.EMPTY;check(LegacyItemHandlerHelper.calcRedstoneFromInventory(handler)==0,"empty helper fullness");
        try{LegacyItemHandlerHelper.giveItemToPlayer(null,a);throw new AssertionError("unproven player helper accepted");}catch(UnsupportedOperationException expected){}
        LegacyItemHandlerHelper.giveItemToPlayer(null,ItemStack.EMPTY);
        try(var jar=new java.util.jar.JarFile("legacy-mods/IC2Classic-1.19.2-2.1.3.4.jar")) {
            String name="ic2/core/inventory/handler/BasicInventory";byte[] original;
            try(var in=jar.getInputStream(jar.getJarEntry(name+".class"))){original=in.readAllBytes();}
            var transformer=new com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer();
            var out=new org.objectweb.asm.tree.ClassNode();new org.objectweb.asm.ClassReader(transformer.transform(name.replace('/','.'),original).bytes()).accept(out,0);
            check(out.methods.stream().flatMap(m->java.util.stream.StreamSupport.stream(m.instructions.spliterator(),false))
                    .anyMatch(i->i instanceof org.objectweb.asm.tree.MethodInsnNode c && c.owner.equals("com/fuckingdeveloper/lms/runtime/itemhandler/LegacyItemHandlerHelper")),"immutable source helper consumer not relocated");
            check(out.methods.stream().flatMap(m->java.util.stream.StreamSupport.stream(m.instructions.spliterator(),false))
                    .noneMatch(i->i instanceof org.objectweb.asm.tree.MethodInsnNode c && c.owner.equals("net/minecraftforge/items/ItemHandlerHelper")),"residual original helper owner");
        }
    }
    private static void check(boolean result,String message){if(!result)throw new AssertionError(message);}
}
