- [x] Identify runtime122 original empty-plugin failure.
- [x] Inspect source metadata/reflection use and current LMS scan/owner lifecycle.
- [x] Inspect pinned Forge ModList/JavaFML and cached ModLauncher layer construction.
- [x] Agree shared source-domain versus strict dependency/export policy.
- [x] Update the normative classloading and related source/resource contracts.
- [x] Implement truthful scan metadata and the selected owner/visibility boundary.
- [ ] Verify independent graph/discovery/identity fixtures and actual client behavior.

## Independent verification

`./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava` passed
(`/tmp/lms-source-domain-final-check.log`). The new immutable two-owner fixture
checks ordinary caller-loader reflection, shared API/constructor identity,
standard Java ServiceLoader and cross-owner resources, 100 concurrent lookups,
whole-library deduplication, top-level/library collision negatives, isolated
domain exclusion, immutable projected class annotations and blocked/closed owner
behavior. Real JEI acceptance and remaining causal blocker are pending runtime123.
