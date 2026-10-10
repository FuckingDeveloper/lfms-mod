package com.fuckingdeveloper.lms.classloading;

/**
 * Profile-scoped transformation boundary for a class owned by a managed legacy artifact.
 * Implementations must be deterministic and must not initialize legacy classes.
 */
@FunctionalInterface
public interface LegacyClassTransformer {
    Result transform(String binaryName, byte[] original) throws Exception;

    /**
     * Supplies raw class bytes owned by the managed artifact/dependency graph.
     * Frame computation may inspect these bytes, but must never define or
     * initialize the class while another managed class is being transformed.
     */
    default void bindManagedClassBytes(ClassBytesLookup lookup) {}

    @FunctionalInterface
    interface ClassBytesLookup {
        byte[] find(String internalName) throws java.io.IOException;
    }

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
