package com.fuckingdeveloper.lms.runtime.event;

import com.fuckingdeveloper.lms.runtime.color.LegacyBlockColor;
import com.fuckingdeveloper.lms.runtime.color.LegacyItemColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.Event;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

/** Source-shaped Forge 1.19.2 procedural color registration events. */
public abstract class LegacyRegisterColorHandlersEvent extends Event {
    public static final class Block extends LegacyRegisterColorHandlersEvent {
        private static final Map<net.minecraft.world.level.block.Block, LegacyBlockColor> COLORS =
                Collections.synchronizedMap(new IdentityHashMap<>());
        private final BlockColors blockColors;

        public Block(BlockColors blockColors) {
            this.blockColors = blockColors;
        }

        public BlockColors getBlockColors() {
            return blockColors;
        }

        public void register(LegacyBlockColor color, net.minecraft.world.level.block.Block... blocks) {
            for (var block : blocks) COLORS.put(block, color);
        }

        /** Evaluates a source block color as packed opaque ARGB for target quads. */
        public static int targetColor(
                BlockState state, BlockAndTintGetter level, BlockPos pos, int tintIndex) {
            LegacyBlockColor color = COLORS.get(state.getBlock());
            int sourceRgb = color == null ? -1 : color.m_92566_(state, level, pos, tintIndex);
            return ARGB.opaque(sourceRgb);
        }
    }

    public static final class Item extends LegacyRegisterColorHandlersEvent {
        private static final Map<net.minecraft.world.item.Item, LegacyItemColor> COLORS =
                Collections.synchronizedMap(new IdentityHashMap<>());

        public void register(LegacyItemColor color, ItemLike... items) {
            for (ItemLike item : items) COLORS.put(item.asItem(), color);
        }

        public static int color(ItemStack stack, int tintIndex) {
            LegacyItemColor color = COLORS.get(stack.getItem());
            if (color == null) throw new IllegalStateException("No legacy item color registered for " + stack.getItem());
            return color.m_92671_(stack, tintIndex);
        }

        public static boolean hasColor(Item item) {
            return COLORS.containsKey(item);
        }

        /**
         * Evaluates the source procedural color contract and converts its RGB
         * result to the target renderer's packed opaque-ARGB representation.
         */
        public static int targetColor(ItemStack stack, int tintIndex) {
            LegacyItemColor color = COLORS.get(stack.getItem());
            int sourceRgb = color == null ? -1 : color.m_92671_(stack, tintIndex);
            return ARGB.opaque(sourceRgb);
        }
    }

}
