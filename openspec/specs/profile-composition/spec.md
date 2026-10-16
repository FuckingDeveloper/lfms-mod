# Source/Target Composition Specification

## Scope

This contract defines extension boundaries, not additional supported generations.
The tested source/target tuple and current functional gaps remain authoritative.

## Requirements

- A source catalog MUST select using descriptor evidence, MUST reject duplicate
  IDs and ambiguous matches, and MUST NOT select the nearest known version.
- Both prelaunch and runtime MUST consume the same installed source catalog.
- Source mappings/evidence paths and session/transformer construction MUST be
  supplied by the selected source profile. Shared selectors MUST NOT import a
  concrete profile or native Minecraft/NeoForge types.
- Common session observation MUST distinguish linkage, initialization, blocked
  and closed states. Native host attachment MUST remain target integration.
- Target admission MUST reject missing required implemented contracts/bindings
  before creating a managed session. Advertised contracts MUST be bounded to
  actual implemented operations; they MUST NOT imply whole-mod support.
- Transformation passes MUST run in declared order with unique IDs. Source
  semantics MUST precede namespace projection; target-shape passes MUST follow
  it. A pass failure MUST propagate and stop subsequent passes. Existing source
  evidence locks, hierarchy lookups and safe writer MUST remain authoritative.
- The shared registration queue MUST preserve owner/registry/name/value identity,
  ordered pending work, valid phase completion and explicit late rejection.
  It MUST remove entries only after the target accepts each operation, retain a
  failed entry and unattempted suffix, and reject replacement/removal of the in-flight queue head during a flush.
  The native adapter MUST mutate registries only through the active RegisterEvent.

## Current admission boundary

The current distribution installs one Forge1.19.2 source profile and one tested
NeoForge26.3 integration. Mixed-generation runtime admission MUST remain explicit
unsupported until cross-profile dependency/domain ordering has been verified.
Profile enumeration, managed loading or shared contracts alone MUST NOT imply
that multiple source generations can coexist.

Target generations MUST follow the build/upgrade gates in
`openspec/specs/target-runtimes/spec.md`. A contract match MUST NOT widen metadata
ranges or advertise an untested target tuple. Shared contracts SHOULD be extracted
from verified operations as required by the runtime vision, not invented for
unimplemented generations.
