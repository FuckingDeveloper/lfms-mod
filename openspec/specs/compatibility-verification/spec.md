# Compatibility Verification Specification

## Purpose

LMS compatibility MUST be demonstrated by evidence at multiple layers rather than inferred from compilation or one large integration mod.

## Verification layers

### Static transformation tests

Focused tests SHOULD verify:

- namespace and descriptor migration;
- SRG/MCP/Mojang symbol resolution;
- nested constructor rewrites;
- multiple rewrites in one method;
- invokedynamic/lambda descriptors;
- signatures/annotations/handles;
- frame computation;
- inheritance/interface migration;
- fail-fast diagnostics.

### Synthetic legacy fixtures

The project SHOULD maintain small redistributable fixtures compiled against legacy Forge generations.

Fixtures SHOULD exercise one compatibility family at a time.

The compiled fixture is the test input and MUST remain unchanged when testing a newer target runtime.

### Runtime integration tests

Representative real mods MAY be used as external integration artifacts.

Runtime testing SHOULD record the first causal failure and distinguish downstream cascade errors.

### Functional tests

Once a mod reaches world load, tests SHOULD exercise observable behavior rather than only startup.

For a technology mod this may include:

- placing blocks;
- creating block entities;
- opening menus;
- recipes;
- inventory transfer;
- energy/fluid/item capabilities;
- client/server synchronization;
- world persistence.

## Regression requirement

A compatibility bug in LMS itself SHOULD gain a focused regression test when practical.

High-priority regression classes include:

- ASM traversal after node removal;
- nested NEW/DUP/<init>;
- classloader boundaries;
- frame hierarchy resolution;
- mapping lookup ambiguity;
- event Pre/Post fan-out;
- cross-registry staging;
- pending-registration failure safety;
- constructor pre-super invariants.

## Compatibility matrix

Every supported target generation SHOULD maintain an evidence-backed source-profile matrix.

Allowed status vocabulary SHOULD distinguish at least:

- PASS
- PARTIAL
- BLOCKED
- UNVERIFIED
- NOT_SUPPORTED

A matrix cell SHOULD link or refer to the test/integration evidence that justifies it.

## No false green

A test MUST NOT pass by swallowing the operation being tested.

A fixture intended to verify event delivery must prove the handler executed.

A fixture intended to verify registration must prove the registered object is visible through the target runtime.

A networking fixture must prove encode/decode/delivery semantics, not merely channel construction.

A creative-tab fixture must prove the item appears in the intended tab, not merely that Item construction succeeds.

## Upgrade gate

Before releasing LMS for a new target runtime generation:

- core transformation tests MUST pass;
- required synthetic fixtures for claimed subsystems MUST pass;
- at least one representative integration artifact for each claimed source generation SHOULD be exercised;
- known semantic gaps MUST be documented;
- the achieved compatibility level MUST be recorded.

## Whole-artifact linkage inventory

An offline diagnostic MAY transform every class in the admitted artifact graph
and inventory unresolved method/field/type references, including callable handles
and nested dynamic constants, without defining or initializing legacy classes.
Results MUST be grouped by exact owner/member/descriptor and retain call sites.
Transformation failures MUST remain visible. Optional integration classes and
unreached references MUST not be reported as proven runtime failures. Inventory
completion MUST not imply functional compatibility, complete verifier coverage,
or successful application of separate target launch/mixin transformations.
