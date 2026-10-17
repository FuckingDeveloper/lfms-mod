# Source potion contents

Runtime135 reaches recipe-manager creation and fails reading the source WATER
constant as a raw Potion. The target exposes holder constants and stores potion
item state in PotionContents. Adapt verified source constants and typed item
queries/mutations, preserving original mod artifacts and class identity.

This bounded family does not migrate arbitrary raw potion NBT, custom Potion
subclasses, effect execution or the old static brewing recipe registry.
