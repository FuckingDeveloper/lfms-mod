# Verification

- [x] Confirm immutable Iron Chests item factories call source Properties.tab
  inside DeferredRegister suppliers, without a legacy setRegistryName accessor.
- [x] Capture membership at the actual native Item constructor boundary.
- [x] Native FML smoke verifies hook application, real Properties-key accessor,
  property reuse/later mutation and unaffected native properties.
- [x] `./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava`
  passed; see local `/tmp/lms-creative-namespace-check.log`.
- [x] Runtime 28 entered the existing world without a new causal exception.
- [ ] Observe creative inventory population and source Iron Chests entries.
  MCP has no creative-screen activation method; exact human test requested.

Overall observed baseline remains level 6 with earlier level-7 gameplay subpaths.
This checkpoint verifies membership capture; it does not close rendering gaps.
