package com.fuckingdeveloper.lms.profile.forge1192.runtime.fluid;

import com.fuckingdeveloper.lms.target.neoforge263.Target263DataComponentBindingBridge;
import com.fuckingdeveloper.lms.target.neoforge263.Target263PendingComponentDefaults;

import net.minecraft.core.*;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.*;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import java.lang.reflect.Constructor;

/** Materializes the actual target fluid prototype without binding global holders early. */
public final class Forge1192EarlyFluidStackBridge {
    private static final Constructor<FluidStack> CONSTRUCTOR=constructor();
    private Forge1192EarlyFluidStackBridge() {}
    public static FluidStack create(Fluid fluid, int amount) {
        var holder=fluid.builtInRegistryHolder();
        try { holder.components(); return new FluidStack(fluid,amount); }
        catch(NullPointerException unbound) { if(!"Components not bound yet".equals(unbound.getMessage()))throw unbound; }
        var provider=Target263DataComponentBindingBridge.currentRegistries().orElseGet(() -> RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        DataComponentMap defaults=Target263PendingComponentDefaults.materialize(holder,provider);
        try{return CONSTRUCTOR.newInstance(holder,amount,new PatchedDataComponentMap(defaults));}
        catch(ReflectiveOperationException failure){throw new IllegalStateException("Early source fluid construction failed",failure);}
    }
    @SuppressWarnings("unchecked") private static Constructor<FluidStack> constructor() {
        try {var result=FluidStack.class.getDeclaredConstructor(Holder.class,int.class,PatchedDataComponentMap.class);if(!result.trySetAccessible())throw new IllegalStateException("Native isolated fluid constructor inaccessible");return (Constructor<FluidStack>)result;}
        catch(ReflectiveOperationException failure){throw new ExceptionInInitializerError(failure);}
    }
}
