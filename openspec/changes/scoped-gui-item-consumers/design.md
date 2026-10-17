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
block-atlas binding and enabled-blend/depth result survive in GUI scope. Other source
model/world methods are not inferred equivalent.

Source flat/3D item-lighting setters retain scoped intent. Source ordinary GUI item
rendering selects lighting from usesBlockLight internally; the target GuiItemAtlas
likewise selects ITEMS_3D/ITEMS_FLAT per item. Flat GUI color/texture pipelines have
no lighting input. The adapter MUST NOT mutate GPU-global lighting during extraction.
Arbitrary manual 3D rendering remains unsupported.

Source depth-enable/disable intent remains local to extracted flat GUI drawing;
depth traces outside planar-gui-depth-layers and nonplanar source primitives retain fail-fast boundaries. Target
GUI ordering and separate item-atlas depth remain authoritative. Source color-mask
channels MUST be captured into immutable native quad pipelines; RGB-only highlights
must preserve destination alpha. Masked text/item operations without a proven
adapter MUST fail, rather than silently draw with a full write mask.

## Static field-read accessors

A direct GUI consumer MAY obtain the source Screen.itemRenderer through a static
managed accessor, including javac synthetic accessors, only when its entire body
is ALOAD0 / GETFIELD / ARETURN, its owner is a Screen subtype, its single
argument is that owner or a subtype, and the source field declaration is exact.
This retains the source protected-field receiver relationship. Source field hiding MUST prevent selection. Branches,
handlers, synchronized accessors, different receiver/return types and side effects
MUST remain unsupported. Accessor names and fixture identities MUST NOT select it.

Bypassing that field-read accessor MUST retain INVOKESTATIC class initialization
at the original invocation point, before null-receiver failure. Initializer errors
MUST propagate. Source null reads MUST remain NullPointerException. The result is
only the existing scoped GUI service view for the verified direct GUI consumer;
no old world renderer or mutable Screen field is fabricated. Other consumers of
the original accessor remain unchanged, and virtual getter override checks remain
mandatory. Evidence caches MUST distinguish opcode and descriptor and clear when
the raw managed evidence graph is rebound.
