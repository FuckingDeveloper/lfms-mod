- [x] Inspect exact source manager bytecode and target manager/interface sources.
- [x] Implement source codec and target selection/order boundaries.
- [x] Verify focused and actual native manager checks.
- [x] Verify actual legacy loot behavior and clean schema diagnostics in dev client.

Runtime48: ordinary Quick Play loads Новый мир with MCP Fabric 0.5.1+26.3,
Minecraft 26.3 / NeoForge 26.3.0.58-beta. No ERROR/FATAL/Caused-by or old global
list-schema diagnostic appeared through the gameplay check. No server /reload.
Native loot command with an actual short-grass BLOCK_STATE executes the original
legacy modifier: 51 no-tool trials insert 9 ic2:hemp_seeds and 4 wheat seeds into
a temporary barrel. After clearing only that test barrel, 30 shears trials insert
30 short grass and zero hemp seeds. This proves observed modifier behavior, not
all loot modifiers/codec shapes or arbitrary source generation support.

Focused checks cover replace, repeated-entry last order, unlisted source exclusion,
native resources/default-priority group ordering, fail-fast missing decoded values,
a source-shaped binary fixture's actual native interface codec/apply dispatch,
and actual mixed-in native manager prepare/apply with an empty source selection.
`./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava` PASSED;
`git diff --check` PASSED. Logs: /tmp/lms-p1-check-48.log and
/tmp/lms-container-families-runtime-48.log (not committed).

Three temporary blocks restored to their original air states; test barrel items
cleared before removal. Player position, selected slot and inventory unchanged.
Client remains running in the user's test world.
