# Testing Strategy Specification

LMS uses unit, mapping/transformation, profile, target-bootstrap, integration, reference-mod acceptance and regression test layers.

A compatibility fix SHOULD add a regression test whenever it can be reproduced without redistributing artifacts that cannot legally be included in the repository.

Reference legacy mods MUST NOT be committed unless redistribution rights permit it. Tests MUST distinguish client and dedicated-server behavior where relevant.

Release acceptance MUST identify automated checks and unavoidable manual checks separately. A passing unit suite alone MUST NOT be considered evidence of L3 or higher compatibility.

## Cross-version transformation fixtures

Generic mapping and transformer tests SHOULD use small redistributable synthetic bytecode fixtures representing owner moves, method removal, changed descriptors, inherited/interface calls, static/instance ambiguity, duplicate anchors, expression rewrites, local-slot shifts, and unsupported semantic changes.

Tests MUST include negative/ambiguity cases and assert that unsafe mappings remain unresolved. Early processors SHOULD be tested for transactional rollback, verifier rejection, class linkage and unchanged source artifacts.

Reference-mod integration tests MUST distinguish a successful transformed Minecraft method from actual loading and gameplay of the original legacy JAR. The 01.000.00 acceptance suite MUST test the declared gameplay subset separately from transformer unit tests.
