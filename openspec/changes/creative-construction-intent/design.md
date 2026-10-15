# Preserve item-tab intent at construction

Forge 1.19.2 Item.Properties.tab records membership consumed by the Item
constructor. The target has no property setter for that membership. Capturing
membership only in removed setRegistryName adapters misses DeferredRegister
factories, whose target item key is supplied by the construction scope.

LMS MUST capture recorded source membership when the target Item constructor
consumes its actual Properties registry key, independently of the registration
API used. It MUST preserve properties reuse: constructing another item with the
same source properties MUST NOT consume the earlier tab declaration. Changing
properties for a later item MUST NOT change membership of an existing item.
Items without a recorded source declaration MUST remain unaffected. The hook
MUST NOT inspect partially initialized source subclass state or populate tabs
before the native creative population lifecycle. Source item enumeration,
deduplication, native registration phases and original artifacts are unchanged.

This establishes membership capture, not custom tab-level enumeration or item
rendering support. Runtime acceptance must observe actual nonempty population.
