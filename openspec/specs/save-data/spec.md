# Save and Persistent Data Specification

Source block-entity persistence and initial update-tag callbacks MUST be adapted
as lifecycle operations when target descriptors/ownership change. Source inheritance,
explicit superclass calls, complete NBT values, registry context and network update
data MUST be preserved independently. Merely linking an old callback MUST NOT be
reported as functional data compatibility. The bounded Forge 1.19.2/26.3 contract
is specified in `openspec/changes/restore-subscriber-and-block-data/design.md`.

Transparent migration of worlds created by historical Minecraft versions is outside the required scope of 01.000.00.

LMS MUST NOT claim old-world compatibility merely because a legacy mod can run in a newly created target-version world.

Any future save migration MUST be specified separately and MUST address identifiers, block/entity data, capabilities/components and irreversible conversion behavior.

LMS compatibility code MUST avoid silently discarding persistent legacy data when an unsupported conversion is detected.

Source server chunk-unload listeners MUST retain the source generation's position
before final persistence when the target event moved after it. Native event
timing MUST remain unchanged, source notifications MUST not duplicate, and
transaction/nesting/failure state MUST not leak. See
`openspec/changes/source-chunk-unload-save-order/design.md`.
