# Verification: block-data native storage boundary

## Automated evidence

- `./gradlew compileJava compileCompatibilityTestJava compileLaunchJava` passed
  after updating the explicit layout callers (`/tmp/lms-storage-compile-2.log`).
- `./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava` passed
  (`/tmp/lms-storage-check-1.log`). Existing once-only native save/load/update,
  inheritance/default/super, capability, container and failure-scope suites pass.
- Additional native checks preserve input/output root identity, registry lookup,
  serialization ops and diagnostic reporter identity; rebinding does not modify
  the previous output root. Lookup-free native output remains valid, while input
  construction without lookup, missing output, foreign contexts and unsupported
  backend implementations fail explicitly. Independent callback bytecode tests
  nested direct-save success/failure and restoration of the exact outer context.
- Synthetic layout tests now verify the emitted native bridge owner as well as
  descriptors. Actual native ABI tests resolve that owner and its entrypoints.
- `./gradlew compatibilityAudit --args='/tmp/lms-api-family-audit-inputs-khbwek71 /tmp/lms-storage-audit.tsv'`
  passed (`/tmp/lms-storage-audit.log`): 3412 classes, 1086 inventory groups,
  7253 sites. All 1087 TSV rows exactly equal the previous callback-layout audit.
  These inventory groups are not a count of known functional bugs.
- Tests/audit/client run sequentially; no live class output is rebuilt.

## Boundaries retained

The native/source facade supplies copied roots at native input boundaries and
original roots at output boundaries. The source retains direct-call identity,
Forge keys/capability ownership and scoped callbacks. Direct source update-tag
calls still use lookup-only context with default diagnostics and no inherited
save output. Trace logging lives at native entrypoints; direct source update-tag
calls no longer produce the redundant native-boundary trace.

The source runtime still uses the current BlockEntity/CompoundTag/HolderLookup
materialization and the existing native persistent-key projection. This change
isolates storage mechanics, not an arbitrary-generation NBT engine. No broader
version support, whole-mod support or new gameplay semantics are claimed.

Verification uses the complete worktree with existing uncommitted user changes;
those original dirty paths are preserved and excluded from this checkpoint.

## Fresh runtime88

Launched after completed checks/audit with
`./gradlew runClient -PlmsTestWorld='Новый мир'`; evidence in
`/tmp/lms-container-families-runtime-88.log`. MCP confirmed Minecraft26.3 /
NeoForge client, integrated server and one player; capabilities, dimensions,
agent brief and player state identified `sp:Новый мир`. The launch log correlates
with the pinned NeoForge runtime and configured Java25 toolchain.

Cold-loaded generator (-206,78,12) retained storage3610, handler side49/access3843,
comparator max2/slot3 and eu_storage flags5/slot6, max_fuel400. Actual use-item
interaction opened it, closed it and opened it again. Macerator (-203,78,15)
also opened. Each menu was confirmed through a no-match bedrock transfer,
transferring nothing. `data get block` exercised native serialization and showed
unchanged source state before/after. No test items or blocks were created.

Full structured inventory matches exactly before/after. Restored captured exact
player coordinates, yaw/pitch and selected slot. No ERROR/FATAL/leading Caused-by
through final runtime inspection. Native smoke intentional exception probes are
separate from this client log. Client is left running with menus closed.

Overall observed compatibility remains level6 plus previously bounded level7
paths. This is a refactor checkpoint, not acceptance of all IC2 behavior. F07
fluids remains the first deferred broad functional family. Native host/NBT
materialization and persistent-key projection are explicit remaining coupling.
