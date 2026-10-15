# Exact holder-backed source sound fields

Runtime41 no longer fails in tool highlighting. AdvancedHoe.useOn then fails on
SoundEvents.f_11955_ (source canonical HOE_TILL): the pinned target declares
Holder.Reference<SoundEvent>, while the existing adapter only checked a Holder
field descriptor. Those are distinct JVM field ABIs.

Extend the existing source-owner/member/SRG identity resolution with the exact
Holder.Reference descriptor fallback. Unwrap via native Holder.value and retain
the original SoundEvent object. Do not infer fields from descriptor coincidence,
rename constants heuristically or change registry binding. Direct source field
reads are admitted; arbitrary field handles remain outside this existing subset.
