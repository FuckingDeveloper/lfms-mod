# Raw play-channel delivery

Source EventNetworkChannel MUST be a distinct channel kind. It MUST share source
channel identity/duplicate detection and native registration timing with existing
SimpleChannel adapters, without adding an indexed discriminator. Typed source
lambdas MAY supply the verified concrete NetworkEvent class to the real private
native event bus. Erased/unknown registration shapes MUST fail explicitly.

Both native play directions MUST register NETWORK handlers. Source event class
names indicate origin: ClientCustomPayloadEvent is server-received. Delivery MUST
retain one event, one buffer and one context supplier across synchronous listeners,
source ownership, handled flag and explicit enqueueWork semantics. Decoded bytes
MUST remain owned until synchronous delivery ends; the delivery buffer MUST then
be released even on listener failure. Deferred work MUST restore source ownership
and MUST NOT fabricate a longer payload lifetime. Source unsafe outgoing buffers
MUST be read at encoding time without changing their reader index.

Cross-owner listener registration, unhandled native fallback, login events, source
packet construction and arbitrary legacy wire negotiation remain explicit gaps.
Source registration predicates MUST remain captured and own-version validated;
native negotiation remains authoritative. No remote-network functionality claim
may follow from constructor admission, codec tests or integrated-server startup.
