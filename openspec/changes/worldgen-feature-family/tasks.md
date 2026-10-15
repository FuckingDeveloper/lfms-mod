# Tasks

- [x] Identify first causal exception and separate it from GUI-plane failures.
- [x] Inspect source construction, registration caller and tree callback family.
- [x] Inspect exact target feature, provider, placer and registry contracts.
- [x] Record lifecycle-key defect and non-equivalent base tree behavior.
- [x] Native policy A accepted for trees and ores.
- [x] Specify selected policy explicitly in architecture and design.
- [x] Adapt supported configured tree/ore construction and placement family.
- [x] Adapt proven tree/ore settings, simple-provider factories and placer callbacks.
- [x] Correct implementation registry identity and stage definitions in native data pack.
- [x] Native smoke: codec roundtrips, source dispatch/setters/context, definition encoding,
  duplicate failure, canonical biome override/reload identity, source resource bytes/native
  precedence, transformed source factory/handle/key/builder and external placer/modifier linkage.
- [ ] Actual client native data loading, growth, ore placement and re-entry acceptance.
- [ ] Run wrapper checks, then client and MCP growth/rejection/reload acceptance.

This task list records planned work, not functional compatibility.


Checkpoint: ./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava
passes after the source-policy implementation. Client attempts exposed and removed
source provider class-to-interface factory linkage and over-broad base-type
relocation. The new tests cover both. Client/MCP acceptance is still pending;
current attempts are not a higher compatibility milestone.

Native ABI inventory now resolves all 113 native call/field sites in the
transformed external worldgen initializer against actual loaded target classes,
including invocation kind and accessibility. The source tree-placement default
retains the exact native TREE_THRESHOLD singleton through a checked getter;
its now-private target field is not accessed directly by transformed code.
The full wrapper checks pass after this correction. Runtime acceptance continues.

## Client acceptance, 2026-10-15

The actual 26.3 / NeoForge 26.3.0.58-beta / Java 25 dev client loaded
Новый мир with MCP Fabric 0.5.1+26.3 attached to its integrated server.
Native data loading published 14 source worldgen definitions. In an initially
empty aerial test area, ordinary held bone meal grew the source rubber sapling
into 7 rubber-wood blocks and 64 source leaves. With a solid block immediately
above another sapling, two uses preserved both the sapling and obstruction;
after removing the obstruction a second tree was observed (5 wood, 42 leaves).
The latter observation does not distinguish a random tick from bone-meal growth.

Native registered `place feature ic2:ore_tin` produced 4 source tin ores in a
stone cube and 3 source deepslate tin ores in a separate deepslate cube. These
commands prove actual registered feature execution and replacement selection;
they alone do not prove biome-driven ore generation in new chunks. All fixture
identifiers/coordinates here are test evidence, not production dispatch rules.

The original sapling linkage crash did not recur. An independent creative-search
tooltip worker exception was observed: source PlayerHandler receives a null player
from the native background tooltip path. This is an open context/lifecycle gap,
not suppressed by this change. The existing global-loot-modifier list error and
CarbonConfig lifecycle block also remain open.

MCP has no leave-world operation; integrated singleplayer does not expose
`save-all flush`. The native pause menu's Save and Quit action was used instead;
logs confirmed saving players/worlds and stopping the integrated server before
restarting the client. Re-entry verification follows below.

Re-entry confirmed all 7 placed ores persisted. It also exposed a functional
blocker: the first intact tree retained all 7 logs but only 25/64 leaves. Both
minecraft:logs and prevents_nearby_leaf_decay queries rejected its wood. Source
procedural pack bytecode supplies tags/blocks/logs, while the native loader queries
tags/block. Stable-tree acceptance therefore remains open until the generic
source tag-resource path projection is verified. See legacy-tag-resource-paths.

Additional native gameplay evidence after explicit tag reload: the region file
r.19.19.mca did not exist before travel to (10000,100,10000). No block placement
commands were issued there. MCP then observed naturally generated source tin and
silver ore near Y=20, plus 45 deepslate uranium ores within radius 25 around
(10000,-35,10000). The mixed ore scan reported 286 matches but was truncated;
no complete population count is claimed. This verifies actual biome-driven
generation in a previously uncreated region under the accepted native policy,
superseding the earlier untested-natural-generation limitation for these ores.

A fresh rubber tree retained 5 logs/50 leaves over 1200 native sprint ticks after
explicit reload; the adjacent leaf retained distance=1/persistent=false. First-load
procedural tag timing and fresh-process tree-persistence acceptance remain open.
Overall milestone remains 6 with these observed level-7 gameplay subpaths.

## Natural rubber-tree acceptance, 2026-10-15

With the same MCP-connected integrated server after the previously recorded
explicit reload, `execute positioned 20000 90 20000 run locate biome
minecraft:swamp` located (19808,90,20736). Region r.38.40.mca did not exist before
travel. No sapling, bone meal, block placement or feature-placement command was
used at this location. A radius-30 scan around (19808,70,20736) found 45 source
rubber-wood blocks and 450 source leaves (495 matches; full scan, no truncation).
An example trunk is (19796,76,20732); its adjacent source leaf at
(19797,78,20732) had distance=1, persistent=false. This confirms actual natural
rubber-tree generation through the native biome/placement path, rather than
inferring it from sapling growth or registry presence.

A subsequent 1200 native sprint ticks retained all 45 logs and 449/450 leaves.
The one lost leaf was (19815,76,20764), near the scan boundary; its supporting
wood may be outside the sampled sphere, and its original distance was not read.
No complete leaf-retention claim or diagnosis of that single decay is inferred.
The native tick query returned normal 20 TPS, with game time advancing from
213485 to 214918. No new causal exception appeared. Player position/dimension
and orientation were restored after inspection. First-load procedural-tag timing
and fresh-process tree persistence remain open; this observation does not remove
those earlier limitations.
