package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import java.util.Objects;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

/** Linkage facade for the removed 1.19.2 immediate block dispatcher. */
public final class LegacyBlockRenderDispatcher {
    private final BlockModelResolver resolver;
    private final LegacyModelBlockRenderer modelRenderer = new LegacyModelBlockRenderer();

    private LegacyBlockRenderDispatcher(BlockModelResolver resolver) {
        this.resolver = Objects.requireNonNull(resolver, "resolver");
    }

    public static LegacyBlockRenderDispatcher from(BlockEntityRendererProvider.Context context) {
        return new LegacyBlockRenderDispatcher(context.blockModelResolver());
    }

    public static LegacyBlockRenderDispatcher from(net.minecraft.client.renderer.entity.EntityRendererProvider.Context context) {
        // Rewriting a source virtual getter must not silently bypass an unknown context subclass override.
        if (context.getClass() != net.minecraft.client.renderer.entity.EntityRendererProvider.Context.class)
            throw new UnsupportedOperationException("Unproven source entity renderer context subclass: " + context.getClass().getName());
        return new LegacyBlockRenderDispatcher(context.getBlockModelResolver());
    }

    public LegacyBakedModel m_110910_(BlockState state) {
        throw unsupported("getBlockModel", state);
    }

    public LegacyModelBlockRenderer m_110937_() {
        return modelRenderer;
    }

    BlockModelResolver resolver() {
        return resolver;
    }

    private static UnsupportedOperationException unsupported(String operation, Object value) {
        return new UnsupportedOperationException(
                "Forge 1.19.2 immediate block rendering operation " + operation
                        + " requires migration to the Minecraft 26.3 extracted block "
                        + "render-state pipeline; value=" + value);
    }
}
