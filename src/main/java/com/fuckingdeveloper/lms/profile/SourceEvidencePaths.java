package com.fuckingdeveloper.lms.profile;

import java.nio.file.Path;
import java.util.Objects;

/** Source-generation-owned evidence, distinct from the active target ABI. */
public record SourceEvidencePaths(Path srg, Path mojang, Path runtimeJar) {
    public SourceEvidencePaths {
        Objects.requireNonNull(srg);
        Objects.requireNonNull(mojang);
        Objects.requireNonNull(runtimeJar);
    }
}
