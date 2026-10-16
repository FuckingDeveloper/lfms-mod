# Managed source configuration registration

Preserve source owner identity without fabricated native ModContainers or native
FML membership. Use the actual host as the backend event container while source
views/registration provenance retain the real managed owner, type, source default
filename, native config object identity and matching source callbacks. Other source
owners must not receive that config's mod-bus event. Genuine native configs keep
their existing native identity and behavior. Registration must retain native file
collision, spec validation, config locking, file loading/watching and errors.

Source COMMON maps to native LOCAL; CLIENT maps to CLIENT, retaining source file
names `<source-id>-common.toml` / `<source-id>-client.toml`. Both registerConfig
forms and implemented ForgeConfigSpec builder/value families must be covered from
exact source declaration and native ABI evidence. Unknown source spec subclasses
and unproven callbacks remain explicit gaps. Admission and transformation must
consume the same catalogued registration rules.

## SERVER semantics — accepted decision

Source Forge1.19.2 stores SERVER under each world's serverconfig directory. Native
NeoForge26.3 SYNCED uses global config by default, with world syncedconfig override
and native login synchronization. Existing native-to-source inspection maps
SYNCED to SERVER, and managed registration now uses the explicitly accepted policy below.

User accepted native NeoForge26.3 policy. Retain source logical type and default `<source-id>-server.toml` filename,
use the native SYNCED lifecycle/storage/synchronization policy. This changes the
source default storage scope, explicitly approved in this session.

This decision was required by AGENTS.md §22: "multiple target semantics are
materially plausible". COMMON/CLIENT, builder and source-ownership work can proceed
independently. Never assign a managed source config the host LMS mod ID in source
views, patch input artifacts, or pretend a blocked path is supported.

Source ConfigValue set/cache identity and reset-all reload semantics MUST be
preserved over native value objects. Native restart-specific cache retention must
not silently change source reads. Managed specs use a native IConfigSpec wrapper
for loader callbacks, while source views return the original spec identity.
Unproven list-correction, custom-spec/subclass and blank-comment variants remain
explicit boundaries rather than being approved by descriptor similarity.

## Evidence and bounded migration

Source headers include the immutable Forge 43.4.0 named patched artifact plus its
separately shipped fmlcore/javafmllanguage 43.4.0 artifacts. All three inputs are
SHA-256 pinned by GenerateSourceHierarchy; missing context declarations MUST NOT
be replaced with guessed methods. Actual target ModConfigSpec, ConfigTracker,
LoadedConfig and ModConfigEvent on NeoForge 26.3.0.58-beta/FML 12.0.9 determine ABI.
Source member ownership and inherited declaration lookup remain profile-owned;
native ABI inspection and file execution belong to the installed target binding.

Builder construction, scalar definitions/ranges/enums/allowed-value collections,
metadata, category stack and configure/build retain native objects. defineList
correction and custom subclasses/specs are unsupported and fail explicitly.
Blank comment variants are not supported: target validation remains authoritative;
this contract does not silently normalize or discard source comments.

Typed lifecycle facade values preserve helper parameters, fields and callable
references. Their methods retain the existing LMS owner-scope token boundary;
escaped tokens fail. Registration under a different active-container override is
explicitly unsupported rather than silently assigned to the calling mod.
Duplicate spec/type registration fails; empty specs are skipped as in the source.
Native collision/validation failure MUST leave managed provenance unregistered.
Registered configs follow the native loader's process lifetime; this change does
not promise dynamic config unregistration or hot-unloading managed mods.

Source reflection of a managed container returns only its own configs; native
host reflection excludes managed backend registrations. Native listeners remain
on the native bus, source listeners retain priority/unregister and ownership scope
on an isolated per-owner bus. Explicit managed config event dispatch checks source
provenance and uses the real native owner lock/event path exactly once.

Source get/set/save and reload operate on exact native value/spec identities.
Save uses the target atomic file writer without manufacturing reload events,
including when source code inspects a genuine native spec. The native loaded
config record supplies its exact owner. Native-only calls retain native behavior.

The proven source pre-load default fallback is implemented for catalogued concrete
ConfigValue.get calls/handles. Calls made through an erased Java Supplier before
loading in a production runtime are not separately adapted or accepted by this
checkpoint; they remain a semantic gap. Loaded managed values and loader-driven
reload/unload caches are verified independently. A responding MCP bridge without
a server/player does not establish config/gameplay readiness.
