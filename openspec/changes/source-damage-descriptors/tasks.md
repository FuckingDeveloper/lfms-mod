# Verification

- [x] Inspect source constructor/flag semantics and exact target DamageSource,
  DamageType, LivingEntity, Witch and damage-type stream-codec contracts.
- [x] Adapt String construction without changing NEW or uninitialized-this use.
- [x] Preserve actual source subclass initialization, source message ID and
  descriptor metadata, direct/constructor handles and nested dynamic constants.
- [x] Adapt eleven verified read-only queries; preserve source virtual override
  versus explicit base-call dispatch with class-owned method-handle caching.
- [x] Test every native query with one independent bound tag at a time, against
  the other ten negatives, and preserve direct descriptor default flags.
- [x] Audit all immutable IC2 base-owner boolean damage queries; all five used
  source members are now covered by the eleven-member family.
- [x] Exercise actual immutable source damage-class static initialization.
- [x] Prove native registered damage holders still round-trip through the target
  stream codec and direct descriptors still reject registry-only synchronization.
- [x] `./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava`
  passed (`/tmp/lms-damage-descriptor-check.log`, local only).
- [x] Runtime 31 re-entered the existing world in survival with hunger zero and
  source quantum armor equipped. Actual starvation reduced player health from
  20 to 18 without the earlier constructor or query linkage crashes.

The earlier runtime 30 uncovered the initially unadapted magic predicate.
Source/target Witch behavior and the four vanilla magic paths established the
correct WITCH_RESISTANT_TO projection; native NeoForge IS_MAGIC is not equivalent.

This verifies a level-7 native-damage/source-filter subpath, not networked custom
source damage, source flag setters, arbitrary entity-source construction or
complete armor attributes/rendering. Those limitations remain explicit.
