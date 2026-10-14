# Scoped source GUI item consumers and flat highlights

Source GUI ItemRenderer consumers MAY use a scoped extraction view only when
source dataflow proves a direct Minecraft renderer getter, exact inherited Screen
itemRenderer field, or pure managed getter of that field. Getter shape alone MUST
NOT bypass virtual overrides: actual source method resolution is checked against
the proven declaring class before returning the view. Unknown/escaping receivers
and custom getters remain unsupported. No world-renderer class is fabricated.

The source renderGuiItem/renderAndDecorateItem/fake variants preserve source null
level, owner selection and seed through the exact native extraction operation.
Decorations preserve the supplied count override. The source draw's white color,
block-atlas binding and disabled-blend result survive in GUI scope. Other source
model/world methods are not inferred equivalent.

Source flat/3D item-lighting setters retain scoped intent. Source ordinary GUI item
rendering selects lighting from usesBlockLight internally; the target GuiItemAtlas
likewise selects ITEMS_3D/ITEMS_FLAT per item. Flat GUI color/texture pipelines have
no lighting input. The adapter MUST NOT mutate GPU-global lighting during extraction.
Arbitrary manual 3D rendering remains unsupported.

Source depth-enable/disable intent remains local to extracted flat GUI drawing;
nonzero/deep or nonplanar source primitives retain fail-fast boundaries. Target
GUI ordering and separate item-atlas depth remain authoritative. Source color-mask
channels MUST be captured into immutable native quad pipelines; RGB-only highlights
must preserve destination alpha. Masked text/item operations without a proven
adapter MUST fail, rather than silently draw with a full write mask.
