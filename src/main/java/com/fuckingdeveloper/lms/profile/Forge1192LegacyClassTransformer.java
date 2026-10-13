package com.fuckingdeveloper.lms.profile;

import com.fuckingdeveloper.lms.classloading.LegacyClassTransformer;
import com.fuckingdeveloper.lms.mapping.Forge1192MappingLayer;
import com.fuckingdeveloper.lms.mapping.Forge1192SrgRuntimeResolver;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.Handle;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.Type;

/**
 * Forge 1.19.2 legacy-owned class transformation pipeline.
 *
 * <p>The initial implementation is intentionally semantic-identity: it parses
 * every managed class through ASM and emits verified class bytes. Profile
 * rewrite passes are added here, before definition, rather than inside the
 * classloader. This establishes the mandatory transformation boundary without
 * claiming that removed Forge APIs have already been adapted.</p>
 */
public final class Forge1192LegacyClassTransformer implements LegacyClassTransformer {
    private static final Forge1192MappingLayer VANILLA_RELOCATIONS = new Forge1192MappingLayer();
    private static final java.util.Map<String, String> EXPLICIT_VANILLA_CLASS_RELOCATIONS = java.util.Map.ofEntries(
            java.util.Map.entry("net/minecraft/client/model/ArmorStandArmorModel", "net/minecraft/client/model/object/armorstand/ArmorStandArmorModel"),
            java.util.Map.entry("net/minecraft/client/model/BoatModel", "net/minecraft/client/model/object/boat/BoatModel"),
            java.util.Map.entry("net/minecraft/client/model/BookModel", "net/minecraft/client/model/object/book/BookModel"),
            java.util.Map.entry("net/minecraft/client/model/DrownedModel", "net/minecraft/client/model/monster/zombie/DrownedModel"),
            java.util.Map.entry("net/minecraft/client/model/GiantZombieModel", "net/minecraft/client/model/monster/zombie/GiantZombieModel"),
            java.util.Map.entry("net/minecraft/client/model/PlayerModel", "net/minecraft/client/model/player/PlayerModel"),
            java.util.Map.entry("net/minecraft/client/model/SkeletonModel", "net/minecraft/client/model/monster/skeleton/SkeletonModel"),
            java.util.Map.entry("net/minecraft/client/model/VillagerModel", "net/minecraft/client/model/npc/VillagerModel"),
            java.util.Map.entry("net/minecraft/client/model/ZombieModel", "net/minecraft/client/model/monster/zombie/ZombieModel"),
            java.util.Map.entry("net/minecraft/client/model/ZombieVillagerModel", "net/minecraft/client/model/monster/zombie/ZombieVillagerModel"),
            java.util.Map.entry("net/minecraft/client/renderer/blockentity/SignRenderer", "net/minecraft/client/renderer/blockentity/StandingSignRenderer"),
            java.util.Map.entry("net/minecraft/Util", "net/minecraft/util/Util"),
            java.util.Map.entry("com/mojang/blaze3d/vertex/BufferVertexConsumer", "com/fuckingdeveloper/lms/runtime/render/LegacyBufferVertexConsumer"),
            java.util.Map.entry("com/mojang/blaze3d/vertex/DefaultVertexFormat", "com/fuckingdeveloper/lms/runtime/render/LegacyDefaultVertexFormat"),
            java.util.Map.entry("com/mojang/blaze3d/vertex/VertexFormat", "com/fuckingdeveloper/lms/runtime/render/LegacyVertexFormat"),
            java.util.Map.entry("com/mojang/blaze3d/vertex/VertexFormat$Mode", "com/fuckingdeveloper/lms/runtime/render/LegacyVertexFormatMode"),
            java.util.Map.entry("com/mojang/blaze3d/vertex/VertexFormatElement", "com/fuckingdeveloper/lms/runtime/render/LegacyVertexFormatElement"),
            java.util.Map.entry("com/mojang/blaze3d/vertex/VertexFormatElement$Type", "com/fuckingdeveloper/lms/runtime/render/LegacyVertexFormatElement$Type"),
            java.util.Map.entry("com/mojang/blaze3d/vertex/VertexFormatElement$Usage", "com/fuckingdeveloper/lms/runtime/render/LegacyVertexFormatElement$Usage"),
            java.util.Map.entry("net/minecraft/client/resources/model/Material", "com/fuckingdeveloper/lms/runtime/render/LegacyMaterial"),
            java.util.Map.entry("net/minecraft/client/resources/model/ModelState", "net/minecraft/client/renderer/block/dispatch/ModelState"),
            java.util.Map.entry("net/minecraft/client/resources/model/BlockModelRotation", "com/fuckingdeveloper/lms/runtime/render/LegacyBlockModelRotation"),
            java.util.Map.entry("net/minecraft/client/resources/model/ModelResourceLocation", "com/fuckingdeveloper/lms/runtime/render/LegacyModelResourceLocation"),
            java.util.Map.entry("net/minecraft/client/color/block/BlockColor", "com/fuckingdeveloper/lms/runtime/render/LegacyBlockColor"),
            java.util.Map.entry("net/minecraft/client/color/item/ItemColor", "com/fuckingdeveloper/lms/runtime/render/LegacyItemColor"),
            java.util.Map.entry("net/minecraft/client/renderer/block/model/ItemTransform", "net/minecraft/client/resources/model/cuboid/ItemTransform"),
            java.util.Map.entry("net/minecraft/client/renderer/block/model/ItemTransforms", "net/minecraft/client/resources/model/cuboid/ItemTransforms"),
            java.util.Map.entry("net/minecraft/client/renderer/block/model/ItemTransforms$TransformType", "net/minecraft/world/item/ItemDisplayContext"),
            java.util.Map.entry("net/minecraft/client/renderer/block/model/BakedQuad", "net/minecraft/client/resources/model/geometry/BakedQuad"),
            java.util.Map.entry("net/minecraft/client/renderer/block/model/BlockElement", "com/fuckingdeveloper/lms/runtime/render/LegacyBlockElement"),
            java.util.Map.entry("net/minecraft/client/renderer/block/model/BlockFaceUV", "com/fuckingdeveloper/lms/runtime/render/LegacyBlockFaceUV"),
            java.util.Map.entry("net/minecraft/client/renderer/block/model/BlockElementFace", "com/fuckingdeveloper/lms/runtime/render/LegacyBlockElementFace"),
            java.util.Map.entry("net/minecraft/client/renderer/block/model/BlockElementRotation", "com/fuckingdeveloper/lms/runtime/render/LegacyBlockElementRotation"),
            java.util.Map.entry("net/minecraft/client/renderer/block/model/FaceBakery", "com/fuckingdeveloper/lms/runtime/render/LegacyFaceBakery"),
            java.util.Map.entry("net/minecraft/client/renderer/block/model/ItemModelGenerator", "com/fuckingdeveloper/lms/runtime/render/LegacyItemModelGenerator"),
            java.util.Map.entry("net/minecraft/client/renderer/block/model/ItemOverrides", "com/fuckingdeveloper/lms/runtime/render/LegacyItemOverrides"),
            java.util.Map.entry("net/minecraft/client/gui/screens/SoundOptionsScreen", "net/minecraft/client/gui/screens/options/SoundOptionsScreen"),
            java.util.Map.entry("net/minecraft/client/renderer/RenderType", "com/fuckingdeveloper/lms/runtime/render/LegacyRenderType"),
            java.util.Map.entry("net/minecraft/client/renderer/RenderType$CompositeRenderType", "com/fuckingdeveloper/lms/runtime/render/LegacyCompositeRenderType"),
            java.util.Map.entry("net/minecraft/client/renderer/RenderType$CompositeState", "com/fuckingdeveloper/lms/runtime/render/LegacyCompositeState"),
            java.util.Map.entry("net/minecraft/client/renderer/RenderType$CompositeState$CompositeStateBuilder", "com/fuckingdeveloper/lms/runtime/render/LegacyCompositeState$Builder"),
            java.util.Map.entry("net/minecraft/client/renderer/block/BlockRenderDispatcher", "com/fuckingdeveloper/lms/runtime/render/LegacyBlockRenderDispatcher"),
            java.util.Map.entry("net/minecraft/client/renderer/block/ModelBlockRenderer", "com/fuckingdeveloper/lms/runtime/render/LegacyModelBlockRenderer"),
            java.util.Map.entry("net/minecraft/client/renderer/RenderStateShard", "com/fuckingdeveloper/lms/runtime/render/LegacyRenderStateShard"),
            java.util.Map.entry("net/minecraft/client/renderer/RenderStateShard$CullStateShard", "com/fuckingdeveloper/lms/runtime/render/LegacyRenderStateShard$CullStateShard"),
            java.util.Map.entry("net/minecraft/client/renderer/RenderStateShard$DepthTestStateShard", "com/fuckingdeveloper/lms/runtime/render/LegacyRenderStateShard$DepthTestStateShard"),
            java.util.Map.entry("net/minecraft/client/renderer/RenderStateShard$EmptyTextureStateShard", "com/fuckingdeveloper/lms/runtime/render/LegacyRenderStateShard$EmptyTextureStateShard"),
            java.util.Map.entry("net/minecraft/client/renderer/RenderStateShard$LayeringStateShard", "com/fuckingdeveloper/lms/runtime/render/LegacyRenderStateShard$LayeringStateShard"),
            java.util.Map.entry("net/minecraft/client/renderer/RenderStateShard$LightmapStateShard", "com/fuckingdeveloper/lms/runtime/render/LegacyRenderStateShard$LightmapStateShard"),
            java.util.Map.entry("net/minecraft/client/renderer/RenderStateShard$LineStateShard", "com/fuckingdeveloper/lms/runtime/render/LegacyRenderStateShard$LineStateShard"),
            java.util.Map.entry("net/minecraft/client/renderer/RenderStateShard$OutputStateShard", "com/fuckingdeveloper/lms/runtime/render/LegacyRenderStateShard$OutputStateShard"),
            java.util.Map.entry("net/minecraft/client/renderer/RenderStateShard$OverlayStateShard", "com/fuckingdeveloper/lms/runtime/render/LegacyRenderStateShard$OverlayStateShard"),
            java.util.Map.entry("net/minecraft/client/renderer/RenderStateShard$ShaderStateShard", "com/fuckingdeveloper/lms/runtime/render/LegacyRenderStateShard$ShaderStateShard"),
            java.util.Map.entry("net/minecraft/client/renderer/RenderStateShard$TextureStateShard", "com/fuckingdeveloper/lms/runtime/render/LegacyRenderStateShard$TextureStateShard"),
            java.util.Map.entry("net/minecraft/client/renderer/RenderStateShard$TexturingStateShard", "com/fuckingdeveloper/lms/runtime/render/LegacyRenderStateShard$TexturingStateShard"),
            java.util.Map.entry("net/minecraft/client/renderer/RenderStateShard$TransparencyStateShard", "com/fuckingdeveloper/lms/runtime/render/LegacyRenderStateShard$TransparencyStateShard"),
            java.util.Map.entry("net/minecraft/client/renderer/RenderStateShard$WriteMaskStateShard", "com/fuckingdeveloper/lms/runtime/render/LegacyRenderStateShard$WriteMaskStateShard"),
            java.util.Map.entry("com/mojang/math/Vector3f", "org/joml/Vector3f"),
            java.util.Map.entry("com/mojang/math/Quaternion", "org/joml/Quaternionf"),
            java.util.Map.entry("net/minecraft/world/level/BlockAndTintGetter", "net/minecraft/client/renderer/block/BlockAndTintGetter"),
            java.util.Map.entry("net/minecraft/core/BlockSource", "net/minecraft/core/dispenser/BlockSource"),
            java.util.Map.entry("net/minecraft/world/entity/animal/Sheep", "net/minecraft/world/entity/animal/sheep/Sheep"),
            java.util.Map.entry("net/minecraft/world/entity/boss/EnderDragonPart", "net/minecraft/world/entity/boss/enderdragon/EnderDragonPart"),
            java.util.Map.entry("net/minecraft/world/entity/decoration/PaintingVariant", "net/minecraft/world/entity/decoration/painting/PaintingVariant"),
            java.util.Map.entry("net/minecraft/world/entity/npc/Villager", "net/minecraft/world/entity/npc/villager/Villager"),
            java.util.Map.entry("net/minecraft/world/entity/npc/VillagerData", "net/minecraft/world/entity/npc/villager/VillagerData"),
            java.util.Map.entry("net/minecraft/world/entity/npc/VillagerProfession", "net/minecraft/world/entity/npc/villager/VillagerProfession"),
            java.util.Map.entry("net/minecraft/world/entity/npc/VillagerTrades$ItemListing", "com/fuckingdeveloper/lms/runtime/villager/LegacyVillagerItemListing"),
            java.util.Map.entry("net/minecraft/world/entity/npc/VillagerType", "net/minecraft/world/entity/npc/villager/VillagerType"),
            java.util.Map.entry("net/minecraft/world/entity/vehicle/AbstractMinecart", "net/minecraft/world/entity/vehicle/minecart/AbstractMinecart"),
            java.util.Map.entry("net/minecraft/world/entity/vehicle/Boat", "net/minecraft/world/entity/vehicle/boat/Boat"),
            java.util.Map.entry("net/minecraft/world/entity/vehicle/Minecart", "net/minecraft/world/entity/vehicle/minecart/Minecart"),
            java.util.Map.entry("net/minecraft/world/item/Tiers", "com/fuckingdeveloper/lms/runtime/item/LegacyTiers"),
            java.util.Map.entry("net/minecraft/ResourceLocationException", "net/minecraft/IdentifierException"),
            java.util.Map.entry("net/minecraft/network/chat/HoverEvent$ItemStackInfo", "com/fuckingdeveloper/lms/runtime/chat/LegacyHoverItemStackInfo"),
            java.util.Map.entry("net/minecraftforge/client/event/TextureStitchEvent", "com/fuckingdeveloper/lms/runtime/event/LegacyTextureStitchEvent"),
            java.util.Map.entry("net/minecraftforge/client/event/TextureStitchEvent$Pre", "com/fuckingdeveloper/lms/runtime/event/LegacyTextureStitchEvent$Pre"),
            java.util.Map.entry("net/minecraftforge/client/event/TextureStitchEvent$Post", "com/fuckingdeveloper/lms/runtime/event/LegacyTextureStitchEvent$Post"),
            java.util.Map.entry("net/minecraftforge/client/event/RegisterColorHandlersEvent", "com/fuckingdeveloper/lms/runtime/event/LegacyRegisterColorHandlersEvent"),
            java.util.Map.entry("net/minecraftforge/client/event/RegisterColorHandlersEvent$Block", "com/fuckingdeveloper/lms/runtime/event/LegacyRegisterColorHandlersEvent$Block"),
            java.util.Map.entry("net/minecraftforge/client/event/RegisterColorHandlersEvent$Item", "com/fuckingdeveloper/lms/runtime/event/LegacyRegisterColorHandlersEvent$Item"),
            java.util.Map.entry("net/minecraftforge/client/event/RegisterGuiOverlaysEvent", "com/fuckingdeveloper/lms/runtime/event/LegacyRegisterGuiOverlaysEvent"),
            java.util.Map.entry("net/minecraftforge/client/event/ModelEvent$BakingCompleted", "com/fuckingdeveloper/lms/runtime/event/LegacyModelBakingCompletedEvent"),
            java.util.Map.entry("net/minecraftforge/client/gui/overlay/IGuiOverlay", "com/fuckingdeveloper/lms/runtime/render/LegacyGuiOverlay"),
            java.util.Map.entry("net/minecraftforge/client/gui/overlay/ForgeGui", "com/fuckingdeveloper/lms/runtime/render/LegacyForgeGui"),
            java.util.Map.entry("net/minecraftforge/client/model/SimpleModelState", "com/fuckingdeveloper/lms/runtime/render/LegacySimpleModelState"),
            java.util.Map.entry("net/minecraftforge/client/ChunkRenderTypeSet", "com/fuckingdeveloper/lms/runtime/render/LegacyChunkRenderTypeSet"),
            java.util.Map.entry("net/minecraftforge/client/event/RenderHighlightEvent$Block", "com/fuckingdeveloper/lms/runtime/event/LegacyRenderHighlightBlockEvent"),
            java.util.Map.entry("net/minecraftforge/event/AddReloadListenerEvent", "net/neoforged/neoforge/event/AddServerReloadListenersEvent"),
            java.util.Map.entry("net/minecraftforge/event/village/VillagerTradesEvent", "com/fuckingdeveloper/lms/runtime/event/LegacyVillagerTradesEvent"),
            java.util.Map.entry("net/minecraftforge/event/village/WandererTradesEvent", "com/fuckingdeveloper/lms/runtime/event/LegacyWandererTradesEvent"),
            java.util.Map.entry("net/minecraftforge/common/crafting/IShapedRecipe", "com/fuckingdeveloper/lms/runtime/recipe/LegacyIShapedRecipe"),
            java.util.Map.entry("net/minecraft/client/renderer/MultiBufferSource", "com/fuckingdeveloper/lms/runtime/render/LegacyMultiBufferSource"),
            java.util.Map.entry("net/minecraft/client/renderer/MultiBufferSource$BufferSource", "com/fuckingdeveloper/lms/runtime/render/LegacyBufferSource"),
            java.util.Map.entry("net/minecraft/util/LazyLoadedValue", "com/fuckingdeveloper/lms/runtime/LegacyLazyLoadedValue"),
            java.util.Map.entry("net/minecraft/util/Tuple", "com/fuckingdeveloper/lms/runtime/Forge1192Tuple"),
            java.util.Map.entry("net/minecraft/world/item/crafting/RecipeSerializer", "com/fuckingdeveloper/lms/runtime/recipe/LegacyRecipeSerializer"),
            java.util.Map.entry("net/minecraft/world/item/ArmorMaterial", "com/fuckingdeveloper/lms/runtime/item/LegacyArmorMaterial"),
            java.util.Map.entry("net/minecraft/world/item/ArmorMaterials", "com/fuckingdeveloper/lms/runtime/item/LegacyArmorMaterials"),
            java.util.Map.entry("net/minecraft/advancements/CriteriaTriggers", "net/minecraft/advancements/triggers/CriteriaTriggers"),
            java.util.Map.entry("net/minecraft/advancements/FrameType", "net/minecraft/advancements/AdvancementType"),
            java.util.Map.entry("net/minecraft/advancements/critereon/InventoryChangeTrigger", "net/minecraft/advancements/triggers/InventoryChangeTrigger"),
            java.util.Map.entry("net/minecraft/advancements/critereon/InventoryChangeTrigger$TriggerInstance", "net/minecraft/advancements/triggers/InventoryChangeTrigger$TriggerInstance"),
            java.util.Map.entry("net/minecraft/advancements/CriterionTrigger", "com/fuckingdeveloper/lms/runtime/advancement/Forge1192LegacyCriterionTrigger"),
            java.util.Map.entry("net/minecraft/advancements/CriterionTrigger$Listener", "com/fuckingdeveloper/lms/runtime/advancement/Forge1192LegacyCriterionListener"),
            java.util.Map.entry("net/minecraft/advancements/critereon/AbstractCriterionTriggerInstance", "com/fuckingdeveloper/lms/runtime/advancement/Forge1192LegacyCriterionInstance"),
            java.util.Map.entry("net/minecraft/advancements/critereon/EntityPredicate$Composite", "com/fuckingdeveloper/lms/runtime/advancement/Forge1192LegacyEntityPredicateComposite"),
            java.util.Map.entry("net/minecraft/advancements/critereon/DeserializationContext", "com/fuckingdeveloper/lms/runtime/advancement/Forge1192LegacyDeserializationContext"),
            java.util.Map.entry("net/minecraft/advancements/critereon/SerializationContext", "com/fuckingdeveloper/lms/runtime/advancement/Forge1192LegacySerializationContext"),
            java.util.Map.entry("net/minecraft/world/level/GameRules", "net/minecraft/world/level/gamerules/GameRules"),
            java.util.Map.entry("net/minecraft/world/level/chunk/ChunkStatus", "net/minecraft/world/level/chunk/status/ChunkStatus"),
            java.util.Map.entry("net/minecraft/world/level/storage/loot/parameters/LootContextParam", "net/minecraft/util/context/ContextKey"),
            java.util.Map.entry("net/minecraft/world/level/material/MaterialColor", "net/minecraft/world/level/material/MapColor"),
            java.util.Map.entry("net/minecraft/world/level/material/MaterialColor$Brightness", "net/minecraft/world/level/material/MapColor$Brightness"));
    private static final java.util.Optional<Forge1192SrgRuntimeResolver> SRG_RUNTIME_RESOLVER =
            Forge1192SrgRuntimeResolver.load(java.nio.file.Path.of(System.getProperty("user.dir")));
    private int transformedClasses;
    private int totalRewrites;
    private volatile LegacyClassTransformer.ClassBytesLookup managedClassBytes;
    private final java.util.Map<String, Boolean> configuredFeatureInitializers =
            new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.Map<String, Boolean> itemComponentInitializers =
            new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.Map<String, Boolean> legacyUnitMapCodecFields =
            new java.util.concurrent.ConcurrentHashMap<>();

    @Override
    public void bindManagedClassBytes(LegacyClassTransformer.ClassBytesLookup lookup) {
        this.managedClassBytes = java.util.Objects.requireNonNull(lookup, "lookup");
    }

    public int transformedClasses() { return transformedClasses; }
    public int totalRewrites() { return totalRewrites; }

    @Override
    public Result transform(String binaryName, byte[] original) {
        ClassReader reader = new ClassReader(original);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        String expected = binaryName.replace('.', '/');
        if (!expected.equals(node.name)) {
            throw new IllegalArgumentException(
                    "Class identity mismatch: requested=" + expected + " bytecode=" + node.name);
        }

        // Removed Forge APIs must be adapted while their original identity
        // is still available. Namespace projection is intentionally second:
        // otherwise a heuristic relocation can destroy the semantic evidence
        // required by a removed-API adapter.
        int rewrites = rewriteRemovedOptionalClientScreenConstructors(node);
        rewrites += rewriteLegacyEnvironmentFieldAccess(node);
        rewrites += rewriteLegacyLevelClientSideFieldAccess(node);
        rewrites += rewriteLegacyEntityLevelFieldAccess(node);
        rewrites += rewriteLegacyBlockPosConstruction(node);
        rewrites += rewriteLegacyChatEventFamily(node);
        rewrites += rewriteLegacyNetworkFacade(node);
        // Preserve the authoritative legacy identity while the direct source
        // superclass constructor is still visible. Later construction-shape
        // adapters may replace that owner and descriptor.
        rewrites += rewriteLegacyNamedBlockConstruction(node);
        rewrites += rewriteLegacySignItemConstruction(node);
        rewrites += rewriteLegacySignBlockConstruction(node);
        rewrites += rewriteLegacyWoodButtonConstruction(node);
        rewrites += rewriteLegacyVillagerProfessionConstruction(node);
        rewrites += rewriteLegacyVillagerProfessionConstants(node);
        rewrites += rewriteLegacySavedDataFamily(node);
        rewrites += rewriteLegacyLootContextFamily(node);
        rewrites += rewriteLegacyDamageSourceSingletonComparisons(node);
        rewrites += rewriteLegacyRecipeViewFamily(node);
        rewrites += rewriteLegacyDirectionPropertyFactories(node);
        rewrites += rewriteLegacyMaterialBlockProperties(node);
        rewrites += rewriteLegacyMaterialColorProperties(node);
        rewrites += rewriteLegacyBlockPropertiesCopy(node);
        rewrites += rewriteLegacyViewBlockingPredicate(node);
        rewrites += rewriteLegacyBlockSetTypeConstruction(node);
        rewrites += rewriteLegacyFarmlandFields(node);
        rewrites += rewriteLegacyKeyMappingConstruction(node);
        rewrites += rewriteLegacyConfiguredFeatureInitialization(node);
        rewrites += rewriteLegacyComposterRegistrations(node);
        rewrites += rewriteLegacyMixinPropertyAccessors(node);
        rewrites += rewriteLegacyFixedBufferRegistration(node);
        rewrites += rewriteLegacyRecipesUpdatedHandlers(node);
        rewrites += rewriteLegacyEarlyClientInitialization(node);
        rewrites += rewriteLegacyEarlyClientPlayerReads(node);
        rewrites += rewriteLegacyCachedMinecraftReads(node);
        rewrites += rewriteLegacyCableStateDefinitionOverride(node);
        rewrites += rewriteLegacyStringRepresentableOverride(node);
        rewrites += rewriteVerifiedVanillaOverrides(node);
        rewrites += rewriteLegacyCreativeTabAssignment(node);
        rewrites += rewriteLegacyCreativeTabLookup(node);
        rewrites += rewriteLegacyCreativeTabEnumeration(node);
        rewrites += rewriteLegacyBlockItemConstruction(node);
        rewrites += rewriteLegacyNamedItemConstruction(node);
        rewrites += rewriteLegacyNamedSimpleToolConstruction(node);
        rewrites += rewriteEarlyItemStackConsumers(node);
        rewrites += rewriteLegacyItemStackConstruction(node);
        rewrites += rewriteLegacyItemStackComparison(node);
        rewrites += rewriteLegacyDiggerItemChecks(node);
        rewrites += rewriteLegacyAttributeModifiers(node);
        rewrites += rewriteLegacyBlockHighlightEvent(node);
        rewrites += rewriteLegacyRenderLivingEventAccessors(node);
        rewrites += rewriteLegacyEntityRenderLayerOverride(node);
        rewrites += rewriteLegacyModelSubclass(node);
        rewrites += rewriteLegacyRenderFrustumConstruction(node);
        rewrites += rewriteLegacyBufferedVertexCacheLifecycle(node);
        rewrites += rewriteLegacyMultiBufferSource(node);
        rewrites += rewriteLegacyBlockRenderLayerRegistration(node);
        rewrites += rewriteLegacyDirectClientReloadRegistration(node);
        rewrites += rewriteLegacyReloadListenerRegistration(node);
        rewrites += rewriteLegacyResourceQueries(node);
        rewrites += rewriteLegacyBlockModelParsing(node);
        rewrites += rewriteLegacyItemTransformFamily(node);
        rewrites += rewriteLegacyModelBakingPipeline(node);
        rewrites += rewriteLegacyBlockAtlasIdentity(node);
        rewrites += rewriteLegacyVector3fFamily(node);
        rewrites += rewriteLegacyQuaternionFamily(node);
        rewrites += rewriteLegacyBakedQuadFamily(node);
        rewrites += rewriteLegacyPackRepositoryFamily(node);
        rewrites += rewriteLegacyPackMetadataConstruction(node);
        rewrites += rewriteLegacySimpleJsonReloadListener(node);
        rewrites += rewriteLegacyIngredientFamily(node);
        rewrites += rewriteLegacyDyeItemLookup(node);
        rewrites += rewriteLegacyUnitMapCodecFamily(node);
        rewrites += rewriteLegacyCriterionTriggerFamily(node);
        rewrites += rewriteLegacyAdvancementDisplayFamily(node);
        rewrites += rewriteLegacyEntityRendererAddLayers(node);
        rewrites += rewriteLegacyInputConstantsFamily(node);
        rewrites += rewriteAllNamespaceReferences(node);
        rewrites += rewriteLegacyTextureAtlasSpriteIdentity(node);
        rewrites += rewriteLegacyBoatRendererSubclass(node);
        rewrites += rewriteLegacyDyeColorValues(node);
        rewrites += rewriteLegacyEnchantmentValues(node);
        rewrites += rewriteLegacyBlockRenderDispatcherFamily(node);
        rewrites += rewriteLegacyPlayerInteractionDecisions(node);
        rewrites += rewriteLegacyLevelSoundPlayback(node);
        rewrites += rewriteLegacyInteractionResultConstants(node);
        rewrites += rewriteLegacyPlacedBlockCriterion(node);
        rewrites += rewriteLegacyBlockStateCloneItem(node);
        rewrites += rewriteLegacyNeighborUpdateFamily(node);
        rewrites += rewriteLegacyVertexConsumerFamily(node);
        rewrites += rewriteTargetInterfaceInvocationShapes(node);
        rewrites += rewriteClassStructureNamespaceMigrations(node);
        rewrites += rewriteLegacyStateOnlyEmissivePredicates(node);
        rewrites += rewriteVerifiedSrgMethodCalls(node);
        rewrites += rewriteVerifiedSrgFieldAccesses(node);
        rewrites += rewriteLegacyMobEffectInstanceConstruction(node);
        rewrites += rewriteLegacyVanillaRegistryFields(node);
        rewrites += rewriteLegacyForgeBuiltinFields(node);
        rewrites += rewriteLegacyForgeRegistries(node);
        rewrites += rewriteLegacyForgeRegistryKeys(node);
        rewrites += rewriteLegacyTickEventFields(node);
        rewrites += rewriteLegacySpawnCheckSemantics(node);
        rewrites += rewriteLegacyIdentifierConstruction(node);
        rewrites += rewriteLegacyBlockEntityTypeConstruction(node);
        rewrites += rewriteLegacyCraftingContainerConstruction(node);
        rewrites += rewriteSemanticAdapters(node);
        verifyNoRemovedForgeClientTypes(node);
        verifyUnresolvedForgeModAccesses(node);
        verifyNoEscapingLegacyRegistryFacade(node);
        verifyNoUnadaptedLifecycleCalls(node);
        verifyNoEscapingLegacyNetworkFacade(node);
        if (rewrites > 0) {
            transformedClasses++;
            totalRewrites += rewrites;
        }

        // Semantic adapters may change descriptors and local/stack types.
        // Frames from the legacy class are therefore no longer authoritative.
        // Recompute both frames and maxs before definition.
        ClassWriter writer = new SafeFrameClassWriter(
                ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS, managedClassBytes);
        node.accept(writer);
        byte[] verified = writer.toByteArray();
        return new Result(verified, rewrites > 0,
                "forge-1.19.2 exact namespace migrations=" + rewrites);
    }

    /** Preserve the exact 1.19.2 Model constructor and abstract render ABI for
     * direct legacy subclasses; target-owned model classes are never widened. */
    private static int rewriteLegacyModelSubclass(ClassNode node) {
        if (!node.superName.equals("net/minecraft/client/model/Model")) return 0;
        boolean sourceConstructor = node.methods.stream()
                .filter(method -> method.name.equals("<init>"))
                .flatMap(method -> java.util.stream.StreamSupport.stream(
                        method.instructions.spliterator(), false))
                .filter(MethodInsnNode.class::isInstance)
                .map(MethodInsnNode.class::cast)
                .anyMatch(call -> call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESPECIAL
                        && call.owner.equals("net/minecraft/client/model/Model")
                        && call.name.equals("<init>")
                        && call.desc.equals("(Ljava/util/function/Function;)V"));
        if (!sourceConstructor) return 0;

        node.superName = "com/fuckingdeveloper/lms/runtime/render/LegacyModel";
        int rewrites = 1;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof MethodInsnNode call
                        && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESPECIAL
                        && call.owner.equals("net/minecraft/client/model/Model")
                        && call.name.equals("<init>")
                        && call.desc.equals("(Ljava/util/function/Function;)V")) {
                    call.owner = "com/fuckingdeveloper/lms/runtime/render/LegacyModel";
                    rewrites++;
                }
            }
        }
        return rewrites;
    }
    /**
     * ASM's ClassRemapper covers the complete class-file surface: descriptors,
     * signatures, annotations/type annotations, frames, handles, indy/condy,
     * record components, nest/inner metadata and instruction operands.
     * Semantic adapters still run afterwards on the migrated tree.
     */
    private static int rewriteAllNamespaceReferences(ClassNode node) {
        ClassNode migrated = new ClassNode();
        Remapper remapper = new Remapper() {
            @Override public String map(String internalName) {
                return migrateInternalName(internalName);
            }
        };
        node.accept(new ClassRemapper(migrated, remapper));
        // Keep a simple deterministic signal for diagnostics. Detailed rewrite
        // counts are not semantic evidence; transformation coverage is.
        int changed = java.util.Arrays.equals(writeNode(node), writeNode(migrated)) ? 0 : 1;
        node.version = migrated.version;
        node.access = migrated.access;
        node.name = migrated.name;
        node.signature = migrated.signature;
        node.superName = migrated.superName;
        node.interfaces = migrated.interfaces;
        node.sourceFile = migrated.sourceFile;
        node.sourceDebug = migrated.sourceDebug;
        node.module = migrated.module;
        node.outerClass = migrated.outerClass;
        node.outerMethod = migrated.outerMethod;
        node.outerMethodDesc = migrated.outerMethodDesc;
        node.visibleAnnotations = migrated.visibleAnnotations;
        node.invisibleAnnotations = migrated.invisibleAnnotations;
        node.visibleTypeAnnotations = migrated.visibleTypeAnnotations;
        node.invisibleTypeAnnotations = migrated.invisibleTypeAnnotations;
        node.attrs = migrated.attrs;
        node.innerClasses = migrated.innerClasses;
        node.nestHostClass = migrated.nestHostClass;
        node.nestMembers = migrated.nestMembers;
        node.permittedSubclasses = migrated.permittedSubclasses;
        node.recordComponents = migrated.recordComponents;
        node.fields = migrated.fields;
        node.methods = migrated.methods;
        return changed;
    }

    /**
     * Minecraft 1.19.2 exposed a sprite's own resource identity directly on
     * TextureAtlasSprite. Target 26.3 keeps that identity on SpriteContents;
     * TextureAtlasSprite#atlasLocation names the containing atlas and is not
     * source-equivalent.
     */
    private static int rewriteLegacyTextureAtlasSpriteIdentity(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        || !call.owner.equals(
                                "net/minecraft/client/renderer/texture/TextureAtlasSprite")
                        || !call.name.equals("m_118413_")
                        || !call.desc.equals("()Lnet/minecraft/resources/Identifier;")) continue;
                call.name = "contents";
                call.desc = "()Lnet/minecraft/client/renderer/texture/SpriteContents;";
                method.instructions.insert(call, new MethodInsnNode(
                        org.objectweb.asm.Opcodes.INVOKEVIRTUAL,
                        "net/minecraft/client/renderer/texture/SpriteContents",
                        "name",
                        "()Lnet/minecraft/resources/Identifier;",
                        false));
                rewrites++;
            }
        }
        return rewrites;
    }

    /**
     * Adapt the exact Forge 1.19.2 BoatRenderer subclass contract only when
     * the legacy class overrides the live-entity texture decision. Target
     * 26.3 moved that decision into extracted render state.
     */
    private static int rewriteLegacyBoatRendererSubclass(ClassNode node) {
        final String sourceSuper = "net/minecraft/client/renderer/entity/BoatRenderer";
        final String targetSuper =
                "com/fuckingdeveloper/lms/runtime/render/Forge1192BoatRendererBridge";
        if (!node.superName.equals(sourceSuper)) return 0;
        boolean textureOverride = node.methods.stream().anyMatch(method ->
                method.name.equals("m_5478_")
                        && method.desc.equals("(Lnet/minecraft/world/entity/vehicle/boat/Boat;)"
                                + "Lnet/minecraft/resources/Identifier;"));
        if (!textureOverride) return 0;
        boolean sourceConstructor = node.methods.stream().anyMatch(method -> {
            if (!method.name.equals("<init>")) return false;
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof MethodInsnNode call
                        && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESPECIAL
                        && call.owner.equals(sourceSuper)
                        && call.name.equals("<init>")
                        && call.desc.equals("(Lnet/minecraft/client/renderer/entity/"
                                + "EntityRendererProvider$Context;Z)V")) return true;
            }
            return false;
        });
        if (!sourceConstructor) return 0;

        int rewrites = 1;
        node.superName = targetSuper;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof MethodInsnNode call
                        && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESPECIAL
                        && call.owner.equals(sourceSuper)) {
                    call.owner = targetSuper;
                    rewrites++;
                }
            }
        }
        return rewrites;
    }

    /**
     * Target 26.3 packs the same dye texture color into ARGB instead of the
     * source generation's normalized RGB float array.
     */
    private static int rewriteLegacyDyeColorValues(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        || !call.owner.equals("net/minecraft/world/item/DyeColor")
                        || !call.name.equals("m_41068_")
                        || !call.desc.equals("()[F")) continue;
                call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                call.owner = "com/fuckingdeveloper/lms/runtime/render/Forge1192DyeColorBridge";
                call.name = "textureDiffuseColors";
                call.desc = "(Lnet/minecraft/world/item/DyeColor;)[F";
                call.itf = false;
                rewrites++;
            }
        }
        return rewrites;
    }

    /** Resolve source process-global enchantment values through the target's
     * world-scoped data-driven registry at the exact consuming operation. */
    private int rewriteLegacyEnchantmentValues(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof FieldInsnNode field
                        && field.getOpcode() == org.objectweb.asm.Opcodes.GETSTATIC
                        && field.owner.equals("net/minecraft/world/item/enchantment/Enchantments")
                        && field.desc.equals(
                                "Lnet/minecraft/world/item/enchantment/Enchantment;")) {
                    String canonical = SRG_RUNTIME_RESOLVER.get()
                            .canonicalFieldName(field.owner, field.name, field.desc)
                            .orElse(null);
                    // Mojang renamed several Java constants while retaining
                    // their exact minecraft registry identities. This table
                    // covers the complete 1.19.2 constant family rather than
                    // only the members reached by the current fixture.
                    String targetName = switch (canonical == null ? "" : canonical) {
                        case "ALL_DAMAGE_PROTECTION" -> "PROTECTION";
                        case "FALL_PROTECTION" -> "FEATHER_FALLING";
                        case "MOB_LOOTING" -> "LOOTING";
                        case "BLOCK_EFFICIENCY" -> "EFFICIENCY";
                        case "BLOCK_FORTUNE" -> "FORTUNE";
                        case "POWER_ARROWS" -> "POWER";
                        case "PUNCH_ARROWS" -> "PUNCH";
                        case "FLAMING_ARROWS" -> "FLAME";
                        case "INFINITY_ARROWS" -> "INFINITY";
                        case "FISHING_LUCK" -> "LUCK_OF_THE_SEA";
                        case "FISHING_SPEED" -> "LURE";
                        default -> canonical;
                    };
                    if (targetName != null && !VANILLA_RELOCATIONS.searchExactField(
                            "net.minecraft.world.item.enchantment.Enchantments",
                            targetName, "Lnet/minecraft/resources/ResourceKey;").isEmpty()) {
                        field.name = targetName;
                        field.desc = "Lnet/minecraft/resources/ResourceKey;";
                        rewrites++;
                    }
                    continue;
                }
                if (insn instanceof MethodInsnNode call
                        && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("net/minecraft/world/item/enchantment/Enchantment")
                        && (call.name.equals("m_6586_") || call.name.equals("getMaxLevel"))
                        && call.desc.equals("()I")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192DynamicRegistryBridge";
                    call.name = "enchantmentMaxLevel";
                    call.desc = "(Lnet/minecraft/resources/ResourceKey;)I";
                    call.itf = false;
                    rewrites++;
                    continue;
                }
                if (insn instanceof MethodInsnNode call
                        && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("net/minecraft/world/item/ItemStack")
                        && (call.name.equals("m_41663_") || call.name.equals("enchant"))
                        && call.desc.equals("(Lnet/minecraft/world/item/enchantment/"
                                + "Enchantment;I)V")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192DynamicRegistryBridge";
                    call.name = "enchant";
                    call.desc = "(Lnet/minecraft/world/item/ItemStack;"
                            + "Lnet/minecraft/resources/ResourceKey;I)V";
                    call.itf = false;
                    rewrites++;
                    continue;
                }
                if (insn instanceof MethodInsnNode call
                        && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESTATIC
                        && call.owner.equals("net/minecraft/world/item/enchantment/EnchantmentHelper")
                        && call.name.equals("getTagEnchantmentLevel")
                        && call.desc.equals("(Lnet/minecraft/world/item/enchantment/Enchantment;"
                                + "Lnet/minecraft/world/item/ItemStack;)I")) {
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192DynamicRegistryBridge";
                    call.name = "itemEnchantmentLevel";
                    call.desc = "(Lnet/minecraft/resources/ResourceKey;"
                            + "Lnet/minecraft/world/item/ItemStack;)I";
                    call.itf = false;
                    rewrites++;
                    continue;
                }
                if (insn instanceof MethodInsnNode call
                        && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("net/minecraft/world/item/ItemStack")
                        && call.name.equals("getEnchantmentLevel")
                        && call.desc.equals("(Lnet/minecraft/world/item/enchantment/Enchantment;)I")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192DynamicRegistryBridge";
                    call.name = "stackEnchantmentLevel";
                    call.desc = "(Lnet/minecraft/world/item/ItemStack;"
                            + "Lnet/minecraft/resources/ResourceKey;)I";
                    call.itf = false;
                    rewrites++;
                    continue;
                }
                if (insn instanceof MethodInsnNode call
                        && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESTATIC
                        && call.owner.equals("net/minecraft/world/item/enchantment/EnchantmentHelper")
                        && (call.desc.equals("(Lnet/minecraft/world/item/enchantment/Enchantment;"
                                        + "Lnet/minecraft/world/entity/LivingEntity;)Ljava/util/Map$Entry;")
                                || call.desc.equals("(Lnet/minecraft/world/item/enchantment/Enchantment;"
                                        + "Lnet/minecraft/world/entity/LivingEntity;"
                                        + "Ljava/util/function/Predicate;)Ljava/util/Map$Entry;"))) {
                    String canonical = call.name.startsWith("m_")
                            ? SRG_RUNTIME_RESOLVER.get().canonicalMethodName(
                                    call.owner, call.name, call.desc).orElse(null)
                            : call.name;
                    if (!"getRandomItemWith".equals(canonical)) continue;
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192DynamicRegistryBridge";
                    call.name = "randomItemWith";
                    call.desc = call.desc.replace(
                            "Lnet/minecraft/world/item/enchantment/Enchantment;",
                            "Lnet/minecraft/resources/ResourceKey;");
                    call.itf = false;
                    rewrites++;
                }
            }
        }
        return rewrites;
    }

    /** Retain the removed source dispatcher dependency as an explicit
     * fail-closed facade; functional tessellation requires a later semantic
     * migration to the target extracted block render-state pipeline. */
    private static int rewriteLegacyBlockRenderDispatcherFamily(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKEVIRTUAL) continue;
                if (call.owner.equals("net/minecraft/client/renderer/blockentity/"
                                + "BlockEntityRendererProvider$Context")
                        && call.name.equals("m_173584_")
                        && call.desc.equals("()Lcom/fuckingdeveloper/lms/runtime/render/"
                                + "LegacyBlockRenderDispatcher;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/render/LegacyBlockRenderDispatcher";
                    call.name = "from";
                    call.desc = "(Lnet/minecraft/client/renderer/blockentity/"
                            + "BlockEntityRendererProvider$Context;)"
                            + "Lcom/fuckingdeveloper/lms/runtime/render/LegacyBlockRenderDispatcher;";
                    call.itf = false;
                    rewrites++;
                } else if (call.owner.equals(
                                "com/fuckingdeveloper/lms/runtime/render/LegacyModelBlockRenderer")
                        && call.name.equals("tesselateBlock")
                        && call.desc.startsWith(
                                "(Lnet/minecraft/world/level/BlockAndTintGetter;")) {
                    call.desc = call.desc.replace(
                            "(Lnet/minecraft/world/level/BlockAndTintGetter;",
                            "(Lnet/minecraft/world/level/BlockGetter;");
                    rewrites++;
                }
            }
        }
        return rewrites;
    }

    /**
     * Forge 1.19.2 represented right-click use decisions with Event.Result;
     * target 26.3 retains the same three semantics using TriState. Rewrite the
     * exact event accessors/mutators and their value identities together.
     */
    private static int rewriteLegacyPlayerInteractionDecisions(ClassNode node) {
        final String event = "net/neoforged/neoforge/event/entity/player/"
                + "PlayerInteractEvent$RightClickBlock";
        final String legacyResult = "Lnet/neoforged/bus/api/Event$Result;";
        final String targetResult = "Lnet/minecraft/util/TriState;";
        int rewrites = 0;
        for (var method : node.methods) {
            boolean rightClickDecisionFamily = false;
            for (var candidate = method.instructions.getFirst();
                    candidate != null; candidate = candidate.getNext()) {
                if (!(candidate instanceof MethodInsnNode call) || !call.owner.equals(event)) continue;
                if (((call.name.equals("getUseBlock") || call.name.equals("getUseItem"))
                        && call.desc.equals("()" + legacyResult))
                        || ((call.name.equals("setUseBlock") || call.name.equals("setUseItem"))
                        && call.desc.equals("(" + legacyResult + ")V"))) {
                    rightClickDecisionFamily = true;
                    break;
                }
            }
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof MethodInsnNode call && call.owner.equals(event)) {
                    if ((call.name.equals("getUseBlock") || call.name.equals("getUseItem"))
                            && call.desc.equals("()" + legacyResult)) {
                        call.desc = "()" + targetResult;
                        rewrites++;
                        continue;
                    }
                    if ((call.name.equals("setUseBlock") || call.name.equals("setUseItem"))
                            && call.desc.equals("(" + legacyResult + ")V")) {
                        call.desc = "(" + targetResult + ")V";
                        rewrites++;
                        continue;
                    }
                }
                if (!(insn instanceof FieldInsnNode field)
                        || !rightClickDecisionFamily
                        || field.getOpcode() != org.objectweb.asm.Opcodes.GETSTATIC
                        || !field.owner.equals("net/neoforged/bus/api/Event$Result")
                        || !field.desc.equals(legacyResult)) continue;
                String mapped = switch (field.name) {
                    case "ALLOW" -> "TRUE";
                    case "DENY" -> "FALSE";
                    case "DEFAULT" -> "DEFAULT";
                    default -> null;
                };
                if (mapped == null) continue;
                field.owner = "net/minecraft/util/TriState";
                field.name = mapped;
                field.desc = targetResult;
                rewrites++;
            }
        }
        return rewrites;
    }

    /** Adapt the exact 1.19.2 Level#playSound player exclusion to Entity. */
    private static int rewriteLegacyLevelSoundPlayback(ClassNode node) {
        final String owner = "net/minecraft/world/level/Level";
        final String tail = "Lnet/minecraft/core/BlockPos;"
                + "Lnet/minecraft/sounds/SoundEvent;"
                + "Lnet/minecraft/sounds/SoundSource;FF)V";
        final String legacyDesc = "(Lnet/minecraft/world/entity/player/Player;" + tail;
        final String targetDesc = "(Lnet/minecraft/world/entity/Entity;" + tail;
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        || !call.owner.equals(owner)
                        || !call.name.equals("m_5594_")
                        || !call.desc.equals(legacyDesc)) continue;
                call.name = "playSound";
                call.desc = targetDesc;
                rewrites++;
            }
        }
        return rewrites;
    }

    /** Adapt 1.19.2 enum constants to the target sealed result-family fields. */
    private static int rewriteLegacyInteractionResultConstants(ClassNode node) {
        final String owner = "net/minecraft/world/InteractionResult";
        final String legacyDesc = "L" + owner + ";";
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof FieldInsnNode field)
                        || field.getOpcode() != org.objectweb.asm.Opcodes.GETSTATIC
                        || !field.owner.equals(owner)
                        || !field.desc.equals(legacyDesc)) continue;
                String targetType = switch (field.name) {
                    case "SUCCESS", "CONSUME" -> owner + "$Success";
                    case "PASS" -> owner + "$Pass";
                    case "FAIL" -> owner + "$Fail";
                    default -> null;
                };
                if (targetType == null) continue;
                field.desc = "L" + targetType + ";";
                rewrites++;
            }
        }
        return rewrites;
    }

    /** Migrate the exact built-in placed-block criterion and trigger call. */
    private static int rewriteLegacyPlacedBlockCriterion(ClassNode node) {
        final String criteria = "net/minecraft/advancements/triggers/CriteriaTriggers";
        final String sourceTrigger = "net/minecraft/advancements/critereon/PlacedBlockTrigger";
        final String targetTrigger = "net/minecraft/advancements/triggers/ItemUsedOnLocationTrigger";
        final String sourceDesc = "(Lnet/minecraft/server/level/ServerPlayer;"
                + "Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V";
        final String targetDesc = "(Lnet/minecraft/server/level/ServerPlayer;"
                + "Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemInstance;)V";
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof FieldInsnNode field
                        && field.getOpcode() == org.objectweb.asm.Opcodes.GETSTATIC
                        && field.owner.equals(criteria)
                        && field.name.equals("f_10591_")
                        && field.desc.equals("L" + sourceTrigger + ";")) {
                    field.name = "PLACED_BLOCK";
                    field.desc = "L" + targetTrigger + ";";
                    rewrites++;
                    continue;
                }
                if (insn instanceof MethodInsnNode call
                        && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals(sourceTrigger)
                        && call.name.equals("m_59469_")
                        && call.desc.equals(sourceDesc)) {
                    call.owner = targetTrigger;
                    call.name = "trigger";
                    call.desc = targetDesc;
                    rewrites++;
                }
            }
        }
        return rewrites;
    }

    /** Preserve exact block-state identity and player-sensitive clone semantics. */
    private static int rewriteLegacyBlockStateCloneItem(ClassNode node) {
        final String owner = "net/minecraft/world/level/block/state/BlockState";
        final String sourceDesc = "(Lnet/minecraft/world/phys/HitResult;"
                + "Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;"
                + "Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/item/ItemStack;";
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        || !call.owner.equals(owner)) continue;
                if (call.name.equals("getCloneItemStack") && call.desc.equals(sourceDesc)) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192BlockStateBridge";
                    call.name = "getCloneItemStack";
                    call.desc = "(L" + owner + ";" + sourceDesc.substring(1);
                    call.itf = false;
                    rewrites++;
                } else if (call.name.equals("m_60713_")
                        && call.desc.equals("(Lnet/minecraft/world/level/block/Block;)Z")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192BlockStateBridge";
                    call.name = "is";
                    call.desc = "(L" + owner + ";Lnet/minecraft/world/level/block/Block;)Z";
                    call.itf = false;
                    rewrites++;
                }
            }
        }
        return rewrites;
    }

    private static byte[] writeNode(ClassNode node) {
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    /**
     * Frame computation must not ask the application/system classloader to
     * resolve managed legacy classes. They intentionally live behind
     * ManagedLegacyClassLoader and may still contain pre-adaptation names.
     *
     * Object is always a verifier-safe common supertype for unrelated
     * reference values. Preserve identical types and the few array cases ASM
     * can safely reason about without class loading; otherwise merge to Object.
     */
    private static final class SafeFrameClassWriter extends ClassWriter {
        private static final String OBJECT = "java/lang/Object";
        private final LegacyClassTransformer.ClassBytesLookup managedClassBytes;
        private final java.util.Map<String, TypeInfo> cache = new java.util.HashMap<>();

        private SafeFrameClassWriter(int flags, LegacyClassTransformer.ClassBytesLookup managedClassBytes) {
            super(flags);
            this.managedClassBytes = managedClassBytes;
        }

        @Override
        protected String getCommonSuperClass(String type1, String type2) {
            if (type1.equals(type2)) return type1;
            if (type1.startsWith("[") || type2.startsWith("[")) {
                return commonArrayType(type1, type2);
            }
            if (isAssignableFrom(type1, type2)) return type1;
            if (isAssignableFrom(type2, type1)) return type2;

            TypeInfo first = info(type1);
            TypeInfo second = info(type2);
            if (first == null || second == null || first.isInterface || second.isInterface) return OBJECT;

            String cursor = first.superName;
            java.util.HashSet<String> seen = new java.util.HashSet<>();
            while (cursor != null && seen.add(cursor)) {
                if (isAssignableFrom(cursor, type2)) return cursor;
                TypeInfo parent = info(cursor);
                cursor = parent == null ? null : parent.superName;
            }
            return OBJECT;
        }

        private String commonArrayType(String first, String second) {
            if (!first.startsWith("[") || !second.startsWith("[")) return OBJECT;
            Type a = Type.getType(first);
            Type b = Type.getType(second);
            if (a.getDimensions() != b.getDimensions()) return OBJECT;
            Type ae = a.getElementType();
            Type be = b.getElementType();
            if (ae.getSort() != Type.OBJECT || be.getSort() != Type.OBJECT) return OBJECT;
            String common = getCommonSuperClass(ae.getInternalName(), be.getInternalName());
            return "[".repeat(a.getDimensions()) + "L" + common + ";";
        }

        private boolean isAssignableFrom(String target, String candidate) {
            if (target.equals(candidate) || target.equals(OBJECT)) return true;
            java.util.ArrayDeque<String> pending = new java.util.ArrayDeque<>();
            java.util.HashSet<String> seen = new java.util.HashSet<>();
            pending.add(candidate);
            while (!pending.isEmpty()) {
                String current = pending.removeFirst();
                if (!seen.add(current)) continue;
                if (target.equals(current)) return true;
                TypeInfo type = info(current);
                if (type == null) continue;
                if (type.superName != null) pending.addLast(type.superName);
                pending.addAll(type.interfaces);
            }
            return false;
        }

        private TypeInfo info(String internalName) {
            if (internalName == null) return null;
            if (cache.containsKey(internalName)) return cache.get(internalName);
            TypeInfo resolved = readManagedInfo(internalName);
            if (resolved == null) resolved = readRuntimeInfo(internalName);
            cache.put(internalName, resolved);
            return resolved;
        }

        private TypeInfo readManagedInfo(String internalName) {
            if (managedClassBytes == null) return null;
            try {
                byte[] bytes = managedClassBytes.find(internalName);
                if (bytes == null) return null;
                ClassReader reader = new ClassReader(bytes);
                String superName = projectHierarchyName(reader.getSuperName());
                String[] rawInterfaces = reader.getInterfaces();
                java.util.ArrayList<String> interfaces = new java.util.ArrayList<>(rawInterfaces.length);
                for (String iface : rawInterfaces) interfaces.add(projectHierarchyName(iface));
                return new TypeInfo(
                        projectHierarchyName(reader.getClassName()),
                        superName,
                        java.util.List.copyOf(interfaces),
                        (reader.getAccess() & org.objectweb.asm.Opcodes.ACC_INTERFACE) != 0);
            } catch (java.io.IOException | RuntimeException ignored) {
                return null;
            }
        }

        private TypeInfo readRuntimeInfo(String internalName) {
            if (!isParentRuntimeType(internalName)) return null;
            try {
                ClassLoader loader = Forge1192LegacyClassTransformer.class.getClassLoader();
                Class<?> type = Class.forName(internalName.replace('/', '.'), false, loader);
                Class<?> parent = type.getSuperclass();
                java.util.ArrayList<String> interfaces = new java.util.ArrayList<>();
                for (Class<?> iface : type.getInterfaces()) {
                    interfaces.add(iface.getName().replace('.', '/'));
                }
                return new TypeInfo(
                        internalName,
                        parent == null ? null : parent.getName().replace('.', '/'),
                        java.util.List.copyOf(interfaces),
                        type.isInterface());
            } catch (LinkageError | ClassNotFoundException ignored) {
                return null;
            }
        }

        private static String projectHierarchyName(String internalName) {
            return internalName == null ? null : migrateInternalName(internalName);
        }

        private static boolean isParentRuntimeType(String type) {
            return type.startsWith("java/")
                    || type.startsWith("javax/")
                    || type.startsWith("jdk/")
                    || type.startsWith("net/minecraft/")
                    || type.startsWith("net/neoforged/")
                    || type.startsWith("com/mojang/")
                    || type.startsWith("com/fuckingdeveloper/lms/");
        }

        private record TypeInfo(String name, String superName, java.util.List<String> interfaces,
                                boolean isInterface) {}
    }

    /**
     * Conservative first migration pass. Only namespaces whose classes are
     * supplied by current NeoForge are rewritten here. Removed APIs are left
     * untouched so linkage remains fail-closed until a semantic adapter exists.
     */
    private static int rewriteExactNamespaceMigrations(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof MethodInsnNode call) {
                    String owner = migrateInternalName(call.owner);
                    String desc = migrateDescriptor(call.desc);
                    if (!owner.equals(call.owner) || !desc.equals(call.desc)) {
                        call.owner = owner;
                        call.desc = desc;
                        rewrites++;
                    }
                } else if (insn instanceof FieldInsnNode field) {
                    String owner = migrateInternalName(field.owner);
                    String desc = migrateDescriptor(field.desc);
                    if (!owner.equals(field.owner) || !desc.equals(field.desc)) {
                        field.owner = owner;
                        field.desc = desc;
                        rewrites++;
                    }
                } else if (insn instanceof TypeInsnNode type) {
                    String migrated = migrateInternalName(type.desc);
                    if (!migrated.equals(type.desc)) {
                        type.desc = migrated;
                        rewrites++;
                    }
                } else if (insn instanceof LdcInsnNode ldc) {
                    Object migrated = migrateConstant(ldc.cst);
                    if (migrated != ldc.cst) {
                        ldc.cst = migrated;
                        rewrites++;
                    }
                } else if (insn instanceof InvokeDynamicInsnNode indy) {
                    String descriptor = migrateDescriptor(indy.desc);
                    if (!descriptor.equals(indy.desc)) { indy.desc = descriptor; rewrites++; }
                    Handle bootstrap = migrateHandle(indy.bsm);
                    if (bootstrap != indy.bsm) { indy.bsm = bootstrap; rewrites++; }
                    for (int i = 0; i < indy.bsmArgs.length; i++) {
                        Object migrated = migrateConstant(indy.bsmArgs[i]);
                        if (migrated != indy.bsmArgs[i]) { indy.bsmArgs[i] = migrated; rewrites++; }
                    }
                }
            }
            String migratedMethodDesc = migrateDescriptor(method.desc);
            if (!migratedMethodDesc.equals(method.desc)) {
                method.desc = migratedMethodDesc;
                rewrites++;
            }
        }
        return rewrites;
    }


    /**
     * Migrate class-structure references as well as executable instructions.
     * JVM linkage can resolve field descriptors, signatures, exceptions and
     * annotations before the corresponding code path is ever executed.
     */
    private static int rewriteClassStructureNamespaceMigrations(ClassNode node) {
        int rewrites = 0;
        String superName = migrateInternalName(node.superName);
        if (!superName.equals(node.superName)) { node.superName = superName; rewrites++; }
        for (int i = 0; i < node.interfaces.size(); i++) {
            String old = node.interfaces.get(i), migrated = migrateInternalName(old);
            if (!migrated.equals(old)) { node.interfaces.set(i, migrated); rewrites++; }
        }
        if (node.signature != null) {
            String migrated = migrateDescriptor(node.signature);
            if (!migrated.equals(node.signature)) { node.signature = migrated; rewrites++; }
        }
        for (var field : node.fields) {
            String migrated = migrateDescriptor(field.desc);
            if (!migrated.equals(field.desc)) { field.desc = migrated; rewrites++; }
            if (field.signature != null) {
                migrated = migrateDescriptor(field.signature);
                if (!migrated.equals(field.signature)) { field.signature = migrated; rewrites++; }
            }
        }
        for (var method : node.methods) {
            if (method.signature != null) {
                String migrated = migrateDescriptor(method.signature);
                if (!migrated.equals(method.signature)) { method.signature = migrated; rewrites++; }
            }
            if (method.exceptions != null) {
                for (int i = 0; i < method.exceptions.size(); i++) {
                    String old = method.exceptions.get(i), migrated = migrateInternalName(old);
                    if (!migrated.equals(old)) { method.exceptions.set(i, migrated); rewrites++; }
                }
            }
            for (var local : method.localVariables == null ? java.util.List.<org.objectweb.asm.tree.LocalVariableNode>of() : method.localVariables) {
                String migrated = migrateDescriptor(local.desc);
                if (!migrated.equals(local.desc)) { local.desc = migrated; rewrites++; }
                if (local.signature != null) {
                    migrated = migrateDescriptor(local.signature);
                    if (!migrated.equals(local.signature)) { local.signature = migrated; rewrites++; }
                }
            }
        }
        return rewrites;
    }

    /**
     * Forge 1.19.2 exposed a built-in mod list screen that no longer exists
     * in the target runtime. An old GUI may only use it as an optional
     * navigation destination. In that case the original parent Screen is a
     * safe fallback, while manufacturing a fake screen class is not.
     *
     * <p>Recognize only the proven constructor stack pattern:
     * NEW ModListScreen; DUP; [code producing parent Screen];
     * INVOKESPECIAL ModListScreen(Screen). Replace it with the already
     * evaluated parent Screen. This deliberately does not touch any other
     * constructor or any other use of the removed type.</p>
     */
    private static int rewriteRemovedOptionalClientScreenConstructors(ClassNode node) {
        final String removed = "net/minecraftforge/client/gui/ModListScreen";
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; ) {
                var next = insn.getNext();
                if (insn instanceof MethodInsnNode call
                        && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESPECIAL
                        && call.owner.equals(removed)
                        && call.name.equals("<init>")
                        && call.desc.equals("(Lnet/minecraft/client/gui/screens/Screen;)V")) {
                    // The NEW and DUP must be adjacent real instructions. Do
                    // not rewrite arbitrary constructors with unknown stack
                    // shape, and never suppress a required gameplay class.
                    TypeInsnNode allocation = null;
                    org.objectweb.asm.tree.AbstractInsnNode duplicate = null;
                    int steps = 0;
                    for (var previous = call.getPrevious(); previous != null && steps++ < 12;
                         previous = previous.getPrevious()) {
                        if (previous instanceof TypeInsnNode type
                                && type.getOpcode() == org.objectweb.asm.Opcodes.NEW
                                && type.desc.equals(removed)) {
                            var after = type.getNext();
                            while (after != null && after.getOpcode() < 0) after = after.getNext();
                            if (after != null && after.getOpcode() == org.objectweb.asm.Opcodes.DUP) {
                                allocation = type;
                                duplicate = after;
                            }
                            break;
                        }
                    }
                    if (allocation != null) {
                        method.instructions.remove(call);
                        method.instructions.remove(duplicate);
                        method.instructions.remove(allocation);
                        rewrites++;
                    }
                }
                insn = next;
            }
        }
        return rewrites;
    }

    /**
     * Forge 1.19.2 repository sources received a PackConstructor callback,
     * while the 26.3 RepositorySource contract makes the source construct the
     * Pack itself. Adapt only the exact source override, and expose the old
     * PackResources operations through a typed LMS contract so the target
     * runtime can wrap them without reflecting on legacy implementation names.
     */
    private int rewriteLegacyPackRepositoryFamily(ClassNode node) {
        final String repositorySource = "net/minecraft/server/packs/repository/RepositorySource";
        final String oldLoadDescriptor = "(Ljava/util/function/Consumer;"
                + "Lnet/minecraft/server/packs/repository/Pack$PackConstructor;)V";
        final String packResources = "net/minecraft/server/packs/PackResources";
        final String access = "com/fuckingdeveloper/lms/runtime/Forge1192LegacyPackResourcesAccess";
        final String bridge = "com/fuckingdeveloper/lms/runtime/Forge1192PackRepositoryBridge";
        int rewrites = 0;

        if (isManagedSubtypeOf(node, repositorySource)) {
            for (var method : node.methods) {
                if (!method.name.equals("m_7686_") || !method.desc.equals(oldLoadDescriptor)) continue;
                int constructorLoads = 0;
                int constructorCalls = 0;
                for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                    if (insn instanceof org.objectweb.asm.tree.VarInsnNode variable && variable.var == 2) {
                        if (variable.getOpcode() != org.objectweb.asm.Opcodes.ALOAD) {
                            throw new IllegalStateException("Legacy RepositorySource mutates PackConstructor local: "
                                    + node.name + "#" + method.name + method.desc);
                        }
                        constructorLoads++;
                    } else if (insn instanceof org.objectweb.asm.tree.IincInsnNode increment
                            && increment.var == 2) {
                        throw new IllegalStateException("Legacy RepositorySource mutates PackConstructor local: "
                                + node.name + "#" + method.name + method.desc);
                    }
                    if (insn instanceof MethodInsnNode call
                            && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEINTERFACE
                            && call.owner.equals("net/minecraft/server/packs/repository/Pack$PackConstructor")
                            && call.name.equals("create")) {
                        String targetDescriptor;
                        if (call.desc.equals("(Ljava/lang/String;Lnet/minecraft/network/chat/Component;Z"
                                + "Ljava/util/function/Supplier;Lnet/minecraft/server/packs/metadata/pack/PackMetadataSection;"
                                + "Lnet/minecraft/server/packs/repository/Pack$Position;"
                                + "Lnet/minecraft/server/packs/repository/PackSource;Z)"
                                + "Lnet/minecraft/server/packs/repository/Pack;")) {
                            targetDescriptor = "(Ljava/lang/String;Lnet/minecraft/network/chat/Component;Z"
                                    + "Ljava/util/function/Supplier;Lnet/minecraft/server/packs/metadata/pack/PackMetadataSection;"
                                    + "Lnet/minecraft/server/packs/repository/Pack$Position;"
                                    + "Lnet/minecraft/server/packs/repository/PackSource;Z)"
                                    + "Lnet/minecraft/server/packs/repository/Pack;";
                        } else if (call.desc.equals("(Ljava/lang/String;Lnet/minecraft/network/chat/Component;Z"
                                + "Ljava/util/function/Supplier;Lnet/minecraft/server/packs/metadata/pack/PackMetadataSection;"
                                + "Lnet/minecraft/server/packs/repository/Pack$Position;"
                                + "Lnet/minecraft/server/packs/repository/PackSource;)"
                                + "Lnet/minecraft/server/packs/repository/Pack;")) {
                            targetDescriptor = call.desc;
                        } else {
                            throw new IllegalStateException("Unsupported legacy PackConstructor#create shape: "
                                    + node.name + "#" + method.name + " call=" + call.desc);
                        }
                        call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                        call.owner = bridge;
                        call.name = "create";
                        call.desc = targetDescriptor;
                        call.itf = false;
                        constructorCalls++;
                    }
                }
                if (constructorCalls == 0 || constructorLoads != constructorCalls) {
                    throw new IllegalStateException("Legacy RepositorySource PackConstructor data flow is ambiguous: "
                            + node.name + "#" + method.name + method.desc
                            + " loads=" + constructorLoads + " calls=" + constructorCalls);
                }
                for (var insn = method.instructions.getFirst(); insn != null; ) {
                    var next = insn.getNext();
                    if (insn instanceof org.objectweb.asm.tree.VarInsnNode variable
                            && variable.var == 2 && variable.getOpcode() == org.objectweb.asm.Opcodes.ALOAD) {
                        method.instructions.remove(insn);
                    }
                    insn = next;
                }
                method.name = "loadPacks";
                method.desc = "(Ljava/util/function/Consumer;)V";
                method.signature = null;
                rewrites += constructorCalls + constructorLoads + 2;
            }
        }

        if (isManagedSubtypeOf(node, packResources)) {
            var required = new java.util.LinkedHashMap<String, String>();
            required.put("m_5542_(Ljava/lang/String;)Ljava/io/InputStream;", "lms$legacyGetRootResource");
            required.put("m_8031_(Lnet/minecraft/server/packs/PackType;Lnet/minecraft/resources/ResourceLocation;)Ljava/io/InputStream;",
                    "lms$legacyGetResource");
            required.put("m_214146_(Lnet/minecraft/server/packs/PackType;Ljava/lang/String;Ljava/lang/String;Ljava/util/function/Predicate;)Ljava/util/Collection;",
                    "lms$legacyListResources");
            required.put("m_5698_(Lnet/minecraft/server/packs/PackType;)Ljava/util/Set;", "lms$legacyNamespaces");
            required.put("m_8017_()Ljava/lang/String;", "lms$legacyPackName");
            for (var entry : required.entrySet()) {
                int split = entry.getKey().indexOf('(');
                String oldName = entry.getKey().substring(0, split);
                String descriptor = entry.getKey().substring(split);
                // rewriteVerifiedVanillaOverrides runs earlier and may already
                // project an exact source SRG identity to its canonical source
                // name when that declaration also remains a valid target
                // override (currently getNamespaces). Both names therefore
                // represent the same mapping-proven 1.19.2 operation here.
                String canonicalSourceName = switch (oldName) {
                    case "m_5542_" -> "getRootResource";
                    case "m_8031_" -> "getResource";
                    case "m_214146_" -> "getResources";
                    case "m_5698_" -> "getNamespaces";
                    case "m_8017_" -> "getName";
                    default -> throw new IllegalStateException("Unknown legacy PackResources identity: " + oldName);
                };
                var matches = node.methods.stream()
                        .filter(method -> (method.name.equals(oldName) || method.name.equals(canonicalSourceName))
                                && method.desc.equals(descriptor))
                        .toList();
                if (matches.size() != 1) {
                    throw new IllegalStateException("Legacy PackResources implementation requires exact declared operation "
                            + entry.getKey() + ": " + node.name + " matches=" + matches.size());
                }
                matches.getFirst().name = entry.getValue();
                rewrites++;
            }
            if (!node.interfaces.contains(access)) {
                node.interfaces.add(access);
                rewrites++;
            }
        }
        return rewrites;
    }

    private static int rewriteLegacyPackMetadataConstruction(ClassNode node) {
        final String owner = "net/minecraft/server/packs/metadata/pack/PackMetadataSection";
        final String legacyDescriptor = "(Lnet/minecraft/network/chat/Component;I)V";
        final String bridge = "com/fuckingdeveloper/lms/runtime/Forge1192PackRepositoryBridge";
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; ) {
                var next = insn.getNext();
                if (!(insn instanceof TypeInsnNode allocation)
                        || allocation.getOpcode() != org.objectweb.asm.Opcodes.NEW
                        || !allocation.desc.equals(owner)) {
                    insn = next;
                    continue;
                }
                var duplicate = allocation.getNext();
                if (duplicate == null || duplicate.getOpcode() != org.objectweb.asm.Opcodes.DUP) {
                    throw new IllegalStateException("Unsupported legacy PackMetadataSection allocation: "
                            + node.name + "#" + method.name + method.desc);
                }
                MethodInsnNode init = null;
                int nestedAllocations = 0;
                for (var cursor = duplicate.getNext(); cursor != null; cursor = cursor.getNext()) {
                    if (cursor instanceof TypeInsnNode nested
                            && nested.getOpcode() == org.objectweb.asm.Opcodes.NEW) {
                        nestedAllocations++;
                    } else if (cursor instanceof MethodInsnNode candidate
                            && candidate.getOpcode() == org.objectweb.asm.Opcodes.INVOKESPECIAL
                            && candidate.name.equals("<init>")) {
                        if (nestedAllocations > 0) {
                            nestedAllocations--;
                        } else {
                            if (candidate.owner.equals(owner) && candidate.desc.equals(legacyDescriptor)) init = candidate;
                            break;
                        }
                    }
                }
                if (init == null) {
                    throw new IllegalStateException("Cannot pair legacy PackMetadataSection constructor: "
                            + node.name + "#" + method.name + method.desc);
                }
                method.instructions.remove(allocation);
                method.instructions.remove(duplicate);
                init.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                init.owner = bridge;
                init.name = "metadata";
                init.desc = legacyDescriptor.substring(0, legacyDescriptor.length() - 1) + "L" + owner + ";";
                init.itf = false;
                rewrites++;
                next = init.getNext();
                insn = next;
            }
        }
        return rewrites;
    }

    /**
     * The old Gson/directory SimpleJsonResourceReloadListener became a
     * codec/FileToIdConverter listener. Preserve the legacy JsonElement map
     * contract through a target-backed superclass rather than guessing a
     * codec for each legacy subclass.
     */
    private static int rewriteLegacySimpleJsonReloadListener(ClassNode node) {
        final String source = "net/minecraft/server/packs/resources/SimpleJsonResourceReloadListener";
        final String bridge = "com/fuckingdeveloper/lms/runtime/Forge1192SimpleJsonResourceReloadListener";
        if (!node.superName.equals(source)) return 0;
        int constructorCalls = 0;
        for (var method : node.methods) {
            if (!method.name.equals("<init>")) continue;
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof MethodInsnNode call
                        && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESPECIAL
                        && call.owner.equals(source)
                        && call.name.equals("<init>")
                        && call.desc.equals("(Lcom/google/gson/Gson;Ljava/lang/String;)V")) {
                    call.owner = bridge;
                    constructorCalls++;
                }
            }
        }
        if (constructorCalls == 0) {
            throw new IllegalStateException("Legacy SimpleJsonResourceReloadListener subclass has no exact source constructor: "
                    + node.name);
        }
        node.superName = bridge;
        return constructorCalls + 1;
    }

    /** Exact Forge 1.19.2 Ingredient factories/accessors removed by the holder-set model. */
    private static int rewriteLegacyIngredientFamily(ClassNode node) {
        final String ingredient = "net/minecraft/world/item/crafting/Ingredient";
        final String bridge = "com/fuckingdeveloper/lms/runtime/Forge1192IngredientBridge";
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call) || !call.owner.equals(ingredient)) continue;
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESTATIC
                        && call.name.equals("m_43927_")
                        && call.desc.equals("([Lnet/minecraft/world/item/ItemStack;)"
                                + "Lnet/minecraft/world/item/crafting/Ingredient;")) {
                    call.owner = bridge;
                    call.name = "ofStacks";
                    call.itf = false;
                    rewrites++;
                } else if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESTATIC
                        && call.name.equals("m_43921_")
                        && call.desc.equals("(Ljava/util/stream/Stream;)"
                                + "Lnet/minecraft/world/item/crafting/Ingredient;")) {
                    call.owner = bridge;
                    call.name = "ofStackStream";
                    call.itf = false;
                    rewrites++;
                } else if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESTATIC
                        && call.name.equals("m_43929_")
                        && call.desc.equals("([Lnet/minecraft/world/level/ItemLike;)"
                                + "Lnet/minecraft/world/item/crafting/Ingredient;")) {
                    call.name = "of";
                    rewrites++;
                } else if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.name.equals("m_43908_")
                        && call.desc.equals("()[Lnet/minecraft/world/item/ItemStack;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = bridge;
                    call.name = "items";
                    call.desc = "(Lnet/minecraft/world/item/crafting/Ingredient;)"
                            + "[Lnet/minecraft/world/item/ItemStack;";
                    call.itf = false;
                    rewrites++;
                }
            }
        }
        return rewrites;
    }

    /** Exact removed DyeItem color index, rebuilt from target DYE components. */
    private static int rewriteLegacyDyeItemLookup(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof MethodInsnNode call
                        && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESTATIC
                        && call.owner.equals("net/minecraft/world/item/DyeItem")
                        && (call.name.equals("m_41082_") || call.name.equals("byColor"))
                        && call.desc.equals("(Lnet/minecraft/world/item/DyeColor;)"
                                + "Lnet/minecraft/world/item/DyeItem;")) {
                    call.owner = "com/fuckingdeveloper/lms/runtime/item/Forge1192DyeItemBridge";
                    call.name = "byColor";
                    call.itf = false;
                    rewrites++;
                }
            }
        }
        return rewrites;
    }

    /**
     * DFU split source {@code Codec.unit(Supplier)} from target registry
     * {@code MapCodec.unit(Supplier)}. Migrate the producing field and every
     * managed field access together, identified by the exact source
     * initializer shape rather than by field name or descriptor alone.
     */
    private int rewriteLegacyUnitMapCodecFamily(ClassNode node) {
        final String codecDescriptor = "Lcom/mojang/serialization/Codec;";
        final String mapCodecDescriptor = "Lcom/mojang/serialization/MapCodec;";
        int rewrites = 0;

        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKESTATIC
                        || !call.owner.equals("com/mojang/serialization/Codec")
                        || !call.name.equals("unit")
                        || !call.desc.equals("(Ljava/util/function/Supplier;)"
                                + codecDescriptor)) {
                    continue;
                }
                var store = nextRealInstruction(call.getNext());
                if (!(store instanceof FieldInsnNode field)
                        || field.getOpcode() != org.objectweb.asm.Opcodes.PUTSTATIC
                        || !field.owner.equals(node.name)
                        || !field.desc.equals(codecDescriptor)) {
                    throw new IllegalStateException(
                            "Unsupported Forge 1.19.2 Codec.unit storage shape in "
                                    + node.name + "#" + method.name + method.desc);
                }
                call.owner = "com/mojang/serialization/MapCodec";
                call.name = "unit";
                call.desc = "(Ljava/util/function/Supplier;)" + mapCodecDescriptor;
                call.itf = false;
                field.desc = mapCodecDescriptor;
                boolean found = false;
                for (var declared : node.fields) {
                    if (declared.name.equals(field.name)
                            && declared.desc.equals(codecDescriptor)) {
                        declared.desc = mapCodecDescriptor;
                        found = true;
                    }
                }
                if (!found) {
                    throw new IllegalStateException(
                            "Missing Forge 1.19.2 Codec.unit field " + node.name + "." + field.name);
                }
                legacyUnitMapCodecFields.put(node.name + "." + field.name, true);
                rewrites += 3;
            }
        }

        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof FieldInsnNode field
                        && field.desc.equals(codecDescriptor)
                        && isLegacyUnitMapCodecField(field.owner, field.name)) {
                    field.desc = mapCodecDescriptor;
                    rewrites++;
                }
            }
        }
        return rewrites;
    }

    private boolean isLegacyUnitMapCodecField(String owner, String name) {
        String key = owner + "." + name;
        return legacyUnitMapCodecFields.computeIfAbsent(key, ignored -> {
            try {
                byte[] bytes = managedClassBytes == null ? null : managedClassBytes.find(owner);
                if (bytes == null) return false;
                ClassNode candidate = new ClassNode();
                new ClassReader(bytes).accept(candidate, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                for (var method : candidate.methods) {
                    if (!method.name.equals("<clinit>")) continue;
                    for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                        if (!(insn instanceof MethodInsnNode call)
                                || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKESTATIC
                                || !call.owner.equals("com/mojang/serialization/Codec")
                                || !call.name.equals("unit")
                                || !call.desc.equals("(Ljava/util/function/Supplier;)"
                                        + "Lcom/mojang/serialization/Codec;")) {
                            continue;
                        }
                        var store = nextRealInstruction(call.getNext());
                        if (store instanceof FieldInsnNode field
                                && field.getOpcode() == org.objectweb.asm.Opcodes.PUTSTATIC
                                && field.owner.equals(owner)
                                && field.name.equals(name)
                                && field.desc.equals("Lcom/mojang/serialization/Codec;")) {
                            return true;
                        }
                    }
                }
                return false;
            } catch (java.io.IOException exception) {
                throw new IllegalStateException(
                        "Cannot inspect legacy Codec.unit field " + key, exception);
            }
        });
    }

    /**
     * Forge 1.19.2 custom criteria owned their listener maps and registered
     * through CriteriaTriggers. Target 26.3 stores active instances inside
     * PlayerAdvancements and requires a codec-backed trigger. Adapt only the
     * exact source interface/members and the proven manual-listener lookup
     * shape; ordinary Map.get calls are deliberately outside this rule.
     */
    private static int rewriteLegacyCriterionTriggerFamily(ClassNode node) {
        final String sourceTrigger = "net/minecraft/advancements/CriterionTrigger";
        final String legacyTrigger =
                "com/fuckingdeveloper/lms/runtime/advancement/Forge1192LegacyCriterionTrigger";
        final String bridge =
                "com/fuckingdeveloper/lms/runtime/advancement/Forge1192CriterionBridge";
        boolean implementsSourceTrigger = node.interfaces.contains(sourceTrigger);
        int rewrites = 0;

        if (implementsSourceTrigger) {
            int index = node.interfaces.indexOf(sourceTrigger);
            node.interfaces.set(index, legacyTrigger);
            rewrites++;
            for (var method : node.methods) {
                if ((method.name.equals("m_7295_") || method.name.equals("getId"))
                        && method.desc.equals("()Lnet/minecraft/resources/ResourceLocation;")) {
                    method.name = "lms$criterionId";
                    rewrites++;
                } else if ((method.name.equals("m_5868_") || method.name.equals("createInstance"))
                        && method.desc.equals("(Lcom/google/gson/JsonObject;"
                                + "Lnet/minecraft/advancements/critereon/DeserializationContext;)"
                                + "Lnet/minecraft/advancements/CriterionTriggerInstance;")) {
                    method.name = "lms$createInstance";
                    method.desc = "(Lcom/google/gson/JsonObject;"
                            + "Lcom/fuckingdeveloper/lms/runtime/advancement/Forge1192LegacyDeserializationContext;)"
                            + "Lnet/minecraft/advancements/CriterionTriggerInstance;";
                    rewrites++;
                }
            }
        }

        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)) continue;
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESTATIC
                        && call.owner.equals("net/minecraft/advancements/critereon/InventoryChangeTrigger$TriggerInstance")
                        && call.name.equals("m_43199_")
                        && call.desc.equals("([Lnet/minecraft/world/level/ItemLike;)"
                                + "Lnet/minecraft/advancements/critereon/InventoryChangeTrigger$TriggerInstance;")) {
                    call.owner = "net/minecraft/advancements/triggers/InventoryChangeTrigger$TriggerInstance";
                    call.name = "hasItems";
                    call.desc = "([Lnet/minecraft/world/level/ItemLike;)"
                            + "Lnet/minecraft/advancements/triggers/Criterion;";
                    call.itf = false;
                    rewrites++;
                    continue;
                }
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESTATIC
                        && call.owner.equals("net/minecraft/advancements/CriteriaTriggers")
                        && (call.name.equals("m_10595_") || call.name.equals("register"))
                        && call.desc.equals("(Lnet/minecraft/advancements/CriterionTrigger;)"
                                + "Lnet/minecraft/advancements/CriterionTrigger;")) {
                    call.owner = bridge;
                    call.name = "register";
                    call.desc = "(L" + legacyTrigger + ";)L" + legacyTrigger + ";";
                    call.itf = false;
                    rewrites++;
                    continue;
                }
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("net/minecraft/advancements/Advancement$Builder")
                        && (call.name.equals("m_138386_") || call.name.equals("addCriterion"))
                        && call.desc.equals("(Ljava/lang/String;"
                                + "Lnet/minecraft/advancements/CriterionTriggerInstance;)"
                                + "Lnet/minecraft/advancements/Advancement$Builder;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = bridge;
                    call.name = "addCriterion";
                    call.desc = "(Lnet/minecraft/advancements/Advancement$Builder;Ljava/lang/String;"
                            + "Ljava/lang/Object;)"
                            + "Lnet/minecraft/advancements/Advancement$Builder;";
                    call.itf = false;
                    rewrites++;
                    continue;
                }
                if (!implementsSourceTrigger
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKEINTERFACE
                        || !call.owner.equals("java/util/Map")
                        || !call.name.equals("get")
                        || !call.desc.equals("(Ljava/lang/Object;)Ljava/lang/Object;")) {
                    continue;
                }
                var previous = call.getPrevious();
                while (previous != null && previous.getOpcode() < 0) previous = previous.getPrevious();
                if (previous instanceof MethodInsnNode advancements
                        && advancements.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && advancements.owner.equals("net/minecraft/server/level/ServerPlayer")
                        && (advancements.name.equals("m_8960_")
                                || advancements.name.equals("getAdvancements"))
                        && advancements.desc.equals("()Lnet/minecraft/server/PlayerAdvancements;")) {
                    method.instructions.insertBefore(call,
                            new org.objectweb.asm.tree.VarInsnNode(
                                    org.objectweb.asm.Opcodes.ALOAD, 0));
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = bridge;
                    call.name = "activeListeners";
                    call.desc = "(Ljava/util/Map;Lnet/minecraft/server/PlayerAdvancements;L"
                            + legacyTrigger + ";)Ljava/lang/Object;";
                    call.itf = false;
                    rewrites++;
                }
            }
        }
        return rewrites;
    }

    /** Exact 1.19.2 DisplayInfo value construction migrated to target records/assets. */
    private static int rewriteLegacyAdvancementDisplayFamily(ClassNode node) {
        final String owner = "net/minecraft/advancements/DisplayInfo";
        final String sourceDescriptor = "(Lnet/minecraft/world/item/ItemStack;"
                + "Lnet/minecraft/network/chat/Component;"
                + "Lnet/minecraft/network/chat/Component;"
                + "Lnet/minecraft/resources/ResourceLocation;"
                + "Lnet/minecraft/advancements/FrameType;ZZZ)V";
        final String bridge =
                "com/fuckingdeveloper/lms/runtime/advancement/Forge1192AdvancementBridge";
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; ) {
                var next = insn.getNext();
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKESPECIAL
                        || !call.owner.equals(owner)
                        || !call.name.equals("<init>")
                        || !call.desc.equals(sourceDescriptor)) {
                    insn = next;
                    continue;
                }
                TypeInsnNode allocation = null;
                org.objectweb.asm.tree.AbstractInsnNode duplicate = null;
                for (var cursor = call.getPrevious(); cursor != null; cursor = cursor.getPrevious()) {
                    if (cursor instanceof TypeInsnNode type
                            && type.getOpcode() == org.objectweb.asm.Opcodes.NEW
                            && type.desc.equals(owner)) {
                        var possibleDuplicate = cursor.getNext();
                        while (possibleDuplicate != null && possibleDuplicate.getOpcode() < 0) {
                            possibleDuplicate = possibleDuplicate.getNext();
                        }
                        if (possibleDuplicate != null
                                && possibleDuplicate.getOpcode() == org.objectweb.asm.Opcodes.DUP) {
                            allocation = type;
                            duplicate = possibleDuplicate;
                            break;
                        }
                    }
                }
                if (allocation == null) {
                    throw new IllegalStateException(
                            "Cannot pair Forge 1.19.2 DisplayInfo construction in "
                                    + node.name + "#" + method.name + method.desc);
                }
                method.instructions.remove(allocation);
                method.instructions.remove(duplicate);
                call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                call.owner = bridge;
                call.name = "displayInfo";
                call.desc = sourceDescriptor.substring(0, sourceDescriptor.length() - 1)
                        + "L" + owner + ";";
                call.itf = false;
                rewrites++;
                insn = next;
            }
        }
        return rewrites;
    }

    /**
     * Normal signs no longer have a dedicated SignItem subclass. They use
     * StandingAndWallBlockItem with a downward attachment direction and the
     * sign-text item component. Reorder the legacy constructor operands and
     * apply both pieces of the modern contract before namespace migration.
     */
    private static int rewriteLegacySignItemConstruction(ClassNode node) {
        int rewrites = 0;
        final String legacyOwner = "net/minecraft/world/item/SignItem";
        final String legacyDesc = "(Lnet/minecraft/world/item/Item$Properties;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/level/block/Block;)V";
        for (var method : node.methods) {
            RegistryNameEvidence registryName = method.name.equals("<init>")
                    ? legacyRegistryNameEvidence(node, method) : null;
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKESPECIAL
                        || !call.owner.equals(legacyOwner)
                        || !call.name.equals("<init>")
                        || !call.desc.equals(legacyDesc)) continue;

                var adaptation = new org.objectweb.asm.tree.InsnList();
                int preservedPropertiesLocal = -1;
                if (registryName != null) {
                    // Keep uninitialized this untouched on the operand stack;
                    // spill only the constructor arguments while attaching the
                    // bytecode-proven item id to Properties.
                    int wallLocal = method.maxLocals++;
                    int standingLocal = method.maxLocals++;
                    int propertiesLocal = method.maxLocals++;
                    preservedPropertiesLocal = propertiesLocal;
                    adaptation.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ASTORE, wallLocal));
                    adaptation.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ASTORE, standingLocal));
                    adaptation.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ASTORE, propertiesLocal));
                    adaptation.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD, standingLocal));
                    adaptation.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD, wallLocal));
                    adaptation.add(new FieldInsnNode(org.objectweb.asm.Opcodes.GETSTATIC,
                            "net/minecraft/core/Direction", "DOWN", "Lnet/minecraft/core/Direction;"));
                    adaptation.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD, propertiesLocal));
                    appendRegistryNameLoad(adaptation, registryName);
                    adaptation.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC,
                            "com/fuckingdeveloper/lms/runtime/Forge1192VanillaConstructionBridge",
                            "withItemId",
                            "(Lnet/minecraft/world/item/Item$Properties;Ljava/lang/String;)"
                                    + "Lnet/minecraft/world/item/Item$Properties;", false));
                } else {
                    // this, properties, standing, wall -> this, standing, wall, properties
                    adaptation.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.DUP2_X1));
                    adaptation.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.POP2));
                    adaptation.add(new FieldInsnNode(org.objectweb.asm.Opcodes.GETSTATIC,
                            "net/minecraft/core/Direction", "DOWN", "Lnet/minecraft/core/Direction;"));
                    adaptation.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.SWAP));
                }
                adaptation.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKEVIRTUAL,
                        "net/minecraft/world/item/Item$Properties", "signText",
                        "()Lnet/minecraft/world/item/Item$Properties;", false));
                method.instructions.insertBefore(call, adaptation);
                call.owner = "net/minecraft/world/item/StandingAndWallBlockItem";
                call.desc = "(Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/core/Direction;Lnet/minecraft/world/item/Item$Properties;)V";
                if (preservedPropertiesLocal >= 0) {
                    var association = new org.objectweb.asm.tree.InsnList();
                    association.add(new org.objectweb.asm.tree.VarInsnNode(
                            org.objectweb.asm.Opcodes.ALOAD, 0));
                    association.add(new org.objectweb.asm.tree.VarInsnNode(
                            org.objectweb.asm.Opcodes.ALOAD, preservedPropertiesLocal));
                    association.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC,
                            "com/fuckingdeveloper/lms/runtime/Forge1192VanillaConstructionBridge",
                            "associateItemProperties",
                            "(Lnet/minecraft/world/item/Item;"
                                    + "Lnet/minecraft/world/item/Item$Properties;)V", false));
                    method.instructions.insert(call, association);
                }
                rewrites++;
            }
        }
        return rewrites;
    }

    /**
     * Sign block construction retained both source values but reversed their
     * order. Both are category-1 references, so a SWAP immediately before the
     * exact super-constructor invocation leaves uninitialized {@code this}
     * untouched at the bottom of the operand stack.
     */
    private static int rewriteLegacySignBlockConstruction(ClassNode node) {
        final String legacy = "(Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;"
                + "Lnet/minecraft/world/level/block/state/properties/WoodType;)V";
        final String modern = "(Lnet/minecraft/world/level/block/state/properties/WoodType;"
                + "Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)V";
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKESPECIAL
                        || !call.name.equals("<init>")
                        || !call.desc.equals(legacy)
                        || !(call.owner.equals("net/minecraft/world/level/block/StandingSignBlock")
                        || call.owner.equals("net/minecraft/world/level/block/WallSignBlock"))) continue;
                method.instructions.insertBefore(call,
                        new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.SWAP));
                call.desc = modern;
                rewrites++;
            }
        }
        return rewrites;
    }

    /**
     * WoodButtonBlock was folded into ButtonBlock. The old subclass constructor
     * is exactly the modern oak button configuration: arrow-sensitive with a
     * thirty-tick press duration.
     */
    private static int rewriteLegacyWoodButtonConstruction(ClassNode node) {
        int rewrites = 0;
        final String legacyOwner = "net/minecraft/world/level/block/WoodButtonBlock";
        final String properties = "Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;";
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKESPECIAL
                        || !call.owner.equals(legacyOwner)
                        || !call.name.equals("<init>")
                        || !call.desc.equals("(" + properties + ")V")) continue;

                var adaptation = new org.objectweb.asm.tree.InsnList();
                // this, properties -> this, BlockSetType.OAK, 30, properties
                adaptation.add(new FieldInsnNode(org.objectweb.asm.Opcodes.GETSTATIC,
                        "net/minecraft/world/level/block/state/properties/BlockSetType", "OAK",
                        "Lnet/minecraft/world/level/block/state/properties/BlockSetType;"));
                adaptation.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.SWAP));
                adaptation.add(new org.objectweb.asm.tree.IntInsnNode(org.objectweb.asm.Opcodes.BIPUSH, 30));
                adaptation.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.SWAP));
                method.instructions.insertBefore(call, adaptation);
                call.owner = "net/minecraft/world/level/block/ButtonBlock";
                call.desc = "(Lnet/minecraft/world/level/block/state/properties/BlockSetType;I" + properties + ")V";
                rewrites++;
            }
        }
        return rewrites;
    }

    /**
     * DirectionProperty was folded into the generic EnumProperty. Keep the
     * legacy factories behind a small bridge because the collection overload
     * also narrowed from Collection to List in the target runtime.
     */
    private static int rewriteLegacyDirectionPropertyFactories(ClassNode node) {
        int rewrites = 0;
        final String owner = "net/minecraft/world/level/block/state/properties/DirectionProperty";
        final String predicateDesc = "(Ljava/lang/String;Ljava/util/function/Predicate;)L" + owner + ";";
        final String collectionDesc = "(Ljava/lang/String;Ljava/util/Collection;)L" + owner + ";";
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKESTATIC
                        || !call.owner.equals(owner)) continue;
                if (call.desc.equals(predicateDesc)) {
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192DirectionPropertyBridge";
                    call.name = "create";
                    call.desc = "(Ljava/lang/String;Ljava/util/function/Predicate;)"
                            + "Lnet/minecraft/world/level/block/state/properties/EnumProperty;";
                    call.itf = false;
                    rewrites++;
                } else if (call.desc.equals(collectionDesc)) {
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192DirectionPropertyBridge";
                    call.name = "create";
                    call.desc = "(Ljava/lang/String;Ljava/util/Collection;)"
                            + "Lnet/minecraft/world/level/block/state/properties/EnumProperty;";
                    call.itf = false;
                    rewrites++;
                }
            }
        }
        return rewrites;
    }

    /**
     * Door and trapdoor constructors replaced their implicit Material-based
     * behavior with an explicit BlockSetType. Recover that semantic input from
     * the same Properties object created by the legacy Material adapter.
     */
    private static int rewriteLegacyBlockSetTypeConstruction(ClassNode node) {
        int rewrites = 0;
        final String properties = "Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;";
        final String blockSetType = "Lnet/minecraft/world/level/block/state/properties/BlockSetType;";
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKESPECIAL
                        || !call.name.equals("<init>")
                        || !call.desc.equals("(" + properties + ")V")
                        || !(call.owner.equals("net/minecraft/world/level/block/DoorBlock")
                            || call.owner.equals("net/minecraft/world/level/block/TrapDoorBlock"))) continue;
                var adaptation = new org.objectweb.asm.tree.InsnList();
                adaptation.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.DUP));
                adaptation.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC,
                        "com/fuckingdeveloper/lms/runtime/Forge1192VanillaConstructionBridge",
                        "legacyBlockSetType", "(" + properties + ")" + blockSetType, false));
                adaptation.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.SWAP));
                method.instructions.insertBefore(call, adaptation);
                call.desc = "(" + blockSetType + properties + ")V";
                rewrites++;
            }
        }
        return rewrites;
    }

    /** Migrate only the verified static farmland property, not the changed class ABI. */
    private static int rewriteLegacyFarmlandFields(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof FieldInsnNode field
                        && field.getOpcode() == org.objectweb.asm.Opcodes.GETSTATIC
                        && field.owner.equals("net/minecraft/world/level/block/FarmBlock")
                        && field.name.equals("f_53243_")
                        && field.desc.equals("Lnet/minecraft/world/level/block/state/properties/IntegerProperty;")) {
                    field.owner = "net/minecraft/world/level/block/FarmlandBlock";
                    field.name = "MOISTURE";
                    rewrites++;
                }
            }
        }
        return rewrites;
    }

    /**
     * The gameplay Material class was removed. Translate only the exact
     * legacy Material field -> Properties.of(Material) shape into a typed
     * semantic material consumed by the target construction adapter.
     */
    private static int rewriteLegacyMaterialBlockProperties(ClassNode node) {
        int rewrites = 0;
        final String material = "net/minecraft/world/level/material/Material";
        final String properties = "net/minecraft/world/level/block/state/BlockBehaviour$Properties";
        final String bridge = "com/fuckingdeveloper/lms/runtime/Forge1192VanillaConstructionBridge";
        final String legacyMaterial = bridge + "$LegacyMaterial";
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKESTATIC
                        || !call.owner.equals(properties)
                        || !(call.name.equals("m_60939_")
                            && call.desc.equals("(L" + material + ";)L" + properties + ";")
                            || call.name.equals("m_60944_")
                            && call.desc.equals("(L" + material + ";Lnet/minecraft/world/level/material/MaterialColor;)L"
                                    + properties + ";"))) continue;
                FieldInsnNode field = call.name.equals("m_60939_")
                        ? materialField(call.getPrevious(), material, 1)
                        : materialField(call.getPrevious(), material, 8);
                if (field == null) continue;
                String materialKind = legacyMaterialKind(field.name);
                if (materialKind == null) continue;
                field.owner = legacyMaterial;
                field.name = materialKind;
                field.desc = "L" + legacyMaterial + ";";
                call.owner = bridge;
                call.name = "legacyMaterialProperties";
                call.desc = call.desc.startsWith("(L" + material + ";L")
                        ? "(L" + legacyMaterial + ";Lnet/minecraft/world/level/material/MaterialColor;)L"
                            + properties + ";"
                        : "(L" + legacyMaterial + ";)L" + properties + ";";
                call.itf = false;
                rewrites++;
            }
        }
        return rewrites;
    }

    private static FieldInsnNode materialField(
            org.objectweb.asm.tree.AbstractInsnNode start, String owner, int maxRealInstructions) {
        FieldInsnNode match = null;
        int inspected = 0;
        for (var cursor = start; cursor != null && inspected < maxRealInstructions; cursor = cursor.getPrevious()) {
            if (cursor.getOpcode() < 0) continue;
            inspected++;
            if (cursor instanceof FieldInsnNode field
                    && field.getOpcode() == org.objectweb.asm.Opcodes.GETSTATIC
                    && field.owner.equals(owner)
                    && field.desc.equals("L" + owner + ";")) {
                if (match != null) return null;
                match = field;
            }
        }
        return match;
    }

    private static String legacyMaterialKind(String fieldName) {
        return switch (fieldName) {
            case "f_76296_" -> "AIR";
            case "f_76299_" -> "CLOTH_DECORATION";
            case "f_76300_" -> "PLANT";
            case "f_76310_" -> "DECORATION";
            case "f_76272_" -> "WOOL";
            case "f_76273_" -> "EXPLOSIVE";
            case "f_76275_" -> "GLASS";
            case "f_76278_" -> "STONE";
            case "f_76279_" -> "METAL";
            case "f_76283_" -> "PISTON";
            case "f_76287_" -> "CAKE";
            case "f_76317_" -> "SAND";
            case "f_76320_" -> "WOOD";
            default -> null;
        };
    }

    /**
     * MaterialColor was replaced by MapColor. Adapt source static constants
     * only when the same Mojang field identity exists on that explicit target
     * replacement, plus the exact legacy Properties.color invocation. Keeping
     * the two proofs independent also covers ordinary control-flow joins where
     * several constants feed one color call.
     */
    private static int rewriteLegacyMaterialColorProperties(ClassNode node) {
        if (SRG_RUNTIME_RESOLVER.isEmpty()) return 0;
        int rewrites = 0;
        final String sourceColor = "net/minecraft/world/level/material/MaterialColor";
        final String targetColor = "net/minecraft/world/level/material/MapColor";
        final String properties = "net/minecraft/world/level/block/state/BlockBehaviour$Properties";
        final String sourceDescriptor = "L" + sourceColor + ";";
        final String targetDescriptor = "L" + targetColor + ";";
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof FieldInsnNode field
                        && field.getOpcode() == org.objectweb.asm.Opcodes.GETSTATIC
                        && field.owner.equals(sourceColor)
                        && field.desc.equals(sourceDescriptor)) {
                    var resolved = SRG_RUNTIME_RESOLVER.get().resolveRelocatedField(
                            sourceColor, field.name, sourceDescriptor, targetColor, targetDescriptor);
                    if (resolved.resolved()) {
                        field.owner = resolved.owner();
                        field.name = resolved.name();
                        field.desc = resolved.descriptor();
                        rewrites++;
                    }
                } else if (insn instanceof MethodInsnNode call
                        && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals(properties)
                        && call.name.equals("m_155949_")
                        && call.desc.equals("(" + sourceDescriptor + ")L" + properties + ";")) {
                    call.name = "mapColor";
                    call.desc = "(" + targetDescriptor + ")L" + properties + ";";
                    rewrites++;
                }
            }
        }
        return rewrites;
    }

    /**
     * The target preserves the old partial Properties.copy contract under the
     * explicit name ofLegacyCopy; ofFullCopy additionally copies behaviors the
     * 1.19.2 method did not copy.
     */
    private static int rewriteLegacyBlockPropertiesCopy(ClassNode node) {
        int rewrites = 0;
        final String owner = "net/minecraft/world/level/block/state/BlockBehaviour$Properties";
        final String descriptor = "(Lnet/minecraft/world/level/block/state/BlockBehaviour;)L" + owner + ";";
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKESTATIC
                        || !call.owner.equals(owner)
                        || !call.name.equals("m_60926_")
                        || !call.desc.equals(descriptor)) continue;
                call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192VanillaConstructionBridge";
                call.name = "legacyBlockPropertiesCopy";
                rewrites++;
            }
        }
        return rewrites;
    }

    /**
     * Modern blocks require their registry key in Properties before the Block
     * superclass constructor runs. Adapt only constructors whose own bytecode
     * proves which String parameter becomes the legacy registry name.
     */
    private int rewriteLegacyNamedBlockConstruction(ClassNode node) {
        final String properties = "Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;";
        if (!isManagedSubtypeOf(node, "net/minecraft/world/level/block/Block")) return 0;
        int rewrites = 0;
        for (var method : node.methods) {
            if (!method.name.equals("<init>")) continue;
            RegistryNameEvidence registryName = legacyRegistryNameEvidence(node, method);
            if (registryName == null) continue;
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKESPECIAL
                        || !call.owner.equals(node.superName)
                        || !call.name.equals("<init>")
                        || Type.getReturnType(call.desc).getSort() != Type.VOID
                        || Type.getArgumentTypes(call.desc).length == 0) continue;
                Type[] arguments = Type.getArgumentTypes(call.desc);
                int propertiesIndex = -1;
                for (int i = 0; i < arguments.length; i++) {
                    if (!arguments[i].equals(Type.getType(properties))) continue;
                    if (propertiesIndex >= 0) {
                        propertiesIndex = -2;
                        break;
                    }
                    propertiesIndex = i;
                }
                if (propertiesIndex < 0) continue;
                var adaptation = new org.objectweb.asm.tree.InsnList();
                int[] trailingLocals = new int[arguments.length];
                for (int i = arguments.length - 1; i > propertiesIndex; i--) {
                    Type argument = arguments[i];
                    int local = method.maxLocals;
                    method.maxLocals += argument.getSize();
                    trailingLocals[i] = local;
                    adaptation.add(new org.objectweb.asm.tree.VarInsnNode(
                            argument.getOpcode(org.objectweb.asm.Opcodes.ISTORE), local));
                }
                appendRegistryNameLoad(adaptation, registryName);
                adaptation.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC,
                        "com/fuckingdeveloper/lms/runtime/Forge1192VanillaConstructionBridge",
                        "withBlockId", "(" + properties + "Ljava/lang/String;)" + properties, false));
                for (int i = propertiesIndex + 1; i < arguments.length; i++) {
                    Type argument = arguments[i];
                    adaptation.add(new org.objectweb.asm.tree.VarInsnNode(
                            argument.getOpcode(org.objectweb.asm.Opcodes.ILOAD), trailingLocals[i]));
                }
                method.instructions.insertBefore(call, adaptation);
                rewrites++;
            }
        }
        return rewrites;
    }

    /** Attach a bytecode-proven legacy item name before Item reads Properties.id. */
    private static int rewriteLegacyNamedItemConstruction(ClassNode node) {
        final String properties = "Lnet/minecraft/world/item/Item$Properties;";
        int rewrites = 0;
        for (var method : node.methods) {
            if (!method.name.equals("<init>")) continue;
            RegistryNameEvidence registryName = legacyRegistryNameEvidence(node, method);
            if (registryName == null) continue;
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKESPECIAL
                        || !call.owner.equals("net/minecraft/world/item/Item")
                        || !call.name.equals("<init>")
                        || !call.desc.equals("(" + properties + ")V")) continue;
                int propertiesLocal = method.maxLocals++;
                var adaptation = new org.objectweb.asm.tree.InsnList();
                adaptation.add(new org.objectweb.asm.tree.VarInsnNode(
                        org.objectweb.asm.Opcodes.ASTORE, propertiesLocal));
                adaptation.add(new org.objectweb.asm.tree.VarInsnNode(
                        org.objectweb.asm.Opcodes.ALOAD, propertiesLocal));
                appendRegistryNameLoad(adaptation, registryName);
                adaptation.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC,
                        "com/fuckingdeveloper/lms/runtime/Forge1192VanillaConstructionBridge",
                        "withItemId", "(" + properties + "Ljava/lang/String;)" + properties, false));
                method.instructions.insertBefore(call, adaptation);
                var association = new org.objectweb.asm.tree.InsnList();
                association.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD, 0));
                association.add(new org.objectweb.asm.tree.VarInsnNode(
                        org.objectweb.asm.Opcodes.ALOAD, propertiesLocal));
                association.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC,
                        "com/fuckingdeveloper/lms/runtime/Forge1192VanillaConstructionBridge",
                        "associateItemProperties",
                        "(Lnet/minecraft/world/item/Item;Lnet/minecraft/world/item/Item$Properties;)V", false));
                method.instructions.insert(call, association);
                rewrites++;
            }
        }
        return rewrites;
    }

    /** Restore legacy SRG-named overrides of Block#createBlockStateDefinition. */
    private int rewriteLegacyCableStateDefinitionOverride(ClassNode node) {
        if (!isManagedSubtypeOf(node, "net/minecraft/world/level/block/Block")) return 0;
        final String desc = "(Lnet/minecraft/world/level/block/state/StateDefinition$Builder;)V";
        for (var method : node.methods) {
            if (!method.name.equals("m_7926_") || !method.desc.equals(desc)) continue;
            var resolved = resolveCanonicalBlockMethod(method.name, method.desc);
            if (resolved == null
                    || !resolved.name().equals("createBlockStateDefinition")
                    || !resolved.descriptor().equals(desc)) continue;
            method.name = resolved.name();
            return 1;
        }
        return 0;
    }

    /**
     * Creative tabs are no longer an Item.Properties attribute. Preserve the
     * properties chain while leaving tab population to the modern tab event.
     */
    private static int rewriteLegacyCreativeTabAssignment(ClassNode node) {
        int rewrites = 0;
        final String owner = "net/minecraft/world/item/Item$Properties";
        final String desc = "(Lnet/minecraft/world/item/CreativeModeTab;)L" + owner + ";";
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        || !call.owner.equals(owner)
                        || !call.name.equals("m_41491_")
                        || !call.desc.equals(desc)) continue;
                call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192VanillaConstructionBridge";
                call.name = "recordLegacyCreativeTab";
                call.desc = "(L" + owner + ";Lnet/minecraft/world/item/CreativeModeTab;)L" + owner + ";";
                call.itf = false;
                rewrites++;
            }
        }
        return rewrites;
    }

    /** Preserve legacy tab-intent propagation between items without claiming population support. */
    private static int rewriteLegacyCreativeTabLookup(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        || !call.owner.equals("net/minecraft/world/item/Item")
                        || !call.name.equals("m_41471_")
                        || !call.desc.equals("()Lnet/minecraft/world/item/CreativeModeTab;")) continue;
                call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192VanillaConstructionBridge";
                call.name = "legacyCreativeTab";
                call.desc = "(Lnet/minecraft/world/item/Item;)Lnet/minecraft/world/item/CreativeModeTab;";
                call.itf = false;
                rewrites++;
            }
        }
        return rewrites;
    }

    /**
     * Preserve the 1.19.2 search-tab enumeration protocol used by recipe and
     * item caches. Legacy item overrides remain the authority for variants;
     * target Item/BlockItem base implementations are reconstructed explicitly.
     * This is distinct from populating Minecraft 26.3 creative tabs.
     */
    private static int rewriteLegacyCreativeTabEnumeration(ClassNode node) {
        int rewrites = 0;
        final String tab = "Lnet/minecraft/world/item/CreativeModeTab;";
        final String list = "Lnet/minecraft/core/NonNullList;";
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null;) {
                var next = insn.getNext();
                if (insn instanceof FieldInsnNode field
                        && field.getOpcode() == org.objectweb.asm.Opcodes.GETSTATIC
                        && field.owner.equals("net/minecraft/world/item/CreativeModeTab")
                        && field.name.equals("f_40754_")
                        && field.desc.equals(tab)) {
                    method.instructions.set(field, new MethodInsnNode(
                            org.objectweb.asm.Opcodes.INVOKESTATIC,
                            "com/fuckingdeveloper/lms/runtime/Forge1192CreativeTabBridge",
                            "searchTab", "()" + tab, false));
                    rewrites++;
                    insn = next;
                    continue;
                }
                if (!(insn instanceof MethodInsnNode call)) {
                    insn = next;
                    continue;
                }
                // The verifier owner of an inherited call may be any legacy
                // Item subtype (not only Item itself), especially for a
                // invokespecial super call. The exact SRG identity and source
                // descriptor establish this as Item#allowedIn.
                if (call.name.equals("m_220152_") && call.desc.equals("(" + tab + ")Z")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192CreativeTabBridge";
                    call.name = "allowedIn";
                    call.desc = "(Lnet/minecraft/world/item/Item;" + tab + ")Z";
                    call.itf = false;
                    rewrites++;
                    insn = next;
                    continue;
                }
                if (!call.name.equals("m_6787_") || !call.desc.equals("(" + tab + list + ")V")) {
                    insn = next;
                    continue;
                }
                boolean baseCall = call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESPECIAL;
                call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192CreativeTabBridge";
                call.name = baseCall ? "fillBaseItemCategory" : "fillItemCategory";
                call.desc = "(Lnet/minecraft/world/item/Item;" + tab + list + ")V";
                call.itf = false;
                rewrites++;
                insn = next;
            }
        }
        return rewrites;
    }

    /** Supply paired block-item ids before the modern Item constructor requires them. */
    private static int rewriteLegacyBlockItemConstruction(ClassNode node) {
        if (!declaresLegacyRegistryNameAccessor(node)) return 0;
        final String desc = "(Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/item/Item$Properties;)V";
        int rewrites = 0;
        for (var method : node.methods) {
            if (!method.name.equals("<init>")) continue;
            RegistryNameEvidence namedItem = legacyRegistryNameEvidence(node, method);
            BlockItemRegistryLookupEvidence registryLookup = legacyBlockItemRegistryLookupEvidence(node, method);
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKESPECIAL
                        || !call.owner.equals("net/minecraft/world/item/BlockItem")
                        || !call.name.equals("<init>")
                        || !call.desc.equals(desc)) continue;
                int propertiesLocal = method.maxLocals++;
                int blockLocal = method.maxLocals++;
                var adaptation = new org.objectweb.asm.tree.InsnList();
                adaptation.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ASTORE, propertiesLocal));
                adaptation.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.DUP));
                adaptation.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ASTORE, blockLocal));
                if (namedItem != null) {
                    adaptation.add(new org.objectweb.asm.tree.VarInsnNode(
                            org.objectweb.asm.Opcodes.ALOAD, propertiesLocal));
                    appendRegistryNameLoad(adaptation, namedItem);
                    adaptation.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC,
                            "com/fuckingdeveloper/lms/runtime/Forge1192VanillaConstructionBridge",
                            "withItemId",
                            "(Lnet/minecraft/world/item/Item$Properties;Ljava/lang/String;)"
                                    + "Lnet/minecraft/world/item/Item$Properties;", false));
                } else if (registryLookup != null) {
                    adaptation.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.DUP));
                    adaptation.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC,
                            registryLookup.owner(), registryLookup.name(), registryLookup.desc(), false));
                    adaptation.add(new org.objectweb.asm.tree.VarInsnNode(
                            org.objectweb.asm.Opcodes.ALOAD, propertiesLocal));
                    adaptation.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC,
                            "com/fuckingdeveloper/lms/runtime/Forge1192VanillaConstructionBridge",
                            "withBlockItemId",
                            "(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/world/item/Item$Properties;)"
                                    + "Lnet/minecraft/world/item/Item$Properties;", false));
                } else {
                    adaptation.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.DUP));
                    adaptation.add(new org.objectweb.asm.tree.VarInsnNode(
                            org.objectweb.asm.Opcodes.ALOAD, propertiesLocal));
                    adaptation.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC,
                            "com/fuckingdeveloper/lms/runtime/Forge1192VanillaConstructionBridge",
                            "withBlockItemId",
                            "(Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/item/Item$Properties;)"
                                    + "Lnet/minecraft/world/item/Item$Properties;", false));
                }
                method.instructions.insertBefore(call, adaptation);
                var association = new org.objectweb.asm.tree.InsnList();
                if (!declaresLegacyBlockItemMapOverride(node)) {
                    association.add(new org.objectweb.asm.tree.VarInsnNode(
                            org.objectweb.asm.Opcodes.ALOAD, blockLocal));
                    association.add(new org.objectweb.asm.tree.VarInsnNode(
                            org.objectweb.asm.Opcodes.ALOAD, 0));
                    association.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC,
                            "com/fuckingdeveloper/lms/runtime/Forge1192VanillaConstructionBridge",
                            "associateBlockItem",
                            "(Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/item/Item;)V", false));
                }
                association.add(new org.objectweb.asm.tree.VarInsnNode(
                        org.objectweb.asm.Opcodes.ALOAD, 0));
                association.add(new org.objectweb.asm.tree.VarInsnNode(
                        org.objectweb.asm.Opcodes.ALOAD, propertiesLocal));
                association.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC,
                        "com/fuckingdeveloper/lms/runtime/Forge1192VanillaConstructionBridge",
                        "associateItemProperties",
                        "(Lnet/minecraft/world/item/Item;Lnet/minecraft/world/item/Item$Properties;)V", false));
                method.instructions.insert(call, association);
                rewrites++;
            }
        }
        return rewrites;
    }

    private static boolean declaresLegacyBlockItemMapOverride(ClassNode node) {
        return node.methods.stream().anyMatch(method -> method.name.equals("m_6192_")
                && method.desc.equals("(Ljava/util/Map;Lnet/minecraft/world/item/Item;)V")
                && (method.access & org.objectweb.asm.Opcodes.ACC_STATIC) == 0);
    }

    /**
     * Prove that a legacy BlockItem derives its registry name from the block
     * argument after the superclass call. The lookup can then be evaluated
     * before super without touching uninitialized {@code this}.
     */
    private record BlockItemRegistryLookupEvidence(String owner, String name, String desc) {}

    private static BlockItemRegistryLookupEvidence legacyBlockItemRegistryLookupEvidence(
            ClassNode node, org.objectweb.asm.tree.MethodNode constructor) {
        String registryField = null;
        for (var method : node.methods) {
            if (!method.name.equals("getRegistryName")
                    || !method.desc.equals("()Lnet/minecraft/resources/ResourceLocation;")) continue;
            var code = executableInstructions(method);
            if (code.size() != 3
                    || code.get(0).getOpcode() != org.objectweb.asm.Opcodes.ALOAD
                    || ((org.objectweb.asm.tree.VarInsnNode) code.get(0)).var != 0
                    || !(code.get(1) instanceof FieldInsnNode field)
                    || field.getOpcode() != org.objectweb.asm.Opcodes.GETFIELD
                    || !field.owner.equals(node.name)
                    || !field.desc.equals("Lnet/minecraft/resources/ResourceLocation;")
                    || code.get(2).getOpcode() != org.objectweb.asm.Opcodes.ARETURN) continue;
            registryField = field.name;
        }
        if (registryField == null) return null;

        BlockItemRegistryLookupEvidence match = null;
        for (var insn = constructor.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (!(insn instanceof MethodInsnNode lookup)
                    || lookup.getOpcode() != org.objectweb.asm.Opcodes.INVOKESTATIC
                    || !lookup.desc.equals("(Lnet/minecraft/world/level/block/Block;)"
                            + "Lnet/minecraft/resources/ResourceLocation;")) continue;
            var blockLoad = previousExecutable(lookup);
            var receiverLoad = blockLoad == null ? null : previousExecutable(blockLoad);
            var store = nextExecutable(lookup);
            if (!(blockLoad instanceof org.objectweb.asm.tree.VarInsnNode block)
                    || block.getOpcode() != org.objectweb.asm.Opcodes.ALOAD
                    || block.var != 1
                    || !(receiverLoad instanceof org.objectweb.asm.tree.VarInsnNode receiver)
                    || receiver.getOpcode() != org.objectweb.asm.Opcodes.ALOAD
                    || receiver.var != 0
                    || !(store instanceof FieldInsnNode field)
                    || field.getOpcode() != org.objectweb.asm.Opcodes.PUTFIELD
                    || !field.owner.equals(node.name)
                    || !field.name.equals(registryField)
                    || !field.desc.equals("Lnet/minecraft/resources/ResourceLocation;")) continue;
            var evidence = new BlockItemRegistryLookupEvidence(lookup.owner, lookup.name, lookup.desc);
            if (match != null && !match.equals(evidence)) return null;
            match = evidence;
        }
        return match;
    }

    /** Attach an explicit legacy registry name before a removed tool superclass runs. */
    private static int rewriteLegacyNamedSimpleToolConstruction(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            if (!method.name.equals("<init>")) continue;
            RegistryNameEvidence registryName = legacyRegistryNameEvidence(node, method);
            if (registryName == null) continue;
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKESPECIAL
                        || !call.name.equals("<init>")
                        || !isLegacyNamedItemConstructor(call.owner, call.desc)) continue;
                int propertiesLocal = method.maxLocals++;
                var adaptation = new org.objectweb.asm.tree.InsnList();
                adaptation.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ASTORE, propertiesLocal));
                adaptation.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD, propertiesLocal));
                appendRegistryNameLoad(adaptation, registryName);
                adaptation.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC,
                        "com/fuckingdeveloper/lms/runtime/Forge1192VanillaConstructionBridge",
                        "withItemId",
                        "(Lnet/minecraft/world/item/Item$Properties;Ljava/lang/String;)"
                                + "Lnet/minecraft/world/item/Item$Properties;", false));
                method.instructions.insertBefore(call, adaptation);
                var association = new org.objectweb.asm.tree.InsnList();
                association.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD, 0));
                association.add(new org.objectweb.asm.tree.VarInsnNode(
                        org.objectweb.asm.Opcodes.ALOAD, propertiesLocal));
                association.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC,
                        "com/fuckingdeveloper/lms/runtime/Forge1192VanillaConstructionBridge",
                        "associateItemProperties",
                        "(Lnet/minecraft/world/item/Item;Lnet/minecraft/world/item/Item$Properties;)V", false));
                method.instructions.insert(call, association);
                rewrites++;
            }
        }
        return rewrites;
    }

    private static boolean isLegacyNamedItemConstructor(String owner, String desc) {
        return switch (owner) {
            case "net/minecraft/world/item/AxeItem", "net/minecraft/world/item/ShovelItem" ->
                    desc.equals("(Lnet/minecraft/world/item/Tier;FFLnet/minecraft/world/item/Item$Properties;)V");
            case "net/minecraft/world/item/HoeItem", "net/minecraft/world/item/PickaxeItem",
                 "net/minecraft/world/item/SwordItem" ->
                    desc.equals("(Lnet/minecraft/world/item/Tier;IFLnet/minecraft/world/item/Item$Properties;)V");
            case "net/minecraft/world/item/ArmorItem" ->
                    desc.equals("(Lnet/minecraft/world/item/ArmorMaterial;"
                            + "Lnet/minecraft/world/entity/EquipmentSlot;"
                            + "Lnet/minecraft/world/item/Item$Properties;)V");
            case "net/minecraft/world/item/ShieldItem" ->
                    desc.equals("(Lnet/minecraft/world/item/Item$Properties;)V");
            default -> false;
        };
    }

    /**
     * Locate a String parameter or literal that the legacy constructor itself
     * proves is its registry name by passing it through Forge
     * GameData#checkPrefix and storing the resulting ResourceLocation on the
     * constructed object.
     */
    private record RegistryNameEvidence(int local, boolean lowercase, String literal) {}

    private static RegistryNameEvidence legacyRegistryNameEvidence(
            ClassNode node, org.objectweb.asm.tree.MethodNode method) {
        RegistryNameEvidence match = null;
        for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (!(insn instanceof MethodInsnNode call)
                    || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKESTATIC
                    || !call.owner.equals("net/minecraftforge/registries/GameData")
                    || !call.name.equals("checkPrefix")
                    || !call.desc.equals("(Ljava/lang/String;Z)Lnet/minecraft/resources/ResourceLocation;")) continue;
            var warn = previousExecutable(call);
            var producer = warn == null ? null : previousExecutable(warn);
            var store = nextExecutable(call);
            boolean lowercase = producer instanceof MethodInsnNode normalization
                    && normalization.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                    && normalization.owner.equals("java/lang/String")
                    && normalization.name.equals("toLowerCase")
                    && normalization.desc.equals("()Ljava/lang/String;");
            var name = lowercase ? previousExecutable(producer) : producer;
            if (warn == null || warn.getOpcode() != org.objectweb.asm.Opcodes.ICONST_0
                    || !(store instanceof FieldInsnNode field)
                    || store.getOpcode() != org.objectweb.asm.Opcodes.PUTFIELD
                    || !field.owner.equals(node.name)
                    || !field.desc.equals("Lnet/minecraft/resources/ResourceLocation;")) continue;
            RegistryNameEvidence evidence;
            if (name instanceof org.objectweb.asm.tree.VarInsnNode load
                    && load.getOpcode() == org.objectweb.asm.Opcodes.ALOAD
                    && isStringArgumentLocal(method, load.var)) {
                evidence = new RegistryNameEvidence(load.var, lowercase, null);
            } else if (name instanceof LdcInsnNode constant && constant.cst instanceof String literal) {
                evidence = new RegistryNameEvidence(-1, lowercase, literal);
            } else {
                continue;
            }
            if (match != null && !match.equals(evidence)) return null;
            match = evidence;
        }
        return match != null ? match : literalRegistryNameEvidence(node);
    }

    /** Exact getRegistryName() shape returning new ResourceLocation(ns, path). */
    private static RegistryNameEvidence literalRegistryNameEvidence(ClassNode node) {
        RegistryNameEvidence match = null;
        for (var method : node.methods) {
            if (!method.name.equals("getRegistryName")
                    || !method.desc.equals("()Lnet/minecraft/resources/ResourceLocation;")) continue;
            java.util.ArrayList<org.objectweb.asm.tree.AbstractInsnNode> code = new java.util.ArrayList<>();
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn.getOpcode() >= 0) code.add(insn);
            }
            if (code.size() != 6
                    || !(code.get(0) instanceof TypeInsnNode allocation)
                    || allocation.getOpcode() != org.objectweb.asm.Opcodes.NEW
                    || !allocation.desc.equals("net/minecraft/resources/ResourceLocation")
                    || code.get(1).getOpcode() != org.objectweb.asm.Opcodes.DUP
                    || !(code.get(2) instanceof LdcInsnNode namespace) || !(namespace.cst instanceof String ns)
                    || !(code.get(3) instanceof LdcInsnNode pathValue) || !(pathValue.cst instanceof String path)
                    || !(code.get(4) instanceof MethodInsnNode constructor)
                    || constructor.getOpcode() != org.objectweb.asm.Opcodes.INVOKESPECIAL
                    || !constructor.owner.equals("net/minecraft/resources/ResourceLocation")
                    || !constructor.name.equals("<init>")
                    || !constructor.desc.equals("(Ljava/lang/String;Ljava/lang/String;)V")
                    || code.get(5).getOpcode() != org.objectweb.asm.Opcodes.ARETURN) continue;
            RegistryNameEvidence evidence = new RegistryNameEvidence(-1, false, ns + ":" + path);
            if (match != null && !match.equals(evidence)) return null;
            match = evidence;
        }
        return match;
    }

    private static void appendRegistryNameLoad(org.objectweb.asm.tree.InsnList instructions,
                                               RegistryNameEvidence evidence) {
        if (evidence.literal() != null) {
            instructions.add(new LdcInsnNode(evidence.literal()));
        } else {
            instructions.add(new org.objectweb.asm.tree.VarInsnNode(
                    org.objectweb.asm.Opcodes.ALOAD, evidence.local()));
        }
        if (evidence.lowercase()) {
            instructions.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKEVIRTUAL,
                    "java/lang/String", "toLowerCase", "()Ljava/lang/String;", false));
        }
    }

    private static boolean declaresLegacyRegistryNameAccessor(ClassNode node) {
        return node.methods.stream().anyMatch(method -> method.name.equals("getRegistryName")
                && method.desc.equals("()Lnet/minecraft/resources/ResourceLocation;")
                && (method.access & org.objectweb.asm.Opcodes.ACC_STATIC) == 0);
    }

    private static boolean isStringArgumentLocal(org.objectweb.asm.tree.MethodNode method, int local) {
        int cursor = (method.access & org.objectweb.asm.Opcodes.ACC_STATIC) == 0 ? 1 : 0;
        for (Type argument : Type.getArgumentTypes(method.desc)) {
            if (cursor == local) return argument.equals(Type.getType(String.class));
            cursor += argument.getSize();
        }
        return false;
    }

    private static java.util.List<org.objectweb.asm.tree.AbstractInsnNode> executableInstructions(
            org.objectweb.asm.tree.MethodNode method) {
        java.util.ArrayList<org.objectweb.asm.tree.AbstractInsnNode> code = new java.util.ArrayList<>();
        for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn.getOpcode() >= 0) code.add(insn);
        }
        return code;
    }

    private static org.objectweb.asm.tree.AbstractInsnNode previousExecutable(
            org.objectweb.asm.tree.AbstractInsnNode instruction) {
        var cursor = instruction.getPrevious();
        while (cursor != null && cursor.getOpcode() < 0) cursor = cursor.getPrevious();
        return cursor;
    }

    private static org.objectweb.asm.tree.AbstractInsnNode nextExecutable(
            org.objectweb.asm.tree.AbstractInsnNode instruction) {
        var cursor = instruction.getNext();
        while (cursor != null && cursor.getOpcode() < 0) cursor = cursor.getNext();
        return cursor;
    }

    /**
     * VillagerProfession changed from a six-field record with a String name to
     * a seven-field record with a Component name and data-driven trade sets.
     * Legacy constructors had no trade-set mapping, so preserve the visible
     * literal name and supply an empty mapping without guessing a namespace.
     */
    private static int rewriteLegacyVillagerProfessionConstruction(ClassNode node) {
        int rewrites = 0;
        final String owner = "net/minecraft/world/entity/npc/VillagerProfession";
        final String desc = "(Ljava/lang/String;Ljava/util/function/Predicate;Ljava/util/function/Predicate;"
                + "Lcom/google/common/collect/ImmutableSet;Lcom/google/common/collect/ImmutableSet;"
                + "Lnet/minecraft/sounds/SoundEvent;)V";
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKESPECIAL
                        || !call.owner.equals(owner)
                        || !call.name.equals("<init>")
                        || !call.desc.equals(desc)) continue;

                TypeInsnNode allocation = null;
                org.objectweb.asm.tree.AbstractInsnNode duplicate = null;
                for (var candidate = call.getPrevious(); candidate != null; candidate = candidate.getPrevious()) {
                    if (candidate instanceof TypeInsnNode type
                            && type.getOpcode() == org.objectweb.asm.Opcodes.NEW
                            && type.desc.equals(owner)) {
                        var after = candidate.getNext();
                        if (after != null && after.getOpcode() == org.objectweb.asm.Opcodes.DUP) {
                            allocation = type;
                            duplicate = after;
                        }
                        break;
                    }
                }
                if (allocation == null) {
                    throw new IllegalStateException("Cannot pair legacy VillagerProfession allocation with constructor: "
                            + node.name + "#" + method.name + method.desc);
                }
                method.instructions.remove(allocation);
                method.instructions.remove(duplicate);
                call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192VillagerBridge";
                call.name = "createProfession";
                call.desc = desc.substring(0, desc.length() - 1)
                        + "Lnet/minecraft/world/entity/npc/villager/VillagerProfession;";
                call.itf = false;
                rewrites++;
            }
        }
        return rewrites;
    }

    /**
     * 1.19.2 exposed the built-in profession values as static fields. 26.3
     * exposes ResourceKeys instead, so resolve the exact mapped key through
     * the active target registry rather than treating the key as the value.
     */
    private static int rewriteLegacyVillagerProfessionConstants(ClassNode node) {
        final String legacy = "net/minecraft/world/entity/npc/VillagerProfession";
        final String legacyDescriptor = "L" + legacy + ";";
        final java.util.Map<String, String> keys = java.util.Map.ofEntries(
                java.util.Map.entry("f_35585_", "NONE"),
                java.util.Map.entry("f_35586_", "ARMORER"),
                java.util.Map.entry("f_35587_", "BUTCHER"),
                java.util.Map.entry("f_35588_", "CARTOGRAPHER"),
                java.util.Map.entry("f_35589_", "CLERIC"),
                java.util.Map.entry("f_35590_", "FARMER"),
                java.util.Map.entry("f_35591_", "FISHERMAN"),
                java.util.Map.entry("f_35592_", "FLETCHER"),
                java.util.Map.entry("f_35593_", "LEATHERWORKER"),
                java.util.Map.entry("f_35594_", "LIBRARIAN"),
                java.util.Map.entry("f_35595_", "MASON"),
                java.util.Map.entry("f_35596_", "NITWIT"),
                java.util.Map.entry("f_35597_", "SHEPHERD"),
                java.util.Map.entry("f_35598_", "TOOLSMITH"),
                java.util.Map.entry("f_35599_", "WEAPONSMITH"));
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; ) {
                var next = insn.getNext();
                if (insn instanceof FieldInsnNode field
                        && field.getOpcode() == org.objectweb.asm.Opcodes.GETSTATIC
                        && field.owner.equals(legacy)
                        && field.desc.equals(legacyDescriptor)) {
                    String targetName = keys.get(field.name);
                    if (targetName != null) {
                        var replacement = new org.objectweb.asm.tree.InsnList();
                        replacement.add(new FieldInsnNode(org.objectweb.asm.Opcodes.GETSTATIC,
                                "net/minecraft/world/entity/npc/villager/VillagerProfession",
                                targetName, "Lnet/minecraft/resources/ResourceKey;"));
                        replacement.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC,
                                "com/fuckingdeveloper/lms/runtime/Forge1192VillagerBridge",
                                "resolveProfession",
                                "(Lnet/minecraft/resources/ResourceKey;)"
                                        + "Lnet/minecraft/world/entity/npc/villager/VillagerProfession;",
                                false));
                        method.instructions.insertBefore(field, replacement);
                        method.instructions.remove(field);
                        rewrites++;
                    }
                }
                insn = next;
            }
        }
        return rewrites;
    }

    private int rewriteLegacySavedDataFamily(ClassNode node) {
        final String legacyStorage = "net/minecraft/world/level/storage/DimensionDataStorage";
        final String targetStorage = "net/minecraft/world/level/storage/SavedDataStorage";
        final String savedData = "net/minecraft/world/level/saveddata/SavedData";
        final String tag = "Lnet/minecraft/nbt/CompoundTag;";
        final String legacySaveDescriptor = "(" + tag + ")" + tag;
        int rewrites = 0;

        var legacyWriter = node.methods.stream()
                .filter(method -> method.name.equals("m_7176_")
                        && method.desc.equals(legacySaveDescriptor)
                        && (method.access & org.objectweb.asm.Opcodes.ACC_STATIC) == 0)
                .findFirst().orElse(null);
        if (legacyWriter != null && isManagedSubtypeOf(node, savedData)) {
            final String legacyContract =
                    "com/fuckingdeveloper/lms/runtime/saveddata/LegacySavedData";
            if (!node.interfaces.contains(legacyContract)) {
                node.interfaces.add(legacyContract);
                var adapter = new org.objectweb.asm.tree.MethodNode(
                        org.objectweb.asm.Opcodes.ACC_PUBLIC
                                | org.objectweb.asm.Opcodes.ACC_SYNTHETIC,
                        "lms$saveLegacy", legacySaveDescriptor, null, null);
                adapter.instructions.add(new org.objectweb.asm.tree.VarInsnNode(
                        org.objectweb.asm.Opcodes.ALOAD, 0));
                adapter.instructions.add(new org.objectweb.asm.tree.VarInsnNode(
                        org.objectweb.asm.Opcodes.ALOAD, 1));
                adapter.instructions.add(new MethodInsnNode(
                        org.objectweb.asm.Opcodes.INVOKEVIRTUAL,
                        node.name, legacyWriter.name, legacyWriter.desc, false));
                adapter.instructions.add(new org.objectweb.asm.tree.InsnNode(
                        org.objectweb.asm.Opcodes.ARETURN));
                adapter.maxStack = 2;
                adapter.maxLocals = 2;
                node.methods.add(adapter);
                rewrites++;
            }
        }

        final String legacyComputeDescriptor = "(Ljava/util/function/Function;"
                + "Ljava/util/function/Supplier;Ljava/lang/String;)L" + savedData + ";";
        final String bridgeComputeDescriptor = "(L" + targetStorage + ";"
                + "Ljava/util/function/Function;Ljava/util/function/Supplier;"
                + "Ljava/lang/String;)L" + savedData + ";";
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)) continue;
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("net/minecraft/server/level/ServerLevel")
                        && call.name.equals("m_8895_")
                        && call.desc.equals("()L" + legacyStorage + ";")) {
                    call.name = "getDataStorage";
                    call.desc = "()L" + targetStorage + ";";
                    rewrites++;
                    continue;
                }
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals(legacyStorage)
                        && call.name.equals("m_164861_")
                        && call.desc.equals(legacyComputeDescriptor)) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/saveddata/Forge1192SavedDataBridge";
                    call.name = "computeIfAbsent";
                    call.desc = bridgeComputeDescriptor;
                    call.itf = false;
                    rewrites++;
                }
            }
        }
        return rewrites;
    }

    private int rewriteLegacyLootContextFamily(ClassNode node) {
        if (SRG_RUNTIME_RESOLVER.isEmpty()) return 0;
        final String params = "net/minecraft/world/level/storage/loot/parameters/LootContextParams";
        final String legacyKey =
                "Lnet/minecraft/world/level/storage/loot/parameters/LootContextParam;";
        final String targetKey = "Lnet/minecraft/util/context/ContextKey;";
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof FieldInsnNode field
                        && field.getOpcode() == org.objectweb.asm.Opcodes.GETSTATIC
                        && field.owner.equals(params)
                        && field.desc.equals(legacyKey)) {
                    String sourceName = SRG_RUNTIME_RESOLVER.get()
                            .canonicalFieldName(params, field.name, legacyKey).orElse(null);
                    String targetName = switch (sourceName == null ? "" : sourceName) {
                        case "THIS_ENTITY", "LAST_DAMAGE_PLAYER", "DAMAGE_SOURCE", "ORIGIN",
                             "BLOCK_STATE", "BLOCK_ENTITY", "TOOL", "EXPLOSION_RADIUS" -> sourceName;
                        case "KILLER_ENTITY" -> "ATTACKING_ENTITY";
                        case "DIRECT_KILLER_ENTITY" -> "DIRECT_ATTACKING_ENTITY";
                        default -> null;
                    };
                    if (targetName != null && VANILLA_RELOCATIONS.searchExactField(
                            params, targetName, targetKey).size() == 1) {
                        field.name = targetName;
                        field.desc = targetKey;
                        rewrites++;
                    }
                    continue;
                }
                if (!(insn instanceof MethodInsnNode call)) continue;
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("net/minecraft/world/level/storage/loot/LootContext")) {
                    if (call.name.equals("m_78936_")
                            && call.desc.equals("(" + legacyKey + ")Z")) {
                        call.name = "hasParameter";
                        call.desc = "(" + targetKey + ")Z";
                        rewrites++;
                        continue;
                    }
                    if (call.name.equals("m_78953_")
                            && call.desc.equals("(" + legacyKey + ")Ljava/lang/Object;")) {
                        call.name = "getOptional";
                        call.desc = "(" + targetKey + ")Ljava/lang/Object;";
                        rewrites++;
                        continue;
                    }
                }
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("net/minecraft/world/level/storage/loot/parameters/"
                                + "LootContextParam")
                        && call.name.equals("m_81284_")
                        && call.desc.equals("()Lnet/minecraft/resources/ResourceLocation;")) {
                    call.owner = "net/minecraft/util/context/ContextKey";
                    call.name = "name";
                    call.desc = "()Lnet/minecraft/resources/Identifier;";
                    rewrites++;
                }
            }
        }
        return rewrites;
    }

    private int rewriteLegacyRecipeViewFamily(ClassNode node) {
        final String recipe = "net/minecraft/world/item/crafting/Recipe";
        if (!isManagedSubtypeOf(node, recipe)) return 0;
        int rewrites = 0;
        for (var source : java.util.List.of(
                new String[] {"m_8043_", "()Lnet/minecraft/world/item/ItemStack;",
                        "com/fuckingdeveloper/lms/runtime/recipe/LegacyRecipeResult",
                        "lms$legacyResult"},
                new String[] {"m_7527_", "()Lnet/minecraft/core/NonNullList;",
                        "com/fuckingdeveloper/lms/runtime/recipe/LegacyRecipeIngredients",
                        "lms$legacyIngredients"})) {
            var implementation = node.methods.stream()
                    .filter(method -> method.name.equals(source[0])
                            && method.desc.equals(source[1])
                            && (method.access & org.objectweb.asm.Opcodes.ACC_STATIC) == 0)
                    .findFirst().orElse(null);
            if (implementation == null || node.interfaces.contains(source[2])) continue;
            node.interfaces.add(source[2]);
            var adapter = new org.objectweb.asm.tree.MethodNode(
                    org.objectweb.asm.Opcodes.ACC_PUBLIC | org.objectweb.asm.Opcodes.ACC_SYNTHETIC,
                    source[3], source[1], null, null);
            adapter.instructions.add(new org.objectweb.asm.tree.VarInsnNode(
                    org.objectweb.asm.Opcodes.ALOAD, 0));
            adapter.instructions.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKEVIRTUAL,
                    node.name, implementation.name, implementation.desc, false));
            adapter.instructions.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.ARETURN));
            adapter.maxStack = 1;
            adapter.maxLocals = 1;
            node.methods.add(adapter);
            rewrites++;
        }
        return rewrites;
    }

    private static void verifyNoRemovedForgeClientTypes(ClassNode node) {
        final String removed = "net/minecraftforge/client/gui/ModListScreen";
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof TypeInsnNode type && type.desc.equals(removed)) {
                    throw new IllegalStateException("Removed Forge client type escaped adaptation: "
                            + node.name + "#" + method.name + method.desc + " -> " + removed);
                }
                if (insn instanceof MethodInsnNode call && call.owner.equals(removed)) {
                    throw new IllegalStateException("Removed Forge client call escaped adaptation: "
                            + node.name + "#" + method.name + method.desc + " -> "
                            + call.owner + "#" + call.name + call.desc);
                }
                if (insn instanceof FieldInsnNode field && field.owner.equals(removed)) {
                    throw new IllegalStateException("Removed Forge client field escaped adaptation: "
                            + node.name + "#" + method.name + method.desc + " -> "
                            + field.owner + "#" + field.name);
                }
            }
        }
    }

    /**
     * Minecraft 1.19.2 exposed the block atlas identifier from InventoryMenu;
     * the same target identity is now owned by TextureAtlas.
     */
    private static int rewriteLegacyBlockAtlasIdentity(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof FieldInsnNode field)
                        || field.getOpcode() != org.objectweb.asm.Opcodes.GETSTATIC
                        || !field.owner.equals("net/minecraft/world/inventory/InventoryMenu")
                        || !field.name.equals("f_39692_")
                        || !field.desc.equals("Lnet/minecraft/resources/ResourceLocation;")) continue;
                field.owner = "net/minecraft/client/renderer/texture/TextureAtlas";
                field.name = "LOCATION_BLOCKS";
                rewrites++;
            }
        }
        return rewrites;
    }

    /**
     * Mojang's mutable 1.19.2 Vector3f was replaced by JOML. Besides moving
     * the type, JOML changed several mutators' return descriptors and
     * normalize's success result, while the axis-angle helpers disappeared.
     * Adapt only exact source SRG identities whose target semantics have been
     * verified; unrelated JOML operations remain untouched.
     */
    private static int rewriteLegacyVector3fFamily(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof FieldInsnNode field
                        && field.getOpcode() == org.objectweb.asm.Opcodes.GETSTATIC
                        && field.owner.equals("com/mojang/math/Vector3f")
                        && field.desc.equals("Lcom/mojang/math/Vector3f;")) {
                    String target = switch (field.name) {
                        case "f_122223_" -> "XP";
                        case "f_122225_" -> "YP";
                        case "f_122227_" -> "ZP";
                        default -> null;
                    };
                    if (target != null) {
                        field.owner = "com/fuckingdeveloper/lms/runtime/render/Forge1192Vector3fBridge";
                        field.name = target;
                        field.desc = "Lorg/joml/Vector3f;";
                        rewrites++;
                    }
                    continue;
                }
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        || !call.owner.equals("com/mojang/math/Vector3f")) continue;
                String target = null;
                String descriptor = null;
                if (call.desc.equals("()F")) {
                    target = switch (call.name) {
                        case "m_122239_" -> "x";
                        case "m_122260_" -> "y";
                        case "m_122269_" -> "z";
                        default -> null;
                    };
                } else if (call.name.equals("m_122261_") && call.desc.equals("(F)V")) {
                    target = "mul";
                    descriptor = "(Lorg/joml/Vector3f;F)V";
                } else if (call.name.equals("m_122272_") && call.desc.equals("(FFF)V")) {
                    target = "set";
                    descriptor = "(Lorg/joml/Vector3f;FFF)V";
                } else if (call.name.equals("m_122278_") && call.desc.equals("()Z")) {
                    target = "normalize";
                    descriptor = "(Lorg/joml/Vector3f;)Z";
                } else if (call.name.equals("m_122270_")
                        && call.desc.equals("(F)Lcom/mojang/math/Quaternion;")) {
                    target = "rotation";
                    descriptor = "(Lorg/joml/Vector3f;F)Lorg/joml/Quaternionf;";
                } else if (call.name.equals("m_122240_")
                        && call.desc.equals("(F)Lcom/mojang/math/Quaternion;")) {
                    target = "rotationDegrees";
                    descriptor = "(Lorg/joml/Vector3f;F)Lorg/joml/Quaternionf;";
                }
                if (target == null) continue;
                if (descriptor == null) {
                    call.name = target;
                } else {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/render/Forge1192Vector3fBridge";
                    call.name = target;
                    call.desc = descriptor;
                    call.itf = false;
                }
                rewrites++;
            }
        }
        return rewrites;
    }

    /**
     * Minecraft 1.19.2's concrete mutable Quaternion became JOML's
     * Quaternionf/Quaternionfc contract. Adapt the exact source operations
     * whose calling convention or return descriptor changed before applying
     * the explicit type relocation. The Euler factory preserves Mojang's
     * verified X/Y/Z multiplication order and avoids leaving an invalid
     * NEW/DUP sequence behind.
     */
    private static int rewriteLegacyQuaternionFamily(ClassNode node) {
        final String legacy = "com/mojang/math/Quaternion";
        final String concrete = "org/joml/Quaternionf";
        final String readOnly = "org/joml/Quaternionfc";
        final String bridge =
                "com/fuckingdeveloper/lms/runtime/render/Forge1192QuaternionBridge";
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null;) {
                var next = insn.getNext();
                if (insn instanceof TypeInsnNode allocation
                        && allocation.getOpcode() == org.objectweb.asm.Opcodes.NEW
                        && allocation.desc.equals(legacy)) {
                    var duplicate = allocation.getNext();
                    if (duplicate == null
                            || duplicate.getOpcode() != org.objectweb.asm.Opcodes.DUP) {
                        throw new IllegalStateException("Unsupported legacy Quaternion allocation: "
                                + node.name + "#" + method.name);
                    }
                    MethodInsnNode init = null;
                    int nested = 0;
                    for (var cursor = duplicate.getNext(); cursor != null;
                            cursor = cursor.getNext()) {
                        if (cursor instanceof TypeInsnNode nestedAllocation
                                && nestedAllocation.getOpcode() == org.objectweb.asm.Opcodes.NEW) {
                            nested++;
                        } else if (cursor instanceof MethodInsnNode call
                                && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESPECIAL
                                && call.name.equals("<init>")) {
                            if (nested > 0) {
                                nested--;
                            } else if (call.owner.equals(legacy)) {
                                init = call;
                                break;
                            }
                        }
                    }
                    if (init == null) {
                        throw new IllegalStateException("Cannot match legacy Quaternion constructor: "
                                + node.name + "#" + method.name);
                    }
                    if (init.desc.equals("(FFFZ)V")) {
                        method.instructions.remove(allocation);
                        method.instructions.remove(duplicate);
                        var factory = new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC,
                                bridge, "eulerXYZ", "(FFFZ)L" + concrete + ";", false);
                        method.instructions.set(init, factory);
                        rewrites++;
                        insn = factory.getNext();
                        continue;
                    }
                    // JOML retains the exact x/y/z/w constructor. Other source
                    // constructors have different descriptors and remain an
                    // explicit fail-closed boundary until proven.
                    if (!init.desc.equals("(FFFF)V")) {
                        throw new IllegalStateException("Unadapted legacy Quaternion constructor: "
                                + node.name + "#" + method.name + init.desc);
                    }
                }
                if (insn instanceof MethodInsnNode call) {
                    if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESTATIC
                            && call.owner.equals(legacy)
                            && call.name.equals("m_175225_")
                            && call.desc.equals("(Lcom/mojang/math/Vector3f;)L"
                                    + legacy + ";")) {
                        call.owner = "com/fuckingdeveloper/lms/runtime/render/"
                                + "Forge1192ItemTransformBridge";
                        call.name = "quaternionFromXYZDegrees";
                        call.desc = "(Lorg/joml/Vector3f;)L" + concrete + ";";
                        rewrites++;
                    } else if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESPECIAL
                            && call.owner.equals("com/mojang/math/Transformation")
                            && call.name.equals("<init>")
                            && call.desc.equals("(Lcom/mojang/math/Vector3f;L" + legacy + ";"
                                    + "Lcom/mojang/math/Vector3f;L" + legacy + ";)V")) {
                        call.desc = "(Lorg/joml/Vector3fc;L" + readOnly + ";"
                                + "Lorg/joml/Vector3fc;L" + readOnly + ";)V";
                        rewrites++;
                    } else if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                            && call.owner.equals(legacy)
                            && call.name.equals("m_80148_")
                            && call.desc.equals("(L" + legacy + ";)V")) {
                        call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                        call.owner = bridge;
                        call.name = "multiply";
                        call.desc = "(L" + concrete + ";L" + readOnly + ";)V";
                        call.itf = false;
                        rewrites++;
                    } else if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                            && call.owner.equals("com/mojang/blaze3d/vertex/PoseStack")
                            && call.name.equals("m_85845_")
                            && call.desc.equals("(L" + legacy + ";)V")) {
                        call.name = "rotate";
                        call.desc = "(L" + readOnly + ";)V";
                        rewrites++;
                    } else if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                            && call.owner.equals("net/minecraft/core/Direction")
                            && call.name.equals("m_122406_")
                            && call.desc.equals("()L" + legacy + ";")) {
                        call.name = "getRotation";
                        call.desc = "()L" + concrete + ";";
                        rewrites++;
                    } else if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                            && call.owner.equals(
                                    "net/minecraft/client/renderer/entity/EntityRenderDispatcher")
                            && call.name.equals("m_114470_")
                            && call.desc.equals("()L" + legacy + ";")) {
                        call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                        call.owner = bridge;
                        call.name = "cameraOrientation";
                        call.desc = "(Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;)"
                                + "L" + concrete + ";";
                        call.itf = false;
                        rewrites++;
                    }
                }
                insn = next;
            }
        }
        return rewrites;
    }

    /**
     * Target 26.3 replaced the packed mutable-era BakedQuad class with a
     * geometry record. Preserve the exact 1.19.2 accessor and construction
     * ABI through an explicit representation bridge before relocating type
     * references to the target record.
     */
    private static int rewriteLegacyBakedQuadFamily(ClassNode node) {
        final String legacy = "net/minecraft/client/renderer/block/model/BakedQuad";
        final String target = "net/minecraft/client/resources/model/geometry/BakedQuad";
        final String bridge = "com/fuckingdeveloper/lms/runtime/render/Forge1192BakedQuadBridge";
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null;) {
                var next = insn.getNext();
                if (insn instanceof TypeInsnNode allocation
                        && allocation.getOpcode() == org.objectweb.asm.Opcodes.NEW
                        && allocation.desc.equals(legacy)) {
                    var dup = allocation.getNext();
                    if (dup == null || dup.getOpcode() != org.objectweb.asm.Opcodes.DUP) {
                        throw new IllegalStateException("Unsupported legacy BakedQuad allocation: "
                                + node.name + "#" + method.name);
                    }
                    MethodInsnNode init = null;
                    int nested = 0;
                    for (var cursor = dup.getNext(); cursor != null; cursor = cursor.getNext()) {
                        if (cursor instanceof TypeInsnNode nestedAllocation
                                && nestedAllocation.getOpcode() == org.objectweb.asm.Opcodes.NEW) {
                            nested++;
                        } else if (cursor instanceof MethodInsnNode call
                                && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESPECIAL
                                && call.name.equals("<init>")) {
                            if (nested > 0) {
                                nested--;
                            } else if (call.owner.equals(legacy)) {
                                init = call;
                                break;
                            }
                        }
                    }
                    if (init == null) {
                        throw new IllegalStateException("Cannot match legacy BakedQuad constructor: "
                                + node.name + "#" + method.name);
                    }
                    String factoryDesc = switch (init.desc) {
                        case "([IILnet/minecraft/core/Direction;"
                                + "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Z)V" ->
                                "([IILnet/minecraft/core/Direction;"
                                        + "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Z)L"
                                        + target + ";";
                        case "([IILnet/minecraft/core/Direction;"
                                + "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;ZZ)V" ->
                                "([IILnet/minecraft/core/Direction;"
                                        + "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;ZZ)L"
                                        + target + ";";
                        default -> throw new IllegalStateException(
                                "Unsupported legacy BakedQuad constructor: "
                                        + node.name + "#" + method.name + init.desc);
                    };
                    method.instructions.remove(allocation);
                    method.instructions.remove(dup);
                    MethodInsnNode factory = new MethodInsnNode(
                            org.objectweb.asm.Opcodes.INVOKESTATIC, bridge,
                            "create", factoryDesc, false);
                    method.instructions.set(init, factory);
                    rewrites++;
                    insn = factory.getNext();
                    continue;
                }
                if (insn instanceof MethodInsnNode call
                        && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals(legacy)) {
                    String bridgeName = switch (call.name + call.desc) {
                        case "m_173410_()Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;" -> "sprite";
                        case "m_111303_()[I" -> "vertices";
                        case "m_111304_()Z" -> "isTinted";
                        case "m_111305_()I" -> "tintIndex";
                        case "m_111306_()Lnet/minecraft/core/Direction;" -> "direction";
                        case "m_111307_()Z" -> "isShade";
                        case "m_111308_()Z" -> "hasAmbientOcclusion";
                        default -> null;
                    };
                    if (bridgeName != null) {
                        call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                        call.owner = bridge;
                        call.name = bridgeName;
                        call.desc = "(L" + target + ";)" + call.desc.substring(2);
                        call.itf = false;
                        rewrites++;
                    }
                }
                insn = next;
            }
        }
        return rewrites;
    }

    /**
     * Forge 1.19.2 exposed player renderer variants as the legacy string
     * identities "default" and "slim". Target 26.3 keys the same renderer
     * map by PlayerModelType, so preserve the source event ABI while routing
     * through the target's explicit legacy-name conversion.
     */
    private static int rewriteLegacyEntityRendererAddLayers(ClassNode node) {
        final String event = "net/minecraftforge/client/event/EntityRenderersEvent$AddLayers";
        final String targetEvent =
                "net/neoforged/neoforge/client/event/EntityRenderersEvent$AddLayers";
        final String bridge =
                "com/fuckingdeveloper/lms/runtime/render/Forge1192EntityRendererLayerBridge";
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        || !call.owner.equals(event)) continue;
                if (call.name.equals("getSkins")
                        && call.desc.equals("()Ljava/util/Set;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = bridge;
                    call.name = "getSkins";
                    call.desc = "(L" + targetEvent + ";)Ljava/util/Set;";
                    call.itf = false;
                    rewrites++;
                } else if (call.name.equals("getSkin")
                        && call.desc.equals("(Ljava/lang/String;)"
                                + "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = bridge;
                    call.name = "getSkin";
                    call.desc = "(L" + targetEvent + ";Ljava/lang/String;)"
                            + "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;";
                    call.itf = false;
                    rewrites++;
                }
            }
        }
        return rewrites;
    }

    /** Exact Forge 1.19.2 input identities retained by the SDL target API. */
    private static int rewriteLegacyInputConstantsFamily(ClassNode node) {
        final String input = "com/mojang/blaze3d/platform/InputConstants";
        final String key = input + "$Key";
        final String type = input + "$Type";
        int rewrites = 0;
        for (var method : node.methods) {
            java.util.Set<Integer> adaptedWindowLocals = adaptLegacyWindowHandleLocals(node, method);
            rewrites += adaptedWindowLocals.size();
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof FieldInsnNode field
                        && field.getOpcode() == org.objectweb.asm.Opcodes.GETSTATIC
                        && field.owner.equals(type)
                        && field.desc.equals("L" + type + ";")
                        && (field.name.equals("KEYSYM") || field.name.equals("SCANCODE"))) {
                    field.name = "KEYBOARD";
                    rewrites++;
                    continue;
                }
                if (!(insn instanceof MethodInsnNode call)) continue;
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESTATIC
                        && call.owner.equals(input)
                        && call.name.equals("m_84830_")
                        && call.desc.equals("(JI)Z")) {
                    MethodInsnNode windowHandle = previousLegacyWindowHandle(call);
                    if (windowHandle != null) {
                        method.instructions.remove(windowHandle);
                    } else if (!hasPreviousAdaptedWindowLoad(call, adaptedWindowLocals)) {
                        throw new IllegalStateException("Legacy keyboard query lacks its Window handle producer: "
                                + node.name + "#" + method.name);
                    }
                    call.owner = "com/fuckingdeveloper/lms/runtime/render/Forge1192InputBridge";
                    call.name = "isKeyDown";
                    call.desc = "(Lcom/mojang/blaze3d/platform/Window;I)Z";
                    rewrites++;
                    continue;
                }
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESTATIC
                        && call.owner.equals("org/lwjgl/glfw/GLFW")
                        && call.name.equals("glfwGetMouseButton")
                        && call.desc.equals("(JI)I")) {
                    MethodInsnNode windowHandle = previousLegacyWindowHandle(call);
                    if (windowHandle != null) {
                        method.instructions.remove(windowHandle);
                    } else if (!hasPreviousAdaptedWindowLoad(call, adaptedWindowLocals)) {
                        throw new IllegalStateException("Legacy mouse query lacks its Window handle producer: "
                                + node.name + "#" + method.name);
                    }
                    call.owner = "com/fuckingdeveloper/lms/runtime/render/Forge1192InputBridge";
                    call.name = "mouseButton";
                    call.desc = "(Lcom/mojang/blaze3d/platform/Window;I)I";
                    rewrites++;
                    continue;
                }
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals(key)) {
                    String name = switch (call.name + call.desc) {
                        case "m_84868_()Lcom/mojang/blaze3d/platform/InputConstants$Type;" -> "getType";
                        case "m_84873_()I" -> "getValue";
                        case "m_84875_()Lnet/minecraft/network/chat/Component;" -> "getDisplayName";
                        default -> null;
                    };
                    if (name != null) {
                        call.name = name;
                        rewrites++;
                    }
                    continue;
                }
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals(type)
                        && call.name.equals("m_84895_")
                        && call.desc.equals("(I)L" + key + ";")) {
                    call.name = "getOrCreate";
                    rewrites++;
                }
            }
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof MethodInsnNode call
                        && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("com/mojang/blaze3d/platform/Window")
                        && call.name.equals("m_85439_")
                        && call.desc.equals("()J")) {
                    throw new IllegalStateException("Unadapted native GLFW window handle use: "
                            + node.name + "#" + method.name);
                }
            }
        }
        return rewrites;
    }

    private static java.util.Set<Integer> adaptLegacyWindowHandleLocals(
            ClassNode node, org.objectweb.asm.tree.MethodNode method) {
        java.util.Set<Integer> locals = new java.util.HashSet<>();
        for (var insn = method.instructions.getFirst(); insn != null;) {
            var next = insn.getNext();
            if (insn instanceof MethodInsnNode call
                    && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                    && call.owner.equals("com/mojang/blaze3d/platform/Window")
                    && call.name.equals("m_85439_")
                    && call.desc.equals("()J")) {
                var storeInsn = nextRealInstruction(call.getNext());
                if (!(storeInsn instanceof org.objectweb.asm.tree.VarInsnNode store)
                        || store.getOpcode() != org.objectweb.asm.Opcodes.LSTORE) {
                    insn = next;
                    continue;
                }
                locals.add(store.var);
                store.setOpcode(org.objectweb.asm.Opcodes.ASTORE);
                method.instructions.remove(call);
            }
            insn = next;
        }
        if (locals.isEmpty()) return locals;
        for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (!(insn instanceof org.objectweb.asm.tree.VarInsnNode load)
                    || load.getOpcode() != org.objectweb.asm.Opcodes.LLOAD
                    || !locals.contains(load.var)) continue;
            var consumer = nextLegacyInputConsumer(load);
            if (consumer == null) {
                throw new IllegalStateException("Native GLFW handle local escapes input adaptation: "
                        + node.name + "#" + method.name + " local=" + load.var);
            }
            load.setOpcode(org.objectweb.asm.Opcodes.ALOAD);
        }
        return locals;
    }

    private static MethodInsnNode nextLegacyInputConsumer(
            org.objectweb.asm.tree.AbstractInsnNode instruction) {
        int examined = 0;
        for (var cursor = instruction.getNext(); cursor != null && examined < 8;
                cursor = cursor.getNext()) {
            if (cursor.getOpcode() < 0) continue;
            examined++;
            if (cursor instanceof MethodInsnNode call
                    && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESTATIC
                    && ((call.owner.equals("com/mojang/blaze3d/platform/InputConstants")
                            && call.name.equals("m_84830_") && call.desc.equals("(JI)Z"))
                        || (call.owner.equals("org/lwjgl/glfw/GLFW")
                            && call.name.equals("glfwGetMouseButton") && call.desc.equals("(JI)I")))) {
                return call;
            }
            if (cursor instanceof org.objectweb.asm.tree.JumpInsnNode
                    || cursor instanceof org.objectweb.asm.tree.TableSwitchInsnNode
                    || cursor instanceof org.objectweb.asm.tree.LookupSwitchInsnNode) return null;
        }
        return null;
    }

    private static boolean hasPreviousAdaptedWindowLoad(
            MethodInsnNode consumer, java.util.Set<Integer> adaptedLocals) {
        int examined = 0;
        for (var cursor = consumer.getPrevious(); cursor != null && examined < 8;
                cursor = cursor.getPrevious()) {
            if (cursor.getOpcode() < 0) continue;
            examined++;
            if (cursor instanceof org.objectweb.asm.tree.VarInsnNode load
                    && load.getOpcode() == org.objectweb.asm.Opcodes.ALOAD
                    && adaptedLocals.contains(load.var)) return true;
            if (cursor instanceof org.objectweb.asm.tree.JumpInsnNode
                    || cursor instanceof org.objectweb.asm.tree.TableSwitchInsnNode
                    || cursor instanceof org.objectweb.asm.tree.LookupSwitchInsnNode) return false;
        }
        return false;
    }

    private static MethodInsnNode previousLegacyWindowHandle(MethodInsnNode consumer) {
        int examined = 0;
        for (var cursor = consumer.getPrevious(); cursor != null && examined < 12;
                cursor = cursor.getPrevious()) {
            if (cursor.getOpcode() < 0) continue;
            examined++;
            if (cursor instanceof MethodInsnNode call
                    && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                    && call.owner.equals("com/mojang/blaze3d/platform/Window")
                    && call.name.equals("m_85439_")
                    && call.desc.equals("()J")) return call;
            if (cursor instanceof org.objectweb.asm.tree.JumpInsnNode
                    || cursor instanceof org.objectweb.asm.tree.TableSwitchInsnNode
                    || cursor instanceof org.objectweb.asm.tree.LookupSwitchInsnNode) return null;
        }
        return null;
    }

    /**
     * Forge 1.19.2 exposed immediate render buffers from its block-highlight
     * event. Restrict the deferred-buffer adapter to methods whose exact ABI
     * identifies them as block-highlight handlers; MultiBufferSource is used
     * elsewhere and must not be globally relocated.
     */
    private static int rewriteLegacyBlockHighlightEvent(ClassNode node) {
        final String legacyEvent = "net/minecraftforge/client/event/RenderHighlightEvent$Block";
        final String legacyBuffers = "net/minecraft/client/renderer/MultiBufferSource";
        final String adaptedEvent =
                "com/fuckingdeveloper/lms/runtime/event/LegacyRenderHighlightBlockEvent";
        final String adaptedBuffers =
                "com/fuckingdeveloper/lms/runtime/render/LegacyMultiBufferSource";
        int rewrites = 0;
        for (var method : node.methods) {
            if (!method.desc.contains("L" + legacyEvent + ";")) continue;
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)) continue;
                if (call.owner.equals(legacyEvent)
                        && call.name.equals("getMultiBufferSource")
                        && call.desc.equals("()L" + legacyBuffers + ";")) {
                    call.owner = adaptedEvent;
                    call.desc = "()L" + adaptedBuffers + ";";
                    rewrites++;
                    continue;
                }
                if (call.owner.equals(legacyBuffers)
                        && call.name.equals("m_6299_")
                        && call.desc.equals("(Lnet/minecraft/client/renderer/RenderType;)"
                                + "Lcom/mojang/blaze3d/vertex/VertexConsumer;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKEVIRTUAL);
                    call.owner = adaptedBuffers;
                    call.itf = false;
                    rewrites++;
                }
            }
        }
        return rewrites;
    }

    private static int rewriteLegacyReloadListenerRegistration(ClassNode node) {
        final String legacyEvent = "net/minecraftforge/event/AddReloadListenerEvent";
        final String listener = "Lnet/minecraft/server/packs/resources/PreparableReloadListener;";
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        || !call.owner.equals(legacyEvent)
                        || !call.name.equals("addListener")
                        || !call.desc.equals("(" + listener + ")V")) continue;
                call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192EventBusBridge";
                call.name = "addReloadListener";
                call.desc = "(Lnet/neoforged/neoforge/event/AddServerReloadListenersEvent;"
                        + listener + ")V";
                call.itf = false;
                rewrites++;
            }
        }
        return rewrites;
    }

    /**
     * Forge 1.19.2 permitted direct client resource-manager mutation during
     * client setup. NeoForge 26.3 requires named listeners to be contributed
     * through AddClientReloadListenersEvent before it snapshots the manager.
     */
    private static int rewriteLegacyDirectClientReloadRegistration(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        || !call.owner.equals("net/minecraft/server/packs/resources/ReloadableResourceManager")
                        || !call.name.equals("m_7217_")
                        || !call.desc.equals("(Lnet/minecraft/server/packs/resources/PreparableReloadListener;)V")) {
                    continue;
                }
                call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192EventBusBridge";
                call.name = "addClientReloadListener";
                call.desc = "(Lnet/minecraft/server/packs/resources/ReloadableResourceManager;"
                        + "Lnet/minecraft/server/packs/resources/PreparableReloadListener;)V";
                call.itf = false;
                rewrites++;
            }
        }
        return rewrites;
    }

    /**
     * Forge 1.19.2 resource queries accepted a Predicate<ResourceLocation>.
     * Minecraft 26.3 gives the same query operations a dedicated Selector
     * functional interface. Preserve predicate behavior through an explicit
     * target adapter for both members of the query family.
     */
    private static int rewriteLegacyResourceQueries(ClassNode node) {
        final String manager = "net/minecraft/server/packs/resources/ResourceManager";
        final String provider = "net/minecraft/server/packs/resources/ResourceProvider";
        final String location = "Lnet/minecraft/resources/ResourceLocation;";
        final String predicate = "Ljava/util/function/Predicate;";
        final String bridge = "com/fuckingdeveloper/lms/runtime/Forge1192ResourceManagerBridge";
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKEINTERFACE) continue;
                if ((call.owner.equals(manager) || call.owner.equals(provider))
                        && call.desc.startsWith("(" + location + ")")) {
                    String targetName = switch (call.name + call.desc) {
                        case "m_213713_(" + location + ")Ljava/util/Optional;" -> "getResource";
                        case "m_215593_(" + location + ")Lnet/minecraft/server/packs/resources/Resource;" ->
                                "getResourceOrThrow";
                        case "m_215595_(" + location + ")Ljava/io/InputStream;" -> "open";
                        case "m_215597_(" + location + ")Ljava/io/BufferedReader;" -> "openAsReader";
                        default -> null;
                    };
                    if (targetName != null) {
                        call.name = targetName;
                        rewrites++;
                        continue;
                    }
                }
                if (!call.owner.equals(manager)) continue;
                String survivingName = switch (call.name + call.desc) {
                    case "m_7187_()Ljava/util/Set;" -> "getNamespaces";
                    case "m_213829_(" + location + ")Ljava/util/List;" -> "getResourceStack";
                    case "m_7536_()Ljava/util/stream/Stream;" -> "listPacks";
                    default -> null;
                };
                if (survivingName != null) {
                    call.name = survivingName;
                    rewrites++;
                    continue;
                }
                String bridgeName;
                String returnType;
                if (call.name.equals("m_214159_")
                        && call.desc.equals("(Ljava/lang/String;" + predicate + ")Ljava/util/Map;")) {
                    bridgeName = "listResources";
                    returnType = "Ljava/util/Map;";
                } else if (call.name.equals("m_214160_")
                        && call.desc.equals("(Ljava/lang/String;" + predicate + ")Ljava/util/Map;")) {
                    bridgeName = "listResourceStacks";
                    returnType = "Ljava/util/Map;";
                } else {
                    continue;
                }
                call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                call.owner = bridge;
                call.name = bridgeName;
                call.desc = "(L" + manager + ";Ljava/lang/String;" + predicate + ")" + returnType;
                call.itf = false;
                rewrites++;
            }
        }
        return rewrites;
    }

    /**
     * The source BlockModel JSON object became the target UnbakedModel model
     * graph while its old binary name was reused by an unrelated render-state
     * interface. Adapt the proven parsing/property subset to an opaque LMS
     * token so it can never be confused with the target BlockModel contract.
     */
    private static int rewriteLegacyBlockModelParsing(ClassNode node) {
        final String legacyModel = "net/minecraft/client/renderer/block/model/BlockModel";
        final String bridge =
                "com/fuckingdeveloper/lms/runtime/render/Forge1192UnbakedModelBridge";
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call) || !call.owner.equals(legacyModel)) continue;
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESTATIC
                        && call.name.equals("m_111461_")
                        && call.desc.equals("(Ljava/io/Reader;)L" + legacyModel + ";")) {
                    call.owner = bridge;
                    call.name = "parse";
                    call.desc = "(Ljava/io/Reader;)Ljava/lang/Object;";
                    call.itf = false;
                    rewrites++;
                    continue;
                }
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.name.equals("getParentLocation")
                        && call.desc.equals("()Lnet/minecraft/resources/ResourceLocation;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = bridge;
                    call.name = "parent";
                    call.desc = "(Ljava/lang/Object;)Lnet/minecraft/resources/ResourceLocation;";
                    call.itf = false;
                    rewrites++;
                    continue;
                }
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.name.equals("m_111491_")
                        && call.desc.equals("()Lnet/minecraft/client/renderer/block/model/ItemTransforms;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = bridge;
                    call.name = "transforms";
                    call.desc = "(Ljava/lang/Object;)"
                            + "Lnet/minecraft/client/renderer/block/model/ItemTransforms;";
                    call.itf = false;
                    rewrites++;
                    continue;
                }
                throw new IllegalStateException("Unsupported Forge 1.19.2 BlockModel operation: "
                        + node.name + "#" + method.name + method.desc + " -> "
                        + call.name + call.desc);
            }
        }
        return rewrites;
    }

    /** Preserve the Forge 1.19.2 item-display transform value contract. */
    private static int rewriteLegacyItemTransformFamily(ClassNode node) {
        final String transforms = "net/minecraft/client/renderer/block/model/ItemTransforms";
        final String transform = "net/minecraft/client/renderer/block/model/ItemTransform";
        final String context = transforms + "$TransformType";
        final String bridge =
                "com/fuckingdeveloper/lms/runtime/render/Forge1192ItemTransformBridge";
        final String transformArg = "L" + transform + ";";
        final String eightTransforms = transformArg.repeat(8);
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null;) {
                var next = insn.getNext();
                if (insn instanceof MethodInsnNode call) {
                    if (call.owner.equals(transforms)
                            && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                            && call.name.equals("m_111808_")
                            && call.desc.equals("(L" + context + ";)L" + transform + ";")) {
                        call.name = "getTransform";
                        rewrites++;
                    } else if (call.owner.equals(transforms)
                            && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                            && call.name.equals("m_111810_")
                            && call.desc.equals("(L" + context + ";)Z")) {
                        call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                        call.owner = bridge;
                        call.name = "hasTransform";
                        call.desc = "(L" + transforms + ";L" + context + ";)Z";
                        call.itf = false;
                        rewrites++;
                    } else if (call.owner.equals(transform)
                            && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                            && call.name.equals("m_111763_")
                            && call.desc.equals("(ZLcom/mojang/blaze3d/vertex/PoseStack;)V")) {
                        call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                        call.owner = bridge;
                        call.name = "apply";
                        call.desc = "(L" + transform
                                + ";ZLcom/mojang/blaze3d/vertex/PoseStack;)V";
                        call.itf = false;
                        rewrites++;
                    } else if (call.owner.equals(transforms)
                            && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESPECIAL
                            && call.name.equals("<init>")) {
                        boolean supportedConstructor = call.desc.equals(
                                "(" + eightTransforms
                                        + "Lcom/google/common/collect/ImmutableMap;)V")
                                || call.desc.equals("(" + eightTransforms + ")V")
                                || call.desc.equals("(L" + transforms + ";)V");
                        String factoryDescriptor = supportedConstructor
                                ? call.desc.substring(0, call.desc.length() - 1)
                                        + "L" + transforms + ";"
                                : null;
                        if (factoryDescriptor != null) {
                            TypeInsnNode allocation = null;
                            org.objectweb.asm.tree.AbstractInsnNode duplicate = null;
                            for (var candidate = call.getPrevious(); candidate != null;
                                 candidate = candidate.getPrevious()) {
                                if (!(candidate instanceof TypeInsnNode type)
                                        || type.getOpcode() != org.objectweb.asm.Opcodes.NEW
                                        || !type.desc.equals(transforms)) continue;
                                allocation = type;
                                duplicate = nextExecutable(type);
                                if (duplicate == null
                                        || duplicate.getOpcode() != org.objectweb.asm.Opcodes.DUP) {
                                    allocation = null;
                                    duplicate = null;
                                }
                                break;
                            }
                            if (allocation == null) {
                                throw new IllegalStateException(
                                        "Cannot pair legacy ItemTransforms allocation with constructor: "
                                                + node.name + "#" + method.name + method.desc);
                            }
                            method.instructions.remove(allocation);
                            method.instructions.remove(duplicate);
                            call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                            call.owner = bridge;
                            call.name = call.desc.equals("(L" + transforms + ";)V")
                                    ? "copy" : "create";
                            call.desc = factoryDescriptor;
                            call.itf = false;
                            rewrites++;
                        }
                    }
                } else if (insn instanceof FieldInsnNode field) {
                    if (field.getOpcode() == org.objectweb.asm.Opcodes.GETSTATIC
                            && field.owner.equals(transforms)
                            && field.name.equals("f_111786_")
                            && field.desc.equals("L" + transforms + ";")) {
                        field.name = "NO_TRANSFORMS";
                        rewrites++;
                    } else if (field.getOpcode() == org.objectweb.asm.Opcodes.GETSTATIC
                            && field.owner.equals(transform)
                            && field.name.equals("f_111754_")
                            && field.desc.equals("L" + transform + ";")) {
                        field.name = "NO_TRANSFORM";
                        rewrites++;
                    } else if (field.getOpcode() == org.objectweb.asm.Opcodes.GETFIELD
                            && field.owner.equals(transforms)
                            && field.desc.equals("L" + transform + ";")) {
                        String accessor = switch (field.name) {
                            case "f_111787_" -> "thirdPersonLeftHand";
                            case "f_111788_" -> "thirdPersonRightHand";
                            case "f_111789_" -> "firstPersonLeftHand";
                            case "f_111790_" -> "firstPersonRightHand";
                            case "f_111791_" -> "head";
                            case "f_111792_" -> "gui";
                            case "f_111793_" -> "ground";
                            case "f_111794_" -> "fixed";
                            default -> null;
                        };
                        if (accessor != null) {
                            method.instructions.set(field, new MethodInsnNode(
                                    org.objectweb.asm.Opcodes.INVOKEVIRTUAL,
                                    transforms, accessor, "()L" + transform + ";", false));
                            rewrites++;
                        }
                    } else if (field.getOpcode() == org.objectweb.asm.Opcodes.GETFIELD
                            && field.owner.equals(transform)
                            && field.desc.equals("Lcom/mojang/math/Vector3f;")) {
                        String accessor = switch (field.name) {
                            case "f_111755_" -> "rotation";
                            case "f_111756_" -> "translation";
                            case "f_111757_" -> "scale";
                            case "rightRotation" -> "rightRotation";
                            default -> null;
                        };
                        if (accessor != null) {
                            method.instructions.set(field, new MethodInsnNode(
                                    org.objectweb.asm.Opcodes.INVOKESTATIC, bridge, accessor,
                                    "(L" + transform + ";)Lcom/mojang/math/Vector3f;", false));
                            rewrites++;
                        }
                    }
                }
                insn = next;
            }
        }
        return rewrites;
    }

    /**
     * Retain the source baked-model registration context without pretending
     * that removed ItemRenderer/ItemModelShaper objects survive on the target.
     */
    private static int rewriteLegacyModelBakingPipeline(ClassNode node) {
        final String event =
                "Lnet/minecraftforge/client/event/ModelEvent$BakingCompleted;";
        final String bridge =
                "com/fuckingdeveloper/lms/runtime/render/Forge1192ModelBakingBridge";
        int rewrites = 0;
        for (var method : node.methods) {
            boolean modelCallback = method.desc.contains(event);
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)) continue;
                if (modelCallback
                        && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("net/minecraft/client/Minecraft")
                        && call.name.equals("m_91291_")
                        && call.desc.equals("()Lnet/minecraft/client/renderer/entity/ItemRenderer;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = bridge;
                    call.name = "registrationContext";
                    call.desc = "(Lnet/minecraft/client/Minecraft;)Ljava/lang/Object;";
                    call.itf = false;
                    rewrites++;
                    continue;
                }
                if (modelCallback
                        && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("net/minecraft/client/renderer/entity/ItemRenderer")
                        && call.name.equals("m_115103_")
                        && call.desc.equals("()Lnet/minecraft/client/renderer/ItemModelShaper;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = bridge;
                    call.name = "itemModelRegistration";
                    call.desc = "(Ljava/lang/Object;)Ljava/lang/Object;";
                    call.itf = false;
                    rewrites++;
                    continue;
                }
                if (modelCallback
                        && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("net/minecraft/client/renderer/ItemModelShaper")
                        && call.name.equals("m_109396_")
                        && call.desc.equals("(Lnet/minecraft/world/item/Item;"
                                + "Lnet/minecraft/client/resources/model/ModelResourceLocation;)V")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = bridge;
                    call.name = "registerItemModel";
                    call.desc = "(Ljava/lang/Object;Lnet/minecraft/world/item/Item;"
                            + "Lnet/minecraft/client/resources/model/ModelResourceLocation;)V";
                    call.itf = false;
                    rewrites++;
                    continue;
                }
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESTATIC
                        && call.owner.equals("net/minecraft/client/renderer/block/BlockModelShaper")
                        && call.name.equals("m_110895_")
                        && call.desc.equals("(Lnet/minecraft/world/level/block/state/BlockState;)"
                                + "Lnet/minecraft/client/resources/model/ModelResourceLocation;")) {
                    call.owner = bridge;
                    call.name = "stateModelKey";
                    call.desc = "(Lnet/minecraft/world/level/block/state/BlockState;)"
                            + "Lnet/minecraft/client/resources/model/ModelResourceLocation;";
                    call.itf = false;
                    rewrites++;
                    continue;
                }
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("net/minecraft/client/Minecraft")
                        && call.name.equals("m_91258_")
                        && call.desc.equals("(Lnet/minecraft/resources/ResourceLocation;)"
                                + "Ljava/util/function/Function;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = bridge;
                    call.name = "spriteLookup";
                    call.desc = "(Lnet/minecraft/client/Minecraft;"
                            + "Lnet/minecraft/resources/ResourceLocation;)"
                            + "Ljava/util/function/Function;";
                    call.itf = false;
                    rewrites++;
                }
            }
        }
        return rewrites;
    }

    /**
     * Configured features moved from the process-global BuiltinRegistries into
     * target datapack RegistryAccess. Defer calls to proven no-arg initializers
     * that consume that exact legacy registry, then resolve the registry from
     * the active server when the initializer runs.
     */
    private int rewriteLegacyConfiguredFeatureInitialization(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            FieldInsnNode treeMapping = legacySimpleTreeMappingField(node, method);
            if (treeMapping != null) {
                method.instructions.clear();
                method.tryCatchBlocks.clear();
                method.localVariables = null;
                method.instructions.add(new org.objectweb.asm.tree.VarInsnNode(
                        org.objectweb.asm.Opcodes.ALOAD, 0));
                method.instructions.add(new FieldInsnNode(
                        org.objectweb.asm.Opcodes.GETFIELD, node.name,
                        treeMapping.name, treeMapping.desc));
                method.instructions.add(new MethodInsnNode(
                        org.objectweb.asm.Opcodes.INVOKESTATIC,
                        "com/fuckingdeveloper/lms/runtime/Forge1192DynamicRegistryBridge",
                        "populateSimpleTreeBlockMapping", "(Ljava/util/Map;)V", false));
                method.instructions.add(new org.objectweb.asm.tree.InsnNode(
                        org.objectweb.asm.Opcodes.RETURN));
                rewrites++;
                continue;
            }
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || (call.getOpcode() != org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                            && call.getOpcode() != org.objectweb.asm.Opcodes.INVOKEINTERFACE)
                        || !call.desc.equals("()V")
                        || !usesLegacyConfiguredFeatures(call.owner, call.name, call.desc)) continue;
                var metadata = new org.objectweb.asm.tree.InsnList();
                metadata.add(new LdcInsnNode(Type.getObjectType(call.owner)));
                metadata.add(new LdcInsnNode(call.name));
                method.instructions.insertBefore(call, metadata);
                call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192DynamicRegistryBridge";
                call.name = "deferConfiguredFeatureInitialization";
                call.desc = "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/String;)V";
                call.itf = false;
                rewrites++;
            }
        }
        return rewrites;
    }

    private static FieldInsnNode legacySimpleTreeMappingField(
            ClassNode owner, org.objectweb.asm.tree.MethodNode method) {
        if (!method.desc.equals("()V")
                || (method.access & org.objectweb.asm.Opcodes.ACC_STATIC) != 0) return null;
        FieldInsnNode mapping = null;
        boolean configuredFeatures = false;
        boolean treeConfiguration = false;
        boolean trunkProvider = false;
        boolean foliageProvider = false;
        for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof FieldInsnNode field) {
                if (field.getOpcode() == org.objectweb.asm.Opcodes.GETFIELD
                        && field.owner.equals(owner.name)
                        && field.desc.equals("Ljava/util/Map;")) {
                    if (mapping != null && !mapping.name.equals(field.name)) return null;
                    mapping = field;
                } else if (field.getOpcode() == org.objectweb.asm.Opcodes.GETSTATIC
                        && field.owner.equals("net/minecraft/data/BuiltinRegistries")
                        && field.name.equals("f_123861_")
                        && field.desc.equals("Lnet/minecraft/core/Registry;")) {
                    configuredFeatures = true;
                } else if (field.owner.equals(
                        "net/minecraft/world/level/levelgen/feature/configurations/TreeConfiguration")
                        && field.desc.equals(
                        "Lnet/minecraft/world/level/levelgen/feature/stateproviders/BlockStateProvider;")) {
                    if (field.name.equals("f_68185_")) trunkProvider = true;
                    if (field.name.equals("f_161213_")) foliageProvider = true;
                }
            } else if (insn instanceof TypeInsnNode type
                    && type.desc.equals(
                    "net/minecraft/world/level/levelgen/feature/configurations/TreeConfiguration")) {
                treeConfiguration = true;
            }
        }
        return configuredFeatures && treeConfiguration && trunkProvider && foliageProvider
                ? mapping : null;
    }

    private boolean usesLegacyConfiguredFeatures(String owner, String name, String descriptor) {
        if (owner.startsWith("net/minecraft/")) return false;
        String key = owner + '#' + name + descriptor;
        return configuredFeatureInitializers.computeIfAbsent(key, ignored -> {
            try {
                byte[] bytes = managedClassBytes == null ? null : managedClassBytes.find(owner);
                if (bytes == null) return false;
                ClassNode candidate = new ClassNode();
                new ClassReader(bytes).accept(candidate, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                var methods = candidate.methods.stream()
                        .filter(method -> method.name.equals(name) && method.desc.equals(descriptor))
                        .toList();
                if (methods.size() != 1) return false;
                return legacySimpleTreeMappingField(candidate, methods.getFirst()) != null;
            } catch (java.io.IOException | RuntimeException e) {
                return false;
            }
        });
    }

    /**
     * Replace legacy Mixin accessors only when their annotations prove the
     * exact target and member and the target runtime exposes an equivalent
     * public property API.
     */
    private int rewriteLegacyMixinPropertyAccessors(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKEINTERFACE) continue;
                LegacyMixinAccessor accessor = legacyMixinAccessor(call.owner, call.name, call.desc);
                if (accessor == null) continue;
                if (accessor.target().equals("net/minecraft/client/renderer/LevelRenderer")
                        && accessor.member().equals("renderBuffers")
                        && call.desc.equals("()Lnet/minecraft/client/renderer/RenderBuffers;")) {
                    var receiverCast = previousExecutable(call);
                    var buffersCast = nextExecutable(call);
                    var fixedBuffersCall = buffersCast == null
                            ? null : nextExecutable(buffersCast);
                    if (!(receiverCast instanceof TypeInsnNode receiverType)
                            || receiverCast.getOpcode() != org.objectweb.asm.Opcodes.CHECKCAST
                            || !receiverType.desc.equals(call.owner)
                            || !(buffersCast instanceof TypeInsnNode)
                            || buffersCast.getOpcode() != org.objectweb.asm.Opcodes.CHECKCAST
                            || !(fixedBuffersCall instanceof MethodInsnNode fixedCall)
                            || fixedCall.getOpcode() != org.objectweb.asm.Opcodes.INVOKEINTERFACE) {
                        continue;
                    }
                    LegacyMixinAccessor fixedAccessor = legacyMixinAccessor(
                            fixedCall.owner, fixedCall.name, fixedCall.desc);
                    if (fixedAccessor == null
                            || !fixedAccessor.target().equals(
                                    "net/minecraft/client/renderer/RenderBuffers")
                            || !fixedAccessor.member().equals("fixedBuffers")
                            || !fixedCall.desc.equals("()Ljava/util/SortedMap;")) continue;
                    method.instructions.remove(receiverCast);
                    method.instructions.remove(buffersCast);
                    method.instructions.remove(fixedCall);
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/render/"
                            + "Forge1192FixedBufferBridge";
                    call.name = "registrationContext";
                    call.desc = "(Lnet/minecraft/client/renderer/LevelRenderer;)"
                            + "Ljava/util/SortedMap;";
                    call.itf = false;
                    rewrites++;
                    continue;
                }
                String targetName;
                String targetDescriptor;
                boolean discardFluentResult;
                if (accessor.target().equals("net/minecraft/world/level/block/state/BlockBehaviour")
                        && accessor.member().equals("properties")
                        && call.desc.equals(
                        "()Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;")) {
                    targetName = "properties";
                    targetDescriptor = call.desc;
                    discardFluentResult = false;
                } else if (accessor.target().equals(
                        "net/minecraft/world/level/block/state/BlockBehaviour$Properties")
                        && accessor.member().equals("explosionResistance")
                        && call.desc.equals("(F)V")) {
                    targetName = "explosionResistance";
                    targetDescriptor = "(F)Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;";
                    discardFluentResult = true;
                } else {
                    continue;
                }
                var cast = previousExecutable(call);
                if (discardFluentResult) {
                    if (!(cast instanceof org.objectweb.asm.tree.VarInsnNode argument)
                            || argument.getOpcode() != org.objectweb.asm.Opcodes.FLOAD) continue;
                    cast = previousExecutable(cast);
                }
                if (!(cast instanceof TypeInsnNode type)
                        || cast.getOpcode() != org.objectweb.asm.Opcodes.CHECKCAST
                        || !type.desc.equals(call.owner)) continue;
                method.instructions.remove(cast);
                call.setOpcode(org.objectweb.asm.Opcodes.INVOKEVIRTUAL);
                call.owner = accessor.target();
                call.name = targetName;
                call.desc = targetDescriptor;
                call.itf = false;
                if (discardFluentResult) {
                    method.instructions.insert(call, new org.objectweb.asm.tree.InsnNode(
                            org.objectweb.asm.Opcodes.POP));
                }
                rewrites++;
            }
        }
        return rewrites;
    }

    /** Capture removed source fixed-buffer registrations as render intent. */
    private static int rewriteLegacyFixedBufferRegistration(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            if ((method.access & org.objectweb.asm.Opcodes.ACC_STATIC) == 0
                    || !method.desc.equals("(Ljava/util/SortedMap;"
                            + "Lnet/minecraft/client/renderer/RenderType;)V")) continue;
            boolean readsBufferSize = false;
            boolean constructsBuffer = false;
            boolean insertsSortedMap = false;
            for (var insn = method.instructions.getFirst(); insn != null;
                    insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)) continue;
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("net/minecraft/client/renderer/RenderType")
                        && call.name.equals("m_110507_") && call.desc.equals("()I")) {
                    readsBufferSize = true;
                } else if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESPECIAL
                        && call.owner.equals("com/mojang/blaze3d/vertex/BufferBuilder")
                        && call.name.equals("<init>") && call.desc.equals("(I)V")) {
                    constructsBuffer = true;
                } else if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEINTERFACE
                        && call.owner.equals("java/util/SortedMap")
                        && call.name.equals("put")
                        && call.desc.equals("(Ljava/lang/Object;Ljava/lang/Object;)"
                                + "Ljava/lang/Object;")) {
                    insertsSortedMap = true;
                }
            }
            if (!readsBufferSize || !constructsBuffer || !insertsSortedMap) continue;
            method.instructions.clear();
            method.tryCatchBlocks.clear();
            method.localVariables = null;
            method.visibleLocalVariableAnnotations = null;
            method.invisibleLocalVariableAnnotations = null;
            method.instructions.add(new org.objectweb.asm.tree.VarInsnNode(
                    org.objectweb.asm.Opcodes.ALOAD, 1));
            method.instructions.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC,
                    "com/fuckingdeveloper/lms/runtime/render/Forge1192FixedBufferBridge",
                    "register", "(Lcom/fuckingdeveloper/lms/runtime/render/LegacyRenderType;)V",
                    false));
            method.instructions.add(new org.objectweb.asm.tree.InsnNode(
                    org.objectweb.asm.Opcodes.RETURN));
            rewrites++;
        }
        return rewrites;
    }

    private record LegacyMixinAccessor(String target, String member) {}

    private LegacyMixinAccessor legacyMixinAccessor(String owner, String name, String descriptor) {
        try {
            byte[] bytes = managedClassBytes == null ? null : managedClassBytes.find(owner);
            if (bytes == null) return null;
            ClassNode mixin = new ClassNode();
            new ClassReader(bytes).accept(mixin, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            org.objectweb.asm.tree.AnnotationNode mixinAnnotation = annotation(
                    mixin.visibleAnnotations, mixin.invisibleAnnotations,
                    "Lorg/spongepowered/asm/mixin/Mixin;");
            if (mixinAnnotation == null) return null;
            Object targets = annotationValue(mixinAnnotation, "value");
            if (!(targets instanceof java.util.List<?> list) || list.size() != 1
                    || !(list.getFirst() instanceof Type target)) return null;
            var methods = mixin.methods.stream()
                    .filter(method -> method.name.equals(name) && method.desc.equals(descriptor))
                    .toList();
            if (methods.size() != 1) return null;
            org.objectweb.asm.tree.AnnotationNode accessor = annotation(
                    methods.getFirst().visibleAnnotations, methods.getFirst().invisibleAnnotations,
                    "Lorg/spongepowered/asm/mixin/gen/Accessor;");
            if (accessor == null) return null;
            Object member = annotationValue(accessor, "value");
            if (!(member instanceof String field) || field.isBlank()) return null;
            return new LegacyMixinAccessor(target.getInternalName(), field);
        } catch (java.io.IOException | RuntimeException ignored) {
            return null;
        }
    }

    private static org.objectweb.asm.tree.AnnotationNode annotation(
            java.util.List<org.objectweb.asm.tree.AnnotationNode> visible,
            java.util.List<org.objectweb.asm.tree.AnnotationNode> invisible,
            String descriptor) {
        if (visible != null) {
            for (var annotation : visible) if (annotation.desc.equals(descriptor)) return annotation;
        }
        if (invisible != null) {
            for (var annotation : invisible) if (annotation.desc.equals(descriptor)) return annotation;
        }
        return null;
    }

    private static Object annotationValue(
            org.objectweb.asm.tree.AnnotationNode annotation, String name) {
        if (annotation.values == null) return null;
        for (int i = 0; i + 1 < annotation.values.size(); i += 2) {
            if (name.equals(annotation.values.get(i))) return annotation.values.get(i + 1);
        }
        return null;
    }

    /**
     * Forge's client recipe notification was replaced by RecipesReceivedEvent.
     * Adapt only handlers which treat the event as a timing signal and never
     * read the legacy event argument; data-consuming handlers need a semantic
     * recipe-map adapter and intentionally remain fail-closed.
     */
    private static int rewriteLegacyRecipesUpdatedHandlers(ClassNode node) {
        final String legacy = "net/minecraftforge/client/event/RecipesUpdatedEvent";
        final String target = "net/neoforged/neoforge/client/event/RecipesReceivedEvent";
        int rewrites = 0;
        for (var method : node.methods) {
            if (!method.desc.equals("(L" + legacy + ";)V")) continue;
            int parameterSlot = (method.access & org.objectweb.asm.Opcodes.ACC_STATIC) == 0 ? 1 : 0;
            boolean used = false;
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof org.objectweb.asm.tree.VarInsnNode variable
                        && variable.getOpcode() == org.objectweb.asm.Opcodes.ALOAD
                        && variable.var == parameterSlot) {
                    used = true;
                    break;
                }
            }
            if (used) continue;
            method.desc = "(L" + target + ";)V";
            if (method.signature != null) method.signature = method.signature.replace(legacy, target);
            if (method.localVariables != null) {
                for (var local : method.localVariables) {
                    if (local.desc.equals("L" + legacy + ";")) local.desc = "L" + target + ";";
                    if (local.signature != null) local.signature = local.signature.replace(legacy, target);
                }
            }
            rewrites++;
        }
        return rewrites;
    }

    /**
     * Minecraft 1.19.2 exposed compostability through ComposterBlock's mutable
     * item-to-chance map.  Minecraft 26.3 stores the equivalent intent in the
     * item's default COMPOSTABLE component.  Adapt only the proven statement
     * shape whose map-put return value is discarded; reads and value-dependent
     * mutations remain fail-closed.
     */
    private static int rewriteLegacyComposterRegistrations(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode put)
                        || put.getOpcode() != org.objectweb.asm.Opcodes.INVOKEINTERFACE
                        || !put.owner.equals("it/unimi/dsi/fastutil/objects/Object2FloatMap")
                        || !put.name.equals("put")
                        || !put.desc.equals("(Ljava/lang/Object;F)F")
                        || nextExecutable(put) == null
                        || nextExecutable(put).getOpcode() != org.objectweb.asm.Opcodes.POP) continue;
                var chance = previousExecutable(put);
                var item = chance == null ? null : previousExecutable(chance);
                var map = item == null ? null : previousExecutable(item);
                if (!(map instanceof FieldInsnNode field)
                        || field.getOpcode() != org.objectweb.asm.Opcodes.GETSTATIC
                        || !field.owner.equals("net/minecraft/world/level/block/ComposterBlock")
                        || !field.name.equals("f_51914_")
                        || !field.desc.equals("Lit/unimi/dsi/fastutil/objects/Object2FloatMap;")) continue;
                method.instructions.remove(field);
                put.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                put.owner = "com/fuckingdeveloper/lms/runtime/Forge1192ComposterBridge";
                put.name = "registerCompostable";
                put.desc = "(Ljava/lang/Object;F)F";
                put.itf = false;
                rewrites++;
            }
        }
        return rewrites;
    }

    /**
     * Forge 1.19.2 render-stage events exposed model-view/projection matrices
     * from which callers rebuilt the current culling frustum. Target 26.3
     * already owns the authoritative prepared frustum in its camera render
     * state. Replace only that exact constructor expression with a defensive
     * copy, leaving unrelated legacy matrix consumers fail-closed.
     */
    private static int rewriteLegacyRenderFrustumConstruction(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null;) {
                var next = insn.getNext();
                if (!(insn instanceof MethodInsnNode constructor)
                        || constructor.getOpcode() != org.objectweb.asm.Opcodes.INVOKESPECIAL
                        || !constructor.owner.equals("net/minecraft/client/renderer/culling/Frustum")
                        || !constructor.name.equals("<init>")
                        || !constructor.desc.equals("(Lcom/mojang/math/Matrix4f;"
                                + "Lcom/mojang/math/Matrix4f;)V")) {
                    insn = next;
                    continue;
                }
                var projection = previousExecutable(constructor);
                var eventLoad2 = projection == null ? null : previousExecutable(projection);
                var pose = eventLoad2 == null ? null : previousExecutable(eventLoad2);
                var last = pose == null ? null : previousExecutable(pose);
                var getPoseStack = last == null ? null : previousExecutable(last);
                var eventLoad1 = getPoseStack == null ? null : previousExecutable(getPoseStack);
                var duplicate = eventLoad1 == null ? null : previousExecutable(eventLoad1);
                var allocation = duplicate == null ? null : previousExecutable(duplicate);
                if (!(projection instanceof MethodInsnNode projectionCall)
                        || projectionCall.getOpcode() != org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        || !projectionCall.owner.equals(
                                "net/minecraftforge/client/event/RenderLevelStageEvent")
                        || !projectionCall.name.equals("getProjectionMatrix")
                        || !projectionCall.desc.equals("()Lcom/mojang/math/Matrix4f;")
                        || !(eventLoad2 instanceof org.objectweb.asm.tree.VarInsnNode load2)
                        || load2.getOpcode() != org.objectweb.asm.Opcodes.ALOAD
                        || !(pose instanceof MethodInsnNode poseCall)
                        || poseCall.getOpcode() != org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        || !poseCall.owner.equals("com/mojang/blaze3d/vertex/PoseStack$Pose")
                        || !poseCall.name.equals("m_85861_")
                        || !poseCall.desc.equals("()Lcom/mojang/math/Matrix4f;")
                        || !(last instanceof MethodInsnNode lastCall)
                        || lastCall.getOpcode() != org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        || !lastCall.owner.equals("com/mojang/blaze3d/vertex/PoseStack")
                        || !lastCall.name.equals("m_85850_")
                        || !lastCall.desc.equals(
                                "()Lcom/mojang/blaze3d/vertex/PoseStack$Pose;")
                        || !(getPoseStack instanceof MethodInsnNode poseStackCall)
                        || poseStackCall.getOpcode() != org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        || !poseStackCall.owner.equals(
                                "net/minecraftforge/client/event/RenderLevelStageEvent")
                        || !poseStackCall.name.equals("getPoseStack")
                        || !poseStackCall.desc.equals(
                                "()Lcom/mojang/blaze3d/vertex/PoseStack;")
                        || !(eventLoad1 instanceof org.objectweb.asm.tree.VarInsnNode load1)
                        || load1.getOpcode() != org.objectweb.asm.Opcodes.ALOAD
                        || load1.var != load2.var
                        || duplicate.getOpcode() != org.objectweb.asm.Opcodes.DUP
                        || !(allocation instanceof TypeInsnNode type)
                        || allocation.getOpcode() != org.objectweb.asm.Opcodes.NEW
                        || !type.desc.equals("net/minecraft/client/renderer/culling/Frustum")) {
                    insn = next;
                    continue;
                }

                var replacement = new org.objectweb.asm.tree.InsnList();
                replacement.add(new org.objectweb.asm.tree.VarInsnNode(
                        org.objectweb.asm.Opcodes.ALOAD, load1.var));
                replacement.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC,
                        "com/fuckingdeveloper/lms/runtime/render/Forge1192RenderStageBridge",
                        "frustum", "(Lnet/neoforged/neoforge/client/event/RenderLevelStageEvent;)"
                                + "Lnet/minecraft/client/renderer/culling/Frustum;", false));
                method.instructions.insertBefore(allocation, replacement);
                method.instructions.remove(allocation);
                method.instructions.remove(duplicate);
                method.instructions.remove(eventLoad1);
                method.instructions.remove(getPoseStack);
                method.instructions.remove(last);
                method.instructions.remove(pose);
                method.instructions.remove(eventLoad2);
                method.instructions.remove(projection);
                method.instructions.remove(constructor);
                rewrites++;
                insn = next;
            }
        }
        return rewrites;
    }

    /**
     * Forge 1.19.2 render-living events exposed the rendered entity directly,
     * while the target event exposes only its extracted render state. Route
     * the exact removed accessor through a target-aware semantic bridge.
     */
    private static int rewriteLegacyRenderLivingEventAccessors(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        || !call.name.equals("getEntity")
                        || !call.desc.equals("()Lnet/minecraft/world/entity/LivingEntity;")) continue;
                if (!call.owner.equals("net/minecraftforge/client/event/RenderLivingEvent$Pre")
                        && !call.owner.equals("net/minecraftforge/client/event/RenderLivingEvent$Post")
                        && !call.owner.equals("net/minecraftforge/client/event/RenderLivingEvent")) continue;
                call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                call.owner = "com/fuckingdeveloper/lms/runtime/render/Forge1192RenderLivingEventBridge";
                call.name = "entity";
                call.desc = "(Ljava/lang/Object;)Lnet/minecraft/world/entity/LivingEntity;";
                call.itf = false;
                rewrites++;
            }
        }
        return rewrites;
    }

    /** Adapt the removed entity-valued RenderLayer override to target render states. */
    private int rewriteLegacyEntityRenderLayerOverride(ClassNode node) {
        final String sourceLayer = "net/minecraft/client/renderer/entity/layers/RenderLayer";
        final String sourceDescriptor = "(Lcom/mojang/blaze3d/vertex/PoseStack;"
                + "Lnet/minecraft/client/renderer/MultiBufferSource;I"
                + "Lnet/minecraft/world/entity/Entity;FFFFFF)V";
        if (!isManagedSubtypeOf(node, sourceLayer)) return 0;
        org.objectweb.asm.tree.MethodNode legacy = null;
        for (var method : node.methods) {
            if (method.name.equals("m_6494_") && method.desc.equals(sourceDescriptor)) {
                legacy = method;
                break;
            }
        }
        if (legacy == null) return 0;
        legacy.name = "lmsLegacyRender";
        final String adapter = "com/fuckingdeveloper/lms/runtime/render/LegacyEntityRenderLayer";
        if (!node.interfaces.contains(adapter)) node.interfaces.add(adapter);

        String targetDescriptor = "(Lcom/mojang/blaze3d/vertex/PoseStack;"
                + "Lnet/minecraft/client/renderer/SubmitNodeCollector;I"
                + "Lnet/minecraft/client/renderer/entity/state/EntityRenderState;FF)V";
        boolean alreadyPresent = node.methods.stream()
                .anyMatch(method -> method.name.equals("submit")
                        && method.desc.equals(targetDescriptor));
        if (alreadyPresent) return 1;
        var submit = new org.objectweb.asm.tree.MethodNode(
                org.objectweb.asm.Opcodes.ACC_PUBLIC | org.objectweb.asm.Opcodes.ACC_SYNTHETIC,
                "submit", targetDescriptor, null, null);
        submit.instructions.add(new org.objectweb.asm.tree.VarInsnNode(
                org.objectweb.asm.Opcodes.ALOAD, 0));
        submit.instructions.add(new org.objectweb.asm.tree.VarInsnNode(
                org.objectweb.asm.Opcodes.ALOAD, 1));
        submit.instructions.add(new org.objectweb.asm.tree.VarInsnNode(
                org.objectweb.asm.Opcodes.ALOAD, 2));
        submit.instructions.add(new org.objectweb.asm.tree.VarInsnNode(
                org.objectweb.asm.Opcodes.ILOAD, 3));
        submit.instructions.add(new org.objectweb.asm.tree.VarInsnNode(
                org.objectweb.asm.Opcodes.ALOAD, 4));
        submit.instructions.add(new org.objectweb.asm.tree.VarInsnNode(
                org.objectweb.asm.Opcodes.FLOAD, 5));
        submit.instructions.add(new org.objectweb.asm.tree.VarInsnNode(
                org.objectweb.asm.Opcodes.FLOAD, 6));
        submit.instructions.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC,
                "com/fuckingdeveloper/lms/runtime/render/Forge1192EntityRenderLayerBridge",
                "submit", "(L" + adapter + ";Lcom/mojang/blaze3d/vertex/PoseStack;"
                        + "Lnet/minecraft/client/renderer/SubmitNodeCollector;I"
                        + "Lnet/minecraft/client/renderer/entity/state/EntityRenderState;FF)V",
                false));
        submit.instructions.add(new org.objectweb.asm.tree.InsnNode(
                org.objectweb.asm.Opcodes.RETURN));
        node.methods.add(submit);
        return 2;
    }

    /**
     * Static DamageSource singletons became registry-backed DamageType keys.
     * Adapt only the exact identity-comparison shape to DamageSource#is(key).
     */
    private int rewriteLegacyDamageSourceSingletonComparisons(ClassNode node) {
        if (SRG_RUNTIME_RESOLVER.isEmpty()
                || VANILLA_RELOCATIONS.classifyMethod(
                        "net.minecraft.world.damagesource.DamageSource", "is",
                        "(Lnet/minecraft/resources/ResourceKey;)Z").status()
                        != Forge1192MappingLayer.Status.VERIFIED_IDENTITY) return 0;
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null;) {
                var current = insn;
                insn = insn.getNext();
                if (!(current instanceof FieldInsnNode field)
                        || field.getOpcode() != org.objectweb.asm.Opcodes.GETSTATIC
                        || !field.owner.equals("net/minecraft/world/damagesource/DamageSource")
                        || !field.desc.equals("Lnet/minecraft/world/damagesource/DamageSource;")) continue;
                var comparison = nextExecutable(field);
                if (!(comparison instanceof org.objectweb.asm.tree.JumpInsnNode jump)
                        || (jump.getOpcode() != org.objectweb.asm.Opcodes.IF_ACMPEQ
                        && jump.getOpcode() != org.objectweb.asm.Opcodes.IF_ACMPNE)) continue;
                String canonical = SRG_RUNTIME_RESOLVER.get()
                        .canonicalFieldName(field.owner, field.name, field.desc).orElse(null);
                if (canonical == null || VANILLA_RELOCATIONS.searchExactField(
                        "net.minecraft.world.damagesource.DamageTypes", canonical,
                        "Lnet/minecraft/resources/ResourceKey;").size() != 1) continue;
                field.owner = "net/minecraft/world/damagesource/DamageTypes";
                field.name = canonical;
                field.desc = "Lnet/minecraft/resources/ResourceKey;";
                method.instructions.insert(field, new MethodInsnNode(
                        org.objectweb.asm.Opcodes.INVOKEVIRTUAL,
                        "net/minecraft/world/damagesource/DamageSource", "is",
                        "(Lnet/minecraft/resources/ResourceKey;)Z", false));
                int opcode = jump.getOpcode() == org.objectweb.asm.Opcodes.IF_ACMPEQ
                        ? org.objectweb.asm.Opcodes.IFNE : org.objectweb.asm.Opcodes.IFEQ;
                method.instructions.set(jump, new org.objectweb.asm.tree.JumpInsnNode(
                        opcode, jump.label));
                rewrites++;
            }
        }
        return rewrites;
    }

    /**
     * Preserve the construction phase of a source persistent vertex cache
     * only when its field and finish/draw/delete methods prove the complete
     * Forge 1.19.2 VertexBuffer lifecycle shape. Functional drawing remains an
     * explicit boundary in the runtime bridge.
     */
    private int rewriteLegacyBufferedVertexCacheLifecycle(ClassNode node) {
        if (!isManagedSubtypeOf(node, "com/mojang/blaze3d/vertex/BufferVertexConsumer")) return 0;
        var bufferFields = node.fields.stream()
                .filter(field -> field.desc.equals("Lcom/mojang/blaze3d/vertex/VertexBuffer;"))
                .toList();
        if (bufferFields.size() != 1) return 0;
        var finish = node.methods.stream()
                .filter(method -> method.name.equals("finish") && method.desc.equals("()V")
                        && containsMethodCall(method, org.objectweb.asm.Opcodes.INVOKEVIRTUAL,
                                "com/mojang/blaze3d/vertex/VertexBuffer", "m_231221_",
                                "(Lcom/mojang/blaze3d/vertex/BufferBuilder$RenderedBuffer;)V"))
                .findFirst().orElse(null);
        var draw = node.methods.stream()
                .filter(method -> method.name.equals("draw") && method.desc.equals("()V")
                        && containsMethodCall(method, org.objectweb.asm.Opcodes.INVOKEVIRTUAL,
                                "com/mojang/blaze3d/vertex/VertexBuffer", "m_166867_",
                                "(Lcom/mojang/math/Matrix4f;Lcom/mojang/math/Matrix4f;"
                                        + "Lnet/minecraft/client/renderer/ShaderInstance;)V"))
                .findFirst().orElse(null);
        var delete = node.methods.stream()
                .filter(method -> method.name.equals("delete") && method.desc.equals("()V")
                        && containsMethodCall(method, org.objectweb.asm.Opcodes.INVOKEVIRTUAL,
                                "com/mojang/blaze3d/vertex/VertexBuffer", "close", "()V"))
                .findFirst().orElse(null);
        if (finish == null || draw == null || delete == null) return 0;

        var bufferField = bufferFields.getFirst();
        bufferField.desc = "Ljava/lang/Object;";
        bufferField.signature = null;
        replaceVertexCacheLifecycleMethod(finish, "finish");
        replaceVertexCacheLifecycleMethod(draw, "draw");
        replaceVertexCacheLifecycleMethod(delete, "delete");
        return 4;
    }

    private static boolean containsMethodCall(org.objectweb.asm.tree.MethodNode method,
            int opcode, String owner, String name, String descriptor) {
        for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof MethodInsnNode call
                    && call.getOpcode() == opcode
                    && call.owner.equals(owner)
                    && call.name.equals(name)
                    && call.desc.equals(descriptor)) return true;
        }
        return false;
    }

    private static void replaceVertexCacheLifecycleMethod(
            org.objectweb.asm.tree.MethodNode method, String operation) {
        method.instructions.clear();
        method.tryCatchBlocks.clear();
        if (method.localVariables != null) method.localVariables.clear();
        method.instructions.add(new org.objectweb.asm.tree.VarInsnNode(
                org.objectweb.asm.Opcodes.ALOAD, 0));
        method.instructions.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC,
                "com/fuckingdeveloper/lms/runtime/render/LegacyBufferedVertexCacheBridge",
                operation, "(Ljava/lang/Object;)V", false));
        method.instructions.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.RETURN));
    }

    private static int rewriteLegacyMultiBufferSource(ClassNode node) {
        final String legacy = "net/minecraft/client/renderer/MultiBufferSource";
        final String adapted =
                "com/fuckingdeveloper/lms/runtime/render/LegacyMultiBufferSource";
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call) || !call.owner.equals(legacy)) continue;
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEINTERFACE
                        && call.name.equals("m_6299_")
                        && call.desc.equals("(Lnet/minecraft/client/renderer/RenderType;)"
                                + "Lcom/mojang/blaze3d/vertex/VertexConsumer;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKEVIRTUAL);
                    call.owner = adapted;
                    call.itf = false;
                    rewrites++;
                } else if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESTATIC
                        && call.name.equals("m_109898_")
                        && call.desc.equals("(Lcom/mojang/blaze3d/vertex/BufferBuilder;)"
                                + "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;")) {
                    call.owner = adapted;
                    call.desc = "(Lcom/mojang/blaze3d/vertex/BufferBuilder;)"
                            + "Lcom/fuckingdeveloper/lms/runtime/render/LegacyBufferSource;";
                    call.itf = false;
                    rewrites++;
                }
            }
        }
        return rewrites;
    }

    /**
     * Preserve Forge 1.19.2's global block/render-layer predicate for a future
     * model-baking adapter. Minecraft 26.3 assigns a chunk layer to each baked
     * quad, so the source registration has no direct target call equivalent.
     */
    private static int rewriteLegacyBlockRenderLayerRegistration(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKESTATIC
                        || !call.owner.equals("net/minecraft/client/renderer/ItemBlockRenderTypes")
                        || !call.name.equals("setRenderLayer")
                        || !call.desc.equals("(Lnet/minecraft/world/level/block/Block;"
                                + "Ljava/util/function/Predicate;)V")) continue;
                call.owner = "com/fuckingdeveloper/lms/runtime/render/Forge1192RenderLayerBridge";
                call.name = "register";
                call.itf = false;
                rewrites++;
            }
        }
        return rewrites;
    }

    private static void verifyNoEscapingLegacyRegistryFacade(ClassNode node) {
        String legacyRegistry = "net/neoforged/neoforge/registries/IForgeRegistry";
        for (var method : node.methods) {
            // A legacy-owned method may retain IForgeRegistry in its internal
            // ABI. The type itself is an opaque handle; this is equivalent to
            // retaining it in a field. Safety is enforced at executable API
            // boundaries below, where registry operations must be adapted.
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof MethodInsnNode call
                        && (call.owner.contains(legacyRegistry) || call.desc.contains(legacyRegistry))) {
                    throw new IllegalStateException("Unadapted legacy IForgeRegistry method boundary: "
                            + node.name + "#" + method.name + " -> " + call.owner + "#" + call.name + call.desc);
                }
                // A field access may retain IForgeRegistry as a storage type after
                // namespace migration. This is safe when the value never crosses an
                // executable registry API boundary: all IForgeRegistry method calls
                // are independently rewritten above and verified below. Rejecting the
                // field descriptor itself incorrectly blocks legacy registry handles.
                if (insn instanceof TypeInsnNode type && type.desc.contains(legacyRegistry)) {
                    throw new IllegalStateException("Legacy IForgeRegistry escapes through type instruction: "
                            + node.name + "#" + method.name);
                }
            }
        }
    }

    private int rewriteVerifiedSrgMethodCalls(ClassNode node) {
        if (SRG_RUNTIME_RESOLVER.isEmpty()) return 0;
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof InvokeDynamicInsnNode indy) {
                    if (indy.name.startsWith("m_")
                            && indy.bsm.getOwner().equals("java/lang/invoke/LambdaMetafactory")
                            && indy.bsmArgs.length >= 1
                            && indy.bsmArgs[0] instanceof Type samType) {
                        Type functionalType = Type.getReturnType(indy.desc);
                        if (functionalType.getSort() == Type.OBJECT) {
                            String functionalOwner = functionalType.getInternalName();
                            var resolved = SRG_RUNTIME_RESOLVER.get().resolve(
                                    functionalOwner, indy.name, samType.getDescriptor());
                            if (resolved.resolved()
                                    && resolved.owner().equals(functionalOwner)
                                    && resolved.descriptor().equals(samType.getDescriptor())) {
                                indy.name = resolved.name();
                                rewrites++;
                            }
                        }
                    }
                    for (int i = 0; i < indy.bsmArgs.length; i++) {
                        if (!(indy.bsmArgs[i] instanceof Handle handle)) continue;
                        if (handle.getTag() == org.objectweb.asm.Opcodes.H_INVOKESTATIC
                                && handle.getOwner().equals("net/minecraft/client/renderer/Sheets")
                                && handle.getName().equals("addWoodType")
                                && handle.getDesc().equals("(Lnet/minecraft/world/level/block/state/properties/WoodType;)V")) {
                            indy.bsmArgs[i] = new Handle(org.objectweb.asm.Opcodes.H_INVOKESTATIC,
                                    "com/fuckingdeveloper/lms/runtime/Forge1192VanillaConstructionBridge",
                                    "legacyAddWoodType", handle.getDesc(), false);
                            rewrites++;
                            continue;
                        }
                        if (!handle.getName().startsWith("m_")) continue;
                        var resolved = handle.getOwner().startsWith("net/minecraft/")
                                ? SRG_RUNTIME_RESOLVER.get().resolve(
                                        handle.getOwner(), handle.getName(), handle.getDesc())
                                : resolveInheritedVanillaMethod(
                                        handle.getOwner(), handle.getName(), handle.getDesc());
                        if (resolved == null || !resolved.resolved()) continue;
                        String owner = handle.getOwner().startsWith("net/minecraft/")
                                ? resolved.owner() : handle.getOwner();
                        boolean targetInterface = runtimeOwnerIsInterface(owner)
                                .orElse(handle.isInterface());
                        int tag = handle.getTag();
                        if (tag == org.objectweb.asm.Opcodes.H_INVOKEVIRTUAL && targetInterface)
                            tag = org.objectweb.asm.Opcodes.H_INVOKEINTERFACE;
                        else if (tag == org.objectweb.asm.Opcodes.H_INVOKEINTERFACE && !targetInterface)
                            tag = org.objectweb.asm.Opcodes.H_INVOKEVIRTUAL;
                        indy.bsmArgs[i] = new Handle(tag, owner,
                                resolved.name(), resolved.descriptor(), targetInterface);
                        rewrites++;
                    }
                }
                if (!(insn instanceof MethodInsnNode call)) continue;

                // Attribute registry identities became Holder-backed. Migrate
                // an exact source member only when its recovered Mojang name
                // has one exact target overload with the Holder descriptor.
                if (call.desc.contains(
                        "Lnet/minecraft/world/entity/ai/attributes/Attribute;")) {
                    String canonical = SRG_RUNTIME_RESOLVER.get().canonicalMethodName(
                            call.owner, call.name, call.desc).orElse(null);
                    String holderDescriptor = call.desc.replace(
                            "Lnet/minecraft/world/entity/ai/attributes/Attribute;",
                            "Lnet/minecraft/core/Holder;");
                    var candidates = canonical == null ? java.util.List
                            .<Forge1192MappingLayer.MethodCandidate>of()
                            : VANILLA_RELOCATIONS.searchNamedMethods(
                                    call.owner.replace('/', '.'), canonical).candidates().stream()
                                    .filter(candidate -> candidate.descriptor().equals(holderDescriptor))
                                    .toList();
                    if (candidates.size() == 1) {
                        call.name = canonical;
                        call.desc = holderDescriptor;
                        rewrites++;
                    }
                }

                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEINTERFACE
                        && call.owner.equals("net/minecraft/world/item/crafting/Recipe")
                        && call.name.equals("m_8043_")
                        && call.desc.equals("()Lnet/minecraft/world/item/ItemStack;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192RecipeBridge";
                    call.name = "legacyResult";
                    call.desc = "(Lnet/minecraft/world/item/crafting/Recipe;)"
                            + "Lnet/minecraft/world/item/ItemStack;";
                    call.itf = false;
                    rewrites++;
                    continue;
                }

                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEINTERFACE
                        && call.owner.equals("net/minecraft/world/item/crafting/Recipe")
                        && call.name.equals("m_7527_")
                        && call.desc.equals("()Lnet/minecraft/core/NonNullList;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192RecipeBridge";
                    call.name = "legacyIngredients";
                    call.desc = "(Lnet/minecraft/world/item/crafting/Recipe;)"
                            + "Lnet/minecraft/core/NonNullList;";
                    call.itf = false;
                    rewrites++;
                    continue;
                }

                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("net/minecraft/world/item/crafting/RecipeManager")
                        && call.name.equals("m_44051_")
                        && call.desc.equals("()Ljava/util/Collection;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192RecipeBridge";
                    call.name = "legacyRecipes";
                    call.desc = "(Lnet/minecraft/world/item/crafting/RecipeManager;)"
                            + "Ljava/util/Collection;";
                    call.itf = false;
                    rewrites++;
                    continue;
                }

                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESTATIC
                        && call.owner.equals("net/minecraft/client/gui/screens/Screen")
                        && (call.name.equals("m_96637_")
                                || call.name.equals("m_96638_")
                                || call.name.equals("m_96639_"))
                        && call.desc.equals("()Z")) {
                    call.owner = "com/fuckingdeveloper/lms/runtime/render/Forge1192ClientInputBridge";
                    call.name = switch (call.name) {
                        case "m_96637_" -> "hasControlDown";
                        case "m_96638_" -> "hasShiftDown";
                        default -> "hasAltDown";
                    };
                    rewrites++;
                    continue;
                }

                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("net/minecraft/client/renderer/debug/DebugRenderer")
                        && call.name.equals("m_113506_")
                        && call.desc.equals("()Z")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/render/Forge1192DebugBridge";
                    call.name = "toggleChunkBorders";
                    call.desc = "(Lnet/minecraft/client/gui/components/debug/DebugScreenEntryList;)Z";
                    call.itf = false;
                    rewrites++;
                    continue;
                }

                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("net/minecraft/client/renderer/GameRenderer")
                        && call.name.equals("m_109153_")
                        && call.desc.equals("()Lnet/minecraft/client/Camera;")) {
                    call.name = "mainCamera";
                    rewrites++;
                    continue;
                }

                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("net/minecraft/client/Camera")
                        && call.name.equals("m_90583_")
                        && call.desc.equals("()Lnet/minecraft/world/phys/Vec3;")) {
                    call.name = "position";
                    rewrites++;
                    continue;
                }

                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("net/minecraft/client/multiplayer/ClientPacketListener")
                        && call.name.equals("m_105141_")
                        && call.desc.equals("()Lnet/minecraft/world/item/crafting/RecipeManager;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192RecipeBridge";
                    call.name = "clientRecipeManager";
                    call.desc = "(Lnet/minecraft/client/multiplayer/ClientPacketListener;)"
                            + "Lnet/minecraft/world/item/crafting/RecipeManager;";
                    call.itf = false;
                    rewrites++;
                    continue;
                }

                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("net/neoforged/neoforge/client/event/RenderLevelStageEvent")
                        && call.name.equals("getStage")
                        && call.desc.equals("()Lcom/fuckingdeveloper/lms/runtime/render/"
                                + "Forge1192RenderStageBridge$Stage;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/render/Forge1192RenderStageBridge";
                    call.name = "stage";
                    call.desc = "(Lnet/neoforged/neoforge/client/event/RenderLevelStageEvent;)"
                            + "Lcom/fuckingdeveloper/lms/runtime/render/"
                            + "Forge1192RenderStageBridge$Stage;";
                    call.itf = false;
                    rewrites++;
                    continue;
                }

                // NeoForge narrowed the spawn-position event's world result
                // from LevelAccessor to ServerLevelAccessor. The event is
                // server-only and the returned object already satisfied the
                // wider source interface.
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("net/neoforged/neoforge/event/entity/living/"
                                + "MobSpawnEvent$PositionCheck")
                        && call.name.equals("getLevel")
                        && call.desc.equals("()Lnet/minecraft/world/level/LevelAccessor;")) {
                    call.desc = "()Lnet/minecraft/world/level/ServerLevelAccessor;";
                    rewrites++;
                    continue;
                }

                // Source command senders exposed their current world through
                // getCommandSenderWorld. Entity#level is the exact target
                // accessor for player receivers.
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("net/minecraft/world/entity/player/Player")
                        && call.name.equals("m_20193_")
                        && call.desc.equals("()Lnet/minecraft/world/level/Level;")) {
                    call.name = "level";
                    rewrites++;
                    continue;
                }

                // The dimension-key lookup retained its nullable ServerLevel
                // result and only changed from SRG to the target named ABI.
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("net/minecraft/server/MinecraftServer")
                        && call.name.equals("m_129880_")
                        && call.desc.equals("(Lnet/minecraft/resources/ResourceKey;)"
                                + "Lnet/minecraft/server/level/ServerLevel;")) {
                    call.name = "getLevel";
                    rewrites++;
                    continue;
                }

                // Forge 1.19.2 exposed ordered integer command permission
                // levels. Target 26.3 represents the same five thresholds as
                // named permission checks.
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("net/minecraft/commands/CommandSourceStack")
                        && call.name.equals("m_6761_")
                        && call.desc.equals("(I)Z")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192ServerBridge";
                    call.name = "hasPermission";
                    call.desc = "(Lnet/minecraft/commands/CommandSourceStack;I)Z";
                    call.itf = false;
                    rewrites++;
                    continue;
                }

                // The source exposed the operator permission threshold as an
                // integer. Target 26.3 stores the same ordered 0..4 value in
                // LevelBasedPermissionSet#level().
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("net/minecraft/server/MinecraftServer")
                        && call.name.equals("m_7022_")
                        && call.desc.equals("()I")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192ServerBridge";
                    call.name = "operatorUserPermissionLevel";
                    call.desc = "(Lnet/minecraft/server/MinecraftServer;)I";
                    call.itf = false;
                    rewrites++;
                    continue;
                }

                // ResourceLocation became Identifier while retaining the
                // same UTF wire representation. Adapt both directions as one
                // exact FriendlyByteBuf family.
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("net/minecraft/network/FriendlyByteBuf")) {
                    if (call.name.equals("m_130085_")
                            && call.desc.equals("(Lnet/minecraft/resources/Identifier;)"
                                    + "Lnet/minecraft/network/FriendlyByteBuf;")) {
                        call.name = "writeIdentifier";
                        rewrites++;
                        continue;
                    }
                    if (call.name.equals("m_130281_")
                            && call.desc.equals("()Lnet/minecraft/resources/Identifier;")) {
                        call.name = "readIdentifier";
                        rewrites++;
                        continue;
                    }
                }

                // ChunkPos#toLong/asLong retained the exact packed-long
                // semantic in 26.3 as the instance/static pack overloads.
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("net/minecraft/world/level/ChunkPos")
                        && call.name.equals("m_45588_")
                        && call.desc.equals("()J")) {
                    call.name = "pack";
                    rewrites++;
                    continue;
                }
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESTATIC
                        && call.owner.equals("net/minecraft/world/level/ChunkPos")
                        && call.name.equals("m_45589_")
                        && call.desc.equals("(II)J")) {
                    call.name = "pack";
                    rewrites++;
                    continue;
                }

                // PoseStack kept the same transform-stack semantics while
                // leaving the SRG namespace and migrating matrices to JOML.
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("com/mojang/blaze3d/vertex/PoseStack")) {
                    String targetName = switch (call.name + call.desc) {
                        case "m_166856_()V" -> "setIdentity";
                        case "m_85836_()V" -> "pushPose";
                        case "m_85837_(DDD)V" -> "translate";
                        case "m_85841_(FFF)V" -> "scale";
                        case "m_85849_()V" -> "popPose";
                        case "m_85850_()Lcom/mojang/blaze3d/vertex/PoseStack$Pose;" -> "last";
                        // Source clear() and target isEmpty() both report that
                        // the stack contains only its base pose.
                        case "m_85851_()Z" -> "isEmpty";
                        default -> null;
                    };
                    if (targetName != null) {
                        call.name = targetName;
                        rewrites++;
                        continue;
                    }
                }
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESTATIC
                        && call.owner.equals("com/mojang/math/Quaternion")
                        && call.name.equals("m_175225_")
                        && call.desc.equals("(Lorg/joml/Vector3f;)"
                                + "Lcom/mojang/math/Quaternion;")) {
                    call.owner = "com/fuckingdeveloper/lms/runtime/render/"
                            + "Forge1192ItemTransformBridge";
                    call.name = "quaternionFromXYZDegrees";
                    call.desc = "(Lorg/joml/Vector3f;)Lorg/joml/Quaternionf;";
                    rewrites++;
                    continue;
                }

                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESTATIC
                        && call.owner.equals("net/minecraftforge/client/model/geometry/"
                                + "UnbakedGeometryHelper")) {
                    if (call.name.equals("createUnbakedItemElements")
                            && call.desc.equals("(ILnet/minecraft/client/renderer/texture/"
                                    + "TextureAtlasSprite;)Ljava/util/List;")) {
                        call.owner = "com/fuckingdeveloper/lms/runtime/render/"
                                + "Forge1192UnbakedGeometryBridge";
                        rewrites++;
                        continue;
                    }
                    if (call.name.equals("bakeElements")
                            && call.desc.equals("(Ljava/util/List;Ljava/util/function/Function;"
                                    + "Lnet/minecraft/client/renderer/block/dispatch/ModelState;"
                                    + "Lnet/minecraft/resources/Identifier;)Ljava/util/List;")) {
                        call.owner = "com/fuckingdeveloper/lms/runtime/render/"
                                + "Forge1192UnbakedGeometryBridge";
                        rewrites++;
                        continue;
                    }
                }

                // Transformation retained its four semantic components, but
                // target 26.3 widened concrete vectors/quaternions to JOML
                // read-only interfaces. This exact legacy constructor shape
                // is verifier-compatible after Vector3f namespace migration;
                // null quaternion operands retain their identity semantics.
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESPECIAL
                        && call.owner.equals("com/mojang/math/Transformation")
                        && call.name.equals("<init>")
                        && call.desc.equals("(Lorg/joml/Vector3f;Lcom/mojang/math/Quaternion;"
                                + "Lorg/joml/Vector3f;Lcom/mojang/math/Quaternion;)V")) {
                    call.desc = "(Lorg/joml/Vector3fc;Lorg/joml/Quaternionfc;"
                            + "Lorg/joml/Vector3fc;Lorg/joml/Quaternionfc;)V";
                    rewrites++;
                    continue;
                }

                // Preserve the exact source copy contract rather than mapping
                // it to target getMatrix(), whose Matrix4fc is a shared
                // read-only view. The concrete JOML result remains valid for
                // the independently verified Direction.rotate migration.
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("com/mojang/math/Transformation")
                        && call.name.equals("m_121104_")
                        && call.desc.equals("()Lcom/mojang/math/Matrix4f;")) {
                    call.name = "getMatrixCopy";
                    call.desc = "()Lorg/joml/Matrix4f;";
                    rewrites++;
                    continue;
                }
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESTATIC
                        && call.owner.equals("net/minecraft/core/Direction")
                        && call.name.equals("m_122384_")
                        && call.desc.equals("(Lcom/mojang/math/Matrix4f;"
                                + "Lnet/minecraft/core/Direction;)"
                                + "Lnet/minecraft/core/Direction;")) {
                    call.name = "rotate";
                    call.desc = "(Lorg/joml/Matrix4fc;Lnet/minecraft/core/Direction;)"
                            + "Lnet/minecraft/core/Direction;";
                    rewrites++;
                    continue;
                }

                // When a removed fluid-texture accessor immediately consumes
                // IClientFluidTypeExtensions.of(state/fluid), retain the
                // authoritative fluid operand and resolve either the legacy
                // source extension value or the target baked fluid model.
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESTATIC
                        && call.owner.equals("net/neoforged/neoforge/client/extensions/common/"
                                + "IClientFluidTypeExtensions")
                        && call.name.equals("of")) {
                    var next = nextExecutable(call);
                    if (next instanceof MethodInsnNode getter
                            && getter.getOpcode() == org.objectweb.asm.Opcodes.INVOKEINTERFACE
                            && getter.owner.equals(call.owner)
                            && (getter.desc.equals("()Lnet/minecraft/resources/Identifier;")
                                || getter.desc.equals("()I"))
                            && (getter.name.equals("getStillTexture")
                                || getter.name.equals("getFlowingTexture")
                                || getter.name.equals("getTintColor"))) {
                        String argument = switch (call.desc) {
                            case "(Lnet/minecraft/world/level/material/FluidState;)"
                                    + "Lnet/neoforged/neoforge/client/extensions/common/"
                                    + "IClientFluidTypeExtensions;" ->
                                    "Lnet/minecraft/world/level/material/FluidState;";
                            case "(Lnet/minecraft/world/level/material/Fluid;)"
                                    + "Lnet/neoforged/neoforge/client/extensions/common/"
                                    + "IClientFluidTypeExtensions;" ->
                                    "Lnet/minecraft/world/level/material/Fluid;";
                            default -> null;
                        };
                        if (argument != null) {
                            call.owner = "com/fuckingdeveloper/lms/runtime/"
                                    + "Forge1192ClientFluidExtensionsBridge";
                            call.name = getter.name;
                            call.desc = "(" + argument + ")"
                                    + (getter.name.equals("getTintColor")
                                            ? "I" : "Lnet/minecraft/resources/Identifier;");
                            call.itf = false;
                            method.instructions.remove(getter);
                            rewrites++;
                            continue;
                        }
                    }
                }

                // Forge 1.19.2 stored still/flowing/overlay/tint values on
                // IClientFluidTypeExtensions implementations. Target 26.3
                // moved texture selection into fluid models and removed these
                // accessors, but source-defined extension objects still carry
                // the authoritative values during legacy callbacks.
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEINTERFACE
                        && call.owner.equals("net/neoforged/neoforge/client/extensions/common/"
                                + "IClientFluidTypeExtensions")) {
                    String bridgeDesc = switch (call.name + call.desc) {
                        case "getStillTexture()Lnet/minecraft/resources/Identifier;" ->
                                "(Ljava/lang/Object;)Lnet/minecraft/resources/Identifier;";
                        case "getFlowingTexture()Lnet/minecraft/resources/Identifier;" ->
                                "(Ljava/lang/Object;)Lnet/minecraft/resources/Identifier;";
                        case "getOverlayTexture()Lnet/minecraft/resources/Identifier;" ->
                                "(Ljava/lang/Object;)Lnet/minecraft/resources/Identifier;";
                        case "getTintColor()I" -> "(Ljava/lang/Object;)I";
                        case "getStillTexture(Lnet/neoforged/neoforge/fluids/FluidStack;)"
                                + "Lnet/minecraft/resources/Identifier;",
                             "getFlowingTexture(Lnet/neoforged/neoforge/fluids/FluidStack;)"
                                + "Lnet/minecraft/resources/Identifier;",
                             "getOverlayTexture(Lnet/neoforged/neoforge/fluids/FluidStack;)"
                                + "Lnet/minecraft/resources/Identifier;" ->
                                "(Ljava/lang/Object;Ljava/lang/Object;)"
                                        + "Lnet/minecraft/resources/Identifier;";
                        case "getTintColor(Lnet/neoforged/neoforge/fluids/FluidStack;)I" ->
                                "(Ljava/lang/Object;Ljava/lang/Object;)I";
                        default -> null;
                    };
                    if (bridgeDesc != null) {
                        call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                        call.owner = "com/fuckingdeveloper/lms/runtime/"
                                + "Forge1192ClientFluidExtensionsBridge";
                        call.desc = bridgeDesc;
                        call.itf = false;
                        rewrites++;
                        continue;
                    }
                }

                // Minecraft 26.3 made CompoundTag primitive/object reads
                // Optional-valued. Forge 1.19.2 returned type-specific defaults
                // (and accepted tag type 99 for any numeric tag), so preserve
                // that exact source family through a semantic target bridge.
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("net/minecraft/nbt/CompoundTag")) {
                    String bridgeName = switch (call.name + call.desc) {
                        case "m_128435_(Ljava/lang/String;)B" -> "getTagType";
                        case "m_128425_(Ljava/lang/String;I)Z" -> "contains";
                        case "m_128445_(Ljava/lang/String;)B" -> "getByte";
                        case "m_128448_(Ljava/lang/String;)S" -> "getShort";
                        case "m_128451_(Ljava/lang/String;)I" -> "getInt";
                        case "m_128454_(Ljava/lang/String;)J" -> "getLong";
                        case "m_128457_(Ljava/lang/String;)F" -> "getFloat";
                        case "m_128459_(Ljava/lang/String;)D" -> "getDouble";
                        case "m_128461_(Ljava/lang/String;)Ljava/lang/String;" -> "getString";
                        case "m_128463_(Ljava/lang/String;)[B" -> "getByteArray";
                        case "m_128465_(Ljava/lang/String;)[I" -> "getIntArray";
                        case "m_128467_(Ljava/lang/String;)[J" -> "getLongArray";
                        case "m_128469_(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;" -> "getCompound";
                        case "m_128437_(Ljava/lang/String;I)Lnet/minecraft/nbt/ListTag;" -> "getList";
                        case "m_128471_(Ljava/lang/String;)Z" -> "getBoolean";
                        case "m_128473_(Ljava/lang/String;)V" -> "remove";
                        case "m_128431_()Ljava/util/Set;" -> "keySet";
                        default -> null;
                    };
                    if (bridgeName != null) {
                        call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                        call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192CompoundTagBridge";
                        call.name = bridgeName;
                        call.desc = "(Lnet/minecraft/nbt/CompoundTag;" + call.desc.substring(1);
                        call.itf = false;
                        rewrites++;
                        continue;
                    }
                }

                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESTATIC
                        && call.owner.equals("net/minecraft/ChatFormatting")
                        && call.name.equals("m_126657_")
                        && call.desc.equals("(Ljava/lang/String;)Lnet/minecraft/ChatFormatting;")) {
                    call.owner = "com/fuckingdeveloper/lms/runtime/chat/Forge1192ChatFormattingBridge";
                    call.name = "byName";
                    rewrites++;
                    continue;
                }

                // Item#isEdible read the legacy item's default food properties.
                // In 26.3 that intent is represented by the bound FOOD component.
                if (call.owner.equals("net/minecraft/world/item/Item")
                        && call.name.equals("m_41472_")
                        && call.desc.equals("()Z")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192ItemBridge";
                    call.name = "isEdible";
                    call.desc = "(Lnet/minecraft/world/item/Item;)Z";
                    call.itf = false;
                    rewrites++;
                    continue;
                }

                // Forge 1.19.2 ItemStack#is(Item) (m_150930_) compared the
                // stack's concrete item by identity. Target 26.3 removed that
                // overload while retaining ItemStack#getItem, so preserve the
                // exact source member rather than broadening it to a predicate.
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("net/minecraft/world/item/ItemStack")
                        && call.name.equals("m_150930_")
                        && call.desc.equals("(Lnet/minecraft/world/item/Item;)Z")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192ItemBridge";
                    call.name = "is";
                    call.desc = "(Lnet/minecraft/world/item/ItemStack;"
                            + "Lnet/minecraft/world/item/Item;)Z";
                    call.itf = false;
                    rewrites++;
                    continue;
                }
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("net/minecraft/world/item/ItemStack")
                        && call.name.equals("m_41778_")
                        && call.desc.equals("()Ljava/lang/String;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192ItemBridge";
                    call.name = "getDescriptionId";
                    call.desc = "(Lnet/minecraft/world/item/ItemStack;)Ljava/lang/String;";
                    call.itf = false;
                    rewrites++;
                    continue;
                }
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("net/minecraft/world/item/ItemStack")
                        && call.name.equals("m_41739_")
                        && call.desc.equals("(Lnet/minecraft/nbt/CompoundTag;)"
                                + "Lnet/minecraft/nbt/CompoundTag;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192ItemBridge";
                    call.name = "save";
                    call.desc = "(Lnet/minecraft/world/item/ItemStack;"
                            + "Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;";
                    call.itf = false;
                    rewrites++;
                    continue;
                }
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("net/minecraft/world/item/Item")
                        && call.name.equals("m_5671_")
                        && call.desc.equals("(Lnet/minecraft/world/item/ItemStack;)Ljava/lang/String;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192ItemBridge";
                    call.name = "getDescriptionId";
                    call.desc = "(Lnet/minecraft/world/item/Item;"
                            + "Lnet/minecraft/world/item/ItemStack;)Ljava/lang/String;";
                    call.itf = false;
                    rewrites++;
                    continue;
                }

                // Forge 1.19.2 ItemStack#addTagElement mutated the stack's
                // arbitrary NBT payload. Target 26.3 preserves that payload in
                // the CUSTOM_DATA component and requires copy-on-write update.
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("net/minecraft/world/item/ItemStack")
                        && call.name.equals("m_41700_")
                        && call.desc.equals("(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)V")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192ItemBridge";
                    call.name = "addTagElement";
                    call.desc = "(Lnet/minecraft/world/item/ItemStack;Ljava/lang/String;"
                            + "Lnet/minecraft/nbt/Tag;)V";
                    call.itf = false;
                    rewrites++;
                    continue;
                }

                // Forge 1.19.2 exposed arbitrary stack NBT as a live mutable
                // CompoundTag. The target stores that payload in CUSTOM_DATA;
                // these exact source members use the bridge's live view so
                // mutations are not lost through detached component copies.
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("net/minecraft/world/item/ItemStack")) {
                    String bridgeName = null;
                    String bridgeDesc = null;
                    if (call.name.equals("m_41782_") && call.desc.equals("()Z")) {
                        bridgeName = "hasTag";
                        bridgeDesc = "(Lnet/minecraft/world/item/ItemStack;)Z";
                    } else if (call.name.equals("m_41783_")
                            && call.desc.equals("()Lnet/minecraft/nbt/CompoundTag;")) {
                        bridgeName = "getTag";
                        bridgeDesc = "(Lnet/minecraft/world/item/ItemStack;)"
                                + "Lnet/minecraft/nbt/CompoundTag;";
                    } else if (call.name.equals("m_41784_")
                            && call.desc.equals("()Lnet/minecraft/nbt/CompoundTag;")) {
                        bridgeName = "getOrCreateTag";
                        bridgeDesc = "(Lnet/minecraft/world/item/ItemStack;)"
                                + "Lnet/minecraft/nbt/CompoundTag;";
                    } else if (call.name.equals("m_41698_")
                            && call.desc.equals("(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;")) {
                        bridgeName = "getOrCreateTagElement";
                        bridgeDesc = "(Lnet/minecraft/world/item/ItemStack;Ljava/lang/String;)"
                                + "Lnet/minecraft/nbt/CompoundTag;";
                    } else if (call.name.equals("m_41737_")
                            && call.desc.equals("(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;")) {
                        bridgeName = "getTagElement";
                        bridgeDesc = "(Lnet/minecraft/world/item/ItemStack;Ljava/lang/String;)"
                                + "Lnet/minecraft/nbt/CompoundTag;";
                    } else if (call.name.equals("m_41749_")
                            && call.desc.equals("(Ljava/lang/String;)V")) {
                        bridgeName = "removeTagKey";
                        bridgeDesc = "(Lnet/minecraft/world/item/ItemStack;Ljava/lang/String;)V";
                    } else if (call.name.equals("m_41751_")
                            && call.desc.equals("(Lnet/minecraft/nbt/CompoundTag;)V")) {
                        bridgeName = "setTag";
                        bridgeDesc = "(Lnet/minecraft/world/item/ItemStack;"
                                + "Lnet/minecraft/nbt/CompoundTag;)V";
                    }
                    if (bridgeName != null) {
                        call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                        call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192ItemBridge";
                        call.name = bridgeName;
                        call.desc = bridgeDesc;
                        call.itf = false;
                        rewrites++;
                        continue;
                    }
                }

                // Forge 1.19.2 maintained a client-only WoodType -> sign
                // material cache in Sheets. Minecraft 26.3 removed that cache
                // and renders registered sign block/model resources directly.
                // Preserve the source lifecycle operation behind an explicit
                // bridge, including when it appears as a method reference.
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESTATIC
                        && call.owner.equals("net/minecraft/client/renderer/Sheets")
                        && call.name.equals("addWoodType")
                        && call.desc.equals("(Lnet/minecraft/world/level/block/state/properties/WoodType;)V")) {
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192VanillaConstructionBridge";
                    call.name = "legacyAddWoodType";
                    call.itf = false;
                    rewrites++;
                }

                // Registry is an interface in the target runtime. Its static
                // register method therefore requires an InterfaceMethodref,
                // even though the invocation opcode remains INVOKESTATIC.
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESTATIC
                        && call.owner.equals("net/minecraft/core/Registry")
                        && call.name.equals("m_122965_")
                        && call.desc.equals("(Lnet/minecraft/core/Registry;"
                                + "Lnet/minecraft/resources/Identifier;Ljava/lang/Object;)"
                                + "Ljava/lang/Object;")) {
                    call.name = "register";
                    call.itf = true;
                    rewrites++;
                    continue;
                }

                // BlockBehaviour#m_60590_ was the exact 1.19.2 SRG identity
                // for the receiver's default material/map color. The target
                // retains that no-context semantic on the same inheritance
                // chain as BlockBehaviour#defaultMapColor.
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("net/minecraft/world/level/block/Block")
                        && call.name.equals("m_60590_")
                        && call.desc.equals("()Lnet/minecraft/world/level/material/MapColor;")) {
                    call.name = "defaultMapColor";
                    rewrites++;
                }

                // Mojang corrected the historical noCollission spelling while
                // retaining its behavior and fluent descriptor.
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("net/minecraft/world/level/block/state/BlockBehaviour$Properties")
                        && call.name.equals("m_60910_")
                        && call.desc.equals("()Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;")) {
                    call.name = "noCollision";
                    rewrites++;
                }

                // 1.19.2 custom WoodType creation supplied only its semantic
                // name. The target's one-argument BlockSetType constructor is
                // the canonical generic-wood default for the newly required
                // behavior record; preserve the source name through that path.
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESTATIC
                        && call.owner.equals("net/minecraft/world/level/block/state/properties/WoodType")
                        && call.name.equals("create")
                        && call.desc.equals("(Ljava/lang/String;)"
                                + "Lnet/minecraft/world/level/block/state/properties/WoodType;")) {
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192VanillaConstructionBridge";
                    call.name = "legacyWoodType";
                    call.itf = false;
                    rewrites++;
                }

                // The 1.19.2 record accessor's SRG name is field-shaped and
                // therefore outside the normal m_* resolver. Its exact owner
                // and return type identify TagKey#location unambiguously.
                if (call.owner.equals("net/minecraft/tags/TagKey")
                        && call.name.equals("f_203868_")
                        && call.desc.equals("()Lnet/minecraft/resources/Identifier;")) {
                    call.name = "location";
                    rewrites++;
                }

                // ResourceLocation#getPath was projected to Identifier in the
                // target namespace; retain the exact 1.19.2 SRG member identity.
                if (call.owner.equals("net/minecraft/resources/Identifier")
                        && call.name.equals("m_135827_")
                        && call.desc.equals("()Ljava/lang/String;")) {
                    call.name = "getNamespace";
                    rewrites++;
                }

                if (call.owner.equals("net/minecraft/resources/Identifier")
                        && call.name.equals("m_135815_")
                        && call.desc.equals("()Ljava/lang/String;")) {
                    call.name = "getPath";
                    rewrites++;
                }

                // ResourceLocation#tryParse retained its nullable parsing
                // contract after ResourceLocation became Identifier. The
                // 1.19.2 mappings identify m_135820_ as tryParse; adapting the
                // exact owner, SRG member and descriptor avoids treating an
                // arbitrary String factory as equivalent.
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESTATIC
                        && call.owner.equals("net/minecraft/resources/Identifier")
                        && call.name.equals("m_135820_")
                        && call.desc.equals("(Ljava/lang/String;)Lnet/minecraft/resources/Identifier;")) {
                    call.name = "tryParse";
                    rewrites++;
                }

                // Minecraft 1.19.2 Minecraft#useFancyGraphics (m_91405_)
                // selected foliage render behavior from the global graphics
                // tier. Target 26.3 represents that surviving choice as the
                // independently configurable cutout-leaves option.
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESTATIC
                        && call.owner.equals("net/minecraft/client/Minecraft")
                        && call.name.equals("m_91405_")
                        && call.desc.equals("()Z")) {
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192ClientGraphicsBridge";
                    call.name = "useFancyGraphics";
                    call.itf = false;
                    rewrites++;
                }

                // ResourceKey#location became ResourceKey#identifier together
                // with ResourceLocation -> Identifier. Keep the exact 1.19.2
                // SRG identity and require the exact target member.
                if (call.owner.equals("net/minecraft/resources/ResourceKey")
                        && call.name.equals("m_135782_")
                        && call.desc.equals("()Lnet/minecraft/resources/Identifier;")
                        && VANILLA_RELOCATIONS.classifyMethod(
                                "net.minecraft.resources.ResourceKey", "identifier",
                                "()Lnet/minecraft/resources/Identifier;").status()
                                == Forge1192MappingLayer.Status.VERIFIED_IDENTITY) {
                    call.name = "identifier";
                    rewrites++;
                }

                // The legacy flag disabled anvil combination repair. That
                // surviving semantic is named setNoCombineRepair in 26.3.
                if (call.owner.equals("net/minecraft/world/item/Item$Properties")
                        && call.name.equals("setNoRepair")
                        && call.desc.equals("()Lnet/minecraft/world/item/Item$Properties;")) {
                    call.name = "setNoCombineRepair";
                    rewrites++;
                }

                if (call.owner.equals("net/minecraft/world/food/FoodProperties$Builder")) {
                    String bridgeName = null;
                    String bridgeDesc = null;
                    if (call.name.equals("m_38766_")
                            && call.desc.equals("()Lnet/minecraft/world/food/FoodProperties$Builder;")) {
                        bridgeName = "fastFood";
                        bridgeDesc = "(Lnet/minecraft/world/food/FoodProperties$Builder;)"
                                + "Lnet/minecraft/world/food/FoodProperties$Builder;";
                    } else if (call.name.equals("m_38765_")
                            && call.desc.equals("()Lnet/minecraft/world/food/FoodProperties$Builder;")) {
                        call.name = "alwaysEdible";
                        rewrites++;
                    } else if (call.name.equals("m_38758_")
                            && call.desc.equals("(F)Lnet/minecraft/world/food/FoodProperties$Builder;")) {
                        call.name = "saturationModifier";
                        rewrites++;
                    } else if (call.name.equals("effect")
                            && call.desc.equals("(Ljava/util/function/Supplier;F)"
                                    + "Lnet/minecraft/world/food/FoodProperties$Builder;")) {
                        bridgeName = "foodEffect";
                        bridgeDesc = "(Lnet/minecraft/world/food/FoodProperties$Builder;"
                                + "Ljava/util/function/Supplier;F)Lnet/minecraft/world/food/FoodProperties$Builder;";
                    } else if (call.name.equals("m_38767_")
                            && call.desc.equals("()Lnet/minecraft/world/food/FoodProperties;")) {
                        bridgeName = "buildFood";
                        bridgeDesc = "(Lnet/minecraft/world/food/FoodProperties$Builder;)"
                                + "Lnet/minecraft/world/food/FoodProperties;";
                    }
                    if (bridgeName != null) {
                        call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                        call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192VanillaConstructionBridge";
                        call.name = bridgeName;
                        call.desc = bridgeDesc;
                        call.itf = false;
                        rewrites++;
                    }
                }

                if (call.owner.equals("net/minecraft/world/item/Item$Properties")
                        && call.name.equals("food")
                        && call.desc.equals("(Lnet/minecraft/world/food/FoodProperties;)"
                                + "Lnet/minecraft/world/item/Item$Properties;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192VanillaConstructionBridge";
                    call.desc = "(Lnet/minecraft/world/item/Item$Properties;Lnet/minecraft/world/food/FoodProperties;)"
                            + "Lnet/minecraft/world/item/Item$Properties;";
                    call.itf = false;
                    rewrites++;
                }

                // Builder#build migrated from an untyped legacy DFU name to
                // the corresponding EntityType ResourceKey. Match the exact
                // 1.19.2 SRG member, not merely its descriptor.
                if (call.owner.equals("net/minecraft/world/entity/EntityType$Builder")
                        && call.name.equals("m_20712_")
                        && call.desc.equals("(Ljava/lang/String;)Lnet/minecraft/world/entity/EntityType;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192VanillaConstructionBridge";
                    call.name = "entityType";
                    call.desc = "(Lnet/minecraft/world/entity/EntityType$Builder;Ljava/lang/String;)Lnet/minecraft/world/entity/EntityType;";
                    call.itf = false;
                    rewrites++;
                }

                // javac records the concrete receiver class for inherited
                // calls. Walk the managed hierarchy to the declaring vanilla
                // ancestor, resolve the exact SRG identity there, and retain
                // the verified managed receiver for virtual dispatch.
                if (!call.owner.startsWith("net/minecraft/") && call.name.startsWith("m_")) {
                    var inherited = resolveInheritedVanillaMethod(call.owner, call.name, call.desc);
                    if (inherited != null) {
                        call.name = inherited.name();
                        call.desc = inherited.descriptor();
                        rewrites++;
                    }
                }
                if (call.owner.equals("net/minecraft/world/level/block/state/BlockState")) {
                    String inheritedName = switch (call.name) {
                        case "m_61122_" -> "cycle";
                        case "m_61147_" -> "getProperties";
                        case "m_61138_" -> "hasProperty";
                        case "m_61143_" -> "getValue";
                        case "m_61145_" -> "getOptionalValue";
                        case "m_61124_" -> "setValue";
                        default -> null;
                    };
                    if (inheritedName != null) {
                        call.name = inheritedName;
                        rewrites++;
                    }
                }

                if (call.owner.equals("net/minecraft/world/level/block/state/BlockState")
                        && call.name.equals("canSustainPlant")
                        && call.desc.equals("(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lcom/fuckingdeveloper/lms/runtime/block/LegacyIPlantable;)Z")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/block/LegacyIPlantable";
                    call.desc = "(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lcom/fuckingdeveloper/lms/runtime/block/LegacyIPlantable;)Z";
                    call.itf = true;
                    rewrites++;
                }
                var resolved = resolveRelocatedVanillaMethod(call.owner, call.name, call.desc);
                if (resolved == null) {
                    resolved = SRG_RUNTIME_RESOLVER.get().resolve(call.owner, call.name, call.desc);
                }
                if (!resolved.resolved() && call.name.startsWith("m_")) {
                    var inherited = resolveInheritedVanillaMethod(call.owner, call.name, call.desc);
                    if (inherited != null) resolved = inherited;
                }
                if (!resolved.resolved()) {
                    resolved = SRG_RUNTIME_RESOLVER.get().resolveCovariantReturn(
                            call.owner, call.name, call.desc);
                }
                if (!resolved.resolved()) continue;
                if (!call.owner.equals(resolved.owner()) || !call.name.equals(resolved.name())
                        || !call.desc.equals(resolved.descriptor())) {
                    call.owner = resolved.owner();
                    call.name = resolved.name();
                    call.desc = resolved.descriptor();
                    rewrites++;
                }
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        || call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEINTERFACE) {
                    var targetInterface = runtimeOwnerIsInterface(call.owner);
                    if (targetInterface.isPresent()
                            && targetInterface.get() != (call.getOpcode()
                                    == org.objectweb.asm.Opcodes.INVOKEINTERFACE)) {
                        call.setOpcode(targetInterface.get()
                                ? org.objectweb.asm.Opcodes.INVOKEINTERFACE
                                : org.objectweb.asm.Opcodes.INVOKEVIRTUAL);
                        call.itf = targetInterface.get();
                        rewrites++;
                    }
                }
            }
        }
        return rewrites;
    }

    /**
     * Minecraft 26.3 replaced the source-position argument carried by direct
     * 1.19.2 neighbour notifications with a nullable redstone
     * {@code Orientation}. The target contract explicitly accepts
     * {@code null} for notifications that do not originate in the target's
     * experimental redstone ordering model. Preserve the notification and its
     * changed-block identity while discarding only the source-generation
     * position value that the target API no longer represents.
     *
     * <p>This rewrite is selected by the exact Forge 1.19.2 SRG member,
     * descriptor and a verified {@code Level} inheritance relationship. A
     * descriptor coincidence on an unrelated owner is deliberately
     * insufficient.</p>
     */
    private int rewriteLegacyNeighborUpdateFamily(ClassNode node) {
        final String level = "net/minecraft/world/level/Level";
        final String blockPos = "Lnet/minecraft/core/BlockPos;";
        final String block = "Lnet/minecraft/world/level/block/Block;";
        final String direction = "Lnet/minecraft/core/Direction;";
        final String state = "Lnet/minecraft/world/level/block/state/BlockState;";
        final String orientation = "Lnet/minecraft/world/level/redstone/Orientation;";
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        || !isManagedSubtypeOf(call.owner, level)) continue;

                if (call.name.equals("m_46672_")
                        && call.desc.equals("(" + blockPos + block + ")V")) {
                    method.instructions.insertBefore(call, new org.objectweb.asm.tree.InsnNode(
                            org.objectweb.asm.Opcodes.ACONST_NULL));
                    call.name = "updateNeighborsAt";
                    call.desc = "(" + blockPos + block + orientation + ")V";
                    rewrites++;
                    continue;
                }
                if (call.name.equals("m_46590_")
                        && call.desc.equals("(" + blockPos + block + direction + ")V")) {
                    method.instructions.insertBefore(call, new org.objectweb.asm.tree.InsnNode(
                            org.objectweb.asm.Opcodes.ACONST_NULL));
                    call.name = "updateNeighborsAtExceptFromFacing";
                    call.desc = "(" + blockPos + block + direction + orientation + ")V";
                    rewrites++;
                    continue;
                }
                if (call.name.equals("m_46586_")
                        && call.desc.equals("(" + blockPos + block + blockPos + ")V")) {
                    var adaptation = new org.objectweb.asm.tree.InsnList();
                    adaptation.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.POP));
                    adaptation.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.ACONST_NULL));
                    method.instructions.insertBefore(call, adaptation);
                    call.name = "neighborChanged";
                    call.desc = "(" + blockPos + block + orientation + ")V";
                    rewrites++;
                    continue;
                }
                if (call.name.equals("m_213960_")
                        && call.desc.equals("(" + state + blockPos + block + blockPos + "Z)V")) {
                    var adaptation = new org.objectweb.asm.tree.InsnList();
                    adaptation.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.SWAP));
                    adaptation.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.POP));
                    adaptation.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.ACONST_NULL));
                    adaptation.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.SWAP));
                    method.instructions.insertBefore(call, adaptation);
                    call.name = "neighborChanged";
                    call.desc = "(" + state + blockPos + block + orientation + "Z)V";
                    rewrites++;
                }
            }
        }
        return rewrites;
    }

    /**
     * Adapt the source-generation fluent vertex writer to the target 26.3
     * channel names. Position narrows from double to the target's float ABI;
     * the bridge performs that conversion explicitly. Target BufferBuilder
     * finalizes a vertex when the next position starts or the mesh is built,
     * so the removed source endVertex operation has no additional target-side
     * action after all channels have been written.
     */
    private static int rewriteLegacyVertexConsumerFamily(ClassNode node) {
        final String owner = "com/mojang/blaze3d/vertex/VertexConsumer";
        final String result = "L" + owner + ";";
        final String bridge =
                "com/fuckingdeveloper/lms/runtime/render/Forge1192VertexConsumerBridge";
        final java.util.Map<String, String> directNames = java.util.Map.ofEntries(
                java.util.Map.entry("m_6122_(IIII)" + result, "setColor"),
                java.util.Map.entry("m_193479_(I)" + result, "setColor"),
                java.util.Map.entry("m_85950_(FFFF)" + result, "setColor"),
                java.util.Map.entry("m_7421_(FF)" + result, "setUv"),
                java.util.Map.entry("m_7122_(II)" + result, "setUv1"),
                java.util.Map.entry("m_7120_(II)" + result, "setUv2"),
                java.util.Map.entry("m_85969_(I)" + result, "setLight"),
                java.util.Map.entry("m_86008_(I)" + result, "setOverlay"),
                java.util.Map.entry("m_5601_(FFF)" + result, "setNormal"));
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKEINTERFACE
                        || !call.owner.equals(owner)) continue;
                if (call.name.equals("m_5483_") && call.desc.equals("(DDD)" + result)) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = bridge;
                    call.name = "addVertex";
                    call.desc = "(" + result + "DDD)" + result;
                    call.itf = false;
                    rewrites++;
                    continue;
                }
                if (call.name.equals("m_5752_") && call.desc.equals("()V")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = bridge;
                    call.name = "endVertex";
                    call.desc = "(" + result + ")V";
                    call.itf = false;
                    rewrites++;
                    continue;
                }
                String targetName = directNames.get(call.name + call.desc);
                if (targetName == null) continue;
                call.name = targetName;
                rewrites++;
            }
        }
        return rewrites;
    }

    private static java.util.Optional<Boolean> runtimeOwnerIsInterface(String owner) {
        if (!SafeFrameClassWriter.isParentRuntimeType(owner)) return java.util.Optional.empty();
        try {
            Class<?> type = Class.forName(owner.replace('/', '.'), false,
                    Forge1192LegacyClassTransformer.class.getClassLoader());
            return java.util.Optional.of(type.isInterface());
        } catch (ClassNotFoundException | LinkageError ignored) {
            return java.util.Optional.empty();
        }
    }

    private static Forge1192SrgRuntimeResolver.Resolution resolveRelocatedVanillaMethod(
            String targetOwner, String name, String targetDescriptor) {
        if (!name.startsWith("m_") || SRG_RUNTIME_RESOLVER.isEmpty()) return null;
        var sources = EXPLICIT_VANILLA_CLASS_RELOCATIONS.entrySet().stream()
                .filter(relocation -> relocation.getValue().equals(targetOwner))
                .toList();
        if (sources.size() > 1) return null;
        String sourceOwner = sources.isEmpty() ? targetOwner : sources.getFirst().getKey();
        String sourceDescriptor = reverseExplicitVanillaRelocations(targetDescriptor);
        if (sourceOwner.equals(targetOwner) && sourceDescriptor.equals(targetDescriptor)) return null;
        return SRG_RUNTIME_RESOLVER.get().resolveRelocatedMethod(
                sourceOwner, name, sourceDescriptor, targetOwner, targetDescriptor);
    }

    private static String reverseExplicitVanillaRelocations(String descriptor) {
        String source = descriptor;
        for (var relocation : EXPLICIT_VANILLA_CLASS_RELOCATIONS.entrySet()) {
            source = source.replace(relocation.getValue(), relocation.getKey());
        }
        return source.replace(
                "net/minecraft/resources/Identifier",
                "net/minecraft/resources/ResourceLocation");
    }

    /**
     * Minecraft 26.3 supplies the queried AABB to view-blocking predicates.
     * A 1.19.2 StatePredicate could not observe that additional context, so
     * preserve its exact behavior through the runtime adapter.
     */
    private static int rewriteLegacyViewBlockingPredicate(ClassNode node) {
        final String owner =
                "net/minecraft/world/level/block/state/BlockBehaviour$Properties";
        final String oldDesc =
                "(Lnet/minecraft/world/level/block/state/BlockBehaviour$StatePredicate;)"
                        + "Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;";
        final String bridgeDesc = "(L" + owner + ";"
                + "Lnet/minecraft/world/level/block/state/BlockBehaviour$StatePredicate;)"
                + "Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;";
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        || !call.owner.equals(owner)
                        || !call.name.equals("m_60971_")
                        || !call.desc.equals(oldDesc)) continue;
                call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192VanillaConstructionBridge";
                call.name = "isViewBlocking";
                call.desc = bridgeDesc;
                call.itf = false;
                rewrites++;
            }
        }
        return rewrites;
    }

    /**
     * The modern emissive predicate receives only BlockState. Adapt a legacy
     * three-argument StatePredicate only when its implementation bytecode
     * proves that BlockGetter and BlockPos are unused.
     */
    private static int rewriteLegacyStateOnlyEmissivePredicates(ClassNode node) {
        final String legacyPredicate = "(Lnet/minecraft/world/level/block/state/BlockState;"
                + "Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z";
        final String oldCall = "(Lnet/minecraft/world/level/block/state/BlockBehaviour$StatePredicate;)"
                + "Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;";
        int rewrites = 0;
        java.util.List<org.objectweb.asm.tree.MethodNode> adapters = new java.util.ArrayList<>();
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        || !call.owner.equals("net/minecraft/world/level/block/state/BlockBehaviour$Properties")
                        || !call.name.equals("m_60991_") || !call.desc.equals(oldCall)) continue;
                var producer = previousExecutable(call);
                if (!(producer instanceof InvokeDynamicInsnNode indy)
                        || !indy.desc.equals("()Lnet/minecraft/world/level/block/state/BlockBehaviour$StatePredicate;")
                        || indy.bsmArgs.length < 3
                        || !(indy.bsmArgs[1] instanceof Handle implementation)
                        || implementation.getTag() != org.objectweb.asm.Opcodes.H_INVOKESTATIC
                        || !implementation.getOwner().equals(node.name)
                        || !implementation.getDesc().equals(legacyPredicate)) continue;
                var target = node.methods.stream()
                        .filter(candidate -> candidate.name.equals(implementation.getName())
                                && candidate.desc.equals(legacyPredicate)
                                && (candidate.access & org.objectweb.asm.Opcodes.ACC_STATIC) != 0)
                        .findFirst().orElse(null);
                if (target == null || usesPredicateContextArguments(target)) continue;

                String adapterName = "$lms$emissivePredicate$" + adapters.size();
                var adapter = new org.objectweb.asm.tree.MethodNode(
                        org.objectweb.asm.Opcodes.ACC_PRIVATE | org.objectweb.asm.Opcodes.ACC_STATIC
                                | org.objectweb.asm.Opcodes.ACC_SYNTHETIC,
                        adapterName, "(Lnet/minecraft/world/level/block/state/BlockState;)Z", null, null);
                adapter.instructions.add(new org.objectweb.asm.tree.VarInsnNode(
                        org.objectweb.asm.Opcodes.ALOAD, 0));
                adapter.instructions.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.ACONST_NULL));
                adapter.instructions.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.ACONST_NULL));
                adapter.instructions.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC,
                        node.name, implementation.getName(), legacyPredicate, false));
                adapter.instructions.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.IRETURN));
                adapter.maxStack = 3;
                adapter.maxLocals = 1;
                adapters.add(adapter);

                indy.desc = "()Ljava/util/function/Predicate;";
                indy.bsmArgs[0] = Type.getMethodType("(Ljava/lang/Object;)Z");
                indy.bsmArgs[1] = new Handle(org.objectweb.asm.Opcodes.H_INVOKESTATIC,
                        node.name, adapterName,
                        "(Lnet/minecraft/world/level/block/state/BlockState;)Z", false);
                indy.bsmArgs[2] = Type.getMethodType(
                        "(Lnet/minecraft/world/level/block/state/BlockState;)Z");
                call.name = "emissiveRendering";
                call.desc = "(Ljava/util/function/Predicate;)"
                        + "Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;";
                rewrites++;
            }
        }
        node.methods.addAll(adapters);
        return rewrites;
    }

    private static boolean usesPredicateContextArguments(org.objectweb.asm.tree.MethodNode method) {
        for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof org.objectweb.asm.tree.VarInsnNode variable
                    && (variable.var == 1 || variable.var == 2)) return true;
            if (insn instanceof org.objectweb.asm.tree.IincInsnNode increment
                    && (increment.var == 1 || increment.var == 2)) return true;
        }
        return false;
    }

    private Forge1192SrgRuntimeResolver.Resolution resolveCanonicalBlockMethod(String name, String desc) {
        if (!name.startsWith("m_") || SRG_RUNTIME_RESOLVER.isEmpty()) return null;
        var resolved = SRG_RUNTIME_RESOLVER.get().resolve(
                "net/minecraft/world/level/block/Block", name, desc);
        return resolved.resolved()
                && resolved.owner().equals("net/minecraft/world/level/block/Block")
                ? resolved : null;
    }

    private Forge1192SrgRuntimeResolver.Resolution resolveInheritedVanillaMethod(
            String receiver, String name, String desc) {
        if (SRG_RUNTIME_RESOLVER.isEmpty()) return null;
        java.util.ArrayDeque<String> pending = new java.util.ArrayDeque<>();
        java.util.HashSet<String> seen = new java.util.HashSet<>();
        pending.add(receiver);
        while (!pending.isEmpty()) {
            String current = pending.removeFirst();
            if (!seen.add(current)) continue;
            if (current.startsWith("net/minecraft/")) {
                var resolved = SRG_RUNTIME_RESOLVER.get().resolve(current, name, desc);
                if (resolved.resolved()) return resolved;
                String targetOwner = migrateInternalName(current);
                String targetDescriptor = migrateDescriptor(desc);
                if (!targetOwner.equals(current) || !targetDescriptor.equals(desc)) {
                    resolved = SRG_RUNTIME_RESOLVER.get().resolveRelocatedMethod(
                            current, name, desc, targetOwner, targetDescriptor);
                    if (resolved.resolved()) return resolved;
                }
            }
            try {
                byte[] bytes = hierarchyBytes(current);
                if (bytes == null) {
                    // An explicitly declared structural relocation is valid
                    // hierarchy evidence even when the removed source class
                    // itself is absent from the target runtime.
                    String migrated = migrateInternalName(current);
                    if (!migrated.equals(current)) pending.addLast(migrated);
                    continue;
                }
                ClassReader reader = new ClassReader(bytes);
                if (reader.getSuperName() != null) pending.addLast(reader.getSuperName());
                java.util.Collections.addAll(pending, reader.getInterfaces());
            } catch (java.io.IOException | RuntimeException ignored) {
                // Incomplete hierarchy evidence fails closed.
            }
        }
        return null;
    }

    private byte[] hierarchyBytes(String internalName) throws java.io.IOException {
        byte[] bytes = managedClassBytes == null ? null : managedClassBytes.find(internalName);
        if (bytes != null) return bytes;
        if (!SafeFrameClassWriter.isParentRuntimeType(internalName)) return null;
        try (var stream = Forge1192LegacyClassTransformer.class.getClassLoader()
                .getResourceAsStream(internalName + ".class")) {
            return stream == null ? null : stream.readAllBytes();
        }
    }

    private boolean isManagedSubtypeOf(String candidate, String target) {
        java.util.ArrayDeque<String> pending = new java.util.ArrayDeque<>();
        pending.add(candidate);
        return hierarchyContains(pending, target);
    }

    private boolean isManagedSubtypeOf(ClassNode candidate, String target) {
        if (candidate.name.equals(target)) return true;
        java.util.ArrayDeque<String> pending = new java.util.ArrayDeque<>();
        if (candidate.superName != null) pending.add(candidate.superName);
        pending.addAll(candidate.interfaces);
        return hierarchyContains(pending, target);
    }

    private boolean hierarchyContains(java.util.ArrayDeque<String> pending, String target) {
        java.util.HashSet<String> seen = new java.util.HashSet<>();
        while (!pending.isEmpty()) {
            String current = pending.removeFirst();
            if (!seen.add(current)) continue;
            if (current.equals(target)) return true;
            try {
                byte[] bytes = hierarchyBytes(current);
                if (bytes == null) {
                    // Follow only explicit source-profile relocations. This
                    // lets semantic subtype checks cross removed vanilla
                    // classes without reviving name-based heuristics.
                    String migrated = migrateInternalName(current);
                    if (!migrated.equals(current)) pending.addLast(migrated);
                    continue;
                }
                ClassReader reader = new ClassReader(bytes);
                if (reader.getSuperName() != null) pending.addLast(reader.getSuperName());
                java.util.Collections.addAll(pending, reader.getInterfaces());
            } catch (java.io.IOException | LinkageError | RuntimeException ignored) {
                // Missing/corrupt hierarchy evidence must fail closed.
            }
        }
        return false;
    }

    private int rewriteVerifiedSrgFieldAccesses(ClassNode node) {
        if (SRG_RUNTIME_RESOLVER.isEmpty()) return 0;
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null;) {
                var current = insn;
                insn = insn.getNext();
                if (!(current instanceof FieldInsnNode field)) continue;
                if (field.getOpcode() == org.objectweb.asm.Opcodes.GETSTATIC
                        && field.owner.equals("net/minecraft/world/level/chunk/status/ChunkStatus")
                        && field.name.equals("f_62326_")
                        && field.desc.equals("Lnet/minecraft/world/level/chunk/status/ChunkStatus;")) {
                    field.name = "FULL";
                    rewrites++;
                    continue;
                }
                if (field.getOpcode() == org.objectweb.asm.Opcodes.GETFIELD
                        && field.name.equals("f_19853_")
                        && field.desc.equals("Lnet/minecraft/world/level/Level;")) {
                    String sourceOwner = resolveInheritedLegacyVanillaFieldOwner(
                            field.owner, field.name, field.desc);
                    String canonical = sourceOwner == null ? null : SRG_RUNTIME_RESOLVER.get()
                            .canonicalFieldName(sourceOwner, field.name, field.desc).orElse(null);
                    if ("net/minecraft/world/entity/Entity".equals(sourceOwner)
                            && "level".equals(canonical)) {
                        method.instructions.set(field, new MethodInsnNode(
                                org.objectweb.asm.Opcodes.INVOKEVIRTUAL,
                                "net/minecraft/world/entity/Entity", "level",
                                "()Lnet/minecraft/world/level/Level;", false));
                        rewrites++;
                        continue;
                    }
                }
                if (field.getOpcode() == org.objectweb.asm.Opcodes.GETFIELD
                        && field.owner.equals("net/minecraft/client/Minecraft")
                        && field.name.equals("f_91064_")
                        && field.desc.equals("Lnet/minecraft/client/renderer/debug/DebugRenderer;")) {
                    String canonical = SRG_RUNTIME_RESOLVER.get()
                            .canonicalFieldName(field.owner, field.name, field.desc).orElse(null);
                    if ("debugRenderer".equals(canonical)) {
                        field.name = "debugEntries";
                        field.desc = "Lnet/minecraft/client/gui/components/debug/DebugScreenEntryList;";
                        rewrites++;
                        continue;
                    }
                }
                // Sheets#f_110739_ was the 1.19.2 block-atlas identifier.
                // The exact target value moved to TextureAtlas.LOCATION_BLOCKS.
                if (field.getOpcode() == org.objectweb.asm.Opcodes.GETSTATIC
                        && field.owner.equals("net/minecraft/client/renderer/Sheets")
                        && field.name.equals("f_110739_")
                        && field.desc.equals("Lnet/minecraft/resources/Identifier;")) {
                    field.owner = "net/minecraft/client/renderer/texture/TextureAtlas";
                    field.name = "LOCATION_BLOCKS";
                    rewrites++;
                    continue;
                }
                LegacyCopperCollectionField copperCollection = legacyCopperCollectionField(field);
                if (copperCollection != null) {
                    field.name = copperCollection.collection();
                    field.desc = "Lnet/minecraft/world/level/block/WeatheringCopperCollection;";
                    var select = new org.objectweb.asm.tree.InsnList();
                    select.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKEVIRTUAL,
                            "net/minecraft/world/level/block/WeatheringCopperCollection",
                            copperCollection.track(),
                            "()Lnet/minecraft/world/level/block/WeatheringCopperCollection$ByState;", false));
                    select.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKEVIRTUAL,
                            "net/minecraft/world/level/block/WeatheringCopperCollection$ByState",
                            copperCollection.state(),
                            "()Ljava/lang/Object;", false));
                    select.add(new TypeInsnNode(org.objectweb.asm.Opcodes.CHECKCAST,
                            copperCollection.valueType()));
                    method.instructions.insert(field, select);
                    rewrites++;
                    continue;
                }
                LegacyColorCollectionField colored = legacyColorCollectionField(field);
                if (colored != null) {
                    field.name = colored.collection();
                    field.desc = "Lnet/minecraft/world/level/block/ColorCollection;";
                    var select = new org.objectweb.asm.tree.InsnList();
                    select.add(new FieldInsnNode(org.objectweb.asm.Opcodes.GETSTATIC,
                            "net/minecraft/world/item/DyeColor", colored.color(),
                            "Lnet/minecraft/world/item/DyeColor;"));
                    select.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKEVIRTUAL,
                            "net/minecraft/world/level/block/ColorCollection", "pick",
                            "(Lnet/minecraft/world/item/DyeColor;)Ljava/lang/Object;", false));
                    select.add(new TypeInsnNode(org.objectweb.asm.Opcodes.CHECKCAST,
                            "net/minecraft/world/level/block/Block"));
                    method.instructions.insert(field, select);
                    rewrites++;
                    continue;
                }
                if (field.getOpcode() == org.objectweb.asm.Opcodes.GETSTATIC
                        && field.owner.equals("net/minecraft/world/entity/ai/attributes/Attributes")
                        && field.desc.equals("Lnet/minecraft/world/entity/ai/attributes/Attribute;")) {
                    String canonical = SRG_RUNTIME_RESOLVER.get()
                            .canonicalFieldName(field.owner, field.name, field.desc).orElse(null);
                    if (canonical != null && VANILLA_RELOCATIONS.searchExactField(
                            "net.minecraft.world.entity.ai.attributes.Attributes",
                            canonical, "Lnet/minecraft/core/Holder;").size() == 1) {
                        field.name = canonical;
                        field.desc = "Lnet/minecraft/core/Holder;";
                        rewrites++;
                        continue;
                    }
                }
                String renamedVanillaField = renamedVanillaField(field);
                if (renamedVanillaField != null) {
                    field.name = renamedVanillaField;
                    rewrites++;
                    continue;
                }
                if (field.getOpcode() == org.objectweb.asm.Opcodes.GETSTATIC
                        && field.owner.equals("net/minecraft/sounds/SoundEvents")
                        && field.desc.equals("Lnet/minecraft/sounds/SoundEvent;")) {
                    var resolved = SRG_RUNTIME_RESOLVER.get().resolveRelocatedField(
                            field.owner, field.name, field.desc, field.owner,
                            "Lnet/minecraft/core/Holder;");
                    if (resolved.resolved()) {
                        field.owner = resolved.owner();
                        field.name = resolved.name();
                        field.desc = resolved.descriptor();
                        var unwrap = new org.objectweb.asm.tree.InsnList();
                        unwrap.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKEINTERFACE,
                                "net/minecraft/core/Holder", "value", "()Ljava/lang/Object;", true));
                        unwrap.add(new TypeInsnNode(org.objectweb.asm.Opcodes.CHECKCAST,
                                "net/minecraft/sounds/SoundEvent"));
                        method.instructions.insert(field, unwrap);
                        rewrites++;
                        continue;
                    }
                }
                String mobEffect = field.getOpcode() == org.objectweb.asm.Opcodes.GETSTATIC
                        && field.owner.equals("net/minecraft/world/effect/MobEffects")
                        && field.desc.equals("Lnet/minecraft/world/effect/MobEffect;")
                        ? modernMobEffectField(field.name) : null;
                if (mobEffect != null) {
                    field.name = mobEffect;
                    field.desc = "Lnet/minecraft/core/Holder;";
                    var unwrap = new org.objectweb.asm.tree.InsnList();
                    unwrap.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKEINTERFACE,
                            "net/minecraft/core/Holder", "value", "()Ljava/lang/Object;", true));
                    unwrap.add(new TypeInsnNode(org.objectweb.asm.Opcodes.CHECKCAST,
                            "net/minecraft/world/effect/MobEffect"));
                    method.instructions.insert(field, unwrap);
                    rewrites++;
                    continue;
                }
                String wallSide = legacyWallSidePropertyField(field);
                if (wallSide != null) {
                    field.owner = "net/minecraft/world/level/block/WallBlock";
                    field.name = wallSide;
                    rewrites++;
                    continue;
                }
                String commonTag = renamedCommonTagField(field);
                if (commonTag != null) {
                    field.name = commonTag;
                    rewrites++;
                    continue;
                }
                String cauldronInteractions = legacyCauldronInteractions(field);
                if (cauldronInteractions != null) {
                    method.instructions.set(field, new MethodInsnNode(
                            org.objectweb.asm.Opcodes.INVOKESTATIC,
                            "com/fuckingdeveloper/lms/runtime/Forge1192CauldronBridge",
                            cauldronInteractions, "()Ljava/util/Map;", false));
                    rewrites++;
                    continue;
                }
                String cauldronInteraction = legacyCauldronInteraction(field);
                if (cauldronInteraction != null) {
                    method.instructions.set(field, new MethodInsnNode(
                            org.objectweb.asm.Opcodes.INVOKESTATIC,
                            "com/fuckingdeveloper/lms/runtime/Forge1192CauldronBridge",
                            cauldronInteraction,
                            "()Lnet/minecraft/core/cauldron/CauldronInteraction;", false));
                    rewrites++;
                    continue;
                }
                if (field.getOpcode() == org.objectweb.asm.Opcodes.GETFIELD
                        && field.name.equals("f_40377_")
                        && field.desc.equals("Lnet/minecraft/world/entity/EquipmentSlot;")) {
                    String sourceOwner = resolveInheritedLegacyVanillaFieldOwner(
                            field.owner, field.name, field.desc);
                    String canonical = sourceOwner == null ? null : SRG_RUNTIME_RESOLVER.get()
                            .canonicalFieldName(sourceOwner, field.name, field.desc).orElse(null);
                    if (sourceOwner != null
                            && sourceOwner.equals("net/minecraft/world/item/ArmorItem")
                            && "slot".equals(canonical)) {
                        method.instructions.set(field, new MethodInsnNode(
                                org.objectweb.asm.Opcodes.INVOKEVIRTUAL,
                                "com/fuckingdeveloper/lms/runtime/item/LegacyArmorItem",
                                "lmsLegacyArmorSlot",
                                "()Lnet/minecraft/world/entity/EquipmentSlot;", false));
                        rewrites++;
                        continue;
                    }
                }
                if (field.getOpcode() == org.objectweb.asm.Opcodes.GETSTATIC
                        && field.name.equals("f_51181_")
                        && field.desc.equals("[Lnet/minecraft/world/phys/shapes/VoxelShape;")) {
                    String sourceOwner = resolveInheritedLegacyVanillaFieldOwner(
                            field.owner, field.name, field.desc);
                    String canonical = sourceOwner == null ? null : SRG_RUNTIME_RESOLVER.get()
                            .canonicalFieldName(sourceOwner, field.name, field.desc).orElse(null);
                    var target = VANILLA_RELOCATIONS.searchExactField(
                            "net.minecraft.world.level.block.CakeBlock", "SHAPES", field.desc);
                    if (sourceOwner != null
                            && sourceOwner.equals("net/minecraft/world/level/block/CakeBlock")
                            && "SHAPE_BY_BITE".equals(canonical)
                            && target.size() == 1) {
                        field.owner = "net/minecraft/world/level/block/CakeBlock";
                        field.name = "SHAPES";
                        rewrites++;
                        continue;
                    }
                }
                if (field.getOpcode() == org.objectweb.asm.Opcodes.GETSTATIC
                        && field.owner.equals("net/minecraft/world/entity/EntityType")
                        && field.desc.equals("Lnet/minecraft/world/entity/EntityType;")) {
                    String canonical = SRG_RUNTIME_RESOLVER.get()
                            .canonicalFieldName(field.owner, field.name, field.desc).orElse(null);
                    if (canonical != null) {
                        var targets = VANILLA_RELOCATIONS.searchExactField(
                                "net.minecraft.world.entity.EntityTypes", canonical, field.desc);
                        if (targets.size() == 1) {
                            field.owner = "net/minecraft/world/entity/EntityTypes";
                            field.name = canonical;
                            rewrites++;
                            continue;
                        }
                    }
                }
                if (field.getOpcode() == org.objectweb.asm.Opcodes.GETFIELD
                        && field.owner.equals("net/minecraft/client/Minecraft")
                        && field.name.equals("f_91080_")
                        && field.desc.equals("Lnet/minecraft/client/gui/screens/Screen;")) {
                    String canonical = SRG_RUNTIME_RESOLVER.get()
                            .canonicalFieldName(field.owner, field.name, field.desc).orElse(null);
                    if ("screen".equals(canonical)) {
                        method.instructions.set(field, new MethodInsnNode(
                                org.objectweb.asm.Opcodes.INVOKESTATIC,
                                "com/fuckingdeveloper/lms/runtime/render/Forge1192MinecraftClientBridge",
                                "screen", "(Lnet/minecraft/client/Minecraft;)"
                                        + "Lnet/minecraft/client/gui/screens/Screen;", false));
                        rewrites++;
                        continue;
                    }
                }
                var resolved = SRG_RUNTIME_RESOLVER.get().resolveField(field.owner, field.name, field.desc);
                if (!resolved.resolved()) {
                    // javac may encode an inherited static field with the
                    // receiver subclass as its constant-pool owner. Resolve
                    // the exact SRG identity along the verified hierarchy for
                    // vanilla subclasses as well as managed subclasses.
                    resolved = resolveInheritedVanillaField(field.owner, field.name, field.desc);
                }
                if (!resolved.resolved()) continue;
                if (!field.owner.equals(resolved.owner()) || !field.name.equals(resolved.name())
                        || !field.desc.equals(resolved.descriptor())) {
                    field.owner = resolved.owner();
                    field.name = resolved.name();
                    field.desc = resolved.descriptor();
                    rewrites++;
                }
            }
        }
        return rewrites;
    }

    private record LegacyColorCollectionField(String collection, String color) {}
    private record LegacyCopperCollectionField(
            String collection, String track, String state, String valueType) {}

    /**
     * Exact Mojang identities occasionally survive with a new target name.
     * Keep these migrations explicit: prove the source SRG identity and the
     * unique target declaration instead of accepting descriptor coincidence.
     */
    private String renamedVanillaField(FieldInsnNode field) {
        if (field.getOpcode() != org.objectweb.asm.Opcodes.GETSTATIC
                || SRG_RUNTIME_RESOLVER.isEmpty()) return null;
        if (field.owner.equals("net/minecraft/world/entity/EquipmentSlot$Type")
                && field.name.equals("ARMOR")
                && field.desc.equals("Lnet/minecraft/world/entity/EquipmentSlot$Type;")) {
            var target = VANILLA_RELOCATIONS.searchExactField(
                    "net.minecraft.world.entity.EquipmentSlot$Type", "HUMANOID_ARMOR",
                    field.desc);
            return target.size() == 1 ? "HUMANOID_ARMOR" : null;
        }
        String canonical = SRG_RUNTIME_RESOLVER.get()
                .canonicalFieldName(field.owner, field.name, field.desc).orElse(null);
        String targetName = switch (field.owner + "#" + (canonical == null ? "" : canonical)) {
            case "net/minecraft/world/level/block/Blocks#GRASS" -> "SHORT_GRASS";
            case "net/minecraft/world/level/block/Blocks#CHAIN" -> "IRON_CHAIN";
            default -> null;
        };
        if (targetName == null) return null;
        var target = VANILLA_RELOCATIONS.searchExactField(
                field.owner.replace('/', '.'), targetName, field.desc);
        return target.size() == 1 ? targetName : null;
    }

    /**
     * NeoForge's common-tag convention renamed a small set of Forge 1.19.2
     * category identities while preserving their meaning.  Require the exact
     * legacy owner/name/descriptor and verify the selected target declaration;
     * unchanged fields continue through ordinary namespace migration.
     */
    private String renamedCommonTagField(FieldInsnNode field) {
        if (field.getOpcode() != org.objectweb.asm.Opcodes.GETSTATIC
                || !field.owner.startsWith("net/neoforged/neoforge/common/Tags$")
                || !field.desc.equals("Lnet/minecraft/tags/TagKey;")) return null;
        String nested = field.owner.substring(field.owner.lastIndexOf('$') + 1);
        String targetName = switch (nested + '#' + field.name) {
            case "Blocks#COBBLESTONE", "Items#COBBLESTONE" -> "COBBLESTONES";
            case "Blocks#GLASS", "Items#GLASS" -> "GLASS_BLOCKS";
            case "Blocks#GRAVEL", "Items#GRAVEL" -> "GRAVELS";
            case "Blocks#SANDSTONE" -> "SANDSTONE_BLOCKS";
            case "Blocks#STONE", "Items#STONE" -> "STONES";
            case "Items#INGOTS_BRICK" -> "BRICKS_NORMAL";
            case "Items#INGOTS_NETHER_BRICK" -> "BRICKS_NETHER";
            case "Items#OBSIDIAN" -> "OBSIDIANS";
            case "Items#SHEARS" -> "TOOLS_SHEAR";
            case "Biomes#IS_WATER" -> "IS_AQUATIC";
            default -> null;
        };
        if (targetName == null) return null;
        try {
            byte[] bytes = hierarchyBytes(field.owner);
            if (bytes == null) return null;
            ClassNode target = new ClassNode();
            new ClassReader(bytes).accept(target,
                    ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            long matches = target.fields.stream()
                    .filter(candidate -> candidate.name.equals(targetName)
                            && candidate.desc.equals(field.desc)
                            && (candidate.access & org.objectweb.asm.Opcodes.ACC_STATIC) != 0)
                    .count();
            return matches == 1 ? targetName : null;
        } catch (java.io.IOException | RuntimeException ignored) {
            return null;
        }
    }

    /**
     * WallBlock's four directional EnumProperty fields kept their semantic
     * identity but dropped the _WALL suffix after 1.19.2. Accept inherited
     * fieldrefs only after proving the managed owner reaches the exact vanilla
     * declaration and the renamed target field exists with the same descriptor.
     */
    private String legacyWallSidePropertyField(FieldInsnNode field) {
        if (field.getOpcode() != org.objectweb.asm.Opcodes.GETSTATIC
                || !field.name.startsWith("f_")
                || !field.desc.equals("Lnet/minecraft/world/level/block/state/properties/EnumProperty;")
                || SRG_RUNTIME_RESOLVER.isEmpty()) return null;
        java.util.ArrayDeque<String> pending = new java.util.ArrayDeque<>();
        java.util.HashSet<String> seen = new java.util.HashSet<>();
        pending.add(field.owner);
        while (!pending.isEmpty()) {
            String current = pending.removeFirst();
            if (!seen.add(current)) continue;
            if (current.equals("net/minecraft/world/level/block/WallBlock")) {
                String canonical = SRG_RUNTIME_RESOLVER.get()
                        .canonicalFieldName(current, field.name, field.desc).orElse(null);
                String modern = switch (canonical == null ? "" : canonical) {
                    case "EAST_WALL" -> "EAST";
                    case "NORTH_WALL" -> "NORTH";
                    case "SOUTH_WALL" -> "SOUTH";
                    case "WEST_WALL" -> "WEST";
                    default -> null;
                };
                if (modern == null) return null;
                var target = VANILLA_RELOCATIONS.searchExactField(
                        "net.minecraft.world.level.block.WallBlock", modern, field.desc);
                return target.size() == 1 ? modern : null;
            }
            try {
                byte[] bytes = hierarchyBytes(current);
                if (bytes == null) continue;
                ClassReader reader = new ClassReader(bytes);
                if (reader.getSuperName() != null) pending.addLast(reader.getSuperName());
                java.util.Collections.addAll(pending, reader.getInterfaces());
            } catch (java.io.IOException | LinkageError | RuntimeException ignored) {
                return null;
            }
        }
        return null;
    }

    private static String legacyCauldronInteractions(FieldInsnNode field) {
        if (field.getOpcode() != org.objectweb.asm.Opcodes.GETSTATIC
                || !field.owner.equals("net/minecraft/core/cauldron/CauldronInteraction")
                || !field.desc.equals("Ljava/util/Map;")
                || SRG_RUNTIME_RESOLVER.isEmpty()) return null;
        String canonical = SRG_RUNTIME_RESOLVER.get()
                .canonicalFieldName(field.owner, field.name, field.desc).orElse(null);
        if (canonical == null) {
            canonical = switch (field.name) {
                case "f_175606_" -> "EMPTY";
                case "f_175607_" -> "WATER";
                case "f_175608_" -> "LAVA";
                case "f_175609_" -> "POWDER_SNOW";
                default -> null;
            };
        }
        String bridgeMethod = switch (canonical == null ? "" : canonical) {
            case "EMPTY" -> "emptyInteractions";
            case "WATER" -> "waterInteractions";
            case "LAVA" -> "lavaInteractions";
            case "POWDER_SNOW" -> "powderSnowInteractions";
            default -> null;
        };
        if (bridgeMethod == null) return null;
        return VANILLA_RELOCATIONS.searchExactField(
                "net.minecraft.core.cauldron.CauldronInteractions", canonical,
                "Lnet/minecraft/core/cauldron/CauldronInteraction$Dispatcher;").size() == 1
                ? bridgeMethod : null;
    }

    /**
     * The six 1.19.2 singleton interactions became named static methods in
     * 26.3. Preserve their stable object-valued field contract through the
     * bridge, keyed by exact owner, descriptor, and SRG/Mojang identity.
     */
    private static String legacyCauldronInteraction(FieldInsnNode field) {
        if (field.getOpcode() != org.objectweb.asm.Opcodes.GETSTATIC
                || !field.owner.equals("net/minecraft/core/cauldron/CauldronInteraction")
                || !field.desc.equals("Lnet/minecraft/core/cauldron/CauldronInteraction;")) return null;
        String canonical = SRG_RUNTIME_RESOLVER.isEmpty() ? null : SRG_RUNTIME_RESOLVER.get()
                .canonicalFieldName(field.owner, field.name, field.desc).orElse(null);
        if (canonical == null) {
            canonical = switch (field.name) {
                case "f_175610_" -> "FILL_WATER";
                case "f_175611_" -> "FILL_LAVA";
                case "f_175612_" -> "FILL_POWDER_SNOW";
                case "f_175613_" -> "SHULKER_BOX";
                case "f_175614_" -> "BANNER";
                case "f_175615_" -> "DYED_ITEM";
                default -> null;
            };
        }
        return switch (canonical == null ? "" : canonical) {
            case "FILL_WATER" -> "fillWater";
            case "FILL_LAVA" -> "fillLava";
            case "FILL_POWDER_SNOW" -> "fillPowderSnow";
            case "SHULKER_BOX" -> "shulkerBox";
            case "BANNER" -> "banner";
            case "DYED_ITEM" -> "dyedItem";
            default -> null;
        };
    }

    /** Minecraft 26.3 consolidated copper stages and wax states into collections. */
    private LegacyCopperCollectionField legacyCopperCollectionField(FieldInsnNode field) {
        if (field.getOpcode() != org.objectweb.asm.Opcodes.GETSTATIC
                || SRG_RUNTIME_RESOLVER.isEmpty()) return null;
        String valueType;
        if (field.owner.equals("net/minecraft/world/level/block/Blocks")
                && field.desc.equals("Lnet/minecraft/world/level/block/Block;")) {
            valueType = "net/minecraft/world/level/block/Block";
        } else if (field.owner.equals("net/minecraft/world/item/Items")
                && field.desc.equals("Lnet/minecraft/world/item/Item;")) {
            valueType = "net/minecraft/world/item/Item";
        } else {
            return null;
        }
        String canonical = SRG_RUNTIME_RESOLVER.get()
                .canonicalFieldName(field.owner, field.name, field.desc).orElse(null);
        if (canonical == null) return null;
        LegacyCopperCollectionField migrated = switch (canonical) {
            case "COPPER_BLOCK" -> new LegacyCopperCollectionField("COPPER_BLOCK", "weathering", "unaffected", valueType);
            case "EXPOSED_COPPER" -> new LegacyCopperCollectionField("COPPER_BLOCK", "weathering", "exposed", valueType);
            case "WEATHERED_COPPER" -> new LegacyCopperCollectionField("COPPER_BLOCK", "weathering", "weathered", valueType);
            case "OXIDIZED_COPPER" -> new LegacyCopperCollectionField("COPPER_BLOCK", "weathering", "oxidized", valueType);
            case "WAXED_COPPER_BLOCK" -> new LegacyCopperCollectionField("COPPER_BLOCK", "waxed", "unaffected", valueType);
            case "WAXED_EXPOSED_COPPER" -> new LegacyCopperCollectionField("COPPER_BLOCK", "waxed", "exposed", valueType);
            case "WAXED_WEATHERED_COPPER" -> new LegacyCopperCollectionField("COPPER_BLOCK", "waxed", "weathered", valueType);
            case "WAXED_OXIDIZED_COPPER" -> new LegacyCopperCollectionField("COPPER_BLOCK", "waxed", "oxidized", valueType);
            case "CUT_COPPER" -> new LegacyCopperCollectionField("CUT_COPPER", "weathering", "unaffected", valueType);
            case "EXPOSED_CUT_COPPER" -> new LegacyCopperCollectionField("CUT_COPPER", "weathering", "exposed", valueType);
            case "WEATHERED_CUT_COPPER" -> new LegacyCopperCollectionField("CUT_COPPER", "weathering", "weathered", valueType);
            case "OXIDIZED_CUT_COPPER" -> new LegacyCopperCollectionField("CUT_COPPER", "weathering", "oxidized", valueType);
            case "WAXED_CUT_COPPER" -> new LegacyCopperCollectionField("CUT_COPPER", "waxed", "unaffected", valueType);
            case "WAXED_EXPOSED_CUT_COPPER" -> new LegacyCopperCollectionField("CUT_COPPER", "waxed", "exposed", valueType);
            case "WAXED_WEATHERED_CUT_COPPER" -> new LegacyCopperCollectionField("CUT_COPPER", "waxed", "weathered", valueType);
            case "WAXED_OXIDIZED_CUT_COPPER" -> new LegacyCopperCollectionField("CUT_COPPER", "waxed", "oxidized", valueType);
            default -> null;
        };
        if (migrated == null) return null;
        var target = VANILLA_RELOCATIONS.searchExactField(
                field.owner.replace('/', '.'), migrated.collection(),
                "Lnet/minecraft/world/level/block/WeatheringCopperCollection;");
        return target.size() == 1 ? migrated : null;
    }

    /** Minecraft 26.3 consolidated exact color-specific block fields. */
    private LegacyColorCollectionField legacyColorCollectionField(FieldInsnNode field) {
        if (field.getOpcode() != org.objectweb.asm.Opcodes.GETSTATIC
                || !field.owner.equals("net/minecraft/world/level/block/Blocks")
                || !field.desc.equals("Lnet/minecraft/world/level/block/Block;")
                || SRG_RUNTIME_RESOLVER.isEmpty()) return null;
        String canonical = SRG_RUNTIME_RESOLVER.get()
                .canonicalFieldName(field.owner, field.name, field.desc).orElse(null);
        if (canonical == null) return null;
        String color = null;
        String suffix = null;
        for (String candidate : java.util.List.of(
                "LIGHT_BLUE", "LIGHT_GRAY", "WHITE", "ORANGE", "MAGENTA", "YELLOW",
                "LIME", "PINK", "GRAY", "CYAN", "PURPLE", "BLUE", "BROWN",
                "GREEN", "RED", "BLACK")) {
            if (!canonical.startsWith(candidate + "_")) continue;
            color = candidate;
            suffix = canonical.substring(candidate.length() + 1);
            break;
        }
        if (color == null) return null;
        String collection = switch (suffix) {
            case "BED", "WOOL", "STAINED_GLASS", "STAINED_GLASS_PANE", "CARPET",
                    "BANNER", "WALL_BANNER", "GLAZED_TERRACOTTA", "CONCRETE",
                    "CONCRETE_POWDER" -> suffix;
            case "TERRACOTTA" -> "DYED_TERRACOTTA";
            case "SHULKER_BOX" -> "DYED_SHULKER_BOX";
            case "CANDLE" -> "DYED_CANDLE";
            case "CANDLE_CAKE" -> "DYED_CANDLE_CAKE";
            default -> null;
        };
        if (collection == null) return null;
        var target = VANILLA_RELOCATIONS.searchExactField(
                "net.minecraft.world.level.block.Blocks", collection,
                "Lnet/minecraft/world/level/block/ColorCollection;");
        return target.size() == 1 ? new LegacyColorCollectionField(collection, color) : null;
    }

    /** Resolve an SRG field whose bytecode owner is a managed subclass. */
    private Forge1192SrgRuntimeResolver.FieldResolution resolveInheritedVanillaField(
            String receiver, String name, String desc) {
        java.util.ArrayDeque<String> pending = new java.util.ArrayDeque<>();
        java.util.HashSet<String> seen = new java.util.HashSet<>();
        pending.add(receiver);
        while (!pending.isEmpty()) {
            String current = pending.removeFirst();
            if (!seen.add(current)) continue;
            if (current.startsWith("net/minecraft/")) {
                var resolved = SRG_RUNTIME_RESOLVER.get().resolveField(current, name, desc);
                if (resolved.resolved()) return resolved;
            }
            try {
                byte[] bytes = hierarchyBytes(current);
                if (bytes == null) continue;
                ClassReader reader = new ClassReader(bytes);
                if (reader.getSuperName() != null) pending.addLast(reader.getSuperName());
                java.util.Collections.addAll(pending, reader.getInterfaces());
            } catch (java.io.IOException | LinkageError | RuntimeException ignored) {
                // Incomplete hierarchy evidence fails closed.
            }
        }
        return new Forge1192SrgRuntimeResolver.FieldResolution(
                receiver, name, desc, false, "No inherited vanilla field identity resolved");
    }

    /**
     * Locate the exact source-era vanilla declaration behind a field access
     * whose constant-pool owner is a managed subclass. Unlike ordinary SRG
     * resolution this deliberately does not require the field to survive in
     * the target runtime; semantic adapters use the returned source identity.
     */
    private String resolveInheritedLegacyVanillaFieldOwner(
            String receiver, String name, String desc) {
        java.util.ArrayDeque<String> pending = new java.util.ArrayDeque<>();
        java.util.HashSet<String> seen = new java.util.HashSet<>();
        pending.add(receiver);
        while (!pending.isEmpty()) {
            String current = pending.removeFirst();
            if (!seen.add(current)) continue;
            if (current.startsWith("net/minecraft/")
                    && SRG_RUNTIME_RESOLVER.get().canonicalFieldName(current, name, desc).isPresent()) {
                return current;
            }
            try {
                byte[] bytes = hierarchyBytes(current);
                if (bytes == null) continue;
                ClassReader reader = new ClassReader(bytes);
                if (reader.getSuperName() != null) pending.addLast(reader.getSuperName());
                java.util.Collections.addAll(pending, reader.getInterfaces());
            } catch (java.io.IOException | LinkageError | RuntimeException ignored) {
                // Incomplete hierarchy evidence fails closed.
            }
        }
        return null;
    }

    /** Exact 1.19.2 SRG identities for MobEffects' migration to Holder fields. */
    private static String modernMobEffectField(String srgName) {
        return switch (srgName) {
            case "f_19596_" -> "SPEED";
            case "f_19597_" -> "SLOWNESS";
            case "f_19598_" -> "HASTE";
            case "f_19599_" -> "MINING_FATIGUE";
            case "f_19600_" -> "STRENGTH";
            case "f_19601_" -> "INSTANT_HEALTH";
            case "f_19602_" -> "INSTANT_DAMAGE";
            case "f_19603_" -> "JUMP_BOOST";
            case "f_19604_" -> "NAUSEA";
            case "f_19605_" -> "REGENERATION";
            case "f_19606_" -> "RESISTANCE";
            case "f_19607_" -> "FIRE_RESISTANCE";
            case "f_19608_" -> "WATER_BREATHING";
            case "f_19609_" -> "INVISIBILITY";
            case "f_19610_" -> "BLINDNESS";
            case "f_19611_" -> "NIGHT_VISION";
            case "f_19612_" -> "HUNGER";
            case "f_19613_" -> "WEAKNESS";
            case "f_19614_" -> "POISON";
            case "f_19615_" -> "WITHER";
            case "f_19616_" -> "HEALTH_BOOST";
            case "f_19617_" -> "ABSORPTION";
            case "f_19618_" -> "SATURATION";
            case "f_19619_" -> "GLOWING";
            case "f_19620_" -> "LEVITATION";
            case "f_19621_" -> "LUCK";
            case "f_19590_" -> "UNLUCK";
            case "f_19591_" -> "SLOW_FALLING";
            case "f_19592_" -> "CONDUIT_POWER";
            case "f_19593_" -> "DOLPHINS_GRACE";
            case "f_19594_" -> "BAD_OMEN";
            case "f_19595_" -> "HERO_OF_THE_VILLAGE";
            case "f_216964_" -> "DARKNESS";
            default -> null;
        };
    }

    /**
     * MobEffectInstance constructors now accept Holder<MobEffect>. Preserve
     * legacy raw-effect construction through the built-in registry holder.
     */
    private static int rewriteLegacyMobEffectInstanceConstruction(ClassNode node) {
        int rewrites = 0;
        String owner = "net/minecraft/world/effect/MobEffectInstance";
        String legacyPrefix = "(Lnet/minecraft/world/effect/MobEffect;";
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKESPECIAL
                        || !call.owner.equals(owner)
                        || !call.name.equals("<init>")
                        || !call.desc.startsWith(legacyPrefix)) continue;
                Type[] arguments = Type.getArgumentTypes(call.desc);
                int[] locals = new int[arguments.length];
                var adaptation = new org.objectweb.asm.tree.InsnList();
                for (int i = arguments.length - 1; i >= 1; i--) {
                    locals[i] = method.maxLocals;
                    method.maxLocals += arguments[i].getSize();
                    adaptation.add(new org.objectweb.asm.tree.VarInsnNode(
                            arguments[i].getOpcode(org.objectweb.asm.Opcodes.ISTORE), locals[i]));
                }
                adaptation.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC,
                        "com/fuckingdeveloper/lms/runtime/Forge1192VanillaConstructionBridge",
                        "mobEffectHolder",
                        "(Lnet/minecraft/world/effect/MobEffect;)Lnet/minecraft/core/Holder;", false));
                for (int i = 1; i < arguments.length; i++) {
                    adaptation.add(new org.objectweb.asm.tree.VarInsnNode(
                            arguments[i].getOpcode(org.objectweb.asm.Opcodes.ILOAD), locals[i]));
                }
                method.instructions.insertBefore(call, adaptation);
                Type[] modern = arguments.clone();
                modern[0] = Type.getObjectType("net/minecraft/core/Holder");
                call.desc = Type.getMethodDescriptor(Type.VOID_TYPE, modern);
                rewrites++;
            }
        }
        return rewrites;
    }

    private static int rewriteLegacyForgeBuiltinFields(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof FieldInsnNode field)
                        || field.getOpcode() != org.objectweb.asm.Opcodes.GETSTATIC
                        || !field.owner.equals("net/minecraftforge/common/ForgeMod")) continue;
                if (field.name.equals("MILK")
                        && (field.desc.equals("Lnet/minecraftforge/registries/RegistryObject;")
                        || field.desc.equals("Lcom/fuckingdeveloper/lms/runtime/registry/LegacyRegistryObject;"))) {
                    MethodInsnNode replacement = new MethodInsnNode(
                            org.objectweb.asm.Opcodes.INVOKESTATIC,
                            "com/fuckingdeveloper/lms/runtime/Forge1192BuiltinRegistryBridge",
                            "milk",
                            "()Lcom/fuckingdeveloper/lms/runtime/registry/LegacyRegistryObject;",
                            false);
                    method.instructions.set(field, replacement);
                    rewrites++;
                } else if (field.desc.equals("Lcom/fuckingdeveloper/lms/runtime/registry/LegacyRegistryObject;")) {
                    String bridgeMethod = switch (field.name) {
                        case "REACH_DISTANCE" -> "reachDistance";
                        case "ATTACK_RANGE" -> "attackRange";
                        case "ENTITY_GRAVITY" -> "entityGravity";
                        case "STEP_HEIGHT_ADDITION" -> "stepHeightAddition";
                        default -> null;
                    };
                    if (bridgeMethod != null) {
                        method.instructions.set(field, new MethodInsnNode(
                                org.objectweb.asm.Opcodes.INVOKESTATIC,
                                "com/fuckingdeveloper/lms/runtime/Forge1192BuiltinRegistryBridge",
                                bridgeMethod,
                                "()Lcom/fuckingdeveloper/lms/runtime/registry/LegacyRegistryObject;",
                                false));
                        rewrites++;
                    }
                }
            }
        }
        return rewrites;
    }

    /**
     * Attribute modifiers moved from UUID/name identity to Identifier identity.
     * Preserve the UUID as the authoritative stable key and adapt the complete
     * construction/query/removal family together.
     */
    private static int rewriteLegacyAttributeModifiers(ClassNode node) {
        final String modifier = "net/minecraft/world/entity/ai/attributes/AttributeModifier";
        final String operation = modifier + "$Operation";
        final String legacyConstructor = "(Ljava/util/UUID;Ljava/lang/String;DL" + operation + ";)V";
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; ) {
                var next = insn.getNext();
                if (insn instanceof TypeInsnNode allocation
                        && allocation.getOpcode() == org.objectweb.asm.Opcodes.NEW
                        && allocation.desc.equals(modifier)) {
                    var duplicate = allocation.getNext();
                    MethodInsnNode constructor = null;
                    int nested = 0;
                    if (duplicate != null && duplicate.getOpcode() == org.objectweb.asm.Opcodes.DUP) {
                        for (var cursor = duplicate.getNext(); cursor != null; cursor = cursor.getNext()) {
                            if (cursor instanceof TypeInsnNode nestedType
                                    && nestedType.getOpcode() == org.objectweb.asm.Opcodes.NEW) nested++;
                            else if (cursor instanceof MethodInsnNode call
                                    && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESPECIAL
                                    && call.name.equals("<init>")) {
                                if (nested > 0) nested--;
                                else if (call.owner.equals(modifier)) { constructor = call; break; }
                            }
                        }
                    }
                    if (constructor != null && constructor.desc.equals(legacyConstructor)) {
                        method.instructions.remove(allocation);
                        method.instructions.remove(duplicate);
                        var factory = new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC,
                                "com/fuckingdeveloper/lms/runtime/Forge1192AttributeBridge",
                                "modifier", "(Ljava/util/UUID;Ljava/lang/String;DL" + operation + ";)L"
                                        + modifier + ";", false);
                        method.instructions.set(constructor, factory);
                        next = factory.getNext();
                        rewrites++;
                    }
                } else if (insn instanceof FieldInsnNode field
                        && field.getOpcode() == org.objectweb.asm.Opcodes.GETSTATIC
                        && field.owner.equals(operation)) {
                    String modern = switch (field.name) {
                        case "ADDITION" -> "ADD_VALUE";
                        case "MULTIPLY_BASE" -> "ADD_MULTIPLIED_BASE";
                        case "MULTIPLY_TOTAL" -> "ADD_MULTIPLIED_TOTAL";
                        default -> null;
                    };
                    if (modern != null) {
                        field.name = modern;
                        rewrites++;
                    }
                } else if (insn instanceof MethodInsnNode call
                        && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("net/minecraft/world/entity/ai/attributes/AttributeInstance")) {
                    if (call.name.equals("m_22111_")
                            && call.desc.equals("(Ljava/util/UUID;)L" + modifier + ";")) {
                        call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                        call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192AttributeBridge";
                        call.name = "getModifier";
                        call.desc = "(Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;Ljava/util/UUID;)L"
                                + modifier + ";";
                        call.itf = false;
                        rewrites++;
                    } else if (call.name.equals("m_22120_")
                            && call.desc.equals("(Ljava/util/UUID;)V")) {
                        call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                        call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192AttributeBridge";
                        call.name = "removeModifier";
                        call.desc = "(Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;Ljava/util/UUID;)V";
                        call.itf = false;
                        rewrites++;
                    }
                }
                insn = next;
            }
        }
        return rewrites;
    }

    /** Registry static values/keys split into BuiltInRegistries and Registries after 1.19.2. */
    private static int rewriteLegacyVanillaRegistryFields(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof FieldInsnNode field)
                        || field.getOpcode() != org.objectweb.asm.Opcodes.GETSTATIC
                        || !field.owner.equals("net/minecraft/core/Registry")) continue;
                if (field.name.equals("f_122832_")
                        && field.desc.equals("Lnet/minecraft/core/Registry;")) {
                    field.owner = "net/minecraft/core/registries/BuiltInRegistries";
                    field.name = "CUSTOM_STAT";
                    rewrites++;
                    continue;
                }
                String targetRegistry = switch (field.name) {
                    case "f_122821_" -> "SOUND_EVENT";
                    case "f_122822_" -> "FLUID";
                    case "f_122823_" -> "MOB_EFFECT";
                    case "f_122824_" -> "BLOCK";
                    // Enchantments became data-driven; there is deliberately
                    // no process-global migration for f_122825_.
                    case "f_122826_" -> "ENTITY_TYPE";
                    case "f_122827_" -> "ITEM";
                    case "f_122828_" -> "POTION";
                    case "f_122829_" -> "PARTICLE_TYPE";
                    case "f_122830_" -> "BLOCK_ENTITY_TYPE";
                    case "f_122833_" -> "CHUNK_STATUS";
                    default -> null;
                };
                if (targetRegistry != null) {
                    field.owner = "net/minecraft/core/registries/BuiltInRegistries";
                    field.name = targetRegistry;
                    field.desc = switch (targetRegistry) {
                        case "FLUID", "BLOCK", "ENTITY_TYPE", "ITEM", "CHUNK_STATUS" ->
                                "Lnet/minecraft/core/DefaultedRegistry;";
                        default -> "Lnet/minecraft/core/Registry;";
                    };
                    rewrites++;
                    continue;
                }
                if (!field.desc.equals("Lnet/minecraft/resources/ResourceKey;")) continue;
                String targetKey = switch (field.name) {
                    case "f_122898_" -> "SOUND_EVENT";
                    case "f_122899_" -> "FLUID";
                    case "f_122900_" -> "MOB_EFFECT";
                    case "f_122901_" -> "BLOCK";
                    case "f_122902_" -> "ENCHANTMENT";
                    case "f_122903_" -> "ENTITY_TYPE";
                    case "f_122904_" -> "ITEM";
                    case "f_122905_" -> "POTION";
                    case "f_122906_" -> "PARTICLE_TYPE";
                    case "f_122907_" -> "BLOCK_ENTITY_TYPE";
                    case "f_122909_" -> "CUSTOM_STAT";
                    case "f_122910_" -> "CHUNK_STATUS";
                    case "f_122911_" -> "RULE_TEST";
                    case "f_122912_" -> "POS_RULE_TEST";
                    case "f_122913_" -> "MENU";
                    case "f_122914_" -> "RECIPE_TYPE";
                    case "f_122915_" -> "RECIPE_SERIALIZER";
                    case "f_122916_" -> "ATTRIBUTE";
                    case "f_122917_" -> "STAT_TYPE";
                    default -> null;
                };
                if (targetKey != null) {
                    field.owner = "net/minecraft/core/registries/Registries";
                    field.name = targetKey;
                    rewrites++;
                }
            }
        }
        return rewrites;
    }

    /**
     * Forge 1.19.2 exposed process-global registry objects through
     * ForgeRegistries. Current built-in registries are the same concrete
     * Registry instances under BuiltInRegistries/NeoForgeRegistries. Replace
     * only fields with a verified process-global target; data-driven registries
     * deliberately remain migration boundaries because they require a
     * RegistryAccess rather than a fabricated global facade.
     */
    private static int rewriteLegacyForgeRegistries(ClassNode node) {
        int rewrites = 0;
        final String legacyOwner = "net/minecraftforge/registries/ForgeRegistries";
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; ) {
                var next = insn.getNext();
                if (!(insn instanceof FieldInsnNode field)
                        || !field.owner.equals(legacyOwner)) {
                    insn = next;
                    continue;
                }
                if (field.getOpcode() != org.objectweb.asm.Opcodes.GETSTATIC) {
                    throw new IllegalStateException("Unsupported ForgeRegistries field write: "
                            + node.name + "#" + method.name + " " + field.name);
                }

                if (field.name.equals("FLUID_TYPES")
                        && field.desc.equals("Ljava/util/function/Supplier;")) {
                    var supplierGet = nextRealInstruction(field.getNext());
                    var cast = nextRealInstruction(supplierGet == null ? null : supplierGet.getNext());
                    if (!(supplierGet instanceof MethodInsnNode get)
                            || get.getOpcode() != org.objectweb.asm.Opcodes.INVOKEINTERFACE
                            || !get.owner.equals("java/util/function/Supplier")
                            || !get.name.equals("get")
                            || !get.desc.equals("()Ljava/lang/Object;")
                            || !(cast instanceof TypeInsnNode type)
                            || type.getOpcode() != org.objectweb.asm.Opcodes.CHECKCAST
                            || !type.desc.equals("com/fuckingdeveloper/lms/runtime/registry/LegacyForgeRegistry")) {
                        throw new IllegalStateException("Unsupported ForgeRegistries.FLUID_TYPES access pattern: "
                                + node.name + "#" + method.name);
                    }
                    field.owner = "net/neoforged/neoforge/registries/NeoForgeRegistries";
                    field.desc = "Lnet/minecraft/core/Registry;";
                    next = cast.getNext();
                    method.instructions.remove(supplierGet);
                    method.instructions.remove(cast);
                    rewrites++;
                    insn = next;
                    continue;
                }

                String targetName = switch (field.name) {
                    case "BLOCKS" -> "BLOCK";
                    case "BLOCK_ENTITY_TYPES" -> "BLOCK_ENTITY_TYPE";
                    case "ENTITY_TYPES" -> "ENTITY_TYPE";
                    case "FLUIDS" -> "FLUID";
                    case "FOLIAGE_PLACER_TYPES" -> "FOLIAGE_PLACER_TYPE";
                    case "ITEMS" -> "ITEM";
                    case "MOB_EFFECTS" -> "MOB_EFFECT";
                    case "POI_TYPES" -> "POINT_OF_INTEREST_TYPE";
                    case "RECIPE_SERIALIZERS" -> "RECIPE_SERIALIZER";
                    case "VILLAGER_PROFESSIONS" -> "VILLAGER_PROFESSION";
                    default -> null;
                };
                if (targetName == null) {
                    String bridgeMethod = switch (field.name) {
                        case "ENCHANTMENTS" -> "enchantments";
                        case "PAINTING_VARIANTS" -> "paintingVariants";
                        default -> null;
                    };
                    if (bridgeMethod != null) {
                        method.instructions.set(field, new MethodInsnNode(
                                org.objectweb.asm.Opcodes.INVOKESTATIC,
                                "com/fuckingdeveloper/lms/runtime/Forge1192DynamicRegistryBridge",
                                bridgeMethod,
                                "()Lnet/minecraft/core/Registry;",
                                false));
                        rewrites++;
                        insn = next;
                        continue;
                    }
                    throw new IllegalStateException("Forge 1.19.2 registry has no verified process-global target: "
                            + node.name + "#" + method.name + " ForgeRegistries." + field.name);
                }
                field.owner = "net/minecraft/core/registries/BuiltInRegistries";
                field.name = targetName;
                field.desc = switch (targetName) {
                    case "BLOCK", "ENTITY_TYPE", "FLUID", "ITEM", "VILLAGER_PROFESSION" ->
                            "Lnet/minecraft/core/DefaultedRegistry;";
                    default -> "Lnet/minecraft/core/Registry;";
                };
                rewrites++;
                insn = next;
            }
        }
        return rewrites;
    }

    private static org.objectweb.asm.tree.AbstractInsnNode nextRealInstruction(
            org.objectweb.asm.tree.AbstractInsnNode instruction) {
        while (instruction != null && instruction.getOpcode() < 0) instruction = instruction.getNext();
        return instruction;
    }

    /**
     * ForgeRegistries.Keys held registry ResourceKey constants in 1.19.2.
     * The modern vanilla keys live in Registries (mostly singular names);
     * NeoForge-only keys live in NeoForgeRegistries.Keys. Resolve actual
     * runtime fields instead of defining a counterfeit ForgeRegistries.Keys.
     */
    private static int rewriteLegacyForgeRegistryKeys(ClassNode node) {
        int rewrites = 0;
        String legacy = "net/minecraftforge/registries/ForgeRegistries$Keys";
        String vanilla = "net/minecraft/core/registries/Registries";
        String neoforge = "net/neoforged/neoforge/registries/NeoForgeRegistries$Keys";
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof FieldInsnNode field) || !field.owner.equals(legacy)) continue;
                if (field.getOpcode() != org.objectweb.asm.Opcodes.GETSTATIC
                        || !field.desc.equals("Lnet/minecraft/resources/ResourceKey;"))
                    throw new IllegalStateException("Unsupported legacy registry key access: "
                            + node.name + "#" + method.name + " " + field.name + ":" + field.desc);
                String singular = switch (field.name) {
                    case "BLOCKS" -> "BLOCK";
                    case "ITEMS" -> "ITEM";
                    case "FLUIDS" -> "FLUID";
                    case "ENTITY_TYPES" -> "ENTITY_TYPE";
                    case "MOB_EFFECTS" -> "MOB_EFFECT";
                    case "SOUND_EVENTS" -> "SOUND_EVENT";
                    case "POTIONS" -> "POTION";
                    case "ENCHANTMENTS" -> "ENCHANTMENT";
                    case "BLOCK_ENTITY_TYPES" -> "BLOCK_ENTITY_TYPE";
                    case "PARTICLE_TYPES" -> "PARTICLE_TYPE";
                    case "MENU_TYPES" -> "MENU";
                    case "RECIPE_TYPES" -> "RECIPE_TYPE";
                    case "RECIPE_SERIALIZERS" -> "RECIPE_SERIALIZER";
                    case "ATTRIBUTES" -> "ATTRIBUTE";
                    case "STAT_TYPES" -> "STAT_TYPE";
                    case "COMMAND_ARGUMENT_TYPES" -> "COMMAND_ARGUMENT_TYPE";
                    case "VILLAGER_PROFESSIONS" -> "VILLAGER_PROFESSION";
                    case "POI_TYPES" -> "POINT_OF_INTEREST_TYPE";
                    case "MEMORY_MODULE_TYPES" -> "MEMORY_MODULE_TYPE";
                    case "SENSOR_TYPES" -> "SENSOR_TYPE";
                    case "SCHEDULES" -> "SCHEDULE";
                    case "ACTIVITIES" -> "ACTIVITY";
                    case "LOOT_POOL_ENTRY_TYPES" -> "LOOT_POOL_ENTRY_TYPE";
                    case "LOOT_FUNCTION_TYPES" -> "LOOT_FUNCTION_TYPE";
                    case "LOOT_CONDITION_TYPES" -> "LOOT_CONDITION_TYPE";
                    case "LOOT_NUMBER_PROVIDER_TYPES" -> "LOOT_NUMBER_PROVIDER_TYPE";
                    case "LOOT_NBT_PROVIDER_TYPES" -> "LOOT_NBT_PROVIDER_TYPE";
                    case "LOOT_SCORE_PROVIDER_TYPES" -> "LOOT_SCORE_PROVIDER_TYPE";
                    case "FLOAT_PROVIDER_TYPES" -> "FLOAT_PROVIDER_TYPE";
                    case "INT_PROVIDER_TYPES" -> "INT_PROVIDER_TYPE";
                    case "HEIGHT_PROVIDER_TYPES" -> "HEIGHT_PROVIDER_TYPE";
                    case "BLOCK_STATE_PROVIDER_TYPES" -> "BLOCK_STATE_PROVIDER_TYPE";
                    case "FOLIAGE_PLACER_TYPES" -> "FOLIAGE_PLACER_TYPE";
                    case "TRUNK_PLACER_TYPES" -> "TRUNK_PLACER_TYPE";
                    case "TREE_DECORATOR_TYPES" -> "TREE_DECORATOR_TYPE";
                    case "FEATURE_SIZE_TYPES" -> "FEATURE_SIZE_TYPE";
                    case "PLACEMENT_MODIFIER_TYPES" -> "PLACEMENT_MODIFIER_TYPE";
                    case "CARVER_TYPES" -> "CARVER";
                    case "FEATURES" -> "FEATURE";
                    case "STRUCTURE_PIECE_TYPES" -> "STRUCTURE_PIECE";
                    case "STRUCTURE_TYPES" -> "STRUCTURE_TYPE";
                    case "STRUCTURE_PLACEMENT_TYPES" -> "STRUCTURE_PLACEMENT";
                    case "STRUCTURE_PROCESSOR_TYPES" -> "STRUCTURE_PROCESSOR";
                    case "STRUCTURE_POOL_ELEMENT_TYPES" -> "STRUCTURE_POOL_ELEMENT";
                    case "CHUNK_STATUS" -> "CHUNK_STATUS";
                    case "RULE_TEST_TYPES" -> "RULE_TEST";
                    case "POS_RULE_TEST_TYPES" -> "POS_RULE_TEST";
                    case "PAINTING_VARIANTS" -> "PAINTING_VARIANT";
                    default -> field.name;
                };
                if (runtimeHasRegistryKey(vanilla, singular)) {
                    field.owner = vanilla;
                    field.name = singular;
                } else if (runtimeHasRegistryKey(neoforge, field.name)) {
                    field.owner = neoforge;
                } else {
                    throw new IllegalStateException("Unresolved ForgeRegistries.Keys mapping: "
                            + node.name + "#" + method.name + " key=" + field.name
                            + " candidate=" + singular);
                }
                rewrites++;
            }
        }
        return rewrites;
    }

    private static boolean runtimeHasRegistryKey(String owner, String name) {
        try {
            Class<?> type = Class.forName(owner.replace('/', '.'), false,
                    Forge1192LegacyClassTransformer.class.getClassLoader());
            return type.getField(name).getType().getName()
                    .equals("net.minecraft.resources.ResourceKey");
        } catch (ClassNotFoundException | NoSuchFieldException | LinkageError ignored) {
            return false;
        }
    }

    private static int rewriteLegacyEnvironmentFieldAccess(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null;) {
                var next = insn.getNext();
                if (insn instanceof FieldInsnNode field) {
                    // Match the exact source-era field before namespace projection:
                    // Forge 1.19.2 exposed the physical distribution as the static
                    // FMLEnvironment.dist field. Current FML exposes it through
                    // getDist(); preserve the legacy read contract without inventing
                    // mutable field state.
                    if (field.getOpcode() == org.objectweb.asm.Opcodes.GETSTATIC
                            && field.owner.equals("net/minecraftforge/fml/loading/FMLEnvironment")
                            && field.name.equals("dist")
                            && field.desc.equals("Lnet/minecraftforge/api/distmarker/Dist;")) {
                        MethodInsnNode getter = new MethodInsnNode(
                                org.objectweb.asm.Opcodes.INVOKESTATIC,
                                "net/neoforged/fml/loading/FMLEnvironment",
                                "getDist",
                                "()Lnet/neoforged/api/distmarker/Dist;",
                                false);
                        method.instructions.set(field, getter);
                        rewrites++;
                    }
                }
                insn = next;
            }
        }
        return rewrites;
    }

    /**
     * Forge/Minecraft 1.19.2 exposed {@code Level.isClientSide} as a public
     * final field. The target keeps the same boolean property behind the
     * public {@code Level#isClientSide()} accessor.
     */
    private static int rewriteLegacyLevelClientSideFieldAccess(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null;) {
                var current = insn;
                insn = insn.getNext();
                if (!(current instanceof FieldInsnNode field)
                        || field.getOpcode() != org.objectweb.asm.Opcodes.GETFIELD
                        || !field.owner.equals("net/minecraft/world/level/Level")
                        || !(field.name.equals("f_46443_") || field.name.equals("isClientSide"))
                        || !field.desc.equals("Z")) {
                    continue;
                }
                method.instructions.set(field, new MethodInsnNode(
                        org.objectweb.asm.Opcodes.INVOKEVIRTUAL,
                        "net/minecraft/world/level/Level",
                        "isClientSide",
                        "()Z",
                        false));
                rewrites++;
            }
        }
        return rewrites;
    }

    /** Source Entity#level was public; target 26.3 exposes the same value by accessor. */
    private static int rewriteLegacyEntityLevelFieldAccess(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null;) {
                var current = insn;
                insn = insn.getNext();
                if (!(current instanceof FieldInsnNode field)
                        || field.getOpcode() != org.objectweb.asm.Opcodes.GETFIELD
                        || !field.owner.equals("net/minecraft/world/entity/Entity")
                        || !(field.name.equals("f_19853_") || field.name.equals("level"))
                        || !field.desc.equals("Lnet/minecraft/world/level/Level;")) {
                    continue;
                }
                method.instructions.set(field, new MethodInsnNode(
                        org.objectweb.asm.Opcodes.INVOKEVIRTUAL,
                        "net/minecraft/world/entity/Entity",
                        "level",
                        "()Lnet/minecraft/world/level/Level;",
                        false));
                rewrites++;
            }
        }
        return rewrites;
    }

    /** Target 26.3 replaced the flooring double constructor with an exact factory. */
    private static int rewriteLegacyBlockPosConstruction(ClassNode node) {
        final String blockPos = "net/minecraft/core/BlockPos";
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null;) {
                var next = insn.getNext();
                if (!(insn instanceof TypeInsnNode allocation)
                        || allocation.getOpcode() != org.objectweb.asm.Opcodes.NEW
                        || !allocation.desc.equals(blockPos)) {
                    insn = next;
                    continue;
                }
                var duplicate = allocation.getNext();
                if (duplicate == null || duplicate.getOpcode() != org.objectweb.asm.Opcodes.DUP) {
                    throw new IllegalStateException("Unsupported legacy BlockPos allocation: "
                            + node.name + "#" + method.name);
                }
                MethodInsnNode constructor = null;
                int nested = 0;
                for (var cursor = duplicate.getNext(); cursor != null; cursor = cursor.getNext()) {
                    if (cursor instanceof TypeInsnNode nestedType
                            && nestedType.getOpcode() == org.objectweb.asm.Opcodes.NEW) {
                        nested++;
                    } else if (cursor instanceof MethodInsnNode call
                            && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESPECIAL
                            && call.name.equals("<init>")) {
                        if (nested > 0) nested--;
                        else if (call.owner.equals(blockPos)) {
                            constructor = call;
                            break;
                        }
                    }
                }
                if (constructor != null && constructor.desc.equals("(DDD)V")) {
                    method.instructions.remove(allocation);
                    method.instructions.remove(duplicate);
                    var factory = new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC,
                            blockPos, "containing", "(DDD)L" + blockPos + ";", false);
                    method.instructions.set(constructor, factory);
                    next = factory.getNext();
                    rewrites++;
                }
                insn = next;
            }
        }
        return rewrites;
    }

    /**
     * Target 26.3 split the source Action/String value object into typed click
     * event records. Preserve the exact source constructor and accessor ABI
     * through a factory that selects the corresponding target value type.
     */
    private static int rewriteLegacyChatEventFamily(ClassNode node) {
        final String clickEvent = "net/minecraft/network/chat/ClickEvent";
        final String clickAction = "net/minecraft/network/chat/ClickEvent$Action";
        final String hoverEvent = "net/minecraft/network/chat/HoverEvent";
        final String hoverAction = "net/minecraft/network/chat/HoverEvent$Action";
        final String bridge = "com/fuckingdeveloper/lms/runtime/chat/Forge1192ChatEventBridge";
        final String clickConstructor = "(L" + clickAction + ";Ljava/lang/String;)V";
        final String hoverConstructor = "(L" + hoverAction + ";Ljava/lang/Object;)V";
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null;) {
                var next = insn.getNext();
                if (insn instanceof TypeInsnNode allocation
                        && allocation.getOpcode() == org.objectweb.asm.Opcodes.NEW
                        && allocation.desc.equals(clickEvent)) {
                    var duplicate = allocation.getNext();
                    if (duplicate == null
                            || duplicate.getOpcode() != org.objectweb.asm.Opcodes.DUP) {
                        throw new IllegalStateException("Unsupported legacy ClickEvent allocation: "
                                + node.name + "#" + method.name + method.desc);
                    }
                    MethodInsnNode constructor = null;
                    int nested = 0;
                    for (var cursor = duplicate.getNext(); cursor != null; cursor = cursor.getNext()) {
                        if (cursor instanceof TypeInsnNode nestedType
                                && nestedType.getOpcode() == org.objectweb.asm.Opcodes.NEW) {
                            nested++;
                        } else if (cursor instanceof MethodInsnNode call
                                && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESPECIAL
                                && call.name.equals("<init>")) {
                            if (nested > 0) {
                                nested--;
                            } else {
                                if (call.owner.equals(clickEvent)
                                        && call.desc.equals(clickConstructor)) {
                                    constructor = call;
                                }
                                break;
                            }
                        }
                    }
                    if (constructor == null) {
                        throw new IllegalStateException("Cannot pair legacy ClickEvent constructor: "
                                + node.name + "#" + method.name + method.desc);
                    }
                    method.instructions.remove(allocation);
                    method.instructions.remove(duplicate);
                    var factory = new MethodInsnNode(
                            org.objectweb.asm.Opcodes.INVOKESTATIC,
                            bridge,
                            "click",
                            "(L" + clickAction + ";Ljava/lang/String;)L" + clickEvent + ";",
                            false);
                    method.instructions.set(constructor, factory);
                    rewrites++;
                    insn = factory.getNext();
                    continue;
                }
                if (insn instanceof MethodInsnNode call
                        && call.owner.equals(clickEvent)
                        && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL) {
                    if (call.name.equals("m_130622_")
                            && call.desc.equals("()L" + clickAction + ";")) {
                        call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                        call.owner = bridge;
                        call.name = "action";
                        call.desc = "(L" + clickEvent + ";)L" + clickAction + ";";
                        call.itf = false;
                        rewrites++;
                    } else if (call.name.equals("m_130623_")
                            && call.desc.equals("()Ljava/lang/String;")) {
                        call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                        call.owner = bridge;
                        call.name = "value";
                        call.desc = "(L" + clickEvent + ";)Ljava/lang/String;";
                        call.itf = false;
                        rewrites++;
                    }
                }
                if (insn instanceof TypeInsnNode allocation
                        && allocation.getOpcode() == org.objectweb.asm.Opcodes.NEW
                        && allocation.desc.equals(hoverEvent)) {
                    var duplicate = allocation.getNext();
                    if (duplicate == null
                            || duplicate.getOpcode() != org.objectweb.asm.Opcodes.DUP) {
                        throw new IllegalStateException("Unsupported legacy HoverEvent allocation: "
                                + node.name + "#" + method.name + method.desc);
                    }
                    MethodInsnNode constructor = null;
                    int nested = 0;
                    for (var cursor = duplicate.getNext(); cursor != null; cursor = cursor.getNext()) {
                        if (cursor instanceof TypeInsnNode nestedType
                                && nestedType.getOpcode() == org.objectweb.asm.Opcodes.NEW) {
                            nested++;
                        } else if (cursor instanceof MethodInsnNode call
                                && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESPECIAL
                                && call.name.equals("<init>")) {
                            if (nested > 0) {
                                nested--;
                            } else {
                                if (call.owner.equals(hoverEvent)
                                        && call.desc.equals(hoverConstructor)) {
                                    constructor = call;
                                }
                                break;
                            }
                        }
                    }
                    if (constructor == null) {
                        throw new IllegalStateException("Cannot pair legacy HoverEvent constructor: "
                                + node.name + "#" + method.name + method.desc);
                    }
                    method.instructions.remove(allocation);
                    method.instructions.remove(duplicate);
                    var factory = new MethodInsnNode(
                            org.objectweb.asm.Opcodes.INVOKESTATIC,
                            bridge,
                            "hover",
                            "(L" + hoverAction + ";Ljava/lang/Object;)L" + hoverEvent + ";",
                            false);
                    method.instructions.set(constructor, factory);
                    rewrites++;
                    insn = factory.getNext();
                    continue;
                }
                if (insn instanceof MethodInsnNode call
                        && call.owner.equals(hoverEvent)
                        && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL) {
                    if (call.name.equals("m_130820_")
                            && call.desc.equals("()L" + hoverAction + ";")) {
                        call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                        call.owner = bridge;
                        call.name = "hoverAction";
                        call.desc = "(L" + hoverEvent + ";)L" + hoverAction + ";";
                        call.itf = false;
                        rewrites++;
                    } else if (call.name.equals("m_130823_")
                            && call.desc.equals("(L" + hoverAction + ";)Ljava/lang/Object;")) {
                        call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                        call.owner = bridge;
                        call.name = "hoverValue";
                        call.desc = "(L" + hoverEvent + ";L" + hoverAction
                                + ";)Ljava/lang/Object;";
                        call.itf = false;
                        rewrites++;
                    }
                }
                insn = next;
            }
        }
        return rewrites;
    }

    /**
     * Capture the removed Forge 1.19.2 SimpleChannel protocol surface while
     * its authoritative source identities are still present. Message
     * registration is retained as metadata; sends remain explicitly
     * fail-closed until a modern payload binding has been proven.
     */
    private static int rewriteLegacyNetworkFacade(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)) continue;
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESTATIC
                        && call.owner.equals("net/minecraftforge/network/NetworkRegistry")
                        && call.name.equals("newSimpleChannel")
                        && call.desc.equals("(Lnet/minecraft/resources/ResourceLocation;Ljava/util/function/Supplier;Ljava/util/function/Predicate;Ljava/util/function/Predicate;)Lnet/minecraftforge/network/simple/SimpleChannel;")) {
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192NetworkBridge";
                    call.desc = "(Lnet/minecraft/resources/ResourceLocation;Ljava/util/function/Supplier;Ljava/util/function/Predicate;Ljava/util/function/Predicate;)Ljava/lang/Object;";
                    call.itf = false;
                    rewrites++;
                    continue;
                }
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("net/minecraftforge/network/simple/SimpleChannel")
                        && call.name.equals("registerMessage")
                        && call.desc.equals("(ILjava/lang/Class;Ljava/util/function/BiConsumer;Ljava/util/function/Function;Ljava/util/function/BiConsumer;)Lnet/minecraftforge/network/simple/IndexedMessageCodec$MessageHandler;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192NetworkBridge";
                    call.desc = "(Ljava/lang/Object;ILjava/lang/Class;Ljava/util/function/BiConsumer;Ljava/util/function/Function;Ljava/util/function/BiConsumer;)Ljava/lang/Object;";
                    call.itf = false;
                    rewrites++;
                    continue;
                }
                if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("net/minecraftforge/network/simple/SimpleChannel")
                        && call.name.equals("send")
                        && call.desc.equals("(Lnet/minecraftforge/network/PacketDistributor$PacketTarget;Ljava/lang/Object;)V")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192NetworkBridge";
                    call.desc = "(Ljava/lang/Object;Lnet/minecraftforge/network/PacketDistributor$PacketTarget;Ljava/lang/Object;)V";
                    call.itf = false;
                    rewrites++;
                    continue;
                }
                if (!call.owner.equals("net/minecraftforge/network/NetworkEvent$Context")) continue;
                String bridgeDesc = switch (call.name + call.desc) {
                    case "enqueueWork(Ljava/lang/Runnable;)Ljava/util/concurrent/CompletableFuture;" ->
                            "(Ljava/lang/Object;Ljava/lang/Runnable;)Ljava/util/concurrent/CompletableFuture;";
                    case "getSender()Lnet/minecraft/server/level/ServerPlayer;" ->
                            "(Ljava/lang/Object;)Lnet/minecraft/server/level/ServerPlayer;";
                    case "setPacketHandled(Z)V" -> "(Ljava/lang/Object;Z)V";
                    default -> null;
                };
                if (bridgeDesc != null) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192NetworkBridge";
                    call.desc = bridgeDesc;
                    call.itf = false;
                    rewrites++;
                }
            }
        }
        return rewrites;
    }

    private static int rewriteLegacyTickEventFields(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null;) {
                var next = insn.getNext();
                if (insn instanceof FieldInsnNode field
                        && field.getOpcode() == org.objectweb.asm.Opcodes.GETFIELD) {
                    MethodInsnNode replacement = legacyTickFieldAdapter(field);
                    if (replacement != null) {
                        method.instructions.set(field, replacement);
                        rewrites++;
                    }
                }
                insn = next;
            }
        }
        return rewrites;
    }

    private static MethodInsnNode legacyTickFieldAdapter(FieldInsnNode field) {
        String bridge = "com/fuckingdeveloper/lms/runtime/Forge1192TickEventBridge";
        boolean tickOwner = field.owner.equals("net/neoforged/neoforge/event/tick/ServerTickEvent")
                || field.owner.equals("net/neoforged/neoforge/event/tick/LevelTickEvent")
                || field.owner.equals("net/neoforged/neoforge/event/tick/PlayerTickEvent")
                || field.owner.equals("net/neoforged/neoforge/client/event/ClientTickEvent");
        if (tickOwner && field.name.equals("phase")) {
            return new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC, bridge, "phase",
                    "(Ljava/lang/Object;)Lcom/fuckingdeveloper/lms/runtime/Forge1192TickEventBridge$Phase;",
                    false);
        }
        if (field.owner.equals("net/neoforged/neoforge/event/tick/PlayerTickEvent")) {
            if (field.name.equals("side")) {
                return new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC, bridge, "side",
                        "(Lnet/neoforged/neoforge/event/tick/PlayerTickEvent;)Lnet/neoforged/fml/LogicalSide;",
                        false);
            }
            if (field.name.equals("player")) {
                return new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC, bridge, "player",
                        "(Lnet/neoforged/neoforge/event/tick/PlayerTickEvent;)Lnet/minecraft/world/entity/player/Player;",
                        false);
            }
        }
        if (field.owner.equals("net/neoforged/neoforge/event/tick/LevelTickEvent")) {
            if (field.name.equals("side")) {
                return new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC, bridge, "side",
                        "(Lnet/neoforged/neoforge/event/tick/LevelTickEvent;)Lnet/neoforged/fml/LogicalSide;",
                        false);
            }
            if (field.name.equals("level")) {
                return new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC, bridge, "level",
                        "(Lnet/neoforged/neoforge/event/tick/LevelTickEvent;)Lnet/minecraft/world/level/Level;",
                        false);
            }
        }
        return null;
    }

    private static int rewriteLegacySpawnCheckSemantics(ClassNode node) {
        int rewrites = 0;
        String eventOwner = "net/neoforged/neoforge/event/entity/living/MobSpawnEvent$PositionCheck";
        String resultOwner = eventOwner + "$Result";
        for (var method : node.methods) {
            boolean hasPositionResult = false;
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof MethodInsnNode call && call.owner.equals(eventOwner)
                        && call.name.equals("setResult")) {
                    hasPositionResult = true;
                    break;
                }
            }
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof MethodInsnNode call && call.owner.equals(eventOwner)) {
                    if (call.name.equals("getSpawnReason")
                            && call.desc.equals("()Lnet/minecraft/world/entity/EntitySpawnReason;")) {
                        call.name = "getSpawnType";
                        rewrites++;
                    } else if (call.name.equals("setResult")
                            && call.desc.equals("(Lnet/neoforged/bus/api/Event$Result;)V")) {
                        call.desc = "(L" + resultOwner + ";)V";
                        rewrites++;
                    }
                } else if (hasPositionResult && insn instanceof FieldInsnNode field
                        && field.getOpcode() == org.objectweb.asm.Opcodes.GETSTATIC
                        && field.owner.equals("net/neoforged/bus/api/Event$Result")
                        && field.name.equals("DENY")) {
                    field.owner = resultOwner;
                    field.name = "FAIL";
                    field.desc = "L" + resultOwner + ";";
                    rewrites++;
                }
            }
        }
        return rewrites;
    }

    private static int rewriteLegacyIdentifierConstruction(ClassNode node) {
        int rewrites = 0;
        final String identifier = "net/minecraft/resources/Identifier";
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; ) {
                var next = insn.getNext();
                if (!(insn instanceof TypeInsnNode allocation)
                        || allocation.getOpcode() != org.objectweb.asm.Opcodes.NEW
                        || !allocation.desc.equals(identifier)) {
                    insn = next;
                    continue;
                }
                var dup = allocation.getNext();
                if (dup == null || dup.getOpcode() != org.objectweb.asm.Opcodes.DUP) {
                    throw new IllegalStateException("Unsupported legacy Identifier allocation: "
                            + node.name + "#" + method.name);
                }
                MethodInsnNode init = null;
                int nested = 0;
                for (var cursor = dup.getNext(); cursor != null; cursor = cursor.getNext()) {
                    if (cursor instanceof TypeInsnNode type
                            && type.getOpcode() == org.objectweb.asm.Opcodes.NEW) {
                        nested++;
                    } else if (cursor instanceof MethodInsnNode call
                            && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESPECIAL
                            && call.name.equals("<init>")) {
                        if (nested > 0) {
                            nested--;
                        } else if (call.owner.equals(identifier)) {
                            init = call;
                            break;
                        }
                    }
                }
                if (init == null) {
                    throw new IllegalStateException("Cannot match legacy Identifier constructor: "
                            + node.name + "#" + method.name);
                }
                String factoryDesc = switch (init.desc) {
                    case "(Ljava/lang/String;)V" ->
                            "(Ljava/lang/String;)Lnet/minecraft/resources/Identifier;";
                    case "(Ljava/lang/String;Ljava/lang/String;)V" ->
                            "(Ljava/lang/String;Ljava/lang/String;)Lnet/minecraft/resources/Identifier;";
                    default -> throw new IllegalStateException("Unsupported legacy Identifier constructor: "
                            + node.name + "#" + method.name + init.desc);
                };
                method.instructions.remove(allocation);
                method.instructions.remove(dup);
                MethodInsnNode factory = new MethodInsnNode(
                        org.objectweb.asm.Opcodes.INVOKESTATIC,
                        "com/fuckingdeveloper/lms/runtime/Forge1192IdentifierBridge",
                        "create", factoryDesc, false);
                method.instructions.set(init, factory);
                rewrites++;
                // 'next' was the DUP we just removed. Continuing from that
                // detached node terminates ASM traversal after the first
                // ResourceLocation in a method. Resume after the replacement
                // factory so every allocation in large <clinit> methods is adapted.
                insn = factory.getNext();
            }
        }
        return rewrites;
    }

    /**
     * Preserve source-era ItemStack construction during callbacks that target
     * 26.3 executes before global default components are bound. The factory
     * uses the ordinary target constructor after binding and an isolated,
     * exact component snapshot before binding.
     */
    private static int rewriteLegacyItemStackConstruction(ClassNode node) {
        int rewrites = 0;
        final String stack = "net/minecraft/world/item/ItemStack";
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null;) {
                var next = insn.getNext();
                if (!(insn instanceof TypeInsnNode allocation)
                        || allocation.getOpcode() != org.objectweb.asm.Opcodes.NEW
                        || !allocation.desc.equals(stack)) {
                    insn = next;
                    continue;
                }
                var dup = allocation.getNext();
                if (dup == null || dup.getOpcode() != org.objectweb.asm.Opcodes.DUP) {
                    throw new IllegalStateException("Unsupported legacy ItemStack allocation: "
                            + node.name + "#" + method.name);
                }
                MethodInsnNode init = null;
                int nested = 0;
                for (var cursor = dup.getNext(); cursor != null; cursor = cursor.getNext()) {
                    if (cursor instanceof TypeInsnNode type
                            && type.getOpcode() == org.objectweb.asm.Opcodes.NEW) {
                        nested++;
                    } else if (cursor instanceof MethodInsnNode call
                            && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESPECIAL
                            && call.name.equals("<init>")) {
                        if (nested > 0) nested--;
                        else if (call.owner.equals(stack)) {
                            init = call;
                            break;
                        }
                    }
                }
                if (init == null) {
                    throw new IllegalStateException("Cannot match legacy ItemStack constructor: "
                            + node.name + "#" + method.name);
                }
                String factoryDesc = switch (init.desc) {
                    case "(Lnet/minecraft/world/level/ItemLike;)V" ->
                            "(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/item/ItemStack;";
                    case "(Lnet/minecraft/world/level/ItemLike;I)V" ->
                            "(Lnet/minecraft/world/level/ItemLike;I)Lnet/minecraft/world/item/ItemStack;";
                    default -> null;
                };
                if (factoryDesc == null) {
                    insn = next;
                    continue;
                }
                method.instructions.remove(allocation);
                method.instructions.remove(dup);
                MethodInsnNode factory = new MethodInsnNode(
                        org.objectweb.asm.Opcodes.INVOKESTATIC,
                        "com/fuckingdeveloper/lms/runtime/Forge1192EarlyItemStackBridge",
                        "create", factoryDesc, false);
                method.instructions.set(init, factory);
                rewrites++;
                insn = factory.getNext();
            }
        }
        return rewrites;
    }

    /** Source instance sameItem became the target static item-identity test. */
    private static int rewriteLegacyItemStackComparison(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null;
                    insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        || !call.owner.equals("net/minecraft/world/item/ItemStack")
                        || !call.name.equals("m_41656_")
                        || !call.desc.equals("(Lnet/minecraft/world/item/ItemStack;)Z")) continue;
                call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                call.name = "isSameItem";
                call.desc = "(Lnet/minecraft/world/item/ItemStack;"
                        + "Lnet/minecraft/world/item/ItemStack;)Z";
                call.itf = false;
                rewrites++;
            }
        }
        return rewrites;
    }

    /** Adjust exact invocation shape for source classes that are target interfaces. */
    private static int rewriteTargetInterfaceInvocationShapes(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof MethodInsnNode call
                        && call.owner.equals("net/minecraft/nbt/NumericTag")) {
                    String targetName = targetNumericMethod(call.name, call.desc);
                    if (targetName != null && !targetName.equals(call.name)) {
                        call.name = targetName;
                        rewrites++;
                    }
                    if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL) {
                        call.setOpcode(org.objectweb.asm.Opcodes.INVOKEINTERFACE);
                        call.itf = true;
                        rewrites++;
                    }
                }
                if (insn instanceof MethodInsnNode call
                        && (call.owner.equals("net/minecraft/core/Registry")
                            || call.owner.equals("net/minecraft/core/DefaultedRegistry"))
                        && call.name.equals("m_203613_")
                        && call.desc.equals("()Ljava/util/stream/Stream;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/registry/Forge1192RegistryTagBridge";
                    call.name = "tagNames";
                    call.desc = "(Ljava/lang/Object;)Ljava/util/stream/Stream;";
                    call.itf = false;
                    rewrites++;
                } else if (insn instanceof MethodInsnNode call
                        && call.owner.equals("net/minecraft/core/DefaultedRegistry")
                        && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL) {
                    // DefaultedRegistry changed from an abstract class to an
                    // interface in the target runtime. The semantic method
                    // identity is resolved separately; fix the JVM call shape.
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKEINTERFACE);
                    call.itf = true;
                    rewrites++;
                }
                if (!(insn instanceof InvokeDynamicInsnNode indy)) continue;
                for (int i = 0; i < indy.bsmArgs.length; i++) {
                    if (!(indy.bsmArgs[i] instanceof Handle handle)
                            || !handle.getOwner().equals("net/minecraft/nbt/NumericTag")) continue;
                    String targetName = targetNumericMethod(handle.getName(), handle.getDesc());
                    int tag = handle.getTag() == org.objectweb.asm.Opcodes.H_INVOKEVIRTUAL
                            ? org.objectweb.asm.Opcodes.H_INVOKEINTERFACE : handle.getTag();
                    if (targetName == null) targetName = handle.getName();
                    if (tag == handle.getTag() && targetName.equals(handle.getName())
                            && handle.isInterface()) continue;
                    indy.bsmArgs[i] = new Handle(tag,
                            handle.getOwner(), targetName, handle.getDesc(), true);
                    rewrites++;
                }
            }
        }
        return rewrites;
    }

    private static String targetNumericMethod(String sourceName, String descriptor) {
        if (SRG_RUNTIME_RESOLVER.isEmpty()) return null;
        String canonical = sourceName.startsWith("m_")
                ? SRG_RUNTIME_RESOLVER.get().canonicalMethodName(
                        "net/minecraft/nbt/NumericTag", sourceName, descriptor).orElse(null)
                : sourceName;
        if (canonical == null) return null;
        return switch (canonical) {
            case "getAsByte" -> "byteValue";
            case "getAsShort" -> "shortValue";
            case "getAsInt" -> "intValue";
            case "getAsLong" -> "longValue";
            case "getAsFloat" -> "floatValue";
            case "getAsDouble" -> "doubleValue";
            case "getAsNumber" -> "box";
            default -> null;
        };
    }

    /**
     * The source DiggerItem base class became target item/tag semantics. Adapt
     * only the exact source type test; construction remains handled by the
     * proven tool-item family adapters.
     */
    private static int rewriteLegacyDiggerItemChecks(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null;) {
                var next = insn.getNext();
                if (insn instanceof TypeInsnNode type
                        && type.getOpcode() == org.objectweb.asm.Opcodes.INSTANCEOF
                        && type.desc.equals("net/minecraft/world/item/DiggerItem")) {
                    method.instructions.set(type, new MethodInsnNode(
                            org.objectweb.asm.Opcodes.INVOKESTATIC,
                            "com/fuckingdeveloper/lms/runtime/Forge1192ItemBridge",
                            "isDiggerItem",
                            "(Ljava/lang/Object;)Z",
                            false));
                    rewrites++;
                }
                insn = next;
            }
        }
        return rewrites;
    }

    /**
     * KeyMapping categories changed from translation-key strings to registered
     * Category values. Replace ordinary allocations with bridge factories so
     * no adapter call is made with an uninitialized object on the operand
     * stack. Direct subclass super calls only convert their final String
     * argument and never expose uninitialized {@code this} to the bridge.
     */
    private static int rewriteLegacyKeyMappingConstruction(ClassNode node) {
        final String owner = "net/minecraft/client/KeyMapping";
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; ) {
                var next = insn.getNext();
                if (!(insn instanceof TypeInsnNode allocation)
                        || allocation.getOpcode() != org.objectweb.asm.Opcodes.NEW
                        || !allocation.desc.equals(owner)) {
                    insn = next;
                    continue;
                }
                var dup = allocation.getNext();
                if (dup == null || dup.getOpcode() != org.objectweb.asm.Opcodes.DUP) {
                    throw new IllegalStateException("Unsupported legacy KeyMapping allocation pattern: "
                            + node.name + "#" + method.name);
                }
                MethodInsnNode init = matchingConstructor(dup, owner);
                if (init == null) {
                    throw new IllegalStateException("Cannot pair legacy KeyMapping allocation with constructor: "
                            + node.name + "#" + method.name);
                }
                String factoryDesc = legacyKeyMappingFactoryDescriptor(init.desc);
                if (factoryDesc != null) {
                    method.instructions.remove(allocation);
                    method.instructions.remove(dup);
                    MethodInsnNode factory = new MethodInsnNode(
                            org.objectweb.asm.Opcodes.INVOKESTATIC,
                            "com/fuckingdeveloper/lms/runtime/Forge1192KeyMappingBridge",
                            "create", factoryDesc, false);
                    method.instructions.set(init, factory);
                    next = factory.getNext();
                    rewrites++;
                }
                insn = next;
            }

            // A managed subclass may invoke the legacy KeyMapping constructor
            // directly. The category String is the last argument in every
            // supported source overload, so it can be converted in place.
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKESPECIAL
                        || !call.owner.equals(owner)
                        || !call.name.equals("<init>")) continue;
                String factoryDesc = legacyKeyMappingFactoryDescriptor(call.desc);
                if (factoryDesc == null) continue;
                method.instructions.insertBefore(call, new MethodInsnNode(
                        org.objectweb.asm.Opcodes.INVOKESTATIC,
                        "com/fuckingdeveloper/lms/runtime/Forge1192KeyMappingBridge",
                        "category",
                        "(Ljava/lang/String;)Lnet/minecraft/client/KeyMapping$Category;", false));
                call.desc = legacyKeyMappingTargetConstructorDescriptor(call.desc);
                rewrites++;
            }
        }
        return rewrites;
    }

    private static MethodInsnNode matchingConstructor(
            org.objectweb.asm.tree.AbstractInsnNode dup, String owner) {
        int nestedAllocations = 0;
        for (var cursor = dup.getNext(); cursor != null; cursor = cursor.getNext()) {
            if (cursor instanceof TypeInsnNode nested
                    && nested.getOpcode() == org.objectweb.asm.Opcodes.NEW) {
                nestedAllocations++;
            } else if (cursor instanceof MethodInsnNode call
                    && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESPECIAL
                    && call.name.equals("<init>")) {
                if (nestedAllocations > 0) {
                    nestedAllocations--;
                } else if (call.owner.equals(owner)) {
                    return call;
                }
            }
        }
        return null;
    }

    private static String legacyKeyMappingFactoryDescriptor(String constructorDesc) {
        boolean supported = switch (constructorDesc) {
            case "(Ljava/lang/String;ILjava/lang/String;)V",
                 "(Ljava/lang/String;Lcom/mojang/blaze3d/platform/InputConstants$Type;ILjava/lang/String;)V",
                 "(Ljava/lang/String;Lnet/minecraftforge/client/settings/IKeyConflictContext;Lcom/mojang/blaze3d/platform/InputConstants$Type;ILjava/lang/String;)V",
                 "(Ljava/lang/String;Lnet/minecraftforge/client/settings/IKeyConflictContext;Lcom/mojang/blaze3d/platform/InputConstants$Key;Ljava/lang/String;)V",
                 "(Ljava/lang/String;Lnet/minecraftforge/client/settings/IKeyConflictContext;Lnet/minecraftforge/client/settings/KeyModifier;Lcom/mojang/blaze3d/platform/InputConstants$Type;ILjava/lang/String;)V",
                 "(Ljava/lang/String;Lnet/minecraftforge/client/settings/IKeyConflictContext;Lnet/minecraftforge/client/settings/KeyModifier;Lcom/mojang/blaze3d/platform/InputConstants$Key;Ljava/lang/String;)V" -> true;
            default -> false;
        };
        return supported
                ? constructorDesc.substring(0, constructorDesc.length() - 1)
                        + "Lnet/minecraft/client/KeyMapping;"
                : null;
    }

    private static String legacyKeyMappingTargetConstructorDescriptor(String constructorDesc) {
        final String suffix = "Ljava/lang/String;)V";
        if (!constructorDesc.endsWith(suffix)) {
            throw new IllegalArgumentException("Not a legacy KeyMapping constructor: " + constructorDesc);
        }
        return constructorDesc.substring(0, constructorDesc.length() - suffix.length())
                + "Lnet/minecraft/client/KeyMapping$Category;)V";
    }

    /**
     * Forge 1.19.2 client entrypoints could observe the Minecraft singleton
     * during mod construction; the target constructs it later. Split only a
     * verifier-proven linear void method at an empty-stack getInstance/store
     * boundary, retaining its early prefix and scheduling the client-dependent
     * suffix for the target client-setup phase.
     */
    private static int rewriteLegacyEarlyClientInitialization(ClassNode node) {
        java.util.ArrayList<org.objectweb.asm.tree.MethodNode> helpers = new java.util.ArrayList<>();
        int rewrites = 0;
        for (var method : java.util.List.copyOf(node.methods)) {
            if (method.name.equals("<init>")) {
                rewrites += deferLegacyMinecraftFieldInitialization(node, method);
                continue;
            }
            if (method.name.startsWith("<")) continue;
            if (!method.desc.equals("()V")
                    || (method.access & (org.objectweb.asm.Opcodes.ACC_STATIC
                        | org.objectweb.asm.Opcodes.ACC_ABSTRACT
                        | org.objectweb.asm.Opcodes.ACC_NATIVE)) != 0
                    || !method.tryCatchBlocks.isEmpty()) continue;

            MethodInsnNode singletonCall = null;
            org.objectweb.asm.tree.AbstractInsnNode split = null;
            boolean unsupportedFlow = false;
            int returns = 0;
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof org.objectweb.asm.tree.JumpInsnNode
                        || insn instanceof org.objectweb.asm.tree.TableSwitchInsnNode
                        || insn instanceof org.objectweb.asm.tree.LookupSwitchInsnNode) {
                    unsupportedFlow = true;
                }
                if (insn.getOpcode() >= org.objectweb.asm.Opcodes.IRETURN
                        && insn.getOpcode() <= org.objectweb.asm.Opcodes.RETURN) returns++;
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKESTATIC
                        || !call.owner.equals("net/minecraft/client/Minecraft")
                        || !(call.name.equals("m_91087_") || call.name.equals("getInstance"))
                        || !call.desc.equals("()Lnet/minecraft/client/Minecraft;")) continue;
                var receiver = previousExecutable(call);
                var store = nextExecutable(call);
                if (singletonCall != null
                        || !(receiver instanceof org.objectweb.asm.tree.VarInsnNode load)
                        || load.getOpcode() != org.objectweb.asm.Opcodes.ALOAD
                        || load.var != 0
                        || !(store instanceof FieldInsnNode field)
                        || field.getOpcode() != org.objectweb.asm.Opcodes.PUTFIELD
                        || !field.owner.equals(node.name)
                        || !field.desc.equals("Lnet/minecraft/client/Minecraft;")) {
                    unsupportedFlow = true;
                    continue;
                }
                singletonCall = call;
                split = receiver;
            }
            if (unsupportedFlow || singletonCall == null || split == null || returns != 1
                    || lastExecutable(method).getOpcode()
                        != org.objectweb.asm.Opcodes.RETURN
                    || !emptyStackAt(node.name, method, split)) continue;

            String helperName = uniqueHelperName(node, "$lms$clientReady$" + method.name);
            var helper = new org.objectweb.asm.tree.MethodNode(
                    org.objectweb.asm.Opcodes.ACC_PRIVATE | org.objectweb.asm.Opcodes.ACC_SYNTHETIC,
                    helperName, "()V", null, null);
            for (var cursor = split; cursor != null; ) {
                var next = cursor.getNext();
                method.instructions.remove(cursor);
                helper.instructions.add(cursor);
                cursor = next;
            }
            helper.maxLocals = method.maxLocals;
            helper.maxStack = method.maxStack;
            helper.localVariables = null;
            method.localVariables = null;
            method.instructions.add(new org.objectweb.asm.tree.VarInsnNode(
                    org.objectweb.asm.Opcodes.ALOAD, 0));
            method.instructions.add(new LdcInsnNode(helperName));
            method.instructions.add(new MethodInsnNode(
                    org.objectweb.asm.Opcodes.INVOKESTATIC,
                    "com/fuckingdeveloper/lms/runtime/Forge1192ClientLifecycleBridge",
                    "runWhenReady", "(Ljava/lang/Object;Ljava/lang/String;)V", false));
            method.instructions.add(new org.objectweb.asm.tree.InsnNode(
                    org.objectweb.asm.Opcodes.RETURN));
            helpers.add(helper);
            rewrites++;
        }
        node.methods.addAll(helpers);
        return rewrites;
    }

    /**
     * Before the target client singleton exists, the source-era equivalent of
     * {@code Minecraft.getInstance().player} is simply no active player. Adapt
     * only the proven ALOAD-0 / own Minecraft field / SRG player-field chain.
     */
    private static int rewriteLegacyEarlyClientPlayerReads(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null;) {
                var next = insn.getNext();
                if (insn instanceof org.objectweb.asm.tree.VarInsnNode load
                        && load.getOpcode() == org.objectweb.asm.Opcodes.ALOAD
                        && load.var == 0) {
                    var ownFieldInsn = nextExecutable(load);
                    var playerFieldInsn = ownFieldInsn == null ? null : nextExecutable(ownFieldInsn);
                    if (ownFieldInsn instanceof FieldInsnNode ownField
                            && ownField.getOpcode() == org.objectweb.asm.Opcodes.GETFIELD
                            && ownField.owner.equals(node.name)
                            && ownField.desc.equals("Lnet/minecraft/client/Minecraft;")
                            && playerFieldInsn instanceof FieldInsnNode playerField
                            && playerField.getOpcode() == org.objectweb.asm.Opcodes.GETFIELD
                            && playerField.owner.equals("net/minecraft/client/Minecraft")
                            && playerField.name.equals("f_91074_")
                            && playerField.desc.equals("Lnet/minecraft/client/player/LocalPlayer;")) {
                        method.instructions.insertBefore(load, new MethodInsnNode(
                                org.objectweb.asm.Opcodes.INVOKESTATIC,
                                "com/fuckingdeveloper/lms/runtime/Forge1192ClientLifecycleBridge",
                                "currentPlayer",
                                "()Lnet/minecraft/client/player/LocalPlayer;",
                                false));
                        method.instructions.remove(load);
                        method.instructions.remove(ownField);
                        method.instructions.remove(playerField);
                        rewrites++;
                    }
                }
                insn = next;
            }
        }
        return rewrites;
    }

    /**
     * Forge 1.19.2 client objects could cache Minecraft during mod
     * construction, while the target creates the singleton later. For fields
     * proven to be initialized from the exact singleton accessor, retain the
     * cached value when present and otherwise resolve the now-live target
     * singleton at the point of use.
     */
    private static int rewriteLegacyCachedMinecraftReads(ClassNode node) {
        java.util.Set<String> cachedFields = new java.util.HashSet<>();
        for (var method : node.methods) {
            if (!method.name.equals("<init>")) continue;
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKESTATIC
                        || !call.owner.equals("net/minecraft/client/Minecraft")
                        || !(call.name.equals("m_91087_") || call.name.equals("getInstance"))
                        || !call.desc.equals("()Lnet/minecraft/client/Minecraft;")) continue;
                var receiver = previousExecutable(call);
                var store = nextExecutable(call);
                if (receiver instanceof org.objectweb.asm.tree.VarInsnNode load
                        && load.getOpcode() == org.objectweb.asm.Opcodes.ALOAD
                        && load.var == 0
                        && store instanceof FieldInsnNode field
                        && field.getOpcode() == org.objectweb.asm.Opcodes.PUTFIELD
                        && field.owner.equals(node.name)
                        && field.desc.equals("Lnet/minecraft/client/Minecraft;")) {
                    cachedFields.add(field.name);
                }
            }
        }
        if (cachedFields.isEmpty()) return 0;

        int rewrites = 0;
        for (var method : node.methods) {
            if (method.name.equals("<init>")) continue;
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof FieldInsnNode field)
                        || field.getOpcode() != org.objectweb.asm.Opcodes.GETFIELD
                        || !field.owner.equals(node.name)
                        || !field.desc.equals("Lnet/minecraft/client/Minecraft;")
                        || !cachedFields.contains(field.name)) continue;
                method.instructions.insert(field, new MethodInsnNode(
                        org.objectweb.asm.Opcodes.INVOKESTATIC,
                        "com/fuckingdeveloper/lms/runtime/Forge1192ClientLifecycleBridge",
                        "currentMinecraft",
                        "(Lnet/minecraft/client/Minecraft;)Lnet/minecraft/client/Minecraft;",
                        false));
                rewrites++;
            }
        }
        return rewrites;
    }

    /** Rename an exact SRG implementation of StringRepresentable's surviving interface method. */
    private int rewriteLegacyStringRepresentableOverride(ClassNode node) {
        if (SRG_RUNTIME_RESOLVER.isEmpty()
                || !isManagedSubtypeOf(node, "net/minecraft/util/StringRepresentable")) return 0;
        String descriptor = "()Ljava/lang/String;";
        java.util.HashMap<String, String> renamed = new java.util.HashMap<>();
        int rewrites = 0;
        for (var method : node.methods) {
            if (!method.name.startsWith("m_") || !method.desc.equals(descriptor)
                    || (method.access & org.objectweb.asm.Opcodes.ACC_STATIC) != 0) continue;
            var resolved = SRG_RUNTIME_RESOLVER.get().resolve(
                    "net/minecraft/util/StringRepresentable", method.name, descriptor);
            if (!resolved.resolved()
                    || !resolved.owner().equals("net/minecraft/util/StringRepresentable")
                    || !resolved.name().equals("getSerializedName")
                    || !resolved.descriptor().equals(descriptor)) continue;
            renamed.put(method.name, resolved.name());
            method.name = resolved.name();
            rewrites++;
        }
        if (renamed.isEmpty()) return rewrites;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof MethodInsnNode call
                        && call.owner.equals(node.name)
                        && call.desc.equals(descriptor)
                        && renamed.containsKey(call.name)) {
                    call.name = renamed.get(call.name);
                    rewrites++;
                }
            }
        }
        return rewrites;
    }

    /**
     * Restore SRG-named method declarations that override an exactly surviving
     * vanilla contract. Call-site projection is insufficient for overrides:
     * the JVM selects implementations by their target-runtime name and
     * descriptor, so leaving the legacy declaration name behind makes an
     * otherwise concrete managed subtype appear abstract.
     *
     * <p>The declaring managed class is never used as mapping evidence. The
     * rewrite requires an exact SRG identity resolved through a proven vanilla
     * superclass/interface, plus an unchanged descriptor in the target
     * runtime. This deliberately excludes descriptor-only matches and API
     * families whose target semantics or descriptors changed.</p>
     */
    private int rewriteVerifiedVanillaOverrides(ClassNode node) {
        if (SRG_RUNTIME_RESOLVER.isEmpty()) return 0;
        int rewrites = 0;
        java.util.HashMap<String, String> renamed = new java.util.HashMap<>();
        for (var method : node.methods) {
            if (!method.name.startsWith("m_")
                    || (method.access & (org.objectweb.asm.Opcodes.ACC_STATIC
                            | org.objectweb.asm.Opcodes.ACC_PRIVATE)) != 0) continue;
            var resolved = resolveOverriddenVanillaMethod(node, method.name, method.desc);
            if (resolved == null || resolved.name().equals(method.name)
                    || !isTargetMethodOverridable(resolved)) continue;
            boolean collision = node.methods.stream().anyMatch(candidate -> candidate != method
                    && candidate.name.equals(resolved.name())
                    && candidate.desc.equals(resolved.descriptor()));
            if (collision) {
                throw new IllegalStateException("Managed class " + node.name
                        + " declares both legacy and target override identities for "
                        + resolved.name() + resolved.descriptor());
            }
            renamed.put(method.name + method.desc, resolved.name());
            method.name = resolved.name();
            rewrites++;
        }
        if (renamed.isEmpty()) return rewrites;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call) || !call.owner.equals(node.name)) continue;
                String targetName = renamed.get(call.name + call.desc);
                if (targetName == null) continue;
                call.name = targetName;
                rewrites++;
            }
        }
        return rewrites;
    }

    private Forge1192SrgRuntimeResolver.Resolution resolveOverriddenVanillaMethod(
            ClassNode node, String name, String descriptor) {
        java.util.LinkedHashMap<String, Forge1192SrgRuntimeResolver.Resolution> matches =
                new java.util.LinkedHashMap<>();
        if (node.superName != null) {
            var resolved = resolveInheritedVanillaMethod(node.superName, name, descriptor);
            if (resolved != null) matches.put(resolved.name() + resolved.descriptor(), resolved);
        }
        for (String interfaceName : node.interfaces) {
            var resolved = resolveInheritedVanillaMethod(interfaceName, name, descriptor);
            if (resolved != null) matches.put(resolved.name() + resolved.descriptor(), resolved);
        }
        return matches.size() == 1 ? matches.values().iterator().next() : null;
    }

    private boolean isTargetMethodOverridable(
            Forge1192SrgRuntimeResolver.Resolution resolved) {
        try {
            byte[] bytes = hierarchyBytes(resolved.owner());
            if (bytes == null) return false;
            ClassNode target = new ClassNode();
            new ClassReader(bytes).accept(target,
                    ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            for (var method : target.methods) {
                if (!method.name.equals(resolved.name())
                        || !method.desc.equals(resolved.descriptor())) continue;
                return (method.access & (org.objectweb.asm.Opcodes.ACC_FINAL
                        | org.objectweb.asm.Opcodes.ACC_STATIC
                        | org.objectweb.asm.Opcodes.ACC_PRIVATE)) == 0;
            }
        } catch (java.io.IOException | RuntimeException ignored) {
            // Missing/corrupt target ABI evidence must fail closed.
        }
        return false;
    }

    /**
     * Item components are bound only after the target item registry phase.
     * Preserve exact linear {@code consumer(new ItemStack(itemLike), id)}
     * statements by deferring the consumer, never by manufacturing a stack
     * with an incomplete component prototype.
     */
    private int rewriteEarlyItemStackConsumers(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null;) {
                var next = insn.getNext();
                if (!(insn instanceof MethodInsnNode constructor)
                        || constructor.getOpcode() != org.objectweb.asm.Opcodes.INVOKESPECIAL
                        || !constructor.owner.equals("net/minecraft/world/item/ItemStack")
                        || !constructor.name.equals("<init>")
                        || !constructor.desc.equals("(Lnet/minecraft/world/level/ItemLike;)V")) {
                    insn = next;
                    continue;
                }
                var itemProducer = previousExecutable(constructor);
                var duplicate = itemProducer == null ? null : previousExecutable(itemProducer);
                var allocation = duplicate == null ? null : previousExecutable(duplicate);
                if (!(itemProducer instanceof FieldInsnNode itemField)
                        || itemField.getOpcode() != org.objectweb.asm.Opcodes.GETSTATIC
                        || !(duplicate instanceof org.objectweb.asm.tree.InsnNode)
                        || duplicate.getOpcode() != org.objectweb.asm.Opcodes.DUP
                        || !(allocation instanceof TypeInsnNode type)
                        || allocation.getOpcode() != org.objectweb.asm.Opcodes.NEW
                        || !type.desc.equals("net/minecraft/world/item/ItemStack")) {
                    insn = next;
                    continue;
                }
                MethodInsnNode identifierConstructor = null;
                MethodInsnNode consumer = null;
                int executable = 0;
                for (var cursor = nextExecutable(constructor);
                     cursor != null && executable++ < 10; cursor = nextExecutable(cursor)) {
                    if (cursor instanceof org.objectweb.asm.tree.JumpInsnNode
                            || cursor instanceof org.objectweb.asm.tree.TableSwitchInsnNode
                            || cursor instanceof org.objectweb.asm.tree.LookupSwitchInsnNode) break;
                    if (cursor instanceof MethodInsnNode call
                            && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESPECIAL
                            && call.owner.equals("net/minecraft/resources/ResourceLocation")
                            && call.name.equals("<init>")) {
                        identifierConstructor = call;
                        continue;
                    }
                    if (identifierConstructor != null && cursor instanceof MethodInsnNode call
                            && (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                                || call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEINTERFACE)
                            && call.desc.equals("(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/resources/ResourceLocation;)V")) {
                        consumer = call;
                    }
                    if (consumer != null) break;
                }
                if (consumer == null || !registrationFreeConsumer(node, consumer)) {
                    insn = next;
                    continue;
                }
                method.instructions.remove(allocation);
                method.instructions.remove(duplicate);
                method.instructions.remove(constructor);
                method.instructions.insertBefore(consumer, new LdcInsnNode(consumer.name));
                consumer.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                consumer.owner = "com/fuckingdeveloper/lms/runtime/Forge1192RegistrationContext";
                consumer.name = "deferItemStackConsumer";
                consumer.desc = "(Ljava/lang/Object;Lnet/minecraft/world/level/ItemLike;"
                        + "Lnet/minecraft/resources/ResourceLocation;Ljava/lang/String;)V";
                consumer.itf = false;
                rewrites++;
                insn = next;
            }
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKESTATIC
                        || !call.desc.equals("(Lnet/minecraft/world/item/Item;)V")
                        || !managedStaticItemStackConsumer(call)) continue;
                var metadata = new org.objectweb.asm.tree.InsnList();
                metadata.add(new LdcInsnNode(Type.getObjectType(call.owner)));
                metadata.add(new LdcInsnNode(call.name));
                method.instructions.insertBefore(call, metadata);
                call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192RegistrationContext";
                call.name = "deferStaticItemStackConsumer";
                call.desc = "(Lnet/minecraft/world/item/Item;Ljava/lang/Class;Ljava/lang/String;)V";
                call.itf = false;
                rewrites++;
            }
            if (!method.desc.equals(
                    "(Lnet/minecraftforge/fml/event/lifecycle/FMLCommonSetupEvent;)V")) continue;
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call) || !call.desc.equals("()V")) continue;
                if ((call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        || call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEINTERFACE)
                        && usesItemStackDefaultsAtDispatch(node, call)) {
                    var metadata = new org.objectweb.asm.tree.InsnList();
                    metadata.add(new LdcInsnNode(Type.getObjectType(call.owner)));
                    metadata.add(new LdcInsnNode(call.name));
                    method.instructions.insertBefore(call, metadata);
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192RegistrationContext";
                    call.name = "deferItemComponentInitializer";
                    call.desc = "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/String;)V";
                    call.itf = false;
                    rewrites++;
                } else if (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESTATIC
                        && usesItemStackDefaults(call.owner, call.name, call.desc)) {
                    var metadata = new org.objectweb.asm.tree.InsnList();
                    metadata.add(new LdcInsnNode(Type.getObjectType(call.owner)));
                    metadata.add(new LdcInsnNode(call.name));
                    method.instructions.insertBefore(call, metadata);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192RegistrationContext";
                    call.name = "deferStaticItemComponentInitializer";
                    call.desc = "(Ljava/lang/Class;Ljava/lang/String;)V";
                    call.itf = false;
                    rewrites++;
                }
            }
        }
        return rewrites;
    }

    /**
     * A lifecycle call can be compiled against a base type while a side-selected
     * implementation performs the component-dependent work.  Resolve that case
     * only when the receiver is a static field and its initializer exposes
     * concrete constructor handles (the shape emitted for supplier-based side
     * selection).  This retains the field/method/override evidence instead of
     * broadening the rewrite from the descriptor alone.
     */
    private boolean usesItemStackDefaultsAtDispatch(ClassNode caller, MethodInsnNode call) {
        if (usesItemStackDefaults(call.owner, call.name, call.desc)) return true;
        var receiver = previousExecutable(call);
        if (!(receiver instanceof FieldInsnNode field)
                || field.getOpcode() != org.objectweb.asm.Opcodes.GETSTATIC
                || !field.desc.equals("L" + call.owner + ";")) return false;
        for (String implementation : concreteStaticFieldImplementations(caller, field)) {
            if (usesItemStackDefaults(implementation, call.name, call.desc)) return true;
        }
        return false;
    }

    private java.util.Set<String> concreteStaticFieldImplementations(
            ClassNode caller, FieldInsnNode field) {
        ClassNode owner = caller.name.equals(field.owner) ? caller : readManagedClass(field.owner);
        if (owner == null) return java.util.Set.of();
        var implementations = new java.util.LinkedHashSet<String>();
        boolean foundAssignment = false;
        for (var method : owner.methods) {
            if (!method.name.equals("<clinit>") || !method.desc.equals("()V")) continue;
            var segmentStart = method.instructions.getFirst();
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof FieldInsnNode assignment)
                        || assignment.getOpcode() != org.objectweb.asm.Opcodes.PUTSTATIC) continue;
                if (assignment.owner.equals(field.owner)
                        && assignment.name.equals(field.name)
                        && assignment.desc.equals(field.desc)) {
                    foundAssignment = true;
                    collectConstructedTypes(owner, method, segmentStart, assignment,
                            implementations, new java.util.HashSet<>());
                }
                segmentStart = insn.getNext();
            }
        }
        if (!foundAssignment) return java.util.Set.of();
        String declaredType = Type.getType(field.desc).getInternalName();
        implementations.removeIf(candidate -> !isManagedSubtype(candidate, declaredType));
        return implementations;
    }

    private void collectConstructedTypes(
            ClassNode owner,
            org.objectweb.asm.tree.MethodNode method,
            org.objectweb.asm.tree.AbstractInsnNode start,
            org.objectweb.asm.tree.AbstractInsnNode end,
            java.util.Set<String> result,
            java.util.Set<String> visiting) {
        for (var insn = start; insn != null && insn != end; insn = insn.getNext()) {
            if (insn instanceof TypeInsnNode type
                    && type.getOpcode() == org.objectweb.asm.Opcodes.NEW) result.add(type.desc);
            if (!(insn instanceof InvokeDynamicInsnNode dynamic)) continue;
            for (Object argument : dynamic.bsmArgs) {
                if (!(argument instanceof Handle handle)) continue;
                if (handle.getTag() == org.objectweb.asm.Opcodes.H_NEWINVOKESPECIAL) {
                    result.add(handle.getOwner());
                } else if (handle.getOwner().equals(owner.name)) {
                    collectConstructedTypesFromMethod(owner, handle, result, visiting);
                }
            }
        }
    }

    private void collectConstructedTypesFromMethod(
            ClassNode owner, Handle handle, java.util.Set<String> result,
            java.util.Set<String> visiting) {
        String key = handle.getOwner() + '#' + handle.getName() + handle.getDesc();
        if (!visiting.add(key)) return;
        try {
            for (var candidate : owner.methods) {
                if (!candidate.name.equals(handle.getName())
                        || !candidate.desc.equals(handle.getDesc())) continue;
                collectConstructedTypes(owner, candidate, candidate.instructions.getFirst(), null,
                        result, visiting);
            }
        } finally {
            visiting.remove(key);
        }
    }

    private ClassNode readManagedClass(String owner) {
        try {
            byte[] bytes = managedClassBytes == null ? null : managedClassBytes.find(owner);
            if (bytes == null) return null;
            ClassNode node = new ClassNode();
            new ClassReader(bytes).accept(node, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            return node;
        } catch (java.io.IOException | RuntimeException ignored) {
            return null;
        }
    }

    private boolean isManagedSubtype(String candidate, String target) {
        var pending = new java.util.ArrayDeque<String>();
        var visited = new java.util.HashSet<String>();
        pending.add(candidate);
        while (!pending.isEmpty()) {
            String current = pending.removeFirst();
            if (!visited.add(current)) continue;
            if (current.equals(target)) return true;
            ClassNode type = readManagedClass(current);
            if (type == null) continue;
            if (type.superName != null) pending.addLast(type.superName);
            pending.addAll(type.interfaces);
        }
        return false;
    }

    private boolean usesItemStackDefaults(String owner, String name, String descriptor) {
        if (owner.startsWith("net/minecraft/") || owner.startsWith("net/minecraftforge/")) return false;
        String key = owner + '#' + name + descriptor;
        return itemComponentInitializers.computeIfAbsent(key,
                ignored -> usesItemStackDefaults(owner, name, descriptor,
                        new java.util.HashSet<>(), 0));
    }

    private boolean usesItemStackDefaults(
            String owner, String name, String descriptor, java.util.Set<String> visiting, int depth) {
        if (depth > 12 || owner.startsWith("java/") || owner.startsWith("net/minecraft/")) return false;
        String key = owner + '#' + name + descriptor;
        if (!visiting.add(key)) return false;
        try {
            byte[] bytes = managedClassBytes == null ? null : managedClassBytes.find(owner);
            if (bytes == null) return false;
            ClassNode candidate = new ClassNode();
            new ClassReader(bytes).accept(candidate, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            var methods = candidate.methods.stream()
                    .filter(method -> method.name.equals(name) && method.desc.equals(descriptor))
                    .toList();
            if (methods.size() != 1) return false;
            for (var insn = methods.getFirst().instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)) continue;
                if (call.owner.equals("net/minecraft/world/item/Item")
                        && call.name.equals("m_41472_")
                        && call.desc.equals("()Z")) return true;
                if (call.owner.equals("net/minecraft/world/item/ItemStack")
                        && call.name.equals("<init>")) return true;
                Type returnType = Type.getReturnType(call.desc);
                if ((returnType.getSort() == Type.OBJECT
                        && returnType.getInternalName().equals("net/minecraft/world/item/ItemStack"))
                        || (returnType.getSort() == Type.ARRAY
                            && returnType.getElementType().getSort() == Type.OBJECT
                            && returnType.getElementType().getInternalName().equals(
                            "net/minecraft/world/item/ItemStack"))) return true;
                if (usesItemStackDefaults(call.owner, call.name, call.desc, visiting, depth + 1)) return true;
            }
            return false;
        } catch (java.io.IOException | RuntimeException ignored) {
            return false;
        } finally {
            visiting.remove(key);
        }
    }

    private boolean managedStaticItemStackConsumer(MethodInsnNode call) {
        try {
            byte[] bytes = managedClassBytes == null ? null : managedClassBytes.find(call.owner);
            if (bytes == null) return false;
            ClassNode owner = new ClassNode();
            new ClassReader(bytes).accept(owner, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            var targets = owner.methods.stream()
                    .filter(method -> method.name.equals(call.name) && method.desc.equals(call.desc)
                            && (method.access & org.objectweb.asm.Opcodes.ACC_STATIC) != 0)
                    .toList();
            if (targets.size() != 1) return false;
            var target = targets.getFirst();
            boolean consumesParameterAsStack = false;
            for (var insn = target.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode constructor)
                        || constructor.getOpcode() != org.objectweb.asm.Opcodes.INVOKESPECIAL
                        || !constructor.owner.equals("net/minecraft/world/item/ItemStack")
                        || !constructor.name.equals("<init>")
                        || !constructor.desc.equals("(Lnet/minecraft/world/level/ItemLike;)V")) continue;
                var item = previousExecutable(constructor);
                if (item instanceof org.objectweb.asm.tree.VarInsnNode load
                        && load.getOpcode() == org.objectweb.asm.Opcodes.ALOAD && load.var == 0) {
                    consumesParameterAsStack = true;
                }
            }
            if (!consumesParameterAsStack) return false;
            for (var insn = target.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof MethodInsnNode nested && nested.name.equals("register")) return false;
            }
            return true;
        } catch (java.io.IOException | RuntimeException ignored) {
            return false;
        }
    }

    private boolean registrationFreeConsumer(ClassNode current, MethodInsnNode call) {
        try {
            ClassNode owner = current;
            if (!call.owner.equals(current.name)) {
                byte[] bytes = managedClassBytes == null ? null : managedClassBytes.find(call.owner);
                if (bytes == null) return false;
                owner = new ClassNode();
                new ClassReader(bytes).accept(owner, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            }
            var target = owner.methods.stream()
                    .filter(method -> method.name.equals(call.name) && method.desc.equals(call.desc))
                    .toList();
            if (target.size() != 1) return false;
            for (var insn = target.getFirst().instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof MethodInsnNode nested
                        && (nested.name.equals("register")
                            || nested.owner.equals("com/fuckingdeveloper/lms/runtime/Forge1192RegistrationContext"))) {
                    return false;
                }
            }
            return true;
        } catch (java.io.IOException | RuntimeException ignored) {
            return false;
        }
    }

    private static int deferLegacyMinecraftFieldInitialization(
            ClassNode node, org.objectweb.asm.tree.MethodNode constructor) {
        int rewrites = 0;
        for (var insn = constructor.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (!(insn instanceof MethodInsnNode call)
                    || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKESTATIC
                    || !call.owner.equals("net/minecraft/client/Minecraft")
                    || !(call.name.equals("m_91087_") || call.name.equals("getInstance"))
                    || !call.desc.equals("()Lnet/minecraft/client/Minecraft;")) continue;
            var receiver = previousExecutable(call);
            var store = nextExecutable(call);
            if (!(receiver instanceof org.objectweb.asm.tree.VarInsnNode load)
                    || load.getOpcode() != org.objectweb.asm.Opcodes.ALOAD
                    || load.var != 0
                    || !(store instanceof FieldInsnNode field)
                    || field.getOpcode() != org.objectweb.asm.Opcodes.PUTFIELD
                    || !field.owner.equals(node.name)
                    || !field.desc.equals("Lnet/minecraft/client/Minecraft;")) continue;
            var deferred = new org.objectweb.asm.tree.InsnList();
            deferred.add(new org.objectweb.asm.tree.VarInsnNode(
                    org.objectweb.asm.Opcodes.ALOAD, 0));
            deferred.add(new LdcInsnNode(field.name));
            deferred.add(new MethodInsnNode(
                    org.objectweb.asm.Opcodes.INVOKESTATIC,
                    "com/fuckingdeveloper/lms/runtime/Forge1192ClientLifecycleBridge",
                    "populateMinecraftFieldWhenReady",
                    "(Ljava/lang/Object;Ljava/lang/String;)V", false));
            constructor.instructions.insert(store, deferred);
            rewrites++;
        }
        return rewrites;
    }

    private static org.objectweb.asm.tree.AbstractInsnNode lastExecutable(
            org.objectweb.asm.tree.MethodNode method) {
        var cursor = method.instructions.getLast();
        while (cursor != null && cursor.getOpcode() < 0) cursor = cursor.getPrevious();
        return cursor;
    }

    private static boolean emptyStackAt(
            String owner, org.objectweb.asm.tree.MethodNode method,
            org.objectweb.asm.tree.AbstractInsnNode instruction) {
        try {
            var analyzer = new org.objectweb.asm.tree.analysis.Analyzer<
                    org.objectweb.asm.tree.analysis.BasicValue>(
                    new org.objectweb.asm.tree.analysis.BasicInterpreter());
            var frames = analyzer.analyze(owner, method);
            var frame = frames[method.instructions.indexOf(instruction)];
            return frame != null && frame.getStackSize() == 0;
        } catch (org.objectweb.asm.tree.analysis.AnalyzerException e) {
            return false;
        }
    }

    private static String uniqueHelperName(ClassNode node, String base) {
        String candidate = base;
        int suffix = 1;
        while (true) {
            String current = candidate;
            if (node.methods.stream().noneMatch(method ->
                    method.name.equals(current) && method.desc.equals("()V"))) return candidate;
            candidate = base + "$" + suffix++;
        }
    }

    /**
     * CraftingContainer changed from a concrete 1.19.2 inventory to an interface.
     * TransientCraftingContainer is its vanilla concrete successor. Only migrate
     * construction when the target runtime proves an identical public constructor.
     */
    private static int rewriteLegacyBlockEntityTypeConstruction(ClassNode node) {
        int rewrites = 0;
        final String owner = "net/minecraft/world/level/block/entity/BlockEntityType";
        final String legacyDesc = "(Lnet/minecraft/world/level/block/entity/BlockEntityType$BlockEntitySupplier;Ljava/util/Set;Lcom/mojang/datafixers/types/Type;)V";
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; ) {
                var next = insn.getNext();
                if (insn instanceof TypeInsnNode allocation
                        && allocation.getOpcode() == org.objectweb.asm.Opcodes.NEW
                        && allocation.desc.equals(owner)) {
                    var dup = allocation.getNext();
                    MethodInsnNode init = null;
                    int nested = 0;
                    if (dup != null && dup.getOpcode() == org.objectweb.asm.Opcodes.DUP) {
                        for (var cursor = dup.getNext(); cursor != null; cursor = cursor.getNext()) {
                            if (cursor instanceof TypeInsnNode nestedType
                                    && nestedType.getOpcode() == org.objectweb.asm.Opcodes.NEW) nested++;
                            else if (cursor instanceof MethodInsnNode call
                                    && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESPECIAL
                                    && call.name.equals("<init>")) {
                                if (nested > 0) nested--;
                                else if (call.owner.equals(owner)) { init = call; break; }
                            }
                        }
                    }
                    if (init != null && init.desc.equals(legacyDesc)) {
                        method.instructions.remove(allocation);
                        method.instructions.remove(dup);
                        MethodInsnNode factory = new MethodInsnNode(
                                org.objectweb.asm.Opcodes.INVOKESTATIC,
                                "com/fuckingdeveloper/lms/runtime/Forge1192VanillaConstructionBridge",
                                "blockEntityType",
                                "(Lnet/minecraft/world/level/block/entity/BlockEntityType$BlockEntitySupplier;Ljava/util/Set;Lcom/mojang/datafixers/types/Type;)Lnet/minecraft/world/level/block/entity/BlockEntityType;",
                                false);
                        method.instructions.set(init, factory);
                        next = factory.getNext();
                        rewrites++;
                    }
                }
                insn = next;
            }
            // Legacy subclasses invoke the removed three-argument constructor
            // directly on super and therefore have no NEW/DUP pair to replace.
            // Discard the obsolete DFU Type value and bind to the verified
            // 26.3 direct constructor while preserving supplier + valid blocks.
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof MethodInsnNode call
                        && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESPECIAL
                        && call.owner.equals(owner)
                        && call.name.equals("<init>")
                        && call.desc.equals(legacyDesc)) {
                    method.instructions.insertBefore(call,
                            new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.POP));
                    call.desc = "(Lnet/minecraft/world/level/block/entity/BlockEntityType$BlockEntitySupplier;Ljava/util/Set;)V";
                    rewrites++;
                }
            }
        }
        return rewrites;
    }

    private static int rewriteLegacyCraftingContainerConstruction(ClassNode node) {
        final String legacy = "net/minecraft/world/inventory/CraftingContainer";
        final String target = "net/minecraft/world/inventory/TransientCraftingContainer";
        int rewrites = 0;
        var constructors = VANILLA_RELOCATIONS.constructorsOf(target);
        if (node.superName.equals(legacy)) {
            boolean migratedConstructor = false;
            for (var method : node.methods) {
                if (!method.name.equals("<init>")) continue;
                MethodInsnNode superCall = null;
                for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                    if (insn instanceof MethodInsnNode call
                            && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESPECIAL
                            && call.owner.equals(legacy)
                            && call.name.equals("<init>")) {
                        if (superCall != null) {
                            throw new IllegalStateException("Ambiguous legacy CraftingContainer super call: "
                                    + node.name + method.desc);
                        }
                        superCall = call;
                    }
                }
                if (superCall == null || !constructors.contains(superCall.desc)) {
                    throw new IllegalStateException("CraftingContainer subclass constructor requires semantic adapter: "
                            + node.name + method.desc + "; target public constructors=" + constructors);
                }
                superCall.owner = target;
                migratedConstructor = true;
                rewrites++;
            }
            if (!migratedConstructor) {
                throw new IllegalStateException("Legacy CraftingContainer subclass has no explicit constructor: "
                        + node.name);
            }
            node.superName = target;
            rewrites++;
        }
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof TypeInsnNode allocation)
                        || allocation.getOpcode() != org.objectweb.asm.Opcodes.NEW
                        || !allocation.desc.equals(legacy)) continue;
                // Only pair a standard NEW/DUP with its matching constructor.
                var dup = allocation.getNext();
                if (dup == null || dup.getOpcode() != org.objectweb.asm.Opcodes.DUP) {
                    throw new IllegalStateException("Unsupported legacy CraftingContainer allocation pattern: "
                            + node.name + "#" + method.name);
                }
                MethodInsnNode init = null;
                int nestedAllocations = 0;
                for (var cursor = dup.getNext(); cursor != null; cursor = cursor.getNext()) {
                    if (cursor instanceof TypeInsnNode nested
                            && nested.getOpcode() == org.objectweb.asm.Opcodes.NEW) {
                        nestedAllocations++;
                        continue;
                    }
                    if (cursor instanceof MethodInsnNode candidate
                            && candidate.getOpcode() == org.objectweb.asm.Opcodes.INVOKESPECIAL
                            && candidate.name.equals("<init>")) {
                        if (nestedAllocations > 0) {
                            nestedAllocations--;
                            continue;
                        }
                        if (candidate.owner.equals(legacy)) {
                            init = candidate;
                        }
                        break;
                    }
                }
                if (init == null) {
                    throw new IllegalStateException("Cannot pair legacy CraftingContainer allocation with constructor: "
                            + node.name + "#" + method.name);
                }
                if (!constructors.contains(init.desc)) {
                    throw new IllegalStateException("CraftingContainer constructor requires semantic adapter: "
                            + node.name + "#" + method.name + init.desc
                            + "; target public constructors=" + constructors);
                }
                allocation.desc = target;
                init.owner = target;
                rewrites++;
            }
        }
        return rewrites;
    }

    private int rewriteSemanticAdapters(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)) continue;

                // Authlib's legacy UUIDTypeAdapter helper is not ABI-stable across
                // Minecraft generations. Preserve the Forge 1.19.2 executable
                // contract behind an LMS boundary instead of linking against the
                // target Authlib implementation by name.
                if (call.owner.equals("com/mojang/util/UUIDTypeAdapter")
                        && call.name.equals("fromString")
                        && call.desc.equals("(Ljava/lang/String;)Ljava/util/UUID;")
                        && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESTATIC) {
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192UuidBridge";
                    call.name = "fromString";
                    call.desc = "(Ljava/lang/String;)Ljava/util/UUID;";
                    call.itf = false;
                    rewrites++;
                }

                // Removed ForgeMod RegistryObject fields are converted to
                // explicit LMS builtin handles. Their RegistryObject operations are
                // adapted below, so the removed Forge holder never reaches linkage.
                if ((call.owner.equals("net/minecraftforge/registries/RegistryObject")
                        || call.owner.equals("net/neoforged/neoforge/registries/RegistryObject")
                        || call.owner.equals("com/fuckingdeveloper/lms/runtime/registry/LegacyRegistryObject"))
                        && call.name.equals("get")
                        && call.desc.equals("()Ljava/lang/Object;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192BuiltinRegistryBridge";
                    call.name = "get";
                    call.desc = "(Lcom/fuckingdeveloper/lms/runtime/registry/LegacyRegistryObject;)Ljava/lang/Object;";
                    call.itf = false;
                    rewrites++;
                }

                // Forge 1.19.2 exposed enableMilkFluid() as an opt-in bootstrap
                // request. The removed ForgeMod holder must never escape into the
                // target loader; preserve the operation as an explicit LMS boundary.
                if (call.owner.equals("net/minecraftforge/common/ForgeMod")
                        && call.name.equals("enableMilkFluid")
                        && call.desc.equals("()V")
                        && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESTATIC) {
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192BuiltinRegistryBridge";
                    call.name = "enableMilkFluid";
                    call.desc = "()V";
                    call.itf = false;
                    rewrites++;
                }

                // Removed Forge 1.19.2 static lifecycle contexts are represented by
                // scoped LMS tokens. Instance calls consume and validate those tokens.
                if (call.owner.equals("net/neoforged/fml/javafmlmod/FMLJavaModLoadingContext")
                        && call.name.equals("get")
                        && call.desc.equals("()Lnet/neoforged/fml/javafmlmod/FMLJavaModLoadingContext;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192LifecycleBridge";
                    call.name = "getJavaModLoadingContext";
                    call.desc = "()Ljava/lang/Object;";
                    call.itf = false;
                    rewrites++;
                }
                if (call.owner.equals("net/neoforged/fml/javafmlmod/FMLJavaModLoadingContext")
                        && call.name.equals("getModEventBus")
                        && call.desc.equals("()Lnet/neoforged/bus/api/IEventBus;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192LifecycleBridge";
                    call.name = "getModEventBus";
                    call.desc = "(Ljava/lang/Object;)Lnet/neoforged/bus/api/IEventBus;";
                    call.itf = false;
                    rewrites++;
                }
                if (call.owner.equals("net/neoforged/fml/ModLoadingContext")
                        && call.name.equals("get")
                        && call.desc.equals("()Lnet/neoforged/fml/ModLoadingContext;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192LifecycleBridge";
                    call.name = "getModLoadingContext";
                    call.desc = "()Ljava/lang/Object;";
                    call.itf = false;
                    rewrites++;
                }
                if (call.owner.equals("net/neoforged/fml/ModLoadingContext")
                        && call.name.equals("getActiveContainer")
                        && call.desc.equals("()Lnet/neoforged/fml/ModContainer;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192LifecycleBridge";
                    call.name = "getActiveContainer";
                    call.desc = "(Ljava/lang/Object;)Lnet/neoforged/fml/ModContainer;";
                    call.itf = false;
                    rewrites++;
                }
                if (call.owner.equals("net/neoforged/fml/ModLoadingContext")
                        && call.name.equals("setActiveContainer")
                        && call.desc.equals("(Lnet/neoforged/fml/ModContainer;)V")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192LifecycleBridge";
                    call.name = "setActiveContainer";
                    call.desc = "(Ljava/lang/Object;Ljava/lang/Object;)V";
                    call.itf = false;
                    rewrites++;
                }
                if (call.owner.equals("net/neoforged/fml/ModLoadingContext")
                        && call.name.equals("getActiveNamespace")
                        && call.desc.equals("()Ljava/lang/String;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192LifecycleBridge";
                    call.name = "getActiveNamespace";
                    call.desc = "(Ljava/lang/Object;)Ljava/lang/String;";
                    call.itf = false;
                    rewrites++;
                }

                // Constructor-only adaptation for legacy subclasses. The
                // builder supplies the modern superclass state, while the
                // original subclass and its virtual icon method remain intact.
                // Creative inventory registration/content still require a
                // separate, verified lifecycle migration.
                if (call.owner.equals("net/minecraft/world/item/CreativeModeTab")
                        && call.name.equals("<init>")
                        && call.desc.equals("(Ljava/lang/String;)V")) {
                    if (!node.superName.equals("net/minecraft/world/item/CreativeModeTab")
                            || !method.name.equals("<init>")) {
                        throw new IllegalStateException(
                                "Unsupported legacy CreativeModeTab constructor context: "
                                        + node.name + "#" + method.name);
                    }
                    method.instructions.insertBefore(call, new MethodInsnNode(
                            org.objectweb.asm.Opcodes.INVOKESTATIC,
                            "com/fuckingdeveloper/lms/runtime/Forge1192CreativeTabBridge",
                            "builder",
                            "(Ljava/lang/String;)Lnet/minecraft/world/item/CreativeModeTab$Builder;",
                            false));
                    call.desc = "(Lnet/minecraft/world/item/CreativeModeTab$Builder;)V";
                    rewrites++;
                }

                // Forge 1.19.2 Dist exposed convenience predicates that are
                // absent from the current enum. Preserve physical-side semantics.
                if (call.owner.equals("net/neoforged/api/distmarker/Dist")
                        && call.name.equals("isClient") && call.desc.equals("()Z")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192SideBridge";
                    call.name = "isClient";
                    call.desc = "(Lnet/neoforged/api/distmarker/Dist;)Z";
                    call.itf = false;
                    rewrites++;
                }
                if (call.owner.equals("net/neoforged/api/distmarker/Dist")
                        && call.name.equals("isDedicatedServer") && call.desc.equals("()Z")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192SideBridge";
                    call.name = "isDedicatedServer";
                    call.desc = "(Lnet/neoforged/api/distmarker/Dist;)Z";
                    call.itf = false;
                    rewrites++;
                }

                // EffectiveSide is a logical-side query. Keep it distinct from
                // physical Dist and route through a profile-owned semantic boundary.
                if (call.owner.equals("net/neoforged/fml/util/thread/EffectiveSide")
                        && call.name.equals("get")
                        && call.desc.equals("()Lnet/neoforged/fml/LogicalSide;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192SideBridge";
                    call.name = "getEffectiveSide";
                    call.desc = "()Lnet/neoforged/fml/LogicalSide;";
                    call.itf = false;
                    rewrites++;
                }
                if (call.owner.equals("net/neoforged/neoforge/event/entity/living/LivingShieldBlockEvent")
                        && call.name.equals("setShieldTakesDamage") && call.desc.equals("(Z)V")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192LivingEventBridge";
                    call.desc = "(Lnet/neoforged/neoforge/event/entity/living/LivingShieldBlockEvent;Z)V";
                    call.itf = false;
                    rewrites++;
                }
                // RegisterEvent no longer exposes Forge 1.19.2's
                // IForgeRegistry facade. Preserve the registry key directly;
                // mutable registry operations are handled by scoped adapters.
                if (call.owner.equals("net/neoforged/neoforge/registries/RegisterEvent")
                        && call.name.equals("getRegistryKey")
                        && call.desc.equals("()Lnet/minecraft/resources/ResourceKey;")) {
                    // Exact modern API after ResourceLocation -> Identifier migration.
                }
                // Forge 1.19.2 exposed IForgeRegistry from RegisterEvent.
                // NeoForge exposes the concrete Registry instead. Rewrite the
                // producer and all known legacy consumer calls as one contract.
                if (call.owner.equals("net/neoforged/neoforge/registries/RegisterEvent")
                        && call.name.equals("getForgeRegistry")
                        && (call.desc.equals("()Lnet/neoforged/neoforge/registries/IForgeRegistry;")
                            || call.desc.equals("()Lnet/minecraftforge/registries/IForgeRegistry;")
                            || call.desc.equals("()Lcom/fuckingdeveloper/lms/runtime/registry/LegacyForgeRegistry;"))) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192RegistrationContext";
                    call.name = "activeRegistryObject";
                    call.desc = "(Lnet/neoforged/neoforge/registries/RegisterEvent;)Ljava/lang/Object;";
                    call.itf = false;
                    rewrites++;
                }
                if (call.owner.equals("net/neoforged/neoforge/registries/IForgeRegistry")
                        || call.owner.equals("com/fuckingdeveloper/lms/runtime/registry/LegacyForgeRegistry")) {
                    String bridgeDesc = switch (call.name) {
                        case "containsKey" -> call.desc.equals("(Lnet/minecraft/resources/Identifier;)Z")
                                ? "(Ljava/lang/Object;Lnet/minecraft/resources/Identifier;)Z" : null;
                        case "getValue" -> call.desc.equals("(Lnet/minecraft/resources/Identifier;)Ljava/lang/Object;")
                                ? "(Ljava/lang/Object;Lnet/minecraft/resources/Identifier;)Ljava/lang/Object;" : null;
                        case "getKey" -> call.desc.equals("(Ljava/lang/Object;)Lnet/minecraft/resources/Identifier;")
                                ? "(Ljava/lang/Object;Ljava/lang/Object;)Lnet/minecraft/resources/Identifier;" : null;
                        case "iterator" -> call.desc.equals("()Ljava/util/Iterator;")
                                ? "(Ljava/lang/Object;)Ljava/util/Iterator;" : null;
                        default -> null;
                    };
                    if (bridgeDesc != null) {
                        call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                        call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192RegistrationContext";
                        call.desc = bridgeDesc;
                        call.itf = false;
                        rewrites++;
                    }
                }
                // Forge 1.19.2 exposed stable, reload-aware ITagManager/ITag
                // views from each registry. Preserve that semantic contract
                // over the target registry's live HolderSet bindings.
                if (call.owner.equals("com/fuckingdeveloper/lms/runtime/registry/LegacyForgeRegistry")
                        && call.name.equals("tags")
                        && call.desc.equals("()Lcom/fuckingdeveloper/lms/runtime/registry/LegacyTagManager;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/registry/Forge1192RegistryTagBridge";
                    call.desc = "(Ljava/lang/Object;)Lcom/fuckingdeveloper/lms/runtime/registry/LegacyTagManager;";
                    call.itf = false;
                    rewrites++;
                }
                // Legacy IForgeRegistry#register is no longer a mutable
                // registry call. Route it through the active RegisterEvent scope.
                if ((call.owner.equals("net/neoforged/neoforge/registries/IForgeRegistry")
                        || call.owner.equals("com/fuckingdeveloper/lms/runtime/registry/LegacyForgeRegistry"))
                        && call.name.equals("register")
                        && call.desc.equals("(Ljava/lang/String;Ljava/lang/Object;)V")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192RegistrationContext";
                    call.desc = "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;)V";
                    call.itf = false;
                    rewrites++;
                }
                if ((call.owner.equals("net/neoforged/neoforge/registries/IForgeRegistry")
                        || call.owner.equals("com/fuckingdeveloper/lms/runtime/registry/LegacyForgeRegistry"))
                        && call.name.equals("register")
                        && call.desc.equals("(Lnet/minecraft/resources/Identifier;Ljava/lang/Object;)V")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192RegistrationContext";
                    call.desc = "(Ljava/lang/Object;Lnet/minecraft/resources/Identifier;Ljava/lang/Object;)V";
                    call.itf = false;
                    rewrites++;
                }
                // The old GameData helper relied on implicit active mod
                // registration context. Redirect to an explicit scoped bridge.
                if ((call.owner.equals("net/neoforged/neoforge/registries/GameData")
                        || call.owner.equals("net/minecraftforge/registries/GameData"))
                        && call.name.equals("checkPrefix")
                        && call.desc.equals("(Ljava/lang/String;Z)Lnet/minecraft/resources/Identifier;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192RegistrationContext";
                    call.desc = "(Ljava/lang/String;Z)Lnet/minecraft/resources/Identifier;";
                    call.itf = false;
                    rewrites++;
                }
                // Forge 1.19.2 FriendlyByteBuf registry-id serialization used
                // IForgeRegistry as an explicit registry token. LMS represents that
                // token as the concrete vanilla Registry and preserves the raw-id
                // varint wire contract in the network bridge.
                if (call.owner.equals("net/minecraft/network/FriendlyByteBuf")
                        && call.name.equals("writeRegistryIdUnsafe")
                        && call.desc.equals("(Lnet/neoforged/neoforge/registries/IForgeRegistry;Ljava/lang/Object;)V")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192NetworkBridge";
                    call.name = "writeRegistryIdUnsafe";
                    call.desc = "(Lnet/minecraft/network/FriendlyByteBuf;Ljava/lang/Object;Ljava/lang/Object;)V";
                    call.itf = false;
                    rewrites++;
                }

                if (call.owner.equals("net/minecraft/network/FriendlyByteBuf")
                        && call.name.equals("readRegistryIdUnsafe")
                        && call.desc.equals("(Lnet/neoforged/neoforge/registries/IForgeRegistry;)Ljava/lang/Object;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192NetworkBridge";
                    call.name = "readRegistryIdUnsafe";
                    call.desc = "(Lnet/minecraft/network/FriendlyByteBuf;Ljava/lang/Object;)Ljava/lang/Object;";
                    call.itf = false;
                    rewrites++;
                }

                // Forge 1.19.2 SimpleChannel is a protocol object, not a class
                // that can be namespace-migrated into current NeoForge. Replace
                // construction with an LMS-owned token; message registration stays
                // fail-closed until codec/payload semantics are adapted.
                if (call.owner.equals("net/neoforged/neoforge/network/NetworkRegistry")
                        && call.name.equals("newSimpleChannel")
                        && call.desc.equals("(Lnet/minecraft/resources/Identifier;Ljava/util/function/Supplier;Ljava/util/function/Predicate;Ljava/util/function/Predicate;)Lnet/neoforged/neoforge/network/simple/SimpleChannel;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192NetworkBridge";
                    call.name = "newSimpleChannel";
                    call.desc = "(Lnet/minecraft/resources/Identifier;Ljava/util/function/Supplier;Ljava/util/function/Predicate;Ljava/util/function/Predicate;)Ljava/lang/Object;";
                    call.itf = false;
                    rewrites++;
                }
                if (call.owner.equals("net/neoforged/neoforge/network/simple/SimpleChannel")
                        && call.name.equals("registerMessage")
                        && call.desc.equals("(ILjava/lang/Class;Ljava/util/function/BiConsumer;Ljava/util/function/Function;Ljava/util/function/BiConsumer;)Lnet/neoforged/neoforge/network/simple/IndexedMessageCodec$MessageHandler;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192NetworkBridge";
                    call.name = "registerMessage";
                    call.desc = "(Ljava/lang/Object;ILjava/lang/Class;Ljava/util/function/BiConsumer;Ljava/util/function/Function;Ljava/util/function/BiConsumer;)Ljava/lang/Object;";
                    call.itf = false;
                    rewrites++;
                }

                if (call.owner.equals("net/neoforged/neoforge/network/NetworkEvent$Context")) {
                    String bridgeDesc = switch (call.name + call.desc) {
                        case "enqueueWork(Ljava/lang/Runnable;)Ljava/util/concurrent/CompletableFuture;" ->
                                "(Ljava/lang/Object;Ljava/lang/Runnable;)Ljava/util/concurrent/CompletableFuture;";
                        case "getSender()Lnet/minecraft/server/level/ServerPlayer;" ->
                                "(Ljava/lang/Object;)Lnet/minecraft/server/level/ServerPlayer;";
                        case "setPacketHandled(Z)V" -> "(Ljava/lang/Object;Z)V";
                        default -> null;
                    };
                    if (bridgeDesc != null) {
                        call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                        call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192NetworkBridge";
                        call.desc = bridgeDesc;
                        call.itf = false;
                        rewrites++;
                    }
                }

                // Current NeoForge EventHooks posts the same NeighborNotifyEvent
                // and returns it; the old ForgeEventFactory owner was removed.
                if (call.owner.equals("net/neoforged/neoforge/event/ForgeEventFactory")
                        && call.name.equals("onNeighborNotify")
                        && call.desc.equals("(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Ljava/util/EnumSet;Z)Lnet/neoforged/neoforge/event/level/BlockEvent$NeighborNotifyEvent;")) {
                    call.owner = "net/neoforged/neoforge/event/EventHooks";
                    rewrites++;
                }

                // Forge 1.19.2 capability type declarations no longer map
                // to a mutable modern capability registry. Validate/capture the
                // declaration without inventing a global registration side effect.
                if (call.owner.equals("net/neoforged/neoforge/capabilities/RegisterCapabilitiesEvent")
                        && call.name.equals("register")
                        && call.desc.equals("(Ljava/lang/Class;)V")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192CapabilityBridge";
                    call.name = "register";
                    call.desc = "(Lnet/neoforged/neoforge/capabilities/RegisterCapabilitiesEvent;Ljava/lang/Class;)V";
                    call.itf = false;
                    rewrites++;
                }

                // Legacy ForgeMod milk-fluid opt-in is an imperative profile
                // request. Capture it explicitly rather than invoking a removed API.
                if (call.owner.equals("net/neoforged/neoforge/common/ForgeMod")
                        && call.name.equals("enableMilkFluid")
                        && call.desc.equals("()V")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192CommonBridge";
                    call.name = "enableMilkFluid";
                    call.desc = "()V";
                    call.itf = false;
                    rewrites++;
                }

                // Removed LazyOptional operations retain an explicit LMS
                // invalidation token. Do not silently substitute java.util.Optional.
                if (call.owner.equals("net/neoforged/neoforge/common/util/LazyOptional")
                        && call.name.equals("empty")
                        && call.desc.equals("()Lnet/neoforged/neoforge/common/util/LazyOptional;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192LazyOptionalBridge";
                    call.name = "empty";
                    call.desc = "()Ljava/lang/Object;";
                    call.itf = false;
                    rewrites++;
                }
                if (call.owner.equals("net/neoforged/neoforge/common/util/LazyOptional")
                        && call.name.equals("invalidate")
                        && call.desc.equals("()V")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192LazyOptionalBridge";
                    call.name = "invalidate";
                    call.desc = "(Ljava/lang/Object;)V";
                    call.itf = false;
                    rewrites++;
                }

                // Forge 1.19.2 FluidStack#isFluidEqual compares fluid identity,
                // not amount or data components. Keep the old contract explicit.
                if (call.owner.equals("net/neoforged/neoforge/fluids/FluidStack")
                        && call.name.equals("isFluidEqual")
                        && call.desc.equals("(Lnet/neoforged/neoforge/fluids/FluidStack;)Z")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192FluidBridge";
                    call.name = "isFluidEqual";
                    call.desc = "(Lnet/neoforged/neoforge/fluids/FluidStack;Lnet/neoforged/neoforge/fluids/FluidStack;)Z";
                    call.itf = false;
                    rewrites++;
                }
                if (call.owner.equals("net/minecraftforge/fluids/FluidUtil")
                        && call.name.equals("getFluidContained")
                        && call.desc.equals("(Lnet/minecraft/world/item/ItemStack;)Ljava/util/Optional;")) {
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192FluidBridge";
                    call.itf = false;
                    rewrites++;
                }
                // Legacy listeners capture the active legacy mod identity when
                // registered. RegisterEvent callbacks additionally receive a scoped
                // registration context, so old registry facade adapters execute only
                // during the legal NeoForge registration phase.
                if (call.owner.equals("net/neoforged/bus/api/IEventBus")
                        && call.name.equals("addListener")
                        && call.desc.equals("(Ljava/util/function/Consumer;)V")) {
                    Type eventType = inferLambdaEventType(call);
                    if (eventType == null)
                        throw new IllegalStateException("Cannot infer legacy lambda event type: "
                                + node.name + "#" + method.name + method.desc);
                    boolean deferUntilItemComponentsBound =
                            requiresComponentBoundLifecycle(call, eventType, node, method);
                    method.instructions.insertBefore(call, new org.objectweb.asm.tree.InsnNode(
                            deferUntilItemComponentsBound
                                    ? org.objectweb.asm.Opcodes.ICONST_1
                                    : org.objectweb.asm.Opcodes.ICONST_0));
                    method.instructions.insertBefore(call, new LdcInsnNode(eventType));
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192EventBusBridge";
                    call.name = "addListener";
                    call.desc = "(Lnet/neoforged/bus/api/IEventBus;Ljava/util/function/Consumer;ZLjava/lang/Class;)V";
                    call.itf = false;
                    rewrites++;
                }
                if (call.owner.equals("net/neoforged/bus/api/IEventBus")
                        && call.name.equals("addListener")
                        && call.desc.equals("(Lnet/neoforged/bus/api/EventPriority;Ljava/util/function/Consumer;)V")) {
                    Type eventType = inferLambdaEventType(call);
                    if (eventType == null)
                        throw new IllegalStateException("Cannot infer prioritized legacy lambda event type: "
                                + node.name + "#" + method.name + method.desc);
                    boolean deferUntilItemComponentsBound =
                            requiresComponentBoundLifecycle(call, eventType, node, method);
                    method.instructions.insertBefore(call, new org.objectweb.asm.tree.InsnNode(
                            deferUntilItemComponentsBound
                                    ? org.objectweb.asm.Opcodes.ICONST_1
                                    : org.objectweb.asm.Opcodes.ICONST_0));
                    method.instructions.insertBefore(call, new LdcInsnNode(eventType));
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192EventBusBridge";
                    call.name = "addListener";
                    call.desc = "(Lnet/neoforged/bus/api/IEventBus;Lnet/neoforged/bus/api/EventPriority;Ljava/util/function/Consumer;ZLjava/lang/Class;)V";
                    call.itf = false;
                    rewrites++;
                }

                if (call.owner.equals("net/neoforged/bus/api/IEventBus")
                        && call.name.equals("register")
                        && call.desc.equals("(Ljava/lang/Object;)V")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192EventBusBridge";
                    call.name = "register";
                    call.desc = "(Lnet/neoforged/bus/api/IEventBus;Ljava/lang/Object;)V";
                    call.itf = false;
                    rewrites++;
                }

                // Forge 1.19.2 IEventBus#post returned cancellation state.
                // NeoForge's EventBus returns the posted Event. Preserve the
                // legacy boolean contract through an LMS runtime adapter.
                if (call.owner.equals("net/neoforged/bus/api/IEventBus")
                        && call.name.equals("post")
                        && call.desc.equals("(Lnet/neoforged/bus/api/Event;)Z")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192EventBusBridge";
                    call.name = "post";
                    call.desc = "(Lnet/neoforged/bus/api/IEventBus;Lnet/neoforged/bus/api/Event;)Z";
                    call.itf = false;
                    rewrites++;
                }
            }
        }
        return rewrites;
    }

    private static Type inferLambdaEventType(MethodInsnNode addListenerCall) {
        InvokeDynamicInsnNode indy = findLambdaConsumer(addListenerCall);
        if (indy == null) return null;
        // LambdaMetafactory's instantiated method type is conventionally
        // bootstrap arg #2. It preserves Consumer<ConcreteEvent> even though
        // the runtime Consumer object itself is erased.
        if (indy.bsmArgs.length >= 3 && indy.bsmArgs[2] instanceof Type instantiated
                && instantiated.getSort() == Type.METHOD) {
            Type[] args = instantiated.getArgumentTypes();
            if (args.length == 1 && args[0].getSort() == Type.OBJECT)
                return Type.getObjectType(migrateInternalName(args[0].getInternalName()));
        }

        // Method-reference/lambda fallback: use the final object argument
        // of the implementation handle after captured arguments.
        Handle handle = lambdaImplementationHandle(indy);
        if (handle != null) {
            Type[] args = Type.getMethodType(handle.getDesc()).getArgumentTypes();
            if (args.length > 0 && args[args.length - 1].getSort() == Type.OBJECT)
                return Type.getObjectType(migrateInternalName(args[args.length - 1].getInternalName()));
        }
        return null;
    }

    private static InvokeDynamicInsnNode findLambdaConsumer(MethodInsnNode addListenerCall) {
        int realInstructions = 0;
        for (var previous = addListenerCall.getPrevious();
             previous != null && realInstructions < 24; previous = previous.getPrevious()) {
            if (previous.getOpcode() >= 0) realInstructions++;
            if (!(previous instanceof InvokeDynamicInsnNode indy)) continue;
            if (!indy.desc.endsWith(")Ljava/util/function/Consumer;")) continue;
            return indy;
        }
        return null;
    }

    private static Handle lambdaImplementationHandle(InvokeDynamicInsnNode indy) {
        if (indy.bsmArgs.length >= 2 && indy.bsmArgs[1] instanceof Handle handle) return handle;
        return null;
    }

    private boolean requiresComponentBoundLifecycle(
            MethodInsnNode addListenerCall, Type eventType, ClassNode caller,
            org.objectweb.asm.tree.MethodNode callerMethod) {
        if (!eventType.getInternalName().startsWith(
                "net/neoforged/fml/event/lifecycle/")) return false;
        InvokeDynamicInsnNode indy = findLambdaConsumer(addListenerCall);
        Handle implementation = indy == null ? null : lambdaImplementationHandle(indy);
        if (implementation == null) return false;
        Handle sourceImplementation = sourceLifecycleHandle(implementation);
        if (!usesItemStackDefaults(sourceImplementation.getOwner(), sourceImplementation.getName(),
                sourceImplementation.getDesc())) return false;
        if (!lambdaEventParameterIsUnused(sourceImplementation)) {
            throw new IllegalStateException("Legacy lifecycle callback requires item-component deferral but reads its "
                    + "source event parameter: " + caller.name + "#" + callerMethod.name
                    + callerMethod.desc + " -> " + implementation.getOwner() + '#'
                    + implementation.getName() + implementation.getDesc());
        }
        return true;
    }

    private static Handle sourceLifecycleHandle(Handle implementation) {
        String sourceDescriptor = implementation.getDesc().replace(
                "Lnet/neoforged/fml/event/lifecycle/",
                "Lnet/minecraftforge/fml/event/lifecycle/");
        if (sourceDescriptor.equals(implementation.getDesc())) return implementation;
        return new Handle(implementation.getTag(), implementation.getOwner(), implementation.getName(),
                sourceDescriptor, implementation.isInterface());
    }

    private boolean lambdaEventParameterIsUnused(Handle implementation) {
        ClassNode owner = readManagedClass(implementation.getOwner());
        if (owner == null) return false;
        for (var method : owner.methods) {
            if (!method.name.equals(implementation.getName())
                    || !method.desc.equals(implementation.getDesc())) continue;
            Type[] arguments = Type.getArgumentTypes(method.desc);
            if (arguments.length == 0 || arguments[arguments.length - 1].getSort() != Type.OBJECT) return false;
            int slot = (method.access & org.objectweb.asm.Opcodes.ACC_STATIC) == 0 ? 1 : 0;
            for (int i = 0; i < arguments.length - 1; i++) slot += arguments[i].getSize();
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof org.objectweb.asm.tree.VarInsnNode variable
                        && variable.getOpcode() == org.objectweb.asm.Opcodes.ALOAD
                        && variable.var == slot) return false;
            }
            return true;
        }
        return false;
    }

    private static void verifyNoEscapingLegacyNetworkFacade(ClassNode node) {
        String simpleChannel = "net/neoforged/neoforge/network/simple/SimpleChannel";
        String networkContext = "net/neoforged/neoforge/network/NetworkEvent$Context";
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)) continue;
                if (call.owner.equals("net/neoforged/neoforge/network/NetworkRegistry")
                        && call.name.equals("newSimpleChannel"))
                    throw new IllegalStateException("Unadapted legacy SimpleChannel construction: "
                            + node.name + "#" + method.name + call.desc);
                if (call.owner.equals(simpleChannel) && !call.name.equals("registerMessage"))
                    throw new IllegalStateException("Unsupported legacy SimpleChannel operation: "
                            + node.name + "#" + method.name + " -> " + call.name + call.desc);
                if (call.owner.equals(networkContext))
                    throw new IllegalStateException("Unadapted legacy NetworkEvent.Context operation: "
                            + node.name + "#" + method.name + " -> " + call.name + call.desc);
            }
        }
    }

    /**
     * ForgeMod is a built-in Forge registry holder, not a portable class alias.
     * Report the exact static field/method contract before JVM linkage, so
     * migration rules can be proven against the target registry rather than
     * inventing an empty compatibility class.
     */
    private static void verifyUnresolvedForgeModAccesses(ClassNode node) {
        final String legacyOwner = "net/minecraftforge/common/ForgeMod";
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof FieldInsnNode field && field.owner.equals(legacyOwner)) {
                    throw new IllegalStateException("UNRESOLVED_FORGE_BUILTIN_REGISTRY_FIELD "
                            + node.name + "#" + method.name + method.desc
                            + " opcode=" + field.getOpcode()
                            + " field=" + field.owner + "." + field.name + ":" + field.desc);
                }
                if (insn instanceof MethodInsnNode call && call.owner.equals(legacyOwner)) {
                    throw new IllegalStateException("UNRESOLVED_FORGE_BUILTIN_REGISTRY_CALL "
                            + node.name + "#" + method.name + method.desc
                            + " opcode=" + call.getOpcode()
                            + " call=" + call.owner + "." + call.name + call.desc);
                }
            }
        }
    }

    private static void verifyNoUnadaptedLifecycleCalls(ClassNode node) {
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)) continue;
                if (call.owner.equals("net/neoforged/fml/ModLoadingContext")
                        && (call.name.equals("getActiveContainer") || call.name.equals("setActiveContainer"))) {
                    throw new IllegalStateException("Unadapted legacy ModLoadingContext boundary: "
                            + node.name + "#" + method.name + " -> " + call.name + call.desc);
                }
            }
        }
    }

    private static Handle migrateHandle(Handle handle) {
        String owner = migrateInternalName(handle.getOwner());
        String desc = migrateDescriptor(handle.getDesc());
        if (owner.equals(handle.getOwner()) && desc.equals(handle.getDesc())) return handle;
        return new Handle(handle.getTag(), owner, handle.getName(), desc, handle.isInterface());
    }

    private static Object migrateConstant(Object value) {
        if (value instanceof Type type) {
            String descriptor = migrateDescriptor(type.getDescriptor());
            return descriptor.equals(type.getDescriptor()) ? value : Type.getType(descriptor);
        }
        if (value instanceof Handle handle) return migrateHandle(handle);
        if (value instanceof ConstantDynamic dynamic) {
            String descriptor = migrateDescriptor(dynamic.getDescriptor());
            Handle bootstrap = migrateHandle(dynamic.getBootstrapMethod());
            Object[] args = new Object[dynamic.getBootstrapMethodArgumentCount()];
            boolean changed = !descriptor.equals(dynamic.getDescriptor()) || bootstrap != dynamic.getBootstrapMethod();
            for (int i = 0; i < args.length; i++) {
                Object old = dynamic.getBootstrapMethodArgument(i);
                args[i] = migrateConstant(old);
                changed |= args[i] != old;
            }
            return changed ? new ConstantDynamic(dynamic.getName(), descriptor, bootstrap, args) : value;
        }
        return value;
    }

    /** Static namespace projection used by non-executing migration planning. */
    public static String projectInternalName(String value) {
        return migrateInternalName(value);
    }

    /** Descriptor projection must use the exact same namespace contract as runtime transformation. */
    public static String projectDescriptor(String descriptor) {
        return migrateDescriptor(descriptor);
    }

    private static String migrateInternalName(String value) {
        // DistExecutor was removed from current NeoForge. Keep the Forge 1.19.2
        // physical-side execution contract behind an LMS-owned runtime boundary
        // instead of migrating it into a non-existent NeoForge class.
        if (value.equals("net/minecraftforge/fml/DistExecutor"))
            return "com/fuckingdeveloper/lms/runtime/Forge1192DistExecutorBridge";
        if (value.equals("net/minecraftforge/api/distmarker/Dist"))
            return "net/neoforged/api/distmarker/Dist";
        if (value.equals("net/minecraftforge/network/NetworkRegistry"))
            return "com/fuckingdeveloper/lms/runtime/Forge1192NetworkBridge";
        if (value.equals("net/minecraftforge/network/simple/SimpleChannel")
                || value.equals("net/minecraftforge/network/simple/IndexedMessageCodec$MessageHandler")
                || value.equals("net/minecraftforge/network/NetworkEvent$Context"))
            return "java/lang/Object";
        if (value.equals("net/minecraftforge/network/PacketDistributor"))
            return "com/fuckingdeveloper/lms/runtime/Forge1192NetworkBridge$LegacyPacketDistributor";
        if (value.equals("net/minecraftforge/network/PacketDistributor$PacketTarget"))
            return "com/fuckingdeveloper/lms/runtime/Forge1192NetworkBridge$LegacyPacketTarget";
        if (value.equals("net/minecraftforge/network/PacketDistributor$TargetPoint"))
            return "com/fuckingdeveloper/lms/runtime/Forge1192NetworkBridge$LegacyTargetPoint";
        if (value.equals("net/minecraft/client/color/block/BlockColor"))
            return "com/fuckingdeveloper/lms/runtime/color/LegacyBlockColor";
        if (value.equals("net/minecraft/client/color/item/ItemColor"))
            return "com/fuckingdeveloper/lms/runtime/color/LegacyItemColor";
        String explicitVanillaRelocation = EXPLICIT_VANILLA_CLASS_RELOCATIONS.get(value);
        if (explicitVanillaRelocation != null) return explicitVanillaRelocation;
        // IForgeItem was a Forge extension interface mixed into Item in 1.19.2.
        // It has no target class in 26.3; references must remain on Item and its
        // operations are resolved/adapted separately rather than namespace-guessed.
        // Forge's global gameplay event bus survived conceptually as NeoForge.EVENT_BUS.
        // This is an exact static-singleton migration; keeping MinecraftForge around
        // would force the managed loader to resolve a removed framework class.
        if (value.equals("net/minecraftforge/common/MinecraftForge"))
            return "net/neoforged/neoforge/common/NeoForge";
        // Forge 1.19.2's phase-bearing tick events became base events whose
        // concrete Pre/Post subclasses encode the old phase. Field reads are
        // adapted by Forge1192TickEventBridge after namespace projection.
        if (value.equals("net/minecraftforge/event/TickEvent$Phase"))
            return "com/fuckingdeveloper/lms/runtime/Forge1192TickEventBridge$Phase";
        if (value.equals("net/minecraftforge/client/event/RenderLevelStageEvent$Stage"))
            return "com/fuckingdeveloper/lms/runtime/render/Forge1192RenderStageBridge$Stage";
        if (value.equals("net/minecraftforge/event/TickEvent$PlayerTickEvent"))
            return "net/neoforged/neoforge/event/tick/PlayerTickEvent";
        if (value.equals("net/minecraftforge/event/TickEvent$ServerTickEvent"))
            return "net/neoforged/neoforge/event/tick/ServerTickEvent";
        if (value.equals("net/minecraftforge/event/TickEvent$LevelTickEvent"))
            return "net/neoforged/neoforge/event/tick/LevelTickEvent";
        if (value.equals("net/minecraftforge/event/TickEvent$ClientTickEvent"))
            return "net/neoforged/neoforge/client/event/ClientTickEvent";
        if (value.equals("net/minecraftforge/event/furnace/FurnaceFuelBurnTimeEvent"))
            return "com/fuckingdeveloper/lms/runtime/LegacyFurnaceFuelBurnTimeEvent";
        if (value.equals("net/minecraftforge/event/entity/living/LivingSpawnEvent$CheckSpawn"))
            return "net/neoforged/neoforge/event/entity/living/MobSpawnEvent$PositionCheck";
        // LivingAttackEvent was replaced by the early, cancellable incoming
        // damage event. The legacy contract used by 1.19.2 mods (entity,
        // source, amount and cancellation) is preserved by this target.
        if (value.equals("net/minecraftforge/event/entity/living/LivingAttackEvent"))
            return "net/neoforged/neoforge/event/entity/living/LivingIncomingDamageEvent";
        if (value.equals("net/minecraftforge/event/entity/living/ShieldBlockEvent"))
            return "net/neoforged/neoforge/event/entity/living/LivingShieldBlockEvent";
        if (value.equals("net/minecraftforge/event/level/BlockEvent$BreakEvent"))
            return "com/fuckingdeveloper/lms/runtime/event/LegacyBreakBlockEvent";
        // Common Forge living/player gameplay events retained their semantic
        // event identity under NeoForge. Map only when the exact target class
        // exists; removed/split event families remain explicit migration boundaries.
        if (value.startsWith("net/minecraftforge/event/entity/living/")
                || value.startsWith("net/minecraftforge/event/entity/player/")) {
            String candidate = value.replace("net/minecraftforge/", "net/neoforged/neoforge/");
            if (Forge1192LegacyClassTransformer.class.getClassLoader()
                    .getResource(candidate + ".class") != null) {
                return candidate;
            }
        }
        // IForgeRegistry was removed. Keep only a verifier-visible LMS facade;
        // all executable registry operations are redirected to
        // Forge1192RegistrationContext by semantic adapters below.
        if (value.equals("net/minecraftforge/registries/IForgeRegistry"))
            return "com/fuckingdeveloper/lms/runtime/registry/LegacyForgeRegistry";
        if (value.equals("net/minecraftforge/registries/tags/ITagManager"))
            return "com/fuckingdeveloper/lms/runtime/registry/LegacyTagManager";
        if (value.equals("net/minecraftforge/registries/tags/ITag"))
            return "com/fuckingdeveloper/lms/runtime/registry/LegacyTag";
        if (value.equals("net/minecraftforge/common/IPlantable"))
            return "com/fuckingdeveloper/lms/runtime/block/LegacyIPlantable";
        if (value.equals("net/minecraftforge/common/PlantType"))
            return "com/fuckingdeveloper/lms/runtime/block/LegacyPlantType";
        if (value.equals("net/minecraftforge/energy/IEnergyStorage"))
            return "com/fuckingdeveloper/lms/runtime/energy/LegacyIEnergyStorage";
        if (value.equals("net/minecraftforge/items/IItemHandler"))
            return "com/fuckingdeveloper/lms/runtime/itemhandler/LegacyIItemHandler";
        if (value.equals("net/minecraftforge/items/IItemHandlerModifiable"))
            return "com/fuckingdeveloper/lms/runtime/itemhandler/LegacyIItemHandlerModifiable";
        if (value.equals("net/minecraft/world/item/Tier"))
            return "com/fuckingdeveloper/lms/runtime/item/LegacyTier";
        if (value.equals("net/minecraft/world/item/AxeItem"))
            return "com/fuckingdeveloper/lms/runtime/item/LegacyAxeItem";
        if (value.equals("net/minecraft/world/item/HoeItem"))
            return "com/fuckingdeveloper/lms/runtime/item/LegacyHoeItem";
        if (value.equals("net/minecraft/world/item/PickaxeItem"))
            return "com/fuckingdeveloper/lms/runtime/item/LegacyPickaxeItem";
        if (value.equals("net/minecraft/world/item/ShovelItem"))
            return "com/fuckingdeveloper/lms/runtime/item/LegacyShovelItem";
        if (value.equals("net/minecraft/world/item/SwordItem"))
            return "com/fuckingdeveloper/lms/runtime/item/LegacySwordItem";
        if (value.equals("net/minecraft/world/item/DyeableLeatherItem"))
            return "com/fuckingdeveloper/lms/runtime/item/LegacyDyeableLeatherItem";
        if (value.equals("net/minecraftforge/common/extensions/IForgeBlock"))
            return "com/fuckingdeveloper/lms/runtime/block/LegacyIForgeBlock";
        if (value.equals("net/minecraftforge/common/extensions/IForgeItem"))
            return "com/fuckingdeveloper/lms/runtime/item/LegacyIForgeItem";
        if (value.equals("net/minecraftforge/registries/RegistryObject"))
            return "com/fuckingdeveloper/lms/runtime/registry/LegacyRegistryObject";
        if (value.equals("net/minecraftforge/common/capabilities/ICapabilityProvider"))
            return "com/fuckingdeveloper/lms/runtime/capability/LegacyICapabilityProvider";
        if (value.equals("net/minecraftforge/common/capabilities/RegisterCapabilitiesEvent"))
            return "net/neoforged/neoforge/capabilities/RegisterCapabilitiesEvent";
        if (value.equals("net/minecraftforge/common/capabilities/CapabilityToken"))
            return "com/fuckingdeveloper/lms/runtime/capability/LegacyCapabilityToken";
        if (value.equals("net/minecraftforge/common/capabilities/CapabilityManager"))
            return "com/fuckingdeveloper/lms/runtime/capability/LegacyCapabilityManager";
        if (value.equals("net/minecraftforge/common/capabilities/Capability"))
            return "com/fuckingdeveloper/lms/runtime/capability/LegacyCapability";
        if (value.equals("net/minecraftforge/common/util/LazyOptional"))
            return "com/fuckingdeveloper/lms/runtime/capability/LegacyLazyOptional";
        if (value.equals("net/minecraftforge/fluids/capability/IFluidHandler"))
            return "com/fuckingdeveloper/lms/runtime/fluid/LegacyIFluidHandler";
        if (value.equals("net/minecraftforge/fluids/IFluidTank"))
            return "com/fuckingdeveloper/lms/runtime/fluid/LegacyIFluidTank";
        if (value.equals("net/minecraftforge/fluids/capability/templates/FluidTank"))
            return "com/fuckingdeveloper/lms/runtime/fluid/LegacyFluidTank";
        if (value.equals("net/minecraft/world/item/ArmorItem"))
            return "com/fuckingdeveloper/lms/runtime/item/LegacyArmorItem";
        if (value.equals("net/minecraft/world/level/block/LeavesBlock"))
            return "com/fuckingdeveloper/lms/runtime/block/LegacyLeavesBlock";
        if (value.equals("net/minecraft/world/level/block/FenceGateBlock"))
            return "com/fuckingdeveloper/lms/runtime/block/LegacyFenceGateBlock";
        if (value.equals("net/minecraft/world/level/block/PressurePlateBlock$Sensitivity"))
            return "net/minecraft/world/level/block/state/properties/BlockSetType$PressurePlateSensitivity";
        if (value.equals("net/minecraft/world/level/block/PressurePlateBlock"))
            return "com/fuckingdeveloper/lms/runtime/block/LegacyPressurePlateBlock";
        if (value.equals("net/minecraft/world/item/SignItem"))
            return "net/minecraft/world/item/StandingAndWallBlockItem";
        if (value.equals("net/minecraft/world/level/block/WoodButtonBlock"))
            return "net/minecraft/world/level/block/ButtonBlock";
        if (value.equals("net/minecraft/world/level/block/state/properties/DirectionProperty"))
            return "net/minecraft/world/level/block/state/properties/EnumProperty";
        // BakedModel was split into unrelated block-state and item rendering
        // contracts. Preserve legacy-owned model identity without pretending
        // either modern interface is a drop-in replacement.
        if (value.equals("net/minecraft/client/resources/model/BakedModel"))
            return "com/fuckingdeveloper/lms/runtime/render/LegacyBakedModel";
        if (value.equals("net/minecraft/world/entity/MobSpawnType"))
            return "net/minecraft/world/entity/EntitySpawnReason";
        if (value.startsWith("net/minecraftforge/client/model/data/"))
            return value.replace("net/minecraftforge/client/model/data/", "net/neoforged/neoforge/model/data/");
        if (value.equals("net/minecraft/resources/ResourceLocation"))
            return "net/minecraft/resources/Identifier";
        // A removed vanilla type must not be relocated solely because an
        // unrelated target class has the same simple name. Moved types require
        // an explicit mapping/semantic adapter above; otherwise fail closed.
        if (value.startsWith("net/minecraftforge/eventbus/"))
            return value.replace("net/minecraftforge/eventbus/", "net/neoforged/bus/");
        if (value.startsWith("net/minecraftforge/fml/"))
            return value.replace("net/minecraftforge/fml/", "net/neoforged/fml/");
        if (value.startsWith("net/minecraftforge/forgespi/"))
            return value.replace("net/minecraftforge/forgespi/", "net/neoforged/neoforgespi/");
        // Main Forge API moved below NeoForge's neoforge namespace.
        if (value.startsWith("net/minecraftforge/")) {
            String candidate = value.replace("net/minecraftforge/", "net/neoforged/neoforge/");
            if (Forge1192LegacyClassTransformer.class.getClassLoader()
                    .getResource(candidate + ".class") != null) {
                return candidate;
            }
            // Absent Forge APIs are semantic migration boundaries. Keeping the
            // legacy name here is intentional: a later family adapter must own
            // the transformation instead of manufacturing a nonexistent
            // NeoForge class name.
            return value;
        }
        return value;
    }

    private static String migrateDescriptor(String descriptor) {
        String migrated = descriptor;
        for (var relocation : EXPLICIT_VANILLA_CLASS_RELOCATIONS.entrySet()) {
            migrated = migrated.replace(relocation.getKey(), relocation.getValue());
        }
        return migrated
                .replace("net/minecraftforge/fml/DistExecutor", "com/fuckingdeveloper/lms/runtime/Forge1192DistExecutorBridge")
                .replace("net/minecraftforge/api/distmarker/Dist", "net/neoforged/api/distmarker/Dist")
                .replace("net/minecraft/client/color/block/BlockColor", "com/fuckingdeveloper/lms/runtime/color/LegacyBlockColor")
                .replace("net/minecraft/client/color/item/ItemColor", "com/fuckingdeveloper/lms/runtime/color/LegacyItemColor")
                .replace("net/minecraftforge/common/IPlantable", "com/fuckingdeveloper/lms/runtime/block/LegacyIPlantable")
                .replace("net/minecraftforge/common/PlantType", "com/fuckingdeveloper/lms/runtime/block/LegacyPlantType")
                .replace("net/minecraftforge/energy/IEnergyStorage", "com/fuckingdeveloper/lms/runtime/energy/LegacyIEnergyStorage")
                .replace("net/minecraftforge/items/IItemHandlerModifiable", "com/fuckingdeveloper/lms/runtime/itemhandler/LegacyIItemHandlerModifiable")
                .replace("net/minecraftforge/items/IItemHandler", "com/fuckingdeveloper/lms/runtime/itemhandler/LegacyIItemHandler")
                .replace("net/minecraft/world/item/Tier", "com/fuckingdeveloper/lms/runtime/item/LegacyTier")
                .replace("net/minecraft/world/item/AxeItem", "com/fuckingdeveloper/lms/runtime/item/LegacyAxeItem")
                .replace("net/minecraft/world/item/HoeItem", "com/fuckingdeveloper/lms/runtime/item/LegacyHoeItem")
                .replace("net/minecraft/world/item/PickaxeItem", "com/fuckingdeveloper/lms/runtime/item/LegacyPickaxeItem")
                .replace("net/minecraft/world/item/ShovelItem", "com/fuckingdeveloper/lms/runtime/item/LegacyShovelItem")
                .replace("net/minecraft/world/item/SwordItem", "com/fuckingdeveloper/lms/runtime/item/LegacySwordItem")
                .replace("net/minecraft/world/item/DyeableLeatherItem", "com/fuckingdeveloper/lms/runtime/item/LegacyDyeableLeatherItem")
                .replace("net/minecraftforge/common/extensions/IForgeBlock", "com/fuckingdeveloper/lms/runtime/block/LegacyIForgeBlock")
                .replace("net/minecraftforge/common/extensions/IForgeItem", "com/fuckingdeveloper/lms/runtime/item/LegacyIForgeItem")
                .replace("net/minecraftforge/registries/RegistryObject", "com/fuckingdeveloper/lms/runtime/registry/LegacyRegistryObject")
                .replace("net/minecraftforge/registries/tags/ITagManager", "com/fuckingdeveloper/lms/runtime/registry/LegacyTagManager")
                .replace("net/minecraftforge/registries/tags/ITag", "com/fuckingdeveloper/lms/runtime/registry/LegacyTag")
                .replace("net/minecraftforge/common/capabilities/ICapabilityProvider", "com/fuckingdeveloper/lms/runtime/capability/LegacyICapabilityProvider")
                .replace("net/minecraftforge/common/capabilities/RegisterCapabilitiesEvent", "net/neoforged/neoforge/capabilities/RegisterCapabilitiesEvent")
                .replace("net/minecraftforge/common/capabilities/CapabilityToken", "com/fuckingdeveloper/lms/runtime/capability/LegacyCapabilityToken")
                .replace("net/minecraftforge/common/capabilities/CapabilityManager", "com/fuckingdeveloper/lms/runtime/capability/LegacyCapabilityManager")
                .replace("net/minecraftforge/common/capabilities/Capability", "com/fuckingdeveloper/lms/runtime/capability/LegacyCapability")
                .replace("net/minecraftforge/common/util/LazyOptional", "com/fuckingdeveloper/lms/runtime/capability/LegacyLazyOptional")
                .replace("net/minecraftforge/fluids/capability/IFluidHandler", "com/fuckingdeveloper/lms/runtime/fluid/LegacyIFluidHandler")
                .replace("net/minecraftforge/fluids/IFluidTank", "com/fuckingdeveloper/lms/runtime/fluid/LegacyIFluidTank")
                .replace("net/minecraftforge/fluids/capability/templates/FluidTank", "com/fuckingdeveloper/lms/runtime/fluid/LegacyFluidTank")
                .replace("net/minecraft/world/item/ArmorItem", "com/fuckingdeveloper/lms/runtime/item/LegacyArmorItem")
                .replace("net/minecraft/world/level/block/LeavesBlock", "com/fuckingdeveloper/lms/runtime/block/LegacyLeavesBlock")
                .replace("net/minecraft/world/level/block/FenceGateBlock", "com/fuckingdeveloper/lms/runtime/block/LegacyFenceGateBlock")
                .replace("net/minecraft/world/level/block/PressurePlateBlock$Sensitivity", "net/minecraft/world/level/block/state/properties/BlockSetType$PressurePlateSensitivity")
                .replace("net/minecraft/world/level/block/PressurePlateBlock", "com/fuckingdeveloper/lms/runtime/block/LegacyPressurePlateBlock")
                .replace("net/minecraft/world/level/block/state/properties/DirectionProperty", "net/minecraft/world/level/block/state/properties/EnumProperty")
                .replace("net/minecraft/client/resources/model/BakedModel", "com/fuckingdeveloper/lms/runtime/render/LegacyBakedModel")
                .replace("net/minecraftforge/client/model/data/", "net/neoforged/neoforge/model/data/")
                .replace("net/minecraft/resources/ResourceLocation", "net/minecraft/resources/Identifier")
                .replace("net/minecraftforge/eventbus/", "net/neoforged/bus/")
                .replace("net/minecraftforge/fml/", "net/neoforged/fml/")
                .replace("net/minecraftforge/forgespi/", "net/neoforged/neoforgespi/");
    }
}
