package com.fuckingdeveloper.lms.profile.forge1192.runtime.registry;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;

/** Target-26.3 implementation of Forge 1.19.2's live registry-tag views. */
public final class Forge1192RegistryTagBridge {
    private static final Map<Registry<?>, Manager<?>> MANAGERS = new WeakHashMap<>();

    private Forge1192RegistryTagBridge() {}

    public static LegacyTagManager<?> tags(Object registryToken) {
        Registry<?> registry = requireRegistry(registryToken);
        synchronized (MANAGERS) {
            return MANAGERS.computeIfAbsent(registry, Manager::new);
        }
    }

    /** Forge 1.19.2 Registry#getTagNames over the target's named tag sets. */
    public static Stream<TagKey<?>> tagNames(Object registryToken) {
        return requireRegistry(registryToken).getTags().map(HolderSet.Named::key);
    }

    private static Registry<?> requireRegistry(Object registryToken) {
        if (registryToken instanceof Registry<?> registry) return registry;
        throw new IllegalArgumentException("Legacy registry tag query requires a target Registry token");
    }

    private static final class Manager<T> implements LegacyTagManager<T> {
        private final Registry<T> registry;
        private final Map<TagKey<T>, Tag<T>> tags = new ConcurrentHashMap<>();

        private Manager(Registry<T> registry) {
            this.registry = registry;
        }

        @Override
        public LegacyTag<T> getTag(TagKey<T> name) {
            Objects.requireNonNull(name, "name");
            if (!name.isFor(registry.key())) {
                throw new IllegalArgumentException("Tag " + name + " does not belong to registry "
                        + registry.key().identifier());
            }
            return tags.computeIfAbsent(name, key -> new Tag<>(registry, key));
        }

        @Override
        public boolean isKnownTagName(TagKey<T> name) {
            Objects.requireNonNull(name, "name");
            return name.isFor(registry.key()) && registry.get(name).isPresent();
        }

        @Override
        public Stream<LegacyTag<T>> stream() {
            return tagNames().stream().map(this::getTag);
        }

        @Override
        public Stream<TagKey<T>> getTagNames() {
            return tagNames().stream();
        }

        @Override
        public Iterator<LegacyTag<T>> iterator() {
            return stream().iterator();
        }

        private LinkedHashSet<TagKey<T>> tagNames() {
            LinkedHashSet<TagKey<T>> names = new LinkedHashSet<>(tags.keySet());
            registry.getTags().map(HolderSet.Named::key).forEach(names::add);
            return names;
        }
    }

    private static final class Tag<T> implements LegacyTag<T> {
        private final Registry<T> registry;
        private final TagKey<T> key;

        private Tag(Registry<T> registry, TagKey<T> key) {
            this.registry = registry;
            this.key = key;
        }

        @Override
        public TagKey<T> getKey() {
            return key;
        }

        @Override
        public Stream<T> stream() {
            return holders().stream().flatMap(HolderSet.Named::stream).map(Holder::value);
        }

        @Override
        public Iterator<T> iterator() {
            return stream().iterator();
        }

        @Override
        public boolean isEmpty() {
            return size() == 0;
        }

        @Override
        public int size() {
            return holders().map(HolderSet.Named::size).orElse(0);
        }

        @Override
        public boolean contains(T value) {
            return stream().anyMatch(candidate -> candidate == value || Objects.equals(candidate, value));
        }

        @Override
        public Optional<T> getRandomElement(RandomSource random) {
            Objects.requireNonNull(random, "random");
            return holders().flatMap(set -> set.getRandomElement(random)).map(Holder::value);
        }

        @Override
        public boolean isBound() {
            return holders().map(HolderSet.Named::isBound).orElse(false);
        }

        private Optional<HolderSet.Named<T>> holders() {
            return registry.get(key);
        }
    }
}
