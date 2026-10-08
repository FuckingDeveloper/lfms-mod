# Version Profiles Specification

## Requirements

- Each supported legacy generation MUST be represented by an isolated version profile.
- The first profile MUST target Forge 1.19.2.
- Profile selection MUST be based on the normalized legacy descriptor.
- Version-specific mappings, transformation rules and runtime providers MUST be owned or declared by the profile.
- Core LFMS SHOULD NOT contain scattered legacy-version conditionals when the behavior belongs to a profile.

## Planned profiles

The architecture reserves profiles for Forge 1.16.x, 1.12.2, 1.7.10 and 1.6.4. Their existence in this document does not imply implemented support.
