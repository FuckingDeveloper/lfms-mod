# Delta: Legacy Discovery

## Added requirement: Configured discovery location

LMS MUST define a deterministic location for legacy artifacts on the target installation.

#### Scenario: Candidate JAR is present
- GIVEN a candidate JAR exists in the configured legacy location
- WHEN LMS discovery runs
- THEN the artifact is enumerated
- AND its original bytes are not modified.

## Added requirement: Forge 1.19.2 recognition

The initial implementation MUST be capable of selecting the Forge 1.19.2 profile when sufficient supported metadata/evidence is present.

#### Scenario: Recognized Forge 1.19.2 artifact
- GIVEN a legacy JAR contains sufficient evidence of a supported Forge 1.19.2 mod
- WHEN profile resolution runs
- THEN the Forge 1.19.2 profile is selected
- AND the evidence used is available to diagnostics.
