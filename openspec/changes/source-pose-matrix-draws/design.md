# Closed matrix drawing flow and source line state

Runtime40 reached actual tool outline drawing and failed on the removed source
PoseStack.Pose.pose getter returning com.mojang.math.Matrix4f. The drawing family
also uses normal Matrix3f, private lambda captures and both matrix vertex methods.
The source vertex position is a homogeneous vector product with w=1; source
normal submission multiplies a Vector3f without renormalization.

Admit only a complete closed class flow: native pose getters, private helpers,
LambdaMetafactory capture and exact VertexConsumer endpoints. Reject source matrix
construction/algebra, fields, arrays, foreign/Object consumers and exposed
signatures. Remap only admitted method graphs, including lambda handles/frames;
do not add global matrix aliases with incompatible constructor defaults.

Native line vertices additionally require a line-width channel. Source default
RenderType.lines selected window-scaled LineStateShard, translucent blending,
depth writes and no culling. Preserve the actual source width formula per vertex
and use native translucent lines. Exact source view-offset layering parity and
arbitrary custom line-state construction are not claimed by this change.
