# Closed matrix drawing flow and source line state

Runtime40 reached actual tool outline drawing and failed on the removed source
PoseStack.Pose.pose getter returning com.mojang.math.Matrix4f. The drawing family
also uses normal Matrix3f, private lambda captures and both matrix vertex methods.
The source vertex position is a homogeneous vector product with w=1; source
normal submission multiplies a Vector3f without renormalization.

Admit only a complete closed matrix-bearing method graph within a class: native pose getters, private helpers,
LambdaMetafactory capture and exact VertexConsumer endpoints. Reject source matrix
construction/algebra, fields, arrays, foreign/Object consumers and exposed
signatures. Remap only admitted method graphs, including lambda handles/frames;
do not add global matrix aliases with incompatible constructor defaults.

Native line vertices additionally require a line-width channel. Source default
RenderType.lines selected window-scaled LineStateShard, translucent blending,
depth writes and no culling. Preserve the actual source width formula per vertex
and use native translucent lines. Exact source view-offset layering parity and
arbitrary custom line-state construction are not claimed by this change.


## Matrix-bearing graph admission

Runtime134 shows a false rejection: a fluid GUI renderer transports matrices
only between pose getters, private helpers and verified vertex sinks, but also
contains unrelated constructor field writes and public generic tooltip methods.
Those unrelated methods MUST NOT invalidate a proven matrix graph merely because
they use fields or return Object. LMS MUST inspect every method whose source
descriptor, signature, instruction, handle, invokedynamic argument or nested
constant carries a source matrix. Existing escape restrictions apply throughout
that graph; source matrix fields, exposed signatures, arrays, algebra/construction
and unknown/Object consumers remain outside this contract. Bootstrap matrix types
MUST be checked even if the invokedynamic capture descriptor contains none.

The source BufferBuilder's verified inherited matrix-position/normal operations
MAY use the same VertexConsumer semantics as the exact source interface. Existing
GUI batch migration remains independently scoped and validated. Neither matrix
transport nor renderer construction proves that an ingredient renderer has been
invoked through a native GUI extraction lifecycle.

Evidence: Forge1.19.2 VertexConsumer position uses a homogeneous vector product
with w=1, and normals use a non-normalizing vector product. Actual target26.3
VertexConsumer.addVertex(Matrix4fc,...) applies transformPosition without a
perspective divide; PoseStack.Pose exposes actual live JOML matrices. Existing
LMS GUI buffers record immutable native GUI geometry. No matrix constructor
alias, global matrix mapping or mod-specific selector is introduced.


Implementation check: `./gradlew compatibilityTest nativeCompatibilitySmoke
compileLaunchJava compatibilityAudit --args='legacy-mods /tmp/lms-matrix-graph-audit.tsv'`
passes (/tmp/lms-matrix-graph-check.log). Independent native checks retain the
original 24-vertex shape/normal behavior and escaping-signature rejection, admit
unrelated ordinary fields/Object accessors, and link the immutable original fluid
renderer with all three constructors and consistent native matrix descriptors.
Audit: 3829 classes, 1137 grouped gaps / 6431 sites, same two prior unrelated
transform failures. Audit totals remain inventory, not functional acceptance.
