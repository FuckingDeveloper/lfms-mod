# Verification

- [x] Identify runtime106 missing-data cause and compare source/native constructors.
- [x] Implement proven callback/builder projection and explicit unsupported shapes.
- [x] `compatibilityTest nativeCompatibilitySmoke compileLaunchJava` passed
  (`/tmp/lms-explosion-verified-check.log`). Real native entity construction
  covers parent/child and named/SRG hooks, inherited fuse, local slots, native
  dirty storage, duplicate rejection and unsupported live reads. The existing
  generic override projection invokes the source blast callback through patched
  NeoForge's protected virtual hook; no extra dispatcher was introduced.
- [x] Runtime108 source block ignition constructed and tracked PrimedExpansionTnt;
  prior missing-data exception disappeared. Next causal blockers are GameEvent
  holder delivery and immediate EntityRenderer extraction, not data definition.
  Client initial data values/persistence are not claimed from construction alone.
