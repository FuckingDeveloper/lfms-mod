# Native dropped custom item geometry

The pinned 26.3 ItemEntityRenderer calls super.extractRenderState before
ItemClusterRenderState.extractItemGroupRenderState. EntityRenderer sets
lightCoords and outlineColor in that super call. Submission uses that same light
and NO_OVERLAY. Bounds control hover height and flat/volumetric stack spacing.

Wrap only the native item-group extraction call with a nested, exception-safe
scope keyed by exact output, stack and owner identities. Record the original
source callback in local coordinates once, then use actual immutable geometry
for bounds and submission, preserving model GROUND transforms and native entity
placement. This extends the existing GUI recording mechanism; it introduces no
fixture geometry, replacement automation or legacy JAR edits.

ON_SHELF, arbitrary GROUND calls outside native item-entity extraction, outlines,
and source vanilla renderer fallback remain gaps. Deferred hand/head/fixed/NONE
contexts retain their existing submission contract.
