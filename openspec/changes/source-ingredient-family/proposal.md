# Source ingredient inheritance over native composition

## First causal blocker

Runtime121 genuine ServiceLoader found the original JEI platform provider, then
provider instantiation failed because source JeiIngredient extends Ingredient,
whereas the configured native Ingredient is final. Source ItemValue/TagValue/Value
and Stream<Value> construction were removed too. This requires a whole ingredient
family, not final-flag removal or a fixture selector.

## Verified contracts

Pinned Forge43.4.0 Ingredient permits subclassing, source EMPTY, stack-array views,
custom test/isSimple/serialization callbacks and invalidateAll/invalidate cache
refresh. Configured26.3 Ingredient is final and uses HolderSet or a genuine native
ICustomIngredient delegate. Its custom item enumeration is cached on first query;
getValues rejects custom ingredients. Native ordinary ingredients reject empty
holder sets; optional ingredient slots represent absence. ICustomIngredient has
stack testing, item-holder enumeration, isSimple, display and registered getType.
Source/target recipe serialization remains a separate gate under recipes/spec.md.

## Approved native policy

Approved by the user: native26.3 recipe/ingredient execution and reload-scoped enumeration.
Preserve original source subclass constructors, virtual callbacks and source
stack/display intent through an explicit source view and native custom-ingredient
delegate. Represent source empty intent as absence only at proven optional-slot
boundaries. Reload recreates native views against current data. Source-driven
mid-reload/out-of-cycle cache invalidation MUST remain fail-fast until adapted;
it MUST NOT silently return stale native cached membership.

Alternative: reproduce the complete source mutable ingredient/cache model and
adapt every native consumer to it, including source invalidation timing. This is
a materially broader recipe-runtime layer. The user selected the native policy; implementation and verification remain pending.

## Boundaries

No native final-flag removal, modified legacy JAR, dummy provider, dropped custom
predicate, guessed item list or automatic success for source serializer linkage.
Native JSON/wire serialization requires a proven registered IngredientType codec;
source custom serialization remains independently unsupported until verified.
Source identity, callable forms, EMPTY, ItemValue/TagValue, construction, queries,
callbacks, equality, reload and optional-slot conversion must be considered as a
family. Existing native-backed source recipes require regression verification.

## Verification

Immutable independent source subclasses/super/constructor/callable fixtures;
source predicates and exact display stacks; native custom tests/holder enumeration;
optional-slot absence versus custom emptiness; reload/cache boundaries and explicit
unsupported invalidation/serialization; compile/focused/native checks; fresh
Gradle client and actual recipe/content operations. JEI initialization alone is
not recipe or ingredient functionality.
