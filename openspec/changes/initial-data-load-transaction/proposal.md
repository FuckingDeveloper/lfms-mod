# Proposed explicit two-phase initial data transaction

Status: ACCEPTED by the user on 2026-10-16; implementation/verification pending.

## Observed dependency cycle

The ordered source setup constructs real target items that depend on world data
(e.g. trim_material/lapis) and publishes procedural static tags. Running before
world registries exist fails (runtime47); running in the current TagsUpdated
boundary publishes tags after their initial load. Splitting the callback is
already disproven by dependent setup operations.

Moving only to ReloadableServerResources.loadResources entry would make world
registries available, but initial static PendingTags have already been consumed
by world-registry parsing. MappedRegistry.prepareTagReload can create new Named
sets for tags outside frozenTags. Replacing pending objects without preserving
all earlier references cannot be claimed equivalent. Direct frozen registry/tag
mutation is not an acceptable shortcut.

## Proposed behavior

An initial world-open transaction may have two explicit native data phases:

1. Complete the current native data phase, including once-only ordered source
   callbacks at the proven pre-component-binding event boundary.
2. Before exposing the world to play, perform the ordinary native static-tag and
   reloadable-data load using the same authoritative world/dimension registries
   and resource packs, now containing the declarations emitted by setup.
3. Publish the final resources through native lifecycle and replay client reload
   listener state against the final context. Do not execute source setup again.

This is intended for late static-tag/reloadable-data declarations. It is not a
solution for source setup that changes world-generation/dimension registry
schemas after those registries have already been constructed. That case must
remain explicit until a separately verified dependency contract exists.

## Contract change and costs

This explicitly replaces the prohibition against an automatic server reload
in legacy-tag-resource-paths/design.md for this initial-load transaction only.
It must not be disguised as a timing fix in the existing single-pass contract.
It repeats native data work and can increase first-world-entry duration. It must
not become a per-tick, per-chunk or arbitrary retry loop. Ordinary /reload and
later world opens must not duplicate original setup callbacks.

Approval of the behavior is not proof that a safe native integration exists.
Implementation must still establish native world-open, failure/cleanup, pending
component/tag identity and client-replay semantics, then pass cold-start and
ordinary-reload tests. No mod selector, legacy JAR patch or automation framework.

## Verification gates

- Focused tests: callback once/order, phase failure prevents publication, native
  registries reused, source initializers not lost, no early global mutation.
- Cold client: procedural source wood in logs and prevents_nearby_leaf_decay
  without a manual /reload; fresh tree leaves survive native ticks/re-entry.
- Native reload: tags, components, recipes and source loot effects still work.
- Existing GUI, inventory and model-data paths remain functional.
