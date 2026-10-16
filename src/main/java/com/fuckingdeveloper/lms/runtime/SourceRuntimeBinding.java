package com.fuckingdeveloper.lms.runtime;

import com.fuckingdeveloper.lms.discovery.LegacyModDescriptor;
import com.fuckingdeveloper.lms.profile.SourceProfile;
import java.util.List;
import java.util.Set;

/** Implemented source/target integration. Native host objects stay in the implementation. */
public interface SourceRuntimeBinding {
    SourceProfile<?> profile();
    String targetId();
    Set<CompatibilityContract> providedContracts();
    void start(List<LegacyModDescriptor> selected, List<LegacyModDescriptor> discovered);
}
