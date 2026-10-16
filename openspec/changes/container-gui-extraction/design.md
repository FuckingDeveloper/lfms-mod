# Bounded container background/label extraction

At the direct source AbstractContainerScreen boundary, native extractBackground
MUST run the actual native default background then invoke the original source
renderBg virtually once. Native extractLabels MUST invoke the original source
renderLabels once under the native container-origin transform. Source subclass
inheritance and explicit source-super callbacks MUST remain intact. Source-owned
geometry and blit-offset storage MUST remain live; uninitialized this is never
passed to a runtime helper.

The default source Screen.renderBackground invoked within renderBg is represented
by the already-extracted native default background. This is valid only inside
that extraction phase. Its final source blend/texture state is retained for later
source batches. The default source label operation calls the actual native base
label implementation nonvirtually. Neither removes a custom source override.

A method-local adapter MAY replace the verified old Tesselator/BufferBuilder GUI
batch API only where the old builder does not escape to unrelated native APIs.
World buffers MUST NOT be globally replaced. Supported formats are POSITION_COLOR,
POSITION_TEX and POSITION_TEX_COLOR with QUADS and their exact known shader
intents. Scope, begin/end order, per-vertex UV/color, planar transforms, source
shader color, texture, blend choice and clipping MUST produce immutable native
GuiElementRenderState snapshots. Unknown topology/channels, escaped buffers,
nonplanar/depth transforms, custom shaders or unscoped drawing MUST fail explicitly.
No old GPU shader object is fabricated; source GUI shader getters expose typed
semantic tokens for the verified shaders. Native GUI snippets and explicit
ColorTargetState implement blend-enabled/disabled variants without mutating
native registries/global GPU state. Shader/format mismatch MUST fail explicitly.

Font drawing MAY project through the actual extractor with fractional translation
and planar pose retained. Text, color and shadow intent must be preserved. The pinned source Font
adjustColor rule (alpha below 4 becomes opaque) MUST be applied before native
text extraction; zero-alpha native text is otherwise skipped. The
current projection's returned width is an information view; advanced bidi/custom
source font callbacks remain an unverified boundary.

Custom full render/post-draw callbacks, source-specific tooltip/input routing,
model-view item rendering, arbitrary scissor/global GPU APIs and custom shaders
are separate gaps. This contract MUST NOT be described as complete legacy GUI
rendering. A real graphical client test is required for background/label claims.

## Native texture binding pair

The exact adjacent source AbstractTexture.getId -> RenderSystem.setShaderTexture
producer/consumer pair MAY bind the actual native AbstractTexture directly.
Receiver, evaluated unit and texture must remain unchanged. No numeric GPU ID,
dummy value or compatibility integer handle is produced. The native texture
object/cache and its view/sampler remain authoritative. Unknown texture units and
source-owned custom texture implementations MUST fail explicitly rather than
silently discard an overridden getId operation. Standalone getId calls, numeric
handles that escape the pair and raw OpenGL consumers remain unsupported.

Source canonical method lookup includes com.mojang platform-library owners only
when the pinned source SRG/Mojang mappings establish exact owner/member/descriptor
identity. This does not enable blind target linkage/namespace renaming. Source
identity and current target ABI validation remain independent.

Source owner comparison MUST normalize dot/slash spelling for package-qualified
library declarations. Source method selection remains indexed and requires exact
owner and fully remapped descriptor; inherited/descriptor-only candidates MUST
NOT be accepted as canonical declarations.

Within the nonescaping local GUI batch, the source Pose.pose getter and its
vertex consumer MAY project together to the actual target JOML Matrix4f. This
preserves the live target PoseStack matrix and planar position semantics. It
MUST NOT globally rename source matrix types, constructors or mutation methods.
The producer and consumer descriptors MUST agree after transformation.

Verified shader intent projection MUST include direct calls, method handles,
invokedynamic arguments and nested constant-dynamic arguments. Source supplier
references MUST resolve to the same typed semantic getter as a direct call.

## Observed verification (2026-10-14)

On Minecraft 26.3 / NeoForge 26.3.0.58-beta / Java 25, the unchanged
Forge 1.19.2 reference artifact opened electric-furnace and generator screens
through ordinary block use. Targeted visual inspection confirmed backgrounds,
labels, inventory/slot geometry and an inventory-item tooltip. MCP insertion of
one raw iron was confirmed in server block NBT. Four coal inserted into the
generator resulted in three remaining, active fuel and energy delivery through
the intervening glassfiber cable. The furnace produced one iron ingot in its
output slot; extraction was confirmed in server player inventory. Closing and
reopening the furnace succeeded without a new runtime exception.

This observes the bounded machine GUI and one cooking/energy-transfer path, not
all machine widgets, custom input, persisted machine state, tool energy usage or
all legacy rendering. Existing initial enchantment-context and global-loot JSON
errors, and the blocked CarbonConfig lifecycle remain separate open gaps.

Executed verification: `./gradlew compatibilityTest nativeCompatibilitySmoke
compileLaunchJava`, then `./gradlew runClient -PlmsTestWorld='Новый мир'` and the
MCP interactions above. No screenshot/log/run state belongs in source control.

## Scoped GUI state family coverage

The exact source public `setShaderTexture(int, ResourceLocation)` and internal
`_setShaderTexture(int, ResourceLocation)` entrypoints MAY normalize to the same
scoped texture-binding operation during GUI extraction. Source bytecode proves
that the public entrypoint calls the internal one on the render thread; target
extraction already executes there. Both MUST retain the evaluated unit and
texture identity, and reject unsupported units and unscoped use. The adjacent
verified native texture-object producer/binder pair MAY use either entrypoint.
Raw numeric texture identifiers outside that pair remain unsupported.

All proven source RenderSystem GUI state operations MUST share exact
owner/canonical-member/descriptor selection for direct calls, method handles,
invokedynamic arguments and nested constants. Adding a verified operation MUST
NOT require a separate mapping for every call encoding. Unknown operations MUST
remain visible as compatibility gaps; they MUST NOT become no-ops. Fixture GUI
classes SHOULD be audited together for residual external linkage failures before
requesting another interactive test. Structural audit is not rendering proof.

Verified GlStateManager blend/depth/texture enable/disable wrappers MUST feed the
same scoped state as their RenderSystem counterparts. This does not authorize
arbitrary raw GL operations or custom blend/depth functions.

Source Window framebuffer width/height and logical GUI width/height queries MAY
use their exact native counterparts. Pixel and logical dimensions MUST stay
distinct. NativeImage width/height and InputStream PNG reading MAY use native
operations; read retains RGBA decoding and closes the input on success/failure
with native allocation cleanup. Source format-selecting reads remain a separate
boundary. These identities MUST apply equally to calls and handles.

## Source blend equations

Source blend-factor enum types MUST retain their distinct identities, declaration
order, names and numeric `value` fields. Supported factor calls MUST create native
ADD blend equations for color and alpha separately; two-factor calls use the same
factors for both. The exact source default is SRC_ALPHA/ONE_MINUS_SRC_ALPHA for
color and ONE/ZERO for alpha (not the target TRANSLUCENT alpha equation).
Blend enable and blend factors are independent scoped state. Each submitted quad
MUST snapshot both equations and write mask into its native pipeline. Changing
source state later MUST NOT change a submitted quad. Native pipeline caches MUST
not contain managed classes or classloaders. Constant-color/alpha factors require
unproven blend-constant state and MUST fail explicitly. Raw integer blend calls,
custom blend operations and unsupported topology remain gaps.

## Approved source batch ABI extension

The approved `source-world-render-extraction/design.md` extends the prior
method-local limit with a consistent source-shaped batch ABI across managed
helpers, casts and callables. It MUST select the actual active GUI/world scope
and MUST NOT pass a source builder to unrelated target APIs as a native builder.
Existing local GUI extraction and its primitive boundaries remain intact.
