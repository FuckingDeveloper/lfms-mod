# Compatibility Model

LMS reports progressive compatibility levels.

| Level | Name | Meaning |
|---|---|---|
| L0 | DETECTED | Legacy artifact is recognized and described. |
| L1 | LOADABLE | Required classes/resources can be transformed/resolved sufficiently for loading. |
| L2 | INITIALIZED | Supported legacy lifecycle reaches successful initialization. |
| L3 | PLAYABLE | A world can be entered and the declared basic gameplay subset operates. |
| L4 | FUNCTIONAL | The declared core gameplay functionality operates. |
| L5 | FULL | All functionality declared in that mod/version's compatibility contract operates, including required rendering/network/worldgen/integrations. |
| L6 | VERIFIED | The declared automated/manual compatibility test suite passes. |

A level MUST NOT imply capabilities that were excluded from the declared compatibility contract.

Compatibility MUST be reported per legacy mod version, legacy profile, LMS version and target runtime.

## Capability states

Individual capabilities use explicit states:

- SUPPORTED: implemented and expected to work.
- PARTIAL: supported only for a documented subset.
- UNSUPPORTED: recognized but not implemented.
- BLOCKED: cannot proceed safely because a required capability is unavailable.
- NOT_APPLICABLE: capability is not used by the artifact.
- UNKNOWN: insufficient evidence.

Mandatory unsupported capabilities MUST block progression to a compatibility level that requires them. Optional integrations MAY degrade without blocking startup when the legacy mod semantics permit it.

## 01.000.00 release gate

01.000.00 is the first working prototype. Before branching `release/01.000.00`, the following MUST be true:

- LMS starts on Minecraft 26.3 / Forge 66 / Java 25.
- An original, un-recompiled Forge 1.19.2 reference JAR is discovered.
- The Forge 1.19.2 profile is selected from recorded evidence.
- Original JAR bytes remain unchanged.
- Required classes/resources pass through the LMS compatibility pipeline.
- Supported legacy lifecycle stages execute in defined order.
- The agreed reference-mod block/item registrations succeed.
- A new world can be created and entered.
- The agreed reference-mod blocks/items can be obtained and placed/used.
- The agreed reference-mod core machine subset operates.
- Networking required by that subset operates.
- Dedicated-server bootstrap is tested for the declared subset.
- A machine-readable compatibility report is produced.
- Cache reuse and invalidation are tested.
- Unsupported functionality is explicitly represented in diagnostics.
- IC2-specific behavior is absent from LMS core.
- Required regression tests pass.

The exact IC2 Classic gameplay subset MUST be defined by a dedicated reference-mod acceptance change before the release gate can be considered satisfied.

JEI completeness, migration of historical worlds and support for additional legacy generations are NOT release blockers for 01.000.00 unless explicitly promoted by a later accepted change.
