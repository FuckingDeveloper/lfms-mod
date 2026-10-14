# Source stack-sensitive crafting remainder queries

Forge 1.19.2 ItemStack.hasCraftingRemainingItem/getCraftingRemainingItem MUST
delegate to source-owned Item overrides with the original mutable stack. Preserve
predicate and result overrides independently, inherited virtual dispatch, and
explicit source-super default behavior. Source results MUST retain identity and
must not be converted to immutable templates inside a source query.

For the default source operation, the native construction remainder template
represents the original item-property intent; its presence is the predicate and
create() supplies a fresh stack. Absence MUST yield ItemStack.EMPTY. Default
result lookup MUST respect a source predicate override; a true predicate without
an actual result/construction remainder MUST fail explicitly. Native modern
template components/count are preserved rather than discarded.

This does not establish native crafting-pipeline invocation of legacy overrides.
Use remainder/food consumption is distinct from crafting remainder. ForgeHooks
getCraftingRemainingItem also includes damage/destruction events and MUST NOT be
blindly replaced by the simpler ItemStack query. Source no-argument Item APIs and
unproven interface-super shapes remain separate boundaries.
