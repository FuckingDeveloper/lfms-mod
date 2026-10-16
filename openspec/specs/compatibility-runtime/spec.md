# Compatibility Runtime Specification

## Requirements

- Runtime bridges MUST represent semantic compatibility that cannot be safely reduced to static rewriting.
- Core runtime bridges MUST be mod-agnostic.
- Mod-specific behavior MUST live outside the core runtime.
- Runtime adaptation MUST expose diagnostics for unsupported operations where practical.
- Compatibility APIs SHOULD minimize per-call overhead when equivalent behavior can be established during transformation/bootstrap.

## Durability callback boundary

Source item damage hooks MUST receive the original entity callback once, even
when target hooks use a stack callback with the same erased descriptor. A native
caller of a source hook MAY receive a stack snapshot when that source callback
fires. Source stack hurt-and-break MUST retain client/creative/damageable guards,
notify before consumption, award the source break statistic, and reset damage
after consumption. Source damage hooks MUST remain virtual and superclass calls
MUST not redispatch recursively. Source supplied-random enchantment evaluation
remains fail-fast for enchanted positive damage until its source probability
contract is verified; unenchanted damage and zero/negative changes are independent.

## Source neighbor origins

Source block neighbor overrides MUST receive the original changed-block position.
Target queued notifications MUST retain that position on each queued operation,
not in a global map or a scope that expires before the queue executes. Native
multi-neighbor updates MAY use their authoritative stored source position.
Explicit source single/full notifications MUST retain their supplied origin.
Nested dispatch MUST restore prior origin and level on completion or exception.
Unknown native origins MUST fail explicitly if a source callback consumes them;
nullable Orientation MUST NOT be used to guess a source position.

## Mutable stack-tag ownership

A source stack tag query MUST retain a live mutable tag for that stack while its
custom-data component remains the same. First source access to immutable/native
custom data MUST detach it from shared component/default state. Source tag set
MUST preserve the supplied tag identity, including intentional explicit aliases.
Ordinary target stack copies and transfer-resource snapshots of source-owned
mutable custom data MUST deep-copy that data so later source mutation cannot
alter an inventory comparison snapshot, another copied stack or a resource value.
Ownership state MUST belong to the stack, not a global map of stacks/loaders.
Native replacement/removal of the component MUST supersede its old ownership.
This establishes ownership and change-detection semantics, not callback delivery,
item energy algorithms or successful packet synchronization in a running world.

## Source logical empty stacks

Complete source stack decoding MUST preserve the source signed-byte count/default
semantics while migrating the full copied schema through the target data fixer.
Source nonpositive counts and explicit air items MUST NOT fail solely because the
target ordinary stack codec requires positive non-air values. Their item identity
and component payload MUST still be decoded against authoritative bound registry
holders; malformed/unknown data MUST NOT be hidden by arbitrary empty returns.

Source mutable tag access MUST retain the decoded payload and live ownership when
a native stack count becomes zero. A target public component view hiding an empty
stack's underlying data MUST NOT be treated as component removal. Native public
component visibility MUST remain unchanged; genuine replacement/removal of the
underlying source-data component MUST continue to supersede its old ownership.
