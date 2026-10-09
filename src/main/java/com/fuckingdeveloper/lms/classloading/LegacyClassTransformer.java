package com.fuckingdeveloper.lms.classloading;

/**
 * Profile-scoped transformation boundary for a class owned by a managed legacy artifact.
 * Implementations must be deterministic and must not initialize legacy classes.
 */
@FunctionalInterface
public interface LegacyClassTransformer {
    Result transform(String binaryName, byte[] original) throws Exception;

    record Result(byte[] bytes, boolean transformed, String evidence) {
        public Result {
            if (bytes == null) throw new IllegalArgumentException("bytes");
            evidence = evidence == null ? "" : evidence;
        }

        public static Result unchanged(byte[] bytes, String evidence) {
            return new Result(bytes, false, evidence);
        }
    }
}
