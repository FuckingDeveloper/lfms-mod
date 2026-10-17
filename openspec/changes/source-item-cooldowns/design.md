# Source item-keyed cooldown family

Source Forge 1.19.2 ItemCooldowns keys state by registered Item identity; target
26.3 keys it by Identifier and normally derives that key from a stack component.
For proven source base declarations, LMS MAY translate add/remove/query/percentage
operations to the native manager with the registered item's identifier. Tick,
expiry, replacement, interpolation and server notifications MUST remain native.
Native query operands MUST resolve to that same identifier; a conflicting default
USE_COOLDOWN group MUST fail explicitly rather than silently merge source items.
Unregistered items and custom manager subclasses/overrides remain unsupported.
No per-mod branches, synthetic global state or rewritten source artifacts.

Mapping owner/member identity plus descriptor MUST establish source declarations.
Direct invokes and callable constants use the existing semantic invocation pass;
custom declarations and source super calls remain unadapted until virtual hook
semantics are proven. Native base and exact ServerItemCooldowns are admitted;
custom native subclasses fail before mutation. Source zero/negative durations
retain native expiry behavior. Client and server each use their actual manager.

Evidence: source 1.19.2 ItemCooldowns bytecode, exact target ItemCooldowns and
ServerItemCooldowns sources. Runtime99 first failure: removed addCooldown(Item,int)
called before the original DynamiteItem creates its projectile.
