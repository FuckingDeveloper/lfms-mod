# Evidence-backed method rule catalog

Introduce a bounded catalog consumed by both transformation and admission. It is
not automatic semantic inference and MUST NOT authorize unknown operations. Each
rule names a source declaration, descriptor, invocation shape, semantic family,
target bridge and evidence. Exact source declaration resolution MUST use pinned
source headers and managed raw bytes, respect overrides/hiding, fail on ambiguity
or missing evidence, and avoid loading legacy classes. Repeated header/declaration
lookup MUST be cached within a fixed evidence graph; rebind MUST invalidate it.

For catalogued families the admission assessment MUST consult the same rule data
as the transformer. An unknown member inside that family MUST remain blocked,
even if an old compatibility allowlist happens to include it. Existing unrelated
families remain on the old assessment until explicitly migrated. Catalog approval
means an implemented bounded adapter, not functional acceptance of an entire mod.

The first family is Forge 1.19.2 ParallelDispatchEvent.enqueueWork(Runnable) and
(Supplier). Resolve inherited calls for all proven source event subclasses and
reject unsupported source overrides/super-call shapes explicitly. Native target
queue timing, ordering, future values/failures and main launch-thread scheduling
MUST remain authoritative. Capture source owner at enqueue time and restore it
around execution and on error, including nested owner restoration. Do not execute
work immediately or enqueue it to a world-time replacement queue. Existing
component-bound callback deferral still requires an unused source event parameter;
this change MUST NOT authorize use of an already-consumed lifecycle work queue.

Source header generation MAY add method declarations from the exact hash-pinned
input artifact. Existing class/field headers remain unchanged. No reflection or
legacy entrypoint execution is allowed for discovery. Report all discovered JARs,
unknown profiles and grouped first admission blockers in a compact machine-readable
batch diagnostic. Do not force a metadata-incomplete artifact into a guessed
profile or weaken config/network/DisplayTest gates.

Verify direct/inherited/negative/override cases, handles/lambdas/nested constants,
source index/rebind, actual target ABI and deferred execution futures/ownership,
then all suites, batch analysis and fresh runtime/MCP behavior. Preserve previous
user changes and immutable input JARs. This is the first migrated rule family,
not a claim that the complete transformer/catalog consolidation is finished.
