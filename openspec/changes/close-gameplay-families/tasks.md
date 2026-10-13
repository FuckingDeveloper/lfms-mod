# Gameplay family backlog — Forge 1.19.2 to target 26.3

## Evidence and acceptance

Highest observed overall milestone: **6, world load**. The user's 2026-10-14
test confirmed coffee effects and armor equipment. Neither observation proves
overall milestone 7 or complete food/armor compatibility.

Interactive client verification belongs to the user: provide concrete actions
and an exact log collection command, then use their result as runtime evidence.
Do not run an interactive gameplay test in place of that workflow.

## Implemented, awaiting the next user run

- [x] Cover the five source positional/seeded level-sound overloads, inherited
  receivers and direct/indy/condy references. Static external coverage: 22 sites.
- [x] Restore the inspected target server-player menu-counter and synchronizer
  access contract as one family, preserving target implementations.
- [x] Record lifecycle gate rejection in the managed session, display rejected
  sessions with an explicit uninitialized status/reason and exclude them from
  the loaded count. Count initialized managed sessions in title branding.
- [x] Index target member/class candidate queries and cache repeated named
  searches, retaining candidate order, ambiguity and negative evidence.
- [x] Add cumulative transformation timing at target load completion.
- [x] Verify with `./gradlew compatibilityTest compileLaunchJava`, including
  compilation of main sources, actual transformed target access, synthetic ASM
  cases, immutable source artifacts and indexed-evidence comparisons.
- [x] User confirms canned food consumption and returned can; hunger is unverified.
- [x] User screenshot confirms blocked dependency display and initialization reason.
- [x] Fix gameplay network-channel provenance and deferred owner-context restoration.
- [x] Adapt exact sided/unsided block capability queries, source overrides and
  explicit source-super dispatch, including interface method references.
- [x] Adapt native server item handlers with committed/aborted transactions;
  retain native cache invalidation and fail explicitly for unsupported providers.
- [x] Fix criterion bucket-class Optional inference using an explicit Class<?> map.
- [x] Verify with `./gradlew compatibilityTest compileLaunchJava`: source optional
  identity/default/super dispatch, negative owners, actual interface lambda linkage,
  actual payload handler scheduling on another thread, forged equal channel tokens,
  exception/context restoration and target item-handler transaction rollback/commit.
  The isolated transfer fixture bootstraps target data with an empty FML loading
  list and declared stone components; this is not a world reload/invalidation test.
- [x] Diagnose next user run: destruction-adjacent server crash is a deferred
  source custom-capability query, not proven to be a destruction callback failure.
- [x] Adapt fall-event float/double distance family: getter, setter, constructors,
  callable references and nested constants; preserve authoritative event state.
- [x] Verify fall family with `./gradlew compatibilityTest compileLaunchJava`:
  actual transformed getter/setter/constructor/lambda/constructor-reference calls,
  cancellation/multiplier/read precision, nested constants and negative owners.
- [ ] User verifies repeated jumps/short falls with the same equipped footwear.
- [ ] User verifies furnace placement/ticking and generator open/slots/close/reopen
  after these changes; no interactive client was launched by the agent.

## Remaining compatibility families

| Family | Current evidence | Required work / acceptance |
| --- | --- | --- |
| Fall event | `crash-2026-10-14_02.37.27-client.txt`: source `getDistance(): float` links against target `getDistance(): double`; handler executes on client as the target event does | Distance family regression checks pass; user jumps/short-fall verification pending. Source float-distance overrides remain fail-fast |
| Custom menu lifecycle and screens | The source block-use callback executed; latest opening attempt failed in `Forge1192NetworkBridge.requireChannel`: gameplay incorrectly required an initialization scope | Provenance/scheduled-context regression tests pass; verify opening, slot interaction, close/reopen and client/server state; diagnose the next first causal failure |
| Food/drink completion | Coffee effect succeeded; canned food consumption and returned can now confirmed by user | Verify hunger, effects, returned container/count and audio after sound-family migration |
| Block neighbour callback delivery | Source container-block override takes the changed block's `BlockPos`; target callback takes nullable `Orientation` | Preserve the source origin through the target notification/queue lifecycle before adapting this callback; verify wires update after placement/removal |
| Block capabilities | Missing query ABI no longer appears; `crash-2026-10-14_02.33.15-server.txt` now fails explicitly on a custom token during scheduled `onLoaded` of a mechanism, after user broke a furnace | Eager source attachment gathering/dispatcher absence must be adapted; this crash is not fixed by the query bridge. Native client lookup, custom attachment, native energy direction flags/fluid operations, nested transactions and direct slot replacement remain gaps |
| Cable functionality | User confirms tin cables with first insulation still have no visual connections; energy transport unverified | Establish callback delivery, model/connectivity synchronization, network membership and energy transfer separately |
| Armor behavior | User confirmed equipment, reported absent worn textures | Adapt worn equipment assets/models, attributes, durability and repair using source contracts; verify actual protection and per-stack/custom overrides |
| Item energy | Prior user report: chainsaw charge does not decrease | Trace gameplay callback delivery, mutable per-stack state and synchronization; verify charge delta in survival and persistence separately |
| Independent dependency-mod lifecycle | CarbonConfig is admitted but its entrypoint is blocked on 14 source API boundaries; its classes are also used as a managed dependency | Adapt loader extension/metadata/ordered-container/reflection and client registration boundaries; prevent duplicate independent/dependency initialization before claiming this mod loaded |
| Startup performance | Latest transformation timing: 1089 reference classes, 84090 ms cumulative transformation; indexed lookup checks pass | Measure the next run, separating build/target artifact regeneration, source transformation, registration and resource loading |
| World-entry resource replay | Source setup is deferred until target dynamic data/component lifecycle; LMS requests an ordinary client-resource reload afterwards | Preserve listener state, registry context and reload ordering; any reduction in reload scope requires evidence that all dependent state remains correct |

Original legacy JARs and all pre-existing uncommitted edits remain immutable
inputs/user-owned work. Named reference artifacts in this backlog are test
evidence, never production transformation selectors.
