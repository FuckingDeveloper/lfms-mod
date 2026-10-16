# Source GUI listener pointer callbacks

Runtime73 accepts native buttons and tile-event settings but the original custom
screen's coordinate/button click, release and drag callbacks have no native event
entrypoint. Original slider dragging starts/stops through those callbacks;
original comparator source selection also uses them. Keep the immutable input.

For verified source GuiEventListener descendants, exact source click/release/drag
members MUST receive native coordinates and semantically mapped mouse button,
unchanged consumption result and drag deltas once. Managed virtual and source-super
dispatch MUST remain distinct. Existing list/edit-box and widget-hook adapters
retain callbacks they already project. Select by source member identity, ancestry
and descriptor, never fixture identity or descriptor alone.

The native MouseButtonEvent and click double-click flag MUST be scoped across
source callback execution and native source-super delegation. Preserve its actual
modifiers and gesture information; nested/failed dispatch MUST restore the previous
context. Explicit source coordinates/button remain authoritative if forwarded
with changes. Calls with no originating native event represent a single explicit
source activation with no supplied modifier bits; they MUST NOT synthesize a
native double-click gesture. This follows the existing source list/edit-box ABI.

Source listener interface defaults are false in the pinned source. Native-super
routing MAY use the corresponding exact native event operation where its actual
ABI exists, preserving nonvirtual dispatch. Unsupported special handles, callback
collisions and missing target members MUST fail explicitly. Virtual/interface
handles and nested dynamic constants retain their exact source receiver types.
No networking packet, comparator formula, world data or mod-specific GUI geometry
is changed. Wheel normalization remains the source-gui-scroll-dispatch contract;
source slider hit testing and integer delta policy remain source-owned.

Compile and execute independent inherited/source-super/handle/default fixtures,
context isolation and existing GUI regressions before client verification.
Verify original component-screen entrypoints and original slider click/release/
scroll code, then human panel behavior and actual server comparator_data.
