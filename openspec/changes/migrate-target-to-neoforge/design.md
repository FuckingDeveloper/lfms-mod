# Design: Target NeoForge

LMS separates legacy-source compatibility from target-runtime integration.

```text
Forge 1.19.2 legacy JAR
  -> discovery + profile + mapping + transformation
  -> LMS compatibility runtime
  -> NeoForge 26.3 target adapter
  -> Minecraft 26.3
```

Bootstrap and target-native registration/event/networking integration belong to the NeoForge adapter. Legacy Forge behavior is translated by the legacy profile and LMS runtime.

Initial development uses NeoForge's documented Gradle tooling. The bootstrap implementation change must record the exact tested NeoForge and plugin versions, with working client/server tasks.

Compatibility reports include both legacy loader/profile and target NeoForge version.
