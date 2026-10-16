# Source sound event values

Runtime57 source iron furnace accepted fuel/input and produced stone. Its active
state packet reaches the original fuel sound callback and fails at
SoundEvent.m_11660_()ResourceLocation. Inspect the full source SoundEvent family:
getLocation/m_11660_, getRange/m_215668_, one/two-argument constructors and the
source flat identifier CODEC/f_11655_. Target26.3 SoundEvent is a final record:
location(), same getRange(float), variable/fixed factories, a direct record codec
and a registry-holder codec. Neither target codec is the source flat codec.

Preserve exact source owner/member/descriptors. Getter and range calls/handles
may use the verified native record accessor/range method. Paired NEW/DUP/source
constructors and constructor handles use native variable/fixed factories. Preserve
nested allocation pairing and reject unknown constructors/target-final subclassing.
Provide the verified flat Identifier codec xmap(variableFactory,location) separately;
do not silently replace source values with holders or registry keys. Callable
constants include method handles, indy bootstrap arguments and nested condy.
No source artifact or fixture-specific branch changes.

Verification pending compilation, executable synthetic family/negative fixtures,
original callback bytecode and another actual furnace activation in a fresh client.

Automated checks passed (`compatibilityTest nativeCompatibilitySmoke
compileLaunchJava`, batch-check-11). Native record execution covers both
constructors and handles, getter/range SRG and named calls/handles, nested resource
allocation, real LambdaMetafactory getter reference, flat-codec round trip and
original immutable fuel callback. Unrelated-owner getter remains untouched;
target-final source subclass is explicitly rejected. The pass runs before
namespace projection so descriptor identity is still source evidence.

Dedicated run6 loads all three unchanged artifacts and its world without new
ERROR/FATAL after this family and empty-stack changes. Actual client furnace
sound and particle behavior remains the next runtime58 check.

Runtime58: Quick Play loads the agreed world. MCP opens the original iron-furnace
GUI using an empty hand, Shift transfers coal and cobblestone, then closes it.
Source state reaches fuel1503/active1/progress97 and subsequently smelts stone.
The previous field-sync payload NoSuchMethodError is absent from the fresh log.
Particle/audio output is human acceptance pending because MCP has no observation
for rendered particles or audible audio; no visual/audio success is inferred.

Runtime58 human acceptance confirms both smoke/flame and crackling from the
working source iron furnace. MCP verifies processing and the absence of the
previous sound-linkage error; human observation supplies the unsupported audio/
particle observation. No ordinary-generator effects are added.
