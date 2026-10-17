# Static accessor extension verification (2026-10-17)

- [x] Inspect runtime97 causal removed Screen.itemRenderer field read and complete accessor body.
- [x] Extend existing inherited-field proof to pure static/synthetic getters declared on
  source Screen subtypes with compatible source receivers; preserve getter class initialization.
- [x] Verify source null/class-initialization order, field hiding, monitor/body negatives,
  evidence graph rebinding, original artifact immutability and native scoped GUI view.
- [x] `compatibilityTest nativeCompatibilitySmoke compileLaunchJava` passed:
  `/tmp/lms-screen-static-accessor-check-2.log`.
- [x] Transformed-linkage audit passed (4323 classes; 5816 sites). Runtime98 loaded
  the test world and opened/closed/reopened the original 13-slot workbench without
  errors. Human visual check FAILED: block-item models are missing in both the
  workbench and creative inventory; this is a separate resource-definition gap.

Arbitrary/static getters with side effects, monitors or handlers remain unadapted;
virtual getter dispatch checks remain active. The original getter body is retained
for other unproven consumers; no Screen field or world renderer is fabricated.
