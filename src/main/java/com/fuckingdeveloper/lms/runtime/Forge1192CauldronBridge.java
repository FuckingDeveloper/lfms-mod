package com.fuckingdeveloper.lms.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.cauldron.CauldronInteractions;
import net.minecraft.world.item.Item;

/** Adapts the mutable 1.19.2 cauldron interaction maps to target dispatchers. */
public final class Forge1192CauldronBridge {
    private static final Map<Item, CauldronInteraction> EMPTY =
            new DispatcherInteractionMap(CauldronInteractions.EMPTY);
    private static final Map<Item, CauldronInteraction> WATER =
            new DispatcherInteractionMap(CauldronInteractions.WATER);
    private static final Map<Item, CauldronInteraction> LAVA =
            new DispatcherInteractionMap(CauldronInteractions.LAVA);
    private static final Map<Item, CauldronInteraction> POWDER_SNOW =
            new DispatcherInteractionMap(CauldronInteractions.POWDER_SNOW);
    private static final CauldronInteraction FILL_WATER = CauldronInteractions::fillWaterInteraction;
    private static final CauldronInteraction FILL_LAVA = CauldronInteractions::fillLavaInteraction;
    private static final CauldronInteraction FILL_POWDER_SNOW = CauldronInteractions::fillPowderSnowInteraction;
    private static final CauldronInteraction SHULKER_BOX = CauldronInteractions::shulkerBoxInteraction;
    private static final CauldronInteraction BANNER = CauldronInteractions::bannerInteraction;
    private static final CauldronInteraction DYED_ITEM = CauldronInteractions::dyedItemIteration;

    private Forge1192CauldronBridge() {}

    public static Map<Item, CauldronInteraction> emptyInteractions() {
        return EMPTY;
    }

    public static Map<Item, CauldronInteraction> waterInteractions() {
        return WATER;
    }

    public static Map<Item, CauldronInteraction> lavaInteractions() {
        return LAVA;
    }

    public static Map<Item, CauldronInteraction> powderSnowInteractions() {
        return POWDER_SNOW;
    }

    public static CauldronInteraction fillWater() {
        return FILL_WATER;
    }

    public static CauldronInteraction fillLava() {
        return FILL_LAVA;
    }

    public static CauldronInteraction fillPowderSnow() {
        return FILL_POWDER_SNOW;
    }

    public static CauldronInteraction shulkerBox() {
        return SHULKER_BOX;
    }

    public static CauldronInteraction banner() {
        return BANNER;
    }

    public static CauldronInteraction dyedItem() {
        return DYED_ITEM;
    }

    private static final class DispatcherInteractionMap extends AbstractMap<Item, CauldronInteraction> {
        private final IdentityHashMap<Item, CauldronInteraction> entries = new IdentityHashMap<>();
        private final CauldronInteraction.Dispatcher dispatcher;
        private final Method put;

        private DispatcherInteractionMap(CauldronInteraction.Dispatcher dispatcher) {
            this.dispatcher = Objects.requireNonNull(dispatcher, "dispatcher");
            try {
                put = CauldronInteraction.Dispatcher.class.getDeclaredMethod(
                        "put", Item.class, CauldronInteraction.class);
                put.setAccessible(true);
            } catch (ReflectiveOperationException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        @Override
        public CauldronInteraction put(Item item, CauldronInteraction interaction) {
            Objects.requireNonNull(item, "item");
            Objects.requireNonNull(interaction, "interaction");
            try {
                put.invoke(dispatcher, item, interaction);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Cannot access target cauldron dispatcher", e);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException runtime) throw runtime;
                if (cause instanceof Error error) throw error;
                throw new IllegalStateException("Target water cauldron dispatcher rejected interaction", cause);
            }
            return entries.put(item, interaction);
        }

        @Override
        public Set<Entry<Item, CauldronInteraction>> entrySet() {
            return Collections.unmodifiableMap(entries).entrySet();
        }

        @Override
        public CauldronInteraction remove(Object key) {
            throw new UnsupportedOperationException(
                    "Minecraft 26.3 cauldron dispatchers do not expose removal");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException(
                    "Minecraft 26.3 cauldron dispatchers do not expose clearing");
        }
    }
}
