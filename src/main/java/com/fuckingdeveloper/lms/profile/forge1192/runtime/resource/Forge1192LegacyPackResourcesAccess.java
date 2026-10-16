package com.fuckingdeveloper.lms.profile.forge1192.runtime.resource;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

/** Typed access to the exact Forge 1.19.2 PackResources operations. */
public interface Forge1192LegacyPackResourcesAccess {
    InputStream lms$legacyGetRootResource(String path) throws IOException;
    InputStream lms$legacyGetResource(PackType type, Identifier id) throws IOException;
    Collection<Identifier> lms$legacyListResources(
            PackType type, String namespace, String directory, Predicate<Identifier> predicate);
    Set<String> lms$legacyNamespaces(PackType type);
    String lms$legacyPackName();
    void close();
}
