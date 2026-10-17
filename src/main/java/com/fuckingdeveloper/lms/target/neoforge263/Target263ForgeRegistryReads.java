package com.fuckingdeveloper.lms.target.neoforge263;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

/** Only already-proven process-global identities; never substitute a world registry. */
public final class Target263ForgeRegistryReads {
    private Target263ForgeRegistryReads() {}
    public static Registry<?> resolve(Identifier identity) {
        return BuiltInRegistries.REGISTRY.getOptional(identity).orElseThrow(() -> new UnsupportedOperationException(
                "No native process-global registry for source identity " + identity));
    }
}
