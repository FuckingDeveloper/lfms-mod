# Tool action identities and source state-query boundary

Runtime39 failed at AdvancedHoe.getHitPositions while submitting a block outline:
ToolActions remained a residual Forge class. Inspection of all seven fixture
users found hoe preview/use queries, chainsaw stripping, copper scraping, custom
block stripping overrides, sword sweep and shield predicates. Native 26.3
ItemAbilities retains only part of the source constants. HOE_TILL is named `till`
in the actual Forge 1.19.2 JAR, not an inferred lowercase field name.

The native block-state extension still posts the tool-use event and invokes the
block override, but its default block extension no longer handles hoe/shovel/axe
transformations. The target BlockTransformer execution also damages items,
replaces blocks and emits sounds/loot/events. It cannot substitute a source
state query, especially simulation used by highlighting.

Preserve the source default rules only in a matched source-call scope at Block's
inherited extension boundary. Native consumers keep the native default. Source
custom overrides and super calls keep their original order. Never suppress the
highlight listener, mutate the original JAR or identify the fixture in dispatch.
