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
        java.util.List<InstructionEdit> edits,
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

    /** Loader-neutral edit recovered from legacy instruction-list JavaScript. */
    public record InstructionEdit(EditKind kind, InstructionLocation location,
                                  String firstArgument, String valueExpression,
                                  InstructionSpec value, java.util.List<InstructionSpec> values) {}

    public record InstructionSpec(InstructionKind kind, MethodRef method,
                                  Integer opcode, Integer variable, String expression) {}

    public enum InstructionKind {
        METHOD_CALL,
        SIMPLE_OPCODE,
        VARIABLE,
        JUMP,
        UNRESOLVED
    }

    public enum EditKind {
        REMOVE,
        INSERT_BEFORE,
        INSERT_AFTER,
        REPLACE
    }

    public enum InstructionLocation {
        EXACT,
        PREVIOUS,
        NEXT,
        EXPRESSION
    }

    public enum Invocation {
        VIRTUAL,
        STATIC,
        INTERFACE,
        SPECIAL,
        UNKNOWN
    }
}
