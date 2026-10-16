# Proposed projectile simulation policy — accepted decision

Source constructor overloads MUST retain supplied type/level, coordinates and
owner. Default-item callbacks MUST NOT be invoked earlier than required by source
construction merely to satisfy a new native constructor. Item counts, source
custom data and source item fallback/sync intent MUST be preserved.

Proposal: native Minecraft26.3 flight, drag, collision ordering and deflection are
the base simulation. Proven source gravity and hit callbacks are adapted to the
corresponding target hooks, with their exact operands and virtual dispatch. Source
callbacks execute at native dispatch timing; source vanilla1.19.2 physics/tick
ordering is not emulated. A callback without a verified native counterpart MUST
fail explicitly. No mod IDs, content disabling, source JAR edits or swallowed
errors are permitted.

Registration/linkage alone does not establish flight, damage, item sync or
serialization. Independent source-shaped fixtures plus real entity instantiation,
flight/collision and save/reload evidence are required for those claims.

User accepted native Minecraft26.3 base simulation in this session.

## Bounded first implementation

Only managed source item-projectile subclasses receive the source-shaped target
superclass. Ordinary source type references/casts still project to the native
item-projectile base, so native vanilla subtypes remain assignable. The source
lazy empty synced stack is defined with the actual native item accessor; no early
getDefaultItem callback is introduced. Source setItem condition/count/copy and
raw/fallback getter behavior are retained. Source float gravity overrides widen
at the native double-gravity hook. Native tick/deflection dispatches hit callbacks.

Native persistence uses native projectile-parent storage, preserving owner and
flags, and stores a nonempty raw item only. Native codecs own the target format;
old-world migration is not claimed. Source custom no-argument synced-data or
CompoundTag persistence callbacks on these projectiles remain explicit rejected
boundaries until a general source entity-data adapter exists. A source float
gravity query on a native-only item projectile is also an explicit gap.
