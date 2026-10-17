- [x] Confirm runtime127 world/player readiness and absence of the previous runtime126 error.
- [x] Verify saved inventory/position recovery and removed owned test blocks persisted.
- [x] Inspect original observer's exact class-token and enabled-state contract.
- [x] Inspect pinned source event and native sealed event/configuration/login ordering.
- [x] Agree source post-login readiness versus immediate native timing.
- [x] Implement the approved event view and dispatch/lifetime policy.
- [x] Verify independent event/lifecycle fixtures and real original JEI notification/start transition.
- [ ] Accept complete JEI plugin startup and recipe UI after the creative-enumeration boundary.

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

## Runtime128

The fresh explicit-world launch reaches a real local player and integrated server.
Original JEI logs LoggingIn, then the source-owned LegacyTagsUpdatedEvent, then
native RecipesReceivedEvent, and transitions ENABLED -> JEI_STARTED / Starting JEI.
This establishes the approved real initial-notification ordering and exact type
identity; it does NOT establish complete JEI startup. The first causal plugin
failure is now original ItemStackListFactory reading removed CreativeModeTab.TABS
(`f_40748_`). JEI catches that LinkageError itself and later fails on missing vanilla
crafting category. The category NPE is secondary. The original source-owned atlas
also completes actual GPU upload (`Created: 256x256x0 jei:...gui.png-atlas`).

MCP confirms runtime128 world/player readiness and the recovered inventory still
matches its original baseline. No new fixture was created. Complete JEI ingredient
listing/categories/UI and actual source seating remain unverified. Client recipe
manager access remains the pre-existing integrated-server-only scope.
