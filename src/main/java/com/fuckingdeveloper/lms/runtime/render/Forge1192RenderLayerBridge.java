package com.fuckingdeveloper.lms.runtime.render;

import com.mojang.logging.LogUtils;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;

/** Preserves removed Forge 1.19.2 block render-layer registrations. */
public final class Forge1192RenderLayerBridge {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<Block, Predicate<RenderType>> PREDICATES =
            Collections.synchronizedMap(new IdentityHashMap<>());
    private static boolean warned;

    private Forge1192RenderLayerBridge() {}

    @SuppressWarnings("unchecked")
    public static void register(Block block, Predicate<?> predicate) {
        Objects.requireNonNull(block, "legacy render-layer block");
        Objects.requireNonNull(predicate, "legacy render-layer predicate");
        PREDICATES.put(block, (Predicate<RenderType>) predicate);
        if (!warned) {
            warned = true;
            LOGGER.warn("LMS preserved Forge 1.19.2 block render-layer predicates, "
                    + "but Minecraft 26.3 assigns layers per baked quad; model-baking "
                    + "materialization is not implemented yet");
        }
    }

    public static Predicate<RenderType> predicate(Block block) {
        return PREDICATES.get(block);
    }
}
