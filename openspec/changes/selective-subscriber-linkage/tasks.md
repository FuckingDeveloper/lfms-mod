# Selective source subscriber linkage — 2026-10-14

Overall observed milestone remains 6 (world load). The 11:33 user run reaches
native screen initialization. Its first cause is eager linkage of an unrelated
removed ItemRenderer signature during subscriber discovery; the subsequent
missing-widget tick NPE follows the aborted screen initialization.

- [x] Inspect exact source EventBus 6.0.3 and target bus 8.0.5 contracts.
- [x] Retain exact defined transformed class bytes in each managed loader.
- [x] Index public source methods without resolving their utility types; link every
  applicable event handler selectively through exact MethodHandle signatures.
- [x] Preserve instance/Class registration, inheritance/virtual dispatch, priority,
  cancellation, duplicate registration and explicit unsupported annotation conflicts.
- [x] Source registration/unregistration own native wrappers and event fanout;
  lambda registrations retain their original consumer identity for teardown.
- [x] Undo partially installed native wrappers and preserve the original error.
- [x] Use ClassValue metadata indexes and weak bus cleanup references rather than
  independent global references to disposable managed classloaders.
- [x] `./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava`.
- [x] Real source JAR fixtures reproduce failed getDeclaredMethods but successful
  supported handler linkage, inherited/static dispatch, cancellation, repeat/reopen,
  unregister isolation, lambda/fanout teardown and injected registration rollback.
- [x] `git diff --check`.
- [ ] User verifies actual generator screen initialization and interaction.

No fake ItemRenderer facade is introduced. GUI rendering/input remains a separate
semantic boundary. Cable persistence/initial update-tag callback migration is the
next batch; no claim of restored wire connectivity has been made.
Pre-existing 8-file tracked user changes and the untracked screen bridge remain
excluded from this checkpoint. Original integration artifacts remain immutable.
