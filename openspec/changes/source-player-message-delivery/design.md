# Source player chat/action-bar delivery

Runtime68 EU reader triggers Player.m_5661_(Component,boolean) linkage failure.
Pinned1.19.2 mappings identify displayClientMessage. Player's source base body is
empty; ServerPlayer delegates to virtual sendSystemMessage(Component,boolean).
LocalPlayer delegates to source ChatListener.handleSystemMessage whose boolean
selects overlay. Target Player separates sendSystemMessage/sendOverlayMessage;
target ChatListener.handleSystemMessage boolean now means remote provenance.

Verified source virtual calls/handles MUST preserve actual managed overrides,
message identity and overlay boolean. Source-super calls MUST preserve the
actual declared Player/ServerPlayer/LocalPlayer default separately. Server
messages MUST use genuine native server delivery (including filtering/network),
retaining virtual two-argument sendSystemMessage. Client chat MUST be native
local system chat (remote=false); overlay MUST use native handleOverlay. Never
pass the source overlay boolean as target remote provenance. Source base Player
empty behavior is retained only with the inspected empty-body evidence. Unknown
owner/descriptor shapes remain unresolved, with no fixture-name dispatch.
Class-owned callback caches and client-only helper linkage preserve loader/side
lifetimes; no fabricated message packet or skipped source callback is allowed.
