# Source block data lifecycle after selective subscriber linkage

Source: Forge 1.19.2. Target: Minecraft 26.3 / NeoForge 26.3.0.58-beta.
The 11:33 user run reports cables losing their connections after world entry.
Source block entities own CompoundTag load/save callbacks and initial network data
through no-argument getUpdateTag and CompoundTag handleUpdateTag. Target storage
invokes ValueInput/ValueOutput callbacks and supplies registry context to update tags.
The old overrides are currently not entered by those native operations. This is a
verified lifecycle gap; its contribution to the reported visuals still needs
user gameplay verification. Selective subscriber linkage is a separate checkpoint.

## Contract

- Source overrides and explicit superclass calls MUST retain their dispatch/order.
  A native wrapper MUST invoke the most-derived source callback exactly once;
  source ancestors MUST NOT each create another native dispatch cycle.
- Native input MUST supply a complete CompoundTag snapshot and actual lookup context,
  preserving unknown keys and NBT value types. Source direct calls MUST retain their
  supplied tag identity. No block/mod identity heuristic may reconstruct custom state.
- With the pinned target TagValueOutput backend, source saves MUST use its actual
  root tag. Source-super output views MUST preserve the native serialization ops,
  reporter and supplied tag identity. Unknown output backends MUST fail explicitly.
- Source base ForgeData MUST keep native receiver ownership and source load identity.
  Source ForgeCaps serialization/deserialization MUST run once at the explicit source
  base call; the existing native hooks MUST not duplicate those operations. Native
  target attachment handling remains in the matching native base operation.
- Initial update-tag creation/handling MUST retain registry context and the source
  networking callbacks/data, independently from persistence callbacks/output.
  The source Forge handleUpdateTag base MUST dispatch source load with the same tag;
  it MUST NOT inherit the target default's additional replacement of data components.
- Proven source-super adaptation is initially limited to the inspected BlockEntity
  base. Changed vanilla subclass data contracts remain explicit unsupported boundaries.
- Managed direct BlockEntity subclasses MUST retain all four inherited source base
  defaults, including when they override only part of the family. A descendant override
  MUST reuse the root native dispatch boundary. Source-super callable references whose
  scoped timing is unproven MUST fail during transformation with an attributable symbol.
- Callback indexes MUST follow class lifetimes. Scoped data contexts MUST restore
  previous thread state on success/failure. Original artifacts remain immutable.

Verify with independent source fixtures and the actual physical-client FML/mixin
probe without original entrypoint initialization, a window or a world. Interactive
generator initialization, cable changes and world re-entry remain the user's test.
