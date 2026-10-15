# Source pure combat helpers

Runtime55: native player armor wear reached source setDamage and both native and
source Damage became 1. The source player callback then failed at
CombatRules.m_19272_(FFF)F. Whole-fixture owner-use inspection found the helper in
its armor base; migrate the API family rather than that fixture callback.

Pinned named Forge 1.19.2 bytecode and its SRG artifact establish both helpers,
their four public constants and empty public constructor. Physical absorption
is a pure three-float function. Target 26.3 adds victim and DamageSource to apply
weapon enchantments; these inputs do not exist at a source helper call. Preserve
the verified pure source computation in a source facade, without inventing a
victim, weapon or source. The target magic helper remains a pure two-float
operation with identical bytecode intent and can be delegated. Preserve SRG and
named member identities, descriptors, constants and general class/handle mapping.
This does not replace the target native damage pipeline or grant new source hooks.

Check actual transformed executable calls and method handles for both operations,
including the lower armor floor and upper protection caps. Runtime acceptance
must include real player damage after the previously successful native wear step.

## Verification and test-world recovery

`compatibilityTest nativeCompatibilitySmoke compileLaunchJava` passed (check58).
Runtime56 physical player hit completed: armor 8, health 20 -> 16.96 for damage 4,
with both source Damage and native DAMAGE set to 1. No causal runtime exception
or ERROR/FATAL was observed. Native source callback behavior is demonstrated;
this is not a claim of all armor/enchantment/custom-model compatibility.

The elevated test platform was unsafe: native knockback moved the player off it
and fall damage killed the player before the next agent turn restored creative
mode. The user was informed and respawned through the UI. Recovery copied actual
dropped inventory stacks (including original charge/module NBT) into the 25
recorded slots. Two merged plain cables were split into their original slots.
The original plain hazmat equipment was reconstructed from the captured full
native equipment NBT (four count-1 stacks with no component patches); the backup
barrel was empty at recovery and was not treated as a surviving authoritative copy.

Restoration verified all 25 slot/id/count entries against the pre-test inventory,
source charges 96600/10000 and four quantum 1000000-EU module sets. Authoritative
server NBT confirmed XP level 1/progress 0.33333334 and saturation 20; player health,
food, pose, selected slot and creative mode were restored. Only recorded drop/XP
entity UUIDs were removed after copying/verification; no drop entities remained
in the recovery area. Temporary blocks, visual-probe offhand item and both newly
added forced-chunk tickets were removed. The native death statistic remains;
full historical player statistics were not fabricated or edited.

Future physical player probes must use safe ground with knockback clearance,
not an elevated platform. Inspect each failed restoration before any dependent
cleanup; a failed copy is never permission to clear its source. Client snapshots
may lag command results, so recovery must also verify authoritative server NBT.
