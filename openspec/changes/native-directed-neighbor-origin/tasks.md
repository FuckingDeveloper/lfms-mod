# Directed neighbor acceptance

- [x] Reproduce runtime75 command failure beside a source block and inspect stack.
- [x] Compare pinned source and target diode/observer producer implementations.
- [x] Preserve exact native single-notification arguments and producer position.
- [x] Applied-mixin tests for comparator, repeater and observer: queue capture,
  immutable position, context cleanup and nested exception restoration.
- [x] `./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava`
  passes: `/tmp/lms-directed-neighbor-check-2.log`. First test attempt selected
  a generated lambda rather than the handler; narrowed the reflection selector
  to the handler ABI and reran successfully.
- [x] Runtime76: replacing comparator at (-209,82,12) succeeds; no fresh runtime
  ERROR/FATAL/Caused-by. The missing-origin blocker is gone.

Functional comparator output is not closed: generator (-210,82,12) has4000EU
and eu_storage/flags0 in slot6, but the native comparator remains OutputSignal0.
Next causal boundary: source three-argument analog-output callback versus native
four-argument callback. Source IC2's similarly named custom directional helper
is not a Forge contract and must not be selected by fixture identity/name.
Original user dirty baseline preserved. Compatibility remains level6 plus
individually verified level7 paths; no full redstone claim.
