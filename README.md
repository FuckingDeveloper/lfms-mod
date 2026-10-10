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

Put candidate legacy JARs in the **development run directory** under `legacy-mods/`. At this milestone LMS only scans metadata and logs detected candidates; it does **not** execute, transform, or load the legacy mod.

See `openspec/` for requirements, architecture and change proposals.
