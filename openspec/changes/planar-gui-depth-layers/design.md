# Source flat GUI depth planes on native strata

A source Z translation of an otherwise planar GUI primitive is a depth plane, not
3D geometry. For the default Forge 1.19.2 projection, GUI near is 1000, far is
1000+2000*(1+layerCount), and model-view Z is near-far. Nonnegative depths below
1000 remain inside the visible interval for every normal GUI layer count.

Within each existing extraction phase, uniform planes in that bounded interval
MAY map to native GuiGraphicsExtractor.nextStratum. Increasing source Z advances a
native stratum; equal depth preserves submission order. With source depth testing
disabled, painter order remains native and no enabled-plane high-water mark changes.
A decreasing depth-tested plane MUST fail before submission: native strata cannot
recreate source depth-buffer occlusion against earlier intersecting primitives.
Negative/out-of-range/nonfinite planes, per-batch mixed depths, perspective, out-of-
plane rotations and Z-to-XY coupling remain explicit gaps. Their Z MUST NOT simply
be discarded to reach the next frame.

The depth decision MUST be shared by source quads, text, direct blits, item helper
poses and ordinary GUI items/decorations. Relative ordering between existing target
GUI phases remains target-owned. Every projected quad retains original XY, UV,
color, mask and scissor state; its emitted Z is zero only after plane ordering has
been established. This extends the prior fail-fast depth boundary explicitly.

## Independent Z scale for flat primitives

A finite independent Z scale does not tilt a local-Z=0 primitive: its effective
plane remains m32, and its XY projection remains the same affine 3x2 transform.
Text, flat quads and direct blits MUST accept this scale, including uniform GUI
scaling and restoration. For explicit local depth z, the effective plane MUST be
m22*z+m32, not z+m32. Quads retain their full transformed position until the
uniform-plane check. Existing effective-depth bounds and ordering still apply;
Z scale MUST NOT permit perspective, XY/Z coupling or mixed planes.

This does not establish 3D item geometry equivalence. The item helper MUST retain
its previous m22=1 boundary until its internal model depth/culling is adapted.
Evidence: source PoseStack scaling multiplies the pose; ComparatorComponent's
immutable drawForeground scales all axes by 0.5 for text, then multiplies all axes
by its reciprocal. Sibling GUI icon/item scaling sites retain Z=1. The target
GuiGraphicsExtractor stores flat primitives under a Matrix3x2fStack; the native
stratum carries the effective source plane ordering.
