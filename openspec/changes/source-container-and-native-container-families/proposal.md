# Complete source loader identity and native container families

The secondary reference artifacts exposed two independent generation contracts:
source loader identity/metadata/configuration ownership and native container
persistence/construction/lifecycle. Adapt the contracts in the Forge 1.19.2
profile and pinned 26.3 target adapter without modifying original artifacts or
branching on a reference mod identity.

Required guarantees and failure boundaries are in design.md. Native FML membership
and frozen registries remain authoritative. Source config editing, native/source
networking, graphical rendering and gameplay require their own observed evidence;
initialization alone establishes none of those claims.

Verification uses the wrapper compatibility/native smoke tasks, fresh runClient,
MCP structured world/inventory/block/container APIs and causal runtime logs.
