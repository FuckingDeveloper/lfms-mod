# Diagnostics Specification

## Requirements

LMS diagnostics MUST be able to report:
- LMS version and target runtime (including target loader and exact build);
- discovered legacy artifacts;
- selected profile or reason no profile was selected;
- applied transformation rules;
- unsupported APIs or integrations detected;
- compatibility level and evidence used to assign it;
- fatal compatibility failures.

Diagnostics MUST distinguish verified behavior from inferred or untested behavior.

## Resolution trace

Each planned transformation SHOULD expose a compact machine-readable decision trace: legacy source identity, canonical owner, candidate current owners/methods, mapping provenance, runtime evidence, rejected candidates, unresolved reason and readiness.

Diagnostics MUST distinguish (1) planned, (2) bytecode verified, (3) class linked, and (4) behavior tested. Successful ASM verification alone MUST NOT be reported as successful legacy-mod compatibility.

A failed transform SHOULD include the failing instruction/anchor and verifier cause when available. Diagnostics SHOULD allow filtering by artifact, transform ID, profile and failure category without requiring verbose full-game logs.
