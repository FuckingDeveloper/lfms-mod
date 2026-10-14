# Tasks: Bootstrap Legacy Discovery

- [x] Create native Minecraft 26.3 / NeoForge 26.3.x LMS project skeleton.
- [x] Implement LMS bootstrap service.
- [x] Define legacy artifact descriptor and evidence model.
- [x] Define configured legacy-mod location.
- [x] Implement non-destructive JAR enumeration.
- [x] Parse available legacy metadata/manifest evidence.
- [x] Define version-profile contract.
- [x] Implement profile resolver.
- [x] Implement initial Forge 1.19.2 profile detection.
- [ ] Implement structured diagnostics/report output.
- [ ] Add unit tests for descriptor/profile resolution.
- [ ] Add integration fixture strategy without committing proprietary legacy mods.
- [x] Verify discovery against the IC2 Classic 1.19.2 reference JAR.
- [ ] Document observed limitations before closing the change.

## Verified reference artifact

IC2Classic-1.19.2-2.1.3.4.jar was discovered non-destructively as mod id `ic2`, Minecraft range `[1.19,1.20)`, and resolved profile `forge-1.19.2`. The reference JAR remains external to the repository.
