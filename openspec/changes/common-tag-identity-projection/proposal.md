# Verified common-tag identity projection

Status: proposed; user decision pending.

## Evidence

Runtime59 original macerator accepts gold ore through its source GUI Shift path,
but rejects vanilla copper ore. Native commands establish copper membership in
c:ores/copper and absence of forge:ores/copper. Original source recipe constructs
its key via the loader namespace helper instead of reading Forge Tags.Items.
Existing static-field migration selects the native common-tag key, while these
runtime-created keys and source JSON references retain the old identity.

## Proposed common rule

Build a bounded, cached registry-aware table from verified source Forge common-tag
declarations to native NeoForge common-tag declarations. Require the verified
semantic member correspondence and registry kind, not namespace/prefix guessing
or descriptor coincidence. Include only proven meanings; ambiguous/unproven
entries retain their current explicit gap. No reference-mod identities select it.

For those exact admitted common-tag identities, canonicalize source TagKey
construction to the target key, and project source tag resource definitions and
nested tag references through the same table. Item/block/fluid/entity/biome
registries remain distinct. Original resource bytes/JARs remain immutable;
derivations participate in cache identity and normal native data-load lifecycle.
Explicit target-shaped entries within a source pack retain precedence over
projected entries; normal resource-stack replacement/merge semantics remain native.
Do not fabricate membership, mutate frozen tags, rewrite arbitrary forge paths,
or create cyclic bidirectional alias tags. Unknown IDs remain unchanged.

## Contract decision

This is an explicit exception to namespace/tag identity preservation in
legacy-tag-resource-paths/design.md for the verified common-tag table only.
It also makes source dynamically constructed keys observe the same modern
identity already selected for verified static Forge common-tag constants.
Source custom namespaces and unmatched forge identities remain preserved.

Do not implement the contract change until accepted (AGENTS.md sections1/22).
After acceptance, inspect all reference-artifact tag creation/resource uses and
actual source/native definitions; implement the proven family together, test
wrong registries/unknown IDs/explicit native precedence/JSON references, then
verify original copper recipe in a cold dev client without manual reload.
