# Source custom item draws at native submission

Runtime37 mixed-content evidence contains two missing source custom item models,
Iron Chest and Trapped Crystal Chest, alongside correctly drawn ore, machine and
cable. Existing GUI-only model deliberately selects missing geometry for NONE
and hand contexts; the original source callback and textures already work in GUI.

The configured target's SpecialModelRenderer.submit receives the fully transformed
pose and exact packed light/overlay on the native render thread. For source-era
NONE, both first/third-person hands, HEAD and FIXED contexts, native callers do not
request bounds before submission. A late source callback can preserve these
inputs directly, using the existing immediate-to-native geometry recorder/flush.
It MUST run once per native submission, not during extraction with guessed light.

Submitted arguments MUST contain only the source display context and an isolated
item snapshot (registered identifier/count/component-map snapshot preserving its captured prototype and patch), not a mutable input
stack, pose, source renderer or block entity. Source mutable custom data MUST be
copied at capture and for each materialization; each submission reconstructs a
fresh stack and resolves the actual source extension/renderer at that boundary.
Original callbacks, geometry and material selection MUST remain authoritative.
No source mod identity selects the adapter.

GUI keeps its established actual-geometry extraction and exact native GUI light.
GROUND and ON_SHELF remain explicit gaps: ItemEntityRenderer and ShelfRenderer
request bounds before final light/overlay are available. Unproven bounding queries
on a deferred model MUST fail explicitly, not invent bounds or invoke callbacks
twice. Target-only display contexts and unadapted outline requests remain gaps.

Verification must include isolated mutable tags, repeated submission with distinct
light/overlay, full source pose, native parent-pose restoration, bounds rejection,
all admitted context selections and real mixed-container/held-item runtime draws.

## Verification and observed limits

`./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava` passes.
The native regression covers all seven admitted display contexts, extraction with
no callback, once-per-submission calls with different lights/overlays, native pose
and vertex coordinates without applying the pose twice, parent-pose restoration,
component removals/count preservation and mutable source-NBT isolation on repeated
materialization. Unsupported early bounds and outline requests are rejected.

Runtime38 loads the original five-stack scene at (-238,85,36). After normal
inventory synchronization, ore, compressor, cable and both custom chest items
render with original geometry/materials. Taking Iron Chest from source slot4 into
hotbar3 and selecting it verifies first-person right-hand rendering as well. It is
returned to slot4; all five original slot/identifier/count values, original empty
selected hotbar1, position and view orientation are restored. No new runtime error
appears; the old global-loot-list schema error remains. This is level6 with new
observed level7 render subpaths, not complete source-mod support.

A separate synchronization gap is observed: before any inventory change after
this restart, the server has five stored items but the transparent client's
contents are visually empty. The normal inventory change refreshes the displayed
list. This adapter does not claim to fix initial synchronization. Third-person,
offhand and item-frame runtime acceptance remain separate from the seven-context
native regression. Dropped items, shelves and outlines remain explicitly open.
