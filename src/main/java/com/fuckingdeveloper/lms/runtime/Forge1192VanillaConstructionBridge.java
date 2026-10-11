package com.fuckingdeveloper.lms.runtime;

import com.mojang.datafixers.types.Type;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

/** Exact construction adapters for vanilla constructors removed after 1.19.2. */
public final class Forge1192VanillaConstructionBridge {
    private static final org.slf4j.Logger LOG =
            org.slf4j.LoggerFactory.getLogger(Forge1192VanillaConstructionBridge.class);
    private static final Map<Item.Properties, CreativeModeTab> LEGACY_TABS_BY_PROPERTIES =
            Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<net.minecraft.resources.Identifier, CreativeModeTab> LEGACY_TABS_BY_ITEM_ID =
            new ConcurrentHashMap<>();
    private static final AtomicBoolean CREATIVE_TAB_GAP_REPORTED = new AtomicBoolean();
    private static final Map<FoodProperties.Builder, LegacyFoodIntent> LEGACY_FOOD_BUILDERS =
            Collections.synchronizedMap(new IdentityHashMap<>());
    private static final Map<FoodProperties, LegacyFoodIntent> LEGACY_FOODS =
            Collections.synchronizedMap(new IdentityHashMap<>());
    private static final Map<BlockBehaviour.Properties, LegacyMaterial> LEGACY_MATERIALS_BY_PROPERTIES =
            Collections.synchronizedMap(new WeakHashMap<>());

    private record LegacyFoodEffect(Supplier<MobEffectInstance> effect, float probability) {}
    private static final class LegacyFoodIntent {
        private boolean fast;
        private final List<LegacyFoodEffect> effects = new ArrayList<>();
    }

    private Forge1192VanillaConstructionBridge() {}

    /** Source Material semantics consumed by the Minecraft 26.3 properties adapter. */
    public enum LegacyMaterial {
        AIR(MapColor.NONE, false, false, false, false, true, false, PushReaction.PUSH_PULL),
        CLOTH_DECORATION(MapColor.WOOL, false, false, false, true, false, false, PushReaction.POPPED),
        PLANT(MapColor.PLANT, false, false, false, false, false, false, PushReaction.POPPED),
        DECORATION(MapColor.NONE, false, false, false, false, false, false, PushReaction.POPPED),
        WOOL(MapColor.WOOL, false, true, true, true, false, true, PushReaction.PUSH_PULL),
        EXPLOSIVE(MapColor.FIRE, false, true, true, true, false, false, PushReaction.PUSH_PULL),
        GLASS(MapColor.NONE, false, true, true, false, false, false, PushReaction.PUSH_PULL),
        STONE(MapColor.STONE, false, true, true, false, false, true, PushReaction.PUSH_PULL),
        METAL(MapColor.METAL, false, true, true, false, false, true, PushReaction.PUSH_PULL),
        PISTON(MapColor.STONE, false, true, true, false, false, true, PushReaction.IMMOVEABLE),
        CAKE(MapColor.NONE, false, true, true, false, false, true, PushReaction.POPPED),
        SAND(MapColor.SAND, false, true, true, false, false, true, PushReaction.PUSH_PULL),
        WOOD(MapColor.WOOD, false, true, true, true, false, true, PushReaction.PUSH_PULL);

        private final MapColor color;
        private final boolean liquid;
        private final boolean solid;
        private final boolean blocksMotion;
        private final boolean flammable;
        private final boolean replaceable;
        private final boolean solidBlocking;
        private final PushReaction pushReaction;

        LegacyMaterial(MapColor color, boolean liquid, boolean solid, boolean blocksMotion,
                       boolean flammable, boolean replaceable, boolean solidBlocking,
                       PushReaction pushReaction) {
            this.color = color;
            this.liquid = liquid;
            this.solid = solid;
            this.blocksMotion = blocksMotion;
            this.flammable = flammable;
            this.replaceable = replaceable;
            this.solidBlocking = solidBlocking;
            this.pushReaction = pushReaction;
        }
    }

    /** Preserve the source Material contribution to BlockBehaviour.Properties. */
    public static BlockBehaviour.Properties legacyMaterialProperties(LegacyMaterial material) {
        return legacyMaterialProperties(material, material.color);
    }

    /** Preserve an explicit source map-color override in Properties.of. */
    public static BlockBehaviour.Properties legacyMaterialProperties(
            LegacyMaterial material, MapColor color) {
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
                .mapColor(color)
                .pushReaction(material.pushReaction)
                .isRedstoneConductor((state, level, pos) -> material.blocksMotion
                        && state.isCollisionShapeFullBlock(level, pos))
                .isSuffocating((state, level, pos) -> material.solidBlocking
                        && state.isCollisionShapeFullBlock(level, pos));
        properties = material.solid ? properties.forceSolidOn() : properties.forceSolidOff();
        if (material.liquid) properties = properties.liquid();
        if (material.flammable) properties = properties.ignitedByLava();
        if (material.replaceable) properties = properties.replaceable();
        LEGACY_MATERIALS_BY_PROPERTIES.put(properties, material);
        return properties;
    }

    /** Recover behavior that legacy door-like blocks derived from Material. */
    public static BlockSetType legacyBlockSetType(BlockBehaviour.Properties properties) {
        LegacyMaterial material = LEGACY_MATERIALS_BY_PROPERTIES.get(properties);
        if (material == LegacyMaterial.WOOD) return BlockSetType.OAK;
        if (material == LegacyMaterial.METAL) return BlockSetType.IRON;
        throw new IllegalStateException("Legacy door-like block has no supported Material semantics");
    }

    /** Attach the legacy block name before the modern Block constructor reads it. */
    public static BlockBehaviour.Properties withBlockId(
            BlockBehaviour.Properties properties, String legacyName) {
        return properties.setId(ResourceKey.create(
                Registries.BLOCK,
                Forge1192RegistrationContext.checkPrefix(legacyName, false)));
    }

    /**
     * Preserve legacy construction-time tab intent for a future modern
     * creative-tab population bridge. This is deliberately not presented as
     * completed tab support: custom legacy tabs do not have a proven 26.3
     * lifecycle mapping yet.
     */
    public static Item.Properties recordLegacyCreativeTab(
            Item.Properties properties, CreativeModeTab legacyTab) {
        LEGACY_TABS_BY_PROPERTIES.put(properties, legacyTab);
        if (CREATIVE_TAB_GAP_REPORTED.compareAndSet(false, true)) {
            LOG.warn("LMS preserved legacy Item.Properties creative-tab membership, but modern creative-tab "
                    + "population is not implemented yet; items will not be silently claimed as populated");
        }
        return properties;
    }

    /** Use the legacy block's explicit registry name for its paired block item. */
    public static Item.Properties withBlockItemId(Block block, Item.Properties properties) {
        try {
            Object value = block.getClass().getMethod("getRegistryName").invoke(block);
            if (!(value instanceof net.minecraft.resources.Identifier id)) {
                throw new IllegalStateException("Legacy block registry name is not an Identifier: " + value);
            }
            rememberCreativeTab(properties, id);
            return properties.setId(ResourceKey.create(Registries.ITEM, id));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Legacy block does not expose getRegistryName(): "
                    + block.getClass().getName(), e);
        }
    }

    /** Preserve Block#asItem during staged legacy registration. */
    public static void associateBlockItem(Block block, Item item) {
        Forge1192RegistrationContext.requireActive();
        Item.BY_BLOCK.put(block, item);
    }

    /** Attach an explicitly named legacy item's modern registry key. */
    public static Item.Properties withItemId(Item.Properties properties, String legacyName) {
        net.minecraft.resources.Identifier id =
                Forge1192RegistrationContext.checkPrefix(legacyName, false);
        rememberCreativeTab(properties, id);
        return properties.setId(ResourceKey.create(Registries.ITEM, id));
    }

    /**
     * 1.19.2's EntityType.Builder#build accepted the DFU identity as a
     * string. Modern Minecraft requires the equivalent typed registry key.
     */
    public static <T extends Entity> EntityType<T> entityType(
            EntityType.Builder<T> builder, String legacyName) {
        return builder.build(ResourceKey.create(
                Registries.ENTITY_TYPE,
                Forge1192RegistrationContext.checkPrefix(legacyName, false)));
    }

    /** Resolve a legacy raw built-in effect to its canonical modern holder. */
    public static Holder<MobEffect> mobEffectHolder(MobEffect effect) {
        return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect);
    }

    public static FoodProperties.Builder fastFood(FoodProperties.Builder builder) {
        LEGACY_FOOD_BUILDERS.computeIfAbsent(builder, ignored -> new LegacyFoodIntent()).fast = true;
        return builder;
    }

    public static FoodProperties.Builder foodEffect(
            FoodProperties.Builder builder, Supplier<MobEffectInstance> effect, float probability) {
        LEGACY_FOOD_BUILDERS.computeIfAbsent(builder, ignored -> new LegacyFoodIntent())
                .effects.add(new LegacyFoodEffect(effect, probability));
        return builder;
    }

    public static FoodProperties buildFood(FoodProperties.Builder builder) {
        FoodProperties food = builder.build();
        LegacyFoodIntent intent = LEGACY_FOOD_BUILDERS.remove(builder);
        if (intent != null) LEGACY_FOODS.put(food, intent);
        return food;
    }

    public static Item.Properties food(Item.Properties properties, FoodProperties food) {
        LegacyFoodIntent intent = LEGACY_FOODS.get(food);
        if (intent == null) return properties.food(food);
        Consumable.Builder consumable = Consumables.defaultFood();
        if (intent.fast) consumable.consumeSeconds(0.8F);
        for (LegacyFoodEffect effect : intent.effects) {
            consumable.onConsume(new ApplyStatusEffectsConsumeEffect(
                    effect.effect().get(), effect.probability()));
        }
        return properties.food(food, consumable.build());
    }

    /** Retained intent for the eventual modern creative-tab population bridge. */
    public static Optional<CreativeModeTab> legacyCreativeTabIntent(
            net.minecraft.resources.Identifier itemId) {
        return Optional.ofNullable(LEGACY_TABS_BY_ITEM_ID.get(itemId));
    }

    /**
     * Recover a legacy item's recorded tab solely to propagate its construction
     * intent to another item. This does not populate a modern creative tab.
     */
    public static CreativeModeTab legacyCreativeTab(Item item) {
        net.minecraft.resources.Identifier id = Forge1192RegistrationContext.stagedIdentifier(item)
                .or(() -> BuiltInRegistries.ITEM.getResourceKey(item).map(ResourceKey::identifier))
                .orElseThrow(() -> new IllegalStateException(
                        "Legacy creative-tab lookup has no authoritative item identifier"));
        CreativeModeTab tab = LEGACY_TABS_BY_ITEM_ID.get(id);
        if (tab == null) {
            throw new IllegalStateException("No preserved legacy creative-tab intent for " + id);
        }
        return tab;
    }

    private static void rememberCreativeTab(Item.Properties properties,
                                            net.minecraft.resources.Identifier itemId) {
        CreativeModeTab tab = LEGACY_TABS_BY_PROPERTIES.remove(properties);
        if (tab != null) LEGACY_TABS_BY_ITEM_ID.put(itemId, tab);
    }

    /**
     * 1.19.2 exposed BlockEntityType(supplier, validBlocks, dataFixerType).
     * The public builder is the surviving construction contract. The legacy
     * data-fixer argument was commonly null and modern Builder#build retains it.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static BlockEntityType<?> blockEntityType(
            BlockEntityType.BlockEntitySupplier<?> supplier, Set<?> validBlocks, Type<?> dataFixerType) {
        // Minecraft 26.3/NeoForge keeps direct construction, but dropped the
        // old DataFixer Type parameter. It did not move to a Builder API.
        // The legacy third argument carried DFU schema metadata, not runtime
        // factory/valid-block semantics, so preserve those two surviving parts.
        return new BlockEntityType<>(
                (BlockEntityType.BlockEntitySupplier) supplier,
                (Set<Block>) (Set) validBlocks);
    }
}
