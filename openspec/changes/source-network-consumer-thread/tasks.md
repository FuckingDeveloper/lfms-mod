# Verification

- [x] Inspect actual source IndexedMessageCodec consumption and pinned target
  PayloadRegistrar default/wrapping behavior.
- [x] State source network-thread versus explicit game-thread enqueue semantics.
- [x] Use native NETWORK registration for the supported plain source consumer.
- [x] Extend the existing channel-scope probe to exercise the actual native
  registered client handler from a separate delivery thread and its explicit
  queued work, preserving provenance and previous-context checks.
- [x] `./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava`.
- [x] Verify original legacy content after a fresh development-client launch;
  distinguish initial chunk data, fresh placement and later neighbor changes.
- [x] Verify saved tin-cable initial transmission before approaching, connected
  rendering from outside interaction range, fresh ordinary placement, and model
  updates after neighbor removal/replacement. Remove temporary fixtures and restore
  player position/view/selection without changing inventory contents.
- [x] `git diff --check`.
- [x] Commit the verified family checkpoint.

Bounded gameplay checks pass; overall compatibility remains partial. A zero
immediate snapshot alone does not establish lost field delivery. The contribution
of the previous thread mismatch to the reported old-wire symptom remains unproven.
Some existing cable blocks have no saved block-entity records; the time/cause of
that loss is unknown and this change does not reconstruct their source state.
The pre-existing global-loot-modifier resource schema error remains. Energy
transport, source builder/login APIs and protocol negotiation beyond the
documented same-LMS subset are not established by these checks. Detailed logs
and world observations remain outside Git.
