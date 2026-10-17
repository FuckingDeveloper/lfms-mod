# Source language values and selection

Forge1.19.2 LanguageInfo is a source-owned value: code, region, name,
bidirectionality and the pinned Java Locale construction. Its equality, hash and
sort order use the source code; identity MUST NOT collapse distinct source
instances merely because their codes match. Source construction, subclass getters
and explicit superclass calls retain ordinary source dispatch. The target's
code-less LanguageInfo record MUST NOT substitute for this value.

Source LanguageManager selected/lookup/enumeration operations MUST consume the
receiver's current native catalog/code and return stable source values for the
same catalog entry. Enumeration returns a detached code-sorted mutable set.
Unknown lookup remains null. Selected lookup falls back to the source en_us
default when no catalog entry is available. Source setSelected MUST pass the
source code to the native manager and retain the exact supplied source object
until native selection or ordinary resource reload replaces it. Neither setter
MUST trigger an extra reload; target resource loading remains authoritative.

State MUST belong to the native manager lifetime, not a global map retaining
worlds/classloaders. Resource reload MUST invalidate catalog views; native code
selection MUST invalidate source-only selection. No arbitrary native object may
be assigned a guessed language code. The family covers verified manager owners,
inherited receivers, handles and nested constants; unrelated same descriptors
remain untouched. Source manager construction/subclass overrides and removed
Mojang bridge-game interface interop remain explicit unproven boundaries.

Evidence: pinned Forge43.4.0 LanguageInfo/LanguageManager bytecode and actual
NeoForge26.3 LanguageManager source. The target stores a selected code, a native
metadata map and a Locale; its normal reload supplies authoritative catalog data.
