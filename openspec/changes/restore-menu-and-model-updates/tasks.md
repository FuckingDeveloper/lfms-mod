# Menu and model update batch — 2026-10-14

- [x] Inspect user's crash/logs, immutable source bytecode and exact pinned target.
- [x] Correct source field ownership instead of widening private Slot.slot.
- [x] Index source declarations and preserve JVM interface/superclass lookup order.
- [x] Preserve verified source mutable slot-coordinate accessors as a pair.
- [x] Adapt all four proven renderer dirty operations and callable references.
- [x] Restore positioned baked-model data refinement before quad collection.
- [x] Regression checks: real Slot subclass, source/native storage separation,
  descriptor/owner negatives, interface ordering, cache rebinding and current nodes.
- [x] Actual FML physical-client probe: applied coordinate access transforms,
  positioned model callback, native padded range loops and detached-receiver failure.
- [x] Final `./gradlew compatibilityTest nativeCompatibilitySmoke compatibilityAudit compileLaunchJava`.
- [x] `git diff --check`.
- [x] Adapt source Minecraft screen transitions to the same client's native Gui;
  verify direct/callable references, receiver/opcode negatives and supplied/null screen forwarding.
- [ ] User acceptance: generator open, slots, close/reopen, placement/removal.
- [ ] User acceptance: insulated tin cables between machines and in horizontal,
  vertical and corner layouts; visual updates when neighbors change.

Final audit: all 2798 classes, 1548 grouped inventory gaps, 11366 sites,
47819 ms transformation. Source dirty-range and client setScreen references no
longer appear as unresolved methods. This is not a functional support count.
CarbonConfig still has 6 lifecycle requirements and separate GUI/input gaps.
Hunger recovery, energy use/transport and worn armor rendering remain open.
Preserve the pre-existing 8-file tracked user diff and untracked screen bridge.
