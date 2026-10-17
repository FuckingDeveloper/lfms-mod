# Source item creator metadata

Forge43.4.0 IForgeItem.getCreatorModId(ItemStack) MUST preserve virtual source
overrides and explicit base/default dispatch. The target's added registry-provider
parameter MUST NOT erase the source override. Native consumers of a managed
source override MUST dispatch to that source implementation once.
Direct, inherited, interface, callable and nested-constant invocations MUST use
one verified member family; unrelated descriptor matches MUST remain untouched.

The source default inspects the supplied stack (not the receiver's registry ID).
It uses the registered item namespace; only minecraft-owned items apply the
source specializations: a single registered enchantment on EnchantedBookItem,
the base potion on PotionItem/TippedArrowItem, and the default entity type of a
SpawnEggItem, ignoring stack entity overrides. Typed native component data MUST
supply these facts without exposing a fake legacy NBT schema. Missing metadata
retains the source fallback/null result. Source override return values, including
null, MUST remain unchanged. No mod identity selects production behavior.

Evidence: Forge43.4.0 IForgeItem and ForgeHooks.getDefaultCreatorModId; target
26.3 IItemExtension adds HolderLookup.Provider, CommonHooks uses native components;
SpawnEggItem now stores its default type in item default components. The native
enchanted-book singleton retains its verified source identity although the target
removed EnchantedBookItem; source subclasses of that removed class remain
unadapted, not inferred from the presence of stored enchantments. Source
spawn-egg construction/type overrides are separate, unproven operations.
