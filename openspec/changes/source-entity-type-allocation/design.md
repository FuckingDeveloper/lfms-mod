# Source raw entity allocation

Runtime107 exposed the original EntityType.create(Level):Entity call in a saved
projectile callback. Source bytecode invokes its factory directly, without spawn
checks, finalization, world insertion or spawn events. Native create(Level,request)
invokes that same two-argument factory when request.ignoreChecks is true. The
request reason is not observed or forwarded on this verified allocation path.

LMS MUST preserve raw allocation, original factory/type/level/result/null and
exception identity, and MUST NOT turn it into spawning. The target MAY use its
explicit ignoreChecks request (TRIGGERED is unobserved here). This is not a rule
for source spawn/finalizeSpawn or a choice to bypass checks on those operations.
Only the exact native EntityType implementation is initially admitted; custom
source/native factory-method subclasses remain an explicit gap. Callable forms
MUST preserve the source method type. CompoundTag loading is a separate storage
contract. Legacy inputs remain immutable; no fixture selectors are permitted.

## Related position operations proven before another runtime

The same callback uses source Entity.moveTo; all four source overloads have been
compared with native snapTo. They MUST preserve coordinates, block bottom-center,
rotation, old position/rotation reset, bounding-box reapplication and motion.
Source overrides, native virtual dispatch and explicit super calls MUST remain
real calls; direct/callable operands are unchanged. This does not adapt relative
teleport, cross-dimension relocation or arbitrary interpolation APIs.
