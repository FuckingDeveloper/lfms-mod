package com.fuckingdeveloper.lms.runtime.render;

import net.minecraft.client.renderer.rendertype.RenderType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Source-shaped Forge 1.19.2 set of eligible chunk render types. */
public final class LegacyChunkRenderTypeSet implements Iterable<RenderType> {
    private static final LegacyChunkRenderTypeSet NONE =
            new LegacyChunkRenderTypeSet(false, Set.of());
    private static final LegacyChunkRenderTypeSet ALL =
            new LegacyChunkRenderTypeSet(true, Set.of());

    private final boolean all;
    private final Set<RenderType> types;

    private LegacyChunkRenderTypeSet(boolean all, Collection<RenderType> types) {
        this.all = all;
        this.types = Set.copyOf(types);
    }

    public static LegacyChunkRenderTypeSet none() {
        return NONE;
    }

    public static LegacyChunkRenderTypeSet all() {
        return ALL;
    }

    public static LegacyChunkRenderTypeSet of(RenderType... types) {
        return of(Arrays.asList(types));
    }

    public static LegacyChunkRenderTypeSet of(Collection<RenderType> types) {
        return types.isEmpty() ? NONE : new LegacyChunkRenderTypeSet(false, types);
    }

    public static LegacyChunkRenderTypeSet union(LegacyChunkRenderTypeSet... sets) {
        return union(Arrays.asList(sets));
    }

    public static LegacyChunkRenderTypeSet union(Collection<LegacyChunkRenderTypeSet> sets) {
        return union((Iterable<LegacyChunkRenderTypeSet>) sets);
    }

    public static LegacyChunkRenderTypeSet union(Iterable<LegacyChunkRenderTypeSet> sets) {
        Set<RenderType> result = new LinkedHashSet<>();
        for (LegacyChunkRenderTypeSet set : sets) {
            if (set.all) return ALL;
            result.addAll(set.types);
        }
        return of(result);
    }

    public static LegacyChunkRenderTypeSet intersection(LegacyChunkRenderTypeSet... sets) {
        return intersection(Arrays.asList(sets));
    }

    public static LegacyChunkRenderTypeSet intersection(Collection<LegacyChunkRenderTypeSet> sets) {
        return intersection((Iterable<LegacyChunkRenderTypeSet>) sets);
    }

    public static LegacyChunkRenderTypeSet intersection(Iterable<LegacyChunkRenderTypeSet> sets) {
        Set<RenderType> result = null;
        boolean sawSet = false;
        for (LegacyChunkRenderTypeSet set : sets) {
            sawSet = true;
            if (set.all) continue;
            if (result == null) result = new LinkedHashSet<>(set.types);
            else result.retainAll(set.types);
        }
        if (!sawSet || result == null) return ALL;
        return of(result);
    }

    public boolean isEmpty() {
        return !all && types.isEmpty();
    }

    public boolean contains(RenderType type) {
        return all || types.contains(type);
    }

    @Override
    public Iterator<RenderType> iterator() {
        return asList().iterator();
    }

    public List<RenderType> asList() {
        if (all) {
            throw new UnsupportedOperationException(
                    "Forge 1.19.2 ChunkRenderTypeSet.all() cannot be enumerated because "
                            + "Minecraft 26.3 no longer exposes a global chunk-layer list");
        }
        return List.copyOf(types);
    }
}
