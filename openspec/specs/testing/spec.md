# Testing Strategy Specification

LMS uses unit, mapping/transformation, profile, target-bootstrap, integration, reference-mod acceptance and regression test layers.

A compatibility fix SHOULD add a regression test whenever it can be reproduced without redistributing artifacts that cannot legally be included in the repository.

Reference legacy mods MUST NOT be committed unless redistribution rights permit it. Tests MUST distinguish client and dedicated-server behavior where relevant.

Release acceptance MUST identify automated checks and unavoidable manual checks separately. A passing unit suite alone MUST NOT be considered evidence of L3 or higher compatibility.
