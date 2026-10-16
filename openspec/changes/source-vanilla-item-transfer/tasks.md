# Source vanilla automation acceptance

- [x] Inspect pinned Forge VanillaInventoryCodeHooks/ItemHandlerHelper and actual
  target hopper/dropper discovery/transfer code. Native selection runs once;
  existing native containers and transactional providers keep their paths.
- [x] Adapt source block handlers after empty native discovery: hopper insertion,
  shared hopper/minecart extraction entry and dropper insertion fallback.
- [x] Preserve source query face, item/data ownership, callback timing, one-item
  transfer, rejecting-slot restoration and native dirty/cooldown ownership.
- [x] Implement proven source helper members; preserve explicit unsupported
  player-give and unknown stack capability/component/damage boundaries.
- [x] Extend comparison proof for ordinary BlockItem defaults, excluding all17
  original shulker identities through the actual native color collection.
- [x] `./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava`
  passes: `/tmp/lms-source-transfer-check-7.log`. Tests include original immutable
  helper consumer transformation, actual applied native mixins, import/export
  rejection, full/stacked destinations, one-item conservation, source callback
  timing, native transaction rejection before mutation, helper slot order/NBT/
  fullness and all17 shulker exclusions.

## Runtime evidence

Runtime79 reaches source insertion but crashes on missing ItemHandlerHelper.
Runtime80 confirms four coal transferred (three retained plus one burning) and
generator4000EU, with dirt rejected. It later crashes at the existing ordinary
BlockItem comparison proof boundary. Both first causal blockers are addressed.
One runtime80 command used the incorrect fixture ID ic2:copper_dust; it failed
without crashing. Correct registered ID ic2:dust_copper was then verified from
the source language data and actual successful registration/load.

Runtime81 cold load accepts three gold ore into actual macerator input slot1,
rejects two bedrock and records no ERROR/FATAL/Caused-by. Runtime82 repeats this
with final callback ordering and independently confirms:

- [x] Blocked machine sides: top hopper retains three gold ore, source retains
  two gold ore, bottom hopper stays empty.
- [x] Source slot0 configured IMPORT-only retains its controlled one-ore fixture;
  two output copper dust from slot2 transfer to native bottom hopper. The exact
  source tag LmsTransferProbe=1b becomes native CUSTOM_DATA with count2 unchanged.
  Slot0 is a controlled access-rule probe, not a claim about normal recipe input.
  The source slot_data encoding5862630359103 is derived from pinned MathUtils:
  ((slot0<<16 | access1365)<<32) | sideMask63.
- [x] Top hopper transfers three gold ore into real input slot1; two bedrock
  remain in the hopper. Source item validity and stacking execute in the original
  BasicInventory, not a replacement recipe/filter implementation.
- [x] A powered native dropper sends exactly one gold ore: dropper2->1,
  source input3->4. A second probe with two bedrock retains both and emits no item
  entity; source contents remain unchanged.
- [x] Runtime82 has no ERROR/FATAL/Caused-by through final cleanup.

No live minecart extraction was exercised; its shared native entry is linked and
applied, without claiming independent gameplay acceptance. General transactional
source capability exports, entity source providers, full filter-module UI coverage
and arbitrary third-party automation remain gaps. Current compatibility remains
level6 plus the individually observed level7 paths above.

## Cleanup and checkpoint

All newly created rig positions(-211..-209,81..83,12) are restored to observed
original air; structured region query returns no blocks. Item-entity query around
the rig returns none. Existing generator(-206,78,12) retains storage4000,
side49/access3843, comparator source flags5/slot6 and max2/slot3. Player position,
camera, health/food/XP/selected slot are unchanged; no player inventory operation
was performed. Runtime82 remains running. No original legacy artifact, MCP code,
token/config/run state or pre-existing user change belongs in the checkpoint.
