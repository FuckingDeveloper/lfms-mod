# Source game-event identities and native delivery

Runtime108 block ignition reached its original event operation, then failed on a
raw GameEvent static field. The target exposes typed holders instead of raw
values. Source constant reads MUST unwrap the actual native registered holder;
source delivery MUST wrap that exact value with its native registry holder.
Names/mappings alone are insufficient: target field owner, static access,
Holder<GameEvent> signature and descriptor MUST be verified from target metadata.
Callable constant reads MUST retain their source signatures and identities.

All four LevelAccessor and two Entity delivery overloads MUST preserve actor,
position/context, native virtual dispatch, once-only delivery and exceptions.
BlockPos defaults retain center-of-block projection. A source override of these
changed raw-value callbacks MUST fail explicitly until its override contract is
implemented. No callback may be silently bypassed. Native holder callbacks remain
native. No registry mutation or fabricated event/actor is permitted.

Custom source event construction/registration and removed source name getters
remain separate boundaries. Normal registered event dispatch uses native timing,
listener lookup and propagation. This rule contains no fixture identity selector.
Verify independent source calls/handles and native listener-entry operands before
runtime ignition. Custom entity rendering remains an independent runtime blocker.
