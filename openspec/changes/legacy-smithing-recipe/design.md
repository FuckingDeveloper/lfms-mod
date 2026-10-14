# Forge 1.19.2 UpgradeRecipe on target 26.3

The source class is a two-slot recipe retaining its identifier, ingredients and
original result stack. Source assembly copies the result, and replaces its legacy
NBT with a copy of the base item's NBT only when that base NBT exists. Native
SmithingTransformRecipe instead migrates component patches; a blind rename would
change this behavior.

Use a source-generation facade implementing the actual target SmithingRecipe
contract. Native input MUST require an empty template and MUST expose base/addition
as source Container slots 0/1 for the proven base implementation. Direct source
callbacks retain their supplied Container; native consumption of custom source
subclasses MUST fail explicitly because native immutable input cannot establish
the original mutable Container ownership/side effects. Source result identity
and NBT ownership MUST be retained. Native display and placement use the
actual target smithing display/ingredient APIs. Source default getIngredients is
empty and MUST remain empty in the source view even though native placement has
both ingredients. Constructor/type/signature/handle references use the existing
profile namespace mechanism; no integration-mod selectors are added.

Register an LMS-owned native serializer only during the target recipe-serializer
RegisterEvent. Its native codecs preserve source id, both ingredients and the full
result stack; deserialization must retain the same assembly semantics. Original
legacy artifacts remain unchanged. Source-era serializer/network APIs are a
separate boundary and MUST fail explicitly until implemented; the facade MUST NOT
pretend the native codec is the legacy byte protocol.

Verify native matches/assembly, template rejection, result identity, NBT copy and
absence behavior, inherited callbacks, source views, native codec round trips and
immutable bytecode fixtures. Then run existing checks and a fresh Quick Play
client; gate subsequent gameplay with MCP status and world/player availability.
