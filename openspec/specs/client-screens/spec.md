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
