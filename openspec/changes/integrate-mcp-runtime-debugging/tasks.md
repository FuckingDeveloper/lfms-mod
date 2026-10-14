# Integration tasks

- [x] Define tool discovery, target/client identification and world-readiness gates.
- [x] Document native Quick Play for explicitly selected existing test worlds.
- [x] Define content identity checks and separate actual behavioral assertions.
- [x] Map supported client, world, interaction, inventory and command tools.
- [x] Document unsupported operations and blocked-check handling.
- [x] Integrate the runbook into repository agent rules, testing spec and README.
- [x] Keep LMS architecture, legacy inputs and MCP Fabric sources unchanged.
- [x] Keep connection configuration and runtime evidence outside Git.

Execution evidence and first causal runtime blockers belong in temporary/local
reports, not this change's committed files. Per-run world and gameplay checks
remain conditional on successful world load and actual bridge capabilities.
