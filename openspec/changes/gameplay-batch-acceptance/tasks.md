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
- [x] Dedicated server: physical-side filtering and common/client event isolation;
  run6 loads all three unchanged legacy entrypoints and a real world, then saves
  and stops cleanly. Gameplay coverage remains scoped below.
- [x] Startup: fresh phase measurements recorded for runtime58 below. No new
  optimization claimed without a measured cause.
- [x] Particles: original iron-furnace callback runs; runtime58 human acceptance
  confirms both smoke/flame and crackling. Ordinary generator has no source effect.

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

## Runtime57 client regression / current handoff

Quick Play opened Новый мир. MCP confirmed 26.3 NeoForge client, integrated
server, all required command/world/container groups and the original inventory.
Client timings: CarbonConfig59/1747ms, IC21074/8717ms, IronChests57/213ms source
classes/cumulative transformation; initial-data bootstrap02:21:31, final02:21:39,
player login02:21:43. These are measurements, not a new optimization claim.

Real empty-hand use opened a new source iron furnace. MCP Shift transfers inserted
coal/cobblestone. Source NBT reports fuel1598, active=true, progress2; later output
slot2 holds one stone. Its field-sync payload hits SoundEvent.m_11660_ before
normal fuel sound setup; the client survives but particle/sound acceptance is not
complete. New source-sound-event-values code/spec/tests are prepared but NOT YET
compiled or run: the user must Save/Quit before rebuilding dev classes.

Cleanup deliberately tried Count:0b and exposed native item codec rejection.
source-empty-stack-decoding prepares a whole-schema decode preserving empty-stack
component payload rather than arbitrary EMPTY; likewise NOT YET compiled/tested.
The failed cleanup command did not clear the furnace output. Native block removal
then removed the fixture; structured radius3 item-entity query returns none.
Original poppy was copied back from the native barrel before barrel removal;
full structured inventory equals the captured baseline. Player position/selected
slot/camera restored. The test-only Stone Age advancement was revoked. Fixture
positions(-206,78,12/13) restored to their observed original air. No survival or
physical damage probes were performed.

8c6b352 is committed/pushed for the verified distribution/server family only.
Existing eight-file dirty baseline remains preserved. New sound/empty-stack work
is uncommitted pending compile/native checks and fresh runtime acceptance.
Save/Quit question is pending; do not stop the live client or rebuild until
serverPresent=false and save logs confirm the user's world exit.

## Runtime58 acceptance

Commits 55e2fe4 (empty stack/tag payload), fc5fc79 (proven native energy
operations), and 82da68d (sound value family) are committed and pushed. Wrapper
`compatibilityTest nativeCompatibilitySmoke compileLaunchJava` passed together
in `/tmp/lms-gameplay-batch-check-11.log`; dedicated run6 also passed. These
results supersede the pending compilation statements in the runtime57 handoff.

Runtime58 real empty-hand interaction opens the source iron furnace; Shift
transfers insert coal/cobblestone, original ticks produce stone. The previous
SoundEvent field-sync failure is absent. User confirms particles and sound.
This closes the positive particle/audio case without adding effects to source
generators that do not emit them. Overall observed level remains 6 with level-7
functional subpaths, not universal compatibility.

Measured source classes/cumulative transform milliseconds: CarbonConfig59/1624,
IC21074/8333, IronChests57/222. Initial setup08:25:05, final data08:25:13.
These cumulative class timings do not establish a startup bottleneck.

Current runtime58 cleanup obligations: original hotbar6 poppy is backed up in
barrel(-206,78,13), slot0; source furnace(-206,78,12) was removed by the human.
Restore poppy and original air after remaining probes; revoke test-earned
Stone Age only. Retain the newer user-owned rotten flesh/lead. Charged-copy and
remaining mechanism/editor acceptance are still pending.

## Runtime59 charged copies and remaining mechanism blocker

After the repeated damage-singleton traversal correction, actual client survival
mining consumes 50EU from a source chainsaw copied through native ItemStack copy
operations. Control stacks in barrel slots1/2 remain1000 while the held stack
becomes950. A second independent held copy also becomes950 while the first used
copy stored in slot3 stays950 and both controls stay1000. Instant MCP server
break did not consume charge and is explicitly not used as proof; realistic
client survival mining supplied the positive callback evidence. Player is back
in creative; no aerial/damage test was intentionally initiated.

A fresh macerator opens, closes and reopens; real Shift insertion accepts gold
ore and original processing consumes800EU, yielding2 gold dust. Shift extraction
takes those dusts. Copper ore is rejected. Native commands establish c:ores/copper
membership and absence of forge:ores/copper. Immutable source copper recipe
uses a dynamically constructed Forge key rather than a migrated static field.
This exposes the standard-tag identity gap, not missing GUI/slot functionality.
Proposed general contract: common-tag-identity-projection/proposal.md, awaiting
user decision; no identity-specific production patch was added.

Test machines/barrel/copies/dust/copper/logs are removed. Structured inventory
equals the runtime58 baseline; armor source/native damage0 restored, XP progress
restored with native experience command, source position/camera/selected slot
restored, Stone Age revoked. Server saturation20 is authoritative (MCP client
state retained stale11); no blanket effect clearing. Crash/startup explosion
terrain limitation is documented in source-damage-singleton-traversal/design.md.
Configuration backups for the next human UI batch are in /tmp only.

## Runtime60 standard tags and nested editor family

User accepted the shared standard-tag identity contract. 39cc7d8 implements the
source declaration table and target admission, source key factories/constructors/
codecs and source tag resource/reference projection. Native checks and the cold
client verify the formerly blocked copper recipe: one copper ore -> two copper
dusts, with800EU consumed by original ticks. Real GUI reopen and Shift extraction
succeed. Unknown tag IDs/registries remain unchanged. No manual reload and no
original artifact changes. compatibilityTest/nativeCompatibilitySmoke/
compileLaunchJava passed together in batch-check-15.

73cd154 adapts source Forge layered push/pop/clear to the existing target Gui
stack. Runtime59 user testing and actual log prove enum/numeric editors both
failed at pushExternalScreen, while boolean save and an actual ZIP backup worked.
The test-only auto-backup toggle is restored false; background RAW_IRON and audio
master0.5 were unchanged. Runtime60 human request covers enum/numeric edits,
save/reopen and return to parent; functional acceptance is pending. Source
getGuiFarPlane and arbitrary immediate layer rendering are not claimed.

Runtime60 probe stayed creative. The original crafting table was temporarily
copied into an empty player slot, then restored. Test block/ore/dust removed;
server inventory and client structured inventory match the captured runtime60
baseline after native inventory click synchronization. XP progress, position,
camera and selection restored. No nearby test item entities and no fresh
ERROR/FATAL at probe completion. Existing eight-file dirty baseline is retained.
Current level remains6 plus verified level7 functional subpaths; remaining
mechanism widgets/comparators, custom equipment and native energy flags retain
separate gaps rather than being closed by copper or loading alone.

## Runtime60 ->61 overlay background correction

Runtime60 human dropdown activation reaches the child screen but crashes in
native Screen.extractBackground with "Can only blur once per frame". Source
Forge does not automatically draw a full-screen background per screen layer.
642940b confines the target default-background omission to managed ordinary
source overlays above the bottom entry of the real native Gui stack; native/
root/off-stack/container background contracts remain unchanged. Native subtitle
tail and render events survive. Source instance push/pop and all original
resize/selection/cancel paths are adapted along with the static family.

Wrapper compatibilityTest/nativeCompatibilitySmoke/compileLaunchJava passed
in batch-check-17, including actual applied target injection/accessor and native
multi-layer blur/reset invariants. Runtime61 Quick Play opens the world in
creative with expected health/food/XP/position and no fresh ERROR/FATAL at handoff.
Manual request pending for dropdown selection and numeric editor/save/reopen.
This startup is not evidence that the remaining configuration workflow passed;
level remains6 plus previously verified level7 functional subpaths. No gameplay
fixtures or original config values were changed during this run.
