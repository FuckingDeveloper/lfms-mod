# Full source recipe access

The exact Forge 1.19.2 Level, ClientLevel, ServerLevel, MinecraftServer and
ClientPacketListener full-manager queries MUST share a verified invocation
family, including inherited receivers, callable handles and nested constants.
Server worlds MUST expose their actual current RecipeManager through native
recipeAccess; no global server may substitute for the receiver's world.
Client access retains the existing co-resident integrated-server restriction.
It MUST validate the active client world/connection and use that client's
integrated server. Remote or stale contexts MUST fail explicitly rather than
fabricate full recipes from the reduced RecipeAccess view. No manager snapshot
may survive a reload or world transition. Source overrides of these removed
accessors remain explicitly unsupported until their dispatch is adapted.

Evidence: pinned Forge43.4.0 ClientLevel delegates to its connection, ServerLevel
to its server; target26.3 Level.recipeAccess returns RecipeAccess, ServerLevel's
covariant implementation returns its server's RecipeManager, ClientLevel uses
the connection's reduced view. This extends the existing bounded client policy,
not the synchronization protocol.

## Recipe information queries

The source manager's getRecipes/getAllRecipesFor/byKey/getRecipeIds MUST expose
actual recipe values and authoritative holder identifiers, never holder objects
in an erased source collection. Type enumeration MUST use the native type index
and retain its current order, returning a source immutable list. Full enumeration
MUST return the source detached mutable set (including ordinary equality-based
deduplication); caller mutation MUST NOT mutate the native manager. Key lookup
MUST retain Optional presence and register holder provenance before returning
its recipe. Missing/null identifiers return source absence. Identifier streams
MUST expose actual holder keys. Queries MUST read the supplied manager on each
invocation, without a global recipe cache. Direct, inherited and callable forms
share the world-access traversal. Matching/assembly through removed source
Container inputs, custom manager construction, network codecs and mutation remain
separate unproven boundaries. The evidence is pinned source RecipeManager bytecode
and target RecipeManager/RecipeMap: values are now RecipeHolder instances and the
native byType index remains authoritative.
