# Legacy Discovery Specification

## Requirements

### Requirement: Artifact discovery
LMS MUST discover candidate legacy mod JARs from configured legacy locations without modifying them.

### Requirement: Evidence-based identification
Discovery MUST collect available evidence such as legacy metadata, manifest data, class/package markers and declared Minecraft/Forge dependencies.

### Requirement: Descriptor output
Discovery MUST produce a normalized descriptor that can be consumed by the profile resolver.

### Requirement: Unknown artifacts
LMS MUST NOT guess a compatibility profile when evidence is insufficient.

#### Scenario: Unsupported or ambiguous JAR
- WHEN discovery cannot identify a supported generation with sufficient confidence
- THEN the artifact is reported as unsupported or ambiguous
- AND no destructive transformation is performed.
