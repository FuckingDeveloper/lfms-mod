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

## Source GUI sheet resource family

Runtime64 traces identify CarbonCheckBox's hovered quad as a bind of
minecraft:textures/gui/checkbox.png with source UVs20/64..40/64. The source archive
contains the64x64 sheet; the target no longer supplies it. Target TextureManager's
loadContents catches FileNotFoundException and returns its missing checkerboard
without logging an error. This explains the magenta region despite clean logs.

Extend the existing isolated source widgets resource pack to the catalog of
minecraft:textures/gui/*.png (including subdirectories) actually present in the
configured immutable source-client archive or admitted legacy artifacts. Preserve
all original pixels and accompanying texture metadata; do not synthesize sprites.
Only source texture-bind operations in the existing GUI extraction bridge project
known catalog IDs into the isolated source-profile namespace. Unknown IDs, other
namespaces/non-GUI assets and native rendering remain unchanged. Existing WIDGETS
identity remains stable. Legacy artifact entries retain the existing deterministic
source override order. Hash all exposed resource paths and bytes into pack identity;
lookup/list/namespaces must agree, and pack snapshots must not change on rediscovery.
No source JAR is changed and modern vanilla resource paths are never replaced.

Verification for the source GUI sheet correction: wrapper compatibilityTest,
nativeCompatibilitySmoke and compileLaunchJava passed in batch-check-24. Native
checks compare every source GUI PNG byte-for-byte, decode the actual64x64 checkbox
sheet, check stable WIDGETS identity, unknown/non-GUI/other-namespace isolation,
lookup/list/namespaces, texture metadata/cache identity and immutable old pack
snapshots after rediscovery. Native-only resource paths are not served by this
pack. Runtime65 starts with widget trace; actual hovered checkbox visual
acceptance remains pending. No source fixture JAR was changed.

Runtime65 visual acceptance: the human confirmed the hover artifact is gone and
icons/highlighting look correct. Widget trace confirms both normal and hovered
checkbox quads now bind lms:legacy/forge1192/gui/checkbox.png with the original
64x64 UV layout. No ERROR/FATAL/causal exception was found in the runtime65 log at
acceptance. This closes the reported hover/missing-sheet failure. Broader custom
screen rendering and unproven GL operations remain separate compatibility gaps.
No implementation changed after batch-check-24; this is runtime evidence only.
