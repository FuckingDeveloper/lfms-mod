# Bootstrap Specification

## Requirements

### Requirement: Native LFMS bootstrap
LFMS MUST load as a native mod on the declared target Minecraft/Forge runtime.

#### Scenario: Supported target starts
- GIVEN Minecraft 26.3 with Forge 66
- WHEN LFMS is installed and the game starts
- THEN LFMS initializes its bootstrap services
- AND diagnostics identify the LFMS version and target runtime.

### Requirement: Bootstrap isolation
Bootstrap MUST NOT depend on a particular legacy mod being present.

#### Scenario: No legacy mods
- GIVEN LFMS is installed with no legacy artifacts
- WHEN the game starts
- THEN LFMS remains operational
- AND reports zero discovered legacy artifacts without treating this as an error.
