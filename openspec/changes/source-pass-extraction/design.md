# Extract proven source passes

## Scope

Continue the approved profile/runtime refactor. Preserve Forge1.19.2 ->
Minecraft26.3 / NeoForge26.3.0.58-beta semantics and current support limits.
No new generation or mod-specific behavior is introduced.

Extract the existing instance-call/callable traversal into shared ASM-only
SemanticInvocationRewriter. Profile-supplied member selection and subtype
proof remain authoritative. Preserve exact callable receiver types, special
invocation receiver identity, nested constants, collision-safe helper naming,
primitive/local widths and exception propagation. Do not infer member identity
from descriptor coincidence or initialize a managed class to inspect ancestry.

Extract block-entity persistence/update callback rewriting into a source-owned
Forge1192BlockDataRewriter. Its explicit evidence inputs are raw managed bytes,
source subtype proof and exact source method identity. It owns its bounded
callback-header cache and clears it when the transformer's managed evidence is
rebound, under the existing write lock. Its calls run under the existing read
lock. Source and target evidence/caches remain distinct.

The ordered pass ID and position, source wrappers/super calls, native callback
entrypoints, NBT behavior and source-only fail-fast boundaries remain unchanged.
The transformer delegates rather than retaining a second implementation.
Existing shared traversal consumers use the same extracted algorithm.

Verify focused callback/handle/lambda/constant and hierarchy-rebinding fixtures,
existing compatibility/native suites, identical dependency-complete audit output
and a fresh Gradle/MCP world with actual source mechanism interactions. Preserve
all prior dirty edits outside the refactor checkpoint and original legacy JARs.
