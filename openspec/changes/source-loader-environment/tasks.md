- [x] Identify runtime124's original static/instance launch query failure.
- [x] Compare exact pinned source static fields/getters and FML12.0.9 instance ownership.
- [x] Implement three read-only queries through installed target selection and shared source invocation catalog.
- [x] Run compile, full compatibility checks and native smoke.
- [ ] Verify original JEI client configuration in a fresh integration client.

`./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava` passed
(`/tmp/lms-loader-environment-check.log`). Native smoke transforms an independent
source-shaped immutable fixture and actually executes its direct calls/handles;
production, physical side and game directory equal the genuine active target
loader values. The actual loader remains required; no dummy environment is used.
