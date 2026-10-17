# Verification

- [x] Inspect pinned source NetworkEvent, EventNetworkChannel and native registrar.
- [x] Add distinct raw play payload and source event contracts.
- [x] Check immutable typed source binary and actual native codec.
- [x] Check shared listener state, buffer lifetime and source/deferred scopes.
- [x] Compile, compatibilityTest/nativeCompatibilitySmoke/compileLaunchJava and preflight.
- [x] Runtime119: six raw-channel constructor requirements admitted; JEI INITIALIZED.
  Wick and Fetzi INITIALIZED too. Later lifecycle/gameplay gates remain separate.
- [ ] Native packet send/receive acceptance; remote/login compatibility unverified.

Evidence: /tmp/lms-riding-raw-final-check.log; runtime119 log. Offline audit
covered3829 current input classes,1188 grouped findings/6829 sites. This differs
from previous dependency-expanded inventories and is not a coverage percentage.
Only the previously known custom-renderer and PackFinder transform failures remain.
