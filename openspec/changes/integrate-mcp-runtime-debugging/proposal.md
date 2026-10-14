# Integrate external MCP Fabric into runtime debugging

Use the existing `minecraft` MCP server to observe and exercise the tested
Minecraft 26.3 / NeoForge 26.3.0.58-beta / Java 25 development client. IC2 Classic
is an external Forge 1.19.2 integration fixture, not a production selector.

This changes development verification policy/documentation and appends native
Quick Play arguments through the existing Gradle run DSL when lmsTestWorld is
explicitly supplied. Default launcher behavior and DevLaunch arguments remain
intact. It does
not add a runtime subsystem, Fabric source profile, target adapter, custom
Minecraft automation framework or MCP Fabric implementation changes.

The loop is: SDD/evidence -> compatibility fix -> existing automated checks ->
fresh Gradle client with explicit test-world Quick Play -> live connection/world
checks -> supported content actions -> structured observations and causal logs.
Keep missing operations and world-load failures explicit. Store transient
connection responses and test evidence outside the repository; do not commit
secrets, local configuration, fixture artifacts or generated test output.
