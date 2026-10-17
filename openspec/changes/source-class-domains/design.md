# Prepared source ownership and truthful scan data

The approved visibility policy is scoped to the selected source profile, not a
Minecraft target version. ManagedClassDomain is target independent. The Forge
bootstrap statically evaluates dependencies and lifecycle capability gates for
all candidates and removes mandatory dependents of rejected source owners before
constructing the domain. A domain indexes original class entries before defining
classes. Top-level ownership collisions fail; bundled libraries may deduplicate
only by whole-JAR SHA-256. Multi-release class entries remain an explicit gap.
Each canonical artifact has its own transforming loader. Peer requests route to
that loader before acquiring an unrelated caller monitor. Source hierarchy byte
evidence uses the same ownership index. Native/JDK parent-first delegation remains
unchanged. The bootstrap preserves source initialization order and checks actual
mandatory-owner initialization before executing a dependent constructor.

Source scan data uses the configured native scanner over a namespace-only ASM
visitor with code/debug/frames skipped. The visitor uses the exact source
transformer's public type projection; it does not run whole-class semantic
rewriters on dormant classes. Annotation/class/member/value data is retained as
an immutable snapshot. Metadata belongs to source views, not native FML. Managed
scan providers include admitted linked sessions and exclude blocked/closed ones;
loaded-mod display/count policy is unchanged. Plugin discovery, assignability,
lifecycle and actual plugin functionality remain independent gates.

Domain resources enumerate caller, canonical peers and native parent. Standard
JDK ServiceLoader and Class.forName are not rewritten. An owner failure blocks
new resolutions; already defined Java identities cannot be rolled back. Closing
all root owners closes the remaining library loaders and releases indexes.
Session scan snapshots and lifecycle context bindings are removed on close.
No mod, package or annotation-fixture identity selects compatibility behavior.

## Remaining bounded scope

No cross-profile visibility, native membership, arbitrary multi-release JAR
ownership, source Mixin execution, library version mediation or promised plugin
functionality. Duplicate top-level classes are rejected even when bytes match.
