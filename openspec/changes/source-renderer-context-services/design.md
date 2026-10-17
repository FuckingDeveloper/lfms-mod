# Source renderer context service preservation


Runtime93 then reaches the source entity renderer's block-dispatcher context
getter. Both source entity and block-entity contexts expose getBlockRenderDispatcher;
target contexts carry a BlockModelResolver instead. The existing source-shaped
dispatcher facade MAY preserve that exact resolver from either context without
performing rendering early or taking a global Minecraft singleton. Source getter
calls and handles MUST use the same operation. Entity-context subclasses whose
virtual source getter could be bypassed MUST fail explicitly until override
semantics are supported. A missing resolver MUST fail, not produce a dummy.

This retains service intent only. Source immediate tessellation/getBlockModel
remain explicit unsupported operations; context linkage MUST NOT be reported as
block-in-entity rendering support. Unchanged context services keep their existing
native mappings; removed item services are not inferred from this block service.
