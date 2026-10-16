# Source stack comparison family

Source item-only comparisons MUST preserve reference and empty-stack cases and
ignore count/NBT. The two ignore-durability variants are behaviorally item-only
in the pinned source; both branches of their original implementation prove this.
Tag equality MUST preserve null-versus-empty legacy NBT and source capability
compatibility. Full matches also checks count; item-and-tag equality does not.
Native component equality MUST NOT substitute for these source operations.
Stacks requiring source damage or native component-to-NBT projection MUST fail
explicitly when tag equality cannot be established. The current exact subset
accepts only CUSTOM_DATA patches and nondamageable items.

The proven capability-free subset includes exact base Item instances and
BlockItem instances other than the exact17 named source shulker items. Pinned
source BlockItem extends Item without an initializer or additional interfaces;
the target retains that class relationship. Managed source subclasses may reach
either proven base only when raw source ancestry/interfaces declare no custom
initializer. The item-instance exclusion remains required even for a proven class.
Source
IForgeItem.initCapabilities only supplies specific vanilla shulker BlockItems;
source stack attachment subjects are unsupported and rejected at registration.
Unknown item subclasses/providers MUST fail explicitly after ordinary item/NBT
mismatch checks, rather than assume empty capabilities. Both empty stacks bypass
capabilities as in source. Source item capabilities/native template comparison
remain unimplemented boundaries. Do not claim full comparison support for all
items from these proven subsets.

Exact source owner, mapped canonical member, opcode and descriptors select all
seven public comparison entry points, including method handles and nested
callable constants. Native inventory comparison remains native; only source
calls consume this source view.
