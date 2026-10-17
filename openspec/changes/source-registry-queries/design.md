# Registry read contract

Pinned Forge43.4.0 Registry/MappedRegistry/DefaultedRegistry declarations and
bytecode establish the read family: get and getOptional for identifier/resource
key, getKey, getResourceKey, getId, byId, getDefaultKey, getRandom and containsKey
for both key forms. Actual26.3 Registry/DefaultedRegistry/DefaultedMappedRegistry
sources establish native virtual getValue/getOptional and the remaining reads.

LMS MAY adapt only these verified source owners and declarations. SRG names MUST
resolve through the configured canonical mappings, indexed/cached as strings;
descriptors alone MUST NOT authorize a rewrite. Source virtual query calls,
method references, LDC handles and nested constants MUST use the same operation
and preserve callable receiver types. Wrong opcodes/owners and constructors or
mutation operations MUST NOT be admitted by this family.

Identifier lookup on a defaulted registry MUST retain its actual default value
for unknown IDs. Optional identifier lookup MUST retain true absence, and
containsKey MUST remain false for an unknown key. ResourceKey lookup MUST retain
its independent native behavior, not be reduced to identifier/default lookup.
Numeric IDs, reverse lookup, returned object/holder identity and random source
MUST be preserved. The adapter MUST use native virtual dispatch and MUST NOT
copy/filter the registry, supply arbitrary defaults, mutate it or catch failures.
Managed source registry subclass overrides remain unsupported and MUST fail
explicitly rather than silently invoking a different native read implementation.

DefaultedRegistry is a source class and a target interface. A typed static bridge
MAY absorb that call-shape boundary; callable thunks MUST retain the source
receiver type after projection. Standalone query tests are not proof of full
registration, tags or functional menu rendering. Existing admission catalogs
remain bounded; this runtime query adapter does not claim whole-artifact support.

The existing hash-pinned source hierarchy generator now includes only the five
registry read declarations (Registry, WritableRegistry, MappedRegistry,
DefaultedRegistry, IdMap) in addition to its previous loader/vertex scope. It
retains the same three input hashes. These headers are source evidence only;
they MUST NOT establish target ABI or admit mutations. Source Registry's exact
IdMap inheritance edge proves inherited byId without broadening the class-parent
method walker to arbitrary interface-default resolution.
