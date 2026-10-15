# Source armor equipment

For the Forge 1.19.2 source profile, base armor material durability defaults and
slot-scoped UUID attribute identities MUST be preserved. Explicit construction
durability and source virtual attribute overrides, including an empty map, MUST
remain authoritative. Derived callbacks MUST NOT execute on uninitialized this.
Native repair queries MUST dispatch the original source armor predicate using
both actual stacks; dynamic predicates MUST NOT be flattened into item lists.

For the target 26.3 plain humanoid worn path, the source texture callback MAY run
at native extraction with the actual wearer, slot and input stack. The resulting
render state MUST isolate the post-callback stack and selected texture identifier;
it MUST NOT retain the live wearer or invoke the source callback again during
submission. Native pose/model/lighting/outline remain authoritative. Explicit
native equipment assets MUST take precedence over derived source assets. Custom
source models, dye/overlay behavior, dispenser behavior and arbitrary enchantment
callbacks MUST remain explicit gaps until their contracts are separately verified.

Source IForgeItem damage defaults MUST read the live source Damage tag and set
its value with the source lower bound, preserving custom virtual getters/setters.
First source tag access MUST expose existing native damage. Later native damage
component writes MUST supersede the previously observed mirror; source mutations
and removal MUST become visible on damage queries. Native setter completion MUST
mirror the actual source callback result, not its requested argument. Copies MUST
isolate mutable damage data. The native DAMAGE component mirror MUST stay within
the native MAX_DAMAGE representation while source raw overshoot remains visible
to the source broken-item semantics. No fixture identity may select this rule.
