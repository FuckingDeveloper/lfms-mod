# Source item cooldown verification

- [x] Inspect runtime99 first causal addCooldown(Item,int) linkage failure.
- [x] Inspect source base add/remove/query/percentage/tick hooks and exact native
  Identifier groups, stack queries and ServerItemCooldowns packet callbacks.
- [x] Adapt proven source base invocations/callable handles through installed target
  binding; reject custom managers and conflicting native default groups explicitly.
- [x] Native source-shaped fixture verifies four operations, method handle,
  fractional timing, replacement, expiry, zero duration, distinct item keys,
  custom-manager rejection and immutable artifact bytes.
- [x] `compatibilityTest nativeCompatibilitySmoke compileLaunchJava compatibilityAudit`
  passed (`/tmp/lms-cooldown-check-3.log`). The default audit is a bounded inventory
  of 3829 classes, not complete gameplay acceptance.
- [x] Selected integration-artifact audit including TNT Expansion: 4323 classes,
  1161 grouped findings / 5815 sites; no remaining ItemCooldowns member finding
  (`/tmp/lms-cooldown-selected-audit.log` and `.tsv`).
- [x] Runtime100: world and MCP capability gates passed; actual `use_item` on
  original Coral TNT Dynamite returned source success. Server crash stack proves
  original entity creation, native movement/collision and original hit/detonation
  callback were reached. Former cooldown error is absent.
- [x] Next causal blocker: static DamageSource.explosion(LivingEntity), SRG
  m_19373_, in BlastEngine.applyToEntities. Server report
  `crash-2026-10-17_14.25.08-server.txt`; source entity type tntexpansion:dynamite,
  id 485, position (-160.40,63,45.73). Full explosion behavior remains blocked.

Runtime100 test cleanup pending after crash: no inventory mutation; player was
teleported to sky for the use test and must be restored to
(-157.16386352530708,63,27.778206510772335), yaw 8.208834/pitch -12.375409,
selected slot 7. Source item remains the user's Coral TNT Dynamite (creative).
Owned workbench at (-197,79,12) still awaits visual test/removal. No projectile
absence or visual correctness is inferred from the early empty MCP queries.

Custom source manager callbacks/super calls and native default group conflicts
remain explicit gaps. No projectile flight, collision, rendering or explosion
acceptance is inferred from a cooldown test. Workbench/creative visual acceptance
of the separately committed static-model family remains pending.
