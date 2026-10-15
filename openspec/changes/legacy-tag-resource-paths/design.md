# Legacy tag resource paths

Runtime evidence: intact source trees lose leaves after successful growth and
world re-entry. Their logs are absent from native minecraft:logs and therefore
from prevents_nearby_leaf_decay. Source bytecode publishes logs/leaves and other
members through a procedural PackResources using Forge 1.19.2 plural tag folders.
The existing pack adapter forwards target singular folder queries unchanged.

Source 1.19.2 uses tags/blocks, tags/items, tags/fluids, tags/entity_types and
tags/game_events; the target registry tag loader uses the corresponding singular
registry paths. LMS MUST project these verified resource paths for admitted source
artifact packs and transformed procedural source packs. It MUST preserve resource
bytes, namespace/tag identity, source execution and native reload lifecycle.
Explicit native entries in the same source pack MUST win over projected entries.
Native packs MUST NOT be wrapped. Prefix matches MUST respect directory boundaries;
unknown directories MUST remain unchanged. Listing and direct lookup MUST agree.
No block identities, generated trees or registry bindings may be fabricated.

This does not define forge-to-c common-tag identity migration, JSON schema
migration, arbitrary registry-directory aliases or complete recipe compatibility.

## First-load timing limitation

The first client check after path projection still lacked log membership. A native
reload restored it. The source common-setup callback publishes procedural tag
definitions, but LMS defers the whole callback until the initial tag/component
reload. The resulting declarations miss that first tag-loading pass.

An attempted change to retain common-setup timing while deferring individual
component-consuming helpers was rejected by runtime verification: later source
setup steps depend on results of earlier helpers (ReactorSubmissions.addSetups
received null). Those code changes were removed. Operation ordering is observable
and cannot be changed merely because helpers have independent call signatures.

Path projection is verified independently; first-load procedural declarations
remain an open lifecycle compatibility gap. A future solution must preserve source
execution order and avoid duplicate callback execution, frozen-tag mutation or
an automatic reload that conceals the timing defect.
