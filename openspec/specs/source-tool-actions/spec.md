# Forge 1.19.2 tool actions

LMS MAY represent source ToolAction name-interned identity through native
ItemAbility. Source constant names, the verified `till` token and mutable default
identity sets MUST be retained; native ItemAbilities no longer contains the full
source constant family. Descriptors, overrides and callable references MUST
remain consistent. Native action availability MUST NOT be inferred from linkage.
The target ItemInstance callback MUST dispatch the original ItemStack override
with the same live stack. Stackless target instances MUST fail explicitly until
a source mutation/ownership contract is established.

Source block-state modification queries MUST retain native event dispatch,
cancellation/result, custom block overrides and simulation. A receiver/argument
matched, nested and exception-safe scope MAY select the verified Forge source
base rules at the native Block default boundary, without changing native calls.
The query MUST NOT execute a complete target BlockTransformer operation: that
would replace blocks, damage tools and emit effects before the source caller.

The admitted source defaults are tilling (including the source conditional
root drop), shovel pathing and property-preserving copper scraping/unwaxing.
These are source-generation API rules, not integration-mod identities. Other
actions with no source default return null. Vanilla axe stripping remains an
explicit gap until its removed table has a verified target query adapter;
source custom block overrides remain authoritative. Unknown native source-base
equivalence MUST NOT be replaced with successful-looking null results.

Tool action queries do not establish modern shield, mining or use-component
behavior. Those target consumption contracts remain separate.
