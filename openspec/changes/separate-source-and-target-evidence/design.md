# Source ancestry, target ABI and reload lookups

The 2026-10-14 17:35 run fails during water-driven block loot generation on
Tags.Items.SHEARS. Earlier in the same run, deferred crop setup fails on
Tags.Biomes.IS_WATER. Source-loader metadata introduced in e1d4815 shadows target
ABI inspection, so existing verified tag renames fail their target-field gate.

Source declaration lookup MUST continue to prefer managed bytes and pinned source
loader metadata. Target ABI validation MUST use actual parent-runtime class bytes,
with a separate cache; managed/source headers MUST NOT prove target members.
Apply the existing verified common-tag aliases to direct GETSTATIC instructions
and GETSTATIC handles in LDC, invokedynamic arguments and nested ConstantDynamic.
Keep unrelated owners, descriptors, writes and unknown aliases unchanged/unresolved.
This restores tag identity linkage; it does not prove migration of legacy tag JSON
contents or datapack membership. Original inputs remain immutable.

The same run has recipe/advancement reload errors resolving minecraft:is_fire.
The source AddReloadListenerEvent callback consumes registries synchronously,
before registered reload listeners execute. The target builds pending components
with lookupWithUpdatedTags, then fires AddServerReloadListenersEvent before global
tag/component binding. Its getServerResources().getRegistryLookup() is the
proven lookup for that entire callback, including its synchronous early stacks.

LMS MUST scope that exact provider around source event callback delivery, in
addition to the existing registered-listener executor scopes. Nested scopes and
exceptions MUST restore the preceding provider. No global registry mutation,
empty fabricated damage tag, removed fire resistance or swallowed callback is
permitted. Data-component pre-binding and block-data scopes retain precedence.

Evidence: exact target patched ReloadableServerResources, Item.Properties,
DataComponentInitializers and NeoForge AddServerReloadListenersEvent sources.
The global-loot-modifier list JSON parse error is a separate resource-format gap;
this change does not claim to resolve it. Generator gameplay acceptance remains
pending; the overall observed compatibility level remains 6 (world load).
