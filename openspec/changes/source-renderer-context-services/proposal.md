# Preserve block service intent from both source renderer contexts

Runtime93 reaches SkyBombRenderer construction after passing RenderType setup,
then fails its original getBlockRenderDispatcher call. Extend the existing
block-entity context bridge to the entity context using the exact context-owned
native resolver. Generalize direct-only source getter handling to the existing
semantic invocation rewriter, including method handles and nested constants.
This does not implement immediate block drawing or fabricate a native renderer.
