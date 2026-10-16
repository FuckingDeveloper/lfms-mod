package com.fuckingdeveloper.lms.classloading;

/** Observed transformation metrics; intentionally absent from uninstrumented transformers. */
public interface InstrumentedClassTransformer extends LegacyClassTransformer {
    int transformedClasses();
    int totalRewrites();
    long transformationMillis();
}
