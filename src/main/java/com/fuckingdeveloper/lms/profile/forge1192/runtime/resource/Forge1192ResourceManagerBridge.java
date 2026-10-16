package com.fuckingdeveloper.lms.profile.forge1192.runtime.resource;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/** Target adapter for the Forge 1.19.2 resource-query functional contract. */
public final class Forge1192ResourceManagerBridge {
    private Forge1192ResourceManagerBridge() {}

    public static Map<Identifier, Resource> listResources(
            ResourceManager manager, String directory, Predicate<Identifier> predicate) {
        Objects.requireNonNull(predicate, "legacy resource predicate");
        return manager.listResources(directory, predicate::test);
    }

    public static Map<Identifier, List<Resource>> listResourceStacks(
            ResourceManager manager, String directory, Predicate<Identifier> predicate) {
        Objects.requireNonNull(predicate, "legacy resource predicate");
        return manager.listResourceStacks(directory, predicate::test);
    }
}
