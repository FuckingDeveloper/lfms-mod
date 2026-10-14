package com.fuckingdeveloper.lms.runtime.capability;

import com.fuckingdeveloper.lms.runtime.event.LegacyAttachCapabilitiesEvent;
import java.util.*;
import net.minecraft.core.Direction;
import net.minecraft.nbt.*;
import net.minecraft.resources.Identifier;

/** Ordered source provider semantics; this instance is held only by its receiver. */
public final class LegacyBlockCapabilityDispatcher {
    private Map<Identifier, LegacyICapabilityProvider> providers = Map.of();
    private List<Runnable> listeners = List.of();
    private boolean initialized;
    private boolean valid = true;
    public boolean hasProviders() { return !providers.isEmpty(); }
    public void complete(LegacyAttachCapabilitiesEvent<?> event) {
        if (initialized) throw new IllegalStateException("Source attachments gathered twice");
        providers = Collections.unmodifiableMap(new LinkedHashMap<>(event.getCapabilities()));
        listeners = event.getListeners(); initialized = true;
    }
    public <T> LegacyLazyOptional<T> getCapability(LegacyCapability<T> token, Direction side) {
        Objects.requireNonNull(token);
        if (!valid || !initialized) return LegacyLazyOptional.empty();
        for (var provider : providers.values()) {
            var result = Objects.requireNonNull(provider.getCapability(token, side), "Source attached provider returned null");
            if (result.isPresent()) return result;
        }
        return LegacyLazyOptional.empty();
    }
    public void invalidate() {
        // Source callbacks are invoked on each explicit invalidation; providers own optional invalidation.
        valid = false; listeners.forEach(Runnable::run);
    }
    public void revive() { valid = true; }
    public boolean isValid() { return valid; }
    public boolean isInitialized() { return initialized; }
    public CompoundTag serialize() {
        var result = new CompoundTag();
        providers.forEach((key, provider) -> {
            if (provider instanceof LegacyINBTSerializable<?> serializable)
                result.put(key.toString(), Objects.requireNonNull(serializable.serializeNBT(), "Source attached NBT is null"));
        });
        return result;
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void deserialize(CompoundTag data) {
        providers.forEach((key, provider) -> {
            Tag tag = data.get(key.toString());
            if (tag != null && provider instanceof LegacyINBTSerializable serializable) serializable.deserializeNBT(tag);
        });
    }
}
