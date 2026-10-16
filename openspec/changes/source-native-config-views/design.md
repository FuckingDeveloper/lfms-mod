# Contract

Forge 1.19.2 inspection of native target configs MUST retain the actual native
container/spec/current loaded data/path. Source COMMON, CLIENT and SERVER retain
source enum order/names and project native LOCAL, CLIENT and SYNCED respectively
for inspection of the active native config. Native STARTUP and multiple native
configs of one projected source type MUST fail explicitly. This projection does
not reproduce source serverconfig directory discovery or target startup timing.

Source getConfigData reads actual current native loaded data; unloading is genuine
absence, not a fabricated config. Source save writes the actual file through the
native atomic writer WITHOUT implicitly dispatching reload. Source explicit config
event dispatch MUST target the actual native owner, preserve its lock, and dispatch
exactly once. Native LoadedConfig.save, which already dispatches reload, MUST NOT
stand in for source save. Managed Forge config registration is specified separately by
`../source-config-registration/design.md`; source custom IConfigSpec implementations
remain unsupported. Managed source views use recorded provenance while the native
backend retains its actual host container.

Verified ForgeConfigSpec inspection may project to native ModConfigSpec: path,
default, comment, type/range and value validation getters retain native definition
identity. Removed inherited get(path) reads native getSpec(). Source integer
correct returns the actual native correction count; source afterReload uses the
native supported spec's cache-reset operation. Native GAME restart cannot be
represented by the source world-restart flag and MUST fail explicitly.

Apply class references consistently to descriptors/signatures/casts/handles and
reflective class-name literals. Native event classes remain native; config getters
and source factories project view ownership. No duplicate reloads, skipped configs,
fixture identities, original JAR changes or global registry mutation.
