# Source creative-tab enumeration

## Status

Approved by the user on 2026-10-17: native enumeration with preserved source
callbacks; no old array mutation or numeric-position emulation. Implementation
and runtime acceptance are tracked separately in tasks.md.

## Problem and evidence

Forge 1.19.2 exposes mutable `CreativeModeTab.TABS`, numeric tab positions,
special tab objects and virtual `fillItemList(NonNullList)` enumeration. Exact
source mappings identify `f_40748_` as TABS, `f_40760_` as HOTBAR,
`f_40761_` as INVENTORY, and `m_6151_` as fillItemList.

The pinned source Forge class declares the array non-final and assigns tab IDs
through `addGroupSafe`. The configured target instead provides registry-backed
`CreativeModeTabs.allTabs()` and content building parameterized by enabled
features, permissions and registry lookup. These are different contracts.

Runtime128 reaches the world and the original JEI startup observer, but its
ItemStackListFactory fails at the removed TABS field. Its bytecode uses the array
for iteration, skips HOTBAR/INVENTORY by identity, and calls fillItemList.
Scanning the seven current legacy artifacts also finds a tab-level fillItemList
override in TNT Expansion. Existing item-level population alone cannot preserve
that callback. Neither fixture's identity may select the implementation.

## Proposed decision

Use the target's current registered tabs and native content rules for proven
read-only enumeration. Preserve source custom-tab objects, tab-level and
item-level callbacks. Do not emulate the old global array's mutation, persistent
array identity or numeric positions. Reject uses requiring those semantics or
whose read-only nature cannot be established.

The alternative is a separate source array/index model and an explicit mapping
of old vanilla categories to the new categories. That requires a broader design;
the target's ordering must not silently stand in for old numeric IDs.

## Scope and verification

Forge 1.19.2 source normalization remains separate from target 26.3 registry and
client content preparation. Original mod JARs remain immutable. No new source
generation, recipe transfer implementation, or complete JEI support is claimed.

See design.md and tasks.md for the bounded contract and acceptance gates.
