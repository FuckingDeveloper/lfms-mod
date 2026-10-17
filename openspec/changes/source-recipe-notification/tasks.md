- [x] Inspect runtime125 class-token failure and original source observer uses.
- [x] Compare pinned source and exact target event/dispatch contracts.
- [x] Define timing identity separately from source full-recipe data semantics.
- [x] Project all bytecode/scan type surfaces and reject source payload/construction.
- [x] Run compile, compatibility tests and native smoke.
- [ ] Verify original observer registration and next causal blocker in runtime126.

`./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava` passed
(`/tmp/lms-recipe-notification-check.log`). An independent immutable source fixture
links its stored class token, class literal, cast, typed callback, lambda, method
handle and nested dynamic constant to the actual native event. Real native bus
delivery preserves event instance/getClass identity; generic signatures and source
scan annotation types agree. Source payload calls/handles/nested handles,
construction and subclassing fail before execution. This does not establish
full client recipe synchronization or JEI recipe UI functionality.
