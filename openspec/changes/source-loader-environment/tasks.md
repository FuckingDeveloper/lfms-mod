- [x] Identify runtime124's original static/instance launch query failure.
- [x] Compare exact pinned source static fields/getters and FML12.0.9 instance ownership.
- [x] Implement three read-only queries through installed target selection and shared source invocation catalog.
- [x] Run compile, full compatibility checks and native smoke.
- [x] Verify original JEI client configuration in a fresh integration client.

`./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava` passed
(`/tmp/lms-loader-environment-check.log`). Native smoke transforms an independent
source-shaped immutable fixture and actually executes its direct calls/handles;
production, physical side and game directory equal the genuine active target
loader values. The actual loader remains required; no dummy environment is used.

Runtime125 passes original JEI ClientConfig construction. Original PluginCaller
executes ConfigManager callbacks for actual JEI, CarbonConfig and IC2 plugins.
The next first causal failure is StartEventObserver.<clinit>: its required-event
class-literal set still references source RecipesUpdatedEvent. The existing timing-
only handler adapter rewrites unused handler parameters, but does not cover this
class-token/registration identity family. Native RecipesReceivedEvent has a
recipe-type subset and recipe map; data-consuming source handlers must remain a
separate semantic boundary. No world is loaded and no gameplay acceptance is
claimed. The pending chair/player recovery remains recorded outside Git.
