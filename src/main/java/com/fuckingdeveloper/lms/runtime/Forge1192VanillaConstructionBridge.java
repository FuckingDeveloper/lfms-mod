package com.fuckingdeveloper.lms.runtime;

import com.mojang.datafixers.types.Type;
import java.util.Set;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

/** Exact construction adapters for vanilla constructors removed after 1.19.2. */
public final class Forge1192VanillaConstructionBridge {
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

    /** Creative tab membership is event-driven now; retain the old fluent chain. */
    public static Item.Properties withoutLegacyCreativeTab(
            Item.Properties properties, CreativeModeTab ignoredTab) {
        return properties;
    }

    /** Use the legacy block's explicit registry name for its paired block item. */
    public static Item.Properties withBlockItemId(Block block, Item.Properties properties) {
        try {
            Object value = block.getClass().getMethod("getRegistryName").invoke(block);
            if (!(value instanceof net.minecraft.resources.Identifier id)) {
                throw new IllegalStateException("Legacy block registry name is not an Identifier: " + value);
            }
            return properties.setId(ResourceKey.create(Registries.ITEM, id));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Legacy block does not expose getRegistryName(): "
                    + block.getClass().getName(), e);
        }
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
