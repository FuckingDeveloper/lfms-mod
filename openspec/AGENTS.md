# OpenSpec Agent Rules

These rules apply to work under `openspec/` in addition to the repository root `AGENTS.md`.

## Normative specification

OpenSpec is the normative architectural/product contract for LMS.

Use MUST, MUST NOT, SHOULD, SHOULD NOT and MAY deliberately.

Do not write implementation observations as normative requirements unless they are intended to constrain future implementations.

## Separation of concerns

Specifications MUST distinguish:

- legacy source-generation behavior;
- target-runtime behavior;
- target-independent semantic behavior;
- current implementation status;
- planned/future scope.

A planned source profile or target runtime MUST NOT be described as supported merely because it appears in a specification.

## Changes that require specification review

Review/update OpenSpec when implementing:

- a new source profile/profile family;
- a new target Minecraft/NeoForge generation;
- a new semantic compatibility subsystem;
- a change to compatibility/fail-fast policy;
- a new mod-specific extension mechanism;
- changes to compatibility-level definitions;
- lifecycle/registry behavior that changes architectural guarantees.

Small implementation fixes that remain inside an existing contract do not require a new proposal.

## Evidence and status

Specifications define required behavior; runtime logs/tests establish implementation status.

Do not encode transient IC2 failures as permanent product requirements.

Reference mods may be named as integration targets but MUST NOT define generic semantics.

## Cross-document consistency

When changing project scope, check at least:

- `openspec/project.md`;
- `openspec/specs/version-profiles/spec.md`;
- `openspec/specs/compatibility-architecture/spec.md`;
- `openspec/specs/target-runtimes/spec.md`.

Avoid duplicating long normative rules when a cross-reference is sufficient.

## Proposals

A substantial architectural implementation SHOULD have a change proposal/design/tasks set before or alongside implementation.

A proposal SHOULD state:

- why the change is needed;
- source/target scope;
- semantic guarantees;
- explicit non-goals;
- failure behavior;
- verification plan;
- migration/compatibility impact.

## No speculative support claims

Never mark a source generation, target generation, subsystem or compatibility level supported without observed evidence.

Illustrative matrices and examples MUST be labelled as illustrative.
