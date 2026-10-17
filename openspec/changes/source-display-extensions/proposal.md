# Source display extension declaration — accepted limitation

Pinned Forge43.4.0 DisplayTest supplies server-list ping mod markers and a
remote-version display predicate, not SimpleChannel handshake acceptance.
ForgeHooksClient.processForgeListPingData and ServerStatusPing are the consumers.
NeoForge26.3.0.58-beta/FML12.0.9 removed both this extension and that Forge ping
mod-list model; replacing it with payload acceptance would change its meaning.

Proposed bounded contract: retain original per-source-container lazy extension
suppliers, DisplayTest record/accessors/constants and predicates. Preserve source
registration/lookup ownership and all direct predicate reads. Native network and
connection rules remain authoritative. The removed Forge server-list mod-version
indicator is an explicit unsupported UI/protocol feature; do not report it as
implemented or substitute an always-true predicate. No custom Forge-status wire
protocol is introduced in this batch. The user accepted that explicit limitation before allowing a declaration-only
mod through the startup gate.

Alternative: keep initialization blocked until a complete native server-list
protocol/display adapter is specified and implemented. Config-screen extensions
remain independent and must retain their existing owner behavior.
