package com.fuckingdeveloper.lms.profile;

import com.fuckingdeveloper.lms.discovery.LegacyModDescriptor;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Deterministic evidence-based selection, shared by prelaunch and live admission. */
public final class SourceProfiles {
    private final List<SourceProfile<?>> profiles;

    public SourceProfiles(Collection<? extends SourceProfile<?>> candidates) {
        var indexed = new LinkedHashMap<String, SourceProfile<?>>();
        for (var profile : candidates) {
            Objects.requireNonNull(profile);
            if (profile.id() == null || profile.id().isBlank())
                throw new IllegalArgumentException("Empty source profile identity");
            if (indexed.putIfAbsent(profile.id(), profile) != null)
                throw new IllegalArgumentException("Duplicate source profile: " + profile.id());
        }
        profiles = List.copyOf(indexed.values());
    }

    public List<SourceProfile<?>> installed() { return profiles; }

    public Optional<SourceProfile<?>> resolve(LegacyModDescriptor mod) {
        Objects.requireNonNull(mod);
        var matches = profiles.stream().filter(p -> p.supports(mod)).toList();
        if (matches.size() > 1)
            throw new IllegalStateException("Ambiguous source profile for " + mod.file()
                    + ": " + matches.stream().map(SourceProfile::id).toList());
        return matches.stream().findFirst();
    }
}
