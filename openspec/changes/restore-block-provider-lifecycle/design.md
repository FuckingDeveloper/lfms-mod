# Block provider lifecycle — evidence and remaining design

Status: block constructor/dispatcher/invalidation/ForgeCaps contracts implemented
and checked with the actual physical-client FML/mixin loader. Gameplay acceptance
and full source mechanism persistence remain pending. See
`openspec/changes/batch-runtime-semantics/tasks.md`.

The next causal blocker is a custom source token falling through a source block
entity override to the original BlockEntity superclass query. The latest server
report is `crash-2026-10-14_02.33.15-server.txt`. It runs inside the scheduled
`onLoaded` callback: the user's preceding furnace destruction does not prove
that a destruction hook caused it.

Forge 1.19.2 evidence from its pinned source JAR:

- `patches/net/minecraft/world/level/block/entity/BlockEntity.java.patch` calls
  `gatherCapabilities()` at the end of the base constructor, after type, position
  and state initialization, before subclass construction finishes.
- `CapabilityProvider` gathers an attachment event eagerly for this constructor
  (`super(BlockEntity.class)`, not its lazy overload).
- `CapabilityDispatcher` queries providers in attachment order, returns the
  first present optional with its identity, rejects null results, and returns
  empty only after genuine provider absence is established.
- Removal and chunk unload invalidate the dispatcher and its callbacks.
- ForgeCaps serialization is separate from ordinary subclass NBT fields.

A correct continuation must preserve eager constructor timing, generic event
subject/filtering, listener priority and owner contexts, per-receiver dispatcher
state, query side/identity, removal/unload invalidation and serializable provider
state. Merely returning empty for an unknown token, or lazily gathering on first
query, is not the source contract. Unknown-token emptiness becomes justified
only after the genuine dispatcher has been gathered and queried.

Receiver-owned state must not be kept in a global map whose values can retain
its weak keys, source loaders or worlds. Native builtin provider adapters must
remain distinct from source attached providers and explicit superclass dispatch.
An early target lifecycle hook must be checked against the pinned target ABI
and existing launch planning/verification before implementation. Source gather
operations and target capability registration must not be blindly equated.

Independent wire investigation: user confirms that tin cables with the first
insulation have no visual connections. The existing legacy neighbour-call
adapter does not establish callback delivery for source Block overrides with a
source-position parameter. That origin must survive target queued notifications;
a nullable redstone Orientation cannot reconstruct it. Model/connectivity state
and actual energy transfer remain independent acceptance checks.
