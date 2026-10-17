package com.fuckingdeveloper.lms.target.neoforge263;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.world.Forge1192UnadaptedHeightOverrides;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelHeightAccessor;

/** Source exclusive endpoints are views of actual native dimensions, never constants. */
public final class Target263SourceHeightQueries {
    private Target263SourceHeightQueries() {}
    private static LevelHeightAccessor nativeAccessor(LevelHeightAccessor accessor) {
        Objects.requireNonNull(accessor);
        if (accessor instanceof Forge1192UnadaptedHeightOverrides)
            throw new UnsupportedOperationException("Source height overrides require native callback integration: " + accessor.getClass().getName());
        return accessor;
    }
    public static int getHeight(LevelHeightAccessor accessor) { return nativeAccessor(accessor).getHeight(); }
    public static int getMinBuildHeight(LevelHeightAccessor accessor) { return nativeAccessor(accessor).getMinY(); }
    public static int getMaxBuildHeight(LevelHeightAccessor accessor) { return nativeAccessor(accessor).getMaxY() + 1; }
    public static int getSectionsCount(LevelHeightAccessor accessor) { return nativeAccessor(accessor).getSectionsCount(); }
    public static int getMinSection(LevelHeightAccessor accessor) { return nativeAccessor(accessor).getMinSectionY(); }
    public static int getMaxSection(LevelHeightAccessor accessor) { return nativeAccessor(accessor).getMaxSectionY() + 1; }
    public static boolean isOutsideBuildHeight(LevelHeightAccessor accessor, int y) { var nativeView = nativeAccessor(accessor); return y < nativeView.getMinY() || y >= nativeView.getMaxY() + 1; }
    public static boolean isOutsideBuildHeight(LevelHeightAccessor accessor, BlockPos pos) { return isOutsideBuildHeight(accessor, pos.getY()); }
    public static int getSectionIndex(LevelHeightAccessor accessor, int y) { return nativeAccessor(accessor).getSectionIndex(y); }
    public static int getSectionIndexFromSectionY(LevelHeightAccessor accessor, int y) { return nativeAccessor(accessor).getSectionIndexFromSectionY(y); }
    public static int getSectionYFromSectionIndex(LevelHeightAccessor accessor, int index) { return nativeAccessor(accessor).getSectionYFromSectionIndex(index); }
    public static LevelHeightAccessor create(int minY, int height) { return LevelHeightAccessor.create(minY, height); }
}
