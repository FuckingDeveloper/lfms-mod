# Block-data storage boundary

Continue the approved source/target refactor without changing persistence or
network callback semantics. This isolates native storage access, not all native
host/NBT types used by the current source runtime.

The generic `BlockDataContext<T,L>` contract carries the optional registry lookup,
output availability and creation of a context bound to a supplied mutable output
root. It MUST preserve the active target serialization ops, diagnostic reporter
and lookup identity. It MUST NOT own Forge key translation or source callbacks.
Contexts MUST be immutable handles scoped by the existing source callback frame;
no global mutable provider registration or disposable-loader cache is introduced.

The target adapter MUST own ValueInput/ValueOutput backend validation, mixin
accessors, native views and reporter/ops preservation. Unsupported backends and
missing required lookup/output context MUST fail explicitly. A target/source
binding facade supplies native callback ABI and delegates synchronous callback
execution to the source profile. Its owner MUST be supplied by the target layout,
including source-super input/output view entrypoints. Direct source dispatch and
source defaults MUST continue through the source runtime.

The source runtime MUST retain callback discovery, ClassValue lifetime, nested
frame restoration, source tag identity, ForgeData/ForgeCaps projection and
once-only source default/super dispatch. Input root copying, output root identity,
registry-context precedence and callback timing MUST remain unchanged. The
installed build supplies the fallback entity context explicitly; this is not
runtime version discovery or another supported target.

Verification: synthetic binding owner emission; native ABI, native reporter/ops
and root identity, unsupported backend/missing context failures, nested callback
success/failure scope restoration, existing save/load/capability/container checks,
dependency-complete audit, then fresh Gradle Quick Play and MCP interactions.
No original legacy artifact, MCP sources, gameplay semantics or version range
changes. Original dirty files and test-run output stay out of the checkpoint.
