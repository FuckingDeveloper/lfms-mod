# Scoped initial block-data diagnostics

Source: Forge 1.19.2. Target: Minecraft 26.3 / NeoForge 26.3.0.58-beta.

A disconnected model can result from absent saved block entities, missing initial
network data, stale model data, or incorrect quad collection. Rebuilding sections
alone does not distinguish these causes. Add opt-in observations at the existing
source data and target positioned-model boundaries; do not change their semantics.

- `LMS_TRACE_MODEL_POS=x,y,z` selects one exact block position. Without the
  environment variable no trace is emitted. No integration-mod identity selects it.
- Observe native persistent input, native update-tag output, native update-tag
  input, and the existing positioned model-data snapshot.
- Diagnostics must not query a live block entity from a meshing worker, request a
  refresh, create missing block entities, reconstruct lost NBT, or replace source
  callbacks. Positioned collection must still read its snapshot once and pass the
  same instance to the source refinement callback.
- For model properties, report scalar values and property types; do not invoke
  arbitrary property-value `toString` methods. Native NBT at the selected position
  is diagnostic output only. Runtime logs and world files remain outside Git.

This is logging instrumentation, not a Minecraft automation interface or a new
compatibility abstraction. MCP Fabric and native launcher operations remain the
runtime verification tools. Neither an empty snapshot nor successful world entry
alone establishes an adapter defect or functional compatibility. The initial
chunk packet in both inspected generations enumerates existing block entities;
missing saved entities must be distinguished from dropped network callbacks.
