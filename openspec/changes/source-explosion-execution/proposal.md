# Source explosion execution — accepted policy

Runtime106 reached the original vanilla blast callback, then failed on source
Explosion.BlockInteraction.BREAK. Source Level.explode returns an Explosion;
target Level.explode returns void and uses a different interaction policy enum.
A field rename alone cannot adapt this boundary.

User accepted this policy in the current session: use native Minecraft 26.3 explosion simulation, retaining the
source explicit destruction intent: NONE -> KEEP, BREAK -> DESTROY (no decay),
DESTROY -> DESTROY_WITH_DECAY. Do not reinterpret an explicit source mode through
new TNT/MOB gamerules. Preserve source callbacks, fire/radius/actor/calculator,
actual execution result, once-only native events, and native client delivery.
Do not fabricate a return object or execute a second explosion to obtain one.

Alternative: emulate the complete 1.19.2 explosion solver and loot algorithm.
This is broader compatibility work and diverges from the accepted native base
simulation direction for projectiles. AGENTS.md section 22 requires an explicit
choice of simulation policy before implementation.

Source subclassed/custom Explosion implementations and constructor/manual
explode/finalize lifecycle need independent proof. No blanket compatibility claim.
Verification: independent source-shaped fixtures for modes, operands, dispatch,
return identity and packet/event once-only behavior; isolated runtime throws and
primed TNT, observed block changes and logs. Legacy JARs remain immutable.
