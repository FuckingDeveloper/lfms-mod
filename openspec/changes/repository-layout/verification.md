# Layout verification (2026-10-16)

Source Forge1.19.2; target Minecraft26.3 / NeoForge26.3.0.58-beta / Java25.
This is an implementation reorganization under existing architecture contracts.
No new source generation, target generation or compatibility family is claimed.

## Changes

- Relocated 354 existing Java classes: 321 source-profile classes/facades and
  33 explicitly target-owned adapters. Source runtime root bridges now have
  domain ownership rather than a single flat package.
- Updated exact Java/ASM/reflection identities, concatenated owner expressions,
  native integration references and test expectations. Old internal names do
  not remain as duplicate aliases.
- Split build responsibilities into launch, source evidence and verification
  scripts. Commands, task identities and output locations remain unchanged.
- Moved offline evidence generators under tools/evidence/forge1192 and added
  repository navigation. Local runtime/generated data and historical specs
  were retained.
- Two formerly package-private internal entrypoints are public across domain
  packages: chunk-unload source bus and early stack creation with materialized
  defaults. No source ABI or target lifecycle changed.

## Executed evidence

Pre-refactor: `./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava`
passed (`/tmp/lms-layout-baseline.log`). Post-refactor final focused/native/launch
suite passed (`/tmp/lms-layout-check-5.log`). Earlier intermediate runs exposed
stale assertion paths and concatenated owner expressions; they were corrected
before acceptance.

The dependency-complete compatibilityAudit passed: 3412 classes, 1086 grouped
findings and 7253 observations. After exact identity normalization, the report
matches the pre-refactor report row-for-row (no additions/removals). This is a
structural inventory, not proof of fluid or optional integration compatibility.
Generated reports remain in /tmp.

A temporary isolated compilation of the original source snapshot also passed.
ASM comparison of method/field invocation sequences across 810 compiled classes
and 5937 existing methods, normalizing relocated identities and compiler-generated
enum switch field names, found one intentional changed sequence: the target
world extraction now calls an explicit event factory. This comparison is not a
claim of full bytecode equivalence and does not replace gameplay checks.

Runtime83 exposed a package-boundary overload regression: an inaccessible boxed
render-tick constructor made javac select the primitive constructor and unbox
null. The target now uses `LegacyWorldStageEvent.forExtraction`; unknown tick
identity remains fail-fast only when a source consumer asks for it. The existing
world-render native check now verifies construction with unknown tick plus the
original frame arguments. It also retains the positive source-provided tick
case. No tick value is fabricated.

All eight original dirty/untracked files were checked against their original
contents after applying the exact relocation/import edits. They remain outside
the refactor checkpoint; the additional internal early-stack visibility change
is accounted for separately. Original legacy JARs are unchanged.

## Runtime gate

Runtime84 uses native Quick Play for the existing run/saves/Новый мир directory:
`./gradlew runClient -PlmsTestWorld='Новый мир'`.
Runtime acceptance is recorded below after connection/world/content checks.


Runtime84 reached the intended integrated world with player Dev. MCP status,
capabilities, server player list and the original player inventory were read.
The three legacy entrypoints (CarbonConfig, IC2, Iron Chests) initialized. Actual
IC2/Iron Chests items and source armor survived in inventory with names/counts.
The existing generator at (-206,78,12) retained storage4000, handler side49/access3843
and comparator flags5/slot6 plus max2/slot3. Real use-item interaction opened its
container, close/reopen succeeded; the existing macerator also opened. Empty
container probes requested an absent bedrock item and transferred nothing.
Player position/yaw/pitch were restored. No test blocks or items were created.

MCP open_container does not recognize this source generator as a native menu
provider; use_item follows the real source interaction instead. An immediate
probe after a use call can precede the asynchronous menu packet; subsequent
observed container probes passed. These tool limitations are not treated as LMS
failures. Visual correctness of every GUI/render context and CarbonConfig widget
was not re-accepted in this pass; prior acceptance remains historical.

No ERROR/FATAL/Caused-by entries appeared in runtime84 through the final container
checks. The launch log is /tmp/lms-container-families-runtime-84.log. Overall
observed compatibility remains level6 with individually accepted level7 paths;
no full-mod claim or performance improvement is made. The next known broad gap
remains F07 source-fluid contracts, deferred during this cleanup.
