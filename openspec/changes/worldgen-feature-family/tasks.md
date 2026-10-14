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
