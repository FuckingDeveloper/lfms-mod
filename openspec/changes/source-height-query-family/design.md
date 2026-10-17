# Native-backed source height queries

Source 1.19.2 LevelHeightAccessor has exclusive block/section upper bounds.
Target 26.3 LevelHeightAccessor has inclusive getMaxY/getMaxSectionY. LMS MUST
preserve the source return convention: max block = native maxY + 1, max section
= native maxSectionY + 1. Minimums, height, section count/index conversion and
outside-height predicates retain their verified native counterparts for valid
native dimension ranges. The outside predicate MUST retain the source
comparison y < min || y >= maxExclusive, including Java integer overflow for
arbitrary source factory operands; it MUST NOT silently assume valid Overworld bounds. Values MUST
come from the actual accessor; Overworld dimensions MUST NOT be hardcoded.
Source create(min,height) MAY use the native factory, preserving argument order.

Selection MUST require exact source root mapping, descriptor and proven receiver
ancestry, including inherited calls through ServerLevel/ChunkAccess. Direct
virtual/interface calls and callable constants MUST preserve receiver types and
source argument/result shape via the existing safe semantic invocation machinery.
Source super/default calls remain unchanged until their base-body and override
semantics are separately proven. Static factories require the exact root owner.

This bounded family admits native-backed accessors. A managed class declaring
source height callbacks MUST be marked as unadapted; consumption through this
query bridge MUST fail explicitly before invoking native default methods that
could ignore those callbacks. Custom source world/accessor implementation and
native engine callback integration remain a separate gap; native-backed query
linkage MUST NOT be claimed as custom-world support. Source artifacts stay intact.
The source marker is inherited and has no dummy methods or fallback state.

Target-specific method names and inclusive-bound interpretation belong to the
installed target adapter. Root source mappings/ancestry/callback declarations
belong to the source profile. Lookup is indexed/cached per transformer binding;
managed hierarchy rebinding MUST invalidate subtype evidence as already required.

Evidence: pinned source 1.19.2 interface bytecode and official/SRG identities;
exact 26.3.0.58-beta LevelHeightAccessor sources. Tests MUST cover negative minY,
section boundaries, inclusive/exclusive endpoints, non-Overworld ranges, native
callable methods, wrong owners/descriptors, and explicit custom-override rejection.
