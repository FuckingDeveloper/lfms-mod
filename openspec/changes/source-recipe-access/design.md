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
