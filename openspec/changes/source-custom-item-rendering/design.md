# Source custom item rendering: evidence and remaining contract

Runtime 31 and the user establish that all 21 Iron Chests entries now populate
the creative tab and all 7 generated upgrade icons render correctly. The 14
chest icons remain missing. Original source models select builtin/entity and
the source item supplies IClientItemExtensions.getCustomRenderer, returning
a BlockEntityWithoutLevelRenderer that delegates to the block-entity dispatcher.
This is a source-generation contract, not an artifact identity selector.

## Verified target boundaries

- Target ItemModel.update receives ItemDisplayContext and the current item.
- Target SpecialModelRenderer.extractArgument receives only ItemStack.
- The final packed light/overlay are supplied later, during native submission.
- GuiItemAtlas submits with light 15728880 and OverlayTexture.NO_OVERLAY.
- The source callback receives item, display context, pose, buffers, light and
  overlay together. Its dispatcher renderItem operation is removed in the target.
- LMS already records source BER geometry into immutable RecordedGeometry.
  Submitted state must not retain a live block entity or mutable input stack.

## Required implementation contract

The adapter MUST preserve the actual source renderer and its geometry,
source display transforms, extension ownership, callback count, light/overlay
semantics, stack ownership, resource-reload lifetime and render-thread boundary.
It MUST distinguish registration intent from invoking a source rendering callback.
It MUST NOT map the old renderer class directly onto a different target renderer,
substitute vanilla chest/cube geometry, or declare an empty generated model as
successful custom rendering.

GUI extraction can use its proven native light/overlay values and reuse the
existing immutable geometry recorder. Other contexts require an explicit model
for the later native lighting input; passing guessed full-bright light is not
equivalent. Bounds must come from actual extracted geometry or another proven
source contract, not an invented unit cube. Source extension initialization timing
also needs verification: moving arbitrary initialization to the first draw could
change source side effects and lifecycle ordering.

## Admitted subset

The implementation admits direct source-base subclasses with a complete source
renderByItem override. Calls to vanilla source-base behavior or access to its state
remain unsupported. The source-base constructor's allocations are private vanilla
fallback caches, unused by this admitted subset; only that unobservable fallback
state construction is omitted. No vanilla fallback rendering is claimed.

Extension materialization is lazy only for verified allocation/capture/accept
registration (optionally preceded by the proven empty Item/BlockItem super call) and a constructor consisting exclusively of capture writes and Object
initialization, with no static initializer. The extension is item-owned (no global
strong-value/weak-key cycle), initialized once, and its getCustomRenderer is invoked
at each GUI extraction. Arbitrary initialization remains fail-fast.

Derived definitions preserve source display properties in a separate immutable
base resource, removing only the builtin/entity selection marker. Original models
and input artifacts remain unchanged; explicit native definitions win. Non-GUI
contexts retain a diagnosed missing-model gap. Source dispatcher renderItem returns
true on renderer absence and false after rendering, exactly as source bytecode.
GUI callbacks record immutable geometry and actual vertex bounds, with a copied
stack; submitted state retains neither the source stack nor a block entity.

Generated-item rendering, GUI custom rendering and creative population acceptance
remain tracked independently. Runtime32 loads the existing world through Gradle and MCP reports a real
Iron Chests stack in the hotbar without a new runtime exception. Visual acceptance
of all GUI icons remains pending. The existing global-loot-list parsing gap remains
unchanged. A separate persistence check confirms the previous chainsaw charge of
950 EU survives the normal shutdown/restart.

Runtime32 visual acceptance failed: entering the full creative tab reached the
transparent-renderer branch and crashed on dispatcher.camera (`f_112249_`). Source
prepare stores the world Camera, and this path immediately asks its getPosition.
The target dispatcher no longer owns Camera; its old cameraPos prepare method has
no callers in the configured source tree. Reading that stale field would be wrong.
Target GameRenderer.update updates mainCamera before extraction, and extract uses
that camera for the level before GUI extraction. The adapter admits only the exact
paired position observation on the native client dispatcher and rejects custom
dispatchers/unpaired camera operations. Native smoke now loads and inspects the
original full source BER in addition to the item callback. GUI acceptance remains
open pending a new run.

Runtime33 MCP opened a temporary native barrel containing all 14 source chest
items without a crash; it was emptied and removed after closing the screen. One
visual check showed every icon using the same obsidian texture. Comparison with
the immutable source PNG establishes this is real obsidian imagery, not missingno.
The adapter record's equal display properties made distinct custom models equal in
TrackingItemStackRenderState identity; matching component patches then aliased
GuiItemAtlas slots. Native SpecialModelWrapper includes its extracted argument in
the identity. The adapter now includes the immutable recorded geometry argument,
item identity/count and component patch, while retaining animated invalidation.
A native TrackingItemStackRenderState regression uses equal display properties
and distinct extracted textures/geometry to prove that the cache keys differ.
No texture-loading workaround or texture substitution is introduced.


Runtime34 acceptance: MCP confirms all 14 actual source stacks in an opened native
barrel. Visual inspection confirms iron/gold/diamond/copper/crystal/obsidian/dirt
imagery with the corresponding trapped variants, rather than fourteen copies of
one cached image. There is no new ERROR/FATAL/cause after opening; the pre-existing
global-loot-list parsing error remains. The temporary barrel is emptied, removed,
and verified as air. The original player inventory is retained. Creative-tab user
re-entry and non-GUI custom rendering remain separate acceptance items. Overall
baseline remains level6 with observed level7 functional subpaths, not complete
reference-mod support.
