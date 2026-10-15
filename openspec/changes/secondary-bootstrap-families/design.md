# Secondary bootstrap families

Source scope: Forge 1.19.2. Target: Minecraft 26.3 / NeoForge 26.3.0.58-beta,
Java 25. External CarbonConfig and Iron Chests binaries are immutable evidence.
Both are currently blocked before constructor invocation by exact API preflight.

The source ChannelBuilder's name, lazy version supplier and acceptance predicates
MUST feed the existing LMS channel protocol operation unchanged. The optional
NetworkDirection overload MUST retain explicit play direction and reject login
phase registrations until that phase is supported. Callback threading and payload
ownership remain those of the existing network adapter; no namespace-only network
replacement or universal protocol compatibility is claimed.

Source DeferredRegister intent MUST execute through the native deferred lifecycle.
Source holder identity MUST survive supplier/method-reference/field boundaries;
early access, duplicate attachment, duplicate names and late registration MUST
fail explicitly. Source callbacks MUST retain their recorded owner, registration
phase and declared construction identifier, before native constructors need it.
Native registry mutation occurs only under actual RegisterEvent. Source factories
are not eagerly invoked merely to build holders. Existing builtin holder behavior
MUST remain unchanged. Custom-registry creation remains a separate gap.

Preflight changes require evidence of executable adapters. Dormant datagen code,
input-event code and reflective loader internals are distinct contracts; none may
be declared supported just to make the constructor run. Functional chest/menu,
configuration editing and networking acceptance remain separate from initialization.

## Accepted keyboard semantics

The user selected named-key intent on SDL. Source KEYSYM declarations are indexed
from the pinned 1.19.2 InputConstants.Type registration bytecode and mapped by
translation identity to actual target keys. Constructors, integer matches and
InputConstants key creation/value queries MUST use this same mapping. Key events
expose source logical key identity with a normalized SDL physical code; this code
MUST NOT be advertised as an unchanged OS-specific GLFW scan number. Raw source
SCANCODE bindings remain fail-fast. Unknown target keys may project to source
UNKNOWN; they MUST NOT accidentally activate an unrelated binding. The source
modifier flags and action values retain their meanings. Source keyboard subclasses
and arbitrary raw GLFW integrations remain separately unverified.

Named loader/library reflection may use actual unchanged declared fields. Source
SRG reflection without a verified mapping MUST fail explicitly. The removed
ModContainer.configs field projects to a fresh source-shaped EnumMap from the
actual target ConfigTracker ownership; zero configs is a real empty result,
multiple configs per source type or native-only STARTUP types remain explicit gaps.
This snapshot does not support mutating native config ownership through reflection.

## Runtime versus data-generation reachability

The constructor planner MUST distinguish a LambdaMetafactory Consumer whose source
callback takes GatherDataEvent from a direct constructor/runtime call. A datagen
callback and its transitive calls are reported INACTIVE_DATA_GENERATION in ordinary
client/server mode, not SUPPORTED. Any independent runtime path to the same API
MUST continue to require a real adapter. Visits MUST retain phase so an earlier
inactive walk cannot hide a later active path. This does not implement runData.

Source datagen listeners remain registered under an uninstantiable explicit source
phase event type; no listener body is removed. Attempting to construct/emit that
source event fails. The existing runtime has no source datagen emitter. This is a
phase classification, not a blanket exclusion by package or method name.

## Admission before partial initialization

A verified unsupported source native-container superclass load/save operation
MUST reject admission before constructor execution. The current conservative
artifact scan recognizes exact BaseContainerBlockEntity and
RandomizableContainerBlockEntity source INVOKESPECIAL NBT descriptors. It scans
all artifact classes, including potentially unreachable code; this limitation is
preferable to installing listeners from a partially completed constructor. It
does not claim general constructor/static-initializer reachability analysis.
Custom name, lock and loot persistence require a separate semantic migration;
calling target base persistence with unconverted source NBT is not equivalent.

Verified source ForgeRegistries fields for sound, potion, particle, menu, recipe
type, attribute and stat type resolve to their actual target BuiltInRegistries
fields. Registry identity and native field descriptors MUST be preserved; this
static alias family does not enable registration outside the target lifecycle.

## Resource admission

A rejected entrypoint MUST NOT independently publish its content packs: its
unregistered block/item names would poison native data tags. Admit the owning
artifact only after successful session initialization. Resources from actual
managed dependencies of an initialized session remain available, even when that
dependency's optional own entrypoint is blocked; binary dependency availability
and entrypoint initialization are separate existing milestones. Do not delete
resource entries or rewrite the immutable input to mask missing registration.
