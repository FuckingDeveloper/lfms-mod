# Source custom-widget rendering on extracted native GUI

The pinned target AbstractButton final widget extractor delegates to extractContents
then handles the cursor; it does not draw a default backdrop implicitly. A verified
source AbstractWidget renderButton override MUST project to native extractContents
for buttons (or extractWidgetRenderState for non-button widgets), entering the same
scoped primitive extraction service as container graphics. Original virtual render
callbacks, inherited source-super calls, visibility/hover fields and native cursor
handling MUST survive. Native final extractors MUST NOT be overridden.

Source default getYImage uses the actual active field and supplied hover flag.
The original empty renderBg extension callback MAY remain empty only when no
managed declaration exists in the hierarchy. Source mutable blit-depth storage
is receiver-owned; nonzero depth rendering remains an explicit gap. Unknown full
render/tooltip/input callbacks are not inferred to be migrated.

Verified inherited GuiComponent centered text uses source integer centering and
shadowed native text extraction. Managed hidden members MUST retain ownership.
Forge ScreenUtils nine-slice borders tile source pixels with clipped remainder
UVs, preserving the source blend/color effects in the existing extraction scope.
The removed vanilla widgets sheet is served unchanged at a dedicated source-profile
identifier, optionally read from an explicit source-client archive; it MUST NOT
replace modern vanilla assets or synthesize missing pixels. Missing assets fail
at their first rendering consumption. Child callbacks inherit receiver-owned depth
storage from the first projected ancestor rather than creating divergent storage.

Source RenderSystem texture-unit/color setters feed the same scoped GUI extraction
state for ordinary calls and callable handles/constants. This includes hovered
widget branches; it does not equate arbitrary source GL state with target rendering.

## Runtime63 hover artifact investigation

The user reports a magenta strip on CarbonConfig icon buttons only while hovered
and provided a cropped screenshot. Enum/numeric save acceptance is separate and
remains valid. Source widget-sheet rows for disabled/normal/hovered states contain
no magenta pixels; no GUI texture load exception appears in runtime63. Root cause
is not yet established. An opt-in `lms.traceWidgetQuads` diagnostic records the
source widget class name, bound resource/object, UVs, vertex/global color and blend
state for hovered source widget extraction. It is bounded to128 distinct string
records and retains no widget or managed classloader. It does not modify drawing,
resource pixels, mappings or source artifacts. Runtime64 will collect evidence;
no visual fix is claimed from adding this instrumentation.
