# Contract

Source screen height and viewport top/bottom MUST remain distinct. Construction
MUST initialize the native viewport to bottom minus top, retaining source screen
height for source reads. Source field reads/writes MUST respect declaration and
field hiding; native geometry MUST synchronize before extraction and input.

Native extraction MUST invoke source render/renderList/renderItem/entry callbacks
with original coordinates, row index, hover and partial tick. Source superclass
calls MUST retain special dispatch. Draws use the existing scoped GUI extraction
bridge; list backgrounds/separators use target presentation and source enable
flags. No duplicate row rendering or disconnected entry snapshots are permitted.

Source children returns the actual mutable native tracked entries, retaining native
entry ownership. Source pointer callbacks MUST receive actual coordinates and
buttons once; callbacks and superclass dispatch MUST remain distinct. Keyboard
navigation whose generation semantics are unproven MUST fail explicitly.

Selection requires verified source ancestry/member identity and descriptors, never
fixture identities. Method handles and nested constants follow the same projection.
Unimplemented family operations MUST fail closed; reaching a screen is not full
functional acceptance. Verify immutable fixture construction and synthetic callback,
geometry/mutation, hidden field and pointer dispatch before real client acceptance.

Source framebuffer scissors use bottom-left pixel coordinates. GUI extraction
MUST project them to the target integer logical GUI grid using nearest-boundary
rounding. Repeated enables replace the current source clip, rather than nesting
it. Disable and exceptional scope exit MUST restore the enclosing native clip.
This does not claim sub-GUI-pixel clip precision unavailable on the target.

Inherited source AbstractWidget.render calls made by row callbacks MAY invoke the
native extractor through the existing planar GUI bridge. Selection MUST verify
source declaration ownership and MUST NOT bypass managed render overrides. Source
planar pose, visible/hover lifecycle and native content callbacks are retained.
Unproven special handles and managed full-render overrides remain unsupported.

Inherited source BufferBuilder fluent vertex/channel calls MUST use the same
verified VertexConsumer semantic operations as interface-owned calls. A GUI batch
MUST NOT leave calls requiring the original BufferBuilder receiver after its
producer has projected to the scoped GUI buffer. Source identity and verified
ancestry, not descriptor coincidence, select these operations. Managed overrides
and escaped concrete-buffer handles remain separate unproven boundaries.

The source AbstractSelectionList `isFocused()Z` extension hook MUST remain separate
from target AbstractWidget focus state. Its source base implementation returns
false; source overrides, virtual calls, superclass calls and method handles MUST
retain source dispatch through `lms$isFocused`. Initial narration of the immutable
source list MUST be exercised before rendering, and residual SRG list calls MUST
be audited rather than limiting linkage checks to already projected operations.

## Focus and selection ownership

Runtime62 trace records two consecutive source selection notifications for a single
pointer action: the first pops the dropdown, the second pops its parent. Source
row activation explicitly selects; it then sets list focus. Target26.3
AbstractSelectionList.setFocused additionally invokes virtual setSelected for a
tracked entry, and ContainerObjectSelectionList.setFocused additionally clears
selection on null focus. Neither operation exists in the pinned source list:
source AbstractSelectionList/ContainerObjectSelectionList inherit
AbstractContainerEventHandler.setFocused, which only assigns its focused field.
Source AbstractSelectionList.setSelected likewise only assigns selected; target
setSelected additionally inspects clipping/input mode and may scroll.

For source list views, focus and selection MUST remain independent. setFocused
MUST assign the actual native focus field without synthesizing selection, child
focus callbacks, clearing nested focus or clearing selection. setSelected's source
base MUST assign the actual native selected field without implicit scroll. Source
overrides retain their virtual callbacks; explicit selection/scroll operations
remain authoritative. Native-only lists keep their native policy. This is a
source-generation setter contract, not suppression of duplicate notifications by
value equality or a special case for CarbonConfig. Source setters may legitimately
notify again when explicitly called with the same entry. Verification MUST cover
pointer-triggered once-only selection, repeat activation, focus change/null,
selected/focused divergence and native storage identity.

Verification: wrapper `compatibilityTest nativeCompatibilitySmoke compileLaunchJava`
passed in `/tmp/lms-gameplay-batch-check-22.log`. Native pointer dispatch selects
once then focuses, explicit repeated clicks still notify, focus changes/null
preserve independent selection, selection clears explicitly, source setters retain
native getter storage and no child focus callbacks are invented. Check21 caught
an incorrect focus-field declaring owner before any client run: target list storage
belongs to AbstractContainerWidget, not the source-era AbstractContainerEventHandler.
The corrected target owner is verified through actual native getters. Runtime63
runs with layer trace to confirm the same original dropdown callback now pops once.
