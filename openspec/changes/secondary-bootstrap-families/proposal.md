# Adapt secondary Forge 1.19.2 bootstrap families

CarbonConfig and Iron Chests expose reusable loader, registration, network and
input contracts missing from the existing source profile. Adapt verified source
operations against the pinned Minecraft 26.3 / NeoForge 26.3.0.58-beta ABI while
keeping both original binaries immutable.

The design preserves lazy registration and protocol callbacks, explicit network
direction, source ownership and named-key intent through SDL. Ordinary runtime
preflight distinguishes inactive data-generation paths from executable support.
Unsupported managed-container enumeration and container superclass persistence
remain explicit admission failures. Full configuration editing, chest gameplay,
raw GLFW scan bindings and source data generation are not claimed.

Verification uses compatibilityTest, nativeCompatibilitySmoke, compileLaunchJava,
then a dev-client world launch and structured MCP state/content checks. No changes
to MCP Fabric or replacement automation framework are required.
