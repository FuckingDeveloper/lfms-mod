# LMS Project Specification

## Purpose

LMS (Legacy Mod Support) is a compatibility system for running legacy Minecraft Forge/FML mods on newer Minecraft/Forge versions with minimal or no modification of the original mod.

The current project scope is Forge/FML compatibility. The generic LMS name MUST NOT be interpreted as a commitment to Fabric, NeoForge, or arbitrary mod loaders in the 01.x architecture generation.

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

- Target runtime: Minecraft 26.3 / Forge 66.
- Target Java runtime: Java 25.
- First legacy profile: Minecraft Forge 1.19.2.
- Initial reference mod: IC2 Classic 1.19.2.
- IC2 Classic is an integration target, not part of LMS core architecture.

## Long-term scope

The architecture is intended to accommodate Forge/FML generations including 1.16.x, 1.12.2, 1.7.10 and 1.6.4 through independent profiles. Their support is not required for 01.000.00.

## Explicit non-goals for 01.000.00

- Universal compatibility with every Forge mod.
- Support for every historical Minecraft/Forge generation.
- Fabric/NeoForge compatibility.
- Transparent migration of old worlds/saves.
- Full compatibility with every optional third-party integration such as JEI.
- Treating transformed legacy code as trusted or sandboxed code.
- Hiding unsupported behavior or claiming compatibility that has not been verified.

## Trust boundary

Legacy mods are executable third-party Java code. LMS transformation MUST NOT be described as a security sandbox, malware scanner, or trust mechanism. Users remain responsible for the provenance of legacy artifacts.
