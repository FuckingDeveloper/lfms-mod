- [x] Inspect runtime121 first causal failure and complete source subclass uses.
- [x] Inspect pinned source Ingredient and exact native Ingredient/ICustomIngredient.
- [x] User approved native26.3 cache/reload and optional-slot policy.
- [x] Design source view/native composition boundaries against existing recipe bridges.
- [x] Implement source inheritance/values/factories/callables/JSON queries, native
  composition/display snapshots, optional EMPTY conversion, native reload guards
  and source tier/material/recipe generic boundaries. Unknown codecs remain explicit.
- [x] Pass compatibilityTest/nativeCompatibilitySmoke/compileLaunchJava, including
  existing smithing JSON/wire, repair and recipe-information regressions.
- [x] Audit3829 current input classes:1171 grouped findings/6793 sites; only the
  pre-existing custom-renderer/PackFinder transform failures remain.
- [x] Runtime122 passed original service provider construction and source
  Ingredient subclass linkage. The next causal failure is empty plugin discovery
  because source scan data is omitted from ModList.getAllScanData.
- [ ] Verify actual runtime ingredient/recipe gameplay after loader discovery.

Evidence: /tmp/lms-source-ingredient-final-check.log and
/tmp/lms-source-ingredient-audit.tsv. Independent immutable source fixtures cover
constructors/ItemValue/super, source predicates and owner, exact stack NBT/display,
factories/handles/EMPTY, required versus optional slots, stable source/native and
array identity, actual applied native snapshot hooks, reload callbacks, stale and
mutated data rejection, source tag diagnostics excluded from native tag members,
and explicit custom-serialization boundaries. Runtime122 confirms first-blocker removal only; recipe UI/crafting remain
blocked by source metadata discovery.

## Native custom display continuation

Runtime126 exposed a concrete source getItems failure in original recipe-wiki
input collection. Exact target Ingredient.display delegates to the general
ICustomIngredient.display contract, including its default enumeration and custom
component-aware overrides. NativeView now converts this declared display through
the existing proven SlotDisplay records for all custom implementations. Native
matching, object identity, reload epochs and mutation guards are retained;
unknown display records and source/native serializers remain explicit gaps.

`./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava` passes
(`/tmp/lms-native-custom-display-check.log`). Actual native DataComponentIngredient
and nested CompoundIngredient fixtures preserve component-bearing display stacks,
ordered alternatives, predicate differences, exact unwrap identity, cached arrays,
mutation rejection and reload refresh. These native smoke fixtures use the probe's
explicitly component-bound STONE holder; no production component binding or target
registry lifecycle is changed. Real recipe-wiki completion is pending runtime127.

Runtime127 loads the world without the previous custom-display error during recipe
reception. Runtime128 passes that point as well, but complete JEI startup is now
blocked by its separate CreativeModeTab.TABS read. These observations do not
establish full wiki/recipe UI acceptance or remote full-recipe synchronization.
