# Source container views and native container persistence

Scope: immutable Forge 1.19.2 binaries on the pinned 26.3 target. No artifact
identity selects compatibility behavior.

## Source loader containers

Source ModList and ModContainer references MUST use source views across fields,
method descriptors, casts, lambda handles and nested dynamic constants. Native
containers are wrapped without changing FML membership. Managed views represent
actual runtime sessions and original TOML metadata; they MUST NOT inherit the
host LMS identity, config ownership or extensions. Native extensions retain their
real native owner; managed extensions remain scoped to their source owner.

Enumeration MUST preserve native loader order and stable dependency order of
initialized managed sessions. A currently initializing session MAY resolve its
own container through the lifecycle context; blocked/closed sessions MUST NOT be
reported loaded. Cycles/conflicting identities MUST fail explicitly. Snapshot
iteration MUST tolerate callbacks that register extensions. Source view registries
MUST release session references when sessions close. Missing metadata/API contracts
MUST fail explicitly rather than return arbitrary nulls. Native metadata remains
native metadata; managed TOML metadata is a source projection, not native ModInfo.

Source config inspection MUST preserve declared type and actual native ownership.
Changed native config APIs require explicit access/save/event semantics, not a
namespace rename. Initialization and editable GUI acceptance are separate tests.

## Native container base persistence

Source superclass NBT callbacks MUST preserve base BlockEntity data, custom name,
lock and optional loot table/seed. Source JSON component strings and source name
locks MUST be converted semantically to target codecs/predicates; copying raw
source tags into target persistence is insufficient. Native state remains the
authority, and unsupported nonrepresentable reverse projections fail explicitly.
Source helper load/save methods and inherited receivers belong to the same family.
No fake inventory, removed superclass call or fixture-specific branch is allowed.

## Verification

Use focused source/native boundary tests, wrapper compatibilityTest and native
smoke, then fresh runClient and MCP world/content/container checks. Preserve all
pre-existing user changes and original JARs. No MCP changes or new automation
framework. Unsupported subsequent families remain diagnosed explicitly.

### Target predicate representation

The target has no built-in predicate for a formatted custom name's plain text.
Register `lms:source_plain_name_lock` through native RegisterEvent as a serializable
data-component predicate, with a nonempty-stack count condition. It MUST compare
CUSTOM_NAME.getString(), preserving source behavior regardless of text formatting.
The target LockCode and native container fields remain authoritative. Reverse
projection of arbitrary native predicates to a source string MUST fail explicitly.
Source superclass persistence invokes the verified native BlockEntity base once,
then projects name/lock semantics; the original source inventory/loot callbacks
remain in control. This is a source-generation rule, not a reference-mod patch.

### Widget narration

A source AbstractWidget narration override supplies the target widget narration
hook, while the native final updateNarration wrapper retains tooltip narration.
Only verified widget inheritance and the exact source callback descriptor permit
this migration. Unrelated NarratableEntry implementations keep their own contract.

### Shared dependency class identity

Within this one admitted source profile, a dependency artifact already owned by an
initialized session MUST reuse that session's class identity. Defining the same
library again in each consumer creates separate singleton/config registries and
incompatible callback types. The profile supplies an immutable map of exact
normalized artifact paths to owning classloaders before a consumer links classes.
The generic classloader MUST prefer its primary artifact, delegate only matching
admitted dependency entries, propagate failures and release delegate references
on close. This establishes one-profile dependency identity, not multi-generation
coexistence. Unowned library dependencies retain existing local transformation.

### Remaining construction siblings

The source BlockEntityType builder stages the exact factory/valid-block set and
uses the existing verified native direct-construction operation at build. Removed
DFU metadata has the same documented boundary as direct source construction.
Source one-argument MenuType construction uses target VANILLA_SET, preserving
factory identity and invocation timing; callable constructor references use the
same operation. DistExecutor unsafeCallWhenOn MUST call the source Callable,
retain side laziness, return source null on the opposite side, wrap Exception as
the source does and propagate Error. Supplier and Callable are not interchangeable.

### Screen registration lifecycle

Source MenuScreens.register intent MUST be staged until target
RegisterMenuScreensEvent, preserving exact type/factory identity. Pending entries
MUST be removed only after successful native registration; partial failure MUST
retain the failed and remaining entries. Duplicate source intent and registration
after the completed target phase MUST fail explicitly. The adapter MUST NOT invoke
the private native register method or mutate the native screen map directly.
The native mod-list service MAY offer actual registered source config factories
for managed display records; it MUST preserve the native service for native rows
and MUST NOT invent a factory when the source owner registered none.

Source session admission MUST use stable dependency order before linking consumer
classes, not JAR filename order. Present active-side mandatory dependencies and
explicit AFTER order precede their consumer; explicit BEFORE reverses its declared
edge. Cycles or conflicts with the required initialized-dependency model MUST fail
explicitly. This implementation does not claim arbitrary cyclic source graphs.

The actual target screen event precedes FMLClientSetupEvent. A whole client setup
listener MUST NOT be moved earlier merely to make screen registration work. A
source callback MAY be relocated to the actual target event only when its complete
bytecode proves it consists exclusively of registry-holder reads, verified screen
constructor factories and menu-screen registrations, without event reads, branches,
exception handlers or other calls. Its lambda handle/SAM descriptor MUST move
consistently and every registration MUST receive the genuine native event. Mixed
callbacks and unsupported late calls remain explicit gaps. No dummy lifecycle
event or direct private-map mutation is permitted.

For a straight-line listener consisting only of proven registry-holder/factory
registration slices, LMS MAY partition menu-screen intent from the surviving
public block-renderer registrations. Move only the complete menu slices to a
synthetic native-event listener on the same source bus; retain the original
listener and all other registrations in their original phase. Each slice must
consume/produce a closed operand stack and use a verified constructor factory.
Any branch, handler, event read, capture or unrelated operation rejects this proof.
This extends the declaration-only case without relocating a mixed setup body.

A source renderer whose generic parameter already erases to BlockEntity has no
compiler-generated bridge. The existing extraction adapter MAY supply a forwarding
thunk for the exact verified source render operation, retaining the original
virtual source method. Missing ACC_BRIDGE is not evidence that rendering is absent.
The extracted geometry/state model remains unchanged; linkage of this thunk does
not establish every rendering operation used by the source renderer.

### Automatic source event subscribers

After the source mod constructor, index original own-artifact class annotations
for Mod.EventBusSubscriber, using the source default FORGE bus, both physical
sides, explicit modid or same-class @Mod owner fallback. Filter owner/side before
loading a class. Install matching static subscriptions through the existing typed
subscriber index and source owner scope. Do not enumerate all reflected methods or
link wrong-side classes. An installation failure MUST propagate and roll back that
automatic installation; session close MUST remove automatic subscriptions and
release class references. This is source FML injection, independent of fixture IDs.

### Per-atlas sprite intent

Expose an appended sprite-source definition for every actual target AtlasManager
entry, including atlases registered by native mods. Carry the definition ID in the
source codec; the target manager resolves the genuine atlas/texture identity.
Invoke source Pre before stitching that atlas, materialize exact requested sprites
through native SingleFile sources, then preserve the existing verified Post checks.
Do not hardcode chest or reference-mod atlas IDs. Source Pre dispatch is serialized
even when the target schedules atlas preparation concurrently. Original artifact
resources and existing native sprite-source definitions remain unchanged.

Source renderer-context getters for the unchanged block-entity dispatcher, entity
dispatcher, model set and font slots MAY use the target record accessors with the
same verified service identity and descriptor. Apply the rule to calls and callable
constants together. Removed block/item immediate-dispatch services remain distinct
semantic boundaries; record naming alone does not establish equivalent services.

### Source inventory NBT

Adapt both source ContainerHelper.saveAllItems overloads and loadAllItems through
one verified source slot/stack schema. Preserve returned destination identity,
unsigned byte slot reads, byte slot writes, source empty-write policy, untouched
unlisted slots and out-of-range filtering. Direct calls and callable constants
MUST use the same rule. Reuse the proven source stack-tag projection for wire and
persistence; unknown native component projections MUST fail explicitly rather
than silently drop data. Source item serialization MUST retain proven damage,
custom name, enchantments and arbitrary source data alongside count/identity.

The verified source DoubleBlockCombiner operation retains its native algorithm and
callbacks. Project the removed DirectionProperty to EnumProperty at the source ABI,
then pass that same property to the target's widened Property parameter. A typed
source bridge MUST retain the projected source descriptor for exact method-handle
invocation; merely widening the handle's parameter descriptor is insufficient.
Owner/member identity and complete source descriptor select this rule.

### Atlas materials during geometry extraction

A source material's explicit atlas/texture pair maps to target SpriteId. During
native block-entity extraction, provide the actual published SpriteGetter to the
recording buffer, resolve that pair and wrap its recording consumer with the native
SpriteCoordinateExpander. Preserve source render-type factory caching and atlas
texture selection. The immutable submission state MUST contain completed numeric
geometry/UV data, not a live sprite getter or block entity. Unscoped bake-time
sprite lookup and unproven foil rendering remain explicit boundaries. The target's
unqualified MaterialBaker lookup is not equivalent to arbitrary explicit atlases.

### Entity implementation relocation evidence

Derive source/target entity class identity by matching canonical entity registry
IDs and direct constructor factory handles in the pinned vanilla registry bytecode.
Trace only verified builder receiver chains whose fluent methods return this;
opaque factories, missing IDs and ambiguous implementations remain unadapted.
The target's ResourceKey declarations MUST resolve through the verified ENTITY_TYPE
key constructor, not field-name coincidence. Record reproducible constructor
pairs and class hashes; use immutable indexed forward/reverse maps in transformation
hot paths. Apply exact class tokens to descriptors/signatures/casts/handles and
retain source identity for SRG resolution. Do not infer nested-class aliases from
a relocated outer class or claim all entity behavior from structural identity.
`tools/GenerateEntityRelocations.java` reproduces this evidence without loading or
modifying original source/target classes.

### Container users and opener tracking

Preserve source Container.startOpen/stopOpen(Player) callbacks while routing source
calls through the target ContainerUser contract. Native callbacks into managed
containers MUST deliver the original Player identity once; nonplayer native users
MUST fail explicitly until a source interpretation is designed. Source superclass
calls retain special dispatch. Generated call/handle thunks retain exact source
receiver/parameter types, including inherited owners and nested constants. Only
verified Container inheritance and the exact source member descriptor select this
family. Counter increment/decrement retain the native engine, supplying the actual
player interaction range to the changed increment operation. No source chest logic
or fixture identity selects adaptation.

### Screen gameplay-input gate

The pinned target KeyboardHandler and Minecraft tick route gameplay input only
when no screen exists. Source Screen.passEvents=false therefore preserves this
native policy without additional state; reads return that actual blocked policy.
A true write MUST fail explicitly until input passthrough is adapted. Exact source
field declaration (including hidden-field negatives) selects the bridge. Callable
field references MUST retain source receiver/value types. isInGameUi controls
background/HUD behavior and MUST NOT be substituted for gameplay-input routing.
