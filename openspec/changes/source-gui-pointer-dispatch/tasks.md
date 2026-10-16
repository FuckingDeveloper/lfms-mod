# GUI pointer family

- [x] Inspect runtime73, original screen/component/slider and pinned native event ABI.
- [x] Implement generic source click/release/drag entries, virtual/super calls and handles.
- [x] Preserve native event identity, SDL button intent, modifiers and double-click scope.
- [x] Verify inherited source/native/default dispatch, consumption and failure restoration.
- [x] Verify original component-screen entry ABI, original slider click/release/wheel and
  CarbonConfig GUI linkage regressions without modifying the input artifacts.
- [x] ./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava passes
  (/tmp/lms-gui-pointer-check-5.log). Source Widget type remains an unrelated residual
  signature boundary in the full original screen API; this is not full-mod linkage.
- [x] Runtime74 human accepts scrollbar dragging and adding the energy-buffer source.
- [x] Human accepts source retention after menu reopen; server comparator_data contains eu_storage/flags5 in slot6, plus max2 in slot3.

Control access is human-confirmed. Filter highlighting alone does not close item
filter contents/automation. Selected comparator direction is source GUI state;
source-side output configuration must persist independently. Runtime73 confirms
its native button path sends max2/slot3 and inventory side49/access3843.
No packet format or source comparator policy changed. Existing test generator
(-206,78,12) retained for runtime74 acceptance. Original user dirty baseline preserved.

## High-resolution wheel evidence

Runtime74 logged140 source wheel deliveries;136 had absolute delta below1.
No runtime ERROR/FATAL/Caused-by appeared. Pinned source MouseHandler passes
fractional GUI deltas; the original component screen itself converts its amount
to int before invoking slider handlers. Its wheel hit area is the scrollbar,
not the source-list rows.

Added an actual original ComponentContainerScreen -> original SliderComponent
fixture, called through the native GuiEventListener entry at the slider's hit
coordinates: ten events of -0.4 are consumed without moving; -1 moves one step;
+1 reverses it. Full nativeCompatibilitySmoke passes
(/tmp/lms-gui-wheel-evidence-1.log). No source artifact or production algorithm
changed for this diagnosis. This is a source integer-consumer behavior, not
failed callback delivery. Do not round/accumulate all source amounts: that would
violate the existing exact source-delta contract and alter continuous consumers.

Before the runtime75 human check, native discreteMouseScroll was false. Minecraft's
existing option can produce whole steps before callbacks. It was not changed
without the user's choice, because it affects all game scrolling. Any LMS-side
integer-step policy requires an explicit evidence-backed semantic decision.
Current compatibility remains level6 with individually accepted level7 paths.

Runtime75 cold restart confirms native-load and live native data both retain
source eu_storage/flags5 on slot6 and max2 on slot3, along with inventory access
settings. No new runtime ERROR/FATAL/Caused-by through original generator opening.
The generator remains available for the human native discrete-scroll
setting check. Normal production behavior is unchanged from5b46343; this follow-up
adds source behavior evidence and tests, not a wheel-normalization workaround.

- [x] Runtime75: the user enabled Minecraft's native discrete scrolling and
  confirmed that scrolling the comparator scrollbar works. Preserve this user
  setting. The latest runtime75 log scan has no ERROR/FATAL/Caused-by entries.

This closes the reported Mac scrolling diagnosis using the existing native
option. LMS continues to preserve fractional source deltas; no production
compatibility semantics changed. Together with runtime74, accepted paths are
dragging, scrolling with the native discrete option, adding a comparator source,
and retaining its configuration across menu reopen and cold restart. This does
not close full comparator output-circuit or configurable item-filter automation
coverage.
