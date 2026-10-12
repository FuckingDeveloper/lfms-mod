# Networking and Distribution Specification

LMS MUST distinguish client, dedicated-server and common compatibility behavior.

Legacy networking adaptation MUST preserve packet/channel identity and ordering semantics where required by supported behavior. Client-only packet handlers/classes MUST NOT be eagerly loaded on a dedicated server.

A compatibility declaration MUST state whether the supported configuration requires LMS on client, server, or both.

Protocol compatibility between differently versioned LMS installations MUST NOT be assumed unless explicitly specified. Network capabilities required by a declared gameplay subset MUST be tested before claiming the corresponding compatibility level.

Target-native networking is NeoForge networking. Legacy Forge packet/channel behavior MUST be adapted rather than assumed ABI-compatible with NeoForge.

For Forge 1.19.2 `SimpleChannel#registerMessage` without an explicit
`NetworkDirection`, the source contract accepts that discriminator in either
play direction. A target adapter MAY therefore register one bidirectional play
payload per legacy channel. It MUST preserve the channel identifier, unsigned
byte discriminator framing, message encoder/decoder, target selection and
main-thread context semantics. It MUST validate that both source version
predicates accept the locally advertised protocol. Support for broader
predicate-based negotiation MUST be tracked separately from same-LMS protocol
operation and MUST NOT be inferred from exact target version matching.
