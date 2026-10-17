# Source model material dependencies in native atlas loading

Source Forge1.19.2 ModelBakery collects UnbakedModel.getMaterials before stitching.
Native26.3 atlases use explicit sprite sources; their default block/item directory
sources do not discover arbitrary source textures/blocks or other model directories.
Runtime116 loaded the world but reported missing existing PNGs referenced by source
model texture slots. No source or target asset is renamed by this migration.

For source model JSON top-level texture slots backed by an actual PNG in the same
immutable admitted artifact, LMS MUST retain the exact sprite/resource Identifier
and contribute a native SingleFile source to the block atlas. Slot indirections
MUST NOT become resource identifiers. Unreferenced PNGs MUST NOT be indiscriminately
stitched. External-resource-only dependencies and custom-loader material APIs are
outside this bounded index; source TextureStitch callbacks remain independently
preserved. Non-string/unproven slot schemas MUST fail with artifact/resource evidence.

The generated atlas additions MUST participate in the ordinary native resource
manager, image/metadata loading, stitching and resource override lifecycle. They
MUST NOT modify source JARs, textures or vanilla atlas definitions on disk. The
index MUST be computed once for the captured admitted artifact set per pack and
MUST NOT retain managed classloaders. Only the block atlas receives these standard
model dependencies; other atlases retain their actual source callbacks.

Independent artifact selection/immutability and native SingleFile codec checks
precede runtime missing-texture diagnostics. No complete custom-material or pixel
correctness claim follows from having an atlas definition alone.
