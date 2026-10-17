- [x] Inspect source/target constructor and visibility evidence.
- [x] Preserve direct source Button allocation using native builder.
- [x] Restore verified retained public block callback signatures across target overrides.
- [x] Snapshot captured source item properties within deferred ITEM construction.
- [x] Pass compatibilityTest, nativeCompatibilitySmoke and compileLaunchJava
  (`/tmp/lms-captured-construction-check.log`).
- [x] Runtime112: Wick now INITIALIZED; source onPlace blocker disappeared.
  Its next same-family blocker was the moved isRandomlyTicking method; expanded
  metadata restoration and added native dispatch regression coverage.
- [x] Repeat compile/focused/native checks after expansion
  (`/tmp/lms-captured-construction-family-check.log`, BUILD SUCCESSFUL).
- [x] Runtime113: explicit test world loaded and MCP gates passed. Previous
  callback and captured-property registration exceptions did not recur.
  Native command gave original black_mud_brick_stairs item; API observed it in
  inventory. Native setblock/readback confirmed east/top/straight/non-waterlogged
  state and hardness1.5 at (-101,245,27). Tagged item and block removed.
  This is native registration/command evidence, not ordinary placement acceptance.
  Fetzi constructor remains BLOCKED by BANNER_PATTERN registry migration; Wick
  INITIALIZED is not dynamic-lighting acceptance. Only that known constructor
  error occurred in this bounded run; JEI remains admission-blocked.
- [ ] Verify registered content and behavior once startup prerequisites pass.
