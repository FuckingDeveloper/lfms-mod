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
