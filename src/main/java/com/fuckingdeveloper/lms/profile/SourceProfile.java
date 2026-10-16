package com.fuckingdeveloper.lms.profile;

import com.fuckingdeveloper.lms.classloading.InstrumentedClassTransformer;
import com.fuckingdeveloper.lms.discovery.LegacyModDescriptor;
import com.fuckingdeveloper.lms.runtime.CompatibilityContract;
import com.fuckingdeveloper.lms.runtime.RuntimeSession;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

/** Source-generation policy; no native target types are part of this contract. */
public interface SourceProfile<S extends RuntimeSession> {
    String id();
    boolean supports(LegacyModDescriptor descriptor);
    SourceEvidencePaths evidence(Path runDirectory);
    InstrumentedClassTransformer createTransformer(String physicalDistribution);
    S createSession(SessionRequest request) throws IOException, ClassNotFoundException;
    Set<CompatibilityContract> requiredContracts();
    java.util.List<com.fuckingdeveloper.lms.transform.TransformationSpec> planLaunch(
            Path runDirectory, LegacyModDescriptor descriptor) throws IOException;
}
