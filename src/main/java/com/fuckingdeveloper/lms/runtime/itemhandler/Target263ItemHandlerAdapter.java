package com.fuckingdeveloper.lms.runtime.itemhandler;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Source imperative/simulation operations over authoritative target transactions. */
public final class Target263ItemHandlerAdapter implements LegacyIItemHandler {
    private final ResourceHandler<ItemResource> handler;
    private final BooleanSupplier valid;

    public Target263ItemHandlerAdapter(ResourceHandler<ItemResource> handler, BooleanSupplier valid) {
        this.handler = Objects.requireNonNull(handler);
        this.valid = Objects.requireNonNull(valid);
    }

    private void checkValid() {
        if (!valid.getAsBoolean()) throw new IllegalStateException("Legacy target item capability was invalidated");
    }

    @Override public int getSlots() { checkValid(); return handler.size(); }

    @Override public ItemStack getStackInSlot(int slot) {
        checkValid();
        return handler.getResource(slot).toStack(Math.toIntExact(handler.getAmountAsLong(slot)));
    }

    @Override public int getSlotLimit(int slot) {
        checkValid();
        return Math.toIntExact(handler.getCapacityAsLong(slot, ItemResource.EMPTY));
    }

    @Override public boolean isItemValid(int slot, ItemStack stack) {
        checkValid();
        return !stack.isEmpty() && handler.isValid(slot, ItemResource.of(stack));
    }

    @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        checkValid();
        if (stack.isEmpty()) return stack;
        int requested = stack.getCount();
        int inserted;
        try (var transaction = Transaction.openRoot()) {
            inserted = handler.insert(slot, ItemResource.of(stack), requested, transaction);
            if (inserted < 0 || inserted > requested) throw new IllegalStateException("Invalid target insertion amount " + inserted);
            if (!simulate) transaction.commit();
        }
        return inserted == 0 ? stack : inserted == requested ? ItemStack.EMPTY : stack.copyWithCount(requested - inserted);
    }

    @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
        checkValid();
        if (amount < 0) throw new IllegalArgumentException("Negative source extraction amount");
        if (amount == 0) return ItemStack.EMPTY;
        ItemResource resource = handler.getResource(slot);
        if (resource.isEmpty()) return ItemStack.EMPTY;
        int extracted;
        try (var transaction = Transaction.openRoot()) {
            extracted = handler.extract(slot, resource, amount, transaction);
            if (extracted < 0 || extracted > amount) throw new IllegalStateException("Invalid target extraction amount " + extracted);
            if (!simulate) transaction.commit();
        }
        return resource.toStack(extracted);
    }
}
