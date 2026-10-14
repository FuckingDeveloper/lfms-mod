# Accepted native placement design

Policy A was selected by the user for both trees and ores. This change fits the
existing source-profile / target-runtime boundaries; it does not load source
Minecraft classes, modify legacy inputs or provide an automation framework.

## Source intent and target binding

The source feature kind and configuration remain an explicit pair. The supported
TREE and ORE kinds bind to a target feature operation. Ore intent becomes a native
OreFeature with ordered replacements, size and discard probability. The removed
static ore-rule defaults project to the exact native ore-bootstrap rule family,
including height-specific stone/deepslate rules.

Tree intent retains providers, placers, roots, minimum size, decorators,
ignoreVines and forceDirt. A native registered TreeIntent codec retains source-only
flags alongside native tree settings; this is necessary because a custom source
callback may read forceDirt. The codec does not serialize an arbitrary source
class name or invent a tree algorithm. Placement executes native TreeFeature.
Ordinary soil uses the native CANNOT_REPLACE_BELOW_TREE_TRUNK predicate and the
source-selected provider; forceDirt keeps the explicit provider request.

## Dispatch and lifetime

Only source subclass superclass/constructor boundaries become native subclasses
with source dispatch. Public TrunkPlacer/FoliagePlacer value types remain native:
relocating those entire classes would incorrectly narrow every configuration
constructor and reject ordinary vanilla placers.

The proven source type, trunk-placement, foliage-placement, foliage-height and
skip-location overrides retain source execution. Native setters feed back to the
real native trunk/foliage tracking implementations. A nested thread-local placement
scope pairs the exact effective TreeFeature, source configuration and WorldGenLevel.
A source provider lookup consumes that world and the original random/position.
The scope restores on success and every failure. No world is retained afterwards.

## Registration and native data loading

Source FEATURES selects native FEATURE_TYPE, not the data registry FEATURE.
Custom placer codec wrappers are accepted only when the actual source Codec is a
MapCodecCodec (including RecordCodecBuilder.create). Map structure/keys survive;
a non-map codec remains an explicit gap. The annotated source trunk registration
invoker projects to target construction and the existing scoped pending registry
mechanism. No global native registration helper is called out of phase.

Source configured/placed registrations collect typed declarations under a recorded
source owner until native data loading starts. This collection is not a native
Registry implementation. An immutable, lower-priority native data pack encodes
features/placements through native codecs. Failed encoding does not consume
pending declarations; late/duplicate declarations fail explicitly. Native datapack
loading, overrides and freeze remain authoritative.

The source biome-modifier resource directory projects per artifact from
forge/biome_modifier to neoforge/biome_modifier, preserving bytes, identifiers,
artifact order and native entries already present in that artifact. Source modifier
callbacks retain their original three-argument virtual method. A native four-
argument wrapper enters the supplied RegistryAccess and calls the original virtual
callback via a typed thunk. Holder-based source builder calls project to native
PlainBuilder. Named legacy placed holders resolve against that exact native data
lookup, retaining reload and pack override identity; unnamed native/source direct
holders stay direct. No process-global world registry is substituted.

## Verification boundary

Native smoke checks cover actual engine callback dispatch, argument/configuration
identity, setter forwarding, false results and thrown callback scope restoration;
native tree-codec flags/providers, ore replacement settings and source definition
publication; transformed source factories, handles, phase keys and builder value
types; immutable external placer/modifier linkage when the fixture is present.
Runtime world growth, actual biome modification and native ore placement require
the separate client/MCP acceptance recorded in tasks.md.

This is not complete worldgen compatibility: arbitrary source Feature subclasses,
non-map placer codecs, additional configuration kinds and unadapted provider/root/
decorator/soil override families remain gaps rather than substituted implementations.
