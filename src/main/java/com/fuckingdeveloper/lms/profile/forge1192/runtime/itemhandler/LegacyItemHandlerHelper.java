package com.fuckingdeveloper.lms.profile.forge1192.runtime.itemhandler;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.item.Forge1192StackComparisonBridge;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Pinned source Forge helper operations over the retained source item-handler API. */
public final class LegacyItemHandlerHelper {
    private LegacyItemHandlerHelper() {}
    public static ItemStack insertItem(LegacyIItemHandler dest, ItemStack stack, boolean simulate) {
        if(dest==null||stack.isEmpty())return stack;
        for(int slot=0;slot<dest.getSlots();slot++) {
            stack=dest.insertItem(slot,stack,simulate);
            if(stack.isEmpty())return ItemStack.EMPTY;
        }
        return stack;
    }
    public static boolean canItemStacksStack(ItemStack a, ItemStack b) {
        return !a.isEmpty() && Forge1192StackComparisonBridge.sameItem(a,b)
                && Forge1192StackComparisonBridge.tagMatches(a,b);
    }
    public static boolean canItemStacksStackRelaxed(ItemStack a, ItemStack b) {
        return !a.isEmpty() && !b.isEmpty() && a.is(b.getItem()) && a.isStackable()
                && Forge1192StackComparisonBridge.tagMatches(a,b);
    }
    public static ItemStack copyStackWithSize(ItemStack stack, int size) {
        if(size==0)return ItemStack.EMPTY;
        var copy=stack.copy();copy.setCount(size);return copy;
    }
    public static ItemStack insertItemStacked(LegacyIItemHandler inventory, ItemStack stack, boolean simulate) {
        if(inventory==null||stack.isEmpty())return stack;
        if(!stack.isStackable())return insertItem(inventory,stack,simulate);
        int size=inventory.getSlots();
        for(int slot=0;slot<size;slot++) {
            if(canItemStacksStackRelaxed(inventory.getStackInSlot(slot),stack)) {
                stack=inventory.insertItem(slot,stack,simulate);
                if(stack.isEmpty())break;
            }
        }
        if(!stack.isEmpty())for(int slot=0;slot<size;slot++) {
            if(inventory.getStackInSlot(slot).isEmpty()) {
                stack=inventory.insertItem(slot,stack,simulate);
                if(stack.isEmpty())break;
            }
        }
        return stack;
    }
    public static int calcRedstoneFromInventory(LegacyIItemHandler inventory) {
        if(inventory==null)return 0;
        int found=0;float proportion=0;
        for(int slot=0;slot<inventory.getSlots();slot++) {
            var stack=inventory.getStackInSlot(slot);
            if(!stack.isEmpty()) {
                proportion+=(float)stack.getCount()/Math.min(inventory.getSlotLimit(slot),stack.getMaxStackSize());found++;
            }
        }
        proportion/=inventory.getSlots();
        return net.minecraft.util.Mth.floor(proportion*14)+(found>0?1:0);
    }
    public static void giveItemToPlayer(Player player, ItemStack stack) { giveItemToPlayer(player,stack,-1); }
    public static void giveItemToPlayer(Player player, ItemStack stack, int preferredSlot) {
        if(stack.isEmpty())return;
        throw new UnsupportedOperationException("Source player-main inventory helper requires its wrapper and pickup/drop lifecycle adapter");
    }
}
