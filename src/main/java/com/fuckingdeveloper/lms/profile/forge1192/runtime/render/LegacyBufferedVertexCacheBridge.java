package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/** Lifecycle boundary for source persistent GPU buffers removed by the target renderer. */
public final class LegacyBufferedVertexCacheBridge {
    private static final Map<Object, Boolean> FINISHED =
            Collections.synchronizedMap(new WeakHashMap<>());

    private LegacyBufferedVertexCacheBridge() {}

    public static void finish(Object cache) {
        FINISHED.put(cache, Boolean.TRUE);
    }

    public static void draw(Object cache) {
        if (!FINISHED.containsKey(cache)) return;
        throw new UnsupportedOperationException(
                "Forge 1.19.2 persistent vertex-cache drawing requires a Minecraft 26.3 render-pass adapter");
    }

    public static void delete(Object cache) {
        FINISHED.remove(cache);
    }
}
