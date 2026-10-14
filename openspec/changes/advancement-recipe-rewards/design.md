# Advancement recipe reward keys

Source Builder.recipe/addRecipe stores resource identifiers. Target 26.3 stores
ResourceKey<Recipe<?>> and resolves unlock behavior when rewards are granted.
Convert exact source identifiers into keys for Registries.RECIPE, preserving
builder identity, fluent chaining, ordering and duplicates. This does not assert
that a recipe with the key has actually been registered or unlocked.

Inspect source/target sibling APIs: experience and non-null function signatures
have ordinary mapped equivalents; loot identifiers changed to holders and their
resolution timing requires a separate authoritative-context adapter. Do not
namespace-rename loot identifiers into holders or fabricate them. This change
implements the proven recipe static/instance pair, including callable constants.
