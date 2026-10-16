# Explicit target callback layout for source block data

Continue the approved source/target refactor without changing the current four
block-data operations: load, save, update-tag and handle-update-tag. Source method
identity, aliases, source-super/default behavior, NBT projection, callback timing
and phase order remain source-owned and unchanged.

The native callback layout MUST be supplied explicitly to the source rewriter.
A bounded immutable description contains the tested target identity, native base
owner and one-reference-argument callback names/descriptors. It MUST reject missing
operations and unsupported shapes; it MUST NOT fabricate an arbitrary method.
Input/output view return descriptors MUST derive from the supplied load/save
callback arguments, not source-profile ValueInput/ValueOutput constants.

The current build's installed adapter provides the NeoForge26.3 layout at one
composition point. Profiles/factories carry the selected layout into fresh
transformers. Default constructors remain convenience entrypoints for that
installed build, not automatic target detection or an untested fallback. Live
NeoForge26.3 bindings MUST reject a different layout before managed admission.

Layout data is ABI evidence, not semantic compatibility proof. Exact target
classes/methods and emitted bridge signatures must be checked in the configured
native runtime. A synthetic alternate layout verifies parameterization only;
it MUST NOT be reported as another supported target. No legacy artifact changes.

This change isolates code-generation ABI. Runtime NBT/native view handling in
Forge1192BlockDataBridge remains target-aware and is an explicit further boundary;
no target-independent persistence implementation or arbitrary new callback shape
is claimed. Original hierarchy caches/evidence locks and safe writer stay intact.

Verify descriptor shape/negative gates, alternate layout emission, actual native
ABI/bridge linkage, existing callback/save/super/capability suites, dependency-
complete audit and fresh Quick Play/MCP world content behavior. Keep prior dirty
changes, generated reports, tokens and local configs out of the checkpoint.
