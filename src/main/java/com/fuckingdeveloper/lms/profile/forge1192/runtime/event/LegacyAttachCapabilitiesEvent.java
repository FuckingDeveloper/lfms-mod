package com.fuckingdeveloper.lms.profile.forge1192.runtime.event;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.capability.LegacyICapabilityProvider;
import java.util.*;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;

/** Source block-attachment data; generic subject identity survives native EventBus migration. */
public final class LegacyAttachCapabilitiesEvent<T> extends Event {
    private final Class<T> type;
    private final T object;
    private final Map<Identifier, LegacyICapabilityProvider> providers = new LinkedHashMap<>();
    private final List<Runnable> listeners = new ArrayList<>();
    public LegacyAttachCapabilitiesEvent(Class<T> type, T object) {
        this.type = Objects.requireNonNull(type); this.object = Objects.requireNonNull(object);
        if (type != net.minecraft.world.level.block.entity.BlockEntity.class)
            throw new IllegalStateException("Only source block attachment lifecycle is adapted: " + type);
    }
    public T getObject() { return object; }
    public java.lang.reflect.Type getGenericType() { return type; }
    public void addCapability(Identifier key, LegacyICapabilityProvider provider) {
        if (providers.putIfAbsent(Objects.requireNonNull(key), Objects.requireNonNull(provider)) != null)
            throw new IllegalStateException("Duplicate source attachment key " + key);
    }
    public Map<Identifier, LegacyICapabilityProvider> getCapabilities() { return Collections.unmodifiableMap(providers); }
    public void addListener(Runnable listener) { listeners.add(Objects.requireNonNull(listener)); }
    public List<Runnable> getListeners() { return Collections.unmodifiableList(listeners); }
}
