# Approved source view / native composition

The source Ingredient superclass and Value/ItemValue/TagValue identities MUST be
source-profile views. Native Ingredient is final and MUST remain final. Source
subclass constructors and virtual/super dispatch MUST execute on the real source
object. Native consumption MUST use explicit target-owned conversion to genuine
native ordinary/custom ingredients. It MUST NOT cast the source object to native
Ingredient or drop its callbacks. Unknown conversion boundaries remain fail-fast.

Source values, EMPTY, stack-array queries and source predicates MUST retain their
source meaning. Only proven optional native slot boundaries may turn source EMPTY
into absence; required-slot conversions must reject it. Source display stacks
MUST retain their actual data. Native custom delegates MUST use source test and
isSimple callbacks; native item enumeration follows the approved reload snapshot
policy. Source cache invalidation outside that lifecycle MUST fail explicitly.
Source/native identity, custom callback ownership and epoch/lifetime must be
tracked independently. No process-global cache may retain disposable source
classloaders indefinitely.

The existing source UpgradeRecipe bridge MUST preserve source ingredient field
identity through source views, and retain native codec/placement/matching behavior
through explicit conversions. Normal native-backed ingredient views MUST unwrap
to their exact original native ingredient. Recipe generic containers crossing
this boundary must be adapted where their actual contents are consumed; type
erasure is not proof of compatible element identity. Source serializers and native
registered ingredient codecs remain separate gates. Custom serializers, arbitrary
source field access and unknown native consumers remain explicit gaps.

Verification must include the existing smithing roundtrip checks, immutable
source subclasses/constructor/super/handles, exact stack metadata/display, default
and custom predicates, source/native views, optional/required EMPTY boundaries,
cache/reload behavior, unsupported serialization and actual JEI startup. Linking
the source subclass must not be reported as functional recipe compatibility.

Native snapshot state is attached to its actual native Ingredient, with weak
source-view/provenance references and copied stack data. There is no global
source-object cache. Native operations reject stale epochs and changed source
enumeration/stack data rather than reuse cached membership. Native static-data
TagsUpdatedEvent publication advances the epoch only when shouldUpdateStaticData
is true, avoiding duplicate integrated-client invalidation.

The source Forge empty-tag Barrier diagnostic remains in source stack/display
queries. For vanilla source value construction its diagnostic MUST NOT become a
native accepted tag member: native membership uses the real current tag contents.
Custom predicates retain their actual source callbacks and declared item view.
Source/native JSON and wire codecs remain separate; native vanilla snapshot
codecs do not claim to serialize arbitrary executable source predicates.
