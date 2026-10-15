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
