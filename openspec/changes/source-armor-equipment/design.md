# Source armor construction and worn extraction

Source Forge 1.19.2 ArmorItem provides slot durability defaults, slot-scoped
UUID-keyed attributes, material enchantability and a virtual repair predicate.
Target 26.3 consumes default components and ItemStack.isValidRepairItem. LMS must
preserve source override dispatch, including deliberately empty attribute maps;
base armor attributes must not replace an override. Exact source UUID identities
use the existing UUID-to-Identifier bridge. Construction must not call a derived
source override before that subclass is initialized. Native component initializers
may execute these callbacks after construction with authoritative registry lookup.

The standard worn path must call the source getArmorTexture(stack,entity,slot,type)
during native humanoid extraction with the actual entity/slot/input stack, then copy the post-callback stack for submission.
A null source result selects the verified source material layer naming rule.
Submission retains only the snapshot and derived resource identifiers, never the
live wearer. Derived asset keys encode the selected complete texture path without
an object cache. Native explicit equipment assets take precedence. Native model,
lighting, pose, visibility, outline and slot selection stay authoritative.
Custom source armor models and dye/overlay layers are not implied by plain-texture
support; unproven source initializeClient armor models remain explicit failures.
Native armor repair dispatch must evaluate the original source predicate against
both actual stacks, not flatten it into a static list of repair items.

This change does not claim dispenser, custom source armor models/animations,
all enchantability callbacks, arbitrary non-armor item repair or all render contexts.
Verify all four slots, override/default durability and attributes, actual native
repair dispatch, extracted stack isolation and selected texture identifiers.
Then verify worn visuals and protection in a dev world through MCP/user acceptance.

## Shelf projection accepted by user, 2026-10-16

Native ShelfRenderer prepares light before model resolution and consumes bounds
later for bottom/center alignment. ON_SHELF projects to source FIXED callback;
native model ON_SHELF transforms and shelf placement stay authoritative. Generalize
the existing identity-scoped item-entity extraction to distinguish the two
contexts; mismatched output/stack/owner/display and unscoped requests fail closed.

Runtime50 exposed skipped source operation constants inside rewritten attribute
constructors. The transformer MUST traverse retained argument instructions after
removing NEW/DUP and replacing the constructor. The three verified operation
identities also migrate in static field handles, invokedynamic arguments and
nested dynamic constants. Native actual linkage tests cover the family.

Source armor classification precedes head-item extraction. Native shouldRender
must recognize an adapted source ArmorItem with its matching equippable slot so
helmets are not also extracted as generic head items before wearer-dependent
texture selection. No dummy asset is installed in global item components.

## Native outline projection

Target 26.3 ItemFeatureRenderer.prepareOutlineSubmit selects each material's
RenderType.outline(), skips absent variants and supplies the native ARGB color.
CustomFeatureRenderer does not do this automatically. Apply the same rule at
recorded batch submission, enforcing color even when the source emits its own
tint. Forward the outline value to nested native states. Ground extraction may
now record outlined geometry with its actual light; submission reuses the same
immutable vertices. Deferred draws retain once-per-submission invocation and
must not transform source world-pose vertices a second time.

Runtime53's native glowing-stone control showed that an outline-only source draw
omitted the normal material. SubmitNodeCollection.submitItem always queues the
normal item and separately queues the outline. Recorded source batches must do
both from one recording, including for materials without an outline variant.
The three source chest geometries on a native shelf were visually correct.

## Damage data boundary

Runtime53 native anvil repair invoked the real source setter: custom_data.Damage
changed from 100 to 40, but native DAMAGE stayed 100. Source IForgeItem.getDamage
reads NBT without a max clamp; setDamage writes max(0,value). Native defaults use
the DAMAGE component and clamp to max. Adapt these default methods for managed
items, direct source armor facades and stacks explicitly entering the source tag
API; custom item overrides remain virtual. A receiver-owned tag state tracks the
last native mirror, imports native damage on first access and detects later
native writes. Source queries mirror the raw source result into the bounded
native component; setter completion mirrors the actual virtual getter result.
No additional global stack/classloader cache is introduced.
