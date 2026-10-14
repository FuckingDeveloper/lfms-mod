# World-generation feature family

Status: policy A accepted by the user on 2026-10-15 for trees and ores; implementation in progress.

## Problem and evidence

The 2026-10-15 02:13:55 integrated-server crash occurs in source sapling
performBonemeal, before source ConfiguredFeature.place can execute. The JVM cannot
resolve FeatureConfiguration. The source callback removes the sapling, invokes
ConfiguredFeature.place, and restores the sapling only when placement returns
false. A linkage error therefore also interrupts source restoration semantics.

This is a separate boundary from the previously corrected GUI plane scaling.
This report does not establish comparator GUI acceptance.

Scope: Forge 1.19.2 source; Minecraft 26.3 / NeoForge 26.3.0.58-beta / Java 25 target.
The external IC2 Classic artifact is evidence only and remains immutable.

Verified source/target differences:

| Source 1.19.2 | Target 26.3 | Consequence |
| --- | --- | --- |
| Feature<C> implementation + configuration C | Feature interface; concrete implementations carry configuration | Type-name relocation cannot supply placement semantics. |
| ConfiguredFeature(feature, config) | No ConfiguredFeature or FeatureConfiguration class | Construction, stored fields, accessors and place calls need one contract. |
| TreeConfiguration: direct providers, forceDirt/dirtProvider | TreeFeature: holder providers, belowTrunkProvider | forceDirt cannot be discarded or treated as an identical field. |
| TrunkPlacer callback receives LevelSimulatedReader and TreeConfiguration | Callback receives WorldGenLevel and TreeFeature | Preserve virtual dispatch and source configuration identity. |
| Foliage callback receives BiConsumer setter | Callback receives FoliageSetter with set/isSet operations | Preserve setter ownership and leaf tracking. |
| BlockStateProvider abstract class; getState(random,pos) | Interface; getState(level,random,pos) | Context and invocation kind both changed. |
| FEATURES is the static feature implementation registry | FEATURE_TYPE is the static implementation-codec registry; FEATURE contains world data | The current FEATURES -> FEATURE key mapping selects the wrong lifecycle. |

The actual external registration handler compares its event key to source
ForgeRegistries.Keys.FEATURES and invokes its initializer in that branch. Current
mapping prevents that branch from receiving the corresponding static target
registration event. Correcting the key alone exposes the unsupported constructor,
codec and cross-registry family during loading; it does not fix gameplay.

Source base tree rules also changed observably. Source validTreePos accepts
isAirOrLeaves, replaceable plants or water; target uses air or the
REPLACEABLE_BY_TREES tag. Source setDirtAt invokes onTreeGrow, then tests forceDirt
and a dirt predicate before calling dirtProvider. Target placeBelowTrunkBlock
invokes its target onTreeGrow and then belowTrunkProvider.getOptionalState.
These implementations are not proven behaviorally identical.

## Accepted decision

A. Native generation rules with source-owned configuration/callbacks: use the
native tree/ore placement engines; preserve source settings, custom trunk/foliage
callbacks, supplied randomness, world/position and placement result. Explicitly
accept native base block-replacement/soil/leaf-update rules where the source did
not override them. This is the smaller adapter and does not copy vanilla worldgen,
but the native base-rule policy requires explicit acceptance before becoming a
normative compatibility contract.

B. Strict source-generation base rules: preserve the 1.19.2 eligibility, soil,
placement and update behavior as well as source overrides. Any required source
algorithm belongs in an isolated source-generation implementation, not reference-
mod code. Native placement may be reused only where equivalence is established.
This is wider work; no source-Minecraft classloading domain or wholesale vanilla
runtime import is implied or authorized by this proposal.

The user selected A for both trees and ores. The adapter uses native generation
algorithms and native base eligibility/soil/update rules while preserving source
configuration intent and custom callbacks. B is retained above as the rejected
alternative, not an implementation requirement. Native/source identity is not
inferred from shared method names.

## Implementation boundaries common to either choice

1. Source-profile declarations retain feature/configuration associations and
   original virtual callback identities, including callable handles.
2. Typed target-independent operations express configured placement intent and
   named worldgen definitions; target adapters own native types/lifecycle/codecs.
3. Static implementation/placer registration uses the actual matching native
   RegisterEvent. Cross-registry values stage with the existing pending-entry
   guarantees. Late registration fails explicitly.
4. Source global configured/placed definitions become staged data declarations,
   contributed before native datapack registry loading. No frozen registry
   mutation, counterfeit native Registry, client-world cache or runtime global
   mutation is an acceptable shortcut.
5. The same configured operation serves direct sapling placement and registered
   world-generation placement. No IC2 names, hardcoded tree, coordinates or mod
   content IDs select production behavior.
6. Source providers/callbacks retain context, randomness and setters. Unknown
   configurations, custom feature operations and unproven callback variants fail
   explicitly; content is not silently skipped.
7. A new source generation or target generation is not part of this change.

## Failure behavior

The adapter must report unsupported construction/registration before publishing a
partially initialized definition. Placement false remains false; exceptions are
not converted to success/false or swallowed. No empty FeatureConfiguration shim
is to be introduced solely to move the next crash to another call.

## Verification and acceptance

- Verify source exact owners/descriptors/hierarchy and target actual ABI; exercise
  direct calls, overrides, handles and constructor initialization rules.
- Prove static-registry event delivery, cross-registry staging and failure retention.
- Verify definition encoding/loading through real target registry loading,
  including custom placer codecs, references, duplicate identities and reload.
- Verify two independent synthetic source configurations/providers/callbacks;
  negatives must reject unrelated members and unsupported operations.
- For the selected base-rule policy, cover air/leaves/water/replaceable/solid
  positions, normal/forced/custom soil and source onTreeGrow overrides.
- Run ./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava.
- Start ./gradlew runClient -PlmsTestWorld='Новый мир' with a fresh /tmp log.
- Use MCP get_status, block/inventory reads and ordinary use_item to test actual
  bonemeal placement in a dedicated nearby test area. Preserve the user's build.
- Confirm actual generated legacy logs/leaves, obstruction failure and repeated
  placement; confirm source callbacks execute, not a substituted vanilla tree.
- Verify world re-entry and compare direct growth with registered worldgen intent.

## Current observed status

Overall milestone remains 6 with previously observed level-7 gameplay subpaths.
Latest last successful milestone: loaded world and normal machine GUI; latest
first causal blocker: source FeatureConfiguration linkage during sapling growth.
No runtime Java change or new test run is part of this evidence/proposal update.
CarbonConfig, copper tags, loot-list migration and other existing gaps stay open.

Evidence was read from the exact configured target source artifact, the mapped
Forge 1.19.2 source-generation class artifact, immutable fixture bytecode and the
latest crash report. Per-run reports and javap outputs remain local, uncommitted.
