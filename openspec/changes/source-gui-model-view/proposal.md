# Closed source GUI model-view transactions

Runtime95 reaches the actual workbench preview, then fails on the removed source
RenderSystem.getModelViewStack. Preserve its proven local push/translate/scale,
apply/draw, pop/apply transaction through native GUI extraction pose snapshots.
This is a source-generation rule, with no fixture identity selection or JAR edits.
