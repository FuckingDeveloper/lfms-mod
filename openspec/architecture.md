# LMS Architecture

## Processing pipeline

```text
Legacy Mod JAR(s)
       |
       v
Artifact Discovery
       |
       v
Descriptor + Dependency Graph
       |
       v
Profile Resolution
       |
       v
Mapping / Capability Planning
       |
       v
Bytecode + Resource Transformation
       |
       v
Derived Artifact / Transformation Cache
       |
       v
Controlled Legacy Classloading
       |
       +----------------------+
       |                      |
       v                      v
Compatibility Runtime    Native NeoForge API
       |                      |
       +----------+-----------+
                  |
                  v
       Lifecycle / Registries / Game
                  |
                  v
             Diagnostics
```

## Architectural layers

### Discovery
Reads candidate artifacts and produces normalized metadata/evidence. It MUST NOT mutate or execute legacy artifacts.

### Dependency Graph
Resolves required/optional legacy dependencies and load ordering before transformation/loading.

### Profile Resolver
Selects a legacy-generation profile from evidence. Ambiguous or unsupported artifacts MUST remain unresolved.

### Mapping System
Resolves legacy symbols through a canonical LMS representation into target symbols. Raw ad-hoc name replacement SHOULD NOT be scattered through transformation code.

### Transformation Engine
Performs profile-selected bytecode transformations. Resource transformation is a separate concern and MUST NOT be hidden inside arbitrary class transformers.

### Transformation Cache
Stores derived outputs keyed by all compatibility-relevant inputs. Cached output is disposable and MUST never be the only copy of a legacy artifact.

### Controlled Classloading
Legacy classes MUST pass through the applicable compatibility pipeline before execution. Unsupported raw legacy classes MUST NOT be intentionally loaded merely to see whether they happen to work.

### Compatibility Runtime
Provides semantic adapters where static rewriting is insufficient.

### Legacy Lifecycle
Maps supported legacy Forge/FML lifecycle concepts onto the target runtime while preserving required ordering semantics.

### Diagnostics
Records decisions across the complete pipeline and MUST make failures attributable to artifact, profile, rule/capability and symbol where possible.

## Separation rules

- LMS core MUST remain mod-agnostic.
- Version-specific behavior belongs to profiles.
- Mod-specific behavior belongs to optional compatibility modules.
- A generic fix discovered while supporting a specific mod SHOULD be implemented at the lowest reusable layer.
- Mod-specific compatibility MUST be a last resort after generic profile/runtime mechanisms have been considered.
- Profiles MUST be independently evolvable and testable.

## Evidence and readiness boundaries

The mapping layer is responsible for **candidate discovery and verification**, not just symbol substitution. Version-profile-scoped legacy and target runtime indexes MAY support this process, but indexes are evidence providers rather than a separate architectural stage or a reason to bypass capability planning.

The capability planner consumes verified mappings and semantic-adapter requirements. It produces explicit READY/UNRESOLVED/BLOCKED decisions and a versioned transformation plan. The early target-loader integration executes only verified, applicable plan entries, checks bytecode integrity and linkage requirements, and records execution evidence.

The source artifact, legacy runtime bytecode, mapping files and target runtime are distinct evidence sources. Their hashes/versions SHOULD be recorded so a decision can be reproduced. Successful early transformation MUST NOT be confused with controlled loading, lifecycle completion or gameplay compatibility.

### Development order

Implementation changes MUST follow the existing pipeline and release gate. For the Forge 1.19.2 reference work, first stabilize generic mapping/target-index evidence and transformation safety, then complete dependency/capability planning, controlled legacy classloading and compatibility runtime, followed by lifecycle/registration, resources/networking/save-data boundaries, reporting/cache and reference-mod acceptance. Work on a specific transform MUST NOT silently reorder these milestones or introduce mod-specific behavior into core.
