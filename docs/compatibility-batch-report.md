# Batch admission inventory

Use production discovery, profile selection, lifecycle/registration planning and
compatibility-surface assessment without executing legacy entrypoints:

```bash
./gradlew compatibilityBatchReport --args='/absolute/path/to/legacy-mods /tmp/lms-batch.tsv' > /tmp/lms-batch.log 2>&1
```

The report contains every discovered mod descriptor, selected profile, first
assessment state and grouped source API boundaries with all affected mod IDs.
`SURFACE_READY_UNVERIFIED` means this bounded static surface has no known blocker;
it does not establish dependency admission, class linkage, initialization or
functional support. The graph is artifact-local and cannot prove arbitrary
reflection, cross-artifact dynamic dispatch or all later gameplay calls.

`PROFILE_UNRESOLVED` preserves metadata/evidence limitations. Do not infer the
Minecraft generation from a JAR filename. `API_BLOCKED` groups exact method
requirements; repeated call sites are not independent bug counts. Grouping lets
one proven family correction be applied to all affected artifacts.

The first method catalog family is lifecycle deferred work. The same immutable
rules drive source invocation transformation and admission. Source declaration
lookup uses hash-pinned Forge headers and raw managed bytes, preserves override
identity and caches positive/negative results only within one evidence graph.
Rebinding clears those caches. Target owners/descriptors come from the explicit
installed target adapter. Direct calls, inherited bound references and nested
callable constants are tested against the real native work queue.

Other existing compatibility families still use the legacy assessment list.
Migrating them requires their actual implementation/evidence, not blanket
approval of every name that happens to link. Config registration, display-test
negotiation and event-network channels remain explicit separate gaps.

Run compile/focused/native checks before a fresh client. This CLI is an inventory,
not a Minecraft automation framework; game checks use the existing MCP tools and
workflow in [runtime debugging](runtime-debugging-mcp.md). Keep generated TSV/logs
outside Git.
