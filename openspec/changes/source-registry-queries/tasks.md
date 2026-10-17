# Verification

- [x] Inspect source and target defaulted/nondefaulted lookup contracts and class/interface boundary.
- [x] Specify twelve source read overloads; preserve independent default/optional/key semantics.
- [x] Twelve source read overloads, Registry/Mapped/Defaulted receivers, direct/handle/nested/SRG calls;
  actual defaulted/ordinary registry absence, optional/key distinction, reverse/ID identity and seeded random.
- [x] `compatibilityTest nativeCompatibilitySmoke compileLaunchJava` passed:
  `/tmp/lms-registry-query-check-3.log`. Regenerated the hash-pinned source headers
  with `generateSourceHierarchy`: `/tmp/lms-registry-source-evidence.log`.
- [x] `compatibilityAudit` passed: 4323 classes, 1162 grouped inventory findings;
  not a gameplay support claim. `/tmp/lms-registry-query-audit.tsv`.
- [x] Runtime95: explicit world loads; MCP accepts the existing source workbench
  and opens tntexpansion:tnt_workbench with 13 slots. Former registry lookup crash
  disappeared; source preview then fails on RenderSystem.m_157191_ (global source
  model-view PoseStack). Screen visual acceptance/reopen remain BLOCKED.
  `/tmp/lms-container-families-runtime-95.log`,
  `run/crash-reports/crash-2026-10-17_03.33.59-client.txt`.
- [x] Runtime96 cleanup: world loads without ERROR/FATAL; MCP verifies the
  temporary workbench, removes it and confirms air at (-197,79,12). Player
  position and selected slot are unchanged; restore original camera orientation.
  Client remains in the test world. `/tmp/lms-container-families-runtime-96-cleanup.log`.
