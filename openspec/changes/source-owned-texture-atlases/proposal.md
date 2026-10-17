# Retain source-owned texture atlases over native resource loading

Runtime119 admitted and initialized JEI, Wick and Fetzi, then failed on a source
public immutable Minecraft textureManager field whose exact native counterpart
is private. The following source TextureAtlasHolder superclass was removed too.
These are general source client-service and owned-atlas families.

Restore only pinned source-public final instance fields with identical native
owner/name/descriptor and final instance storage. Do not remove final or infer
renamed services by type. Keep source-owned atlas identity, TextureManager
registration, resource enumeration, prefix and sprite lookup callbacks, reload
barrier, native upload and close ownership. Use native26.3 sprite loading/stitching;
do not merge independent source atlases into an unrelated global atlas.

Derived reload-local native single-file source definitions MAY express the exact
source enumeration to the public native loader without editing input resources,
mutating global atlas registries or invoking private stitching operations.
Unsupported custom preparation data or specialized atlas mutation remains explicit.
