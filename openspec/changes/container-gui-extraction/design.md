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
