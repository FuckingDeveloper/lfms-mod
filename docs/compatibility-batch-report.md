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

The method catalog covers lifecycle deferred work, the proven source configuration family and source menu factories. The same immutable
rules drive source invocation transformation and admission. Source declaration
lookup uses hash-pinned Forge headers and raw managed bytes, preserves override
identity and caches positive/negative results only within one evidence graph.
Rebinding clears those caches. Target owners/descriptors come from the explicit
installed target adapter. Direct calls, inherited bound references and nested
callable constants are tested against the real native work queue.

Other existing compatibility families still use the legacy assessment list.
Migrating them requires their actual implementation/evidence, not blanket
approval of every name that happens to link. Source configuration registration uses native files and source ownership/cache
semantics; custom specs/list correction remain explicit boundaries. Display-test
negotiation and event-network channels remain separate gaps.

Regenerate source declaration evidence with the immutable pinned Forge 43.4.0
named patched JAR plus its FML core and Java FML JARs:

```bash
./gradlew generateSourceHierarchy -PlegacySourceJar=/absolute/forge-named.jar -PlegacyFmlCoreJar=/absolute/fmlcore.jar -PlegacyJavaFmlJar=/absolute/javafmllanguage.jar
```

The generator verifies all three hashes and rejects duplicate declarations. Loader
APIs are not assumed to live in the main Forge artifact.

Run compile/focused/native checks before a fresh client. This CLI is an inventory,
not a Minecraft automation framework; game checks use the existing MCP tools and
workflow in [runtime debugging](runtime-debugging-mcp.md). Keep generated TSV/logs
outside Git.

## Configuration checkpoint (2026-10-17)

TNT Expansion passes the bounded constructor surface and its actual constructor
initializes in runtime90 after source config registration is implemented. It then
fails deferred registry callbacks on the projectile base-class and menu-factory
families. This is direct evidence that `SURFACE_READY_UNVERIFIED` is not a promise
of full initialization or world load. JEI still has11 grouped API requirements,
Wick2; Fetzi remains profile-unresolved. See the
[configuration verification](../openspec/changes/source-config-registration/tasks.md)
for tests, runtime limits and the next causal blockers.


The projectile/menu checkpoint passed the former deferred registry blockers in
runtime91. The subsequent client-setup deferred task hits the existing source
screen-phase guard. Registry completion does not establish completed client
lifecycle, a menu or a loaded world. The menu-factory tests preserve the existing
source VANILLA_SET construction policy, original buffer/null identity and native
MenuType source-interface support. The user subsequently approved bounded native screen registration during actual
client setup. Runtime92 passed the previous screen-phase guard and reached entity
renderer creation in client resource reload. It then failed on missing source
RenderType.m_110458_(Identifier) in a model constructor. MCP connected without a
server/player; no world or functional menu/projectile milestone is claimed.
Native screen-phase regression checks verify direct/deferred timing, duplicate
preservation, common/late rejection and scope restoration. See
`/tmp/lms-screen-setup-check.log` and `/tmp/lms-container-families-runtime-92.log`.


## Rendering/query checkpoint (2026-10-17)

Runtime93 passed the missing texture-bearing factory after eight proven cutout,
armor and translucent overloads were verified, including outline flags and the
source-vs-target culling name change. Runtime94 also passed the entity-context
block service getter and loaded the test world. MCP verified the actual
TNT workbench registry key by set/readback; the first screen attempt exposed
source DefaultedRegistry lookup. Runtime95 passed that lookup and opened the
server-backed source menu with13 slots. Its original preview then failed on
RenderSystem.m_157191_(): source global model-view stack is not yet adapted to
GUI extraction. Acknowledging an open menu is not successful screen rendering.

Native read probes cover12 overloads on Registry/MappedRegistry/DefaultedRegistry,
including unknown-ID fallback, true optional absence, ResourceKey differences,
reverse/numeric identity, random selection and callable constants. All compile,
compatibility and native smoke checks pass. The selected-artifact audit contains
4323 classes and1162 grouped findings, an inventory rather than a bug count.
Source registry subclasses, immediate block drawing, custom entity submission,
projectile gameplay and the remaining GUI matrix boundary are not claimed solved.
Original mod artifacts remain unchanged.
