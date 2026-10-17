# Original entity callbacks over native frame states

For verified source renderers whose nearest vanilla superclass is EntityRenderer,
LMS MUST preserve construction, erased/typed render and texture callback dispatch.
Only superclass construction changes to the target integration base; ordinary
EntityRenderer references remain native-compatible. Original managed parent and
explicit super calls MUST preserve their dispatch; callable forms retain source
receiver/method types. Unsupported changed callback overrides MUST fail explicitly.

Native base extraction supplies positions, lighting, visibility and name metadata.
The source render callback MUST execute once during extraction, with actual native
partial tick, interpolated source yaw, local pose and recording buffers. Submission
MUST consume completed model/geometry snapshots in the same frame; it MUST NOT
retain a live entity or managed callback. Nested/failing extraction MUST restore
its context. Source root-super render requests preserve source pose/light and the
native name-display operation; omitting super MUST NOT fabricate a name draw.

For source renderSingleBlock and renderWhiteSolidBlock, the active native entity
family MUST determine the accepted native renderer context. Preserve block state,
source model resolution, transforms, light, overlay/white flash and native outlines.
Use fresh native BlockModelRenderState values during extraction, not live block
entities or model resolution in a later draw. Retained model/pose state MUST be
owned by that frame. Source BakedModel lookup/tessellation, custom model internals,
unknown native context families and geometry-bound measurement for recorded block
models remain explicit boundaries, not made-up empty/box models.

Initial contexts are verified native PrimedTnt/TntRenderer and
AbstractMinecart/AbstractMinecartRenderer roots. No mod IDs or fixture class names
are selectors. Choosing this native context was approved by the user in this turn.
Native living/model renderer families remain separate existing contracts.

Verification MUST distinguish metadata/dispatch and native recording/submission
checks from actual client rendering. Runtime109+ should retry block ignition,
read real server block/entity changes and recover owned runtime108 fixtures and
inventory; visual confirmation is required for pixels the structured API cannot expose.
Optional MCP framebuffer captures may supply that evidence; request a human check
when those captures cannot establish the result.
