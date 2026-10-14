package com.fuckingdeveloper.lms.runtime;

import com.fuckingdeveloper.lms.mixin.*;
import com.fuckingdeveloper.lms.runtime.capability.LegacyBlockCapabilityCarrier;
import java.lang.invoke.*;
import java.util.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.*;

/** Source callbacks retain their order; native boundaries supply data and context. */
public final class Forge1192BlockDataBridge {
    private Forge1192BlockDataBridge() {}
    private enum Operation {
        LOAD("lms$sourceLoad", void.class, CompoundTag.class),
        SAVE("lms$sourceSave", void.class, CompoundTag.class),
        UPDATE("lms$sourceUpdateTag", CompoundTag.class),
        HANDLE("lms$sourceHandleUpdateTag", void.class, CompoundTag.class);
        final String method; final MethodType type;
        Operation(String method, Class<?> result, Class<?>... args) { this.method = method; type = MethodType.methodType(result, args); }
    }
    private static final ClassValue<Map<Operation, MethodHandle>> CALLBACKS = new ClassValue<>() {
        @Override protected Map<Operation, MethodHandle> computeValue(Class<?> type) {
            if (!LegacyBlockDataCallbacks.class.isAssignableFrom(type)) return Map.of();
            try {
                var lookup = MethodHandles.privateLookupIn(type, MethodHandles.lookup());
                var found = new EnumMap<Operation, MethodHandle>(Operation.class);
                for (var operation : Operation.values()) try {
                    found.put(operation, lookup.findVirtual(type, operation.method, operation.type));
                } catch (NoSuchMethodException absent) { /* This hierarchy does not own that source callback. */ }
                return Map.copyOf(found);
            } catch (IllegalAccessException failure) { throw new IllegalStateException("Cannot link source block data callbacks: " + type.getName(), failure); }
        }
    };
    private static final class Frame {
        final BlockEntity entity; final HolderLookup.Provider lookup; final TagValueOutput output; final ProblemReporter reporter;
        final IdentityHashMap<CompoundTag, SaveView> saveViews = new IdentityHashMap<>();
        Frame(BlockEntity entity, HolderLookup.Provider lookup, TagValueOutput output, ProblemReporter reporter) {
            this.entity = entity; this.lookup = lookup; this.output = output; this.reporter = reporter;
        }
    }
    private record SaveView(Tag preservedNeoData, boolean hasPersistentData) {}
    private static final ThreadLocal<Frame> CURRENT = new ThreadLocal<>();
    public static boolean sourceCallbackActive(BlockEntity entity) { var frame = CURRENT.get(); return frame != null && frame.entity == entity; }
    public static boolean dataContextActive() { return CURRENT.get() != null; }
    public static Optional<HolderLookup.Provider> currentLookup() { var frame = CURRENT.get(); return frame == null ? Optional.empty() : Optional.ofNullable(frame.lookup); }

    private static Object invoke(BlockEntity entity, Operation operation, CompoundTag tag, Frame frame) {
        MethodHandle callback = CALLBACKS.get(entity.getClass()).get(operation);
        if (callback == null) throw new IllegalStateException("No source block data callback " + operation + " for " + entity.getClass().getName());
        Frame previous = CURRENT.get(); CURRENT.set(frame);
        try {
            if (operation == Operation.UPDATE) return Objects.requireNonNull(callback.invoke(entity), "Source update tag is null");
            callback.invoke(entity, tag); return null;
        } catch (RuntimeException | Error failure) { throw failure; }
        catch (Throwable failure) { throw new IllegalStateException("Source block data callback failed: " + entity.getClass().getName() + "#" + operation.method, failure); }
        finally { if (previous == null) CURRENT.remove(); else CURRENT.set(previous); }
    }
    private static Target263TagValueInputAccess inputAccess(ValueInput input) {
        if (!(input instanceof Target263TagValueInputAccess access))
            throw new IllegalStateException("Source block data needs the pinned native TagValueInput backend: " + input.getClass().getName());
        return access;
    }
    private static Target263TagValueOutputAccess outputAccess(TagValueOutput output) {
        if (!(output instanceof Target263TagValueOutputAccess access)) throw new IllegalStateException("Native block output context accessor was not applied");
        return access;
    }
    private static HolderLookup.Provider outputLookup(Target263TagValueOutputAccess output) {
        return output.lms$ops() instanceof RegistryOps<?> ops && ops.lookupProvider instanceof RegistryOps.HolderLookupAdapter adapter ? adapter.lookupProvider : null;
    }
    public static void load(BlockEntity entity, ValueInput input) {
        var access = inputAccess(input);
        invoke(entity, Operation.LOAD, access.lms$root().copy(), new Frame(entity, input.lookup(), null, access.lms$reporter()));
    }
    public static void save(BlockEntity entity, ValueOutput output) {
        if (!(output instanceof TagValueOutput tagged)) throw new IllegalStateException("Unsupported source block output backend: " + output.getClass().getName());
        var access = outputAccess(tagged);
        invoke(entity, Operation.SAVE, tagged.buildResult(), new Frame(entity, outputLookup(access), tagged, access.lms$reporter()));
    }
    public static CompoundTag updateTag(BlockEntity entity, HolderLookup.Provider lookup) {
        return (CompoundTag)invoke(entity, Operation.UPDATE, null, new Frame(entity, Objects.requireNonNull(lookup), null, ProblemReporter.DISCARDING));
    }
    public static void handleUpdateTag(BlockEntity entity, ValueInput input) {
        var access = inputAccess(input);
        invoke(entity, Operation.HANDLE, access.lms$root().copy(), new Frame(entity, input.lookup(), null, access.lms$reporter()));
    }
    private static Frame frame(BlockEntity entity) {
        Frame current = CURRENT.get();
        if (current != null && current.entity == entity) return current;
        if (entity.getLevel() != null) return new Frame(entity, entity.getLevel().registryAccess(), null, ProblemReporter.DISCARDING);
        return new Frame(entity, Target263DataComponentBindingBridge.currentRegistries().orElse(null), null, ProblemReporter.DISCARDING);
    }
    private static HolderLookup.Provider requireLookup(Frame frame) {
        if (frame.lookup == null) throw new IllegalStateException("No native lookup context for source block data: " + frame.entity.getClass().getName());
        return frame.lookup;
    }

    /** Direct source calls retain the supplied CompoundTag instead of recopying it. */
    public static void loadLegacy(BlockEntity entity, CompoundTag tag) { invoke(entity, Operation.LOAD, Objects.requireNonNull(tag), frame(entity)); }
    public static void saveLegacy(BlockEntity entity, CompoundTag tag) {
        Frame source = frame(entity); TagValueOutput output;
        if (source.output != null) {
            var access = outputAccess(source.output); output = Target263TagValueOutputAccess.lms$withRoot(access.lms$reporter(), access.lms$ops(), tag);
        } else {
            var original = TagValueOutput.createWithContext(source.reporter, requireLookup(source));
            var access = outputAccess(original); output = Target263TagValueOutputAccess.lms$withRoot(access.lms$reporter(), access.lms$ops(), tag);
        }
        invoke(entity, Operation.SAVE, tag, new Frame(entity, source.lookup, output, source.reporter));
    }
    public static CompoundTag updateTagLegacy(BlockEntity entity) { Frame source = frame(entity); return updateTag(entity, requireLookup(source)); }
    public static void handleUpdateTagLegacy(BlockEntity entity, CompoundTag tag) { invoke(entity, Operation.HANDLE, Objects.requireNonNull(tag), frame(entity)); }

    /** Native source-super load view projects the verified persistent-data key. */
    public static ValueInput sourceInput(BlockEntity entity, CompoundTag tag) {
        Frame source = frame(entity); var projected = new CompoundTag();
        for (var entry : tag.entrySet()) projected.put(entry.getKey(), entry.getValue());
        projected.remove("NeoForgeData"); // An opaque source key is not native persistent data.
        if (tag.contains("ForgeData")) projected.put("NeoForgeData", tag.get("ForgeData"));
        return TagValueInput.create(source.reporter, requireLookup(source), projected);
    }
    public static void finishSourceLoad(BlockEntity entity, CompoundTag tag) {
        if (tag.contains("ForgeData")) ((LegacyBlockPersistentDataCarrier)entity).lms$sourcePersistentData(tag.getCompoundOrEmpty("ForgeData"));
        if (tag.contains("ForgeCaps")) ((LegacyBlockCapabilityCarrier)entity).lms$sourceCapabilities().deserialize(tag.getCompoundOrEmpty("ForgeCaps"));
    }
    public static ValueOutput sourceOutput(BlockEntity entity, CompoundTag tag) {
        Frame source = frame(entity);
        if (CURRENT.get() != source) throw new IllegalStateException("Source-super save lacks its callback output context");
        if (source.output == null) throw new IllegalStateException("Source-super save has no native output context");
        source.saveViews.put(tag, new SaveView(tag.get("NeoForgeData"), ((LegacyBlockPersistentDataCarrier)entity).lms$sourcePersistentData() != null));
        var access = outputAccess(source.output);
        return Target263TagValueOutputAccess.lms$withRoot(access.lms$reporter(), access.lms$ops(), tag);
    }
    public static void finishSourceSave(BlockEntity entity, CompoundTag tag) {
        Frame source = frame(entity);
        SaveView view = Objects.requireNonNull(source.saveViews.remove(tag), "Source-super output view is missing");
        if (view.hasPersistentData()) tag.put("ForgeData", Objects.requireNonNull(tag.get("NeoForgeData"), "Native base omitted persistent data"));
        if (view.preservedNeoData() == null) tag.remove("NeoForgeData"); else tag.put("NeoForgeData", view.preservedNeoData());
        var capabilities = ((LegacyBlockCapabilityCarrier)entity).lms$sourceCapabilities();
        if (capabilities.hasProviders()) tag.put("ForgeCaps", capabilities.serialize());
    }
    /** Both inspected base versions return a fresh empty tag, with no side effects. */
    public static CompoundTag baseUpdateTag(BlockEntity entity) { Objects.requireNonNull(entity); return new CompoundTag(); }
}
