# LMS Project Specification

## Purpose

LMS (Legacy Mod Support) is a compatibility system for running legacy Minecraft Forge/FML mods on newer Minecraft/NeoForge versions with minimal or no modification of the original mod.

The initial legacy source scope is Forge/FML mods. The target loader for the 01.x architecture generation is NeoForge. Fabric, additional target loaders and arbitrary mod-loader compatibility are not required.

## Normative language

The terms MUST, MUST NOT, SHOULD, SHOULD NOT, and MAY are normative requirements.

## Product goals

- LMS MUST prioritize binary compatibility with original legacy mod JARs.
- LMS MUST NOT require recompilation or source modification where compatibility can reasonably be provided by transformation or runtime adaptation.
- LMS MUST preserve the original legacy artifact and produce derived/cache artifacts separately.
- LMS MUST use isolated version profiles where behavior cannot safely be generalized between Minecraft/Forge generations.
- LMS MUST keep mod-specific compatibility outside the core runtime.
- LMS SHOULD make compatibility failures deterministic and diagnosable rather than silently ignoring unsupported behavior.
- Source compatibility MAY be provided as a secondary development path; binary compatibility remains the primary product goal.

## Initial target

- Target runtime: Minecraft 26.3 / NeoForge 26.3.x (exact tested build pinned in the implementation change).
- Target Java runtime: Java 25.
- First legacy profile: Minecraft Forge 1.19.2.
- Initial reference mod: IC2 Classic 1.19.2.
- IC2 Classic is an integration target, not part of LMS core architecture.

## Long-term scope

LMS is intended to become a multi-generation compatibility engine rather than a fixed translator from Forge 1.19.2 to Minecraft 26.3.

The architecture MUST keep the legacy source generation and the modern target runtime as independent compatibility dimensions:

- **Source profiles** describe and normalize legacy Forge/FML generations.
- **Semantic compatibility operations** describe legacy intent without binding it to one target Minecraft release.
- **Target adapters** implement those operations on a tested Minecraft/NeoForge generation.

The canonical planned source generations are Forge 1.19.2, 1.16.5, 1.12.2, 1.7.10 and, later, 1.20.1. Neighboring patch/minor versions MAY share a profile family when their ABI and behavior can be proven compatible. Forge 1.6.4 remains a possible older-generation extension, but is not a priority milestone.

Support for these generations is not required for 01.000.00.

LMS MUST be maintainable across future target Minecraft/NeoForge releases. A future target such as Minecraft 27.3 MUST be treated as a new tested target adapter/runtime generation, not as an assumption that the 26.3 adapter remains compatible.

See `openspec/specs/compatibility-architecture/spec.md`, `openspec/specs/target-runtimes/spec.md`, and `openspec/specs/version-profiles/spec.md`.

## Explicit non-goals for 01.000.00

- Universal compatibility with every Forge mod.
- Support for every historical Minecraft/Forge generation.
- Fabric and additional target-loader compatibility.
- Transparent migration of old worlds/saves.
- Full compatibility with every optional third-party integration such as JEI.
- Treating transformed legacy code as trusted or sandboxed code.
- Hiding unsupported behavior or claiming compatibility that has not been verified.

## Trust boundary

Legacy mods are executable third-party Java code. LMS transformation MUST NOT be described as a security sandbox, malware scanner, or trust mechanism. Users remain responsible for the provenance of legacy artifacts.
