- [x] Confirm runtime127 world/player readiness and absence of the previous runtime126 error.
- [x] Verify saved inventory/position recovery and removed owned test blocks persisted.
- [x] Inspect original observer's exact class-token and enabled-state contract.
- [x] Inspect pinned source event and native sealed event/configuration/login ordering.
- [x] Agree source post-login readiness versus immediate native timing.
- [x] Implement the approved event view and dispatch/lifetime policy.
- [ ] Verify independent event/lifecycle fixtures and real original JEI start.

## Verification checkpoint

`./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava` passed
(`/tmp/lms-source-tags-final-check.log`). Independent typed gate checks cover
configuration, replacement, late stale publication, reconfiguration, once-only
login delivery and disconnect cleanup. A source-shaped immutable fixture checks
class/constructor/enum/registry/static-data identity, actual native subclasses,
explicit and annotated source subscriptions, source owner/namespace/priority,
manual source posting and unregister. A genuine native Connection over a Netty
EmbeddedChannel verifies deferred/ready delivery and disconnect behavior; actual
native login-packet mixin installation is checked in the transformed target class.
The fresh integration client must still establish actual login/JEI behavior.
