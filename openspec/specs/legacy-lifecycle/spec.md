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

### Native config extension projection

The source ConfigScreenFactory's Minecraft/parent-screen function MAY implement
the target native config-screen extension. Source registration and lookup keys
MUST project to the target IConfigScreenFactory key on the same real native
container; suppliers MUST remain lazy and original source factory identity MUST
be preserved on lookup. Native factories MAY project back to a source function
using their owning native container. This MUST NOT fabricate managed native
containers or imply that managed mod configuration factories are registered.
A source mod-list screen construction MUST create the real target mod-list
screen with the evaluated parent, not silently substitute the parent screen.
Source subclasses of the removed screen remain fail-fast.

### Nested registered event ownership

A registered source event callback MAY temporarily enter its recorded owner even
when another source owner initiated the target event. That callback MUST restore
the previous context, active-container state and owning thread on success or
failure. Same-owner re-entry MUST preserve the current context identity. This
explicit event-dispatch operation MUST remain separate from networking's strict
recorded-context entry, which MUST NOT overwrite an unrelated active owner.

### Source client reload-registration event

The source once-per-client-construction reload-listener registration event MAY
project to the target sorted client registration event. Its unnamed listeners
MUST receive deterministic source-owner keys, preserving evaluation, registration
phase and listener identity. Existing keys MUST remain untouched; repeated source
listener keys MUST be disambiguated deterministically. A missing client event,
unknown input-code semantics or incomplete managed-container iteration MUST NOT
be declared supported solely because unrelated container getters link.
