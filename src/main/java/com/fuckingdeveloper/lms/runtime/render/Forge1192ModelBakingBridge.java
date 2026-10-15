package com.fuckingdeveloper.lms.runtime.render;

import com.fuckingdeveloper.lms.runtime.Forge1192VanillaConstructionBridge;
import com.fuckingdeveloper.lms.runtime.event.LegacyModelBakingCompletedEvent;
import com.fuckingdeveloper.lms.runtime.event.LegacyRegisterColorHandlersEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
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
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.quad.BakedColors;
import net.neoforged.neoforge.model.data.ModelData;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Identifier LEGACY_BLOCK_ATLAS =
            Identifier.withDefaultNamespace("textures/atlas/blocks.png");
    private static final Logger LOG = LoggerFactory.getLogger(Forge1192ModelBakingBridge.class);
    private static final ThreadLocal<Context> ACTIVE = new ThreadLocal<>();
    private static final Object ITEM_OVERRIDE_RESOLVER = new Object();

    private Forge1192ModelBakingBridge() {}

    public static Context context(ModelEvent.ModifyBakingResult mutable) {
        return new Context(mutable);
    }

    public static void invoke(Context context, Consumer<LegacyModelBakingCompletedEvent> listener) {
        if (ACTIVE.get() != null) {
            throw new IllegalStateException("Nested legacy model-baking callback");
        }
        ACTIVE.set(context);
        try {
            listener.accept(new LegacyModelBakingCompletedEvent(context));
            LOG.info("LMS legacy model-baking callback={} blockModels={} itemModels={}",
                    listener.getClass().getName(), context.blockModels, context.itemModels);
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

    public static Object itemOverrideResolver(Minecraft ignored) {
        return ITEM_OVERRIDE_RESOLVER;
    }

    public static LegacyBakedModel resolveItemOverride(
            Object resolver, ItemStack stack, net.minecraft.world.level.Level level,
            net.minecraft.world.entity.LivingEntity entity, int seed) {
        if (resolver != ITEM_OVERRIDE_RESOLVER) {
            throw new IllegalStateException("Foreign legacy item override resolver");
        }
        return new ItemOverrideSelection(stack);
    }

    public static void registerItemModel(
            Object context, Item item, LegacyModelResourceLocation modelKey) {
        Context active = requireContext(context);
        Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
        if (itemId == null || BuiltInRegistries.ITEM.getValue(itemId) != item) {
            throw new IllegalStateException("Unregistered legacy item model: " + item
                    + " resolvedId=" + itemId);
        }
        Identifier targetModelId =
                Forge1192VanillaConstructionBridge.itemModelIdentity(item);
        if (targetModelId == null) {
            throw new IllegalStateException(
                    "Registered legacy item has no target item-model component: " + itemId);
        }
        active.registerItemModel(itemId, targetModelId, modelKey);
    }

    public static LegacyModelResourceLocation stateModelKey(BlockState state) {
        return LegacyModelResourceLocation.fromBlockState(state);
    }

    public static Function<Identifier, net.minecraft.client.renderer.texture.TextureAtlasSprite>
            spriteLookup(Minecraft ignored, Identifier atlasTexture) {
        Context context = requireActive();
        if (!LEGACY_BLOCK_ATLAS.equals(atlasTexture)) {
            throw new IllegalStateException(
                    "Unsupported Forge 1.19.2 texture atlas during model baking: " + atlasTexture);
        }
        return texture -> {
            var preparations = context.mutable.getBlockAtlasPreparations();
            var sprite = preparations.getSprite(texture);
            if (sprite != null) return sprite;
            var missing = preparations.getSprite(MissingTextureAtlasSprite.getLocation());
            if (missing == null) {
                throw new IllegalStateException("Mutable target block atlas has no missing sprite");
            }
            return missing;
        };
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
        private final Map<Object, Object> models;
        private final Map<LegacyModelResourceLocation, LegacyBakedModel> legacyModels =
                new java.util.HashMap<>();
        private int blockModels;
        private int itemModels;

        private Context(ModelEvent.ModifyBakingResult mutable) {
            this.mutable = Objects.requireNonNull(mutable, "mutable baking event");
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

        private void registerItemModel(Identifier itemId, Identifier targetModelId,
                                       LegacyModelResourceLocation modelKey) {
            LegacyBakedModel model = legacyModels.get(modelKey);
            // Forge 1.19.2 ItemModelShaper#register stored into a map and a
            // later registration for the same item replaced the earlier one.
            if (model != null) {
                LegacyItemModel migrated = new LegacyItemModel(model, missingMaterial());
                result().itemStackModels().put(modelKey.identifier(), migrated);
                result().itemStackModels().put(targetModelId, migrated);
                itemModels++;
                return;
            }
            // A source callback may deliberately retain a model that was
            // already baked from resources (Map#get + conditional put) and
            // then register another item against that exact model key.
            ItemModel baked = result().itemStackModels().get(modelKey.identifier());
            if (baked == null || baked == result().missingModels().item()) {
                throw new IllegalStateException(
                        "Legacy item model key was not populated before registration: item="
                                + itemId + " model=" + modelKey);
            }
            result().itemStackModels().put(targetModelId, baked);
            itemModels++;
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
            context.legacyModels.put(location, model);
            if (location.blockState() != null) {
                context.result().blockStateModels().put(location.blockState(),
                        new LegacyBlockModel(model, location.blockState(), context.missingMaterial()));
                context.blockModels++;
            } else if ("inventory".equals(location.getVariant())) {
                context.result().itemStackModels().put(location.identifier(),
                        new LegacyItemModel(model, context.missingMaterial()));
                context.itemModels++;
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
            implements DynamicBlockStateModel {
        @Override
        public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
            output.add(new LegacyBlockPart(
                    legacy, state, random, ModelData.EMPTY, null, null, fallbackMaterial));
        }

        @Override
        public void collectParts(
                BlockAndTintGetter level, BlockPos pos, BlockState state,
                RandomSource random, List<BlockStateModelPart> output) {
            var snapshot = level.getModelData(pos);
            Target263ModelDataTrace.log(pos, "positioned-snapshot", snapshot);
            output.add(new LegacyBlockPart(
                    legacy, state, random, java.util.Objects.requireNonNull(
                            legacy.getModelData(level, pos, state, snapshot),
                            "Legacy baked model returned null model data"), level, pos, fallbackMaterial));
        }

        @Override
        public Material.Baked particleMaterial() {
            return new Material.Baked(legacy.getParticleIcon(ModelData.EMPTY),
                    fallbackMaterial.forceTranslucent());
        }

        @Override
        public int materialFlags() {
            return aggregateMaterialFlags(
                    legacy.m_213637_(state, null, RandomSource.create(0L)));
        }
    }

    private record LegacyBlockPart(
            LegacyBakedModel legacy, BlockState state, RandomSource random,
            ModelData modelData, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos,
            Material.Baked fallbackMaterial) implements BlockStateModelPart {
        @Override
        public List<BakedQuad> getQuads(@Nullable Direction direction) {
            return legacy.getQuads(state, direction, random, modelData, null).stream()
                    .map(quad -> applyLegacyBlockTint(quad, state, level, pos))
                    .toList();
        }

        @Override
        public boolean useAmbientOcclusion() {
            return legacy.m_7541_();
        }

        @Override
        public Material.Baked particleMaterial() {
            return new Material.Baked(legacy.getParticleIcon(modelData),
                    fallbackMaterial.forceTranslucent());
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
            if (selected instanceof ItemOverrideSelection selection) {
                resolver.appendItemLayers(
                        output, selection.stack(), displayContext, level, owner, seed);
                return;
            }
            List<BakedQuad> quads = collectItemQuads(selected);
            output.appendModelIdentityElement(this);
            ItemStackRenderState.LayerRenderState layer = output.newLayer();
            layer.setQuads(ItemQuads.split(quads));
            int highestTintIndex = highestTintIndex(quads);
            for (int tintIndex = 0; tintIndex <= highestTintIndex; tintIndex++) {
                int targetColor = LegacyRegisterColorHandlersEvent.Item.targetColor(
                        item, tintIndex);
                layer.tintLayers().add(targetColor);
                output.appendModelIdentityElement(targetColor);
            }
            layer.setUsesBlockLight(selected.m_7547_());
            layer.setParticleMaterial(new Material.Baked(selected.m_6160_(),
                    fallbackMaterial.forceTranslucent()));
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

    private record ItemOverrideSelection(ItemStack stack) implements LegacyBakedModel {
        private IllegalStateException invalidUse() {
            return new IllegalStateException(
                    "Selected target item stack escaped legacy override resolution");
        }

        @Override public List<BakedQuad> m_213637_(
                BlockState state, Direction direction, RandomSource random) { throw invalidUse(); }
        @Override public boolean m_7541_() { throw invalidUse(); }
        @Override public boolean m_7539_() { throw invalidUse(); }
        @Override public boolean m_7547_() { throw invalidUse(); }
        @Override public boolean m_7521_() { throw invalidUse(); }
        @Override public net.minecraft.client.renderer.texture.TextureAtlasSprite m_6160_() {
            throw invalidUse();
        }
        @Override public net.minecraft.client.renderer.texture.TextureAtlasSprite getParticleIcon(
                ModelData data) { throw invalidUse(); }
        @Override public LegacyItemOverrides m_7343_() { throw invalidUse(); }
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

    /** Match source ItemRenderer: six culled lists, then unculled, seed reset per list. */
    private static List<BakedQuad> collectItemQuads(LegacyBakedModel model) {
        var quads = new java.util.ArrayList<BakedQuad>();
        RandomSource random = RandomSource.create(42L);
        for (Direction direction : Direction.values()) {
            random.setSeed(42L);
            quads.addAll(model.m_213637_(null, direction, random));
        }
        random.setSeed(42L);
        quads.addAll(model.m_213637_(null, null, random));
        return List.copyOf(quads);
    }

    private static int aggregateMaterialFlags(List<BakedQuad> quads) {
        int flags = 0;
        for (BakedQuad quad : quads) flags |= quad.materialInfo().flags();
        return flags;
    }

    private static int highestTintIndex(List<BakedQuad> quads) {
        int highest = -1;
        for (BakedQuad quad : quads) {
            highest = Math.max(highest, quad.materialInfo().tintIndex());
        }
        return highest;
    }

    private static BakedQuad applyLegacyBlockTint(
            BakedQuad quad, BlockState state, @Nullable BlockAndTintGetter level,
            @Nullable BlockPos pos) {
        BakedQuad.MaterialInfo material = quad.materialInfo();
        int tintIndex = material.tintIndex();
        if (tintIndex < 0) return quad;
        int tint = LegacyRegisterColorHandlersEvent.Block.targetColor(
                state, level, pos, tintIndex);
        BakedQuad.MaterialInfo untinted = new BakedQuad.MaterialInfo(
                material.sprite(), material.layer(), material.itemRenderType(),
                material.itemGlintRenderType(), material.itemGlintSpecialRenderType(),
                -1, material.shadeDirectionOverride(), material.lightEmission(),
                material.ambientOcclusion());
        BakedQuad result = new BakedQuad(
                quad.position0(), quad.position1(), quad.position2(), quad.position3(),
                quad.packedUV0(), quad.packedUV1(), quad.packedUV2(), quad.packedUV3(),
                quad.direction(), untinted, quad.bakedNormals(), BakedColors.of(
                        ARGB.multiply(quad.bakedColors().color(0), tint),
                        ARGB.multiply(quad.bakedColors().color(1), tint),
                        ARGB.multiply(quad.bakedColors().color(2), tint),
                        ARGB.multiply(quad.bakedColors().color(3), tint)));
        return Forge1192BakedQuadBridge.rememberShade(
                result, Forge1192BakedQuadBridge.isShade(quad));
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
