# Source scan metadata and shared class ownership

## Runtime evidence

Runtime122 passed the source Ingredient inheritance blocker and standard service
provider construction. JEI then failed with `plugins must not be empty` in its
original JeiStarter. Its original ForgePluginFinder enumerates
ModList.getAllScanData(), searches its annotation and calls Class.forName on the
announced classes. The current source ModList view delegates getAllScanData only
to native NeoForge, omitting every managed legacy artifact, including JEI itself.

The adjoining class-identity problem is also visible in implementation: the
legacy dependency gate includes mandatory mod dependencies only; the loader shares
a dependency owner only after that session is INITIALIZED. Sessions are created,
linked and initialized sequentially. Present optional API dependencies and owners
created later can therefore be absent or copied into another loader. Merely
adding annotation rows does not establish cross-mod plugin compatibility.

Pinned Forge43.4.0 ModList.getAllScanData enumerates its complete sorted mod file
list and caches distinct scan data. Its JavaFML container loads module classes;
ModLauncher10.0.9 ModuleLayerHandler.buildLayer uses one ModuleClassLoader for the
modules of a layer (the defineModules loader lambda returns that same loader).
That differs from the current explicit per-session visibility restriction.

## Decision accepted

User approved: a shared visibility domain for admitted artifacts of the same proven
source profile (currently only Forge1.19.2). Keep one defining owner per class/API,
with an indexed immutable artifact/class ownership graph prepared before any
managed class is defined. Mod dependency declarations still govern admission,
missing/optional dependency diagnostics and initialization order. Shared source
visibility also covers ordinary reflective plugin lookup. Native/JDK delegation,
source transformation and target-loader constraints remain authoritative.

This explicitly changes classloading/spec.md's current requirement that inter-mod
class visibility follow only the resolved dependency graph. It does not promise
cross-generation sharing, Fabric support, native FML membership or a security
sandbox. Duplicate/conflicting ownership is a diagnostic, never first-JAR-wins.
Identical copies of the same admitted bundled library may share a proven owner;
unknown overlapping artifacts must fail before publication.

Alternative: retain strict dependency-only visibility and design a separate,
explicit cross-owner provider-export/discovery contract. A scan annotation alone
must not silently authorize arbitrary sibling loading; provider identity and
owner routing would need a separately proven semantic boundary. This alternative
is more isolated but leaves general Forge reflective discovery unimplemented
until that boundary is ready. No mod-name or annotation-fixture selector is allowed.

## Independent guarantees in either design

- Build truthful scan metadata from immutable admitted artifacts without executing
  constructors or arbitrary classes. Preserve source annotation/class/member/value
  data and apply only the same proven type projection as source bytecode.
- Keep metadata discovery, reflective class identity, source lifecycle and actual
  plugin functionality as separate acceptance gates.
- Never register managed records as real native FML members, fake plugin objects,
  rewrite legacy inputs or suppress original plugin failures to advance startup.
- Preflight-rejected artifacts are not active domain members. Construction/runtime
  failures must update session/domain diagnostics and dependent initialization;
  already defined Java classes cannot be claimed unloaded or rolled back.
- Prepare ownership before definition; do not copy optional APIs into multiple
  loaders and later try to merge incompatible Class identities.
- Keep resource/service visibility deterministic and explicitly consistent with
  the selected class-domain policy. Release owner indexes/context bindings on close.
- Do not hold an unrelated caller-loader monitor while resolving another owner;
  verify actual JVM class identity, concurrency and circularity failure behavior.

## Verification plan

Two independent immutable mod-shaped JARs and a shared API, mandatory/present-optional
and missing dependencies, reverse plugin discovery, raw/projected annotation data,
Class.forName/assignability/constructor identity, duplicate library ownership and
conflict negatives, blocked/closed owners, concurrent lookup and cache disposal.
Then existing checks and fresh Gradle/MCP checks of real JEI discovery, actual
plugins and remaining first causal blocker. Discovering plugins is not proof of
recipe UI or optional-integration functionality.
