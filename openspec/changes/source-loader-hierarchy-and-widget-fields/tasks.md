# Source loader inheritance and widget state — 2026-10-14

The 16:53 generator run passes the previously blocked getters and Button
construction, then fails reading inherited active (f_93623_) through a source
ToolTipButton -> Forge ExtendedButton -> vanilla widget hierarchy. Field lookup
stops at the absent source Forge ancestor. The later null button tick error is a
consequence of interrupted initialization. Overall observed milestone remains 6.

- [x] Inspect source AbstractWidget fields, SRG/Mojang identity and pinned target
  storage/visibility/getter semantics. Inspect original Forge ancestor declarations.
- [x] Generate all 1133 source Forge loader class headers and fields from the
  pinned 1.19.2/43.4.0 named patched artifact, recording SHA-256 provenance.
- [x] Prefer actual managed bytes, then indexed source loader evidence, before
  existing runtime hierarchy fallback; retain JVM field lookup order and source hiding.
- [x] Preserve direct x/y raw storage after target visibility changes, without
  routing source fields through potentially overridden getters/setters.
- [x] Keep original receiver/return/value types on field handles and nested constants;
  keep field handles out of the SRG method-descriptor resolver.
- [x] Verify immutable source widgets through original Forge ancestors and eight
  field families, actual native state, hidden fields, indy/condy handles and rebinding.
- [x] `./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava compatibilityAudit`.
- [x] Preserve source declarations after namespace projection; index reverse aliases,
  reject ambiguous aliases and keep semantic replacement shims on their own runtime hierarchy.
- [ ] User checks actual generator open, slots, buttons, close/reopen.

The source evidence resource is metadata only; no source runtime classes are
defined or patched. Regeneration uses the repository wrapper:

```
./gradlew generateSourceHierarchy -PlegacySourceJar=/absolute/path/to/pinned-source.jar
```

Only the inspected SHA-256 source artifact is accepted by this generator. A new
source artifact requires explicit provenance/contract review. Unknown fields and
changed semantic operations remain unsupported, including removed widget texture
state and old immediate GUI rendering/input. Current source metadata need not
cover missing separate FML artifacts; missing evidence remains unresolved.

Logs: `/tmp/lms-widget-hierarchy-final.log`, `/tmp/lms-widget-hierarchy-audit.log`;
original gameplay evidence: `/tmp/lms-generator-1653-full.log`.

Final audit: 2798 classes, 1526 grouped inventory gaps. The inherited widget
activity/visibility/dimension/coordinate fields from this failure no longer
appear unresolved. The existing resource-pack data-flow rejection remains.
The reported gaps are an inventory, not functional support counts. Headless
fixtures establish bounded state/linkage semantics, not full GUI rendering/input.
