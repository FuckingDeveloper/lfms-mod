# Vertical GUI scroll dispatch

The source GuiEventListener mouseScrolled(double x, double y, double amount)
contract MUST dispatch through actual target listeners with horizontal delta zero
and unchanged x/y/vertical delta and boolean consumption. Container routing MUST
retain child hit-testing and first consuming listener. A false child result MUST
remain false so the source parent can execute its own fallback scrolling.

Managed source overrides MUST receive normalized native scroll once. Virtual
calls, interface calls, inherited receivers, method handles and nested constants
MUST retain dispatch and source receiver types. Managed source-super calls MUST
retain special dispatch. Native source-super calls to the exact listener/container
interface defaults MAY invoke the native four-argument operation nonvirtually
with horizontal delta zero: the source and target GuiEventListener defaults both
return false, and ContainerEventHandler defaults both dispatch to the child at
the supplied pointer position.

Selection requires verified listener ancestry, exact canonical source member and
descriptor. It MUST NOT depend on a fixture identity. Existing list/edit-box
adapters retain ownership of callbacks they already project. Callback collisions
and unproven special handles MUST fail explicitly.

Source callbacks carry the source MouseHandler's single normalized axis: use
vertical delta when nonzero; on macOS use horizontal delta only when vertical is
zero; elsewhere horizontal-only input has source amount zero. This exact source
policy MUST apply before source callback dispatch, including native list/entry
adapters. Target mouse-wheel sensitivity/discrete scaling has already run and
MUST NOT be applied a second time. This does not expose a two-axis source API.

Inherited native-super defaults MAY be recognized through managed/source hierarchy
and exact pinned declaration identity, without loading partially transformed
classes. Ambiguous defaults MUST fail. Other native source-super implementations
require separate evidence and MUST remain fail-fast. Keyboard focus traversal
remains an independent unproven boundary.

Tests MUST execute original immutable consumer scrolling through a real nested
entry, including a consuming and declining child, before another client test.
Synthetic tests MUST cover inherited overrides, super dispatch, exact handle
receiver type, interface-default dispatch and unrelated-owner/descriptor negatives.
