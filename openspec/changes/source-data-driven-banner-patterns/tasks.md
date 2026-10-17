- [x] Verify pinned source banner registry/hash/texture and pattern-item contracts.
- [x] Verify native codec, data-registry, delayed pattern component and loom item tag.
- [x] Obtain approval for native data loading and pre-setup factory materialization.
- [x] Preserve source declaration/holder identity and explicit early/late/duplicate boundaries.
- [x] Implement target low-priority generated definitions and native pattern items.
- [x] Add immutable independent source binary and native codec regression checks.
- [x] Pass compatibilityTest/nativeCompatibilitySmoke/compileLaunchJava
  (`/tmp/lms-banner-data-check-3.log`). Initial checks caught a pre-validation
  composition side effect and a malformed fixture descriptor; both corrected.
- [x] Audit selected immutable inputs:4355 classes,1113 grouped findings/5671 sites
  (`/tmp/lms-banner-data-audit.tsv`). Inventory only, not compatibility coverage.
- [x] Runtime114/115: Fetzi INITIALIZED; subsequent unrelated resource/parser
  and empty-renderer blockers were fixed in separate checkpoints.
- [x] Runtime116: explicit world opened with MCP readiness gates. Native pack
  published five patterns plus the loom item tag. Native banner component parsing
  accepted all five real registry identities (sunburst/sakura/fish/flower/bamboo).
  Original flower item was observed in inventory; native predicates confirmed its
  exact delayed pattern tag component and membership in minecraft:loom_patterns.
  Both tagged test stacks were removed. No ERROR/FATAL/causal exception occurred.
  Full loom application remains UNVERIFIED: exposed tools cannot select its
  pattern/output widgets. Source renderer registration succeeds; source seats
  were not gameplay-accepted (native summon rejected the attempted fixture).
- [ ] Verify bounded actual loom application and native registry reload behavior.
- [ ] Independently verify source factory failure/null/subclass rejection before publication.

Explicit gaps: old banner hash-to-native-holder lookup, old banner save migration,
source BannerPattern subclasses, early cross-registry reads, arbitrary source
pattern-item subclass callbacks. Native data-pack overrides remain authoritative.
