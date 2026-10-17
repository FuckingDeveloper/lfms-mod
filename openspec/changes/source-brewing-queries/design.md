# Approved native brewing semantics

The user selected Minecraft26.3 recipes and native reload semantics on
2026-10-17. Source and target generations remain separate: the Forge1.19.2 profile
owns old symbols, argument order and facade types; the target adapter owns
RecipeManager, PotionIngredient, property sets and recipe assembly.

## Query contract

- Source isIngredient MUST use current native BREWING_REAGENTS acceptance.
  Source input/container queries MUST follow native inputs and the native
  BREWING_POTION_INPUTS tag, with native feature filtering.
- Source hasMix and mix MUST query authoritative current brewing recipes and
  execute native matching/assembly. Preserve the source mix argument roles:
  PotionBrewing.mix takes reagent first and input second. Source no-match mix
  returns the exact original input; Forge getOutput returns ItemStack.EMPTY.
- Reads MUST follow native reload rather than retaining startup tables or a
  world/global recipe snapshot. Calls without the required authoritative recipe
  context MUST fail explicitly. Existing full-client recipe access requires a
  co-resident server; remote full-recipe synchronization is not established by
  this family and MUST NOT be fabricated from reduced RecipeAccess.
- The old ALLOWED_CONTAINERS read MAY expose an immutable source ingredient view
  of native acceptance. It MUST NOT return an empty list to bypass the migration.
  Unknown historical mutable table fields remain unsupported.
- A source Forge VanillaBrewingRecipe facade MAY represent the native brewing
  subsystem, preserving source isInput/isIngredient/getOutput and their virtual
  callbacks. Its output MUST come from actual native recipes. It MUST NOT be a
  dummy recipe whose only effect is advancing startup.
- Forge registry enumeration MUST preserve source interface/concrete identities
  and must not omit admitted source callbacks. Source recipe registration is a
  separate target data/reload lifecycle boundary: until migrated it MUST fail
  explicitly, not accept then silently discard callbacks.
- Pure source BrewingRecipe construction and local ingredient/output operations
  can retain their source behavior independently of native registration.
  Construction/linkage MUST NOT be reported as registration support.

## Evidence

Pinned Forge43.4.0 PotionBrewing, IBrewingRecipe, BrewingRecipe,
VanillaBrewingRecipe and BrewingRecipeRegistry source/bytecode. Actual target
26.3.0.58-beta BrewingRecipe, BrewingInput, PotionIngredient, RecipeManager,
RecipePropertySet, BrewingStandMenu and BrewingStandBlockEntity. Native brewing
matches input/reagent predicates and assembles an output template; ingredient
slot acceptance uses BREWING_REAGENTS. Native input acceptance also uses the
BREWING_POTION_INPUTS tag. The old vanilla static tables no longer exist.

## Required verification

Independent original-bytecode/native checks must cover argument order, source
no-match identity/EMPTY, actual matching/output counts and data, ingredient and
input predicates, recipe-context absence, reload replacement, callback/owner
identity and explicit registration rejection. A fresh explicit world must verify
original consumers and record the first remaining causal blocker. Enumeration
and successful startup do not establish brewing gameplay or exhaustive coverage
of all modern custom ingredient predicates.
