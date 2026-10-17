# Source enchanted-book reads

Runtime132 skips enchanted-book ingredient variants because the source static
book reader was removed. Its next consumers require an ACTIVE enchantment view
and the original registry-derived description ID. Adapt those operations as one
bounded read family, without identifying a consuming mod.

Book construction/subclasses, custom enchantment subclasses, EnchantmentCategory,
and source enchantment simulation are separate unsupported families.
