# Restore menu field ownership and model updates

## Evidence and scope

Source: Forge 1.19.2. Target: Minecraft 26.3 / NeoForge 26.3.0.58-beta / Java 25.
Original artifacts remain immutable; reference identities are evidence only.

The user's 2026-10-14 world test confirms repeated jumps no longer crash, canned
food is consumed and its can returned, and cables show improved connectivity.
Hunger recovery, energy transport and complete cable rendering remain unverified.
Generator opening fails on both sides during slot construction. The source slot
subclass declares its own protected `f_40217_:I`; inherited-field fallback wrongly
rewrote accesses into the native private final `Slot.slot`. Source Slot's native
field is also private: widening it would be the wrong fix.

Client packets successfully update cable fields and request model-data refresh,
then fail on source `LevelRenderer.m_109494_(IIIIII)V`. The source and exact target
implement the same inclusive block-range padding and block-to-section conversion;
the operation now belongs to `LevelExtractor`. Three related public operations
also survive there: conditional state-based block invalidation, exact section
invalidation, and section invalidation with neighbor padding.

The source Forge baked-model contract additionally refines the supplied block
model-data snapshot through `getModelData` before collecting quads. The existing
positioned model adapter skipped that callback. Source block-entity data refresh
already delegates to the native model-data manager; no new live entity query or
automatic refresh is added to the meshing path.

Source Slot coordinate accessors identify native x/y fields with exact mixin
target/descriptor and `@Mutable`. Native rendering and hit testing still use those
same integer coordinates. That verified direct accessor subset can be preserved;
arbitrary slot icons, source initialization overrides and GUI extraction are
separate contracts, not automatic name migrations.

The continued menu-opening path also invokes source `Minecraft.setScreen` after
creating the screen. It now belongs to the native `Gui` of that same client. The
source/target implementations retain opening-event cancellation/replacement,
closing/removal and initialization/input transitions. Delegation preserves the
native implementation, including target-specific focus/text-input/layer handling.
This is not an immediate-rendering adapter.

## Implementation contracts

- Honor JVM source field declaration order before vanilla identity migration.
  Source-owned fields retain their names and ownership; real inherited native
  fields still migrate. Cache declaration sets/ancestry and invalidate on graph
  rebinding. Current transformed nodes use current fields and ancestry.
- Adapt all four proven dirty operations and direct/indy/nested-constant uses.
  Delegate to the native extractor bound to the exact active renderer, and reject
  unrelated receivers and unsupported source overrides. Preserve native bounds,
  padding and conditional state comparison; do not force a resource reload.
- Delegate source screen transitions, including null, to the same client's
  native GUI owner. Preserve native event/lifecycle behavior without duplicate
  events or fabricated screens.
- Refine the target-provided positioned model-data snapshot once before quad
  collection, retain virtual source dispatch and reject null refinement results.
- Preserve the verified mutable x/y accessor operations over the same native slot,
  without granting access to the unrelated private native slot index.

## Verification and limits

Synthetic source fixtures verify hiding, inherited/interface resolution, exact
descriptors, cache rebinding and mutated nodes. A transformed fixture constructs
an actual native Slot subclass and proves distinct source/native index storage.
ASM checks cover the four render operations and callable constants, receiver and
invocation-kind negatives, and unsupported overrides. A model fixture verifies
position/state/snapshot identity, callback ordering and null failure.

The physical-client FML probe repeats those fixtures against loaded target classes,
verifies applied mutable-coordinate access transforms, and exercises the native
block/section range loops at negative coordinates and section boundaries. Its
isolated renderer/extractor binding bypasses GPU and game constructors, restores
the singleton afterwards and checks unrelated-receiver rejection. A native GUI
receiver fixture verifies supplied-screen identity and null forwarding; full
opening/closing event sequences still require gameplay acceptance. It does not
initialize original legacy entrypoints, open a window, load a world, create the
actual generator screen or prove energy transport.

Overall observed compatibility milestone remains 6 (world load). The next first
causal gameplay blocker must come from the user's test; offline inventory gaps
are not evidence that a particular missing operation executes first.
