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
- [ ] Runtime74 actual scrollbar, wheel hit testing and source-list clicks.
- [ ] Human source addition/reopen acceptance and server comparator_data verification.

Control access is human-confirmed. Filter highlighting alone does not close item
filter contents/automation. Selected comparator direction is source GUI state;
source-side output configuration must persist independently. Runtime73 confirms
its native button path sends max2/slot3 and inventory side49/access3843.
No packet format or source comparator policy changed. Existing test generator
(-206,78,12) retained for runtime74 acceptance. Original user dirty baseline preserved.
