# Rejected experiment: ordered source setup before initial data loading

Forge 1.19.2 common setup publishes procedural resources and consumes item
construction defaults. Target 26.3 binds global default components during data
loading. Deferring the callback past tag loading loses first-load declarations;
splitting it into deferred helpers loses observable ordering.

The attempted target adapter executed the complete source callback at its original event
phase in an isolated-default lookup scope. Only actual built-in registries are
available there. Exact item construction/default initializers remain authoritative;
missing dynamic lookups MUST fail, never become dummy registry entries. No global
holder defaults are bound early. Component initializers added in this phase are
consumed by the subsequent ordinary target data load; no pending set exists yet.
Source helpers remain synchronous inside the scope. The later pre-binding scope
continues to rebuild an already-created pending set when needed.

No forced server reload, duplicate callback, source-JAR modification or fixture
identity selector is permitted. Initial procedural tag membership and original
setup dependencies must be verified in a fresh client process.

Runtime47 failed closed on a native trim_material/lapis lookup during the original
item scan. The implementation was removed. A single apple-default smoke test
was insufficient to establish arbitrary inventory scanning before dynamic data.
This document records the rejected experiment, not an implemented contract.
