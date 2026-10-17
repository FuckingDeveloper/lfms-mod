# Timing identity and explicit data boundary

Source RecipesUpdatedEvent identity MAY project to the configured target's real
recipe-reception event. For the current source/target tuple this is Forge43.4.0
RecipesUpdatedEvent -> NeoForge26.3.0.58-beta RecipesReceivedEvent. The target event
is on the client main bus after recipe reception, including native empty/subset
notifications. LMS MUST NOT emit extra events or infer a full client recipe map.

The projection MUST cover class literals, generic signatures, method descriptors,
annotations, casts, arrays, method handles and nested constants using the same
namespace projection as source scan metadata. Ordinary getClass/class-token
comparison and actual native event-bus registration MUST retain one identity.

Source payload getter calls/handles, event construction, direct subclassing and
unproven event-specific member access MUST fail explicitly before source execution.
They MUST NOT be silently redirected to the native subset map. Inherited Object
identity operations MAY retain their native meaning. Unknown reflection over
removed payload/constructors remains unverified and MUST NOT be described as
supported. Other event families and recipe data/codec/network behavior are not
established by notification compatibility. Dedicated-server discovery MUST NOT
resolve client classes merely because their tokens occur in a source JAR.
