# Native lighting policy — accepted 2026-10-17

Evidence: source ClientboundLightUpdatePacketData stores trustEdges, serializes it
and passes it to LayerLightSectionStorage.queueSectionData. false adds an untrusted
section for the old edge consistency pass. Target packet data and queueSectionData
have no such operand or storage. Native client applyLightData/readSectionList owns
queued section updates, enabling lighting and neighbor mesh rebuilds.

The accepted native policy admits a bounded family comprising exact source chunk
with light, light-only and light-data constructors and proven read-only coordinate,
chunk-data, mask and layer-list accessors. Preserve source operand evaluation and
constructor initialization safety. Direct constructions and constructor references
must use actual native snapshot constructors; source packet subclassing and legacy
raw FriendlyByteBuf codec bodies remain explicit unsupported boundaries.

Each mapping requires exact source declaration/descriptor and actual target ABI.
No raw source SimpleChannel codec is inferred from native packet compatibility.
The native wire format, registry context, side/thread checks and normal send path
remain authoritative. Do not replace a full-chunk update with a different update
algorithm without evidence. Do not emulate source light propagation in LMS.

Verification must cover constructor operands/once-only effects, native masks/data
and getter identity, original artifact immutability, decoding boundaries, safe
bytecode/callable references and actual loaded-world chunk refresh.
