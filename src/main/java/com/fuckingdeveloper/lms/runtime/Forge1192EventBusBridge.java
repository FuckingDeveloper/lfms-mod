package com.fuckingdeveloper.lms.runtime;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
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

import com.fuckingdeveloper.lms.runtime.event.LegacyRenderHighlightBlockEvent;
import com.fuckingdeveloper.lms.runtime.event.LegacyTextureStitchEvent;
import com.fuckingdeveloper.lms.runtime.event.LegacyRegisterColorHandlersEvent;
import com.fuckingdeveloper.lms.runtime.event.LegacyRegisterGuiOverlaysEvent;
import com.fuckingdeveloper.lms.runtime.event.LegacyVillagerTradesEvent;
import com.fuckingdeveloper.lms.runtime.event.LegacyWandererTradesEvent;

/**
 * Semantic adapters for Forge 1.19.2 event-bus contracts that changed in NeoForge.
 */
public final class Forge1192EventBusBridge {
    private static final List<PrioritizedListener<LegacyTextureStitchEvent.Pre>> TEXTURE_STITCH_PRE =
            new CopyOnWriteArrayList<>();
    private static final List<PrioritizedListener<LegacyTextureStitchEvent.Post>> TEXTURE_STITCH_POST =
            new CopyOnWriteArrayList<>();
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
    }

    /**
     * Forge 1.19.2 IEventBus#post(Event) returned whether the event was cancelled.
     * Current NeoForge returns the posted event. Posting still occurs on the real
     * target bus; only the legacy return contract is reconstructed.
     */
    public static <T extends Event> void addListener(IEventBus bus, Consumer<T> listener,
                                                     Class<T> eventType) {
        String modId = Forge1192LifecycleBridge.requireActive().modId();
        registerListener(bus, net.neoforged.bus.api.EventPriority.NORMAL, eventType,
                event -> invokeScoped(modId, listener, event));
    }

    public static <T extends Event> void addListener(IEventBus bus, net.neoforged.bus.api.EventPriority priority,
                                                     Consumer<T> listener, Class<T> eventType) {
        String modId = Forge1192LifecycleBridge.requireActive().modId();
        registerListener(bus, priority, eventType,
                event -> invokeScoped(modId, listener, event));
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
            registerListener(bus, annotation.priority(), eventType, listener);
        }
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
            Class<T> eventType, Consumer<T> listener) {
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
                    event -> dispatchLegacyWandererTrades((Consumer) listener));
            return;
        }
        for (Class<? extends Event> concreteType : concreteEventTypes(eventType)) {
            bus.addListener(priority, false, (Class) concreteType, (Consumer) listener);
        }
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

    private static void dispatchLegacyTextureStitch(TextureAtlasStitchedEvent target) {
        var pre = new LegacyTextureStitchEvent.Pre(target.getAtlas());
        ordered(TEXTURE_STITCH_PRE).forEach(listener -> listener.consumer().accept(pre));
        pre.requireMaterialized();

        var post = new LegacyTextureStitchEvent.Post(target.getAtlas());
        ordered(TEXTURE_STITCH_POST).forEach(listener -> listener.consumer().accept(post));
    }

    private static <T extends Event> List<PrioritizedListener<T>> ordered(
            List<PrioritizedListener<T>> listeners) {
        return listeners.stream()
                .sorted(Comparator.comparingInt(listener -> listener.priority().ordinal()))
                .toList();
    }

    private record PrioritizedListener<T extends Event>(
            net.neoforged.bus.api.EventPriority priority, Consumer<T> consumer) {}

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
                    legacy.requireMaterialized();
                });
    }

    private static void dispatchLegacyWandererTrades(
            Consumer<LegacyWandererTradesEvent> listener) {
        var legacy = new LegacyWandererTradesEvent();
        listener.accept(legacy);
        legacy.requireMaterialized();
    }

    private static void registerLegacyFuelComponents(
            ModifyDefaultComponentsEvent event,
            Consumer<LegacyFurnaceFuelBurnTimeEvent> listener) {
        event.modifyMatching((item, components) -> true, (components, lookup, item) -> {
            CookingFuel existing = components.get(DataComponents.COOKING_FUEL);
            int initial = existing != null && existing.burnTime() instanceof ResolvableInt.Constant constant
                    ? constant.value() : 0;
            LegacyFurnaceFuelBurnTimeEvent legacy =
                    new LegacyFurnaceFuelBurnTimeEvent(new ItemStack(item), initial);
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
                    RenderLevelStageEvent.AfterOpaqueFeatures.class,
                    RenderLevelStageEvent.AfterTranslucentFeatures.class,
                    RenderLevelStageEvent.AfterTranslucentBlocks.class,
                    RenderLevelStageEvent.AfterTranslucentParticles.class,
                    RenderLevelStageEvent.AfterWeather.class,
                    RenderLevelStageEvent.OitTranslucent.class,
                    RenderLevelStageEvent.AfterLevel.class);
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
        try (var lifecycle = Forge1192LifecycleBridge.enterRecorded(modId);
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
