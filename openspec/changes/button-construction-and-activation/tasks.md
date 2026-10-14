# Bounded source Button construction and activation — 2026-10-14

Preflight of the generator initialization path reveals four six-argument native
Button superclass constructions, including the comparator checkbox. These are
known structural blockers after the screen getter family, so the same user run
should not be spent rediscovering them. Source Button activation is no-argument;
native activation takes InputWithModifiers. Source checkbox overrides must still run.

- [x] Inspect actual source Button constructors/default activation and pinned
  native Button default narration, constructor and callback behavior.
- [x] Preserve all source constructor arguments and append native default narration
  only for proven direct superclass construction.
- [x] Preserve source activation via typed callbacks, native wrapper delivery,
  source parent dispatch and default base callback with original receiver.
- [x] Handle source virtual callable references through the checked semantic traversal.
- [x] Cache source press hierarchy metadata with transformer binding lifetime.
- [x] Verify actual target construction/activation with immutable source fixtures,
  including source Button and Forge ExtendedButton superclass paths.
- [x] `./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava compatibilityAudit`.
- [ ] User verifies generator initialization and actual interactions.

Source custom tooltip constructors, direct allocation of abstract native Button,
other unverified native superclass activation, source-super callable references and
immediate custom widget rendering are unproven boundaries. This checkpoint does
not claim input routing, hover, narration customizations or rendering equivalence.

Final log: `/tmp/lms-button-family-final.log`. Audit: 2798 classes, 1535 grouped
gaps. Four old six-argument Button superclass sites no longer appear unresolved;
the only transformation rejection remains the pre-existing resource-pack data-flow
boundary. Interactive client/world tests were left to the user.
