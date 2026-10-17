- [x] Inspect runtime120 first causal failure and actual ServiceLoader call.
- [x] Read classloading dependency-visibility and lifecycle owner contracts.
- [x] Verify immutable owner/dependency/parent resource enumeration and closure.
- [x] Verify genuine JDK service discovery and scoped context/failure restoration.
- [x] Compile and pass compatibilityTest/nativeCompatibilitySmoke/compileLaunchJava.
- [x] Launch runtime121 through Gradle with the explicit test-world directory.
- [x] Runtime121: original service descriptor/provider found. Provider construction
  now reaches the source Ingredient subclass and fails on native final inheritance.

Focused evidence: /tmp/lms-managed-service-check.log. Tests exercise genuine
JDK provider lookup, encoded JAR paths, dependency visibility, context/failure
restoration and released binding/closed-resource behavior. Runtime121 confirms discovery; full provider functionality remains blocked
by the independent ingredient inheritance family.
