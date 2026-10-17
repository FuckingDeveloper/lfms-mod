# Registered source damage factories with explicit world provenance

Source static damage factories did not need a registry; native registered damage
sources do. Proven factory operations MAY use the nonnull source actor's level.
For nullable explosion factories, a direct invocation MAY additionally capture
the unique Level/ServerLevel/ClientLevel formal argument of its enclosing source
method at entry. Multiple level arguments MUST NOT be guessed between. The
captured argument MUST occupy a fresh local, preserving it against source local
reuse. No global current-world lookup or fabricated direct holder is permitted.
Actor and contextual level damage registries MUST agree when both are present.
With no proven world, an ownerless factory MUST fail explicitly.

Admitted bounded family: explosion(LivingEntity), explosion(Explosion),
mobAttack(LivingEntity), playerAttack(Player), thorns(Entity). Exact source
owner/member/descriptor mapping proves selection; similar signatures do not.
Standalone handles keep their original signature and actor/Explosion-derived
world only; they do not capture unrelated invocation locals. Unknown factories
remain unchanged. Input artifacts and original argument evaluation order remain
unchanged; context capture reads only formal arguments.

Explosion attribution MUST preserve source source-mob semantics: the actor is
both direct and causing entity; the Explosion overload resolves its indirect
living source, ignoring a separate direct explosive entity as source 1.19.2 did.
Use native registered DamageTypes and world tags. Pinned vanilla explosion and
player_explosion definitions retain source message IDs, exhaustion 0.1 and ALWAYS
scaling; actor attack/thorns factories retain their native target definitions.
This does not admit arbitrary EntityDamageSource constructors/custom flags.

Evidence: source 1.19.2 factory bytecode; exact target DamageSources/Explosion
sources and damage-type JSON. Runtime100 reaches original detonation then fails
at DamageSource.explosion(LivingEntity) in a method with one ServerLevel formal.

## Virtual damage delivery

Exact source Entity-hierarchy hurt(DamageSource,float) virtual invocations and
callable handles MAY dispatch the original managed public m_6469_ override if
present, preserving its operands, return value and runtime failure. Otherwise
the actual native hurtOrSimulate selects server hurtServer/client hurtClient
using the victim's own level. Source super calls remain unadapted because source
base-body behavior differs. ClassValue ownership MUST avoid retaining disposable
loaders; a coincidental method in an unrelated native mod MUST NOT count as a
source override. This does not prove all source entity override/super families.

## Source entity movement synchronization flags

Runtime101 passed factory creation and native damage delivery, then failed on
Entity.hurtMarked after original knockback. Proven source Entity.hurtMarked and
hasImpulse field reads/writes MAY project to native syncVelocity and needsSync,
respectively. Both source and native trackers consume/reset the former for a
velocity broadcast to tracking players and self; the latter requests regular
movement/metadata processing. Source field hiding MUST be respected and callable
field shapes preserved through the existing field adapter. Native tracker timing
owns consumption; these MUST NOT become detached LMS boolean storage.
