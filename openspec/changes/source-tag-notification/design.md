# Proposed source view and connection-scoped client delivery

User approved source post-login delivery. Runtime acceptance remains pending.

A source-owned event view MUST retain the source's one concrete class identity,
getRegistryAccess, UpdateCause (SERVER_DATA_LOAD / CLIENT_PACKET_RECEIVED),
shouldUpdateStaticData and supported source constructor semantics. It MUST wrap
real target registry publication facts and remain outside the target sealed event
hierarchy. The source bus bridge MUST translate listener type/dispatch once with
original priority/ownership/unregister behavior. Unknown source subclasses MUST
remain fail-fast until their identity/dispatch semantics are established.

For the proposed source readiness policy, target-side integration MUST retain
at most the current pending client configuration publication for its actual
connection. It MUST dispatch the source view only after the corresponding native login packet handler (including its
LoggingIn dispatch) completes, with current client player/registries usable. It
MUST not post the view on the native bus or reschedule native components/tags.
A closed/failed/replaced connection must discard its pending state. Reload and
reconfiguration must not reuse another connection's publication or duplicate a
single source notification. Dedicated-server classloading must not resolve client
classes to register common source tag callbacks.

Verification must independently cover source class-token/getClass identity,
registry/cause/static-data values, source static and instance/explicit listener
shapes, priority/owner/unregister, real native subclasses, login/config ordering,
connection replacement/disconnect cleanup, reload, integrated static-data flags,
and exact once-only source delivery. Then repeat real JEI startup and test-world
readiness; reaching a world is not proof of recipe UI or plugin functionality.

## Implemented boundary

The target wraps the exact native configuration/play tag-publication calls and
preserves their original bus post/result. Server source listeners wrap the genuine
ServerDataLoad at their own native priority. Client source listeners use a source
bus with the same recorded owner/priority/unregister behavior. Source explicit
posting stays on that source bus, without native sealed-event impersonation.
A weak native-event/source-view cache preserves one view across server listeners.

Initial client delivery occurs at RETURN of the native login packet handler,
after player insertion, camera setup and complete native LoggingIn dispatch. The
single active-connection publication gate uses identity keys and weak retirement
tombstones, preventing late publications from a replaced connection reclaiming
state. It does not retain disposed connection keys in its retirement index.
Connection disconnect and closed-channel handling clear pending state. No source
callback runs under the gate lock. Source subclassing remains fail-fast; generic
catch-all Event/reflection consumers outside explicit source tag registrations
are not established by this family.
