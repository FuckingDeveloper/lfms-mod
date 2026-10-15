# Verification

- [x] Read the source and pinned target chunk packet, block data and model-data contracts.
- [x] Preserve the existing native/source callback boundaries and worker snapshot ownership.
- [x] Add disabled-by-default tracing selected by exact position, without fixture selectors.
- [x] `./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava`.
  Existing native block-data and positioned-model probes exercise callback order,
  snapshot identity/refinement and rejection of live-world queries from the model.
- [x] Observe actual saved source data, initial tag output/input and a nonempty
  positioned snapshot in a development client.
- [x] Place a fresh connected cable pair through MCP's ordinary interaction tool
  and verify source connection data in the native saved chunk after normal exit.
- [x] Verify fresh-pair initial transmission and positioned rendering after restart.
- [x] Complete visual verification from outside interaction range and remove temporary
  gameplay fixtures; restore player position/view and inventory.
- [x] `git diff --check`.
- [x] Commit only this diagnostic checkpoint.

Compatibility remains partial. Missing block-entity records in an existing save
are not reconstructed by this change. The cause/timing of their loss remains
unproven; energy transport and all legacy cable variants remain separate checks.
Detailed observations and per-run logs are temporary/local evidence outside Git.
