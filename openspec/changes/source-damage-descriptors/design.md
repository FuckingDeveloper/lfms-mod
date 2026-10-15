# Source damage descriptors and read-only queries

Runtime 29 reproduced starvation with source electric armor. Its source damage
filter initializes custom DamageSource(String) descriptors for identity/name
comparison, and fails before it can inspect the genuine native starvation source.

Source 1.19.2's String constructor defines a real damage descriptor: the supplied
message ID, exhaustion 0.1, no scaling, no flags and no associated entity. LMS MAY
construct an equivalent native DamageSource with an actual direct DamageType
holder. It MUST preserve subclass initialization rules, virtual entity/message
overrides, constructor references and the original string. It MUST NOT register
late damage types or substitute a native generic descriptor for source metadata.

Read-only source boolean queries with exact DamageSource owner/member identity
MAY project to verified native damage tags: projectile, explosion, armor bypass,
helmet damage, invulnerability bypass, effect bypass, enchantment bypass, fire,
fall, no-anger and magic. Source custom query overrides MUST remain virtually dispatched;
explicit source base calls MUST use base behavior. Native type data remains the
authority. Source mutators, entity-source constructors and
arbitrary inherited call owners remain explicit unsupported boundaries.

A direct descriptor is sufficient for source metadata/filter execution. It is
NOT proof of networked custom damage support: the target DamageType stream codec
requires a registry reference. Native damage event synchronization with such
descriptors MUST remain unsupported until an explicit data-registration and
client contract is implemented. This change MUST NOT hide that limitation by
rewriting the damage packet to a generic native type. Native registered damage
sources, including starvation, MUST retain their actual holder and semantics.

## Magic category evidence

The source isMagic predicate selects the 0.15 damage factor in Witch; the target
uses WITCH_RESISTANT_TO for that same operation. Its vanilla definitions contain
exactly magic, indirect_magic, thorns and sonic_boom, the four source factory
paths that call setMagic. NeoForge IS_MAGIC is deliberately NOT used: it adds
dragon breath/wither categories and omits sonic boom, changing source behavior.
Native tag reload remains authoritative for the projected target predicate.
