package com.fuckingdeveloper.lms.runtime.registry;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

/** Source-owned intent; only the supplied target phase executor performs mutation. */
public final class StagedRegistrations<K, N, V> {
    private record Entry<N,V>(N name,V value) {}
    private final Map<String,Map<K,ArrayDeque<Entry<N,V>>>> pending = new HashMap<>();
    private final Map<String,Set<K>> completed = new HashMap<>();

    public synchronized boolean completed(String owner,K key) {
        return completed.getOrDefault(owner,Set.of()).contains(key);
    }

    public synchronized void complete(String owner,K key) {
        Objects.requireNonNull(owner); Objects.requireNonNull(key);
        completed.computeIfAbsent(owner,ignored -> new HashSet<>()).add(key);
    }

    public synchronized void stage(String owner,K key,N name,V value) {
        Objects.requireNonNull(owner); Objects.requireNonNull(key);
        Objects.requireNonNull(name); Objects.requireNonNull(value);
        if (completed(owner,key))
            throw new IllegalStateException("Registration after completed phase: " + owner + " / " + key + " / " + name);
        pending.computeIfAbsent(owner,ignored -> new HashMap<>())
                .computeIfAbsent(key,ignored -> new ArrayDeque<>()).addLast(new Entry<>(name,value));
    }

    public synchronized Optional<N> nameForIdentity(String owner,V value) {
        N match = null;
        var registries = pending.get(owner);
        if (registries == null) return Optional.empty();
        for (var queue : registries.values()) for (var entry : queue) {
            if (entry.value() != value) continue;
            if (match != null && !match.equals(entry.name()))
                throw new IllegalStateException("Object staged under multiple registry names");
            match = entry.name();
        }
        return Optional.ofNullable(match);
    }

    public void flush(String owner,K key,BiConsumer<N,V> execute) {
        Objects.requireNonNull(execute);
        while (true) {
            Entry<N,V> entry;
            synchronized (this) {
                var registries = pending.get(owner);
                var queue = registries == null ? null : registries.get(key);
                if (queue == null || queue.isEmpty()) return;
                entry = queue.getFirst();
            }
            execute.accept(entry.name(),entry.value());
            synchronized (this) {
                var registries = pending.get(owner);
                var queue = registries == null ? null : registries.get(key);
                if (queue == null || queue.peekFirst() != entry)
                    throw new IllegalStateException("Pending registration queue changed during flush: " + owner + " / " + key);
                queue.removeFirst();
                if (queue.isEmpty()) registries.remove(key);
                if (registries.isEmpty()) pending.remove(owner);
            }
        }
    }
}
