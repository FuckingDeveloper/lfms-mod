# Source manager playback over native audio

Runtime104 GUI selection logged the removed SoundManager.play(SoundInstance):void.
Target SoundManager.play returns SoundEngine.PlayResult while still delegating
exactly once to the sound engine. Source void callers cannot observe a status;
LMS MAY discard only the new result after the real playback call. It MUST NOT
remove playback, invent a successful result or swallow backend/callback errors.

The proven sibling pause() pauses every source channel. Native pauseAllExcept with
an empty exclusion array expresses that same operation. Source getDebugString()
reads actual engine channel diagnostics, now exposed as getChannelDebugString().
These three operations MAY use native virtual dispatch, with original managed
source callbacks preserved. Explicit source SoundManager super calls MUST execute
the actual native base implementation without redispatching source overrides.
Callable constants MUST retain source signatures and receiver types. Exact root
mapping plus descriptor/declaration proof is required; unrelated members stay put.

Source callback discovery belongs to the profile; use ClassValue and exact public
method signatures/source-managed declaring-loader evidence. Native implementation
and new result type belong to the installed target adapter. The shared composition
class MUST expose only its service identity, without client-only class types in
method signatures. Client service ABI handles are resolved lazily by the client
bridge and MUST match the installed target contract exactly. No global map may
retain disposable managed loaders. Source artifacts remain immutable inputs.

playDelayed/queueTickingSound/stop/isActive retain their independently inspected
native ABI and are not changed by this family. ResourceManager constructor,
reload preparation and category-gain policy are separate boundaries: source
updateSourceVolume MASTER<=0 explicitly stops all, whereas native category gains
have changed state semantics; do not blindly rename that operation.

Evidence: exact source SoundManager bytecode, target manager/engine source,
PlayResult enum and pauseAllExcept channel loop. Native smoke must cover every
PlayResult, once-only operands, source override/super order, native override vs
base dispatch, pause exclusion emptiness, real debug return and callback failure.
