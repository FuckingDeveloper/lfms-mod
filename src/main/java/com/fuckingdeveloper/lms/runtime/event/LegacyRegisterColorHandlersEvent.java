package com.fuckingdeveloper.lms.runtime.event;

import com.fuckingdeveloper.lms.runtime.render.LegacyBlockColor;
import com.fuckingdeveloper.lms.runtime.render.LegacyItemColor;
import com.mojang.logging.LogUtils;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.bus.api.Event;
import org.slf4j.Logger;

import java.util.AbstractList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Source-shaped Forge 1.19.2 procedural color registration events. */
public abstract class LegacyRegisterColorHandlersEvent extends Event {
    public static final class Block extends LegacyRegisterColorHandlersEvent {
        private final BlockColors blockColors;

        public Block(BlockColors blockColors) {
            this.blockColors = blockColors;
        }

        public BlockColors getBlockColors() {
            return blockColors;
        }

        public void register(LegacyBlockColor color, net.minecraft.world.level.block.Block... blocks) {
            Set<Property<?>> properties = new LinkedHashSet<>();
            for (var block : blocks) properties.addAll(block.getStateDefinition().getProperties());
            blockColors.register(new LegacyTintLayers(color, Set.copyOf(properties)), blocks);
        }
    }

    public static final class Item extends LegacyRegisterColorHandlersEvent {
        private static final Logger LOGGER = LogUtils.getLogger();
        private static final Map<net.minecraft.world.item.Item, LegacyItemColor> COLORS =
                Collections.synchronizedMap(new IdentityHashMap<>());
        private static boolean warned;

        public void register(LegacyItemColor color, ItemLike... items) {
            for (ItemLike item : items) COLORS.put(item.asItem(), color);
            if (!warned) {
                warned = true;
                LOGGER.warn("LMS preserved Forge 1.19.2 procedural item-color registrations, "
                        + "but Minecraft 26.3 item tint population is data-driven and is not materialized yet");
            }
        }

        public static int color(ItemStack stack, int tintIndex) {
            LegacyItemColor color = COLORS.get(stack.getItem());
            if (color == null) throw new IllegalStateException("No legacy item color registered for " + stack.getItem());
            return color.getColor(stack, tintIndex);
        }

        public static boolean hasColor(Item item) {
            return COLORS.containsKey(item);
        }
    }

    private static final class LegacyTintLayers extends AbstractList<BlockTintSource> {
        private final LegacyBlockColor color;
        private final Set<Property<?>> properties;

        private LegacyTintLayers(LegacyBlockColor color, Set<Property<?>> properties) {
            this.color = color;
            this.properties = properties;
        }

        @Override
        public BlockTintSource get(int index) {
            if (index < 0) throw new IndexOutOfBoundsException(index);
            return source(index);
        }

        @Override
        public int size() {
            return Integer.MAX_VALUE;
        }

        @Override
        public Iterator<BlockTintSource> iterator() {
            return Collections.singleton(source(0)).iterator();
        }

        private BlockTintSource source(int tintIndex) {
            return new BlockTintSource() {
                @Override
                public int color(BlockState state) {
                    return color.getColor(state, null, null, tintIndex);
                }

                @Override
                public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
                    return color.getColor(state, level, pos, tintIndex);
                }

                @Override
                public Set<Property<?>> relevantProperties() {
                    return properties;
                }
            };
        }
    }
}
