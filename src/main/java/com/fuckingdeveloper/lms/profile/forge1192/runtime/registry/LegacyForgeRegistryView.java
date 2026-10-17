package com.fuckingdeveloper.lms.profile.forge1192.runtime.registry;

import java.util.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.*;
import com.fuckingdeveloper.lms.target.InstalledTargetAdapters;

/** Key-backed source reads. No target registry/classloader is retained in this view. */
public final class LegacyForgeRegistryView<T> implements LegacyForgeRegistry, Iterable<T> {
    private final Identifier name;
    LegacyForgeRegistryView(Identifier name) { this.name = Objects.requireNonNull(name); }
    @SuppressWarnings("unchecked")
    public Registry<T> nativeRegistry() { return (Registry<T>) InstalledTargetAdapters.forgeReadRegistry(name); }
    public Identifier getRegistryName() { return name; }
    public ResourceKey<Registry<T>> getRegistryKey() { return ResourceKey.createRegistryKey(name); }
    public Identifier getDefaultKey() { return LegacyRegistryManagerView.defaultKey(name); }
    private T defaultValue(Registry<T> registry) {
        var key = getDefaultKey();
        if (key == null) return null;
        return registry.getOptional(key).orElseThrow(() -> new UnsupportedOperationException(
                "Source registry default has no native entry: " + name + " -> " + key));
    }
    public boolean containsKey(Identifier key) { return key != null && nativeRegistry().containsKey(key); }
    public boolean containsValue(T value) { return getResourceKey(value).isPresent(); }
    public boolean isEmpty() { return nativeRegistry().size() == 0; }
    public T getValue(Identifier key) {
        var registry = nativeRegistry();
        return (key == null ? Optional.<T>empty() : registry.getOptional(key)).orElseGet(() -> defaultValue(registry));
    }
    public T getRaw(Identifier key) {
        var registry = nativeRegistry();
        return key != null && registry.keySet().contains(key) ? registry.getOptional(key).orElse(null) : null;
    }
    @SuppressWarnings("unchecked")
    public LegacyTagManager<T> tags() { return (LegacyTagManager<T>) Forge1192RegistryTagBridge.tags(this); }
    public T getValue(int id) {
        var registry = nativeRegistry(); T value = registry.byId(id);
        return value != null && registry.getId(value) == id ? value : defaultValue(registry);
    }
    public Optional<ResourceKey<T>> getResourceKey(T value) {
        if (value == null) return Optional.empty();
        var registry = nativeRegistry(); var key = registry.getResourceKey(value);
        if (key.isPresent()) return key;
        // Source HashBiMap reverse lookup uses equality; native registry indices
        // use identity. The common registered-object path above remains O(1).
        return registry.entrySet().stream().filter(entry -> Objects.equals(value, entry.getValue()))
                .map(Map.Entry::getKey).findFirst();
    }
    public Identifier getKey(T value) { return getResourceKey(value).map(ResourceKey::identifier).orElse(getDefaultKey()); }
    public ResourceKey<T> getKey(int id) { return getResourceKey(getValue(id)).orElse(null); }
    public int getID(T value) {
        var registry = nativeRegistry();
        var key = getResourceKey(value);
        T active = key.isPresent() ? registry.getOptional(key.get()).orElseThrow() : defaultValue(registry);
        return active == null ? -1 : registry.getId(active);
    }
    public int getID(Identifier key) {
        var registry = nativeRegistry();
        // Source getID(name) reads names directly, unlike source getValue(name).
        return getID(key != null && registry.keySet().contains(key) ? registry.getOptional(key).orElse(null) : null);
    }
    @Override public Iterator<T> iterator() {
        var iterator = nativeRegistry().iterator();
        return new Iterator<>() {
            public boolean hasNext() { return iterator.hasNext(); }
            public T next() { return iterator.hasNext() ? iterator.next() : null; }
        };
    }
    public Set<Identifier> getKeys() {
        return Collections.unmodifiableSet(new AbstractSet<>() {
            public int size() { return nativeRegistry().keySet().size(); }
            public Iterator<Identifier> iterator() { return nativeRegistry().keySet().iterator(); }
            public boolean contains(Object value) { return nativeRegistry().keySet().contains(value); }
        });
    }
    public Collection<T> getValues() {
        return Collections.unmodifiableSet(new AbstractSet<>() {
            public int size() { return nativeRegistry().size(); }
            public Iterator<T> iterator() { return nativeRegistry().iterator(); }
            public boolean contains(Object value) { return nativeRegistry().stream().anyMatch(entry -> Objects.equals(value, entry)); }
        });
    }
    public Set<Map.Entry<ResourceKey<T>, T>> getEntries() {
        return Collections.unmodifiableSet(new AbstractSet<>() {
            public int size() { return nativeRegistry().size(); }
            public Iterator<Map.Entry<ResourceKey<T>, T>> iterator() {
                return nativeRegistry().entrySet().stream().map(entry -> Map.entry(entry.getKey(), entry.getValue())).iterator();
            }
        });
    }
}
