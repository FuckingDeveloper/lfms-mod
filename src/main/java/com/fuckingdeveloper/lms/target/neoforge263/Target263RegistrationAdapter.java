package com.fuckingdeveloper.lms.target.neoforge263;

import com.fuckingdeveloper.lms.runtime.CompatibilityContract;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegisterEvent;

/** Native registry mutation is confined to the provided active target event. */
public final class Target263RegistrationAdapter {
    private Target263RegistrationAdapter() {}
    public static Set<CompatibilityContract> contracts() {
        return Set.of(CompatibilityContract.PHASE_STAGED_REGISTRATION_V1);
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void register(RegisterEvent event,Identifier name,Object value) {
        Objects.requireNonNull(event); Objects.requireNonNull(name); Objects.requireNonNull(value);
        ResourceKey<? extends Registry<Object>> key = (ResourceKey)event.getRegistryKey();
        event.register(key,name,() -> value);
    }
}
