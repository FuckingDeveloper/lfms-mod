- [x] User accepts explicit two-phase initial load.
- [x] Inspect exact WorldLoader, ReloadableServerResources and TagLoader phases.
- [x] Implement bounded native transaction and final-context client replay.
- [x] Compile and focused/native checks.
- [x] Cold runtime tags/leaf persistence without manual reload.
- [x] Ordinary reload, legacy loot and world re-entry regression.

Runtime49 cold-process evidence (before any explicit /reload): bootstrap starts
00:59:14, original IC2 CommonSetup runs once at 00:59:15, final data starts
00:59:23 and finishes preparation at 00:59:24. Integrated world loads normally.
Native command checks accept source rubber wood in minecraft:logs and
minecraft:prevents_nearby_leaf_decay. A nonpersistent leaf directly above it
changes distance 7 -> 1 and survives 1200 sprint ticks (normal 20 TPS restored).
A fresh source rubber sapling grows after held bone meal: 5 wood/44 leaves remain
unchanged after 1200 further native ticks (game time 780721 -> 782142).
The source display/GUI confirmation and saved-tree re-entry check await the user.

Ordinary reload then completes, retaining the wood's decay-prevention tag and
executing source loot: 40 no-tool grass-loot trials yield 4 ic2:hemp_seeds and
3 wheat seeds. Log still contains exactly one bootstrap, one final preparation
and one source CommonSetup. No ERROR/FATAL/Caused-by diagnostics through these
checks. Inventory is exactly equal to the initial structured snapshot; player
position and rotation restored after controlled bone-meal testing.

Actual native WorldLoader is loaded in smoke tests and its single redirect is
verified. Controlled executor tests prove bootstrap commit precedes final work,
only final result is published, scope cleanup and causal failures at all three
boundaries. ./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava
and git diff --check pass. Logs remain outside Git:
/tmp/lms-two-phase-check.log, /tmp/lms-container-families-runtime-49.log.

MCP open_container cannot open these legacy machine MenuProviders. Empty-hand
use through place_block predicts Success for generator/macerator and causes no
runtime exception; this is not a screen/slot acceptance claim. Human GUI check
requested with the exact log filter. Direct place-feature attempts for the tree
at the aerial fixture returned false; actual held-bone-meal growth succeeded.

Temporary aerial fixture (previously entirely air): x -214..-202, y119..134,
z7..19. Contains the fresh tree and a 5x5 soil platform/bonemeal plants, retained
only for save/re-entry acceptance. Temporary loot barrel cleared and removed.

Final runtime49 acceptance: the user saves/exits/reopens Новый мир in the same
client and confirms generator GUI opens/closes/reopens. MCP reads exactly the
same 5 wood/44 leaves after re-entry; the wood remains in the prevention tag.
The log still has one bootstrap/final pair and one original CommonSetup. No
ERROR/FATAL/Caused-by diagnostics. The user confirms actual GUI behavior, beyond
MCP's earlier prediction-only observation.

Aerial fixture fully restored to original air (2704-position scan count0,
not truncated). Seven new plant drops created during removal were removed by
exact observed UUID; no item entities existed there before cleanup and none
remain afterward. Inventory restored before human acceptance; subsequent user
movement/control retained. Client remains running in the user's world.
