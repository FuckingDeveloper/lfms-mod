# Proposed enumeration contract

The native enumeration choice in proposal.md was approved on 2026-10-17.

## Source normalization

- The source profile MUST identify fields/members from verified source owner,
  inheritance and descriptor evidence, including constant/handle forms where
  admitted; descriptor coincidence alone is insufficient.
- TABS access MAY be adapted only when its use is proven to be read-only
  enumeration. Mutation, array escape with unknown semantics, old numeric-ID
  dependence and persistent array-identity dependence MUST fail explicitly.
- Enumeration MUST retain actual registered tab-object identities. HOTBAR,
  INVENTORY and SEARCH tokens MUST resolve to their corresponding native tabs.
- The bridge MUST retain virtual source tab-level fillItemList overrides and
  source item-level enumeration. A source super call MUST execute the source
  base behavior without recursively invoking the override.
- The same verified source override contract MUST govern native custom-tab
  population and source enumeration; it MUST NOT depend on fixture identity.

## Target boundary

- The target adapter MUST supply current registered tabs in native enumeration
  order. This is not the source array's historical ordering or numeric identity.
- Native tab contents MUST be built through the actual target population API,
  using the active connection/world's enabled features, registry lookup and
  native permission policy. Missing context MUST fail explicitly rather than
  manufacture empty or unrestricted contents.
- Population MUST occur on the valid target thread and after registration/data
  readiness. It MUST preserve native events and invalidation/cache behavior and
  reject unsafe reentrant population.
- Source callbacks MUST execute with the correct source ownership. Retained
  source output MUST obey the existing native stack validation/deduplication
  contract. New caches MUST NOT retain disposable source classloaders.

## Verification and limits

Independent transformation fixtures MUST cover accepted iteration and rejected
mutation/escape/index semantics, including unsupported handle forms. Native
fixtures MUST cover special-tab identities, actual contents, source override
dispatch/super calls, invalid context and repeat enumeration. Tests MUST verify
that native population invokes the tab-level override as well as per-item
callbacks. Runtime acceptance requires the original consumer to pass the former
TABS blocker; later JEI failures remain separate causal blockers.

## Bounded implementation and verification

The initial array admission recognizes the compiler's enhanced-for loop shape.
It proves the array, length and index locals do not escape or get reassigned,
checks the zero-origin/unit-increment loop and rejects external jumps into it.
Other read-only forms are not yet admitted. Array handles, including nested
constants, are rejected; special-tab getters and virtual fill handles are
projected by the verified owner/member contract.

External fillItemList enumeration reads copied native display stacks after the
native content-building API has run. Source tab-level callbacks execute during
that native population in the recorded tab owner's scope. A source super call
uses preserved source item membership and item overrides, without calling the
tab override again. Reentrant content preparation fails explicitly.

On 2026-10-17 compilation, compatibilityTest, nativeCompatibilitySmoke and
compileLaunchJava pass. The native smoke exercises the actual installed native
builder's generator without modifying its frozen registry; full native registry
population requires the integration client. The static audit processes 3829
classes with the same two prior unrelated transformation failures. Inventory
counts are not functional coverage. Logs: /tmp/lms-creative-final-check.log and
/tmp/lms-creative-audit.tsv. Runtime acceptance remains pending at this checkpoint.

Source-parent super calls with a verified managed declaration retain their exact
invokespecial dispatch. Only the proven native source-base super call is replaced
by base membership enumeration. An inherited super call whose declaration cannot
be established remains fail-fast rather than skipping a potential parent body.
Independent native fixtures cover the child/parent/native-base chain, duplicate
outputs, source-scope restoration and unresolved inherited-super rejection.

Runtime129 confirms the original consumer passed the array/token/fill boundary
and native population produced nine nonempty source tabs. Complete JEI startup
is still blocked by removed RegistryManager.ACTIVE; reopening a screen retries
its failed startup and crashes with missing vanilla crafting category. The client
exited before the attempted TERM; no process was killed or relaunched afterward.
After this live run, the source-parent dispatch refinement passed compatibilityTest,
nativeCompatibilitySmoke, compileLaunchJava and the full audit; the additional
negative fixture passed nativeCompatibilitySmoke. The refinement has native-smoke
acceptance, not another live-client acceptance. Logs:
/tmp/lms-creative-super-check.log and /tmp/lms-creative-super-negative-check.log.
