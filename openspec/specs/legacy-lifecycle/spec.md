# Legacy Lifecycle Specification

## Requirements

### Lifecycle model
LMS MUST model supported legacy Forge/FML lifecycle stages explicitly rather than relying on accidental target-loader ordering.

### Ordering
For each profile, lifecycle ordering constraints MUST be documented and enforced where required for observable legacy behavior.

### Event adaptation
Supported legacy lifecycle/event concepts MUST be translated to target lifecycle/event behavior by the profile/runtime. Unsupported events MUST be diagnosable.

### Registration boundary
Registration triggered by legacy lifecycle code MUST be routed through the appropriate LMS registry compatibility mechanism rather than bypassing target registry constraints.

### Side awareness
Client-only legacy lifecycle behavior MUST NOT execute on a dedicated server. Server/common behavior MUST NOT require client classes.

### Failure
A fatal lifecycle adaptation failure MUST identify the artifact, lifecycle stage and failed compatibility capability.

### Living fall-event precision boundary

Forge 1.19.2 fall-distance reads MUST expose source float precision from the
same authoritative target event. Source writes and construction MUST widen the
float distance to the target double without replacing the damage multiplier,
cancellation state, entity, side or dispatch timing. Getter/setter calls,
method references, nested dynamic constants and source constructors MUST use
verified event ancestry and exact source descriptors. Source float-distance
overrides remain fail-fast until override dispatch can be preserved. A read
followed by a write intentionally applies source float rounding; merely
observing the event MUST NOT round or otherwise mutate its target distance.
