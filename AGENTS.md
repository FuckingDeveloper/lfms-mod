# AGENTS.md — LMS Development Rules

These instructions apply to all automated coding agents working in this repository.

## 1. Specification-driven development is mandatory

LMS uses specification-driven development (SDD).

Before changing architecture or compatibility semantics:

1. read `openspec/project.md`;
2. read the relevant files under `openspec/specs/`;
3. inspect the current implementation and runtime evidence;
4. update/add specification text when a change introduces a new architectural contract;
5. implement against that contract;
6. verify the implementation.

Implementation MUST NOT silently redefine a normative MUST/MUST NOT requirement.

When implementation and specification disagree, do not silently choose one. Determine whether the code is wrong or the spec requires an explicit change.

## 2. Product identity

LMS is a binary-first compatibility engine for running legacy Forge/FML mods on modern Minecraft/NeoForge.

It is NOT:

- an IC2 compatibility mod;
- a source-porting tool that requires legacy mods to be recompiled;
- a security sandbox;
- a collection of startup hacks whose only goal is reaching the menu.

Legacy artifacts are immutable inputs.

Do not modify, recompile or patch the original legacy mod JAR to make a test pass.

## 3. Two independent version axes

Always distinguish:

- **source generation**: Forge/FML version the legacy mod was compiled for;
- **target generation**: Minecraft/NeoForge/Java version LMS currently executes on.

Source-specific behavior belongs in source profiles/profile families.

Target-specific behavior belongs in target adapters/runtime integration.

Shared semantic intent belongs in target-independent LMS runtime/semantic services.

Do not design new code under the assumption that Forge 1.19.2 or Minecraft 26.3 will remain the only supported generation.

Read:

- `openspec/specs/version-profiles/spec.md`
- `openspec/specs/compatibility-architecture/spec.md`
- `openspec/specs/target-runtimes/spec.md`

## 4. No hidden reference-mod special cases

Production generic compatibility code MUST NOT branch on:

- `modId == "ic2"`;
- `ic2/` package prefixes;
- exact IC2 class names;
- CarbonConfig class names;
- another integration fixture's identity;

when the underlying compatibility rule is a Forge/Minecraft generation rule.

If an existing special case can be expressed using verified owner/member identity, mappings, inheritance, descriptors and/or bytecode shape, generalize it.

Descriptor coincidence alone is insufficient.

If truly unavoidable mod-specific compatibility is needed, stop and isolate/design it explicitly outside generic LMS core/profile code.

## 5. Evidence hierarchy

Before implementing a migration, prefer evidence in this order:

1. actual target classes/source available in the configured development runtime;
2. target NeoForge source/patches for the exact version;
3. generated/patched artifacts and mappings;
4. official NeoForge/Forge documentation for the relevant version;
5. verified legacy bytecode/source behavior.

Do not implement an adapter from memory or from an API example belonging to another Minecraft version.

The actual target ABI wins.

## 6. Semantic migration, not blind renaming

A similar class or method name is not proof of equivalent behavior.

For changed APIs verify, where applicable:

- invocation timing;
- cancellation semantics;
- thread/side semantics;
- ownership and lifecycle;
- registry mutability/phase;
- return-value meaning;
- data lifetime;
- networking direction/threading;
- rendering phase/model.

Use a semantic bridge when behavior changed.

Fail fast when equivalence cannot yet be established.

## 7. No fake compatibility

Forbidden solely to make startup advance:

- `catch (Throwable)` and continue;
- arbitrary null/dummy returns;
- swallowing registration errors;
- silently skipping event listeners;
- removing meaningful calls without proof;
- mutating frozen/global registries outside the target lifecycle;
- disabling content;
- claiming functional support from linkage alone.

A deliberate no-op requires evidence that the removed source operation has no target-runtime semantic effect needed for compatibility. Document the limitation.

## 8. Family-level debugging

When one failure reveals an API family:

1. identify the first causal blocker;
2. inspect all related uses in the legacy fixture;
3. inspect the source-generation contract;
4. inspect the target-generation contract;
5. implement all safe/proven members of the family;
6. leave uncertain members fail-fast;
7. compile and runtime-test.

Avoid spending one full client run per trivial sibling method when the family can be proven together.

## 9. ASM safety

When modifying bytecode:

- preserve verifier/stack correctness;
- preserve constructor initialization rules;
- never use uninitialized `this` illegally before `super()`;
- correctly pair nested `NEW/DUP/<init>` sequences;
- do not continue traversal from removed/detached ASM nodes;
- handle descriptors, signatures, annotations, handles, invokedynamic and ConstantDynamic when relevant;
- use the project's safe frame/hierarchy mechanisms rather than loading partially transformed legacy classes.

When a transformation is subtle, inspect transformed bytecode or add a focused regression test.

## 10. Mapping safety and performance

Never guess SRG/MCP/Mojang symbol identity.

Use mapping evidence and semantic identity.

Repeated symbol lookup in transformation hot paths MUST be indexed/cached appropriately; do not scan an entire mapping dataset for every instruction.

Caches MUST NOT accidentally retain disposable legacy classloaders indefinitely.

## 11. Registration semantics

Legacy cross-registry registration may need staging until the matching target `RegisterEvent`.

Required invariants:

- values may be constructed before their target registration phase when legacy behavior requires it;
- mutation occurs only in a valid target phase;
- registration after the target phase has completed fails explicitly;
- pending entries must not be silently lost on partial flush failure;
- global/frozen registries are not mutated as a shortcut.

## 12. Linkage versus functionality

Track separately:

- structural/linkage compatibility;
- lifecycle compatibility;
- functional compatibility.

Examples:

- making `Item.Properties.tab(...)` link does not prove creative-tab membership works;
- creating a tool item does not prove attributes/repair tags work;
- resolving a capability type does not prove capability behavior works;
- registering a packet does not prove codec/thread/direction semantics work.

Do not overstate progress.

## 13. Creative tabs

Old item-property tab assignment and modern creative-tab population are different lifecycle models.

A construction adapter MAY temporarily preserve the fluent chain, but the legacy tab intent must either:

- be preserved and later applied through the modern population lifecycle; or
- remain an explicit compatibility gap.

Do not mark creative-tab compatibility complete because the old setter became a no-op.

## 14. Networking and rendering

Treat these as semantic migration boundaries.

Old Forge SimpleChannel and modern payload APIs MUST NOT be blindly namespace-renamed.

Old immediate rendering and modern extracted/render-state pipelines MUST NOT be declared equivalent without an explicit model.

Preserve fail-fast boundaries until a general adapter is designed.

## 15. Build and runtime verification

Use the repository Gradle wrapper.

For meaningful compatibility changes:

1. compile;
2. run focused tests if present;
3. run the appropriate integration client/server when practical;
4. verify the previous blocker disappeared;
5. identify the next first causal blocker.

Typical client run:

```bash
./gradlew runClient > /tmp/lms-run.log 2>&1
```

Useful filter:

```bash
grep -nE 'LMS lifecycle|LMS runtime-session|LMS managed-transform|ERROR|FATAL|Exception|Caused by:|NoClassDefFoundError|ClassNotFoundException|VerifyError|NoSuchMethodError|NoSuchFieldError|AbstractMethodError' /tmp/lms-run.log
```

When asking a human to test a build, ALWAYS provide the exact command/filter needed to collect the relevant evidence.

Do not ask the human to choose arbitrary log excerpts.

## 16. Apparent hangs

Do not label a client as deadlocked solely because the loading UI stops moving.

Use thread dumps when needed.

Distinguish:

- normal main-thread wait for modloading workers;
- active bytecode transformation;
- a real lock/deadlock;
- NeoForge fatal error UI/Swing dialog waiting for user interaction.

Find the causal exception earlier in the log.

## 17. Compatibility milestones

Use the levels defined in the compatibility architecture spec.

Never claim "working" based only on compilation, linkage or reaching a loading screen.

Record the highest milestone actually observed.

## 18. Git safety

Before work:

```bash
git status --short --branch
git branch --show-current
```

Preserve all pre-existing user changes.

Never use destructive cleanup without explicit permission:

- `git reset --hard`
- `git clean -fd`
- `git checkout -- .`
- broad `git restore .`
- force push

Do not stage generated `run/` state, logs, IDE state, `.DS_Store`, or unrelated user changes.

Commit verified work in coherent checkpoints and push periodically during long autonomous sessions.

Do not wait until quota/context exhaustion before preserving verified progress.

## 19. Commit quality

Good commits represent one coherent compatibility family or infrastructure correction.

Examples:

- `Optimize legacy SRG symbol lookup`
- `Adapt legacy tick event family`
- `Stage cross-registry legacy registrations`
- `Adapt legacy tool item construction`

Do not combine unrelated generated files or user changes.

## 20. Target upgrades

A new target Minecraft/NeoForge generation requires the process in `openspec/specs/target-runtimes/spec.md`.

Compilation on a new target is not compatibility proof.

Do not widen metadata version ranges to untested future versions.

## 21. Source-generation expansion

Do not implement another legacy generation by copying the entire Forge 1.19.2 transformer.

Introduce/reuse profile-family and semantic boundaries.

Canonical planned generations are documented in `openspec/specs/version-profiles/spec.md`.

## 22. When to stop for a design decision

Autonomous debugging SHOULD continue through ordinary compile/link/runtime failures.

Stop and request a design decision when:

- multiple target semantics are materially plausible;
- a change would introduce mod-specific behavior into generic LMS;
- required evidence is unavailable;
- continuing requires modifying a legacy input artifact;
- a destructive repository operation is required;
- a compatibility claim would need to be weakened or falsified.

## 23. Reporting

When pausing/stopping, report:

- current observed compatibility level;
- last successful milestone;
- first remaining causal blocker;
- implemented compatibility families;
- commands/tests actually executed;
- commits created/pushed;
- uncommitted changes;
- known semantic gaps.

Never claim a test was run if it was not.
