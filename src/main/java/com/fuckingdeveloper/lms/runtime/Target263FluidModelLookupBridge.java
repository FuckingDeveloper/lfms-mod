package com.fuckingdeveloper.lms.runtime;

import java.util.Map;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.world.level.material.Fluid;

/** Exact pending target models, available only during the target baking-result callback. */
public final class Target263FluidModelLookupBridge {
    private static final ThreadLocal<Map<Fluid,FluidModel>> ACTIVE = new ThreadLocal<>();
    private Target263FluidModelLookupBridge() {}
    public static Scope enter(Map<Fluid,FluidModel> models) {
        var previous=ACTIVE.get();ACTIVE.set(Map.copyOf(models));return new Scope(previous);
    }
    public static FluidModel current(Fluid fluid) {
        var models=ACTIVE.get();
        if(models==null)return null;
        var model=models.get(fluid);
        if(model==null)throw new IllegalStateException("No pending target fluid model for "+fluid);
        return model;
    }
    public static final class Scope implements AutoCloseable {
        private final Map<Fluid,FluidModel> previous;
        private Scope(Map<Fluid,FluidModel> previous){this.previous=previous;}
        public void close(){if(previous==null)ACTIVE.remove();else ACTIVE.set(previous);}
    }
}
