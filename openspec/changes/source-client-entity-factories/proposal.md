# Source client entity factories over native spawn packets

Runtime110 links original seating factories whose JavaFML source callbacks accept
PlayMessages.SpawnEntity. Target ClientPacketListener.createEntityFromPacket has
the actual native packet immediately before EntityType.create. Preserve the
source callback, source owner and real client level at that exact construction
boundary, then let native recreateFromPacket/publication proceed once. Do not
replace a custom callback with the default factory, supply null packet data or
manufacture an entity merely to advance registration.

Capture the builder's source factory and transfer intent to its actual constructed
native EntityType before registry publication. Keep it on the native object rather
than a global loader-retaining map. Source entity spawn-packet overrides execute
under the actual native ServerEntity context; the verified NetworkHooks factory
creates a real native add-entity packet. Native spawn simulation/publication is
a continuation of the approved current-engine policy, not legacy wire support.

The source packet view preserves fields with established native counterparts.
Legacy additional spawn-data buffers and encode/decode are separate unsupported
contracts; unknown consumers fail explicitly. No old wire protocol, extra-data
empty substitute, custom entity persistence or source-class bypass is claimed.
Verify independent callback/type/level/packet identity, null/failure results,
source owner/super dispatch, native fallback and fresh loaded-world seating.
