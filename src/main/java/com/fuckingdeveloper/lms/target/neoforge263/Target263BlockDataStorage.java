package com.fuckingdeveloper.lms.target.neoforge263;

import com.fuckingdeveloper.lms.mixin.Target263TagValueInputAccess;
import com.fuckingdeveloper.lms.mixin.Target263TagValueOutputAccess;
import com.fuckingdeveloper.lms.runtime.blockdata.BlockDataContext;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.*;

/** Pinned storage backend, independent of source Forge keys and callback discovery. */
public final class Target263BlockDataStorage {
    private Target263BlockDataStorage() {}

    public record Read(CompoundTag root, Context context) {}

    /** Immutable handle; referenced output roots remain deliberately mutable and identity-preserving. */
    public static final class Context implements BlockDataContext<CompoundTag, HolderLookup.Provider> {
        private final HolderLookup.Provider lookup;
        private final TagValueOutput output;
        private final ProblemReporter reporter;

        private Context(HolderLookup.Provider lookup, TagValueOutput output, ProblemReporter reporter) {
            this.lookup = lookup;
            this.output = output;
            this.reporter = Objects.requireNonNull(reporter);
        }
        public Optional<HolderLookup.Provider> lookup() { return Optional.ofNullable(lookup); }
        public boolean hasOutput() { return output != null; }
        public Context withOutput(CompoundTag root) {
            var original = output != null ? output : TagValueOutput.createWithContext(reporter, requireLookup());
            var access = outputAccess(original);
            return new Context(lookup, Target263TagValueOutputAccess.lms$withRoot(
                    access.lms$reporter(), access.lms$ops(), Objects.requireNonNull(root)), reporter);
        }
        public ValueInput input(CompoundTag root) {
            return TagValueInput.create(reporter, requireLookup(), Objects.requireNonNull(root));
        }
        public TagValueOutput output() {
            if (output == null) throw new IllegalStateException("No native block output context");
            return output;
        }
        private HolderLookup.Provider requireLookup() {
            if (lookup == null) throw new IllegalStateException("No native lookup context for block data");
            return lookup;
        }
    }

    public static Read read(ValueInput input) {
        if (!(input instanceof Target263TagValueInputAccess access))
            throw new IllegalStateException("Block data needs the pinned native TagValueInput backend: " + input.getClass().getName());
        return new Read(access.lms$root(), new Context(input.lookup(), null, access.lms$reporter()));
    }
    public static Context write(ValueOutput output) {
        if (!(output instanceof TagValueOutput tagged))
            throw new IllegalStateException("Unsupported block output backend: " + output.getClass().getName());
        var access = outputAccess(tagged);
        HolderLookup.Provider lookup = access.lms$ops() instanceof RegistryOps<?> ops
                && ops.lookupProvider instanceof RegistryOps.HolderLookupAdapter adapter ? adapter.lookupProvider : null;
        return new Context(lookup, tagged, access.lms$reporter());
    }
    public static Context lookup(HolderLookup.Provider lookup) {
        return new Context(Objects.requireNonNull(lookup), null, ProblemReporter.DISCARDING);
    }
    public static Context forEntity(BlockEntity entity) {
        var level = entity.getLevel();
        return new Context(level != null ? level.registryAccess() : Target263DataComponentBindingBridge.currentRegistries().orElse(null),
                null, ProblemReporter.DISCARDING);
    }
    public static Context require(BlockDataContext<CompoundTag, HolderLookup.Provider> context) {
        if (!(context instanceof Context nativeContext))
            throw new IllegalStateException("Block data context does not belong to the installed NeoForge26.3 storage adapter");
        return nativeContext;
    }
    private static Target263TagValueOutputAccess outputAccess(TagValueOutput output) {
        if (!(output instanceof Target263TagValueOutputAccess access))
            throw new IllegalStateException("Native block output context accessor was not applied");
        return access;
    }
}
