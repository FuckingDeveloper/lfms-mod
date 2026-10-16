# Complete source empty-stack decoding

Runtime57 cleanup of a test furnace exposed source of(CompoundTag) rejecting a
stack with Count:0b in the target count codec. The source constructor reads a
signed byte, initializes its tag and capabilities, and considers nonpositive
counts/air items empty after construction. Returning canonical EMPTY before data
fixing/decoding would discard potentially observable data and bypass validation.

Retain the existing whole-source-schema target data fix and authoritative lookup.
For a source nonpositive signed-byte count or explicit air identity, decode the
fixed item holder/component patch with those same native registry/component
codecs, but construct the native ItemStack directly with the original source
count instead of the target positive-count/non-air codec. The target constructor
accepts this state and retains the patch. Require a bound holder and validate
components even when logically empty. Do not return arbitrary EMPTY on unknown
items, invalid component data, or lookup failures. Ordinary positive, non-air
stacks keep the current native codec path. Genuine empty-map encoding keeps the
native optional codec path. Source item/entity capability initializer callbacks
remain the existing explicit gap; this change does not claim their support.

Pinned source ItemStack constructor/isEmpty/getByte behavior and actual target
ItemStack MAP_CODEC, Item.CODEC_WITH_BOUND_COMPONENTS and constructor establish
this count/air boundary. Tests must cover zero/negative/missing count, air,
retained custom data, unchanged input and continued meaningful validation errors.

Native smoke also established that the native constructor retains the component
patch for an empty stack, but native getComponents/getComponentsPatch hide that
patch. Source getTag has no such empty-count guard. Add exact raw component access
to the existing stack carrier, used only by source mutable-tag ownership/damage
mirrors. A count transition to zero MUST NOT look like removal of custom_data or
native damage; actual component replacements/removals remain authoritative.
Native public component visibility is unchanged. Test live source tag identity and
mutation at zero count, then raise count and observe the same native patch data.

Normalize Count in the copied source schema to the source constructor's signed
byte/default before data fixing; this makes missing-count payloads participate in
componentization too. An actually empty input map retains optional-empty-map
semantics. No caller input is mutated. Native smoke passes zero/negative/wrapped
numeric count, missing count with payload, air, live empty source tag identity and
mutation, count transitions, positive ordinary/narrowed counts and unknown-item
failure. Full wrapper checks pass in batch-check-11.

Dedicated run6: real registered Iron Chests source inventory accepts stone,
native data modification to its old-format Items[0].Count=0b completes without
exception, and source inventory readback becomes Items:[]. A subsequent ordinary
slot replacement with three gold ingots reads back count3, proving positive
inventory operations remain intact. Test inventory/block/forceload restored;
native stop saves all dimensions and Gradle exits successfully. No remote-client
or source item-capability initialization claim is made.
