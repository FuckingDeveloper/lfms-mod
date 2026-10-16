# Source configuration UI controls, notifications and narration

## Checkbox

The source Checkbox constructors MUST preserve x/y/width/height, message,
initial selection and showLabel. A source-shaped native AbstractButton subclass
MAY represent the removed constructor contract without mutating native Checkbox
access or inventing a font dependency in source construction. Native activation
MUST invoke the original no-argument source callback virtually once. The source
base toggles its own selection; managed selected() overrides and explicit super
activation MUST retain dispatch. Source custom rendering remains in the existing
GUI extraction adapter. Default presentation MAY use native checkbox sprites,
retaining source geometry and label visibility. Source base render/super callbacks
MUST execute the base drawing rather than recursively call a source override.

## System notifications

Source SystemToast identity, constructor arguments, nullable message and reset
intent MUST remain meaningful. Source enum names, order, category identity and
5000/10000 ms display durations MUST survive even for categories the target
removed. Categories MUST NOT be aliased to an unrelated native category.

A source-shaped Toast implementation MAY capture title/message/category and
materialize a genuine native SystemToast at its first layout/update/extraction
consumption. It MUST NOT return dummy dimensions. Native layout/wrapping and
presentation are the target policy. The source token remains the source enum;
queue lookup, clear, add, addOrUpdate and reset MUST use the actual native
ToastManager and preserve identity and native toast-add cancellation. Native
extraction/update consume the delegate, not an emulated scheduler. The original
source immediate toast render callback and custom render overrides remain an
explicit fail-fast boundary. Native manager construction/rendering is not a
source constructor/immediate-render compatibility claim.

The Minecraft toast accessor MUST return that client's actual GUI-owned manager.
Unrelated descriptors or owners MUST NOT select a migration. No managed mod
identity may influence control, category, message or notification behavior.

## Narration

The source two-component joinForNarration operation MUST create a fresh mutable
component with the original component references separated by the actual native
narration separator. It MUST preserve styles/siblings and input immutability.
Native varargs joining MAY implement it because inspected source/target code
performs that exact append sequence.

All supported operations MUST cover exact verified source owners/members and
normal calls, handles, invokedynamic and nested constants. Class and descriptor
projection MUST remain consistent. Unproven operations and native/source callback
collisions MUST fail explicitly. Verification MUST include original immutable
consumer construction/state callbacks plus native queue identity/cancellation and
component mutation tests. A real client is required for visual and complete
configuration workflow acceptance.

## Layered screens

Runtime59 preserves boolean edits/save, but enum/numeric editors fail before
opening with a residual ForgeHooksClient.pushGuiLayer call. Exact source static
push/pop/clear operations MUST use the target Gui-owned screen-layer stack.
Source push retains the parent without removing it, initializes the child at
current GUI dimensions and narrates it; target pushScreenLayer does this through
Screen.init including immediate narration. Pop removes the child and restores/
narrates its parent; an empty stack delegates to normal setScreen(null). Clear
unwinds to the bottom parent. Preserve the native init/events/removal/resizing
lifecycle; do not emulate a separate stack or substitute setScreen for push.
The original source getGuiFarPlane/rendering helpers remain outside this
transition contract. Callable constants require the same exact operation evidence.

Runtime59 file comparison confirms auto-backup=true persisted and the actual
source backup ZIP contains2026_10_16-10_49_42.cfg; the numeric and enum values
remained unchanged while mouse handlers failed at pushExternalScreen. The test
backup toggle was restored to its original false before runtime60. Checks in
batch-check-15 verify all three layer transition calls/handles, actual target
Gui method declarations and both original immutable pushScreen consumers.
Interactive runtime60 editor/return/save acceptance is pending human input;
MCP cannot activate arbitrary configuration widgets.

## Layer default background ownership

Runtime60 reaches the real DropDownScreen after push; native extraction then
throws "Can only blur once per frame" while extracting its inherited Screen
background after the parent's background. Source Forge drawScreen calls each
screen's render callback and does not automatically insert a full-screen
background per layer. A source overlay must not acquire an additional opaque/
blurred full-screen backdrop merely by inheriting the target Screen hook.

For managed ordinary screens above the bottom screen in the actual native Gui
stack, the target base Screen.extractBackground default painting MUST be omitted.
Bottom/root screens retain the existing native default policy. Preserve the
native deferred-subtitle tail, render events and source/native custom drawing;
do not disable blur globally or catch its invariant failure. Container screens
retain their separately specified native background contract. Native screens,
off-stack screens and empty stacks MUST remain unchanged. Read the existing
Gui stack by identity, including intermediate overlays, rather than inventing
another layer stack or inspecting a fixture's class name.

Source IForgeMinecraft defaults additionally expose Minecraft.pushGuiLayer and
popGuiLayer; they delegate to the same verified static ForgeHooksClient methods.
Adapt these exact instance calls/handles together. Direct source extension-
interface references and arbitrary layer rendering helpers remain distinct gaps.

Verification of the runtime60 blocker: wrapper compatibilityTest,
nativeCompatibilitySmoke and compileLaunchJava passed together in batch-check-17.
The actual FML-applied Gui accessor and Screen background injection are present.
Native GuiRenderState checks cover three stacked screens over three reset frames,
retain its duplicate-blur exception, preserve root/off-stack/empty-stack policy
and use identity even when equals is overridden. Original immutable dropdown
contains one instance pop and two static pops; all three project to the same
native bridge. The ordinary instance push/pop family and its handles are also
checked. An earlier check16 wrongly expected only the instance pop in the
consumer inventory; inspection corrected the assertion to include all3 paths.
Runtime61 interactive selection/input/save/return remains pending; no full
source immediate screen-rendering support is inferred from this background fix.

## Coordinate-only widget pointer hooks

Runtime61 does not crash, but enum selection exits the editor without a saved
value and the numeric value remains unchanged. These are open functional failures.
Inspection additionally exposes two generic input defects: source list callbacks
receive raw SDL button codes, and coordinate-only AbstractWidget onClick/onRelease/
onDrag overrides have no native event-shaped entry point. The native AbstractButton
onClick invokes onPress, whereas a source slider overrides onClick to update its
value. Bypassing that override changes behavior, even if activation links.

Native list/entry pointer boundaries MUST project button identity through the
existing named GLFW/SDL map in both directions. Source-source calls retain source
codes. Coordinate-only widget hooks MUST receive native x/y and drag deltas once;
source superclass calls MUST retain special dispatch. Exact source widget ancestry,
member mappings and descriptors select the migration. Virtual source calls and
callable constants MUST retain receiver types and override dispatch. Unproven
special handles MUST fail explicitly. Source coordinate-only calls carry no button,
modifier or double-click data; verified native base implementations may receive a
primary-button event with no modifiers and doubleClick=false. Native-only widgets
are unchanged. This does not claim the separate old boolean Screen/listener input
callback family is adapted, or that config saving is accepted before runtime tests.

Verification: `./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava`
passed in `/tmp/lms-gameplay-batch-check-20.log`. Synthetic native dispatch exercises
click/release/drag through inherited source overrides and special base calls,
coordinates/deltas and virtual callable receiver types. Original immutable slider
native hook entrypoints link. List dispatch checks use actual SDL primary code1
and assert source primary code0, rather than testing native input with GLFW code0.
Check18 initially omitted the new test from the smoke JAR allowlist; check19 and
check20 include and execute it successfully. Optional `lms.traceScreenLayers=true`
logs source set/push/pop/clear callers and real stack depth for runtime diagnosis;
it does not alter transitions. Runtime62 uses this trace and awaits interactive
selection/save/numeric acceptance. General old boolean listener input callbacks
remain unclaimed by this coordinate-hook fix.

The user explicitly authorized autonomous dev-client shutdown for this test world
on2026-10-16: preserving test-world changes is not critical. Runtime61 nevertheless
exited normally before shutdown; MCP confirmed serverPresent=false and logs recorded
player/world saving. This supersedes repeated Save/Quit approval questions for this
same test-world iteration, not permission to alter unrelated worlds.
