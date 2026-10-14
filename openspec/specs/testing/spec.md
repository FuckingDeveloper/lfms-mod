# Testing Strategy Specification

LMS uses unit, mapping/transformation, profile, target-bootstrap, integration, reference-mod acceptance and regression test layers.

A compatibility fix SHOULD add a regression test whenever it can be reproduced without redistributing artifacts that cannot legally be included in the repository.

Reference legacy mods MUST NOT be committed unless redistribution rights permit it. Tests MUST distinguish client and dedicated-server behavior where relevant.

Release acceptance MUST identify automated checks and unavoidable manual checks separately. A passing unit suite alone MUST NOT be considered evidence of L3 or higher compatibility.

## Cross-version transformation fixtures

Generic mapping and transformer tests SHOULD use small redistributable synthetic bytecode fixtures representing owner moves, method removal, changed descriptors, inherited/interface calls, static/instance ambiguity, duplicate anchors, expression rewrites, local-slot shifts, and unsupported semantic changes.

Tests MUST include negative/ambiguity cases and assert that unsafe mappings remain unresolved. Early processors SHOULD be tested for transactional rollback, verifier rejection, class linkage and unchanged source artifacts.

Reference-mod integration tests MUST distinguish a successful transformed Minecraft method from actual loading and gameplay of the original legacy JAR. The 01.000.00 acceptance suite MUST test the declared gameplay subset separately from transformer unit tests.

## External MCP runtime debugging

When a configured `minecraft` MCP server is available, LMS development MUST use
its supported Minecraft API operations to verify the behavior affected by a
compatibility fix after the appropriate automated build/fixture checks. A fresh
Gradle `runClient` execution MUST run the fixed classes before acceptance. An
existing explicitly selected test world MAY be opened with the tested target's
native Quick Play launch argument. World load MUST be confirmed independently
from bridge connectivity before in-world checks.

MCP Fabric is external development instrumentation, not a new LMS subsystem or
a claim of Fabric source/target compatibility. Integration MUST NOT change LMS
architecture, patch immutable legacy artifacts, or introduce a custom game
automation framework. MCP Fabric sources SHOULD remain unchanged.

Verification MUST prefer structured client/world, inventory, block and container
data plus causal runtime logs. Screenshots MAY supplement rendering evidence
when structured data cannot prove the behavior, but MUST NOT be the default
connection, registration or world-readiness check.

The runtime procedure MUST discover exposed tools and check current side and
capabilities before use. Listed tools/methods alone MUST NOT prove that the
operation is currently usable. A responding bridge or initialized legacy
entrypoint MUST NOT establish world load, registry visibility or gameplay.
Registration checks MUST verify exact content identities through target runtime
operations; behavioral checks MUST execute the relevant source/target operation
and observe its result. Fixture construction commands alone MUST NOT prove
normal item placement, menu delivery, consumption or energy behavior.

Unavailable operations, unreachable bridges, world-load failures and denied
capabilities MUST remain explicit BLOCKED/UNVERIFIED results. A custom replacement
for missing MCP operations MUST NOT be implemented without agreement. Tokens,
local connection configuration, temporary files and test-run output MUST NOT be
committed. See `docs/runtime-debugging-mcp.md` for the operational runbook.
