# Source distribution filtering

Managed Forge 1.19.2 definitions bypass the original RuntimeDistCleaner.
Dedicated-server baseline `/tmp/lms-gameplay-server-1.log` fails initializing a
common entrypoint because JVM verification resolves Screen from client-only methods.
The immutable source bytecode explicitly annotates those methods with OnlyIn(CLIENT).

The source profile must apply the verified Forge 1.19.2 RuntimeDistCleaner contract
before other rewrites, frame calculation and class definition, using the actual
physical target distribution. This is independent from logical server/world side.
Reject a class annotated for another distribution; remove annotated opposite-side
fields/methods and explicitly annotated interfaces. Unpack OnlyIns; remove class
side annotations after processing, as the source loader does. Recursively remove
synthetic lambda bodies referenced by removed methods through the source loader's
exact LambdaMetafactory.metafactory bootstrap. Do not strip arbitrary client
references, infer annotations from names, or bypass a common listener.

Runtime sessions bind the distribution explicitly. Unbound offline transformers
retain source members for distribution-neutral audits; tests must explicitly bind
when claiming distribution filtering. No global mutable distribution/cache.

Evidence: actual fmlloader 1.19.2-43.4.0 RuntimeDistCleaner and LambdaGatherer
bytecode, target FMLEnvironment.getDist(), original fixture annotations. Both target
sides retain ordinary JVM linkage and meaningful failures after this pass.

## Verification

Pending focused synthetic/source-artifact checks and actual dedicated server run.

Second dedicated run confirms the original Screen verification blocker is gone;
common source constructor reaches event registration, then links TextureAtlas
through LMS's combined event bridge. Isolate the existing client event family in
an explicitly lazy client holder. Common dispatch selects this holder only for
verified client event identities or native client-event types. Keep registration,
subscription capture/cleanup, priority, cancellation and lifecycle scope unchanged.
Guard client-resource replay registration by the physical distribution; common
setup and tag binding still execute on both distributions. This is side isolation
of existing operations, not skipping meaningful source registration callbacks.

`./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava` passed
(check-3). Both physical distributions have synthetic class/member/interface,
recursive lambda and idempotence checks; immutable CarbonConfig retains common
callbacks while removing its explicitly client-only methods/lambda.

Dedicated runs 3/4: all three legacy entrypoints INITIALIZED, real target server
26.3 reaches Done. Native console commands in the separate `run/world` test save
placed exact registered generator and chest IDs at previously-air positions.
Chest slot0 accepted 3 gold ingots via native item replacement and source NBT
readback. Generator accepted source fuel through native data merge; subsequent
source ticks changed fuel1000 ->640, storage0 ->3600 and active=true. This proves
source simulation/save callbacks, not normal GUI fuel insertion or a remote
client connection. Run4 native stop saved all dimensions and Gradle exited0.
Run3 was stopped by TERM and its native shutdown hook saved all dimensions;
Gradle143 is intentional process termination, not a successful Gradle task.

Fresh run4 timing: FML begins 02:15:41; all entrypoints 02:16:05; initial-data
bootstrap 02:16:13, final 02:16:22, Done 02:16:23. Cumulative source transformation time: CarbonConfig 1184ms/17 classes,
IC2 7554ms/989 classes, Iron Chests 181ms/53 classes. No new startup optimization is claimed.

Forward System.in to the existing Gradle runServer JavaExec task to retain its
native console (including stop/save); no new control API or automation service.

Run5 reopens the dedicated save: chest slot0 still has the same 3 gold ingots.
Generator loaded source fuel/storage and resumed simulation; exact last-tick
snapshot persistence is not claimed (see gameplay-batch-acceptance). Actual native
redstone dispenser equipped registered source bronze armor on an armor stand.
Temporary chest inventory was cleared; four fixture blocks restored to air;
tagged stand removed (no item drops found); chunk8,8 force-load removed. Native
stop saved all dimensions, Gradle task completed successfully. No remote client
login or full dedicated-server gameplay support is inferred from these subpaths.
