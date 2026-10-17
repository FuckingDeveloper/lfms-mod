# Source tag notification identity and client readiness

## Evidence

Runtime127 loads the explicit world, has no ERROR/FATAL after the custom display
fix, and confirms the persisted player/inventory/fixture recovery through MCP.
JEI's original observer enters ENABLED and receives the genuine recipe event,
but never enters JEI_STARTED. Its immutable source required-event set contains
exact TagsUpdatedEvent.class and RecipesUpdatedEvent.class; it ignores events
while DISABLED and records each incoming event.getClass().

Pinned Forge43.4.0 TagsUpdatedEvent is one concrete event class, retaining
RegistryAccess, UpdateCause and integrated-connection semantics. Target26.3 is a
sealed root with ServerDataLoad and ClientPacketReceived concrete subclasses.
Native client configuration posts ClientPacketReceived before configuration
finishes, before ClientPacketListener.handleLogin posts LoggingIn. Blind namespace
projection therefore changes both exact event identity and client readiness.
This is a generation contract, not a JEI-specific condition.

## Decision accepted

User approved: retain the source tag notification contract through a source-owned
view. Deliver server tag updates at their native stage. Capture an initial client
configuration tag update for its genuine connection, and deliver its source view
once after that connection's native login notification completes. Later client
updates are delivered at their ordinary native update boundary while the player
is ready. Preserve actual registries, cause, shouldUpdateStaticData and callbacks.
Do not repost or delay native events or change native tag/component binding.

This selects source client readiness over the target's earlier configuration
notification for source callbacks. It changes callback timing across this native
phase boundary and requires AGENTS.md §22 agreement. No arbitrary timer, synthetic
recipe/tag data, guessed mod selector or legacy JAR edit is allowed.

Alternative: preserve native configuration timing, expose a truthful source view
immediately and explicitly leave source consumers requiring post-login tags
unsupported. Actual tag data is correct, but these consumers do not fully start.

## Scope

Only Forge1.19.2 source tag notifications on the configured target. No source
Mixin execution, cross-generation coexistence or full remote recipe synchronization
is established. Source server/dual-phase setup and native static-data epoch updates
continue at their established real native phases.
