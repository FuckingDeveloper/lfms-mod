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
absence MAY produce the source empty optional. A source/custom capability MUST query the genuine source attachment dispatcher;
only established dispatcher absence MAY produce empty. A missing dispatcher
lifecycle hook, detached/replaced native receiver or unsupported native provider
operation MUST fail explicitly. Native client-provider lookup remains a separate gap. Source-provider overrides remain usable on
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

## Eager source block attachments

The Forge 1.19.2 source attachment event MUST execute at the end of the native
block-entity base constructor, after its base fields are set and before subclass
construction. First-query gathering is forbidden. Subject filtering and listener
priority/owner contexts MUST be preserved. Dispatcher state MUST belong to the
receiver, without global maps retaining receivers, source loaders or worlds.
Queries MUST return the first present provider optional in insertion order,
retain token/side/identity, reject null results, and return empty after genuine
provider absence or invalidation. During gathering the source base dispatcher
is still empty, matching the original superclass contract.

Removal and source chunk-unload callbacks MUST invalidate attachment listeners.
Source invalidate/revive operations MUST retain the original dispatcher lifecycle;
revive changes validity and MUST NOT re-fire the constructor event. Serializable
providers MUST retain their original ordered ForgeCaps keys and Tag payloads
through the target ValueInput/ValueOutput boundary. Duplicate keys MUST fail.
Source item/entity attachment lifecycle and inherited GenericEvent APIs remain
separate unsupported boundaries; registration requiring them MUST fail explicitly.

## Native energy operation views

A present native energy provider MAY expose source amount/capacity and
receive/extract operations without claiming source direction flags. Nonnegative
requests MUST use native transactions, abort simulations, validate actual
returned amounts before commit, and preserve target invalidation and sided
provider selection. Unrepresentable integer queries MUST fail explicitly.
canReceive/canExtract MUST remain explicit unsupported operations until an
authoritative equivalent is available; full/empty storage or a simulated transfer
MUST NOT be used to infer permanent direction flags. Nested target transactions
remain an explicit separate gap. Source-owned overrides and their flags MUST
retain precedence. Native-provider functionality MUST be tracked separately
from the reference mod's independent energy network.
