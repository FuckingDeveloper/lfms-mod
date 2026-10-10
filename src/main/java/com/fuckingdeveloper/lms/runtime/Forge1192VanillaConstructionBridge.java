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

    private record LegacyFoodEffect(Supplier<MobEffectInstance> effect, float probability) {}
    private static final class LegacyFoodIntent {
        private boolean fast;
        private final List<LegacyFoodEffect> effects = new ArrayList<>();
    }

    private Forge1192VanillaConstructionBridge() {}

    /** Preserves the surviving color semantics of Properties.of(Material.METAL). */
    public static BlockBehaviour.Properties metalBlockProperties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL);
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
