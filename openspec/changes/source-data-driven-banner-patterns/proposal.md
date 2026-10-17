# Programmatic source banner patterns on a data-driven target

## Evidence

Pinned Forge43.4.0 BannerPattern stores a string hash name; location(key,banner)
derives texture identity from the actual registry key. Source banner patterns are
programmatic registry entries. Runtime112 fails on Registry.f_235735_, the source
BANNER_PATTERN registry key, before the five original declarations can attach.
Minecraft26.3 BannerPattern is an immutable (Identifier assetId,String
translationKey) record with DIRECT_CODEC; its registry is loaded from data packs.
It is not a native built-in registration target. Merely renaming the key leaves
factories forever unexecuted and is not an implementation.

## Proposed explicit native lifecycle policy (approved by user)

For this exact source registry family, retain immutable source declarations and
execute attached deferred factories once after native built-in registration and
before original source common setup. Factories keep owner, order, dependencies
and original failures; no fabricated native RegisterEvent or registry unfreezing.
Serialize resulting definitions through the actual target BannerPattern codec
into a low-priority built-in server-data pack before native dynamic data loading.
Native data packs and world registry publication remain authoritative.

Keep source hash-name intent as source metadata, not a guessed target asset ID.
Texture identity derives from the declared registry key, as in the source.
Source direct object identity and world-loaded native value/holder identity must
be distinguished; do not bind source holders to an unrelated synthetic registry.
Source hash lookup and native component/banner-pattern-item behavior need explicit
verified adapters. Unknown patterns/subclasses and early reads before declaration
materialization fail explicitly.

## Semantic choice requiring approval

This moves this family’s source factory execution to the supported pre-setup
boundary. Source code that needs a banner value during built-in block/item
registration is outside this bounded policy. Alternative: design a broader source
registry lifecycle preserving cross-registry ordering and early source reads.
The user approved native data loading. Implementation must still pass the checks below before any support claim.
Original legacy JARs remain immutable; no fixture identity selects the rule.

## Verification

Independent immutable source fixtures, duplicate/late/failure/early-read checks,
native codec/resource reload and actual banner-pattern registry/tag/loom behavior
are required separately. Menu startup is not functional banner acceptance.
