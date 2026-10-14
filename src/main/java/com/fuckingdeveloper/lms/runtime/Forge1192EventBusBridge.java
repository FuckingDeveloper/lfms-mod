package com.fuckingdeveloper.lms.runtime;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.fml.event.lifecycle.ModLifecycleEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.storage.loot.providers.number.floats.ResolvableFloat;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;

import java.util.function.Consumer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.fuckingdeveloper.lms.runtime.event.LegacyRenderHighlightBlockEvent;
import com.fuckingdeveloper.lms.runtime.event.LegacyModelBakingCompletedEvent;
import com.fuckingdeveloper.lms.runtime.event.LegacyTextureStitchEvent;
import com.fuckingdeveloper.lms.runtime.event.LegacyRegisterColorHandlersEvent;
import com.fuckingdeveloper.lms.runtime.event.LegacyRegisterGuiOverlaysEvent;
import com.fuckingdeveloper.lms.runtime.event.LegacyVillagerTradesEvent;
import com.fuckingdeveloper.lms.runtime.event.LegacyWandererTradesEvent;
import com.fuckingdeveloper.lms.runtime.render.Forge1192ModelBakingBridge;

/**
 * Semantic adapters for Forge 1.19.2 event-bus contracts that changed in NeoForge.
 */
public final class Forge1192EventBusBridge {
    private static final AtomicBoolean LEGACY_CLIENT_RELOAD_LISTENER_PUBLISHED =
            new AtomicBoolean();
    private static final AtomicBoolean DEFERRED_CLIENT_RELOAD_REPLAY_REGISTERED =
            new AtomicBoolean();
    private static final AtomicBoolean DEFERRED_LIFECYCLE_CHANGED_CLIENT_STATE =
            new AtomicBoolean();
    private static final List<PrioritizedListener<LegacyTextureStitchEvent.Pre>> TEXTURE_STITCH_PRE =
            new CopyOnWriteArrayList<>();
    private static final List<PrioritizedListener<LegacyTextureStitchEvent.Post>> TEXTURE_STITCH_POST =
            new CopyOnWriteArrayList<>();
    private static final java.util.Map<Identifier, java.util.Set<Identifier>> PREPARED_TEXTURE_SPRITES =
            new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Set<Identifier> EARLY_TEXTURE_STITCH_POST =
            java.util.concurrent.ConcurrentHashMap.newKeySet();
    private static boolean textureStitchDispatcherRegistered;

    private Forge1192EventBusBridge() {}

    /**
     * Forge 1.19.2 listeners were unnamed. The target sorts listeners by an
     * explicit key, so derive a stable mod-scoped base and disambiguate only
     * against listeners already present in this concrete reload event.
     */
    public static void addReloadListener(
            net.neoforged.neoforge.event.AddServerReloadListenersEvent event,
            PreparableReloadListener listener) {
        String modId = Forge1192LifecycleBridge.requireActive().modId();
        String hash = Integer.toUnsignedString(listener.getClass().getName().hashCode(), 16);
        String base = "legacy_reload/" + hash;
        Identifier key = Identifier.fromNamespaceAndPath(modId, base);
        int suffix = 1;
        while (event.getRegistry().containsKey(key)) {
            key = Identifier.fromNamespaceAndPath(modId, base + "_" + suffix++);
        }
        event.addListener(key, listener);
    }

    /** Stage a source-era direct client reload-listener mutation into the target event. */
    public static void addClientReloadListener(
            ReloadableResourceManager ignored, PreparableReloadListener listener) {
        String modId = Forge1192LifecycleBridge.requireActive().modId();
        Forge1192LifecycleBridge.requireActive().modEventBus().addListener(
                net.neoforged.bus.api.EventPriority.NORMAL, false,
                AddClientReloadListenersEvent.class,
                event -> addNamedClientReloadListener(event, listener, modId));
    }

    public static void registerClientReloadListener(AddClientReloadListenersEvent event, PreparableReloadListener listener) {
        addNamedClientReloadListener(event, java.util.Objects.requireNonNull(listener), Forge1192LifecycleBridge.requireActive().modId());
    }

    private static void addNamedClientReloadListener(
            AddClientReloadListenersEvent event,
            PreparableReloadListener listener,
            String modId) {
        String hash = Integer.toUnsignedString(listener.getClass().getName().hashCode(), 16);
        String base = "legacy_client_reload/" + hash;
        Identifier key = Identifier.fromNamespaceAndPath(modId, base);
        int suffix = 1;
        while (event.getRegistry().containsKey(key)) {
            key = Identifier.fromNamespaceAndPath(modId, base + "_" + suffix++);
        }
        event.addListener(key, listener);
        LEGACY_CLIENT_RELOAD_LISTENER_PUBLISHED.set(true);
    }

    /**
     * Forge 1.19.2 IEventBus#post(Event) returned whether the event was cancelled.
     * Current NeoForge returns the posted event. Posting still occurs on the real
     * target bus; only the legacy return contract is reconstructed.
     */
    public static <T extends Event> void addListener(IEventBus bus, Consumer<T> listener,
                    boolean deferUntilItemComponentsBound,
                                                     Class<T> eventType) {
        String modId = Forge1192LifecycleBridge.requireActive().modId();
        registerListener(bus, net.neoforged.bus.api.EventPriority.NORMAL, eventType,
                event -> invokeScoped(modId, listener, event), deferUntilItemComponentsBound);
    }

    public static <T extends Event> void addListener(IEventBus bus, net.neoforged.bus.api.EventPriority priority,
                                                     Consumer<T> listener,
                                                     boolean deferUntilItemComponentsBound,
                                                     Class<T> eventType) {
        String modId = Forge1192LifecycleBridge.requireActive().modId();
        registerListener(bus, priority, eventType,
                event -> invokeScoped(modId, listener, event), deferUntilItemComponentsBound);
    }

    /**
     * Forge 1.19.2 register(Object) discovers @SubscribeEvent methods. Register
     * each handler explicitly so LMS can wrap every callback in the owning
     * legacy mod/registration scope rather than losing provenance on the
     * shared NeoForge mod bus.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void register(IEventBus bus, Object subscriber) {
        String modId = Forge1192LifecycleBridge.requireActive().modId();
        if (subscriber == null) throw new NullPointerException("subscriber");

        List<Method> handlers = new ArrayList<>();
        for (Class<?> type = subscriber.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            for (Method method : type.getDeclaredMethods()) {
                if (method.getAnnotation(net.neoforged.bus.api.SubscribeEvent.class) == null) continue;
                if (method.getParameterCount() != 1 || !Event.class.isAssignableFrom(method.getParameterTypes()[0]))
                    throw new IllegalArgumentException("Invalid legacy event subscriber method: " + method);
                handlers.add(method);
            }
        }
        handlers.sort(Comparator.comparing(Method::toGenericString));
        if (handlers.isEmpty())
            throw new IllegalArgumentException("Legacy event subscriber has no @SubscribeEvent methods: "
                    + subscriber.getClass().getName());

        for (Method method : handlers) {
            var annotation = method.getAnnotation(net.neoforged.bus.api.SubscribeEvent.class);
            Class<? extends Event> eventType = method.getParameterTypes()[0].asSubclass(Event.class);
            method.setAccessible(true);
            Consumer listener = event -> invokeScoped(modId, ignored -> invokeSubscriber(subscriber, method, ignored), (Event) event);
            if (eventType == com.fuckingdeveloper.lms.runtime.event.LegacyAttachCapabilitiesEvent.class) {
                var parameter = method.getGenericParameterTypes()[0];
                java.lang.reflect.Type subject = null;
                if (parameter instanceof java.lang.reflect.ParameterizedType typed) subject = typed.getActualTypeArguments()[0];
                if (subject != null && subject != net.minecraft.world.level.block.entity.BlockEntity.class)
                    throw new IllegalStateException("Unsupported source attachment subject: " + method.toGenericString());
                registerBlockAttachmentListener(bus, annotation.priority(), listener);
            } else registerListener(bus, annotation.priority(), eventType, listener, false);
        }
    }

    public static void addGenericListener(IEventBus bus, Class<?> subject, Consumer<?> listener) {
        if (subject != net.minecraft.world.level.block.entity.BlockEntity.class)
            throw new IllegalStateException("Only source block attachment subjects are adapted: " + subject);
        String modId = Forge1192LifecycleBridge.requireActive().modId();
        @SuppressWarnings("unchecked") var typed = (Consumer<com.fuckingdeveloper.lms.runtime.event.LegacyAttachCapabilitiesEvent<?>>)listener;
        registerBlockAttachmentListener(bus, net.neoforged.bus.api.EventPriority.NORMAL,
                event -> invokeScoped(modId, typed, (com.fuckingdeveloper.lms.runtime.event.LegacyAttachCapabilitiesEvent<?>)event));
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerBlockAttachmentListener(IEventBus bus, net.neoforged.bus.api.EventPriority priority, Consumer listener) {
        if (bus != net.neoforged.neoforge.common.NeoForge.EVENT_BUS)
            throw new IllegalStateException("Source attachments require the gameplay event bus");
        bus.addListener(priority, false, com.fuckingdeveloper.lms.runtime.event.LegacyAttachCapabilitiesEvent.class, listener);
    }

    /**
     * Forge 1.19.2 represented several lifecycle stages as data on one concrete
     * event class. NeoForge represents those dispatch points as subclasses and
     * rejects listeners registered on the abstract base. Fan out a legacy base
     * listener to every concrete target stage; member-level stage adaptation is
     * deliberately separate from this registration-shape bridge.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <T extends Event> void registerListener(
            IEventBus bus, net.neoforged.bus.api.EventPriority priority,
            Class<T> eventType, Consumer<T> listener,
            boolean deferUntilItemComponentsBound) {
        if (eventType == com.fuckingdeveloper.lms.runtime.event.LegacyAttachCapabilitiesEvent.class)
            throw new IllegalStateException("Source attachment lambda requires an explicit generic subject");
        if (deferUntilItemComponentsBound) {
            if (!ModLifecycleEvent.class.isAssignableFrom(eventType)) {
                throw new IllegalStateException("Item-component deferral is only valid for mod lifecycle events: "
                        + eventType.getName());
            }
            registerComponentBoundLifecycle(bus, priority, eventType, listener);
            return;
        }
        if (eventType == LegacyFurnaceFuelBurnTimeEvent.class) {
            IEventBus modBus = Forge1192LifecycleBridge.requireActive().modEventBus();
            modBus.addListener(priority, false, ModifyDefaultComponentsEvent.class,
                    event -> registerLegacyFuelComponents(event, (Consumer) listener));
            return;
        }
        if (eventType == LegacyRenderHighlightBlockEvent.class) {
            bus.addListener(priority, false, ExtractBlockOutlineRenderStateEvent.class,
                    event -> registerLegacyBlockHighlightRenderer(event, (Consumer) listener));
            return;
        }
        if (eventType == LegacyModelBakingCompletedEvent.class) {
            registerLegacyModelBakingListener(bus, priority, (Consumer) listener);
            return;
        }
        if (eventType == LegacyTextureStitchEvent.Pre.class) {
            registerLegacyTextureStitchListener(priority, (Consumer) listener, true);
            return;
        }
        if (eventType == LegacyTextureStitchEvent.Post.class) {
            registerLegacyTextureStitchListener(priority, (Consumer) listener, false);
            return;
        }
        if (eventType == LegacyRegisterColorHandlersEvent.class) {
            registerLegacyColorListeners(priority, (Consumer) listener, true, true);
            return;
        }
        if (eventType == LegacyRegisterColorHandlersEvent.Block.class) {
            registerLegacyColorListeners(priority, (Consumer) listener, true, false);
            return;
        }
        if (eventType == LegacyRegisterColorHandlersEvent.Item.class) {
            registerLegacyColorListeners(priority, (Consumer) listener, false, true);
            return;
        }
        if (eventType == LegacyRegisterGuiOverlaysEvent.class) {
            Forge1192LifecycleBridge.requireActive().modEventBus().addListener(
                    priority, false, RegisterGuiLayersEvent.class,
                    event -> ((Consumer) listener).accept(new LegacyRegisterGuiOverlaysEvent(event)));
            return;
        }
        if (eventType == LegacyVillagerTradesEvent.class) {
            bus.addListener(priority, false, ServerAboutToStartEvent.class,
                    event -> dispatchLegacyVillagerTrades(event, (Consumer) listener));
            return;
        }
        if (eventType == LegacyWandererTradesEvent.class) {
            bus.addListener(priority, false, ServerAboutToStartEvent.class,
                    event -> dispatchLegacyWandererTrades(event, (Consumer) listener));
            return;
        }
        for (Class<? extends Event> concreteType : concreteEventTypes(eventType)) {
            bus.addListener(priority, false, (Class) concreteType, (Consumer) listener);
        }
    }

    /**
     * Forge 1.19.2 lifecycle callbacks ran after registered items had usable
     * default state and could still contribute setup that affected those
     * defaults. Minecraft 26.3 binds that state during data loading. Capture
     * the source event and replay an event-parameter-independent handler after
     * dynamic tags load but before the pending component set is applied.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <T extends Event> void registerComponentBoundLifecycle(
            IEventBus bus, net.neoforged.bus.api.EventPriority priority,
            Class<T> eventType, Consumer<T> listener) {
        registerDeferredClientReloadReplay();
        AtomicReference<T> pending = new AtomicReference<>();
        AtomicBoolean invoked = new AtomicBoolean();
        bus.addListener(priority, false, eventType, event -> {
            if (!pending.compareAndSet(null, event)) {
                throw new IllegalStateException("Legacy lifecycle event was captured more than once: "
                        + eventType.getName());
            }
        });
        NeoForge.EVENT_BUS.addListener(priority, false, TagsUpdatedEvent.ServerDataLoad.class, event -> {
            if (!event.shouldUpdateStaticData() || invoked.get()) return;
            T lifecycleEvent = pending.getAndSet(null);
            if (lifecycleEvent == null) {
                throw new IllegalStateException(
                        "Component binding began before the deferred legacy lifecycle event was captured: "
                                + eventType.getName());
            }
            if (!invoked.compareAndSet(false, true)) return;
            Target263DataComponentBindingBridge.runBeforeBinding(
                    event.getServerResources(), event.getRegistries(),
                    () -> {
                        listener.accept(lifecycleEvent);
                        DEFERRED_LIFECYCLE_CHANGED_CLIENT_STATE.set(true);
                    });
        });
    }

    /**
     * Replay target client resources when a whole deferred source lifecycle
     * callback populated state after the initial client reload had completed.
     */
    private static void registerDeferredClientReloadReplay() {
        if (!DEFERRED_CLIENT_RELOAD_REPLAY_REGISTERED.compareAndSet(false, true)) return;
        NeoForge.EVENT_BUS.addListener(
                net.neoforged.bus.api.EventPriority.LOWEST, false,
                TagsUpdatedEvent.ServerDataLoad.class, event -> {
                    if (!event.shouldUpdateStaticData()
                            || !LEGACY_CLIENT_RELOAD_LISTENER_PUBLISHED.get()) return;
                    net.minecraft.client.Minecraft minecraft =
                            net.minecraft.client.Minecraft.getInstance();
                    if (minecraft == null) {
                        throw new IllegalStateException(
                                "Deferred legacy client reload requires an active target client");
                    }
                    // Queue after this complete event dispatch so source
                    // callbacks at the same priority also finish first.
                    minecraft.execute(() -> {
                        if (DEFERRED_LIFECYCLE_CHANGED_CLIENT_STATE.getAndSet(false)) {
                            minecraft.reloadResourcePacks();
                        }
                    });
                });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerLegacyModelBakingListener(
            IEventBus bus, net.neoforged.bus.api.EventPriority priority,
            Consumer<LegacyModelBakingCompletedEvent> listener) {
        bus.addListener(priority, false, ModelEvent.ModifyBakingResult.class, event -> {
            dispatchPreparedLegacyTextureStitchPost(event);
            Forge1192ModelBakingBridge.invoke(
                    Forge1192ModelBakingBridge.context(event), listener);
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerLegacyColorListeners(
            net.neoforged.bus.api.EventPriority priority, Consumer listener,
            boolean block, boolean item) {
        IEventBus modBus = Forge1192LifecycleBridge.requireActive().modEventBus();
        if (block) {
            modBus.addListener(priority, false, RegisterColorHandlersEvent.BlockTintSources.class,
                    event -> listener.accept(new LegacyRegisterColorHandlersEvent.Block(event.getBlockColors())));
        }
        if (item) {
            modBus.addListener(priority, false, RegisterColorHandlersEvent.ItemTintSources.class,
                    event -> listener.accept(new LegacyRegisterColorHandlersEvent.Item()));
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static synchronized void registerLegacyTextureStitchListener(
            net.neoforged.bus.api.EventPriority priority, Consumer listener, boolean pre) {
        if (pre) {
            TEXTURE_STITCH_PRE.add(new PrioritizedListener<>(priority, listener));
        } else {
            TEXTURE_STITCH_POST.add(new PrioritizedListener<>(priority, listener));
        }
        if (textureStitchDispatcherRegistered) return;

        Forge1192LifecycleBridge.requireActive().modEventBus().addListener(
                net.neoforged.bus.api.EventPriority.HIGHEST, false,
                TextureAtlasStitchedEvent.class, Forge1192EventBusBridge::dispatchLegacyTextureStitch);
        textureStitchDispatcherRegistered = true;
    }

    public static java.util.Set<Identifier> collectLegacyTextureSprites(
            net.minecraft.client.renderer.texture.TextureAtlas atlas) {
        EARLY_TEXTURE_STITCH_POST.remove(atlas.location());
        var pre = new LegacyTextureStitchEvent.Pre(atlas);
        ordered(TEXTURE_STITCH_PRE).forEach(listener -> listener.consumer().accept(pre));
        java.util.Set<Identifier> requested = pre.requestedSprites();
        PREPARED_TEXTURE_SPRITES.put(atlas.location(), requested);
        return requested;
    }

    private static void dispatchLegacyTextureStitch(TextureAtlasStitchedEvent target) {
        java.util.Set<Identifier> prepared = PREPARED_TEXTURE_SPRITES.remove(target.getAtlas().location());
        var pre = prepared == null
                ? new LegacyTextureStitchEvent.Pre(target.getAtlas())
                : new LegacyTextureStitchEvent.Pre(target.getAtlas(), prepared);
        // Atlases without an LMS sprite source remain explicitly unsupported:
        // invoke Pre at the only available boundary and fail if it asks for a
        // sprite that the target did not already materialize.
        if (prepared == null) {
            ordered(TEXTURE_STITCH_PRE).forEach(listener -> listener.consumer().accept(pre));
        }
        pre.requireMaterialized();

        if (!EARLY_TEXTURE_STITCH_POST.remove(target.getAtlas().location())) {
            var post = new LegacyTextureStitchEvent.Post(target.getAtlas());
            ordered(TEXTURE_STITCH_POST).forEach(listener -> listener.consumer().accept(post));
        }
    }

    /**
     * Target model dispatch is derived before atlas upload, while Forge 1.19.2
     * model callbacks observed TextureStitchEvent.Post first. Recreate that
     * ordering from the exact stitched preparations without uploading them.
     */
    private static void dispatchPreparedLegacyTextureStitchPost(
            ModelEvent.ModifyBakingResult event) {
        Identifier location = TextureAtlas.LOCATION_BLOCKS;
        if (!EARLY_TEXTURE_STITCH_POST.add(location)) return;
        try {
            var atlas = new PreparedLegacyTextureAtlas(
                    location, event.getBlockAtlasPreparations());
            var post = new LegacyTextureStitchEvent.Post(atlas);
            ordered(TEXTURE_STITCH_POST).forEach(listener -> listener.consumer().accept(post));
        } catch (RuntimeException | Error failure) {
            EARLY_TEXTURE_STITCH_POST.remove(location);
            throw failure;
        }
    }

    private static <T extends Event> List<PrioritizedListener<T>> ordered(
            List<PrioritizedListener<T>> listeners) {
        return listeners.stream()
                .sorted(Comparator.comparingInt(listener -> listener.priority().ordinal()))
                .toList();
    }

    private record PrioritizedListener<T extends Event>(
            net.neoforged.bus.api.EventPriority priority, Consumer<T> consumer) {}

    private static final class PreparedLegacyTextureAtlas extends TextureAtlas {
        private final java.util.Map<Identifier, TextureAtlasSprite> sprites;
        private final TextureAtlasSprite missing;

        private PreparedLegacyTextureAtlas(
                Identifier location, SpriteLoader.Preparations preparations) {
            super(location);
            this.sprites = java.util.Map.copyOf(preparations.regions());
            this.missing = java.util.Objects.requireNonNull(
                    preparations.getSprite(MissingTextureAtlasSprite.getLocation()),
                    "Prepared target atlas has no missing sprite");
        }

        @Override
        public TextureAtlasSprite getSprite(Identifier location) {
            return sprites.getOrDefault(location, missing);
        }

        @Override
        public TextureAtlasSprite missingSprite() {
            return missing;
        }

        @Override
        public java.util.Map<Identifier, TextureAtlasSprite> getTextures() {
            return sprites;
        }

        @Override
        public void upload(SpriteLoader.Preparations preparations) {
            throw new UnsupportedOperationException("Prepared legacy atlas is read-only");
        }

        @Override
        public void clearTextureData() {
            throw new UnsupportedOperationException("Prepared legacy atlas is read-only");
        }

        @Override
        public void close() {
            throw new UnsupportedOperationException("Prepared legacy atlas is read-only");
        }
    }

    private static void registerLegacyBlockHighlightRenderer(
            ExtractBlockOutlineRenderStateEvent event,
            Consumer<LegacyRenderHighlightBlockEvent> listener) {
        var target = event.getHitResult();
        var camera = event.getCamera();
        event.addCustomRenderer((renderState, submitter, poseStack, levelRenderState) -> {
            var legacy = new LegacyRenderHighlightBlockEvent(target, camera, poseStack, submitter);
            listener.accept(legacy);
            legacy.flushGeometry();
            return legacy.isCanceled();
        });
    }

    private static void dispatchLegacyVillagerTrades(
            ServerAboutToStartEvent event,
            Consumer<LegacyVillagerTradesEvent> listener) {
        event.getServer().registryAccess().lookupOrThrow(Registries.VILLAGER_PROFESSION)
                .listElements()
                .forEach(profession -> {
                    var legacy = new LegacyVillagerTradesEvent(profession.value());
                    listener.accept(legacy);
                    legacy.preserveForTargetConsumption(event.getServer());
                });
    }

    private static void dispatchLegacyWandererTrades(
            ServerAboutToStartEvent event,
            Consumer<LegacyWandererTradesEvent> listener) {
        var legacy = new LegacyWandererTradesEvent();
        listener.accept(legacy);
        legacy.preserveForTargetConsumption(event.getServer());
    }

    private static void registerLegacyFuelComponents(
            ModifyDefaultComponentsEvent event,
            Consumer<LegacyFurnaceFuelBurnTimeEvent> listener) {
        event.modifyMatching((item, components) -> true, (components, lookup, item) -> {
            CookingFuel existing = components.get(DataComponents.COOKING_FUEL);
            int initial = existing != null && existing.burnTime() instanceof ResolvableInt.Constant constant
                    ? constant.value() : 0;
            LegacyFurnaceFuelBurnTimeEvent legacy =
                    new LegacyFurnaceFuelBurnTimeEvent(
                            Forge1192EarlyItemStackBridge.create(item, 1, components.build()), initial);
            listener.accept(legacy);
            if (!legacy.overridden()) return;
            ResolvableFloat speed = existing == null
                    ? new ResolvableFloat.Constant(1.0F) : existing.speedMultiplier();
            components.set(DataComponents.COOKING_FUEL,
                    new CookingFuel(new ResolvableInt.Constant(legacy.getBurnTime()), speed));
        });
    }

    private static List<Class<? extends Event>> concreteEventTypes(Class<? extends Event> eventType) {
        if (eventType == ServerTickEvent.class)
            return List.of(ServerTickEvent.Pre.class, ServerTickEvent.Post.class);
        if (eventType == LevelTickEvent.class)
            return List.of(LevelTickEvent.Pre.class, LevelTickEvent.Post.class);
        if (eventType == PlayerTickEvent.class)
            return List.of(PlayerTickEvent.Pre.class, PlayerTickEvent.Post.class);
        if (eventType == ClientTickEvent.class)
            return List.of(ClientTickEvent.Pre.class, ClientTickEvent.Post.class);
        if (eventType == RenderLevelStageEvent.class)
            return List.of(
                    RenderLevelStageEvent.AfterSky.class,
                    RenderLevelStageEvent.AfterOpaqueBlocks.class,
                    RenderLevelStageEvent.AfterTranslucentBlocks.class,
                    RenderLevelStageEvent.AfterTranslucentParticles.class,
                    RenderLevelStageEvent.AfterWeather.class);
        if (eventType == InputEvent.class)
            return List.of(
                    InputEvent.Key.class,
                    InputEvent.MouseButton.Pre.class,
                    InputEvent.MouseButton.Post.class,
                    InputEvent.MouseScrollingEvent.class,
                    InputEvent.InteractionKeyMappingTriggered.class);
        return List.of(eventType);
    }

    private static void invokeSubscriber(Object subscriber, Method method, Event event) {
        try {
            method.invoke(subscriber, event);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtime) throw runtime;
            if (cause instanceof Error error) throw error;
            throw new IllegalStateException("Legacy event subscriber failed: " + method, cause);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot invoke legacy event subscriber: " + method, e);
        }
    }

    private static <T extends Event> void invokeScoped(String modId, Consumer<T> listener, T event) {
        try (var lifecycle = Forge1192LifecycleBridge.enterCallback(modId);
             var mod = Forge1192RegistrationContext.enterMod(modId)) {
            if (event instanceof RegisterEvent registration) {
                try (var registry = Forge1192RegistrationContext.enterRegistration(registration)) {
                    listener.accept(event);
                } catch (RuntimeException | Error e) {
                    throw e;
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to close legacy registration scope", e);
                }
            } else {
                listener.accept(event);
            }
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to close legacy mod scope", e);
        }
    }

    public static boolean post(IEventBus bus, Event event) {
        Event posted = bus.post(event);
        return posted instanceof ICancellableEvent cancellable && cancellable.isCanceled();
    }
}
