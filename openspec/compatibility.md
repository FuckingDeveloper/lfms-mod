# Compatibility Model

LFMS reports compatibility as progressive levels.

| Level | Name | Meaning |
|---|---|---|
| L0 | DETECTED | Legacy artifact is recognized and described. |
| L1 | LOADABLE | Required classes can be transformed/resolved sufficiently for loading. |
| L2 | INITIALIZED | Legacy mod lifecycle reaches successful initialization. |
| L3 | PLAYABLE | A world can be entered and the agreed basic blocks/items/entities operate. |
| L4 | FUNCTIONAL | The mod's agreed core gameplay functionality operates. |
| L5 | FULL | Rendering, networking, worldgen and required integrations for the declared target are functional. |
| L6 | VERIFIED | The declared compatibility test suite passes. |

Compatibility MUST be reported per legacy mod version, legacy platform/profile and LFMS target version.

Unsupported integrations MUST be reported separately and MUST NOT be silently counted as working.

## 01.000.00 acceptance target

On Minecraft 26.3 + Forge 66, LFMS MUST discover an original, un-recompiled Forge 1.19.2 mod JAR, select the correct profile, apply required transformations/runtime adaptations, and reach an agreed playable subset in a world.

IC2 Classic 1.19.2 is the first reference artifact. Exact IC2 gameplay acceptance cases will be specified by later changes rather than embedded in core LFMS requirements.
