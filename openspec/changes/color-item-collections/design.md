# Source colored item/block fields over native collections

Target 26.3 consolidates color-specific Items fields, including all sixteen dyes,
into ColorCollection<Item>. The existing block collection projection MUST select
Item or Block based on the verified source declaration and actual target owner.
It MUST NOT cast Item results to Block or replace item/color identity with a
constructed substitute. Direct fields and callable field references (LDC,
invokedynamic and nested ConstantDynamic) MUST use the same exact source
owner/member/descriptor and verified native collection plan. The emitted getter
uses the actual ColorCollection.pick(DyeColor) result. Names and descriptors
alone do not authorize an absent source declaration or target collection.

Class-local callable adapter plans MAY be deduplicated. No source classloader or
world lookup is retained by a new global cache. Unsupported field categories
remain unadapted/fail-fast. This expands the existing collection algorithm; it
is not a recipe-specific or mod-specific fix.
