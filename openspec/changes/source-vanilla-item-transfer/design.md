# Vanilla automation into source item handlers

Runtime77 native hopper retains four coal above a source generator: the source
block entity exposes Forge IItemHandler but no native Container/ResourceHandler.
Pinned source Forge VanillaInventoryCodeHooks defines hopper insertion/extraction
and dropper insertion as immediate single-item operations with source simulation,
sided query, slot order and source filters/access rules. Pinned target hopper and
dropper still have immediate Container paths, separate from ResourceHandler paths.

After actual native discovery returns neither Container nor ResourceHandler,
the target adapter MAY query the source block capability through the existing
source provider dispatcher, with the real destination face. Absent source provider
MUST preserve the original native fallback, including item-entity pickup/ejection.
Present but rejecting source provider MUST suppress that fallback. Existing native
containers/providers MUST retain target behavior without duplicate discovery.

Hopper block and minecart extraction and block-hopper/dropper insertion MUST move
one item through actual source insert/extract operations, preserving simulation,
slot order, item/data identity, destination limits and cooldown ownership. Never
mutate the stack returned by source getStackInSlot. Reject invalid provider return
values and simulator/execution disagreements explicitly. Source side/filter rules
remain source-owned. Native hopper owns its normal changed/cooldown lifecycle.
Hopper insertion removes one item before provider simulation/execution as in both
pinned source Forge and target vanilla; rejection restores the saved hopper slot.
Dropper providers instead observe the original selected slot until native writeback.

These are imperative source operations, not native transactional capability
exports. Calling them inside a target transaction MUST fail before source mutation.
General source->native ResourceHandler export, source entity capability discovery
and arbitrary mod automation remain explicit gaps. No custom Minecraft automation
framework, mod-specific inventory access, source artifact or MCP modification.

Verify independent source-provider fixtures and native applied mixins, then live
native hopper insertion/rejection/extraction with original source inventory rules.

Runtime79 reaches original BasicInventory.insertItem then fails on missing source
ItemHandlerHelper. Relocate that exact Forge class to retained source operations:
slot-order insertion, existing-stacks-first insertion, strict/relaxed stack
comparison, copy-with-size and inventory redstone fullness. Comparison MUST use
the existing source NBT/capability proof boundary, not substitute native component
equality or assume unknown item capabilities empty. Unsupported player-give
operations MUST fail explicitly until the source player-main wrapper and its
pickup/drop lifecycle have an adapter. Empty give remains the source no-op.

Runtime80 confirms output extraction and exact source NBT, then reaches the old
comparison proof boundary while stacking native gold ore. Pinned named source
Item/BlockItem headers and source ShulkerItemStackInvWrapper prove the ordinary
BlockItem default case and its17 exact item exclusions. Expand that existing
comparison proof without assuming unknown native subclasses capability-free.
