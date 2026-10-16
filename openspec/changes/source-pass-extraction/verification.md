# Pass extraction evidence — 2026-10-16

Current tested tuple remains Forge1.19.2 -> Minecraft26.3 /
NeoForge26.3.0.58-beta / Java25. No additional source/target or functional family
is claimed. The previous successful gameplay baseline was runtime85.

## Implementation

Shared SemanticInvocationRewriter now owns the existing traversal for direct
instance calls, callable handles, lambda capture types and nested dynamic
constants. It depends on ASM/JDK types and supplied member/subtype proof, not
Minecraft, NeoForge or a concrete source profile. Existing call/handle consumers
in the source transformer delegate to this one implementation.

Source Forge1192BlockDataRewriter owns persistence/update callback rewriting and
its raw-header callback cache. The transformer supplies explicit source evidence
and resets this cache under its existing bind/write lock; rewrites retain the
existing read lock. Native/source callback shapes, super defaults, source NBT,
wrapper naming, unresolved boundaries and pipeline IDs/order are unchanged.
The source pass still contains current target callback ABI strings; this is
source/target compatibility code, not a target-independent storage abstraction.

The extraction was checked mechanically against the captured original source.
Both new components are identical whether generated from the original HEAD or
the original dirty transformer; none of the prior user edits was moved into the
checkpoint. All eight original dirty/untracked files remain preserved.

## Automated checks

Final `./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava`
passed: /tmp/lms-pass-extraction-check-3.log. The first compile exposed an
annotation left above the extracted interface; it was moved with the interface
before verification. Existing native block-data, callback/super, capability
persistence, menu, sound and callable regression checks continue to pass.

InvocationExtractionCheck independently defines and links a Java fixture with
an inherited direct call, LambdaMetafactory capture of a subclass, unbound method
handle and nested ConstantDynamic. It checks real invocation results and exact
callable receiver/return types, plus unrelated-owner and changed-return rejection.
No Minecraft class or source profile supplies these fixture semantics.

BlockDataExtractionCheck verifies inherited source dispatch versus native wrapper
creation, stable header snapshots until explicit reset, changed graph visibility
after reset, source aliases and collision rejection. Synthetic byte arrays are
changed for rebinding; original legacy JARs remain immutable.

Dependency-complete compatibilityAudit passed:3412 classes,1086 grouped findings,
7253 observations. The report exactly matches the preceding audit, with no added
or removed rows. Raw logs/reports remain in /tmp. This is an inventory and does
not close fluid/optional integration gaps.

## Runtime86

Started using native Quick Play:
`./gradlew runClient -PlmsTestWorld='Новый мир'`.
Log: /tmp/lms-container-families-runtime-86.log.
Runtime content checks are recorded below after world readiness.


Runtime86 reached the intended integrated world with player Dev and all MCP
capability groups available. CarbonConfig, IC2 and Iron Chests entrypoints
initialized through the existing profile/target binding. Actual IC2/Iron Chests
items and source armor were read from the player inventory.

The saved generator at(-206,78,12) loaded from disk. Native serialization through
`data get block` before/after real use-item open -> close -> reopen returned the
same source storage4000, side49/access3843 and comparator flags5/slot6,max2/slot3.
Both asynchronous container opens were confirmed by subsequent MCP container
probes, which requested an absent bedrock item and moved nothing. Native
serialization exercises the source save path as well as the cold-load path;
this does not claim a new save/restart mutation test or all callback variants.

Inventory API results matched exactly before/after. Camera was restored; a small
position drift during the probe was restored to the captured position via the
native teleport command. Health, food, XP, mode and selected slot stayed unchanged.
No test items/blocks were created, removed or transferred. No ERROR/FATAL/Caused-by
entries appeared through final checks. Visual rendering and every config widget
were not manually re-accepted in this pass. The client is left running.

Overall observed compatibility remains level6 plus these bounded level7 paths.
The next broad functional gap remains F07 source fluids; helper extraction does
not close that gap or establish another generation/target. The transformer still
owns additional families and target-aware bridge rules, so further extraction
must remain evidence-led.
