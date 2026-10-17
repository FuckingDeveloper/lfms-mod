# Source entity data initialization through native builders

Source Entity construction invokes defineSynchedData():void after allocating its
mutable SynchedEntityData. Native construction invokes defineSynchedData(Builder)
before assigning the built data. A source override MUST run at that native hook,
preserving superclass order and each definition value evaluation exactly once.

The first admitted shape is statically proven definition-only use of this.entityData
inside that callback. Its field receiver MUST be the original this, its source
field declaration MUST be Entity.entityData, and every such read MUST flow directly
and uniquely to SynchedEntityData.define(accessor,value). Those operands become
native builder definitions, preserving native duplicate/missing/serializer checks.
An explicit superclass callback MUST receive the same builder. Existing local
variables/annotations/frames MUST be adjusted for the added argument.

Escaped/cached data, reads/mutations during construction, callbacks passed as
callables or called outside the hook, and hidden coincident fields MUST NOT be
silently treated as builder definitions. Reject unsupported callback shapes before
definition. Normal post-construction get/set keep using real native synced data.
Native storage, ID allocation, synchronization and dirty flags remain native-owned.

This does not adapt entity persistence or private native hooks. In particular,
The pinned NeoForge target retains PrimedTnt's protected virtual explode hook;
verify existing generic override projection rather than introducing a second dispatcher.
Fixing construction alone MUST NOT claim mod-specific blast behavior is working.
No JAR modification or fixture identity selection is allowed.

Verify independent parent/child source callbacks, native inherited definitions,
original values/once-only callbacks, local indices, duplicate and negative shapes,
then actual entity creation in a loaded world. Custom item-projectile callbacks
remain rejected by their existing contract pending explicit integration.
