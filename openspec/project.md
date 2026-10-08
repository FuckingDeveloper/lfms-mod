# LFMS Project Specification

## Purpose

LFMS (Legacy Forge Mod Support) is a compatibility layer for running legacy Minecraft Forge mods on newer Minecraft/Forge versions with minimal or no modification of the original mod.

## Normative language

The terms MUST, MUST NOT, SHOULD, SHOULD NOT, and MAY are normative requirements.

## Product goals

- LFMS MUST prioritize binary compatibility with original legacy mod JARs.
- LFMS MUST NOT require modification of a legacy mod where compatibility can reasonably be provided through transformation or runtime adaptation.
- LFMS MUST use version-specific compatibility profiles where behavior cannot safely be generalized between Minecraft/Forge generations.
- LFMS MUST keep mod-specific compatibility outside the core runtime.
- LFMS SHOULD make compatibility failures diagnosable rather than silently ignoring unsupported behavior.
- Source compatibility MAY be provided as a secondary development path, but binary compatibility is the primary product goal.

## Initial target

- Target runtime: Minecraft 26.3 / Forge 66.
- First legacy profile: Minecraft Forge 1.19.2.
- Initial reference mod: IC2 Classic 1.19.2.
- IC2 Classic is an integration target, not part of the LFMS architecture. Core LFMS MUST NOT contain IC2-specific behavior.

## Long-term scope

LFMS is designed to support multiple legacy generations through isolated profiles, including 1.16.x, 1.12.2, 1.7.10 and 1.6.4. Support for those generations is not a requirement for 01.000.00.

## Non-goals for 01.000.00

- Universal compatibility with every Forge mod.
- Supporting every historical Minecraft/Forge generation.
- Hiding unsupported behavior or claiming compatibility that has not been verified.
