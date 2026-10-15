# Block extension default ancestry

Runtime52 failed on landing on a legacy block: its source child interface's
addLandingEffects conflicted with IBlockExtension inherited through native Block.
The source interface extended IForgeBlock. LMS mapped that parent to its detached
LegacyIForgeBlock facade, losing the original more-specific default relationship.

LegacyIForgeBlock must extend the native IBlockExtension while retaining its
separate, already-adapted old onDestroyedByPlayer signature. This restores
ancestry, not an arbitrary class/member-name winner. A source child override is
more specific than the target base and receives native dispatch exactly once.
Unknown source signatures still require their own adaptation; target ancestry
alone is not proof that other source operations are equivalent.

The Forge 1.19.2 and NeoForge 26.3 sources have identical addLandingEffects and
addRunningEffects descriptors, invocation side/timing and boolean suppression
meaning. Both defaults return false. Verify the pair through actual target
interface invocation and then through landing on the integration block.
Original source JARs remain untouched; no fixture identity is used in production.

Verification: check53 passed compatibilityTest, nativeCompatibilitySmoke and
compileLaunchJava. Runtime53 reloaded the saved crash position and a second
teleport from Y82 landed on the same aluminium cable at (-208,78,15), reaching
Y78.78125/onGround with the server ticking and no repeated default-method error.
