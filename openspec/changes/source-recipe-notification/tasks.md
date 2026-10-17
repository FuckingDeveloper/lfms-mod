- [x] Inspect runtime125 class-token failure and original source observer uses.
- [x] Compare pinned source and exact target event/dispatch contracts.
- [x] Define timing identity separately from source full-recipe data semantics.
- [x] Project all bytecode/scan type surfaces and reject source payload/construction.
- [x] Run compile, compatibility tests and native smoke.
- [x] Verify original observer registration and next causal blocker in runtime126.

`./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava` passed
(`/tmp/lms-recipe-notification-check.log`). An independent immutable source fixture
links its stored class token, class literal, cast, typed callback, lambda, method
handle and nested dynamic constant to the actual native event. Real native bus
delivery preserves event instance/getClass identity; generic signatures and source
scan annotation types agree. Source payload calls/handles/nested handles,
construction and subclassing fail before execution. This does not establish
full client recipe synchronization or JEI recipe UI functionality.

Runtime126 loads the explicit existing world and MCP confirms an integrated
server, local player, dimension and original source content. Original JEI logs
receipt of the genuine native RecipesReceivedEvent. The previous class-token
failure is absent. The next causal failure is IC2's original recipe-wiki callback
asking for NativeView.getItems on a native custom ingredient; the existing view
only admits CustomDisplayIngredient. It is reported by native recipe payload task
handling; the client remains connected. JEI full startup/recipe UI are not yet
accepted. No source getter was replaced with a dummy recipe manager.

The old test chair/support were read, verified as the owned fixture and removed;
there were no nearby seat entities. Slot21 was restored to slot4, full inventory
matched the saved baseline, and player position/vitals/XP were verified restored
before stopping the dev JVM. Persistence will be checked on the next world load.
