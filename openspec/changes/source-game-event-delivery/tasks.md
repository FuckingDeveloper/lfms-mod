# Verification

- [x] Compare runtime108 first server cause, source GameEvent/LevelAccessor/Entity
  and exact patched native holder fields/default delivery implementations.
- [x] Implement metadata-proven raw constant projection, field handles and all
  four level/two entity delivery operations; reject changed source overrides.
- [x] `compatibilityTest nativeCompatibilitySmoke compileLaunchJava` passed
  (`/tmp/lms-game-events-final-check.log`). Native entry spies verify exact registered
  holder, actor/null, Vec3/context identity, BlockPos center, default/native
  override dispatch, once-only callback/failure and source override rejection.
  These are not claims of real vibration/listener gameplay.
- [x] Updated selected audit: 4323 classes, 1124 groups / 5693 sites
  (`/tmp/lms-game-events-final-audit.log` and `.tsv`). Prior vanilla raw constant
  and Level event-delivery findings disappeared; no new game-event transform
  failure was introduced. Interface-owned field-handle adapters also have a
  focused native check for the correct JVM reference kind.
- [ ] Runtime ignition/listener acceptance remains BLOCKED by independent custom
  EntityRenderer extraction. Runtime108 is the preserved prior failing log;
  no fresh client was launched into that known renderer crash merely to claim
  a startup attempt. Entity-render static block context decision is prepared in
  source-entity-render-extraction/proposal.md.
