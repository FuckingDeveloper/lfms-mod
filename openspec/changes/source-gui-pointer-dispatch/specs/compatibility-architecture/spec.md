# Source GUI listener pointer dispatch

## Added requirements

Verified source GuiEventListener click/release/drag callbacks MUST participate in
the target pointer-event lifecycle once, preserving coordinates, semantic button,
deltas and boolean consumption. Source virtual and superclass calls MUST retain
their dispatch. Native-only modifier/gesture data MUST survive scoped source
execution and native super delegation, including nested/failing callbacks.

Selection MUST use verified source member/ancestry/descriptor evidence. Existing
list, edit-box and widget-hook contracts MUST retain ownership of their projected
callbacks. Native super methods MUST be proven against actual target ABI, including
when inherited through an unchanged managed intermediary. Unknown special handles,
collisions and absent target contracts MUST fail explicitly. No fixture identity
or comparator-specific behavior may select this rule.
