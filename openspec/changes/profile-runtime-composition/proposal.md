# Compose source profiles and target integrations

The user approved preparing LMS for additional source/target combinations.
The current implementation remains Forge1.19.2 -> Minecraft26.3 / NeoForge26.3.
This change implements existing version-axis boundaries without changing mod
semantics or claiming additional generations or multi-generation coexistence.

Introduce shared profile selection, typed session creation and common session
observation; use the same installed source catalog in prelaunch and runtime.
Move source-specific admission/diagnostics into the source integration. Keep
native event-bus attachment explicitly within the current target integration.
Expose the existing transformation sequence as an ordered, named pass pipeline,
preserving source-identity, namespace-projection and post-projection boundaries.
Extract proven phase-staged registration queue semantics into a Java-only typed
contract; native mutation remains in the tested target adapter.

Unknown sources stay diagnosed as unresolved. Ambiguous selection, duplicate
profile IDs, missing target bindings/contracts and invalid pass order fail
explicitly. No class is initialized to select a profile. Only the current source
binding is installed; mixed source-generation runtime admission remains an
explicit gap until shared dependency/domain ordering is designed.

Verify independent selectors/factories, pass order/failure, partial registration
flush and late registration, then existing compatibility/native suites,
dependency-complete audit and fresh native Quick Play/MCP content interactions.
Preserve original dirty changes and original legacy artifacts. Do not add a new
loader, speculative IR, reflective universal target JAR or placeholder profiles.
