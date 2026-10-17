# Source texture-bearing render factory contract

The pinned Forge43.4.0 named RenderType bytecode and paired source SRG/Mojang
mappings establish the following source factories. Actual target26.3.0.58-beta
RenderTypes/RenderPipelines establish their native operations:

| Source SRG | Source operation | Native target operation |
| --- | --- | --- |
| m_110431_ | armorCutoutNoCull | armorCutoutNoCull |
| m_110452_ | entityCutout (culling enabled) | entityCutoutCull |
| m_110443_ / m_110458_ | entityCutoutNoCull(bool/default) | entityCutout(bool/default) |
| m_110448_ / m_110464_ | entityCutoutNoCullZOffset(bool/default) | entityCutoutZOffset(bool/default) |
| m_110454_ / m_110473_ | entityTranslucent(bool/default) | entityTranslucent(bool/default) |

The adapter MUST preserve the exact texture and affects-outline flag; each source
one-argument default above is true. It MUST distinguish culling from no-culling
and view-offset layering from ordinary cutout. These factories use source NEW_ENTITY
quads, lightmap/overlay, source alpha-cutout threshold 0.1, normal depth/write
semantics and their declared blending. Native extraction/submission, shaders and
render phase remain authoritative under the existing rendering contracts. The
armor pipeline retains its native armor shader and view-offset layering.

Source intent MUST remain source-shaped; only target resolution selects a native
pipeline. Direct calls, method references and nested callable constants MUST
retain source return/argument identity. Unsupported factories (including unproven
emissive boolean variants) and custom/chunk standalone pipelines MUST NOT receive
an approximate fallback. Native factory identity is not proof of model geometry,
entity animation, texture loading or a successful frame.

Evidence: pinned source RenderType lambda state builders show culling enabled by
default for entityCutout, explicit NO_CULL for both no-cull variants, and
VIEW_OFFSET_Z_LAYERING only for the offset and armor variants. Target RenderTypes
and RenderPipelines retain those distinctions; similarly named entityCutout now
selects a pipeline with withCull(false), so a same-name mapping was incorrect.
