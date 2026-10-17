# Supplier-backed stairs — accepted target construction policy

Runtime110 now executes previously blocked source registration and reaches the
removed Forge StairBlock(Supplier<BlockState>,Properties) constructor.
Pinned Forge43.4.0 stores the supplier without evaluating it and delegates later
base-block queries through getModelState/getModelBlock. Its source construction
uses AIR as its own private initial base, so this is an actual lazy contract.
Minecraft26.3 requires an actual BlockState at construction and stores that base.
An eager supplier.get is therefore not an equivalent rename.

Proposed target-native policy: evaluate the supplied base once at actual native
stair construction, then use native Minecraft26.3 geometry and base-state rules.
Preserve original supplier exceptions, state/properties identity and source custom
subclass callbacks. A dependency unavailable at that construction point fails
explicitly; no dummy base or global registry mutation. Source dynamic changes to
the supplier's later result are not supported by this policy. No mod identity
selects the rule; immutable input JARs remain unchanged.

Alternative: retain dynamic source supplier semantics via a broader independent
stair compatibility base and source delegation model. The user selected the native one-time policy; this alternative is not implemented.
