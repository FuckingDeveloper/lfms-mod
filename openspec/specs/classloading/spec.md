# Classloading Specification

## Requirements

### Controlled loading
A class belonging to a managed legacy artifact MUST be transformed as required by its resolved profile before execution.

### No probing by execution
Discovery/profile selection MUST NOT execute arbitrary legacy entrypoints merely to identify an artifact.

### Dependency visibility
Admitted artifacts of the same proven source profile MAY share a class visibility
domain. The complete immutable artifact/class ownership index MUST be prepared
before any managed class is defined. Every class MUST have one defining owner;
unknown ownership conflicts MUST fail before publication. Identical bundled
library artifacts MAY share an owner only when whole-artifact identity is proven.
Dependency declarations MUST still govern admission and initialization order.
Preflight-rejected artifacts MUST NOT become domain members. Blocked/closed owners
MUST reject new class resolution explicitly; already defined classes MUST NOT be
reported as unloaded or rolled back. Native/JDK delegation remains authoritative.
Different source profiles MUST NOT be merged implicitly.

Domain resources and service descriptors MUST enumerate the caller's own artifact,
then other admitted domain artifacts in deterministic order, then the native
parent, deduplicating URLs. Ordinary reflective lookup MUST reach the unique
class owner without holding an unrelated caller-loader monitor. Ownership and
resource indexes MUST be released when the domain closes.

### Source scan metadata
Source scan views MUST include truthful metadata from admitted immutable source
artifacts without executing classes. Type projection MUST match proven source
bytecode projection. Metadata discovery MUST NOT fabricate native FML membership
or imply plugin functionality. Rejected, blocked and closed sessions MUST NOT be
presented as active source scan providers.

### Client/server isolation
Client-only classes MUST NOT be eagerly resolved on a dedicated server solely because they exist in a legacy JAR.

### Transformation failure
If a mandatory transformation fails, LMS MUST prevent execution of the invalid derived class and produce an attributable diagnostic.

### Original artifact
Classloading MUST NOT require modifying the source legacy JAR in place.
