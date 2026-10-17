# Source chunk/light packet snapshots over native lighting

The source terrain job sends ClientboundLevelChunkWithLightPacket after raw chunk
edits. The source constructor includes trustEdges; the target record constructor
and target client light solver no longer carry that flag. This is the next known
static blocker after height queries, not yet a successful runtime path.

Proposed decision: preserve source chunk coordinates, data, light masks and layer
bytes, snapshot through the native 26.3 constructors/codecs and use the native
client lighting algorithm. Evaluate the original boolean operand but do not emulate
its 1.19.2 edge-solver policy. The native solver owns edge consistency and rebuilds.
Do not patch the legacy JAR, send legacy-format packets or substitute an empty
chunk. A source read of the removed flag remains unsupported; no boolean is invented.

This is a semantic choice between new-engine lighting and preserving an old light
solver control. AGENTS.md §22 requires an explicit decision before implementation.
User accepted native Minecraft 26.3 lighting on 2026-10-17. Implementation
may proceed against the bounded design; raw legacy codecs remain unsupported.
