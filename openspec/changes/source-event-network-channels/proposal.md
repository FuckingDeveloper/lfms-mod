# Source raw event-network channels

JEI's remaining constructor-surface requirements are the Forge43.4.0
EventNetworkChannel family. Source events carry unprefixed original payload bytes
and context suppliers on the network thread. ClientCustomPayloadEvent means a
client-origin/server-received packet; ServerCustomPayloadEvent is the reverse.
They are not SimpleChannel discriminator messages.

Extend native play payload registration to an explicit raw-channel kind, retaining
channel identity, original protocol supplier/predicates, source owner, listener
selection/order, context identity, packet-handled state and explicit enqueueWork.
Native same-LMS version registration remains authoritative; arbitrary cross-version
predicate negotiation, login channels and legacy wire compatibility remain gaps.
Do not alias the event channel to an indexed codec or add a discriminator byte.

A received payload must own its bytes across native decoding and defer release
until source network delivery finishes; source enqueued work must follow the
original buffer lifetime rather than receiving a fabricated buffer. Unknown event
registration and changed packet constructors must fail explicitly. Native packet
send/receive and immutable source binary fixtures are required independently of
constructor admission before making a functional JEI network claim.
