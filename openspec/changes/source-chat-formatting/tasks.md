- [x] Inspect runtime123 first causal formatting serializer failure.
- [x] Verify pinned source named bytecode, joined.tsrg and client.txt.
- [x] Inspect exact target enum and native named TextColor RGB mapping.
- [x] Implement source query/lookup/filter family through shared catalog/traversal.
- [x] Verify compile, independent family checks and native smoke.
- [x] Observe original JEI serializer passing in a fresh client.

`./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava` passed
(`/tmp/lms-formatting-domain-final-check.log`). Queries cover all22 actual enum
values and all four filter combinations. Every named/SRG rule checks direct,
handle/nested constants and wrong-owner/opcode negatives. CODEC/interface identity
and actual JEI recipe UI are not claimed.

Runtime124 passes the previous original ChatFormatting serializer failure and
reaches JEI's subsequent ClientConfig construction. The new first causal blocker
is the old static FMLLoader.isProduction invocation targeting a native instance
method. The original formatting serialization is observed; recipe UI/world are
still blocked. The static audit now inventories3829 classes,1167 grouped gaps and
6787 sites; these counts are unresolved inventory, not support coverage.
