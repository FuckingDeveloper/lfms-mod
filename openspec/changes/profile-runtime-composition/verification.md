# Composition acceptance — 2026-10-16

## Scope and observed milestone

Forge1.19.2 -> Minecraft26.3 / NeoForge26.3.0.58-beta / Java25 remains the only
installed/tested pair. Observed level6 world load and bounded level7 paths remain;
no whole-mod, additional-generation, mixed-generation or universal-target claim.
First remaining broad functional gap is still F07 source fluids. This change
prepares extension boundaries and preserves current compatibility semantics.

## Implemented boundaries

- Shared evidence-based source catalog used by prelaunch and runtime; unknown
  descriptors remain unresolved, ambiguity/duplicate IDs fail explicitly.
- Profile-owned evidence, launch planning and typed session/transformer factories.
  Forge-specific compatibility and executable-spec planners were moved from
  common packages into the source profile.
- Common session observation and explicit native source/target bindings. The
  generic bootstrap validates required implemented contracts before admission;
  source-specific lifecycle/diagnostics and host attachment have their own owner.
- Named source-semantic/projection/target-shape pipeline retains existing order,
  source evidence locks, private rewrite helpers and safe frame recomputation.
  The working copy has176 existing passes, including the preserved uncommitted
  container-screen pass. That user pass is not part of this checkpoint.
- Java-only typed phase-staging queue preserves identity/scope/order and failed
  suffixes. Native registry mutation remains in Target263RegistrationAdapter;
  source value adaptations remain source-owned. ArrayDeque removes accepted
  heads without shifting the remaining entries; no startup speedup is claimed.

## Executed checks

`./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava`
passed after the initial extraction and after final boundaries/tests; final log:
`/tmp/lms-composition-check-4.log`. Independent CompositionContractCheck verifies
selection/ambiguity/unknown loader-version, immutable inputs, fresh transformer
ownership, missing binding/contract rejection before admission, ordered phase
execution, exception identity/short-circuit, owner/key isolation, failed flush
retention/retry, identity name ambiguity and completed-phase rejection.
Existing native regression checks continue to cover actual FML-applied hooks,
registration/callbacks, item handlers, rendering/input, source-side scope and
persistence. Expected deliberate unload/world-event exception probes are not
client runtime failures.

Dependency-complete `compatibilityAudit` passed:3412 classes,1086 grouped findings,
7253 observations. Its report is exactly identical to the pre-change report
(no added/removed rows); unresolved fluid and optional integration gaps remain.
Reports/logs are under /tmp and excluded from Git.

## Fresh runtime85

Launched with `./gradlew runClient -PlmsTestWorld='Новый мир'`.
Log: `/tmp/lms-container-families-runtime-85.log`. Native launch preparation and
world registration used the new selected-profile path. All three legacy
entrypoints initialized and logged the source/target/contract binding.

MCP status/capabilities established a NeoForge26.3 client with an integrated
server and player Dev in the existing world. Actual IC2/Iron Chests inventory
items, names/counts, source equipment and durability were read. Real use-item
interaction opened the existing generator at(-206,78,12), close/reopen passed,
and the existing macerator at(-203,78,15) also opened. Container probes requested
an absent bedrock item and transferred nothing. Generator data retained storage4000,
handler side49/access3843, comparator flags5/slot6 and max2/slot3.

An initial use immediately after a camera change returned Fail; a fresh raycast
confirmed the intended generator and the subsequent interaction succeeded.
Render crosshair/menu updates are asynchronous; no success was inferred from
that initial call. No blocks/items were created, removed or transferred. Inventory
was compared exactly before/after probes. Player position, camera, selected slot,
health/food/XP remained/restored to the actual pre-probe state.

No ERROR/FATAL/Caused-by entries appeared through final runtime85 checks. The
client is left running. GUI visuals and every CarbonConfig widget were not
re-accepted; previous accepted results remain historical. Original legacy JARs
were not modified. All eight original dirty/untracked files were verified against
their captured baseline; only the mechanical transformer pipeline extraction is
applied to that file, with the prior changes retained outside the checkpoint.

## Remaining architectural boundaries

Only one current source/target binding is implemented. Mixed source admission
fails explicitly until dependency/domain ordering is established. Existing
source bridges/native client registration still include direct native calls;
these are further extraction candidates, not target-independent implementations.
The large transformer still owns helper implementations behind the new pass
composition. Future extractions must follow verified operations and target ABI.
