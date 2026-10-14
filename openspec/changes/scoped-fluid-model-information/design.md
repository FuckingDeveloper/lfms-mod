# Pending native fluid-model information

On the pinned target, ModelManager bakes fluid models before ModifyBakingResult,
then applies them after the preparation barrier. During that callback, source
still/flowing texture information without a source client extension MUST resolve
against that exact pending map, never the previous or uninitialized global model
set. The target adapter scopes the map around the actual callback, restores it
on exceptions, and rejects missing models. Source-defined accessors retain priority.
Outside that callback the ordinary applied target model lookup remains authoritative.
This does not establish fluid tint/rendering or arbitrary resource reload compatibility.
