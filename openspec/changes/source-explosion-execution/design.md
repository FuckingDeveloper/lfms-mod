# Native explosion execution with explicit source modes

Source mode values MUST retain their names, order and enum identity in a
source-owned facade. NONE maps to native KEEP, BREAK to DESTROY without decay,
and DESTROY to DESTROY_WITH_DECAY. A direct identity rename of DESTROY is wrong.
Source Level.explode overloads MUST retain evaluated operands and return the real
native explosion instance, including when NeoForge cancels execution.

The target MUST execute its ordinary ServerLevel explosion path exactly once,
retaining native start/detonation events, block processing, player knockback and
client packet delivery. A narrowly scoped constructor hook MAY project the mode
and capture that path's actual result. It MUST claim the construction before
invoking the constructor; nested constructors/events MUST NOT steal the result.
Scopes MUST restore on failure and nesting, and MUST NOT affect unrelated native
explosions. Missing capture MUST fail explicitly, never return a dummy result.

Initial admission is the exact native ServerLevel implementation. Client-side
source explosions, custom source/native Level overrides, constructors/manual
Explosion lifecycle, custom Explosion subclasses and mutable block-list/solver helper APIs remain separate
contracts. Inherited source data initialization and PrimedTnt callback
projection are verified separately; this change does not claim those are complete.

Evidence: pinned source Level/Explosion bytecode and exact patched target
ServerLevel/ServerExplosion. Source DESTROY alone supplies the loot explosion
radius; native DESTROY_WITH_DECAY alone does the same. Native ServerLevel invokes
onExplosionStart after construction and sends its own packet after execution.

The source damage, hit-player map and indirect-owner queries MUST expose real
native state; map identity/mutability is preserved. Damage/map storage requires
a native ServerExplosion. An arbitrary custom implementation MUST fail explicitly.
