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
- [ ] Verify runtime122 first-blocker removal and actual ingredient/recipe behavior.

Evidence: /tmp/lms-source-ingredient-final-check.log and
/tmp/lms-source-ingredient-audit.tsv. Independent immutable source fixtures cover
constructors/ItemValue/super, source predicates and owner, exact stack NBT/display,
factories/handles/EMPTY, required versus optional slots, stable source/native and
array identity, actual applied native snapshot hooks, reload callbacks, stale and
mutated data rejection, source tag diagnostics excluded from native tag members,
and explicit custom-serialization boundaries. Runtime122 pending.
