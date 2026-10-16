# Source world rendering through target frame extraction

Status: option A approved by the user on 2026-10-16; implement against design.md.

## Evidence

Runtime69 fails when original world-overlay code reaches GUI-only disableTexture.
The pinned NeoForge26.3.0.58-beta RenderLevelStageEvent contract requires custom
state extraction in ExtractLevelRenderStateEvent, buffer uploads in
PrepareRenderBuffersEvent and drawing through the supplied active render pass.
SubmitCustomGeometryEvent likewise requires previously extracted state. Thus
adding a GUI scope around the existing late callback is not a valid world adapter.
Source1.19.2 RenderLevelStageEvent callbacks can create geometry while that stage
is drawing; source RenderSystem operations describe mutable immediate state.
The source integration overlay uses POSITION_COLOR TRIANGLES for a cube, and
related world consumers include outlines, caches and line/buffer operations.
Fixture names identify evidence only, never production selection rules.

## Recommended contract (A)

Invoke adapted source world-render callbacks once per supported source stage,
in source stage/priority order, during the target's extraction of that frame.
Supply genuine frame camera/frustum/partial-tick/renderer inputs through a
source-shaped event contract. Preserve explicit source local transforms and
record complete immutable geometry plus render state. Upload before the target
render pass and draw at the corresponding supported native stage in that same
frame. Retain no live world, player, callback or managed class in the frame's
submitted geometry. Native event subscribers keep their original native timing.

This deliberately changes when source callback code executes relative to world
drawing. It does not claim to preserve arbitrary side effects of source render
callbacks at their old chronological render instant. Preserve unsupported stage,
shader, texture, topology and state boundaries explicitly; do not skip listeners,
mutate GPU-global state or substitute GUI projection for world geometry.

## Alternative (B)

Keep source callback execution at its old drawing stage, but postpone newly
recorded geometry to a subsequent frame so buffers can be prepared legally.
This introduces visual latency and extra stale-frame/lifetime behavior. It is
not recommended and is not implicitly authorized by the proposed contract.

## Verification

Inspect all related original consumers and exact target pass/vertex/pipeline
contracts before choosing the proven initial primitive set. Independent fixtures
must cover callback priority/order/once-only delivery, failure/nesting restoration,
source/world versus GUI state isolation, immutable vertices/topology, transforms,
depth/blend/cull state, source buffer lifecycle and unsupported-operation failure.
Native FML checks precede actual MCP client interaction and user visual acceptance.
Original artifacts and MCP Fabric remain immutable. No Minecraft automation
framework or source identity-specific workaround is introduced.
