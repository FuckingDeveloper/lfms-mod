# Nested source item rendering and block-item names

Runtime34 crashes in an actual transparent source block entity when nonempty
contents call Minecraft.getItemRenderer -> ItemRenderer.renderStatic. This is
separate from the completed empty-item GUI path. Source no-world renderStatic
delegates with null owner/level, explicit light/overlay/seed and false handedness.
Target ItemModelResolver.updateForTopItem extracts those inputs into an owned
ItemStackRenderState; its submit operation accepts the later collector.

The adapter MUST preserve source pose, context, light, overlay, seed and owner/level
for proven overloads. It MUST isolate the input stack, extract once at the source
call boundary, and retain only extracted target state and immutable pose/light
inputs. Nested state MUST submit through the native collector along with recorded
source geometry; it MUST NOT draw immediately or silently omit contained items.
Independent source handedness that disagrees with the target context remains
unsupported. Explicit source BakedModel rendering is not covered by renderStatic.
Only verified source singleton-getter/call dataflow is admitted, including local aliases and repeated calls with exclusively proven consumers; unproven field-stored
renderer receivers and method-reference forms remain unresolved. Runtime35 exposed the local-alias source form during TubeRenderer initialization; source-value provenance now crosses local copies, and a complete consumer audit prevents changing a renderer that escapes to another operation.

Source BlockItem.getDescriptionId delegates to its actual block. The target uses
construction-time item-name intent. Every source BlockItem construction, including
ordinary deferred-registration subclasses, MUST preserve that block description
key through the existing withBlockItemDescription bridge. The rule MUST NOT depend
on registry-name accessors or integration-artifact identity. Target-native callers
and original language resources MUST remain unchanged.

Runtime36 confirms actual translated Iron Chest/Trapped Crystal Chest item names
and loads the existing nonempty transparent chest without the renderStatic crash.
Its inventory synchronization then exposed the source LevelRenderer.blockChanged
member, omitted from the existing render-invalidation family. Source bytecode
ignores BlockGetter and both states, forwarding only position and (flags & 8)
to the padded section-dirty operation. Target LevelExtractor.blockChanged forwards
the same position/flags with the identical bit-8 predicate and padding. The
existing exact-owner/inheritance/member/descriptor family now admits this member,
including its method-reference forms. No packet delivery/error is swallowed.


Runtime37 acceptance: the original filled trapped crystal chest loads and renders
its copper ore, verified visually after structured checks. MCP opens the 108-slot
menu, takes and restores the ore, closes the screen, and confirms source Items is
again exactly slot0/copper_ore/Count1. No new error is logged after synchronization;
only the pre-existing global-loot-list error remains. Client inventory reports
Iron Chest and Trapped Crystal Chest instead of item.ironchest keys. Native
compatibility suites pass. Source items in the player's hand still use the
previously diagnosed missing-model path; this change does not claim to fix it.
Overall evidence remains level6 with observed level7 subpaths.
