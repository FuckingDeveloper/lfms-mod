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
