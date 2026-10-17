# Complete timing-only recipe notification identity

Runtime125 reaches original JEI plugin callbacks but fails at its startup
observer's required-event class-token set. The existing migration only changes
unused handler parameters, leaving class literals and registration keys behind.
Pinned source RecipesUpdatedEvent and target RecipesReceivedEvent both notify
client main-bus listeners after server recipe data reception. Native data may be
partial, so payload equivalence is deliberately not claimed.

Extend the existing timing-only migration to a single projected type identity
across class tokens, signatures, descriptors, callbacks and callable constants.
Reject source payload access and source event construction/subclassing. Use the
real native event with no synthetic notification, dummy recipe manager or changed
legacy JAR. This preserves the existing notification semantics and keeps recipe
map/synchronization migration separate.
