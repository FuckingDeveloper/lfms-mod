# Common tag identity contract

The accepted common-tag-identity-projection proposal permits a bounded exception
to resource tag identity preservation. Source profile evidence contains324 pinned
Forge1.19.2-43.4.0 declarations (registry, bucket, member, source identifier).
Target263 admission requires an actual public static TagKey declaration in the
same registry and the same declared path, or one of the14 already verified
common-tag member renames. A matching name/descriptor alone never admits a
different path. Missing/unproven entries remain unchanged. No mod identities
select the rule. Source table and target declaration policy remain separate.

Exact source TagKey.create and the Forge-added ItemTags/BlockTags/FluidTags.create
helpers MUST select the canonical native key. Public source constructors MUST
preserve fresh object identity while normalizing the identifier; only initialized
registry/identifier arguments may enter the helper before invokespecial. Source
flat/hashed codecs preserve the inspected native/source scalar syntax and
canonicalize decoded values. Native factories/codecs outside managed source
bytecode are unaffected. Direct calls, handles and nested constants follow the
same operation identity.

Admitted source packs MUST project tag definition identifiers and nested #tag
references through the same registry-aware table. Unknown custom tags/plain
item IDs remain unchanged. Native entries in the same pack win over projected
entries; native resource-stack replace/order semantics remain authoritative.
Get/list/namespace discovery MUST agree, including cross-directory path renames.
Do not cache procedural resource bytes across bootstrap/final/reload phases.
Only admitted source packs are wrapped. Non-tag resources, native packs and
client resources retain existing semantics. Other JSON schema fields outside
tag definitions are not part of this verified contract.

The declaration index is immutable per runtime, bounded by324 source entries,
and contains only native keys; it does not retain disposable source classloaders.
Derived resources are streamed anew through each native resource reload and have
no persisted transformation cache. The packaged source catalog and adapter code
are inputs to the runtime artifact; future persistent caches MUST include both
and the exact target ABI in their identity.

Verification: compatibilityTest, nativeCompatibilitySmoke and compileLaunchJava
passed together in batch-check-15. Checks execute canonical native item/block
identities, wrong-registry/unknown-ID boundaries, a transformed source factory,
ordinary fresh constructor, factory handle and nested constructor handle, flat/
hashed codec syntax and malformed hash rejection, original immutable tag helper,
resource references/optional flags, renamed definition paths, native precedence,
namespace discovery and mutable procedural relisting. Earlier check13 exposed
that original helpers use the Forge-added ItemTags.create API; all three verified
item/block/fluid helpers are included, not just the general TagKey factory.

Runtime60 cold Quick Play completes bootstrap/final native data phases and loads
the original world without manual reload. Real original macerator GUI Shift path
accepts one vanilla copper ore; source state contains that item in slot1. An
explicit test energy input800 is consumed by original ticks and slot2 becomes
two original copper dusts. Close/reopen and Shift extraction succeed; source
inventory is empty afterward. This proves the prior copper recipe blocker is
removed, not every mod recipe. No fresh ERROR/FATAL observed during the probe.

Fixture restored to observed original air. Player stays creative throughout;
full structured inventory, XP progress, position, camera and selected slot match
the runtime60 baseline after native click synchronization. Source jar is unchanged.
Other unproven JSON schemas, native energy direction flags and custom armor model
contracts remain separately tracked gaps.
