# Item projectile base family verification

- [x] Inspect actual source constructors/default/sync/getters/gravity and native storage/simulation.
- [x] Obtain user acceptance of native Minecraft26.3 base simulation.
- [x] Preserve wide native source-type projection and source-shaped managed constructors.
- [x] Define the actual native synced stack as empty without early default-item callback.
- [x] Preserve source raw/fallback/count/copy/custom-data and float-gravity overrides.
- [x] Adapt mapped hit overrides and native callback dispatch without content identity selectors.
- [x] Keep custom old entity-data callbacks fail-closed pending a general entity-data adapter.
- [x] Exercise all three real entity constructors, coordinates/owner offset, native ancestry,
  delayed default callback, source get/set, gravity, native hit dispatch and empty-item/owner storage.
- [x] Check exact nonempty item/codec operands; native item codecs cannot encode unbound
  global item prototypes in the headless probe. No global registry binding was fabricated.
- [x] Link the immutable external item-projectile subclass and its mapped overrides.
- [x] Run compatibilityTest/nativeCompatibilitySmoke/compileLaunchJava successfully.
- [x] Run selected-artifact transformed-linkage audit; former projectile base gaps absent.
- [ ] Verify real native flight, collision, item sync and nonempty-item persistence in a loaded world.
- [x] Runtime91 passed former deferred registry failures and reached source client setup.
  The next blocker was the shared screen-registration lifecycle (after native phase),
  before any world/player became available. MCP gameplay remains BLOCKED.

The level and living owner in the native probe are bounded constructor test doubles,
not a running world. Registration and callback tests do not establish flight or
serialization roundtrip. Native-only source float-gravity queries and source
custom legacy synced-data/NBT callbacks remain explicit unsupported boundaries.

Evidence: `/tmp/lms-menu-projectile-check-7.log` (native constructor/data tests),
`/tmp/lms-menu-projectile-audit.tsv` (4,323 selected classes; prior base-class gaps
absent), `/tmp/lms-container-families-runtime-91.log` and preserved crash report.
The selected static graph contains six source fixtures plus Carbon's actual nested
Primitive Collections dependency, not Wick's nested MixinExtras. Counts are not a
whole-mod compatibility measure. Last observed fresh milestone is registration4;
no successful world load or flight/persistence acceptance is recorded here.
