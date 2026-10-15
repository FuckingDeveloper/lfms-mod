# Source custom item rendering

Forge 1.19.2 custom item rendering and target extracted item rendering MUST be
treated as separate semantic contracts. LMS MUST preserve the source renderer's
geometry and display transforms rather than substitute reference-mod geometry.

For the current target, GUI extraction MAY invoke a complete source renderByItem
override with the proven native GUI light (15728880) and NO_OVERLAY. Extraction
MUST copy the input stack and freeze geometry and actual vertex bounds. Submitted
state MUST NOT retain a live source stack or block entity. Non-GUI lighting is an
explicit functional gap and MUST remain diagnosed until equivalent semantics are
established.

Lazy source extension materialization MAY be used only when original bytecode
proves registration consists exclusively of allocation, pure outer capture and
Consumer.accept. An optional explicit call to the verified empty source Item
initializeClient implementation, including its inherited BlockItem declaration,
MAY be preserved as an empty semantic operation. Arbitrary initialization MUST
fail explicitly. Extension ownership MUST follow the item lifetime; global caches
MUST NOT retain disposable managed loaders through value-to-key references.

Source vanilla renderer fallback methods, reload caches and inherited state are
not admitted by the complete-override subset. Their absence MUST NOT be presented
as full BlockEntityWithoutLevelRenderer support. Source renderItem's boolean
contract MUST be retained: true on renderer absence, false after rendering.

Derived builtin/entity item definitions MUST preserve original model properties
and input artifacts. Only the verified source selection marker MAY be removed
from the derived base. Explicit target definitions MUST take precedence; unknown
loaders and overrides MUST NOT be silently treated as supported.

For the native client block-entity dispatcher, the exact source field-read plus
Camera.getPosition operation MAY resolve to the active target main camera position.
The source dispatcher was prepared with that same world camera; the target updates
its main camera before level and GUI extraction. LMS MUST preserve the receiver
scope and reject a custom dispatcher. Unpaired camera access and other camera
operations MUST remain unsupported; the removed camera object MUST NOT be faked.

GUI atlas identity MUST distinguish actual extracted custom-render arguments,
including geometry and selected materials; equal model display properties and
component patches are insufficient. The adapter MUST preserve native per-frame
invalidation for source callbacks with observable dynamic state.
