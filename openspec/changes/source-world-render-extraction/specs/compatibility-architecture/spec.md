# Source world rendering through extracted frame state

## Added requirements

For the approved Forge 1.19.2 -> target 26.3 world-render migration, LMS MUST
invoke source callbacks once per supported stage in source priority order during
extraction of the same frame. Native subscribers MUST retain native timing.
Source events MUST expose actual frame inputs; unproven inputs MUST fail
explicitly rather than substitute guessed values. The accepted timing difference
is described in ../../design.md.

Submitted geometry MUST contain copied vertices and render intent, without live
world, player, event or managed callback references. The target adapter MUST
upload before drawing and use the corresponding event's existing active pass.
Owned GPU buffers MUST close after that frame and on partial upload failure.
Unsupported stages, formats, shaders, state and persistent-cache draws MUST fail
explicitly. No legacy artifact may be modified or listener silently omitted.

Source immediate builder objects MAY traverse managed helpers and callable
signatures through a consistent source ABI. They MUST NOT be substituted for
unrelated target builder arguments. GUI and world extraction state MUST remain
separate and restore ownership on nesting and failure. Source subtype decisions
MUST use verified source declarations before semantic builder relocation.
