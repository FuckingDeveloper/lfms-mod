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

## Block-data callback composition

The source block-data pass MUST receive an explicit immutable target callback
layout for load, save, update-tag and update-tag handling. Source member identity,
aliases and source-super/default semantics MUST remain source-owned. Target base
owner, callback names/descriptors and native input/output view descriptors MUST
come from the selected target layout. Missing operations and unsupported ABI
shapes MUST fail explicitly. The live target binding MUST reject a mismatched
layout before source-bootstrap creation. Installed-build convenience constructors
MAY select the explicitly installed layout; they MUST NOT infer another target
or advertise support from descriptor parameterization alone. Runtime storage and
registry-context adaptation require separate native verification.

## Block-data storage contexts

Native storage backend access and native input/output view construction MUST live
in the target adapter. The target-independent context contract MUST preserve
lookup identity, output availability and exact mutable-root binding, retaining
target serialization ops and diagnostics. Unsupported backends and missing
required contexts MUST fail explicitly. The target layout MUST name the native
binding bridge; generated wrappers and source-super views MUST use that owner.

Source callback discovery, nested frame restoration, Forge data/capability
projection and source-super/default dispatch MUST remain source-owned. Native
input boundaries MUST retain root-copy semantics; direct source calls and output
views MUST retain supplied root identity. Context extraction MUST NOT change
callback timing or registry-context precedence. Native target/source binding
facades MAY compose these operations explicitly; a layout/context match alone
MUST NOT imply another supported target or whole-mod compatibility.

## Catalogued method families

A migrated method family MUST use the same immutable evidence-backed rules for
transformation and admission assessment. Rules MUST retain exact source member
identity, invocation shape, target bridge, semantic family and evidence. Source
inheritance MUST be resolved from source declarations/raw managed headers, never
inferred from target inheritance or descriptor coincidence. Unknown/overridden
members and unsupported invocation shapes MUST remain explicit gaps. Evidence
caches MUST retain no loaded source classes and MUST be cleared on graph rebinding.

Native ABI belongs to the installed target binding. Approval of a catalog rule
MUST NOT imply whole-mod functionality; static batch reports MUST distinguish
unresolved profiles, incomplete graphs, API blockers and a surface with no known
blockers. Legacy assessment lists MAY remain for unmigrated families until their
implementation and evidence have been consolidated.
