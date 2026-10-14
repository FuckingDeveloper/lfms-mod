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
