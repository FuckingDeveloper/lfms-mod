# Source loader-version evidence

Some immutable JavaFML mods declare a language-loader range without an explicit
Minecraft dependency. Discovery currently discards that evidence. Preserve the
actual language-loader ID/range in the normalized descriptor. Each source profile
may admit its own pinned loader generation when the artifact explicitly declares
it compatible. This is compatibility eligibility, not discovery of the exact compiler.
An explicit Minecraft dependency remains authoritative; filenames, resource-pack
formats and timestamps do not establish source compatibility. Multiple matching
installed profiles must still be rejected as ambiguous. Unknown loaders/ranges fail
closed. Prelaunch and runtime use the same descriptor/catalog.

Forge 1.19.2's JavaFML generation is 43 (pinned Forge43.4.0). No source generation
is added. Verify absent/conflicting Minecraft evidence, malformed loader ranges,
wrong loader/major, renamed immutable JARs and ambiguous profiles before runtime.
