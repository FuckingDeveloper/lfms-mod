# Gameplay family backlog — Forge 1.19.2 to target 26.3

## Current P1 snapshot — 2026-10-16

This snapshot supersedes historical open/closed statements below for these items.
Overall compatibility remains world-load with selected functional paths verified;
there is no complete-mod functional-support claim.

| Item | Status and first causal boundary |
| --- | --- |
| Old saved cables | CLOSED/retired by explicit user decision; no repair or reconstruction requested |
| Global loot modifiers | Observed path FIXED by a general source-list/codec/target-order family; runtime48 executes original hemp drop and shears exclusion; source-global-loot-order records scope |
| First-load procedural tags / fresh-process leaf persistence | FIXED for the observed source path by the approved bounded two-phase initial data transaction. Runtime49 cold entry has log/decay tags before manual reload; a fresh tree retains 5 logs/44 leaves over 1200 ticks and save/re-entry. Setup runs once; ordinary reload and generator GUI pass. See initial-data-load-transaction/tasks.md |
| Transparent chest initial display | OPEN source-behavior limitation. Source load directly fills inventory without marking display dirty; no general missing Forge callback identified. User explicitly rejected mod-specific extension; none implemented |
| Null-player creative-search tooltip | OPEN source-behavior limitation. Original 1.19.2 Minecraft search supplies null, Forge forwards it, original handler dereferences it. No player substitution/listener suppression implemented |

Fresh cable placement/save/re-entry and model connection tests from runtime44–46
are separate from the retired historical cables. Remaining P2 rendering/armor and
broader configuration/recipe gaps are not closed by the current P1 batch.

## Evidence and acceptance

Highest observed overall milestone: **6, world load**. The user's 2026-10-14
test confirmed coffee effects and armor equipment. Neither observation proves
overall milestone 7 or complete food/armor compatibility.

The user's later explicit MCP runtime-debugging request authorizes automated
gameplay checks through the existing MCP Fabric tools. Use structured server state
to confirm client predictions. Keep the human check/report workflow for operations
MCP cannot perform, with exact actions and an exact log collection command.

Latest user acceptance (11:33, 2026-10-14): generator opening crashes and cable
connections regress. The first generator cause is eager subscriber discovery
linking an unrelated removed ItemRenderer utility signature; the subsequent
missing-button NPE follows aborted initialization. Selective subscriber linkage
is now fixture-verified. Source block NBT/update callbacks also had a verified
native lifecycle gap; restoring them does not yet prove the reported wire visuals
or energy transport work. Both gameplay acceptance items remain open.

16:01 acceptance: the user confirms visual wire connections. Generator opening
passes selective subscriber linkage, then fails at inherited getGuiLeft during
FilterComponent initialization. The later missing-button NPE is consequential.
All five screen state getters and protected dimension writes are now fixture-verified.
Actual generator interaction, wire persistence and energy transport remain open.
Preflight also closes the four old Button superclass constructor sites and
source no-argument activation via native input callbacks, including the verified
ExtendedButton inherited default. Whole GUI rendering/input remains a separate gap.

16:53 acceptance: generator initialization now passes those getters/constructors,
then fails reading inherited widget active through the original Forge ancestor.
Source loader declaration evidence and reverse namespace lookup now cover that
ancestry generically. Eight widget state fields and field handles pass native
fixtures, including hidden fields and rebinding. Generator acceptance remains open.

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
- [x] User confirms jumping no longer crashes in the 2026-10-14 09:42 test.
- [ ] User verifies furnace placement/ticking and generator open/slots/close/reopen
  after these changes; no interactive client was launched by the agent.

## Remaining compatibility families

| Family | Current evidence | Required work / acceptance |
| --- | --- | --- |
| Fall event | `crash-2026-10-14_02.37.27-client.txt`: source `getDistance(): float` links against target `getDistance(): double`; handler executes on client as the target event does | Distance getter no longer blocks: latest crash reaches source ItemStack.m_41622_ damage callback. Distance and durability family checks pass; the user now confirms jumping no longer crashes. Broader fall variants remain unverified. Source float-distance overrides remain fail-fast |
| Custom menu lifecycle and screens | Gameplay channel provenance is restored; `crash-2026-10-14_09.42.56-server.txt` fails constructing a source slot because its own field was incorrectly redirected to native private `Slot.slot` | Source field declaration/hiding resolution now corrected generically; actual target Slot fixtures and mutable x/y accessors pass. Opening/slots/close/reopen and source screen rendering still require acceptance |
| Event subscriber linkage/lifetime | The 11:33 generator run aborts init while reflection eagerly links an unrelated ItemRenderer method signature | Exact defined-byte indexes link applicable handlers selectively; actual FML fixtures preserve inheritance, cancellation, duplicate prevention, unregister/reopen and rollback. Generator gameplay acceptance remains pending |
| Food/drink completion | Coffee effect succeeded; canned food consumption and returned can now confirmed by user | Verify hunger, effects, returned container/count and audio after sound-family migration |
| Block neighbour callback delivery | Source container-block override takes the changed block's `BlockPos`; target callback takes nullable `Orientation` | Source-position delivery now survives queued/multi/instant dispatch; actual FML/mixin scope probes pass. Verify visual wire updates after placement/removal and keep native unknown-origin calls fail-fast |
| Block capabilities | Missing query ABI no longer appears; `crash-2026-10-14_02.33.15-server.txt` now fails explicitly on a custom token during scheduled `onLoaded` of a mechanism, after user broke a furnace | Eager block attachment gathering, ordered queries, genuine absence, invalidation and ForgeCaps save/load now pass unit and actual FML/mixin probes; gameplay verification pending. Native client lookup, item/entity attachment, native energy direction flags/fluid operations, nested transactions and direct slot replacement remain gaps |
| Cable functionality | The 16:01 user run confirms visual wire connections after model invalidation and source data lifecycle adaptation; energy transport unverified | Visual connections are observed. Verify placement/removal, corners and world re-entry separately from energy transport |
| Block data persistence/initial synchronization | Native target ValueInput/ValueOutput and lookup-bearing update callbacks do not enter old CompoundTag overrides | Adapt the complete load/save/update/handle family, inherited defaults, explicit super order, ForgeData identity and once-only ForgeCaps persistence. Fixtures use real FML storage operations. Verify fresh cable connections and world re-entry; erased historical values cannot be reconstructed |
| Armor behavior | User confirmed equipment, reported absent worn textures | Adapt worn equipment assets/models, attributes, durability and repair using source contracts; verify actual protection and per-stack/custom overrides |
| Item energy | Prior user report: chainsaw charge does not decrease | Source damage callbacks and mutable tag copy/resource ownership now adapted; native comparison snapshots detect tag changes. Verify gameplay callback delivery, charge delta and persistence separately |
| Independent dependency-mod lifecycle | CarbonConfig is admitted but its entrypoint now has 6 remaining source API boundaries (previously 14); its classes are also used as a managed dependency | Adapt loader extension/metadata/ordered-container/reflection and client registration boundaries; prevent duplicate independent/dependency initialization before claiming this mod loaded |
| Startup performance | Latest transformation timing: 1089 reference classes, 84090 ms cumulative transformation; indexed lookup checks pass | Measure the next run, separating build/target artifact regeneration, source transformation, registration and resource loading |
| World-entry resource replay | Source setup is deferred until target dynamic data/component lifecycle; LMS requests an ordinary client-resource reload afterwards | Preserve listener state, registry context and reload ordering; any reduction in reload scope requires evidence that all dependent state remains correct |

Original legacy JARs and all pre-existing uncommitted edits remain immutable
inputs/user-owned work. Named reference artifacts in this backlog are test
evidence, never production transformation selectors.

Latest batch implementation and verification details:
`openspec/changes/batch-runtime-semantics/tasks.md`.

Menu/model batch evidence and verification: `openspec/changes/restore-menu-and-model-updates/tasks.md`.

Subscriber/data follow-up evidence: `openspec/changes/selective-subscriber-linkage/tasks.md`
and `openspec/changes/restore-subscriber-and-block-data/tasks.md`.


17:35 follow-up: the latest gameplay run crashed in water-driven block loot
generation on missing Tags.Items.SHEARS; earlier deferred setup failed on
Tags.Biomes.IS_WATER. Source metadata had shadowed target ABI evidence and
disabled existing aliases. Separately, synchronous reload callbacks lacked the
target lookupWithUpdatedTags provider, causing early-stack fire-resistance
initializers to fail on minecraft:is_fire. Details and verification:
`openspec/changes/separate-source-and-target-evidence/tasks.md`.
The forge:global_loot_modifiers list JSON format remains a resource-migration
gap. No new generator gameplay success or energy transport/use is inferred.


## Family checkpoint — widget extraction and recipe execution

- [x] Project source custom-widget content callbacks without overriding final target
  extractors; preserve inherited receiver depth and managed hidden fields.
- [x] Preserve source widget sheet pixels in an isolated pack and source nine-slice
  border geometry, centered labels, scoped texture/color state and callable handles.
- [x] Forward source milk opt-in through native registration and read pending native
  fluid textures during the actual baking-result callback rather than uninitialized
  global model state.
- [x] Replace whole-dataset hashing in reverse symbol lookups with dataset-owned
  immutable method/field indexes.
- [x] Prove absent source capability initializers across managed item/interface
  bytecode for ordinary stack merging; custom providers remain fail-fast.
- [x] Automated checks: compatibilityTest, nativeCompatibilitySmoke, compileLaunchJava.
- [x] Runtime subpaths: generator GUI open/close/reopen and fuel insertion/consumption;
  existing cable route delivers energy to macerator; gold ore yields two gold dust;
  deepslate iron ore yields two iron dust and shift-transfer merges into an existing
  inventory stack, confirmed on the server.

These are observed level-7 subpaths, not an overall level-7/8 completion claim.
Overall baseline remains level 6 with partial gameplay. Arbitrary GUI buttons,
hover/input routes, worn armor and chainsaw charge remain incompletely verified.
MCP's direct open_container cannot open this packet-driven source menu; actual
empty-hand use_item can. MCP provides no arbitrary widget activation tool.

The original sapling AbstractMethodError is removed, but actual growth reaches a
new first causal worldgen boundary: source FeatureConfiguration/ConfiguredFeature
are removed and target Feature is now an interface with configuration carried by
specific feature implementations. Tree/trunk/foliage callback contracts require a
family-level design; namespace substitution cannot implement this migration.

Copper ore rejection is independent of successful gold/iron recipes. The source
uses its own TagKey factory for forge:ores/copper; native c:ores/copper contains the
item, while forge:ores/copper is absent. Existing static Forge Tags field projection
does not cover factory-created source identities or old tag resource directories.
A source tag-resource/common-default projection must preserve source contributions,
pack precedence, replace behavior and reload identity together; blanket namespace
replacement is insufficient. Copper acceptance remains open.

CarbonConfig lifecycle and the old global-loot-modifier list resource boundary
remain open. World re-entry preservation of existing machine inventories/energy
has not passed controlled acceptance and remains a separate requirement.

Changes are specified in widget-render-extraction, native-loader-feature-intents,
scoped-fluid-model-information, instance-owned-symbol-indexes and
proven-default-stack-capabilities. Per-run logs, JVM exception traces, worlds and
screenshots remain local and are not committed.

Follow-up: source item-backed widgets exposed the removed Minecraft ItemRenderer
getter. A complete, identity-independent immediate GUI helper body and direct
receiver flow now project to the native GUI item extractor with null level/owner,
seed zero and the caller's planar/fractional pose. Full instruction evidence and
changed-light/cache-rebinding negatives pass native smoke tests. The client/world
and macerator main screen load after this fix. Arbitrary widget activation remains
human acceptance, not a completed automated check. See proven-gui-item-operations.

Filter follow-up: the reported screen crash identified source setupFor3DItems.
The inspected callback also consumes the inherited screen ItemRenderer getter,
ordinary GUI items/decorations and RGB-only hover highlights. These now project
as one scoped GUI consumer family. Tests transform the actual immutable external
callback and require all inspected lighting/item/mask/depth sites to project;
independent fixtures verify native mask pipelines and reject custom getter
virtual overrides. Compilation, compatibilityTest, nativeCompatibilitySmoke and
compileLaunchJava pass. Filter activation and hover remain human acceptance because
MCP does not expose arbitrary GUI widget activation; no full-filter claim is made.
See scoped-gui-item-consumers. Nonzero/3D source drawing remains a diagnosed gap.

Planar-depth follow-up: selected-filter foreground drawing uses an ordinary XY
quad translated to source Z=100. The previous guard intentionally rejected it;
planar-gui-depth-layers now explicitly extends that boundary. Shared ordered-plane
projection covers source quads, text, blits and item helper poses. Tests submit
Z=0/100/100 geometry through actual native GuiGraphicsExtractor/GuiRenderState,
verify exactly one new native stratum and unchanged XY, and reject descending,
mixed, perspective/nonfinite/out-of-plane inputs. Previous generic item consumers
also now retain the source ordinary draw's enabled blend/depth state. Full checks
pass; filter selection/hover still requires human acceptance. This supersedes the
blanket nonzero-depth limitation above, not the remaining arbitrary 3D limitation.


Comparator follow-up: crash-2026-10-15_01.35.20-client identified ordinary uniform
PoseStack scaling of flat text. The shared planar validator incorrectly required
m22=1. Flat text/quads/blits now accept independent finite Z scale, while explicit
blit depth uses m22*z+m32. The 3D item helper retains the previous Z-scale boundary.
Immutable GUI-family bytecode confirms comparator 0.5 scaling and reciprocal
restoration; inspected icon/item scaling siblings retain Z=1. Native checks cover
half-scale geometry, restored XY, effective local depths, equal native strata,
multiple finite scale factors and continued 3D-item/perspective/tilt rejection.
compatibilityTest, nativeCompatibilitySmoke and compileLaunchJava pass. Comparator
activation and text appearance still require human acceptance through its GUI
button; MCP has no arbitrary widget activation operation. This extends the flat
primitive contract in planar-gui-depth-layers; no full-GUI compatibility claim.

Runtime after the scale fix: Quick Play loaded Новый мир; MCP confirmed the actual
26.3 NeoForge integrated server, source macerator block and legacy inventory.
Empty-hand use opened the macerator main GUI; targeted visual confirmation showed
its slots, title, widgets and item tooltip without a new crash. The main GUI was
opened again successfully after closing. Comparator activation remains untested.
The existing forge:global_loot_modifiers reload error is still present.


2026-10-15 02:13:55 follow-up: the latest crash is server-side rubber-sapling
growth, not GUI rendering. Source FeatureConfiguration cannot link. Inspection
also establishes that source FEATURES currently maps to target data registry
FEATURE rather than static FEATURE_TYPE; the corresponding source initialization
branch is not delivered in the required phase. Correcting that key alone is not
a fix: configuration, codecs, custom placers/providers and data-definition
lifecycle must migrate together. Source and target base soil/replacement rules
are observably different. worldgen-feature-family/proposal.md records the
concrete native-versus-source policy decision requested under AGENTS.md section
22. No runtime fix or new gameplay acceptance is claimed for this report.

2026-10-15 worldgen follow-up: the accepted native policy now reaches actual
source sapling growth and registered native ore execution. MCP observed a rubber
tree, obstruction preserving a sapling, a second tree after obstruction removal,
stone/deepslate tin replacements and all 7 placed ores surviving client re-entry.
The original FeatureConfiguration crash is removed. This does not prove natural
ore generation in new chunks or complete source worldgen coverage.

Runtime acceptance also reproduced intact-tree leaf loss (7 logs remained, leaves
fell from 64 to 25). Source procedural tag resources use old plural registry
folders. legacy-tag-resource-paths projects five verified tag-directory families
for artifact and procedural packs; native smoke and wrapper checks pass. After
native reload, MCP confirms source wood in logs/prevents_nearby_leaf_decay and a
new tree's nonpersistent leaves have distance=1 next to its trunk.

First-load lifecycle remains OPEN: source procedural tag definitions are produced
by the component-deferred setup callback after the first tag-loading pass. A
selective helper deferral experiment failed because later setup consumes earlier
helper results; the experiment was removed. Preserve source order while designing
that lifecycle boundary. No automatic reload/frozen-tag mutation is implemented.
Independent creative-search tooltip null-player errors, forge global-loot-list
parsing, common tag defaults/copper recipes and CarbonConfig remain open.

Further acceptance supersedes the natural-ore limitation above for the observed
variants: a previously absent region around (10000,10000) generated source tin,
silver and deepslate uranium naturally. The post-reload fresh tree retained all
5 logs and 50 leaves over 1200 native sprint ticks; tick frequency returned to
20 TPS. First-load procedural-tag timing and other backlog items remain open.

Natural rubber-tree generation is now observed as well: an initially absent
region in a newly visited swamp contained source rubber logs and leaves without
sapling/bonemeal/feature-placement commands. MCP found 45 wood and 450 leaves in
a complete radius-30 scan; all wood and 449 leaves remained after 1200 native
ticks. This is generation acceptance after explicit tag reload, not resolution
of the first-load procedural-tag timing or complete leaf-persistence gap. Exact
evidence and the single boundary-leaf loss are in worldgen-feature-family/tasks.md.


2026-10-15 runtime31/32 verified follow-up:
- MCP actual starvation with source quantum armor reduces health 20 -> 18 without
  the removed DamageSource constructor/category-query crashes.
- At hunger zero an actual filled tin can restores foodLevel 0 -> 2 and saturation
  2; source empty-can stack increases 2 -> 3.
- With armor removed to exclude recharge, an actual survival oak break reduces
  source chainsaw charge 1000 -> 950 EU. Normal shutdown and runtime32 restart
  retain charge 950 in minecraft:custom_data. Other tool callbacks and independent
  charged-copy gameplay remain separate acceptance items.
- User confirms Iron Chests has 21 creative entries and seven working upgrade
  icons. The 14 custom chest icons remain OPEN: runtime32 reached original source
  geometry but failed in the transparent renderer's removed dispatcher-camera
  query. See source-custom-item-rendering for the current general adapter work.
- Later source-native-config-views and related UI-family acceptance supersedes
  the historical CarbonConfig startup/input blockers above for observed normal
  configuration paths; the recorded 32-symbol GUI audit still has explicit gaps.

Runtime34 supersedes the chest GUI blocker above for the observed container path:
MCP opens a native test container with all 14 source chest variants, visual
inspection confirms distinct original material images, and the log has no new
runtime error. The general adapter preserves the source renderer/BER callbacks
and fixes transparent-renderer camera observation plus native GUI identity.
Creative-tab user re-entry and held/world item rendering remain open.
