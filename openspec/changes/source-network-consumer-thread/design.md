# Preserve the plain source packet-consumer execution boundary

Source: Forge 1.19.2 / Forge 43.4.0. Target: Minecraft 26.3 /
NeoForge 26.3.0.58-beta / Java 25.

The inspected source SimpleChannel/IndexedMessageCodec invokes the registered
consumer directly from network-message receipt. A source handler may explicitly
enqueue game-thread work through NetworkEvent.Context. The pinned target
PayloadRegistrar defaults to MAIN and wraps registered handlers in
MainThreadPayloadHandler. LMS had implicitly inherited that target default.

Register the supported plain source channel consumer using the native NETWORK
handler mode. Retain native enqueueWork and the existing source-owner context on
both execution boundaries. Do not inspect integration-mod packet identity,
introduce delays/retries, change payload framing, or patch source artifacts.
Unimplemented source builder/login APIs remain separate support boundaries.

Investigation trigger: a freshly placed cable has correct server connections
while the immediate client model snapshot is still zero; a later mesh build
contains the expected connections. This observation does not distinguish a lost
update from a normal delayed field update followed by deferred meshing outside
the view. The consumer-thread mismatch is independently established by
source/target evidence; its contribution to the reported runtime symptom must be
tested rather than assumed.
