package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import java.util.Collections;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.renderer.LevelRenderer;

/** Captures source fixed-buffer registration intent after that target map was removed. */
public final class Forge1192FixedBufferBridge {
    private static final Set<LegacyRenderType> REGISTERED = ConcurrentHashMap.newKeySet();
    private static final SortedMap<Object, Object> CONTEXT = Collections.emptySortedMap();

    private Forge1192FixedBufferBridge() {}

    @SuppressWarnings("rawtypes")
    public static SortedMap registrationContext(LevelRenderer renderer) {
        if (renderer == null) throw new IllegalStateException("Level renderer is not initialized");
        return CONTEXT;
    }

    public static void register(LegacyRenderType renderType) {
        if (renderType == null) throw new IllegalArgumentException("Legacy render type is null");
        REGISTERED.add(renderType);
    }

    public static Set<LegacyRenderType> registeredTypes() {
        return Set.copyOf(REGISTERED);
    }
}
