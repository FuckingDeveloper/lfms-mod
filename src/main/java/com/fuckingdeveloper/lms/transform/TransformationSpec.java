package com.fuckingdeveloper.lms.transform;

/**
 * Loader-neutral description of one early bytecode transformation.
 *
 * This is data produced by compatibility planning, not mod-specific code.
 * The launch/FML layer consumes a serialized form of these records later.
 */
public record TransformationSpec(
        String id,
        Kind kind,
        MethodRef target,
        MethodRef anchor,
        MethodRef replacement,
        Readiness readiness,
        String reason
) {
    public enum Kind {
        METHOD_CALL_REDIRECT,
        INSTRUCTION_EDIT
    }

    public enum Readiness {
        READY,
        UNRESOLVED
    }

    public record MethodRef(String owner, String name, String descriptor, Invocation invocation) {}

    public enum Invocation {
        VIRTUAL,
        STATIC,
        INTERFACE,
        SPECIAL,
        UNKNOWN
    }
}
