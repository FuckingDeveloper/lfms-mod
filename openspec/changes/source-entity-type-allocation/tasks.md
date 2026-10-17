# Verification

- [x] Runtime107: saved projectile exposed raw EntityType.create(Level), then inspect
  all three related fixture calls and their follow-up position/world-insert paths.
- [x] Compare source direct factory with native create(Level,EntitySpawnRequest):
  reason unobserved, ignoreChecks selects raw allocation, no spawn/finalization.
- [x] Implement direct/callable raw factory with unchanged nullable result and
  exceptions; custom EntityType overrides remain explicit unsupported boundaries.
- [x] Compare/adapt all four source moveTo snap overloads and native/source overrides,
  super calls and callable forms. Independent real constructed entity verifies
  block bottom-center, old position/rotation reset, unchanged motion and counts.
- [x] `compatibilityTest nativeCompatibilitySmoke compileLaunchJava` passed
  (`/tmp/lms-entity-allocation-position-check.log`). The factory probe uses actual
  native create over an isolated type/factory and supplies a constructor-only
  level double; it does not fake a spawn or world lifecycle.
- [x] Selected audit: 4323 classes, 1134 groups / 5709 sites
  (`/tmp/lms-entity-allocation-position-audit.log` and `.tsv`). Prior raw create
  and position-snap findings disappeared; inventory is not gameplay acceptance.
- [x] Runtime108 loaded the world and completed the previously failing saved
  projectile without an allocation/position exception. Actual spawned-mob counts
  were not measured; fresh throws completed separately. Later block ignition
  crashed on GameEvent/entity-renderer boundaries and cleanup remains pending.
