package com.fuckingdeveloper.lms.runtime.registry;

import java.util.stream.Stream;
import net.minecraft.tags.TagKey;

/** Read-only Forge 1.19.2 tag-manager contract exposed by an LMS registry view. */
public interface LegacyTagManager<T> extends Iterable<LegacyTag<T>> {
    LegacyTag<T> getTag(TagKey<T> name);

    boolean isKnownTagName(TagKey<T> name);

    Stream<LegacyTag<T>> stream();

    Stream<TagKey<T>> getTagNames();
}
