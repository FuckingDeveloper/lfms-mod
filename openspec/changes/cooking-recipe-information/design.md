# Native cooking recipe information through source queries

The verified source server RecipeManager accessor MAY call the native accessor
on the same server. Native recipe enumeration MUST retain holder provenance
before exposing the source recipe-object collection. Native IDs MUST originate
from authoritative holders, never result items/class names. An identity object
seen under different identifiers MUST fail explicitly. The index MUST use weak
identity references with queue cleanup and MUST NOT retain managers, worlds,
legacy recipes or disposable classloaders. Original source identity views take
precedence. Native recipes without enumerated holder provenance remain fail-fast.

Source interface and concrete Recipe/AbstractCookingRecipe query calls MUST
share the same ID/result/ingredient adapters. Source-owned views retain their
original callback results. Native single-input result information MUST read the
actual target result template, without fabricating input or resolving a display
in a fake world; its source-facing value is a fresh mutable stack. This is an
information projection, not write-through mutation of target recipe results.
The source cooking-time and experience queries expose the actual target stored
values, preserving receiver and numeric meaning. No construction or source wire
compatibility is inferred. Explicit source-super invocation remains distinct.
