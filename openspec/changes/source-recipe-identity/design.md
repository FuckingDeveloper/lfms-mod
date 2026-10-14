# Preserve source-owned recipe identifiers

Source Recipe.getId is part of the recipe object; target IDs belong to holders.
For source recipes declaring the exact getId contract, add a typed source view
using the existing recipe-view adapter mechanism and virtual source dispatch.
The UpgradeRecipe facade retains its construction ID and implements that view.

Exact source Recipe interface invocations and callable handles MUST consume the
source identity view. Unknown native recipes without an authoritative holder
context MUST fail explicitly, not receive guessed IDs or null placeholders.
Source declarations and source-super behavior remain intact. Native holder-ID
recovery is a separate unimplemented boundary, not inferred from result items.
