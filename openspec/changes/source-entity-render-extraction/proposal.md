# Source entity rendering and static block model context — decision pending

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

## Proposed decision

Create one shared target-owned static-block context for source renderSingleBlock
operations, independent of legacy mod identity and of a particular vanilla
renderer. Use native model resolution, tint/layer/lighting and special-model
handling in that context; retain source transforms, overlay/white-flash and
explicit callback data. Models that depend on a different native context do not
receive an invented renderer identity.

Alternative: project the old generic operation onto a chosen existing native
renderer context. That can select renderer-specific modern model variants even
when source code called the generic helper. A per-mod/per-block selector is not
an allowed alternative.

AGENTS.md section 22 requires this new context semantic choice before
implementation. The already accepted extraction timing is not being reopened.

## Verification

Independent source renderer constructor/synthetic bridge/typed callback fixtures;
once-only extraction, parent callbacks, failure cleanup and immutable submission;
exact native block-model/context/pose/light/overlay operands; no entity retained
in submitted state; actual source primed TNT ignition/explosion and human visual
acceptance. Custom living renderers, old model internals and arbitrary immediate
shader states require separate proof. No legacy JAR modification is allowed.

Current client is stopped after runtime108 crash. Owned sky fixtures/tagged stack
and inventory restoration remain recorded in docs/compatibility-api-coverage.md;
resume cleanup once a client can render the primed entity safely.
