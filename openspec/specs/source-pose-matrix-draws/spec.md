# Closed source pose-matrix draws

LMS MAY adapt exact source PoseStack.Pose pose/normal getters and matrix-bearing
VertexConsumer operations to native live matrices when a complete class audit
proves the matrix values remain in a closed drawing flow. Private helpers and
LambdaMetafactory captures MAY retain these values; descriptors, method handles,
lambda signatures and frames MUST migrate consistently.

The admitted flow MUST NOT construct or operate on source matrices, store them
in fields/arrays, expose them in public/protected signatures, return them through
Object or pass them to foreign consumers. Unproven flows MUST remain unresolved.
Global source Matrix3f/Matrix4f namespace aliases are forbidden for this subset:
source constructors and mutable algebra are separate contracts.

Position submission MUST multiply the homogeneous source vector without an
invented perspective divide. Normal submission MUST retain the source matrix
product without additional normalization. Native pose getter identity/liveness
MUST be preserved. The source's vertex/color/normal/end ordering MUST be retained.

For the verified default source RenderType.lines, the removed global line-width
state MUST become the native per-vertex channel using the actual source
window-scale rule: max(2.5, framebufferWidth / 1920 * 2.5). Other render types MUST
not acquire this channel as an invented default. Custom source line-width state
remains a separate unsupported operation.
