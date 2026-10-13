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

Forge 1.19.2 capability handles MUST be keyed by their original generic payload
type's internal name, not the anonymous token subclass. Distinct tokens for the
same payload and the corresponding built-in Forge token MUST resolve to one
handle. Recovery MUST use source signature evidence and MUST NOT load an
unadapted payload class to infer its identity. Identity caches MUST NOT retain
disposable source classloaders. Preserved handles and optional identity tests
MUST NOT be claimed as working target provider lookup, attachment, registration,
or invalidation integration; those operations remain independent boundaries.
