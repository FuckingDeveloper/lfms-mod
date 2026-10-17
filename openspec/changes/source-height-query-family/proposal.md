# Preserve source world-height query semantics

Runtime102 reached the original terrain tick job and failed on the inherited
ServerLevel.getMinBuildHeight query. Adapt the complete proven native-backed
LevelHeightAccessor query family, retaining exclusive source upper bounds and
current target dimensions. This is a generation rule, not a TNT adapter.
