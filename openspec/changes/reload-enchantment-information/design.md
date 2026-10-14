# Read-only enchantment information in target reload scope

The target AddServerReloadListenersEvent supplies the authoritative
ReloadableServerResources registry lookup, including read-only registry/tag
views before an active world exists. Holder-backed enchantment operations
(enchant stack, max-level query, stack-level query and equipped-item selection)
MUST resolve their holder from this scoped provider when present. They MUST NOT
require that provider's RegistryLookup to be a mutable Registry or invent an
Enchantment value. Absence in the scoped provider MUST fail rather than select
an unrelated active-world registry. Existing unscoped registration/world
resolution remains the fallback when no reload scope is present.

Full source registry-global access remains distinct and still requires a real
Registry. A read-only information query MUST NOT grant registry mutation or
cache this reload provider beyond synchronous callback lifetime. Nested/failing
callbacks MUST restore previous scope. This contract applies independently of
which mod's recipe listener uses enchantment information.

Runtime evidence: the reference mod's initial reload aborted in enchantment
max-level lookup before GlobalRecipes finished rebuilding machine recipe lists.
Gold ore then belonged to the native common tag but was rejected by the machine
slot. Successful registration/tag linkage did not prove recipe availability.
