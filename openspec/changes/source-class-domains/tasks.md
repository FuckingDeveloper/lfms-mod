- [x] Identify runtime122 original empty-plugin failure.
- [x] Inspect source metadata/reflection use and current LMS scan/owner lifecycle.
- [x] Inspect pinned Forge ModList/JavaFML and cached ModLauncher layer construction.
- [x] Agree shared source-domain versus strict dependency/export policy.
- [x] Update the normative classloading and related source/resource contracts.
- [x] Implement truthful scan metadata and the selected owner/visibility boundary.
- [x] Verify independent discovery/identity fixtures and original plugin discovery/callback behavior.
- [ ] Extend admission-negative fixtures for mandatory/present-optional/missing dependency cascades.
- [ ] Accept actual JEI recipe UI and source seating gameplay after startup blockers.

## Independent verification

`./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava` passed
(`/tmp/lms-source-domain-final-check.log`). The new immutable two-owner fixture
checks ordinary caller-loader reflection, shared API/constructor identity,
standard Java ServiceLoader and cross-owner resources, 100 concurrent lookups,
whole-library deduplication, top-level/library collision negatives, isolated
domain exclusion, immutable projected class annotations and blocked/closed owner
behavior. Real JEI acceptance and remaining causal blocker are pending runtime123.

## Runtime123

The prepared Forge1.19.2 domain admitted seven source mods and nine artifacts.
All seven original entrypoint constructors completed; IC2 logged its JEI plugin.
The previous `plugins must not be empty` failure disappeared. The next first
causal failure is the original JEI configuration serializer calling removed
ChatFormatting.isColor (`m_126664_`) during client reload-listener registration.
World, recipe UI and seating acceptance remain BLOCKED; pending fixture recovery
is retained outside Git. The source metadata primitive-array fixture additionally
verifies defensive copies without replacing the original annotation value type;
circular cross-owner inheritance produces ClassCircularityError.

Runtime125 strengthens discovery acceptance: original PluginCaller sends the
ConfigManager to actual CarbonConfig and IC2 plugin instances alongside JEI's
own plugins. This verifies cross-owner reflective discovery, assignability,
construction and a real original callback. It does not establish recipe UI or
full optional-integration functionality.
