# Source fuel query family

Forge 1.19.2 ForgeHooks.getBurnTime MUST short-circuit empty stacks, dispatch the
source item override with the actual stack and nullable RecipeType, use source
vanilla rules only for the -1 default, and post the source fuel event once per
query. ItemStack.getBurnTime is only the item callback, without fallback/events.
Explicit Item/IForgeItem default-super calls MUST return the verified -1 default.

Pinned AbstractFurnaceBlockEntity.getFuel rules retain insertion order, source
literal item registration IDs, source tag IDs, and non_flammable_wood exclusion.
Queries read currently bound target tags without retaining world/classloader
caches. This preserves source updateBurns intent at completed tag reloads; no
support is claimed for querying in the middle of a reload or source updateBurns.
No native fast-furnace context is fabricated. Native CookingFuel components MUST
NOT be used as a source baseline: they may already contain legacy event results
and use context-dependent durations (coal normal=1600, fast=800).

The source event MUST expose the original stack/type, ignore negative setBurnTime,
and cancel after a nonnegative override, preserving priority/receiveCanceled.
Listeners use the existing gameplay bus and lifecycle ownership wrapper.
Existing native default-component materialization remains a distinct partial
boundary; its two-argument event explicitly rejects a recipe-type query. It does
not establish per-stack events for target vanilla furnaces. Source queries MUST
NOT double-apply these materialized component results.

Source overrides require verified Item ancestry and the exact Forge extension
signature. Other ForgeHooks operations (loot modification, crafting-player
context, tool-drop checks) remain separate semantic boundaries, not renamed by
this fuel adapter. Legacy artifacts remain immutable.
