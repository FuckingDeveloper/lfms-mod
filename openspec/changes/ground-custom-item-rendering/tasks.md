# Tasks

- [x] Inspect pinned native extraction/light/bounds/submission ordering.
- [x] Specify scoped source-local GROUND recording and explicit gaps.
- [x] Implement identity-matched, exception-safe native extraction scope.
- [x] Verify scope isolation, actual transformed bounds and native submissions.
- [x] Compile and run compatibility checks.
- [x] Run dev client and verify actual dropped legacy content.

## Verification

`./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava` passed.
The focused native check covers distinct GROUND model transforms, actual vertex
bounds, packed light, isolated source input, once-only recording, repeated native
submission, nested scope failure cleanup and mismatch/outline rejection.

Runtime39: `./gradlew runClient -PlmsTestWorld='Новый мир'` loaded the existing
world through MCP Fabric. Tagged test item entities contained Iron Chest x1,
Trapped Crystal Chest x32 and Obsidian Chest x64. A deliberate framebuffer check
confirmed the original source textures and native stack spreading without the
missing-model cube. Restoring ordinary gravity placed the first two on the
platform at y85 and the third on terrain at y80 without a crash. All three tagged
test entities were removed; original player position, orientation and selected
slot were restored. Inventory and existing blocks were not changed by the probe.

No new runtime ERROR/FATAL or causal exception was observed. The existing
`forge:global_loot_modifiers` list-schema error remains a separate blocker.
Compatibility remains functionally partial: target shelf/outline semantics and
initial transparent-container content synchronization remain unverified/gaps.
