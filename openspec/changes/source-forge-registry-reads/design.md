# Read-view contract

- RegistryManager.ACTIVE MUST retain a distinct, stable source manager identity.
  Known standard process-global source registry names MUST yield stable keyed
  views. Unknown names MUST return null as in source; target-only registries MUST
  NOT become source ACTIVE members merely because the target root contains them.
- The admitted standard source registry set and default entry keys MUST be
  derived from the pinned ForgeRegistries.Keys/GameData.init declarations.
  Known but unsupported data-driven/loader-specific registries MUST fail
  explicitly. The later source-enchanted-book-reads contract admits only the
  enchantment read view with authoritative per-consumption resolution. Source custom registry construction is outside this adapter.
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

## Callable keys and producer interoperability

Runtime130 passes ACTIVE/view acquisition and reaches the original registry
wrapper's reverse item-name lookup. Its next error is the unbound source
ResourceKey.location method reference, not the old manager field. The full pinned
seven-member ResourceKey family now shares one source traversal for direct,
handle, lambda and nested-constant operations. Only location's native spelling
is target-selected; source SRG identities and descriptors remain profile-owned.
Factory interning, isFor/cast and codec behavior are tested against actual native
keys. The obsolete direct-only location rule was removed.

Source interface reads use the existing semantic invocation traversal to retain
exact lambda capture receiver types. Explicit source concrete casts may normalize
an already-proven native static registry token to its stable source read view;
source instanceof uses that same admission rule. Null and invalid casts retain
normal Java results/failures. This does not admit arbitrary native registries,
source subclasses, moved data-driven registries or mutation. Interface reads
already supported on native tokens retain their existing target operations.
In particular, no broad conversion of ForgeRegistries field producers or
RegisterEvent mutation tokens is performed.

Null ResourceKey lookup preserves source NullPointerException; null Identifier
lookup preserves source absence. Exact target projections of unsupported source
loader keys are recognized by the target adapter and fail rather than appearing
silently absent. Arbitrary target-only registries remain absent from source ACTIVE.

Additional passing checks: /tmp/lms-registry-callables-check.log; static audit
/tmp/lms-registry-callables-audit.tsv (3829 classes, same two prior unrelated
transform failures, 1143 groups/6745 sites). Producer casts, interface-bound lambda
capture, raw-name/tag reads and null/projected-key boundaries have independent
fixtures. Original CarbonConfig NamedRegistry's data-driven registry path remains
unverified and outside the bounded process-global read-view contract.

## Runtime131 and inherited native registry reads

Runtime131 passes both ACTIVE and ResourceKey callable lookup. JEI reaches item
subtype analysis. Its first chronological failure is the removed EnchantedBookItem
class; its original factory logs/skips those entries. A later fluid-helper class
fails verification because an inherited DefaultedRegistry SRG member was resolved
to a concrete MappedRegistry target owner, while the target receiver is an interface.
These failures must not be reported as successful complete ingredient registration.

The registry read traversal now proves the source parent edge before canonical
SRG lookup. Native optional holder lookup (numeric/key) and throwing holder lookup
are normalized to actual target Registry reads. Source direct/inherited/callable
forms retain their original receiver type at lambda capture, genuine optional
absence, holder identity and missing-holder exception semantics. No holders are
created and no registry is mutated.

Compile, compatibility/native checks and full audit pass in
/tmp/lms-registry-holder-check.log; the audit retains the same two unrelated
transform failures. Independent fixtures reproduce the exact inherited reverse
lookup and MappedRegistry method-reference/DefaultedRegistry capture shape from
the fluid helper. Live holder-read acceptance awaits runtime132.

## Runtime132 bounded acceptance

The exact fresh build loads the explicit existing world and local Dev player.
All seven source constructors initialize. The original JEI registry/key path
passes, and the original fluid helper gets past the former verifier failure:
its construction advances from helper line 187 to renderer construction at 188.
This establishes original helper linkage; executing its tag stream in a live GUI
is still unverified. Native independent fixtures exercise the holder operations.

The first remaining chronological error is EnchantedBookItem in JEI's subtype
callback. The first fatal vanilla-plugin setup error is now Matrix4f in fluid
renderer construction. Neither is a registry-read failure. The original factory
logs/skips the failed book entries; full ingredients and JEI startup are NOT
accepted. Missing vanilla crafting category is fallout from failed setup.
Runtime log: /tmp/lms-container-families-runtime-132.log. The dev JVM was stopped
with TERM after MCP/log verification. No gameplay fixture mutation or recovery
is pending. Highest overall observed milestone remains world load (level 6).

Implementation checkpoints pushed: 03b8e85 (ACTIVE read views), ef623e9 (callable
keys, producer casts and interface capture), fc54a18 (inherited holder reads).
Existing user modifications remain outside these commits. Remaining semantic
limits include source mutable/snapshot/custom registries, moved data-driven and
loader-specific views, inactive overrides, and unsupported holder/delegate/codec
members of the concrete source ForgeRegistry. The original CarbonConfig
NamedRegistry data-driven initialization path is outside this bounded contract.
