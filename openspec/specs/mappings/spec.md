# Mapping System Specification

## Concept

LMS SHOULD resolve legacy symbols through a canonical compatibility representation:

```text
legacy symbol/name space
        |
        v
canonical LMS symbol/semantic operation
        |
        v
target Minecraft/Forge symbol
```

## Requirements

- Mapping data MUST be version/profile scoped.
- Descriptor-aware method/field mappings MUST distinguish overloads.
- A mapping MUST be able to represent class, method, field and descriptor changes.
- Pure renames SHOULD be handled as mapping/transformation rules rather than runtime bridges.
- Semantic API changes MUST NOT be misrepresented as simple name mappings.
- Unresolved mandatory symbols MUST be diagnosable.
- Mapping data SHOULD be testable independently of a full game launch.
