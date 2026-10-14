package com.fuckingdeveloper.lms.runtime.render;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

/** Source-shaped Minecraft 1.19.2 model key, including its variant. */
public final class LegacyModelResourceLocation {
    private static final char VARIANT_SEPARATOR = '#';

    private final Identifier identifier;
    private final String variant;
    private final BlockState blockState;

    public LegacyModelResourceLocation(String namespace, String path, String variant) {
        this(Identifier.fromNamespaceAndPath(namespace, path), variant);
    }

    public LegacyModelResourceLocation(String combined) {
        this(decompose(combined));
    }

    public LegacyModelResourceLocation(Identifier identifier, String variant) {
        this(identifier, variant, null);
    }

    public LegacyModelResourceLocation(String identifier, String variant) {
        this(Identifier.parse(identifier), variant);
    }

    private LegacyModelResourceLocation(String[] parts) {
        this(Identifier.parse(parts[0]), parts[1]);
    }

    private LegacyModelResourceLocation(
            Identifier identifier, String variant, BlockState blockState) {
        this.identifier = Objects.requireNonNull(identifier, "identifier");
        this.variant = Objects.requireNonNull(variant, "variant");
        this.blockState = blockState;
    }

    public static LegacyModelResourceLocation fromBlockState(BlockState state) {
        BlockState value = Objects.requireNonNull(state, "block state");
        Identifier id = net.minecraft.core.registries.BuiltInRegistries.BLOCK
                .getKey(value.getBlock());
        if (id == null) throw new IllegalStateException("Unregistered block model state: " + value);
        return new LegacyModelResourceLocation(id, value.toString(), value);
    }

    private static String[] decompose(String combined) {
        int separator = combined.indexOf(VARIANT_SEPARATOR);
        if (separator < 0) return new String[]{combined, ""};
        return new String[]{combined.substring(0, separator), combined.substring(separator + 1)};
    }

    public Identifier identifier() {
        return identifier;
    }

    public String getNamespace() {
        return identifier.getNamespace();
    }

    public String getPath() {
        return identifier.getPath();
    }

    public String getVariant() {
        return variant;
    }

    public BlockState blockState() {
        return blockState;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof LegacyModelResourceLocation location
                && identifier.equals(location.identifier)
                && variant.equals(location.variant);
    }

    @Override
    public int hashCode() {
        return 31 * identifier.hashCode() + variant.hashCode();
    }

    @Override
    public String toString() {
        return identifier + String.valueOf(VARIANT_SEPARATOR) + variant;
    }
}
