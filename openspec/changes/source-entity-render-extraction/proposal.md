# Source entity rendering and static block model context — accepted native renderer contexts

Runtime108 created/tracked the original primed TNT, then its own EntityRenderer
failed because native EntityRenderer requires createRenderState. Merely creating
an empty state would suppress the original renderer and is forbidden.

The existing approved same-frame world extraction policy and block-entity
recorded-geometry pipeline provide the timing/lifetime model. Extend that model
to source renderers whose verified direct vanilla base is EntityRenderer:
execute the original erased/typed render callbacks during extraction, freeze
geometry/model state, then submit it in the same frame without retaining the
live entity/callback. Preserve explicit source-super name rendering through the
corresponding native name-display stage. Unproven callback shapes remain explicit
boundaries. Do not substitute a vanilla TNT renderer for the source callback.

The original source renderWhiteSolidBlock helper delegates to the general source
BlockRenderDispatcher.renderSingleBlock. Native static block rendering resolves
a BlockModelRenderState using a BlockDisplayContext; 1.19.2 has no such operand.
Native renderers each own a distinct context (for example TntRenderer's public
BLOCK_DISPLAY_CONTEXT). Choosing a native renderer's context would affect modern
model/resource customization and cannot be inferred solely from call descriptors.

## Accepted decision

User chose the context of the corresponding native Minecraft renderer. Do not
introduce a shared synthetic static-block context or select by fixture identity.
The initial proven context projection uses actual entity ancestry: PrimedTnt ->
TntRenderer.BLOCK_DISPLAY_CONTEXT, AbstractMinecart ->
AbstractMinecartRenderer.BLOCK_DISPLAY_CONTEXT. These are exact vanilla API
families in the pinned target; they are not mod/class-name selectors. Unknown
families remain explicit boundaries when they consume static block rendering.
The original source callback still chooses the model, transforms and overlay;
it is not replaced by the native renderer's whole implementation.

The already accepted extraction timing is not being reopened.

## Verification

Independent source renderer constructor/synthetic bridge/typed callback fixtures;
once-only extraction, parent callbacks, failure cleanup and immutable submission;
exact native block-model/context/pose/light/overlay operands; no entity retained
in submitted state; actual source primed TNT ignition/explosion and visual
acceptance (optional MCP framebuffer evidence or human confirmation). Custom living renderers, old model internals and arbitrary immediate
shader states require separate proof. No legacy JAR modification is allowed.

Current client is stopped after runtime108 crash. Owned sky fixtures/tagged stack
and inventory restoration remain recorded in docs/compatibility-api-coverage.md;
resume cleanup once a client can render the primed entity safely.
