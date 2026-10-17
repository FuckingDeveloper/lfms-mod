# Verification

- [x] Inspect source RenderSystem/GameRenderer and actual native extractor snapshots.
- [x] Inspect the complete source preview transaction; no source identity selector.
- [x] Verify transformed source calls and native immutable quad snapshots,
  pending/applied/pop order, baseline composition, balance/nesting/Z failures and
  exception identity/scope restoration. Unproven escaped/observed/callable shapes
  retain their original unsupported source calls.
- [x] `compatibilityTest nativeCompatibilitySmoke compileLaunchJava` passed:
  `/tmp/lms-modelview-check-3.log`.
- [x] `compatibilityAudit` passed: 4323 classes, 1162 grouped findings, 5820 sites;
  inventory only. `/tmp/lms-modelview-audit.tsv`.
- [x] Runtime97 loads the explicit world and MCP connects to its player/server.
  Opening the owned test workbench passes the former model-view failure; the
  source preview then fails on a synthetic accessor reading removed
  Screen.itemRenderer (f_96542_). Open/close/reopen and pixels remain BLOCKED.
  `/tmp/lms-container-families-runtime-97.log`,
  `run/crash-reports/crash-2026-10-17_14.01.47-client.txt`.
  The owned temporary workbench at (-197,79,12) remains for the next run.

Source corpus inspection: the same operations also occur in an item GUI renderer,
entity-selector GUI, world overlay and screen helper in other external fixtures.
Only closed local flows are projected; cross-helper/world/3D flows remain gaps.
