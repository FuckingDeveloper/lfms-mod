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
- [ ] User verifies canned food, custom mechanism menus, title/list status and
  startup timing after restarting the client.

## Remaining compatibility families

| Family | Current evidence | Required work / acceptance |
| --- | --- | --- |
| Custom menu lifecycle and screens | The source block-use callback executed; opening failed on private `ServerPlayer.nextContainerCounter()` in `crash-2026-10-14_01.33.02-server.txt` | Verify opening, slot interaction, close/reopen and client/server state; diagnose the next first causal failure |
| Food/drink completion | Coffee effect succeeded; canned food failed on coordinate sound overload in `crash-2026-10-14_01.40.51-server.txt` | Verify hunger, effects, returned container/count and audio after sound-family migration |
| Block neighbour callback delivery | Source container-block override takes the changed block's `BlockPos`; target callback takes nullable `Orientation` | Preserve the source origin through the target notification/queue lifecycle before adapting this callback; verify wires update after placement/removal |
| Cable functionality | User reports disconnected cables; energy transport unverified | Establish callback delivery, model/connectivity synchronization, network membership and energy transfer separately |
| Armor behavior | User confirmed equipment, reported absent worn textures | Adapt worn equipment assets/models, attributes, durability and repair using source contracts; verify actual protection and per-stack/custom overrides |
| Item energy | Prior user report: chainsaw charge does not decrease | Trace gameplay callback delivery, mutable per-stack state and synchronization; verify charge delta in survival and persistence separately |
| Independent dependency-mod lifecycle | CarbonConfig is admitted but its entrypoint is blocked on 14 source API boundaries; its classes are also used as a managed dependency | Adapt loader extension/metadata/ordered-container/reflection and client registration boundaries; prevent duplicate independent/dependency initialization before claiming this mod loaded |
| Startup performance | Previous user run spent about two minutes between discovery and initial resource reload completion; indexed lookup checks now pass | Measure the next run, separating build/target artifact regeneration, source transformation, registration and resource loading |
| World-entry resource replay | Source setup is deferred until target dynamic data/component lifecycle; LMS requests an ordinary client-resource reload afterwards | Preserve listener state, registry context and reload ordering; any reduction in reload scope requires evidence that all dependent state remains correct |

Original legacy JARs and all pre-existing uncommitted edits remain immutable
inputs/user-owned work. Named reference artifacts in this backlog are test
evidence, never production transformation selectors.
