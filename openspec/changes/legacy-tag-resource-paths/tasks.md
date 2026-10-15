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

## Intrinsic namespace follow-up, 2026-10-15

- [x] Inspect immutable procedural PackResources: namespace enumeration comes
  from ModList, while real tag declarations include source loader namespace data.
- [x] Restore intrinsic Forge server-data namespace discovery without changing
  source/native container metadata or manufacturing resources.
- [x] Exercise the actual native MultiPackResourceManager with a procedural
  source that lists real forge tag data but derives namespaces from target mods.
  Native discovery, source bytes, missing lookups and client exclusion pass.
- [x] `./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava`
  passes (`/tmp/lms-creative-namespace-check.log`, local only).
- [x] Runtime 28: after explicit native `/reload`, server command item predicates
  confirm `ic2:dust_coal` belongs to both `forge:dusts/coal` and
  `ic2:coal_and_charcoal`. The earlier missing-tag error no longer occurs.

This closes the reload namespace-discovery defect. The first-load declaration
timing defect and native-common-tag/default identity migration remain open.
