# Contract

Scope: Forge 1.19.2 source inspection/editing APIs on the pinned target 26.3.

Keep the native GameRules object authoritative. Project removed nested source
Key/Type/Value/Visitor types consistently through descriptors, signatures, handles
and constants. Source generic-then-typed visitor callbacks MUST execute once in
source order, exposing actual target rule identity, default and description IDs.
Default detached instances and static visitation use the native vanilla feature
set; experimental rules outside that set remain an explicit scope boundary.

Source value views MUST retain their native owner and definition. Boolean and
integer reads/writes MUST use genuine native get/set, including native change
notification when a server is supplied. Views MUST NOT own disconnected mutable
copies. Integer writes MUST validate the target definition's actual codec. Copies
MUST preserve the exact available rule set and values without aliasing state.

Source-shaped snapshots use string values with canonical target registry IDs and
native parsing; unknown IDs, types, categories or invalid values MUST fail
explicitly. This does not promise old world-save conversion. Native persistence
remains native. Static source named rule constants, source registrations and
custom category-to-enum projection remain unadapted rather than guessed.

No fixture identities, original JAR changes, skipped configs or fabricated values.
Verify native rule callbacks, type/owner identity, copy independence, snapshots,
transformed source visitor dispatch and actual configuration-screen opening.

Exact source MinecraftServer/ServerLevel getGameRules operations retain native
getter dispatch and live server ownership when their result type is relocated.
These source/target methods are verified from actual class declarations and bodies;
other world/client accessors remain separate unproven contracts.
