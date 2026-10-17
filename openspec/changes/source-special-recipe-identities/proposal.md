# Removed special-recipe identities

## Evidence and decision

Pinned target26.3 resources define tipped_arrow as crafting_imbue, shulker box
recoloring as crafting_transmute, and suspicious stews as ordinary shapeless
recipes with typed effects. Their source1.19.2 concrete Java classes no longer
exist. ShieldDecorationRecipe survives. JEI's original replacement algorithm
uses exact Class keys and actual recipe.getClass(), not a recipe ID predicate.
Aliasing the removed classes to ImbueRecipe/TransmuteRecipe/ShapelessRecipe would
misclassify unrelated recipes. Replacing recipe objects with LMS views would
change the existing source-recipe-access contract requiring actual native values.

User-approved policy: preserve native recipe objects and their
actual class identity. Admit source-owned nominal tokens for the three removed
source types, with exact source ancestry; these tokens do not classify native
recipes as old concrete instances. Construction, subclassing, reflection-driven
instantiation and unproven executable members remain explicit unsupported
boundaries, not dummy recipe implementations. Thus old class-based replacement
code runs its original selection and does not select these now data-driven native
recipes; the ordinary native recipes remain in the original consumer's normal
list. No recipe is hidden or replaced, no mod identity selects transformation,
and no native recipe matching/assembly is changed.

Alternative: design source-kind recipe views and explicitly revise the actual-
native-object contract before implementing them. This would alter observable
getClass/identity and requires a larger compatibility model.

The proposed native identity policy does not establish support for the removed
source constructors or complete display of dynamic native recipe outputs.
