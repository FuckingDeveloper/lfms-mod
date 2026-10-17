# Verification

- [x] Inspect pinned source bytecode, SRG/Mojang identity and actual target state/pipelines.
- [x] Specify cutout, no-cull, depth-offset and outline/default distinctions.
- [x] Verify eight transformed source factories: direct calls, handles/nested constants,
  exact native selection, textures, blending and true/false/default outline state.
- [x] `compatibilityTest nativeCompatibilitySmoke compileLaunchJava` passed.
  Evidence: `/tmp/lms-render-factory-check.log`.
- [x] Runtime93 passes the former NukeModel RenderType factory failure.
  Next causal blocker: SkyBombRenderer constructor calls source entity-context
  getBlockRenderDispatcher (`m_234597_`) with a source-shaped dispatcher return.
  MCP connected, but no integrated server/player; world/gameplay remain BLOCKED.
  Evidence: `/tmp/lms-container-families-runtime-93.log`.

TNT Expansion constant-pool inspection found its RenderType factory use only in
NukeModel.m_110458_; sibling coverage above is generation-based rather than a
fixture-specific selector. No source JAR was modified.
