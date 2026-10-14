# Failure Handling Specification

LMS MUST prefer attributable compatibility errors over downstream opaque JVM linkage failures when an incompatibility can be detected earlier.

A compatibility failure SHOULD identify the legacy artifact, selected profile, subsystem/stage, transformation or capability, class/member/resource when applicable, root cause, and whether startup may safely continue.

Failure of one optional legacy artifact MAY be isolated only when continuing cannot corrupt registries, dependency state, persistent data or networking assumptions.

LMS MUST NOT continue after a failure when doing so is known to leave globally inconsistent registration/lifecycle state. Diagnostics SHOULD retain causal chains instead of replacing the original exception with a generic message.
