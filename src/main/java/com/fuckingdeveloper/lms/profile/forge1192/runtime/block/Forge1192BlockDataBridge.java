package com.fuckingdeveloper.lms.profile.forge1192.runtime.block;

import com.fuckingdeveloper.lms.runtime.blockdata.BlockDataContext;
import com.fuckingdeveloper.lms.target.InstalledTargetAdapters;

import com.fuckingdeveloper.lms.mixin.*;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.capability.LegacyBlockCapabilityCarrier;
import java.lang.invoke.*;
import java.util.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.world.level.block.entity.BlockEntity;

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
        final BlockEntity entity; final BlockDataContext<CompoundTag, HolderLookup.Provider> context;
        final IdentityHashMap<CompoundTag, SaveView> saveViews = new IdentityHashMap<>();
        Frame(BlockEntity entity, BlockDataContext<CompoundTag, HolderLookup.Provider> context) {
            this.entity = entity; this.context = Objects.requireNonNull(context);
        }
    }
    private record SaveView(Tag preservedNeoData, boolean hasPersistentData) {}
    private static final ThreadLocal<Frame> CURRENT = new ThreadLocal<>();
    public static boolean sourceCallbackActive(BlockEntity entity) { var frame = CURRENT.get(); return frame != null && frame.entity == entity; }
    public static boolean dataContextActive() { return CURRENT.get() != null; }
    public static Optional<HolderLookup.Provider> currentLookup() { var frame = CURRENT.get(); return frame == null ? Optional.empty() : frame.context.lookup(); }

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
    /** Native bindings supply roots and scoped storage handles; source callbacks remain synchronous. */
    public static void load(BlockEntity entity, CompoundTag tag, BlockDataContext<CompoundTag, HolderLookup.Provider> context) {
        invoke(entity, Operation.LOAD, Objects.requireNonNull(tag), new Frame(entity, context));
    }
    public static void save(BlockEntity entity, CompoundTag tag, BlockDataContext<CompoundTag, HolderLookup.Provider> context) {
        invoke(entity, Operation.SAVE, Objects.requireNonNull(tag), new Frame(entity, context));
    }
    public static CompoundTag updateTag(BlockEntity entity, BlockDataContext<CompoundTag, HolderLookup.Provider> context) {
        return (CompoundTag)invoke(entity, Operation.UPDATE, null, new Frame(entity, context));
    }
    public static void handleUpdateTag(BlockEntity entity, CompoundTag tag, BlockDataContext<CompoundTag, HolderLookup.Provider> context) {
        invoke(entity, Operation.HANDLE, Objects.requireNonNull(tag), new Frame(entity, context));
    }
    public static BlockDataContext<CompoundTag, HolderLookup.Provider> context(BlockEntity entity) { return frame(entity).context; }
    private static Frame frame(BlockEntity entity) {
        Frame current = CURRENT.get();
        if (current != null && current.entity == entity) return current;
        return new Frame(entity, InstalledTargetAdapters.blockDataContext(entity));
    }

    /** Direct source calls retain the supplied CompoundTag instead of recopying it. */
    public static void loadLegacy(BlockEntity entity, CompoundTag tag) { invoke(entity, Operation.LOAD, Objects.requireNonNull(tag), frame(entity)); }
    public static void saveLegacy(BlockEntity entity, CompoundTag tag) {
        Frame source = frame(entity);
        invoke(entity, Operation.SAVE, tag, new Frame(entity, source.context.withOutput(tag)));
    }
    public static CompoundTag updateTagLegacy(BlockEntity entity) {
        Frame source = frame(entity);
        var lookup = source.context.lookup().orElseThrow(() -> new IllegalStateException("No native lookup context for source block data: " + entity.getClass().getName()));
        return updateTag(entity, InstalledTargetAdapters.blockDataLookup(lookup));
    }
    public static void handleUpdateTagLegacy(BlockEntity entity, CompoundTag tag) { invoke(entity, Operation.HANDLE, Objects.requireNonNull(tag), frame(entity)); }

    /** Native source-super load view projects the verified persistent-data key. */
    public static CompoundTag sourceInputTag(CompoundTag tag) {
        var projected = new CompoundTag();
        for (var entry : tag.entrySet()) projected.put(entry.getKey(), entry.getValue());
        projected.remove("NeoForgeData"); // An opaque source key is not native persistent data.
        if (tag.contains("ForgeData")) projected.put("NeoForgeData", tag.get("ForgeData"));
        return projected;
    }
    public static void finishSourceLoad(BlockEntity entity, CompoundTag tag) {
        if (tag.contains("ForgeData")) ((LegacyBlockPersistentDataCarrier)entity).lms$sourcePersistentData(tag.getCompoundOrEmpty("ForgeData"));
        if (tag.contains("ForgeCaps")) ((LegacyBlockCapabilityCarrier)entity).lms$sourceCapabilities().deserialize(tag.getCompoundOrEmpty("ForgeCaps"));
    }
    public static BlockDataContext<CompoundTag, HolderLookup.Provider> prepareSourceOutput(BlockEntity entity, CompoundTag tag) {
        Frame source = frame(entity);
        if (CURRENT.get() != source) throw new IllegalStateException("Source-super save lacks its callback output context");
        if (!source.context.hasOutput()) throw new IllegalStateException("Source-super save has no native output context");
        source.saveViews.put(tag, new SaveView(tag.get("NeoForgeData"), ((LegacyBlockPersistentDataCarrier)entity).lms$sourcePersistentData() != null));
        return source.context;
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
