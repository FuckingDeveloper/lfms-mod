# Read-view contract

- RegistryManager.ACTIVE MUST retain a distinct, stable source manager identity.
  Known standard process-global source registry names MUST yield stable keyed
  views. Unknown names MUST return null as in source; target-only registries MUST
  NOT become source ACTIVE members merely because the target root contains them.
- The admitted standard source registry set and default entry keys MUST be
  derived from the pinned ForgeRegistries.Keys/GameData.init declarations.
  Known but unsupported data-driven/loader-specific registries MUST fail
  explicitly. Source custom registry construction is outside this adapter.
- A view MUST NOT own mutable registry contents or cache a disposable runtime
  lookup. Target adapters MUST resolve the proven native registry identity and
  perform actual native reads. Missing native counterparts MUST fail explicitly.
- Key/value/ID reads MUST preserve source default fallback, optional reverse
  lookup absence and contains semantics. getID(identifier) MUST preserve source's
  direct-name behavior rather than incorrectly resolving aliases.
- Iteration MUST follow native numeric-ID order for the active registry. The
  source iterator's observed null result after exhaustion MUST be retained.
  Source collection accessors MUST remain immutable live views; entries MUST
  not permit setValue. Identity of actual entry values and keys MUST be retained.
- Source concrete ForgeRegistry storage, descriptors, signatures, annotations,
  method handles, invokedynamic and nested constants MUST agree on one projected
  read-view type. The view MUST implement the retained source registry interface
  identity, permitting source-owned interface boundaries.
- Selection MUST require verified source declaration/ancestry and exact member
  descriptors. Source subclasses, construction, mutable/snapshot managers,
  internal fields and unknown methods MUST fail before execution. Object identity
  operations and original read-only Iterable operations may remain executable.
- Existing interface read adapters MUST accept the new view without admitting
  it as a mutation token or weakening RegisterEvent phase checks.

## Evidence

Source Forge 43.4.0 sources: RegistryManager, ForgeRegistry, ForgeRegistries and
GameData. Target: actual patched 26.3.0.58-beta Registry, MappedRegistry,
DefaultedMappedRegistry and registry-root classes. Source ForgeRegistry.getID
uses the configured default for missing values; getResourceKey returns genuine
absence. getID(identifier) reads names directly, without aliases. Its iterator
returns null after exhaustion. These are not inferred from method names.

## Verification plan

Compile and run compatibility/native checks. Synthetic original source bytecode
must cover stored class/interface identity, type/handle projection, all admitted
reads, ordinary/defaulted/absent cases, immutable collections and negative
mutation/snapshot/subclass/unknown calls. Run static audit and a fresh explicit
world. Accept only the former blocker's disappearance and bounded observed
population/reads; full JEI startup remains a separate milestone.

## Implementation checkpoint

The read adapter supports the pinned standard static registry names, source
configured defaults and raw/non-raw lookup differences. Known moved data-driven
and Forge-specific names remain explicit boundaries; no native-world registry is
substituted for a process-global manager. Source ForgeRegistries field producers
and RegisterEvent mutation tokens retain their existing independent adapters.
The new concrete view interoperates with existing read, tag and numeric-wire
operations, but is deliberately not admitted by mutation-token checks.

Checks passed: `./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava
compatibilityAudit --args='legacy-mods /tmp/lms-forge-registry-audit.tsv'`.
Log: /tmp/lms-forge-registry-final-check.log. Audit: 3829 classes, same two prior
unrelated transformation failures, 1147 gap groups/6752 sites (inventory, not
functional coverage). Original runtime consumer acceptance is pending.
