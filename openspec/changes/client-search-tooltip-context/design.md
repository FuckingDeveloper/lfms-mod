# Logical client context during native search tooltip evaluation

Historical runtime39 fails in a source tooltip listener's server-side player path
on a native SessionSearchTrees worker. The previous blanket classification as a
source null-player defect was incomplete: original PlayerHandler.getClientPlayerHandler
explicitly accepts null and resolves the genuine client handler. Its selection
uses source EffectiveSide, which (like pinned target FML) derives a fallback from
ThreadGroup. Original1.19.2 creative search populates/refreshes its tooltip index
synchronously on the client; target SessionSearchTrees evaluates tooltip lines
inside an asynchronous search constructor. Worker group ancestry is not proof
that this client-owned operation is logically server-side. Runtime65 normal
search/hover does not currently reproduce the failure; do not claim a fresh crash.

At the verified target SessionSearchTrees ItemStack.getTooltipLines invocation,
source EffectiveSide queries MUST observe logical CLIENT for that synchronous
call scope, including nested original source callbacks. Preserve the actual
nullable player, tooltip context/flag, return value, exceptions and once-only
native invocation. Native FML EffectiveSide and unrelated source queries remain
unchanged. Restore the previous source context on success/failure; keep no player,
world or managed loader in a static cache. This neither marshals callbacks between
threads nor claims arbitrary legacy tooltip listeners are worker-thread safe.
No fixture identity, skipped listener, fake player or suppressed exception selects
or implements this contract. Creative and recipe tooltip streams share this exact
native evaluation helper and receive the same proven client-operation context.
