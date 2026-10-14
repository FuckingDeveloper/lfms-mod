# Target Platform Specification

## Scope

LMS 01.x is a native NeoForge mod targeting Minecraft 26.3 and Java 25. Legacy input artifacts are Forge/FML mods, starting with Forge 1.19.2.

## Requirements

- LMS MUST distinguish source legacy loader (Forge/FML) from target loader (NeoForge).
- LMS MUST NOT assume legacy Forge and target NeoForge APIs or lifecycle are binary compatible.
- The build MUST pin an exact NeoForge 26.3.x version rather than use a floating dependency in reproducible release builds.
- Development MUST support NeoForge client and dedicated-server runs.
- The initial toolchain SHOULD use the officially supported NeoForge development tooling; the selected toolchain/plugin versions MUST be pinned and recorded in the implementation change.
- LMS MUST report the actual NeoForge build used in diagnostics.
- The 01.000.00 release gate MUST include NeoForge client and dedicated-server verification.

## Boundaries

Support for Forge as a *target* runtime is not required in 01.000.00. A future target-loader adapter MAY provide it without changing the legacy artifact/profile model.
