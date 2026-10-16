# Client Screen Compatibility

Source generation: Forge 1.19.2. Target generation for the bounded implementation:
Minecraft 26.3 / NeoForge 26.3.0.58-beta.

## Container-screen state getters

The five Forge-added source getters for left/top position, image width/height and
hovered slot MAY project to the inspected native getters on the same screen.
Projection MUST use exact source owner ancestry, method name and descriptor;
descriptor coincidence MUST NOT select unrelated methods.

Source overrides, inherited dispatch and explicit superclass calls MUST preserve
their implementation and invocation kind. Callable references, invokedynamic and
nested dynamic constants MUST retain their receiver, descriptor and dispatch.
State reads MUST observe live native state, including genuine absent hovered slots,
without taking a snapshot or synthesizing geometry. Unknown target-name collisions
in source subclasses MUST fail explicitly rather than hijack native virtual calls.

Source screen dimension writes MAY retain their original protected field access
on the same native receiver. The pinned target adapter MUST restore the source
width/height mutability: inspected target centering, bounds and slot seeding read
these fields directly. A read adapter MUST NOT leave source dimension writes
illegally targeting final fields. Source-owned hidden fields remain distinct.

This contract does not establish old GUI rendering/input equivalence, slot-color
rendering semantics or functional menu support. These remain separate boundaries.

## Bounded source button construction and activation

A direct source Button subclass's six-argument superclass construction MAY append
the inspected native default narration supplier to the target seven-argument
constructor. The source default has no tooltip callback effects; custom source
tooltip constructors MUST NOT silently lose their callbacks. All six evaluated
arguments and the native button receiver MUST retain their identity/value.

The source no-argument activation MUST retain virtual override and explicit
source-super order when invoked by native input activation. Native input data
MUST NOT be fabricated as a source argument. The inspected source and target
Button defaults both invoke their stored OnPress callback with the same receiver;
source-super base activation MAY implement exactly that operation.
The inspected source/target ExtendedButton inherit this same activation default;
their native superclass path MAY terminate at that verified default as well.

Custom rendering, other unverified native superclass activation, direct allocation of
the now-abstract native Button, custom tooltip constructors and source-super callable
references remain unproven boundaries in this bounded implementation.

## Source loader inheritance and widget fields

Native loader ancestors in source member lookup MUST use source-generation
declaration evidence. Target namespace or target ancestry alone MUST NOT authorize
a source field match. Managed declarations MUST take precedence over bundled
source evidence; absent evidence MUST remain unresolved. Field lookup MUST preserve
the JVM declaration/interface/superclass order and descriptor identity.

Source public widget coordinates MAY use exact native raw storage after target
visibility changes. Such direct field reads/writes MUST NOT invoke overridable
geometry getters/setters. Source-owned hidden fields MUST remain distinct. Surviving
field handles MUST retain their original callable type, including receiver type;
nested constants MUST undergo the same checked lookup as direct instructions.
Source metadata MUST be generated from the pinned immutable development artifact
with recorded provenance and indexed once, rather than hand-written per mod.
Metadata-only class headers MUST NOT be defined as executable runtime classes.
After an explicit source-profile namespace projection, hierarchy queries MUST
retain the original source declaration evidence. Reverse projections MUST be
indexed; ambiguous source aliases MUST NOT select target ancestry as a shortcut.
This reverse lookup applies to namespace projection, not semantic replacement
shims whose target hierarchy is deliberately implemented by LMS.

### Source widget pointer hooks

Verified source AbstractWidget coordinate-only onClick/onRelease/onDrag overrides
MUST participate in the native pointer lifecycle with once-only coordinates and
drag deltas. Superclass dispatch MUST remain special. Selection MUST use source
ancestry/member identity, not fixture names. Native list/entry pointer boundaries
MUST translate named source/target button identity in both directions; raw SDL
button values MUST NOT leak into source callbacks. Unproven callable forms MUST
fail explicitly. Config editor functionality still requires real save/reopen
acceptance independently of callback linkage.

### Source list focus and selection

Adapted source lists MUST retain independent focus and selection on the actual
native storage. Source base setFocused MUST NOT implicitly select, clear selection
or deliver child focus callbacks; source base setSelected MUST NOT implicitly scroll.
Explicit source selection callbacks and scrolling retain their dispatch. Repeated
explicit selections MUST NOT be deduplicated by equality. Native-only list behavior
MUST remain unchanged.

### Logical context of client search tooltips

Source logical-side queries during verified client search tooltip evaluation MUST
retain logical CLIENT even on native background workers with inherited server
thread groups. Preserve actual nullable player/context/flag arguments, once-only
callback delivery and exceptions. Native FML side queries and unrelated source
operations MUST remain unchanged; nested source context MUST restore on all exits.
This does not establish general callback thread safety or authorize fake players.
