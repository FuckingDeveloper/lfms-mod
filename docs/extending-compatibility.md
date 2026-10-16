# Extending source and target combinations

Current executable scope remains Forge1.19.2 -> Minecraft26.3 /
NeoForge26.3.0.58-beta / Java25. These are extension boundaries, not claims that
other versions or mixed-generation sessions already work.

## Where the current path goes

1. `InstalledSourceProfiles` supplies the same explicit catalog to prelaunch and
   runtime. `SourceProfiles.resolve` selects from descriptor evidence and rejects
   ambiguity. It never instantiates a managed legacy entrypoint.
2. `SourceProfile` owns source evidence locations, prelaunch planning and typed
   session/transformer creation. Forge-specific compatibility/spec planning lives
   under `profile.forge1192`, rather than inside shared bootstrapping.
3. `Target263SourceBindings` composes the current native host with source runtime
   integrations. `RuntimeBootstrap` validates all selections/bindings/contracts
   before admitting sessions. The combination is logged as `LMS profile-binding`.
4. Source admission retains the existing dependency gates, source ordering,
   lifecycle diagnostics and native host attachment. Common `RuntimeSession`
   observation lets UI report state without knowing its concrete source class.
5. `TransformationPipeline` runs named passes in source-semantic, namespace
   projection and target-shape order. It preserves caller order and propagates
   failures; it does not load classes or replace the safe writer/hierarchy logic.
6. `StagedRegistrations<K,N,V>` owns Java-only registration intent and phase
   completion. The source registry bridge retains source value adaptations and
   scope checks. `Target263RegistrationAdapter` performs actual native mutation
   through RegisterEvent. Failed flush entries and their suffix remain pending.

## Adding a source profile

- Establish exact source ABI/mapping/lifecycle evidence and its immutable fixtures.
- Implement `SourceProfile<S>` with source-owned planners and a real session.
- Reuse proven passes/contracts where semantics match; retain generation-specific
  differences explicitly. Do not duplicate the entire Forge1.19.2 transformer.
- Add it to the explicit catalog and supply a tested binding in the target
  composition root. A missing binding/contract fails before managed admission.
- Verify selection, negative/ambiguous descriptors, source callbacks and bounded
  gameplay behavior. Multiple profiles in a catalog do not imply safe simultaneous
  execution; current runtime admission rejects mixed source generations.

## Adding a target

Build against the actual new runtime and follow the target upgrade specification.
Implement native adapters and source bindings for proven contracts; keep the old
adapter available for its tested build. Build selection remains explicit. There
is no reflection-based universal JAR, automatic version-range widening or
unverified fallback to the closest target.

## Remaining work

The Forge transformer still owns many implementation helpers; the ordered pass
composition is the first extraction boundary, not a completed decomposition into
176 independent subsystems. Many source bridges and their admission implementation
still use native types. Extract another semantic contract only when its tested
behavior justifies it. Fluids, source/native energy and rendering retain their
previously documented semantic gaps.

See [repository navigation](repository-layout.md), [coverage](compatibility-api-coverage.md)
and [normative composition](../openspec/specs/profile-composition/spec.md).
