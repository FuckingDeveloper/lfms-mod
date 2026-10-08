# Capability Policy Specification

## States

Every diagnosed compatibility capability uses one of:
SUPPORTED, PARTIAL, UNSUPPORTED, BLOCKED, NOT_APPLICABLE, UNKNOWN.

## Requirements

Profiles MUST declare or derive capabilities required for supported behavior.

A mandatory capability in UNSUPPORTED, BLOCKED or unresolved UNKNOWN state MUST prevent LMS from claiming a compatibility level that requires it.

Optional functionality MAY degrade when its absence does not violate required lifecycle or data integrity.

LMS MUST NOT silently replace an unsupported operation with a no-op when that could change persistent state, registration identity, networking semantics or core gameplay behavior.

A deliberate no-op compatibility rule MUST be documented as such and exposed in diagnostics.
