package com.fuckingdeveloper.lms.runtime;

import net.minecraft.resources.Identifier;

/**
 * Forge 1.19.2 ResourceLocation construction compatibility.
 *
 * <p>The legacy two-part constructor is strict construction. Minecraft 26.3
 * exposes the equivalent contract as Identifier.fromNamespaceAndPath; tryBuild
 * is intentionally not used because it is the nullable/lenient alternative.</p>
 */
public final class Forge1192IdentifierBridge {
    private Forge1192IdentifierBridge() {}

    public static Identifier create(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }
}
