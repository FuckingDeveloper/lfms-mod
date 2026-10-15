package com.fuckingdeveloper.lms.runtime;

import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbility;
import static com.fuckingdeveloper.lms.runtime.Forge1192ToolActions.*;

/** State query only; the source caller retains application, costs and effects. */
public final class Forge1192ToolModificationBridge {
    private record Query(BlockState state, UseOnContext context, ItemAbility action, boolean simulate) {}
    private static final ThreadLocal<Query> CURRENT = new ThreadLocal<>();
    private Forge1192ToolModificationBridge() {}

    public static ItemStack requireStack(ItemInstance instance) {
        if (instance instanceof ItemStack stack) return stack;
        throw new UnsupportedOperationException("Source tool-action callback requires a live ItemStack, not " + instance.getClass().getName());
    }

    public static BlockState query(BlockState state, UseOnContext context, ItemAbility action, boolean simulate) {
        Query previous = CURRENT.get();
        CURRENT.set(new Query(state, context, action, simulate));
        try { return state.getToolModifiedState(context, action, simulate); }
        finally { if (previous == null) CURRENT.remove(); else CURRENT.set(previous); }
    }

    public static boolean active(BlockState state, UseOnContext context, ItemAbility action, boolean simulate) {
        Query query = CURRENT.get();
        return query != null && query.state() == state && query.context() == context
                && query.action() == action && query.simulate() == simulate;
    }

    /** Verified IForgeBlock default, reached only after native event/custom dispatch. */
    public static BlockState sourceDefault(BlockState state, UseOnContext context, ItemAbility action, boolean simulate) {
        if (!context.getItemInHand().canPerformAction(action)) return null;
        Block block = state.getBlock();
        if (action == HOE_TILL) {
            if (block == Blocks.ROOTED_DIRT) {
                if (!simulate && !context.getLevel().isClientSide())
                    Block.popResourceFromFace(context.getLevel(), context.getClickedPos(), context.getClickedFace(),
                            Forge1192EarlyItemStackBridge.create(Items.HANGING_ROOTS));
                return Blocks.DIRT.defaultBlockState();
            }
            if ((block == Blocks.GRASS_BLOCK || block == Blocks.DIRT_PATH || block == Blocks.DIRT || block == Blocks.COARSE_DIRT)
                    && context.getLevel().getBlockState(context.getClickedPos().above()).isAir())
                return (block == Blocks.COARSE_DIRT ? Blocks.DIRT : Blocks.FARMLAND).defaultBlockState();
            return null;
        }
        if (action == SHOVEL_FLATTEN) {
            return block == Blocks.GRASS_BLOCK || block == Blocks.DIRT || block == Blocks.PODZOL
                    || block == Blocks.COARSE_DIRT || block == Blocks.MYCELIUM || block == Blocks.ROOTED_DIRT
                    ? Blocks.DIRT_PATH.defaultBlockState() : null;
        }
        if (action == AXE_SCRAPE) return WeatheringCopper.getPrevious(state).orElse(null);
        if (action == AXE_WAX_OFF) {
            Block unwaxed = HoneycombItem.WAX_OFF_BY_BLOCK.get().get(block);
            return unwaxed == null ? null : unwaxed.withPropertiesOf(state);
        }
        if (action == AXE_STRIP)
            throw new UnsupportedOperationException("Source vanilla axe-stripping state query requires adaptation of the removed stripping table");
        return null; // Other source action identities have no IForgeBlock default transformation.
    }
}
