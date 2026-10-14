# Complete verified block/item constant renames

Use exact source-generation SRG/canonical owner/member/descriptor identity and
actual target declarations to adapt Items.GRASS -> SHORT_GRASS, Items.CHAIN ->
IRON_CHAIN and Items.SCUTE -> TURTLE_SCUTE. The block grass/chain identities already
have the corresponding verified migration. Item grass/chain target registrations
reference those same blocks; target turtle-scute is the renamed turtle item.

Direct GETSTATIC and GETSTATIC handles MUST retain native field identity, getter
shape and value type. The existing recursive field-constant adapter MUST handle
LDC, invokedynamic and nested ConstantDynamic without accepting descriptor-only
identity. Unknown identities/writes remain unresolved. No source resource-id,
tag JSON or save migration is inferred from these binary member migrations.
