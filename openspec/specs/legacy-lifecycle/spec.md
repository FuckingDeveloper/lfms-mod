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

### Selective subscriber linkage and ownership

Annotated source subscriber discovery MUST use class-file metadata before
resolving method signatures. For managed classes, that metadata MUST describe
the exact defined transformed class. Resolving unrelated utility-method types
MUST NOT be a prerequisite for linking an otherwise supported event handler.
Every applicable annotated handler MUST still be validated and linked; a missing
handler event type or invalid signature MUST fail explicitly before installation.

Instance/class registration MUST retain source public instance/static selection,
inherited annotation and virtual dispatch, priority and cancelled-event policy.
Conflicting inherited annotations without established source precedence MUST
remain an explicit unsupported boundary. Source registration with no applicable
handlers MAY remain empty, as in the source bus; this MUST NOT skip applicable
handlers. Repeated registration of the same subscriber on the same bus MUST NOT
duplicate callbacks. Source unregister MUST remove all wrappers belonging to
that registration, including event fanout, without removing another subscriber.
Partial installation failure MUST undo installed wrappers and retain its cause.
Class-file indexes MUST NOT independently retain disposable managed loaders.
Native reinitialization MAY reuse the source owner previously recorded for that
same subscriber and bus after unregister. Such provenance MUST use weak ownership
keys, retain the original owner on duplicate registration, and restore thread
context after registration. An unknown owner MUST fail explicitly; class/package
names MUST NOT substitute for recorded provenance.
