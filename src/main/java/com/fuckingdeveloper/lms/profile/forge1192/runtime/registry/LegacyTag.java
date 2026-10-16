package com.fuckingdeveloper.lms.profile.forge1192.runtime.registry;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;

/**
 * Stable Forge 1.19.2 tag view backed by the target registry's live tag data.
 *
 * <p>The target may rebind tags during data reload. Implementations therefore
 * resolve their contents for every operation instead of snapshotting holders.</p>
 */
public interface LegacyTag<T> extends Iterable<T> {
    TagKey<T> getKey();

    Stream<T> stream();

    boolean isEmpty();

    int size();

    boolean contains(T value);

    Optional<T> getRandomElement(RandomSource random);

    boolean isBound();
}
