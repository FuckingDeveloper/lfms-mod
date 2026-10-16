package com.fuckingdeveloper.lms.profile.forge1192.runtime.item;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.registry.Forge1192RegistrationContext;

import com.mojang.datafixers.types.Type;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
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
import net.minecraft.world.level.block.state.properties.WoodType;
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
    private static final Map<Item.Properties, CreativeModeTab> LEGACY_TABS_BY_PROPERTIES =
            Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<net.minecraft.resources.Identifier, CreativeModeTab> LEGACY_TABS_BY_ITEM_ID =
            new ConcurrentHashMap<>();
    private static final Map<Item, Item.Properties> ITEM_PROPERTIES =
            Collections.synchronizedMap(new IdentityHashMap<>());
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
        AIR,
        AMETHYST,
        BAMBOO,
        BAMBOO_SAPLING,
        BARRIER,
        BUBBLE_COLUMN,
        BUILDABLE_GLASS,
        CACTUS,
        CAKE,
        CLAY,
        CLOTH_DECORATION,
        DECORATION,
        DIRT,
        EGG,
        EXPLOSIVE,
        FIRE,
        FROGLIGHT,
        FROGSPAWN,
        GLASS,
        GRASS,
        HEAVY_METAL,
        ICE,
        ICE_SOLID,
        LAVA,
        LEAVES,
        METAL,
        MOSS,
        NETHER_WOOD,
        PISTON,
        PLANT,
        PORTAL,
        POWDER_SNOW,
        REPLACEABLE_FIREPROOF_PLANT,
        REPLACEABLE_PLANT,
        REPLACEABLE_WATER_PLANT,
        SAND,
        SCULK,
        SHULKER_SHELL,
        SNOW,
        SPONGE,
        STONE,
        STRUCTURAL_AIR,
        TOP_SNOW,
        VEGETABLE,
        WATER,
        WATER_PLANT,
        WEB,
        WOOD,
        WOOL;

        private final MapColor color;
        private final boolean liquid;
        private final boolean solid;
        private final boolean blocksMotion;
        private final boolean flammable;
        private final boolean replaceable;
        private final boolean solidBlocking;
        private final PushReaction pushReaction;

        LegacyMaterial() {
            var source = com.fuckingdeveloper.lms.profile.forge1192.Forge1192MaterialSnapshot.material(name());
            this.color = MapColor.byId(source.colorId());
            this.liquid = source.liquid();
            this.solid = source.solid();
            this.blocksMotion = source.blocksMotion();
            this.flammable = source.flammable();
            this.replaceable = source.replaceable();
            this.solidBlocking = source.solidBlocking();
            this.pushReaction = switch (source.pushReaction()) {
                case "NORMAL" -> PushReaction.PUSH_PULL;
                case "DESTROY" -> PushReaction.POPPED;
                case "BLOCK" -> PushReaction.IMMOVEABLE;
                default -> throw new IllegalStateException("Unknown source push reaction: " + source.pushReaction());
            };
        }

        public boolean isLiquid() { return liquid; }
        public boolean isSolid() { return solid; }
        public boolean blocksMotion() { return blocksMotion; }
        public boolean isFlammable() { return flammable; }
        public boolean isReplaceable() { return replaceable; }
        public boolean isSolidBlocking() { return solidBlocking; }
        public PushReaction getPushReaction() { return pushReaction; }
        public MapColor getColor() { return color; }
    }

    /** Retain source identity, independently of target tags or tool behavior. */
    public static LegacyMaterial getMaterial(net.minecraft.world.level.block.state.BlockState state) {
        Block block = state.getBlock();
        LegacyMaterial constructed = LEGACY_MATERIALS_BY_PROPERTIES.get(block.properties());
        if (constructed != null) return constructed;
        var id = BuiltInRegistries.BLOCK.getKey(block);
        if (id == null) throw new IllegalStateException("Legacy material queried for an unregistered block");
        // Target DataFixers v3692 proves this exact native block rename.
        // Keep the immutable source snapshot indexed by source identities.
        String sourceId = id.toString().equals("minecraft:short_grass")
                ? "minecraft:grass" : id.toString();
        String source = com.fuckingdeveloper.lms.profile.forge1192.Forge1192MaterialSnapshot
                .materialForBlock(sourceId).orElseThrow(() -> new IllegalStateException(
                        "No Forge 1.19.2 material evidence for block " + id + " (" + block.getClass().getName() + ")"));
        return LegacyMaterial.valueOf(source);
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
        return com.fuckingdeveloper.lms.profile.forge1192.runtime.registry.LegacyDeferredRegister.blockProperties(properties);
    }

    /** Recover behavior that legacy door-like blocks derived from Material. */
    public static BlockSetType legacyBlockSetType(BlockBehaviour.Properties properties) {
        LegacyMaterial material = LEGACY_MATERIALS_BY_PROPERTIES.get(properties);
        if (material == LegacyMaterial.WOOD) return BlockSetType.OAK;
        if (material == LegacyMaterial.METAL) return BlockSetType.IRON;
        throw new IllegalStateException("Legacy door-like block has no supported Material semantics");
    }

    /** Preserve the 1.19.2 view-blocking callback when the target adds AABB context. */
    public static BlockBehaviour.Properties isViewBlocking(
            BlockBehaviour.Properties properties,
            BlockBehaviour.StatePredicate legacyPredicate) {
        return properties.isViewBlocking(
                (state, level, pos, bounds) -> legacyPredicate.test(state, level, pos));
    }

    /** Preserve source Material evidence across the old partial Properties.copy operation. */
    public static BlockBehaviour.Properties legacyBlockPropertiesCopy(BlockBehaviour source) {
        BlockBehaviour.Properties copied = BlockBehaviour.Properties.ofLegacyCopy(source);
        LegacyMaterial material = LEGACY_MATERIALS_BY_PROPERTIES.get(source.properties());
        if (material != null) LEGACY_MATERIALS_BY_PROPERTIES.put(copied, material);
        return com.fuckingdeveloper.lms.profile.forge1192.runtime.registry.LegacyDeferredRegister.blockProperties(copied);
    }

    /**
     * Rebuild the pressure-plate portion of BlockSetType from the two
     * independent 1.19.2 inputs: Material and Sensitivity.
     */
    public static BlockSetType legacyPressurePlateType(
            BlockBehaviour.Properties properties,
            BlockSetType.PressurePlateSensitivity sensitivity) {
        LegacyMaterial material = LEGACY_MATERIALS_BY_PROPERTIES.get(properties);
        if (material == null) {
            throw new IllegalStateException(
                    "Legacy pressure plate has no preserved Material semantics");
        }
        BlockSetType base = material == LegacyMaterial.WOOD ? BlockSetType.OAK : BlockSetType.STONE;
        if (base.pressurePlateSensitivity() == sensitivity) return base;
        return new BlockSetType(
                "lms_legacy_pressure_plate_" + base.name() + "_" + sensitivity.name().toLowerCase(java.util.Locale.ROOT),
                base.canOpenByHand(), base.canOpenByWindCharge(),
                base.canButtonBeActivatedByArrows(), sensitivity, base.soundType(),
                base.doorClose(), base.doorOpen(), base.trapdoorClose(), base.trapdoorOpen(),
                base.pressurePlateClickOff(), base.pressurePlateClickOn(),
                base.buttonClickOff(), base.buttonClickOn());
    }

    /**
     * Adapt the 1.19.2 name-only custom wood contract to the target's added
     * block-set contract using its explicit generic-wood defaults.
     */
    public static WoodType legacyWoodType(String name) {
        BlockSetType setType = BlockSetType.register(new BlockSetType(name));
        return new WoodType(name, setType);
    }

    /**
     * Preserve the Forge 1.19.2 client-setup operation after removal of
     * {@code Sheets}' WoodType-to-sign-material cache. Minecraft 26.3 sign
     * rendering consumes registered block/model resources and has no
     * equivalent mutable render cache to populate.
     */
    public static void legacyAddWoodType(WoodType type) {
        java.util.Objects.requireNonNull(type, "legacy wood type");
    }

    /** Attach the legacy block name before the modern Block constructor reads it. */
    public static BlockBehaviour.Properties withBlockId(
            BlockBehaviour.Properties properties, String legacyName) {
        return properties.setId(ResourceKey.create(
                Registries.BLOCK,
                Forge1192RegistrationContext.checkPrefix(legacyName, false)));
    }

    /** Preserve legacy construction-time membership for target tab population. */
    public static Item.Properties recordLegacyCreativeTab(
            Item.Properties properties, CreativeModeTab legacyTab) {
        LEGACY_TABS_BY_PROPERTIES.put(properties, legacyTab);
        return properties;
    }

    /** Use the legacy block's explicit registry name for its paired block item. */
    public static Item.Properties withBlockItemId(Block block, Item.Properties properties) {
        var nativeKey = BuiltInRegistries.BLOCK.getResourceKey(block);
        if (nativeKey.isPresent()) {
            var id = nativeKey.get().identifier();
            rememberCreativeTab(properties, id);
            return properties.setId(ResourceKey.create(Registries.ITEM,id));
        }
        try {
            Class<?> lookupType = block.getClass();
            while (lookupType != null && !java.lang.reflect.Modifier.isPublic(lookupType.getModifiers())) {
                lookupType = lookupType.getSuperclass();
            }
            if (lookupType == null) {
                throw new IllegalAccessException("No public block type exposes the legacy accessor");
            }
            Object value = java.lang.invoke.MethodHandles.publicLookup().findVirtual(
                    lookupType, "getRegistryName",
                    java.lang.invoke.MethodType.methodType(net.minecraft.resources.Identifier.class))
                    .invoke(block);
            if (!(value instanceof net.minecraft.resources.Identifier id)) {
                throw new IllegalStateException("Legacy block registry name is not an Identifier: " + value);
            }
            rememberCreativeTab(properties, id);
            return properties.setId(ResourceKey.create(Registries.ITEM, id));
        } catch (Throwable e) {
            if (e instanceof Error error) throw error;
            throw new IllegalStateException("Legacy block does not expose getRegistryName(): "
                    + block.getClass().getName(), e);
        }
    }

    /** Use a legacy constructor's bytecode-proven block registry lookup. */
    public static Item.Properties withBlockItemId(
            net.minecraft.resources.Identifier id, Item.Properties properties) {
        if (id == null) {
            throw new IllegalStateException("Legacy block-item registry lookup returned null");
        }
        rememberCreativeTab(properties, id);
        return properties.setId(ResourceKey.create(Registries.ITEM, id));
    }

    /** Source BlockItem names come from the block, not the item's registry key. */
    public static Item.Properties withBlockItemDescription(Item.Properties properties, Block block) {
        return properties.overrideDescription(block.getDescriptionId());
    }

    public static net.minecraft.world.item.BlockItem blockItemWithSourceName(Block block, Item.Properties properties) {
        return new net.minecraft.world.item.BlockItem(block, withBlockItemDescription(properties, block));
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

    /** Stable source order for items assigned to one legacy tab object. */
    public static List<net.minecraft.resources.Identifier> legacyCreativeItemIds(
            CreativeModeTab tab) {
        return LEGACY_TABS_BY_ITEM_ID.entrySet().stream()
                .filter(entry -> entry.getValue() == tab)
                .map(Map.Entry::getKey)
                .sorted(java.util.Comparator.comparing(net.minecraft.resources.Identifier::toString))
                .toList();
    }

    /**
     * Recover a legacy item's recorded tab solely to propagate its construction
     * intent to another item. This does not populate a modern creative tab.
     */
    public static CreativeModeTab legacyCreativeTab(Item item) {
        // Creative-tab population happens long after registration scopes close.
        // At that point the target registry key is the authoritative identity;
        // staged lookup is only a construction/registration-time fallback.
        net.minecraft.resources.Identifier id = BuiltInRegistries.ITEM.getResourceKey(item)
                .map(ResourceKey::identifier)
                .or(() -> Forge1192RegistrationContext.stagedIdentifierIfActive(item))
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
        CreativeModeTab tab = LEGACY_TABS_BY_PROPERTIES.get(properties);
        if (tab != null) LEGACY_TABS_BY_ITEM_ID.put(itemId, tab);
    }

    /** Capture source tab intent when the target consumes the actual item key. */
    public static void captureCreativeItem(Item.Properties properties) {
        if (!LEGACY_TABS_BY_PROPERTIES.containsKey(properties)) return;
        ResourceKey<Item> key = ((com.fuckingdeveloper.lms.mixin.Target263ItemPropertiesAccess)
                (Object) properties).lms$itemId();
        if (key == null) throw new IllegalStateException("Legacy creative item has no construction key");
        rememberCreativeTab(properties, key.identifier());
    }

    public static void associateItemProperties(Item item, Item.Properties properties) {
        Item.Properties previous = ITEM_PROPERTIES.putIfAbsent(item, properties);
        if (previous != null && previous != properties) {
            throw new IllegalStateException("Multiple Item.Properties objects for " + item);
        }
    }

    static Optional<Item.Properties> itemProperties(Item item) {
        return Optional.ofNullable(ITEM_PROPERTIES.get(item));
    }

    /** Resolve the target model identity without reading unbound holder components. */
    public static net.minecraft.resources.Identifier itemModelIdentity(Item item) {
        Item.Properties properties = ITEM_PROPERTIES.get(item);
        if (properties != null) return properties.effectiveModel();
        return BuiltInRegistries.ITEM.getResourceKey(item)
                .map(ResourceKey::identifier)
                .orElseThrow(() -> new IllegalStateException(
                        "Item model registration has neither preserved properties nor a target registry key for "
                                + item));
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
    public static net.minecraft.world.inventory.MenuType<?> menuType(net.minecraft.world.inventory.MenuType.MenuSupplier<?> factory) {
        return new net.minecraft.world.inventory.MenuType<>(factory,net.minecraft.world.flag.FeatureFlags.VANILLA_SET);
    }

}
