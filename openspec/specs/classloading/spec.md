# Classloading Specification

## Requirements

### Controlled loading
A class belonging to a managed legacy artifact MUST be transformed as required by its resolved profile before execution.

### No probing by execution
Discovery/profile selection MUST NOT execute arbitrary legacy entrypoints merely to identify an artifact.

### Dependency visibility
Class visibility between legacy artifacts MUST follow the resolved dependency graph and target-loader constraints.

### Client/server isolation
Client-only classes MUST NOT be eagerly resolved on a dedicated server solely because they exist in a legacy JAR.

### Transformation failure
If a mandatory transformation fails, LMS MUST prevent execution of the invalid derived class and produce an attributable diagnostic.

### Original artifact
Classloading MUST NOT require modifying the source legacy JAR in place.
