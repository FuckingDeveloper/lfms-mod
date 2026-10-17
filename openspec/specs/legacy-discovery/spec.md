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

### Declared language-loader eligibility

The normalized descriptor MUST retain the actual language-loader identifier and
version range separately from the Minecraft dependency. When no Minecraft
dependency is declared, a source profile MAY match an explicit declaration of
compatibility with its pinned language-loader generation. This establishes
eligibility for that profile, not the artifact's exact compiler version. An
explicit Minecraft dependency MUST take precedence; absent/malformed/unknown
loader evidence MUST NOT authorize fallback. All installed profiles MUST be
assessed and ambiguity MUST remain an error. Filenames, descriptions, pack-format
numbers and timestamps MUST NOT substitute for compatibility declarations.
