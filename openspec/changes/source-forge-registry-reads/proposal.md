# Source Forge registry reads

Runtime129 passes creative enumeration, but JEI's original registry wrapper
cannot resolve RegistryManager.ACTIVE. Inspecting its complete bytecode reveals
stored ForgeRegistry values, iteration, key/value/ID lookup and membership.
The pinned Forge 43.4.0 sources distinguish active registries from historical
VANILLA/FROZEN copies and mutable registration internals. Namespace relocation
cannot preserve that distinction.

Implement the verified read-only ACTIVE/ForgeRegistry contract for standard
process-global source registries. Preserve source defaults, unknown registry
absence, stable read-view identity and immutable/live collection semantics.
Use exact source owner/member evidence and project all stored/callable types.
Keep the original JAR immutable and keep mutation, snapshots, source subclasses,
unknown members and moved data-driven registries explicitly unsupported.

This is a bounded read adapter, not support for RegistryBuilder, registry
replacement/remapping, inactive overrides or all of JEI. It preserves proven
source read behavior rather than selecting a different target simulation.
Source scope: Forge 1.19.2; target: the pinned NeoForge 26.3 build. Verification
must include independent transformed fixtures, actual native registries, negative
members, original runtime consumer and the next first causal failure.
