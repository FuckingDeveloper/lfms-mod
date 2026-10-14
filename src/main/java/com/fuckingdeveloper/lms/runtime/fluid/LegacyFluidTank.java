package com.fuckingdeveloper.lms.runtime.fluid;

/**
 * Structural superclass boundary for legacy FluidTank subclasses.
 * Executable fluid operations must be proven and bridged separately.
 */
public class LegacyFluidTank implements LegacyIFluidHandler, LegacyIFluidTank {
    protected int capacity;

    public LegacyFluidTank(int capacity) {
        this.capacity = capacity;
    }
}
