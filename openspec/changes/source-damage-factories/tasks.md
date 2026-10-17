# Damage factory/delivery verification

- [x] Inspect runtime100 causal explosion factory failure and subsequent original
  Entity.hurt call before another client run.
- [x] Inspect source explosion attribution and exact native registry factories,
  damage JSON, side selector and native virtual hooks.
- [x] Adapt five bounded factory overloads plus proven virtual damage delivery;
  capture a unique formal world before local reuse; reject ownerless/ambiguous
  unscoped factories and preserve managed callbacks.
- [x] Native ASM fixtures verify fresh-local context capture, native explosion key,
  source attribution, ambiguous/null-handle failures, native server/client damage
  branch, amount/source/result and exactly-once original managed override.
  World/entities in these probes are bounded test doubles, not gameplay evidence.
- [x] `compatibilityTest nativeCompatibilitySmoke compileLaunchJava` passed
  (`/tmp/lms-damage-factories-check-2.log`).
- [x] Selected integration audit passed: 4323 classes, 1155 grouped findings,
  5789 sites. Former four static factory and Entity/LivingEntity hurt member
  findings are absent (`/tmp/lms-damage-factories-audit.log` and `.tsv`).
- [x] Runtime101 restored the saved source projectile; it passed the prior
  factory and Entity.hurt blockers, then failed after knockback on removed
  Entity.hurtMarked (f_19864_). No additional human throw was required.
- [x] Inspect exact source/native tracking consumer/reset behavior and adapt
  hurtMarked/hasImpulse to live syncVelocity/needsSync. Reuse field-declaration
  proof and callable-field adapters; native regression verifies set/get/handles
  affect actual tracker fields and resets.
- [x] Full focused/native/build checks passed again
  (`/tmp/lms-damage-factories-check-3.log`).
- [x] Selected audit after tracker adaptation: 4323 classes, 1154 grouped
  findings / 5785 sites; removed hurtMarked finding absent
  (`/tmp/lms-damage-sync-audit.log` and `.tsv`).
- [x] Runtime102 passed all previous damage and tracker operations. Original
  detonation queued its terrain job and original level-tick listener executed;
  next causal blocker is ServerLevel.m_141937_ (source getMinBuildHeight), then
  related height queries in chunk carving. World readiness was transient; MCP
  disconnected at that crash, so player restoration/GUI checks remain pending.
- [x] Runtime104 after height/packet adaptation: source dynamite use, collision
  and original terrain change completed on an isolated sky fixture without the
  earlier damage/tracker exceptions. User accepted workbench images/selection.
  Temporary item/block and old workbench cleaned; baseline inventory/vitals,
  position/camera/slot restored. Separate SoundManager click error remains.

Remaining boundaries: arbitrary source damage constructors/mutators/super bodies,
unscoped ownerless callable factories, ambiguous world operands and custom native
mod APIs. Overall compatibility remains level 6 with observed functional subpaths;
no whole-mod explosion/visual acceptance is inferred from structural audits.
