# Verification

- [x] Inspect exact target ClientItem/CuboidItemModelWrapper/ItemTintSource
  codecs and retained native generated geometry parents.
- [x] Inventory immutable Iron Chests models: 7 generated upgrade models,
  14 builtin/entity chest models. Only the proven generated family is admitted.
- [x] Native smoke verifies cuboid-definition and tint codecs, source layer
  indices, source callback dispatch exactly once, absent callback white,
  native resource discovery, explicit-native precedence, loader/override/entity
  negatives and unchanged artifact bytes.
- [x] `./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava`
  passed (`/tmp/lms-static-item-check.log`, local only).
- [x] Runtime 29 loaded the existing world with no new model decode exception.
  All seven missing upgrade-model warnings disappeared; the 14 unsupported
  builtin/entity model warnings remain explicit.
- [ ] Human visual inventory check of the generated upgrade icons.

No custom chest renderer, arbitrary source override or complete Iron Chests
rendering acceptance is claimed. Overall level 6 plus earlier observed level-7
subpaths remains unchanged by resource discovery alone.

## Closed block-model inheritance (2026-10-17)

- [x] Inspect immutable source item/block resources and exact target cube templates.
  Source 1.19.2 and target 26.3 cube/cube_bottom_top JSON are equal.
- [x] Extend derived definitions through artifact-local indexed parent chains; keep
  original model identity and native definition precedence. Reject missing/external
  parents, cycles, inherited loaders, overrides and custom-render markers.
- [x] Native regression covers inherited/direct cubes, codec identity, negative
  boundaries, native precedence and unchanged artifact bytes.
- [x] `./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava`
  passed (`/tmp/lms-static-block-models-check.log`).
- [x] Fresh runtime99 loaded `sp:Новый мир`, MCP client/integrated-server gates
  passed; creative content populated (147/106/110/40 stacks across TNT tabs).
  No `Missing item model`, ERROR or FATAL entries after reload/world entry.
- [ ] Runtime99 original workbench open/reopen (waiting for client control).
- [ ] Human visual acceptance: creative icons and workbench preview/selection.
