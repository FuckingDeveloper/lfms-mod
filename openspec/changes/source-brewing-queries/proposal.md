# Source brewing queries over target recipes — proposed semantics

Forge1.19.2 stores vanilla brewing mixes and allowed containers in static lists.
Minecraft26.3 uses world recipes, PotionIngredient predicates and synchronized
recipe-property sets. Two materially different semantics are possible: replay the
old vanilla tables, or query current native data-driven recipes.

Proposed rule: query the target world's recipes/property sets for ingredients,
containers, hasMix and mix, retaining the source static call signatures. Views
must follow native reload and feature filtering; calls without authoritative
recipe context fail explicitly. Custom source Forge recipe callbacks require a
separate lifecycle bridge and must not be silently skipped. No original JAR is
modified. The user approved native Minecraft26.3 recipes and reload semantics under
AGENTS.md §22 on 2026-10-17. Typed potion contents can proceed independently.
