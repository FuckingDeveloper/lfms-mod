# Contract

Source EditBox construction and source direct subclasses MUST use a target EditBox
subclass retaining actual native text, cursor, selection, limits and renderer.
Descriptors remain native EditBox so target-produced text fields remain genuine
native values. Applying removed source operations to an unadapted native text
field MUST fail explicitly. Native-only text fields remain unchanged.

The source predicate MUST validate before any value/cursor/selection mutation or
responder notification. setValue validates the untruncated argument; insertion
and deletion validate the resulting candidate. Rejection and predicate failure
MUST leave state unchanged. Native text sanitization/codepoint-safe insertion is
the target policy; source integer limits retain UTF-16 units.

Preserve verified source responder timing/count, including source setValue's
cursor-movement notification followed by its explicit argument notification.
Cursor movement notifies once, insertion once, deletion once. Native max-length
truncation retains source bypass of the predicate and its own notification.

Native input MUST invoke source overrides once, preserving super dispatch and
native event identity when unmodified. Named keys/modifiers follow the existing
GLFW-to-SDL semantic map; raw GLFW scan codes remain unsupported. Source char
callbacks admit BMP events; supplementary native events require explicit design.
Source source-formatter replacement MUST retain the live callback and actual text
and offset. Source blink ticks need no mutation because native blinking uses
elapsed time; source overrides/call dispatch remain present.

Calls and normal method/constructor handles MUST retain verified ancestry,
member identity, descriptors and source handle types; managed method hiding
MUST NOT be bypassed. Unsupported cases fail explicitly. Verify filter rejection,
exceptions, selection replacement, deletion, callbacks and immutable consumer
construction before dev-client acceptance.
