package com.fuckingdeveloper.lms.runtime.render;

import com.fuckingdeveloper.lms.runtime.event.LegacyModelBakingCompletedEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.ItemQuads;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.ModelEvent;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/** Semantic bridge from the combined 1.19.2 baked-model registry to 26.3. */
public final class Forge1192ModelBakingBridge {
    private static final ThreadLocal<Context> ACTIVE = new ThreadLocal<>();

    private Forge1192ModelBakingBridge() {}

    public static Context context(
            ModelEvent.ModifyBakingResult mutable, ModelEvent.BakingCompleted completed) {
        return new Context(mutable, completed);
    }

    public static void invoke(Context context, Consumer<LegacyModelBakingCompletedEvent> listener) {
        if (ACTIVE.get() != null) {
            throw new IllegalStateException("Nested legacy model-baking callback");
        }
        ACTIVE.set(context);
        try {
            listener.accept(new LegacyModelBakingCompletedEvent(context));
        } finally {
            ACTIVE.remove();
        }
    }

    public static Object registrationContext(Minecraft ignored) {
        return requireActive();
    }

    public static Object itemModelRegistration(Object context) {
        return requireContext(context);
    }

    public static void registerItemModel(
            Object context, Item item, LegacyModelResourceLocation modelKey) {
        Context active = requireContext(context);
        Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
        if (itemId == null || !itemId.equals(modelKey.identifier())
                || !"inventory".equals(modelKey.getVariant())) {
            throw new IllegalStateException("Legacy item model registration cannot be represented: item="
                    + itemId + " model=" + modelKey);
        }
        active.markItemRegistration(itemId);
    }

    public static LegacyModelResourceLocation stateModelKey(BlockState state) {
        return LegacyModelResourceLocation.fromBlockState(state);
    }

    public static Function<Identifier, net.minecraft.client.renderer.texture.TextureAtlasSprite>
            spriteLookup(Minecraft minecraft, Identifier atlasTexture) {
        return texture -> minecraft.getAtlasManager().get(new SpriteId(atlasTexture, texture));
    }

    private static Context requireActive() {
        Context context = ACTIVE.get();
        if (context == null) {
            throw new IllegalStateException("No active legacy model-baking callback");
        }
        return context;
    }

    private static Context requireContext(Object value) {
        Context active = requireActive();
        if (value != active) {
            throw new IllegalStateException("Foreign legacy item-model registration context");
        }
        return active;
    }

    public static final class Context {
        private final ModelEvent.ModifyBakingResult mutable;
        private final ModelEvent.BakingCompleted completed;
        private final Map<Object, Object> models;
        private final java.util.Set<Identifier> itemRegistrations = new java.util.HashSet<>();

        private Context(
                ModelEvent.ModifyBakingResult mutable, ModelEvent.BakingCompleted completed) {
            this.mutable = Objects.requireNonNull(mutable, "mutable baking event");
            this.completed = Objects.requireNonNull(completed, "completed baking event");
            if (mutable.getModelBakery() != completed.getModelBakery()) {
                throw new IllegalStateException("Model-baking lifecycle contexts do not match");
            }
            this.models = new LegacyModelMap(this);
        }

        public Map<Object, Object> models() {
            return models;
        }

        private ModelBakery.BakingResult result() {
            return mutable.getBakingResult();
        }

        private Material.Baked missingMaterial() {
            return result().missingModels().blockPart().particleMaterial();
        }

        private void markItemRegistration(Identifier itemId) {
            if (!itemRegistrations.add(itemId)) {
                throw new IllegalStateException("Legacy item model registered more than once: " + itemId);
            }
            if (!result().itemStackModels().containsKey(itemId)) {
                throw new IllegalStateException("Legacy item model key was not populated before registration: "
                        + itemId);
            }
        }
    }

    private static final class LegacyModelMap extends AbstractMap<Object, Object> {
        private final Context context;

        private LegacyModelMap(Context context) {
            this.context = context;
        }

        @Override
        public Object get(Object key) {
            LegacyModelResourceLocation location = requireKey(key);
            boolean missing;
            if (location.blockState() != null) {
                BlockStateModel current = context.result().blockStateModels()
                        .get(location.blockState());
                missing = current == null || current == context.result().missingModels().block();
            } else if ("inventory".equals(location.getVariant())) {
                ItemModel current = context.result().itemStackModels().get(location.identifier());
                missing = current == null || current == context.result().missingModels().item();
            } else {
                throw new IllegalStateException("Legacy model key lacks authoritative block state: "
                        + location);
            }
            return new ModelSentinel(missing ? context.missingMaterial().sprite() : null);
        }

        @Override
        public Object put(Object key, Object value) {
            LegacyModelResourceLocation location = requireKey(key);
            if (!(value instanceof LegacyBakedModel model)) {
                throw new IllegalArgumentException("Not a legacy baked model: " + value);
            }
            Object previous = get(location);
            if (location.blockState() != null) {
                context.result().blockStateModels().put(location.blockState(),
                        new LegacyBlockModel(model, location.blockState(), context.missingMaterial()));
            } else if ("inventory".equals(location.getVariant())) {
                context.result().itemStackModels().put(location.identifier(),
                        new LegacyItemModel(model, context.missingMaterial()));
            } else {
                throw new IllegalStateException("Unsupported legacy model variant: " + location);
            }
            return previous;
        }

        @Override
        public Set<Entry<Object, Object>> entrySet() {
            throw new UnsupportedOperationException(
                    "Iteration over the split target model registries is not source-equivalent");
        }

        private static LegacyModelResourceLocation requireKey(Object key) {
            if (key instanceof LegacyModelResourceLocation location) return location;
            throw new IllegalArgumentException("Not a Forge 1.19.2 model key: " + key);
        }
    }

    private record LegacyBlockModel(
            LegacyBakedModel legacy, BlockState state, Material.Baked fallbackMaterial)
            implements BlockStateModel {
        @Override
        public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
            output.add(new LegacyBlockPart(legacy, state, random, fallbackMaterial));
        }

        @Override
        public Material.Baked particleMaterial() {
            return materialFor(legacy.m_213637_(state, null, RandomSource.create(0L)),
                    fallbackMaterial);
        }

        @Override
        public int materialFlags() {
            return aggregateMaterialFlags(
                    legacy.m_213637_(state, null, RandomSource.create(0L)));
        }
    }

    private record LegacyBlockPart(
            LegacyBakedModel legacy, BlockState state, RandomSource random,
            Material.Baked fallbackMaterial) implements BlockStateModelPart {
        @Override
        public List<BakedQuad> getQuads(@Nullable Direction direction) {
            return List.copyOf(legacy.m_213637_(state, direction, random));
        }

        @Override
        public boolean useAmbientOcclusion() {
            return legacy.m_7541_();
        }

        @Override
        public Material.Baked particleMaterial() {
            return materialFor(getQuads(null), fallbackMaterial);
        }

        @Override
        public int materialFlags() {
            return aggregateMaterialFlags(getQuads(null));
        }
    }

    private record LegacyItemModel(LegacyBakedModel legacy, Material.Baked fallbackMaterial)
            implements ItemModel {
        @Override
        public void update(
                ItemStackRenderState output, ItemStack item, ItemModelResolver resolver,
                ItemDisplayContext displayContext, @Nullable ClientLevel level,
                @Nullable ItemOwner owner, int seed) {
            net.minecraft.world.entity.LivingEntity living = owner == null
                    ? null : owner.asLivingEntity();
            LegacyBakedModel selected = legacy.m_7343_()
                    .m_173464_(legacy, item, level, living, seed);
            if (selected == null) selected = legacy;
            List<BakedQuad> quads = List.copyOf(selected.m_213637_(
                    null, null, RandomSource.create(seed)));
            output.appendModelIdentityElement(this);
            ItemStackRenderState.LayerRenderState layer = output.newLayer();
            layer.setQuads(ItemQuads.split(quads));
            layer.setUsesBlockLight(selected.m_7547_());
            layer.setParticleMaterial(materialFor(quads, fallbackMaterial));
            layer.setItemTransform(transforms(selected).getTransform(displayContext));
            layer.setExtents(() -> CuboidItemModelWrapper.computeExtents(quads));
            if (item.hasFoil()) {
                layer.setFoilType(ItemStackRenderState.FoilType.STANDARD);
                output.setAnimated();
            }
            if ((aggregateMaterialFlags(quads) & BakedQuad.FLAG_ANIMATED) != 0) {
                output.setAnimated();
            }
        }
    }

    private static ItemTransforms transforms(LegacyBakedModel model) {
        Method accessor;
        try {
            try {
                accessor = model.getClass().getMethod("getTransformMap");
            } catch (NoSuchMethodException missingCustomAccessor) {
                accessor = model.getClass().getMethod("m_7442_");
            }
            Object value = accessor.invoke(model);
            if (value instanceof ItemTransforms transforms) return transforms;
            throw new IllegalStateException("Legacy model returned an invalid item transform map: "
                    + value);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalStateException("Legacy baked model has no accessible transform map: "
                    + model.getClass().getName(), e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("Legacy baked model transform lookup failed: "
                    + model.getClass().getName(), e.getCause());
        }
    }

    private static Material.Baked materialFor(
            List<BakedQuad> quads, Material.Baked fallback) {
        return quads.isEmpty() ? fallback : new Material.Baked(
                quads.getFirst().materialInfo().sprite(), fallback.forceTranslucent());
    }

    private static int aggregateMaterialFlags(List<BakedQuad> quads) {
        int flags = 0;
        for (BakedQuad quad : quads) flags |= quad.materialInfo().flags();
        return flags;
    }

    private record ModelSentinel(net.minecraft.client.renderer.texture.TextureAtlasSprite sprite)
            implements LegacyBakedModel {
        @Override public List<BakedQuad> m_213637_(BlockState state, Direction direction, RandomSource random) {
            return List.of();
        }
        @Override public boolean m_7541_() { return true; }
        @Override public boolean m_7539_() { return true; }
        @Override public boolean m_7547_() { return false; }
        @Override public boolean m_7521_() { return false; }
        @Override public net.minecraft.client.renderer.texture.TextureAtlasSprite m_6160_() { return sprite; }
        @Override public net.minecraft.client.renderer.texture.TextureAtlasSprite getParticleIcon(
                net.neoforged.neoforge.model.data.ModelData data) { return sprite; }
        @Override public LegacyItemOverrides m_7343_() { return LegacyItemOverrides.f_111734_; }
    }
}
