# Verification

- [x] Verify source HOE_TILL mapping and exact target Holder.Reference ABI.
- [x] Extend the existing holder projection with verified reference-backed fields.
- [x] Native smoke executes five original-shaped SRG field reads (hoe, shovel,
  axe strip/scrape/unwax) and verifies exact native SoundEvent identity.
- [x] Run compatibilityTest, nativeCompatibilitySmoke and compileLaunchJava.
- [x] Runtime42 MCP use_item returns success on a temporary dirt block; server
  get_block confirms farmland. The former sound field blocker is absent. The
  original tool NBT (charge 100000), free backup slot, player position/view/slot
  and temporary air positions are restored after the probe.

The only ERROR in runtime42 at verification is the previously known
forge:global_loot_modifiers list-schema error. The runtime39 null-player creative
tooltip failure and initial chest/cable presentation issues remain open.
