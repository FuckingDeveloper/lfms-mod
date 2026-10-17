# Verification

- [x] Inspect pinned source entity context and actual target entity/block-entity contexts.
- [x] Inspect all context uses in the external SkyBombRenderer fixture: bakeLayer
  already passes; block dispatcher is the failing constructor service.
- [x] Preserve each actual resolver and fail on missing resolver/unknown entity-context subclass.
- [x] Verify named/SRG direct/handle/nested getters for both actual native contexts,
  exact service identity, artifact immutability and unsupported model use.
- [x] `compatibilityTest nativeCompatibilitySmoke compileLaunchJava` passed:
  `/tmp/lms-render-context-check.log`.
- [x] Runtime94 passes prior context blocker and loads the explicit test world.
  MCP confirms Minecraft26.3/NeoForge, integrated server, Dev, dimensions and inventory.
  Set/readback tntexpansion:tnt_workbench at (-197,79,12) succeeds. Real container
  interaction reaches the source TntWorkbenchScreen, whose preview then crashes
  on DefaultedRegistry.m_7745_(Identifier). No successful menu-render claim.
  Evidence: `/tmp/lms-container-families-runtime-94.log` and crash
  `run/crash-reports/crash-2026-10-17_03.26.17-client.txt`.
  The temporary workbench remains at that previously-air coordinate for cleanup
  after the next world load; player position/inventory were not changed.

Source immediate renderSingleBlock/tessellation, custom entity callback submission
and item-renderer service migration remain separate functional boundaries. They
are not claimed from retaining a context resolver.
