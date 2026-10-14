# Container-screen state getter family — 2026-10-14

The user's 16:01 run confirms visual cable connections. Generator initialization
now passes subscriber discovery and fails at inherited getGuiLeft; the later
missing-button tick NPE follows the interrupted initialization. Overall observed
milestone remains 6; wire energy transport/persistence and full menu support are
not established by this run.

- [x] Inspect actual source Forge-patched AbstractContainerScreen bytecode and
  pinned target source: both getter groups read the same receiver fields.
- [x] Adapt all five proven getters, declarations, inherited/super calls and
  virtual/special callable references including nested constants.
- [x] Reject source target-name collisions and invalid override shapes.
- [x] Verify independent source fixtures and actual native screen state/dispatch.
- [x] Restore protected dimension mutability; test source writes after construction.
- [x] `./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava compatibilityAudit`.
- [ ] User verifies generator open/slots/close/reopen.
- [ ] User verifies wire world re-entry and energy separately from geometry.

Unproven source slot-color and old GUI rendering/input operations remain gaps.
Original legacy JARs and all pre-existing user changes remain preserved.

Final log: `/tmp/lms-screen-state-final.log`. Audit: 2798 classes, 1537 grouped
gaps; the old container-screen getters no longer appear as unresolved members.
The native screen fixture bypasses construction because a headless process has
no Minecraft singleton; it establishes field/getter/override semantics only.
The gameplay log is preserved at `/tmp/lms-generator-1601-full.log`.
