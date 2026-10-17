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

### Two-phase initial data transaction

When an ordered source setup needs authoritative dynamic defaults and publishes
static-tag/reloadable data, a target adapter MAY perform two explicit native data
phases before world publication. The bootstrap phase MUST execute source setup
once at the supported pre-binding boundary. The final phase MUST re-read native
static tags and reloadable data using the same world/dimension registries and
resource manager. Both phases MUST retain native component/tag binding lifecycle.
Failure of either phase MUST prevent world publication and preserve native cleanup.
Client listener replay MUST use the final phase's registry context. The adapter
MUST NOT repeat source setup, mutate frozen registries, loop until loading succeeds,
or apply this extra phase to ordinary reloads or subsequent world opens once setup
has been consumed. Changes to world-generation/dimension data after its registry
construction remain outside this contract and MUST NOT be claimed supported.

### Source extension declarations and removed display UI

Source extension registration MUST retain the active source container, original
lazy supplier and lookup identity. ModLoadingContext convenience registration
MUST delegate to its actual active source container. Managed declarations MUST
NOT be stored on the native LMS host. Source DisplayTest constructors, accessors,
constants and direct predicate calls MUST preserve source behavior. For the
approved NeoForge26.3 subset, the removed Forge server-list mod-version indicator
is an explicit unsupported feature: retaining its declaration MUST NOT imply
native status-wire, server-list rendering or handshake support. Native networking
checks MUST NOT be replaced by the source display predicate.

An explicit-class source event-listener registration MUST preserve priority,
receive-canceled policy, exact event type and consumer identity, source ownership,
unregister and failure cleanup through the existing event adapter. Catalogued
admission and transformation MUST use the same source declaration evidence.

### Source tag notification identity and readiness

Forge1.19.2 tag notifications MUST retain one source-owned concrete identity,
actual registry access, update cause and static-data update rule. Native sealed
subclasses MUST NOT change exact source class-token/getClass matching. Server
notifications MUST remain at their native publication phase with source priority.
An initial native client-configuration publication MUST reach source listeners
once after that connection's native login dispatch completes; native dispatch and
tag/component binding MUST NOT be delayed or duplicated. Later ready-client
updates MUST retain their actual publication boundary. Pending publications MUST
be scoped to the real connection and discarded on replacement/disconnect.
Source constructors and explicit posting MUST retain supported source meaning;
unknown subclasses remain fail-fast. See source-tag-notification.
