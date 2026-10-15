# Remaining gameplay batch (2026-10-16)

User authorized the complete remaining batch plus absent smoke/flame. Overall
observed level remains 6 with verified functional level-7 subpaths. Historical
unchecked boxes in close-gameplay-families are chronological, not current failures.

- [ ] Energy/capabilities: independent charged copies, tool callbacks, persistence,
  sided native interoperability. Existing generator -> cable -> macerator and
  chainsaw 1000 -> 950/save/restart are already observed; do not reopen as unknown.
- [ ] Mechanisms: remaining filter/comparator/buttons and Shift routes; copper
  recipe after first-load tags. Generator open/close/reopen and gold/deepslate iron
  recipes already observed. MCP lacks arbitrary widget activation.
- [ ] Configuration: remaining editor families, actual save/apply/restart, backup.
  Normal configuration lists/screens are already accepted; preserve explicit audit gaps.
- [ ] Equipment: custom armor model callbacks, dyes/overlay, dispenser,
  enchantment callbacks. Four-slot textures, armor protection, repair and damage
  synchronization were accepted in the preceding equipment batch.
- [ ] Dedicated server: resolve physical-side loading from actual runServer evidence.
- [ ] Startup: fresh phase measurements; optimize only measured causes.
- [ ] Particles: compare original animateTick/active-state/particle operations and
  verify a machine that actually emits them in the immutable source artifact.

Retired/limited, not secretly implemented:
- Old cables retired by user. Fresh-pair save/reload was separately verified.
- Transparent chest cold inventory preview is original source behavior; user
  rejected a fixture-specific extension. No missing general callback proven.
- Creative-search null-player tooltip remains a source behavior limitation.

## Smoke/flame investigation

Original IC2Classic 2.1.3.4 bytecode registers generator as BaseGeneratorBlock,
without enableAnimations(). FuelGenTileEntity extends BaseGeneratorTileEntity;
neither declares/implements IParticleSpawner. Base generator block construction
and its BaseFacingBlock/BaseTexturedBlock ancestry must not be replaced with new
furnace effects. Source IC2ContainerBlock only calls animationTick when its
animations flag is enabled and the tile implements IParticleSpawner.
IronFurnaceTileEntity does implement the callback: while active, it emits source
smoke/flame and occasional crackling. Check that real callback as the positive
case before claiming particle-family functionality. No production mod-specific
particle behavior added.

## Dedicated/default equipment acceptance

Source distribution filtering and lazy client event isolation passed full checks.
Dedicated server loads all three source entrypoints, ordinary initial-data setup
once and the actual world. Real registered generator simulates fuel -> EU, chest
accepts slot writes, native stop saves the world. Restart reads the three saved
chest ingots. Generator source fields restore and continue ticking, but the last
pre-stop energy snapshot did not match: source ticks do not mark every fuel/storage
change dirty. Both source and target chunk saves require dirty chunks; do not
claim exact tick-state persistence or silently add mod-specific dirty calls.
Controlled ordinary fuel/inventory/save acceptance remains required.

Actual native dispenser, triggered by redstone, equips the original registered
bronze chest armor onto a tagged armor stand; source item leaves the dispenser,
native entity equipment reads count1/ic2:armor_bronze_chest. This closes the
observed default armor dispenser path through native EQUIPPABLE defaults; custom
source equip/dispenser overrides remain unverified. Positive result requires
no new production behavior. All created test blocks/entities/items and force-load
ticket are removed from the separate dedicated test world before shutdown.
