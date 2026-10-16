package com.fuckingdeveloper.lms.profile;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/** Immutable managed-artifact inputs; contains no native loader/container objects. */
public record SessionRequest(String modId, Path artifact, List<Path> dependencies,
                             ClassLoader parent, List<String> entrypoints,
                             String physicalDistribution) {
    public SessionRequest {
        Objects.requireNonNull(modId);
        Objects.requireNonNull(artifact);
        Objects.requireNonNull(parent);
        dependencies = List.copyOf(dependencies);
        entrypoints = List.copyOf(entrypoints);
        Objects.requireNonNull(physicalDistribution);
    }
}
