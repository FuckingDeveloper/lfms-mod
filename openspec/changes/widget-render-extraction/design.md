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
