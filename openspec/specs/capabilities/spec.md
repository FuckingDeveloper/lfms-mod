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

## Block provider query migration

Exact source `getCapability(Capability[, Direction])` calls MAY dispatch through
a typed block-provider bridge after verifying block-entity inheritance or the
source provider interface identity. Source overrides MUST execute with the
original token and nullable side, retaining their returned optional identity.
Explicit source-super block queries MUST bypass the override rather than
recursing into it. Per-class dispatch caches MUST NOT retain disposable loaders.

Native target block queries MUST use the authoritative attached server level,
position, current block entity and matching sided target capability. Target
absence MAY produce the source empty optional. An unknown source/custom
capability, detached/replaced receiver or unsupported native provider operation
MUST fail explicitly. Native client-provider and source attachment-event
integration remain separate gaps. Source-provider overrides remain usable on
their original side independently of this native-provider restriction.

Present native providers MUST invalidate the source optional when the target
invalidates its cache. Query retention MUST retain the target invalidation
listener without adding a global strong cache of worlds or source loaders.

Native item handlers MAY expose source slot reads, validation, capacity,
insertion and extraction through target resource handlers. Simulation MUST
abort target changes; committed source operations MUST commit a target
transaction. Returned counts and remainders MUST reflect the actual transfer,
input stacks MUST remain unchanged, and source integer queries MUST fail when
target amounts cannot be represented. Empty-resource capacity MUST use the
target's documented general-capacity contract. Direct slot replacement and
composition inside an existing target transaction remain separate gaps.
Native fluid operations and energy direction flags remain fail-closed until
their source contracts can be preserved; provider presence alone MUST NOT be
claimed as successful transfer.
