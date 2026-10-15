# Legacy ordered global loot modifiers

Forge 1.19.2 selects modifiers from the full low-to-high resource stack at
forge:loot_modifiers/global_loot_modifiers.json. Each replace clears the preceding
list; each repeated entry is removed and appended (last occurrence determines
order). Unlisted definitions do not execute. Target 26.3 instead loads all
modifier JSONs and sorts by descending per-modifier priority.

For admitted source packs LMS MUST preserve legacy selection and relative order.
The source group executes at the target default priority because the source has
no priority field. Native higher/lower priorities retain their target meaning;
equal-priority native/source interleaving remains unspecified by the target.
Native resources outside the legacy list remain ordinary target resources.
An explicit legacy list can select a target override of a source definition.
The source group MUST remain ordered even if native sorting is unstable.

The target loot-manager reload boundary reads the old control document before
native decoding, excludes that document and unselected source definitions from
that one listener's resource view, and retains selected IDs in native lookup.
No source pack/JAR, global resource manager or unrelated listener is mutated.
Missing/malformed selected definitions fail explicitly; no silently enabled
unlisted source modifiers. State belongs to the manager/reload, not a static
legacy classloader cache.

The initial codec migration is limited to source IGlobalLootModifier subtypes
whose codec method returns a field proven to be produced by Codec.unit(Supplier).
The existing producer transformation yields MapCodec.unit; the matching callback
return type migrates with it. Other codec shapes remain unsupported explicitly.
A source priority method collision is rejected rather than reinterpreted.

Verify list stack/replace/repetition, native-resource preservation, actual target
manager mixins, callback linkage, and actual legacy loot effects in dev runtime.
