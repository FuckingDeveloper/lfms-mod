# LMS — Legacy Mod Support

Binary-first compatibility system for legacy Minecraft Forge/FML mods on modern NeoForge.

**Target:** Minecraft 26.3, NeoForge 26.3.0.58-beta, Java 25.  
**First legacy profile:** Forge 1.19.2.  
**Reference mod:** IC2 Classic 1.19.2.

## Development

Use Gradle 9.2.1 or the official NeoForge 26.3 MDK wrapper.

```bash
gradle runClient
gradle runServer
gradle build
```

Put candidate legacy JARs in the **development run directory** under `legacy-mods/`.

LMS is under active compatibility development. Discovery, managed legacy loading, bytecode transformation and runtime adaptation exist, but compatibility is incomplete and subsystem support must be judged by verified compatibility milestones rather than by startup alone.

See `openspec/` for normative requirements, architecture and change proposals. In particular:

- `openspec/project.md` — product scope and normative goals;
- `openspec/specs/version-profiles/spec.md` — legacy source generations/profile families;
- `openspec/specs/compatibility-architecture/spec.md` — Source Profile -> semantic operations -> Target Adapter architecture;
- `openspec/specs/compatibility-runtime-vision/spec.md` — long-term Legacy Minecraft Mod Compatibility Runtime vision, including loader-neutral semantics and future Fabric/multi-generation possibilities;
- `openspec/specs/target-runtimes/spec.md` — maintaining LMS across future Minecraft/NeoForge targets;
- `openspec/specs/compatibility-verification/spec.md` — testing and compatibility evidence;
- `AGENTS.md` — mandatory repository rules for autonomous coding agents.
