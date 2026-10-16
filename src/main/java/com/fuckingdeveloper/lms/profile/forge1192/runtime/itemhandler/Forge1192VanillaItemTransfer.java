package com.fuckingdeveloper.lms.profile.forge1192.runtime.itemhandler;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.capability.Forge1192BlockCapabilityBridge;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.capability.LegacyForgeCapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import java.util.Objects;

/** Imperative Forge source transfer contract, only after native discovery is empty. */
public final class Forge1192VanillaItemTransfer {
    private Forge1192VanillaItemTransfer() {}

    public static LegacyIItemHandler find(Level level, BlockPos pos, Direction side) {
        if (level.isClientSide()) throw new IllegalStateException("Source automation requires server level");
        var receiver=level.getBlockEntity(pos);
        if(receiver==null)return null;
        var optional=Forge1192BlockCapabilityBridge.getCapability(receiver,LegacyForgeCapabilities.ITEM_HANDLER,side);
        if(!optional.isPresent())return null;
        Object value=optional.orElseGet(()->{throw new IllegalStateException("Source item provider disappeared");});
        if(!(value instanceof LegacyIItemHandler handler))throw new IllegalStateException("Source item capability has invalid value " + value.getClass());
        return handler;
    }

    private static void immediate() {
        if(Transaction.getLifecycle()!=Transaction.Lifecycle.NONE)
            throw new IllegalStateException("Imperative source automation cannot join a native transaction");
    }

    public static boolean insertOne(Container from, int fromSlot, LegacyIItemHandler into) {
        immediate();
        var original=from.getItem(fromSlot);
        if(original.isEmpty())return false;
        var saved=original.copy();
        // Both pinned source Forge and native vanilla remove the hopper item before provider callbacks.
        var one=from.removeItem(fromSlot,1);
        for(int slot=0;slot<into.getSlots();slot++) {
            var simulated=Objects.requireNonNull(into.insertItem(slot,one.copy(),true),"Source insert simulation returned null");
            validateRemainder(one,simulated);
            if(!simulated.isEmpty())continue;
            var remainder=Objects.requireNonNull(into.insertItem(slot,one.copy(),false),"Source insert returned null");
            validateRemainder(one,remainder);
            if(!remainder.isEmpty())throw new IllegalStateException("Source insert disagrees with its simulation");
            // Native caller retains its normal source-container dirty/cooldown lifecycle.
            return true;
        }
        from.setItem(fromSlot,saved);
        return false;
    }

    public static boolean extractOne(LegacyIItemHandler from, Container into) {
        immediate();
        for(int slot=0;slot<from.getSlots();slot++) {
            var simulated=Objects.requireNonNull(from.extractItem(slot,1,true),"Source extract simulation returned null");
            if(simulated.isEmpty())continue;
            if(simulated.getCount()!=1)throw new IllegalStateException("Source extract simulation exceeded request");
            for(int target=0;target<into.getContainerSize();target++) {
                var current=into.getItem(target);
                if(!into.canPlaceItem(target,simulated))continue;
                if(Math.min(simulated.getMaxStackSize(),into.getMaxStackSize(simulated))<1)continue;
                if(!current.isEmpty() && (!ItemStack.isSameItemSameComponents(current,simulated)
                        || current.getCount()>=Math.min(current.getMaxStackSize(),into.getMaxStackSize(current))))continue;
                var extracted=Objects.requireNonNull(from.extractItem(slot,1,false),"Source extract returned null");
                if(extracted.isEmpty()||extracted.getCount()!=1||!ItemStack.isSameItemSameComponents(extracted,simulated))
                    throw new IllegalStateException("Source extract disagrees with its simulation");
                into.setItem(target,current.isEmpty()?extracted:current.copyWithCount(current.getCount()+1));
                into.setChanged();return true;
            }
        }
        return false;
    }

    private static void validateRemainder(ItemStack requested, ItemStack remainder) {
        if(!remainder.isEmpty() && (remainder.getCount()!=1 || !ItemStack.isSameItemSameComponents(requested,remainder)))
            throw new IllegalStateException("Source insert returned invalid single-item remainder");
    }
}
