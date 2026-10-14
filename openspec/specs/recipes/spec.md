# Recipe Compatibility Specification

Recipe compatibility MUST distinguish source construction/views, target executable
recipe behavior, registration, native serialization and source wire protocols.
Linking a recipe class MUST NOT establish recipe registration or crafting support.

For the Forge 1.19.2 two-input UpgradeRecipe contract, a target adapter MAY expose
base/addition through a native smithing input with an empty template slot. It MUST
retain the source identifier, ingredient objects, original result-stack identity
and source default query behavior. Assembly MUST copy the result and replace its
legacy NBT with a copy of the base stack's legacy NBT only when that NBT exists.
It MUST NOT substitute native component-transmutation semantics without evidence.

Native recipe display/placement MAY expose the actual target ingredient slots
while the source default ingredient-list view remains empty. A native serializer
for this adapted behavior MUST preserve the identifier, ingredients, result stack
and assembly semantics across decoding. It MUST be registered in the valid target
recipe-serializer registration phase. Supporting a native codec MUST NOT be
presented as implementation of the source JSON/byte-buffer serializer contract.
Unsupported source serialization MUST remain explicit.

Custom source subclass callbacks MUST NOT be supplied a fabricated mutable
Container backed only by an immutable target recipe-input snapshot. Direct source
calls MUST preserve their supplied Container. Until authoritative mutation and
ownership semantics are proven, native consumption/serialization of such
subclasses MUST fail explicitly rather than discard overrides or their state.
