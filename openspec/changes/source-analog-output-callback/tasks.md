# Analog-output acceptance

- [x] Compare exact source3/native4 ABI and original generator callback chain.
- [x] Add native entry before ordinary verified override renaming; the later
  pipeline position failed runtime77 and was corrected using a full original
  immutable-consumer transformation regression.
- [x] Actual native block-state tests: six directions, parent and inherited child,
  virtual/super dispatch, once-only execution, state/level/position identity,
  negative owner/static/collision selection. Isolated unregistered receivers
  avoid constructing blocks after registry freeze; no registry mutation.
- [x] `./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava`
  passes: `/tmp/lms-analog-output-check-4.log`.
- [x] Runtime78 native comparator output: source4000/4000EU ->15,
  source0/4000EU ->0, source2000/4000EU ->7. Partial/empty cases use fresh fixture
  block placement and genuine source load/tick callbacks, not invented output.
- [x] Runtime78 comparator/repeater placements and observer placement/removal
  beside source generator complete without ERROR/FATAL/Caused-by.

One exploratory `/data merge` repeats source load on the existing object and
appends its comparator listeners; it is not used as evidence of normal
load/tick behavior. The exploratory fixture was replaced before acceptance.
Full comparator circuit combinations and directional custom-mod extensions
are not claimed. The exact directionless source callback is retained.

Native hopper probe in runtime77 leaves four coal in its inventory and the
source generator empty. The source-only inventory has no native discoverable
container/resource handler. This is the next automation boundary; source filter
highlighting and GUI insertion do not establish native automation support.

Existing test generator(-206,78,12) and original user changes remain untouched.
The isolated rig(-210,82,12)/(-209,82,12) remains for automation diagnostics;
all its positions were originally air. No player inventory/position changes.

Runtime82 automation acceptance subsequently removes this isolated rig and
confirms all its positions are air; the earlier existing generator remains.
