# Source nested item draws

Source ItemRenderer.renderStatic MAY be adapted to native ItemModelResolver
extraction and deferred ItemStackRenderState submission for verified overloads.
LMS MUST preserve display context, world/owner, seed, pose, light and overlay.
The no-world source overload MUST retain its null world/owner and false
handedness. Independent source handedness not represented by the native context
MUST fail explicitly.

The adapter MUST extract exactly once at the source callback boundary using an
isolated stack and pose. Submitted state MUST NOT retain the mutable input stack,
pose or live source block entity. Bounds MUST incorporate actual nested target
extents and the source transform. Native submission MUST preserve its parent's
pose and source packed light/overlay.

Migration MUST verify both exact source member identity and singleton-getter
dataflow. Local aliases MAY be admitted only when every consumer of the proven singleton value is compatible with its target type. Field-stored/foreign renderer receivers, explicit BakedModel operations and
unproven method-reference shapes MUST remain unsupported. The adapter MUST NOT
silently omit nonempty contained items to avoid a render failure.
