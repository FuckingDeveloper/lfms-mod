# Contract and evidence

Pinned1.19.2 RenderSystem.getModelViewStack returns the mutable stack, whereas
applyModelViewMatrix copies its current matrix into separately applied state.
GameRenderer resets that stack to identity plus a fixed GUI Z translation before
GUI rendering; the source projection supplies the matching depth interval.
Target26.3 GuiGraphicsExtractor snapshots its Matrix3x2f pose into item/primitives
render states. A native affine extraction pose can preserve a source relative XY
transaction without changing a GPU global during extraction.

LMS MAY adapt exact source getModelViewStack/applyModelViewMatrix calls only when
source dataflow proves the returned stack remains local and is used solely for
push/pop, translate and scale. The source stack MUST NOT escape to fields, arrays,
returns, foreign calls, identity comparisons or matrix observation. Callable
constants carrying these global operations remain unsupported. Method bodies
without a local getter MUST NOT silently apply another callback's state. An
unproven method MUST remain unchanged, including its source operations and
existing lazy consumption failure; it MUST NOT be removed or replaced by a no-op.
An unrelated method in the same class MUST NOT be disabled by a failed proof.

The source profile MUST retain the distinction between pending stack changes and
applied state. Each apply MUST publish a copied XY transform composed after the
native baseline pose. Popping the pending stack MUST NOT change already applied
state until apply is called again. Native item/primitive snapshots MUST remain
unchanged by later stack edits. XY-affine and finite matrices are required;
relative Z translation/scaling, perspective and XY/Z mixing MUST fail explicitly.
The original source GUI Z baseline is not observable by this admitted subset.

A complete callback MUST restore stack depth, relative matrix and applied state.
On success, an unbalanced/unrestored transaction MUST fail. On callback failure,
native pose and thread scope MUST still restore without replacing the original
exception merely because source cleanup did not run. Nested extracted source GUI
callbacks/native tooltips during a live model-view transaction MUST fail until
composition with intervening native local transforms is proven. Balanced local
subtransactions in the same scope retain normal source stack semantics.

World/global/cross-frame matrix behavior and arbitrary3D previews remain gaps.
A successful affine probe is not proof of rendered pixels or menu interaction.
