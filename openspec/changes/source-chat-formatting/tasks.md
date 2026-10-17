- [x] Inspect runtime123 first causal formatting serializer failure.
- [x] Verify pinned source named bytecode, joined.tsrg and client.txt.
- [x] Inspect exact target enum and native named TextColor RGB mapping.
- [x] Implement source query/lookup/filter family through shared catalog/traversal.
- [x] Verify compile, independent family checks and native smoke.
- [ ] Observe original JEI serializer passing in a fresh client.

`./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava` passed
(`/tmp/lms-formatting-domain-final-check.log`). Queries cover all22 actual enum
values and all four filter combinations. Every named/SRG rule checks direct,
handle/nested constants and wrong-owner/opcode negatives. CODEC/interface identity
and actual JEI recipe UI are not claimed.
