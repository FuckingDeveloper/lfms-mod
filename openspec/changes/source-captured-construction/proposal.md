# Preserve captured source construction and public callback contracts

## Scope and cause

Forge 1.19.2 on Minecraft26.3/NeoForge26.3.0.58-beta. Runtime111
exposed three independent general source contracts: direct six-argument Button
allocation, public cross-block callbacks, and Item.Properties captured before
DeferredRegister executes its factory. These are source-generation operations,
not fixture selectors. Original legacy artifacts remain immutable.

## Contract

- The exact supported source Button constructor MUST create a native button with
  the original bounds, title and callback. Allocation rewriting MUST preserve JVM
  initialization rules. Unknown allocation shapes and tooltip forms fail closed.
- The verified source-public Block/BlockBehaviour signatures with unchanged native member identity and descriptor MUST
  remain callable across block instances with native virtual dispatch. Access
  widening MUST include actual target overrides to preserve Java visibility
  consistency; it MUST NOT change finality or unrelated callback signatures.
- Within an actual source ITEM deferred factory, captured exact native
  Item.Properties without an explicit identity MUST receive that entry's native
  identity before constructor consumption. Reused source builders MUST NOT be
  mutated. Component, remainder, feature, description, repair and creative-tab
  intent MUST be preserved. Existing explicit source identity takes precedence.
  Unknown Properties subclasses MUST fail rather than lose subclass behavior.
- Outside that construction scope, native item construction MUST be unaffected.
  No frozen registry mutation, new registration phase or source factory reorder
  is introduced.

## Evidence and boundaries

Source evidence is the pinned named Forge43.4.0 JAR (SHA256
579b78e19ff551ae3b340a6f908ca0923633a9395b35e6ae446772a778793bb4).
Target evidence is the configured patched26.3.0.58-beta artifact. The callback
access metadata generator verifies exact source owners/names/descriptors and target
ancestry without loading game classes; its current result contains768 entries.
This includes source Block.isRandomlyTicking now declared by BlockBehaviour.
Visibility restoration is not a claim that every callback has equivalent timing
or that changed signatures are adapted. No final flags are changed.
The target Properties model field is final ResourceKey::identifier; the new
snapshot retains this native default while copying all mutable construction
fields exposed by the configured target.

Independent checks cover direct Button allocation/callbacks, distinct item model
identities and snapshots, subclass rejection, and actual native cross-block
virtual access. These checks do not establish successful item registration,
world placement, arbitrary Button constructor handles or arbitrary custom
Properties subclasses. Fresh runtime results are recorded separately.
