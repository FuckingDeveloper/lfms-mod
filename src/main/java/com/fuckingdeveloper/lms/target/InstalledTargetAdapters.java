package com.fuckingdeveloper.lms.target;

import com.fuckingdeveloper.lms.runtime.blockdata.BlockDataTargetLayout;
import com.fuckingdeveloper.lms.runtime.blockdata.BlockDataContext;
import com.fuckingdeveloper.lms.target.neoforge263.Target263BlockDataStorage;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.fuckingdeveloper.lms.target.neoforge263.Target263BlockDataLayout;

/** Explicit build composition. This does not discover or guess future runtimes. */
public final class InstalledTargetAdapters {
    private InstalledTargetAdapters() {}
    public static net.minecraft.core.Registry<?> forgeReadRegistry(net.minecraft.resources.Identifier identity) { return com.fuckingdeveloper.lms.target.neoforge263.Target263ForgeRegistryReads.resolve(identity); }
    public static net.minecraft.world.item.CreativeModeTab[] creativeTabs() { return com.fuckingdeveloper.lms.target.neoforge263.Target263CreativeEnumeration.tabs(); }
    public static net.minecraft.world.item.CreativeModeTab specialCreativeTab(com.fuckingdeveloper.lms.runtime.item.CreativeTabRole role) { return com.fuckingdeveloper.lms.target.neoforge263.Target263CreativeEnumeration.special(role); }
    public static java.util.List<net.minecraft.world.item.ItemStack> creativeTabContents(net.minecraft.world.item.CreativeModeTab tab) { return com.fuckingdeveloper.lms.target.neoforge263.Target263CreativeEnumeration.contents(tab); }
    public static BlockDataContext<CompoundTag, HolderLookup.Provider>
            blockDataContext(BlockEntity entity) {
        return Target263BlockDataStorage.forEntity(entity);
    }
    public static BlockDataContext<CompoundTag, HolderLookup.Provider>
            blockDataLookup(HolderLookup.Provider lookup) {
        return Target263BlockDataStorage.lookup(lookup);
    }
    public static org.objectweb.asm.Handle lifecycleWorkBridge(String sourceDescriptor) {
        return com.fuckingdeveloper.lms.target.neoforge263.Target263LifecycleWorkLayout.bridge(sourceDescriptor);
    }
    public static org.objectweb.asm.Handle explicitListenerBridge(String sourceBus,String descriptor) {
        return new org.objectweb.asm.Handle(org.objectweb.asm.Opcodes.H_INVOKESTATIC,
                "com/fuckingdeveloper/lms/profile/forge1192/runtime/lifecycle/Forge1192EventBusBridge",
                "addListenerExplicit","(L"+sourceBus+";"+descriptor.substring(1),false);
    }
    public static int sourceIngredientStackingId(net.minecraft.world.item.ItemStack stack) { return com.fuckingdeveloper.lms.target.neoforge263.Target263SourceIngredients.stackingId(stack); }
    public static com.google.gson.JsonObject sourceIngredientItemJson(net.minecraft.world.item.ItemStack stack) { return com.fuckingdeveloper.lms.target.neoforge263.Target263SourceIngredients.itemJson(stack); }
    public static java.util.Collection<net.minecraft.world.item.ItemStack> sourceIngredientTagItems(net.minecraft.tags.TagKey<net.minecraft.world.item.Item> tag) { return com.fuckingdeveloper.lms.target.neoforge263.Target263SourceIngredients.tagItems(tag); }
    public static com.fuckingdeveloper.lms.profile.forge1192.runtime.recipe.LegacyIngredient.Value sourceIngredientValue(com.google.gson.JsonObject json) { return com.fuckingdeveloper.lms.target.neoforge263.Target263SourceIngredients.value(json); }
    public static boolean rawChannelPresent(net.minecraft.network.Connection connection,net.minecraft.resources.Identifier channel) {
        return com.fuckingdeveloper.lms.target.neoforge263.Target263RawNetworkChannels.isRemotePresent(connection,channel);
    }
    public static boolean registerRawChannel(com.fuckingdeveloper.lms.profile.forge1192.runtime.network.Forge1192NetworkBridge.LegacyChannel channel,
            net.neoforged.neoforge.network.registration.PayloadRegistrar registrar) {
        return com.fuckingdeveloper.lms.target.neoforge263.Target263RawNetworkChannels.register(channel,registrar);
    }
    public static net.neoforged.bus.api.IEventBus sourceTagBus() { return com.fuckingdeveloper.lms.target.neoforge263.Target263SourceTagNotifications.sourceBus(); }
    public static com.fuckingdeveloper.lms.profile.forge1192.runtime.event.LegacyTagsUpdatedEvent sourceTagView(net.neoforged.neoforge.event.TagsUpdatedEvent event) { return com.fuckingdeveloper.lms.target.neoforge263.Target263SourceTagNotifications.view(event); }
    public static String recipeNotificationEvent() { return "net/neoforged/neoforge/client/event/RecipesReceivedEvent"; }
    public static String loaderEnvironmentBridge() { return "com/fuckingdeveloper/lms/target/neoforge263/Target263LoaderEnvironment"; }
    public static String sourceTextureAtlasHolder() { return "com/fuckingdeveloper/lms/target/neoforge263/Target263SourceTextureAtlasHolder"; }
    public static String sourceAtlasPreparations() { return "net/minecraft/client/renderer/texture/SpriteLoader$Preparations"; }
    public static String sourceRidingBridge() { return "com/fuckingdeveloper/lms/target/neoforge263/Target263SourceRiding"; }
    public static String sourceEntityFactories() { return "com/fuckingdeveloper/lms/target/neoforge263/Target263SourceEntityFactories"; }
    public static String sourceSpawnPacketInterface() { return "com/fuckingdeveloper/lms/target/neoforge263/Target263SourceSpawnPacket"; }
    public static String sourceSpawnEntity() { return "com/fuckingdeveloper/lms/target/neoforge263/Target263SourceSpawnEntity"; }
    public static String sourceBannerPatternItem() { return "com/fuckingdeveloper/lms/target/neoforge263/Target263BannerPatternItem"; }
    public static String sourceStairs() { return "com/fuckingdeveloper/lms/target/neoforge263/Target263SourceStairs"; }
    public static String sourceEntityRenderer() { return "com/fuckingdeveloper/lms/target/neoforge263/render/Target263SourceEntityRenderer"; }
    public static String throwableItemBase() { return "net/minecraft/world/entity/projectile/throwableitemprojectile/ThrowableItemProjectile"; }
    public static String throwableItemAdapter() { return "com/fuckingdeveloper/lms/target/neoforge263/entity/Target263Forge1192ThrowableItemProjectile"; }
    public static java.util.Map<String,String> gameEventFields() { return com.fuckingdeveloper.lms.target.neoforge263.Target263GameEventLayout.fields(); }
    public static String gameEventsBridge() { return "com/fuckingdeveloper/lms/target/neoforge263/Target263SourceGameEvents"; }
    public static String entityPositionMethod() { return "snapTo"; }
    public static String entityAllocationBridge() { return "com/fuckingdeveloper/lms/target/neoforge263/Target263SourceEntityAllocation"; }
    public static String explosionsBridge() { return "com/fuckingdeveloper/lms/target/neoforge263/Target263SourceExplosions"; }
    public static String entityDataBuilder() { return "net/minecraft/network/syncher/SynchedEntityData$Builder"; }
    public static String soundManagerPlaybackBridge() { return "com/fuckingdeveloper/lms/target/neoforge263/render/Target263SoundManagerPlayback"; }
    public static String chunkLightPacketsBridge() { return "com/fuckingdeveloper/lms/target/neoforge263/Target263SourceChunkLightPackets"; }
    public static String heightQueriesBridge() { return "com/fuckingdeveloper/lms/target/neoforge263/Target263SourceHeightQueries"; }
    public static String entitySyncBridge() { return "com/fuckingdeveloper/lms/target/neoforge263/Target263SourceEntitySync"; }
    public static String damageDeliveryBridge() { return "com/fuckingdeveloper/lms/target/neoforge263/Target263SourceDamageDelivery"; }
    public static String damageFactoriesBridge() { return "com/fuckingdeveloper/lms/target/neoforge263/Target263SourceDamageFactories"; }
    public static String itemCooldownsBridge() { return "com/fuckingdeveloper/lms/target/neoforge263/Target263SourceItemCooldowns"; }
    public static String registryQueriesBridge() { return "com/fuckingdeveloper/lms/target/neoforge263/Target263SourceRegistryQueries"; }
    public static String configSpecOwner() { return com.fuckingdeveloper.lms.target.neoforge263.Target263ConfigLayout.NATIVE; }
    public static String configValuesBridge() { return "com/fuckingdeveloper/lms/target/neoforge263/Target263SourceConfigValues"; }
    public static boolean hasPublicConfigMethod(String owner, String name, String descriptor) {
        return com.fuckingdeveloper.lms.target.neoforge263.Target263ConfigLayout.hasMethod(owner, name, descriptor);
    }
    public static BlockDataTargetLayout blockDataCallbacks() { return Target263BlockDataLayout.layout(); }
}
