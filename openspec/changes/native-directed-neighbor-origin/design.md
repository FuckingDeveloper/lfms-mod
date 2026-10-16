# Native directed neighbor origins

Runtime75 installing a native comparator beside a source generator fails with
`Source neighbor callback has no authoritative origin`. The first causal call is
DiodeBlock.updateNeighborsInFront -> Level.neighborChanged -> queued update.

Pinned source 1.19.2 DiodeBlock and ObserverBlock pass their own block position
as the explicit source argument to the single-neighbor notification. The pinned
26.3 implementations still own that position, but pass only redstone Orientation
through the changed call ABI. Orientation cannot reconstruct the missing position.

At these exact native producer call sites, the target adapter MUST scope the
producer position around the single-neighbor call, preserving destination, block,
orientation, cancellation, queue lifetime and exception behavior. The existing
queue bridge MUST retain that position until execution. Nested scopes MUST restore
previous contexts. No guessed destination or active-callback origin is allowed.
The subsequent multi-neighbor notification retains its own native sourcePos;
it MUST NOT inherit the directed producer position. Legacy artifacts are immutable.

This covers comparator/repeater through DiodeBlock and ObserverBlock without mod
identity selection. Other direct native producers (ice, rails, cactus and pistons)
were inspected but are not declared covered: each requires its own pinned source
origin evidence. Missing origins remain explicit failures.

Verify actual applied mixins and queue capture for all three producers, then
native comparator output at full/partial/empty source energy and runtime logs.
