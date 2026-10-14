package com.fuckingdeveloper.lms.runtime;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.BucketResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.lang.reflect.Constructor;
import java.util.Optional;

/** Profile-owned compatibility for Forge 1.19.2 fluid identity operations. */
public final class Forge1192FluidBridge {
    private static final Constructor<ItemResource> ISOLATED_RESOURCE_CONSTRUCTOR =
            isolatedResourceConstructor();
    private static final Constructor<FluidStack> ISOLATED_FLUID_CONSTRUCTOR =
            isolatedFluidConstructor();

    private Forge1192FluidBridge() {}

    /**
     * Legacy isFluidEqual ignores amount and fluid stack metadata.
     * NeoForge's same-fluid predicate preserves that identity comparison.
     */
    public static boolean isFluidEqual(FluidStack left, FluidStack right) {
        return FluidStack.isSameFluid(left, right);
    }

    /**
     * Forge getFluidContained represented an empty container as Optional.empty().
     *
     * <p>The source query is valid while legacy setup is still contributing item
     * defaults. Target ItemAccess normally canonicalizes its stack through the
     * globally-bound default ItemResource, which is deliberately unavailable in
     * that window. Preserve the already materialized isolated components and
     * expose them to the target provider through a read-only access instead.
     */
    public static Optional<FluidStack> getFluidContained(ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();

        ItemResource resource = isolatedResource(stack);
        ItemAccess access = new ReadOnlyItemAccess(resource, stack.getCount());
        var handler = Capabilities.Fluid.ITEM.getCapability(stack, access);
        if (handler == null) return Optional.empty();

        // NeoForge's built-in bucket provider derives a FluidResource from the
        // fluid's global default resource. During the pre-binding source
        // lifecycle that global prototype is intentionally unavailable, but
        // the exact provider semantics are fully determined by the bucket item.
        if (handler instanceof BucketResourceHandler) {
            Fluid fluid;
            if (stack.getItem() instanceof BucketItem bucket) {
                fluid = bucket.content;
            } else if (stack.getItem() == Items.MILK_BUCKET && NeoForgeMod.MILK.isBound()) {
                fluid = NeoForgeMod.MILK.get();
            } else {
                throw new IllegalStateException("Unknown target bucket fluid provider item "
                        + stack.getItem());
            }
            return fluid == Fluids.EMPTY
                    ? Optional.empty()
                    : Optional.of(isolatedFluidStack(fluid,
                            net.neoforged.neoforge.fluids.FluidType.BUCKET_VOLUME));
        }

        for (int index = 0; index < handler.size(); index++) {
            FluidStack contained =
                    net.neoforged.neoforge.transfer.fluid.FluidUtil.getStack(handler, index);
            if (!contained.isEmpty()) return Optional.of(contained);
        }
        return Optional.empty();
    }

    private static ItemResource isolatedResource(ItemStack stack) {
        try {
            return ISOLATED_RESOURCE_CONSTRUCTOR.newInstance(stack.copyWithCount(1));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot materialize isolated early ItemResource", e);
        }
    }

    private static FluidStack isolatedFluidStack(Fluid fluid, int amount) {
        try {
            return ISOLATED_FLUID_CONSTRUCTOR.newInstance(
                    fluid.builtInRegistryHolder(), amount,
                    new PatchedDataComponentMap(DataComponentMap.EMPTY));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot materialize isolated early FluidStack", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Constructor<ItemResource> isolatedResourceConstructor() {
        try {
            Constructor<?> constructor = ItemResource.class.getDeclaredConstructor(ItemStack.class);
            if (!constructor.trySetAccessible()) {
                throw new IllegalStateException("ItemResource isolated constructor is inaccessible");
            }
            return (Constructor<ItemResource>) constructor;
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Constructor<FluidStack> isolatedFluidConstructor() {
        try {
            Constructor<?> constructor = FluidStack.class.getDeclaredConstructor(
                    Holder.class, int.class, PatchedDataComponentMap.class);
            if (!constructor.trySetAccessible()) {
                throw new IllegalStateException("FluidStack isolated constructor is inaccessible");
            }
            return (Constructor<FluidStack>) constructor;
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private record ReadOnlyItemAccess(ItemResource resource, int amount) implements ItemAccess {
        @Override
        public ItemResource getResource() {
            return resource;
        }

        @Override
        public int getAmount() {
            return amount;
        }

        @Override
        public int insert(ItemResource ignored, int ignoredAmount, TransactionContext transaction) {
            return 0;
        }

        @Override
        public int extract(ItemResource ignored, int ignoredAmount, TransactionContext transaction) {
            return 0;
        }
    }
}
