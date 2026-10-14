package com.fuckingdeveloper.lms.runtime.capability;

/** Exact source built-in token identities; target provider services remain separate. */
public final class LegacyForgeCapabilities {
    private LegacyForgeCapabilities() {}
    public static final LegacyCapability<Object> ENERGY = LegacyCapabilityManager.named("net/minecraftforge/energy/IEnergyStorage", false);
    public static final LegacyCapability<Object> FLUID_HANDLER = LegacyCapabilityManager.named("net/minecraftforge/fluids/capability/IFluidHandler", false);
    public static final LegacyCapability<Object> FLUID_HANDLER_ITEM = LegacyCapabilityManager.named("net/minecraftforge/fluids/capability/IFluidHandlerItem", false);
    public static final LegacyCapability<Object> ITEM_HANDLER = LegacyCapabilityManager.named("net/minecraftforge/items/IItemHandler", false);
}
