# Native recipes with source nominal type tokens

User approved retaining native Minecraft26.3 recipe identities. Actual values,
getClass and execution MUST remain native. Removed Forge1.19.2 TippedArrowRecipe,
ShulkerBoxColoring and SuspiciousStewRecipe MUST have three distinct source-owned
nominal identities with their verified CustomRecipe ancestry. Native ImbueRecipe,
TransmuteRecipe and ShapelessRecipe MUST NOT be aliased to those source tokens.
No target recipe may be hidden or replaced to make an old class-key consumer pass.

Projection MUST cover class literals, generic signatures, descriptors, annotations,
arrays, casts, instanceof and nested type constants through the ordinary namespace
projection. Source construction, direct subclassing and unproven executable or
field members (including constructor/method handles inside dynamic constants)
MUST fail explicitly before execution. Nominal support MUST NOT be claimed as
constructor/crafting or reflective modifier/name parity. Token-only classes have
no executable recipe instances; they MUST NOT supply dummy matching/results.

The surviving ShieldDecorationRecipe remains a native type. The source token
policy does not establish display/creation support for its old recipe makers.
Pinned target resources supply ordinary tipped-arrow imbue, shulker transmutation
and component-bearing suspicious-stew shapeless recipes. Source rules for custom
recipe construction/matching/serialization remain separate gaps.
