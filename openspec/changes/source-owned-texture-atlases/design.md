# Client field and atlas contracts

Visibility restoration MUST require pinned source public-final-instance evidence
and exact retained native owner/name/descriptor/final-instance evidence. Access
restoration MUST NOT change field mutability, construction or getter dispatch.
The generated family is limited to Minecraft client service fields.

A source-owned atlas MUST remain a real native TextureAtlas registered under the
source ID with the provided TextureManager. Source getResourcesToLoad executes
once per preparation and source getSprite resolves the original prefix. Resource
preparation MUST consume the active ResourceManager, native image metadata,
missing-sprite rules and stitching. The native reload barrier and upload executor
MUST remain authoritative. Apply MUST await actual mip preparation before upload.
Close MUST retain the source clearTextureData operation; TextureManager retains
its native texture ownership. No legacy JAR or external pack is modified.

The target adapter MAY expose a reload-local ResourceManager overlay containing
only its derived atlas definition. All source image/metadata reads MUST delegate
to the genuine manager. Source callbacks/direct calls/handles and generic erased
prepare/apply overrides MUST keep dispatch and superclass behavior. Custom source
TextureAtlas.Preparations construction and obsolete low-level atlas mutation
remain unsupported; this family does not promise general legacy render support.
