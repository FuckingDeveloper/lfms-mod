# Source bonemeal callback delivery

Forge 1.19.2 BonemealableBlock defines validity (BlockGetter, position, state,
client-side boolean), success (Level, random, position, state) and execution
(ServerLevel, random, position, state). Target 26.3 changes the validity reader
type and adds BonemealSource to all three callbacks.

For native INTERACTION delivery, managed source declarations MUST receive their
original virtual callback once, preserving the actual world, random, position,
state and return value. The source validity boolean MUST be derived from the
actual Level's side, never from the fertilizer source. Source names/signatures
and inheritance/super calls between managed classes MUST remain intact.

The new MOB fertilizer source has no proven source-generation equivalent and
MUST fail explicitly. A validity reader without an authoritative Level side MUST
fail explicitly. Native inherited defaults, source-super calls to changed native
bonemeal APIs and arbitrary direct legacy calls on native implementations remain
separate unproven boundaries; this change does not fabricate them.

Selectors MUST use verified BonemealableBlock ancestry and exact source callback
identity/descriptors, never fixture identities. No source JAR changes are allowed.
