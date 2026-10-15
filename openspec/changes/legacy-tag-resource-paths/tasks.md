# Tasks

- [x] Reproduce intact-tree leaf loss and inspect native block tags through MCP.
- [x] Trace source procedural tag publication and exact native leaf-distance predicate.
- [x] Specify source-only tag-folder projection with native-entry precedence.
- [x] Adapt immutable artifact packs and transformed procedural source packs.
- [x] Verify five directory families, nested paths, byte identity, direct/list agreement,
  native precedence, missing resources, boundary negatives, client exclusion,
  reload data and single source ownership in native smoke.
- [x] Run ./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava.
- [x] Verify actual source wood support-tag membership and stable new tree through MCP after explicit reload.
- [ ] Verify world re-entry and retained leaves.

Checks passed on 2026-10-15. Runtime acceptance is pending; no complete worldgen,
common-tag alias, recipe or complete-mod functionality claim follows from these checks.

Runtime: the first native load still misses source procedural definitions due to
whole-callback component deferral. Explicit MCP reload then restored source wood
membership in minecraft:logs and prevents_nearby_leaf_decay. Ordinary bone meal
grew a fresh tree with 5 wood and 50 leaves; an adjacent leaf had distance=1,
persistent=false. No leaf-persistence override is used. Stable-count observation
is recorded below. See design.md for the rejected setup-order experiment.

Final retained code passed ./gradlew compatibilityTest nativeCompatibilitySmoke
compileLaunchJava again after removing that experiment. The remaining startup
lifecycle gap is not fixed by compilation or by a successful explicit reload.

Final MCP observation: all 5 logs and 50 nonpersistent leaves remained after
1200 native sprint ticks. `time query gametime` advanced from 210953 to 212164;
`tick query` confirmed normal 20 TPS afterwards. This strengthens the earlier
75-second wall-clock observation, which alone could be affected by focus pauses.
The first-load and fresh-process tree-persistence gaps remain open.
