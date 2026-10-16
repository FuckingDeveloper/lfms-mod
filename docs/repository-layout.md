# Repository navigation

LMS has two independent version axes: a legacy source profile and a tested
modern target. Package ownership follows those axes; package names alone do not
prove that an implementation is target independent.

## Top-level directories

| Path | Responsibility |
| --- | --- |
| `src/main/java` | Mod bootstrap, common infrastructure, source profiles and target adapters |
| `src/main/resources` | Mixin configuration and pinned mapping/ABI/resource evidence |
| `src/main/templates` | Expanded mod metadata |
| `src/launch` | Separate early FML transformation library and service declarations |
| `src/compatibilityTest` | Independent compatibility fixtures, regression checks and audit tools |
| `gradle` | Wrapper and build scripts grouped by responsibility |
| `tools/evidence/forge1192` | Offline evidence generators; not production runtime code |
| `openspec/specs` | Normative contracts |
| `openspec/changes` | Proposals, implementation tasks and historical acceptance evidence |
| `docs` | Operational runbooks and current coverage/navigation |
| `run`, `build`, `.gradle`, `logs` | Local/generated state; not source or compatibility evidence to commit |

Do not move or delete a local test world, original mod JAR, cache or log as part
of source cleanup. A file being ignored does not make it disposable.

## Java ownership

All packages below are relative to `com.fuckingdeveloper.lms`.

```text
lms/
  LmsMod                         NeoForge bootstrap/integration entrypoint
  PreLaunchPlanCompiler          Launch-plan entrypoint
  analysis/                      Artifact and bytecode analysis
  classloading/                  Managed artifact ownership/loading
  discovery/                     Immutable legacy descriptors/discovery
  mapping/                       Mapping and symbol evidence
  transform/                     Shared transformation infrastructure
  compat/                        Compatibility planning and existing shared bridges
  mixin/                         Native integration hooks (current target)
  profile/
    forge1192/                   Implemented source profile and transformation rules
      runtime/
        advancement/ block/ capability/ chat/ color/ container/ data/
        energy/ event/ fluid/ item/ itemhandler/ lifecycle/ network/
        recipe/ registry/ render/ resource/ saveddata/ villager/ world/ worldgen/
  target/
    neoforge263/                  Explicit Minecraft26.3/NeoForge adapters
      render/ worldgen/           Target-owned rendering and generation integration
```

`profile.forge1192` owns selection, source declarations, source ABI identities
and the existing transformation passes. Its runtime subpackages own the source
facades/callback contracts by API family. A new generation must not be added by
copying this entire tree.

`target.neoforge263` owns explicitly target-specific implementations. Shared
meaning should be extracted only when proven operations justify it; this layout
does not introduce an empty semantic layer or a speculative universal IR.

Some source bridges still directly use native APIs. They remain target-aware
source bridges, not reusable target-independent services. Moving them does not
complete that architectural separation. Likewise `mixin` and `src/launch` remain
native integration points. Future extractions should follow the normative
[source/semantic/target contract](../openspec/specs/compatibility-architecture/spec.md)
and the [runtime vision](../openspec/specs/compatibility-runtime-vision/spec.md).

## Build ownership

`build.gradle` pins/configures the current target and applies:

- `gradle/lms-launch.gradle`: early launch library, installation, launch plan and
  client/server integration;
- `gradle/lms-source-evidence.gradle`: isolated Forge1.19.2 inspection workspace;
- `gradle/lms-verification.gradle`: compatibility fixtures, audits and native smoke.

Commands, source sets and output locations remain unchanged. Build scripts use
project-relative paths. There is no new multi-module build or dependency scope.

## Editing internal class identities

A Java package migration must update imports **and** ASM owners/descriptors,
reflection names, concatenated owner constants, launch-plan producers, mixin
references and assertion expectations. No old internal-package shim is retained:
these are LMS implementation names, not a stable third-party API. The existing
launch-plan task regenerates derived artifacts before a fresh run; original
legacy JARs are never rewritten.

Start new debugging work from the [API-family matrix](compatibility-api-coverage.md)
and [MCP runbook](runtime-debugging-mcp.md). Historical change checklists remain
historical evidence and are not a second current backlog.
