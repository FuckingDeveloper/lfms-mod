# Approved same-frame source world rendering

User approved optionA: source callbacks execute during target frame extraction;
immutable geometry is uploaded before passes and drawn in the corresponding
native stage of that same frame. Native subscribers retain their original timing.
Read proposal.md for evidence and the explicitly accepted timing difference.

Use a source-shaped event, not a synthetic target event with invented target-only
fields. Expose actual frame camera, renderer, partial tick and prepared frustum;
projection is an owned copy with stable source identity, source pose is local and
native view rotation remains authoritative at draw. Source render-tick queries
remain fail-fast until an equivalent native counter is established; never use
world time as a guessed substitute. Supported stage projection remains the
previous five proven source/target identities. Nonempty sky batches and OIT paths
without a proven matching pass remain explicit boundaries, not silently discarded.

Source Tesselator/BufferBuilder may pass through managed helper descriptors,
casts and callable constants using consistent source-shaped ABI objects. This
extends the earlier method-local-only GUI restriction: it does not authorize
passing these objects as native target builders. Existing local GUI batches keep
their own adapter. At runtime a source batch selects one active extraction domain;
GUI and world state must remain separate, including nested/failing callbacks.
Shared RenderSystem operations capture intent in the selected scope, never
mutate GPU-global state. Unknown source methods/formats/states fail explicitly.

First proven world set: POSITION_COLOR with the source position/color shader,
triangles/quads/triangle strip/fan and native debug lines/line strip. Convert
polygon modes to explicit triangles preserving winding and vertex order. Source
expanded thick-line modes, lit/textured primitives and persistent cache draws
remain separate boundaries until their full shader/channel/lifetime contracts
are proven. Depth uses the native reversed-Z comparison; disabling depth testing
also disables writes as source GL does. Preserve blend equation, culling, color
mask and shader color at batch completion.

Store only copied vertices/state in the frame's render data. Upload via the
native device during PrepareRenderBuffersEvent; draw with the provided active
RenderPass, actual native projection/uniforms and event model-view rotation.
Never open a pass inside an active pass or upload there. Owned GPU buffers close
after native AfterLevel, with partial-upload cleanup on failure. No live world,
player, source event or managed callback is retained in submitted geometry.

Tests must cover source ABI helper/lambda flow, GUI regression, domain isolation,
source stage/callback priority and once-only delivery, immutable geometry, topology,
finite coordinates, source transforms, pipeline state, failure restoration and
native resource lifecycle. MCP functional tests and human visual acceptance are
separate requirements; compilation alone proves neither.
