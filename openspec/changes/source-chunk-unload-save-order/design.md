# Source chunk unload notification before final persistence

Pinned Forge1.19.2 ChunkMap.lambda$scheduleUnload$13 bytecode calls setLoaded(false),
posts ChunkEvent.Unload, then save(chunk), then ServerLevel.unload. Pinned target
ChunkMap.lambda$scheduleUnload$0 calls setLoaded(false), save, posts native Unload,
then unload. Source dirty-mark listeners therefore run after the final save;
target markUnsaved additionally schedules eager persistence. Runtime66/67 lost
previously disk-confirmed generator EU; a vanilla control BE also disappeared.
This boundary is a generation lifecycle rule, not a source-content special case.

Within the actual target server unload transaction, source listeners accepting
ChunkEvent.Unload (including base-event listeners) MUST receive one notification
after setLoaded(false) and before the native final save, on the same thread with
the actual chunk/level and ordinary source owner/priority context. Source dirty
marks MUST be consumed by that final save. Native listeners MUST retain the
original target event timing and once-only delivery. Suppress a source callback
at the later native event only when the same transaction already delivered that
chunk; unrelated native/manual/client notifications MUST still be delivered.

Use the existing NeoForge event bus implementation for isolated source-phase
subscriptions, preserving listener order/priority and registration cleanup. Do
not post the earlier event on the global native bus. Source bus listener routing
must cover annotated subscribers, explicit typed listeners and base-event types.
Nested transactions and all failures MUST restore previous scoped state. Never
retain chunks/worlds in static collections. Native save and unload remain the
actual target operations; no custom persistence, snapshot replay or dirty-state
suppression may substitute for them. Unknown target callsites fail at injection.

Native event bus rejects abstract event registration. The verified ChunkEvent
base expands to Load/Unload and ChunkDataEvent.Load/Save; ChunkDataEvent expands
to its Load/Save pair. Source unload routing registers only concrete Unload on
the isolated phase bus. Broader unsupported abstract event ancestors remain
fail-fast rather than being silently narrowed to unload notifications.
