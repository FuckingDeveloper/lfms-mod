# Compatibility Runtime Specification

## Requirements

- Runtime bridges MUST represent semantic compatibility that cannot be safely reduced to static rewriting.
- Core runtime bridges MUST be mod-agnostic.
- Mod-specific behavior MUST live outside the core runtime.
- Runtime adaptation MUST expose diagnostics for unsupported operations where practical.
- Compatibility APIs SHOULD minimize per-call overhead when equivalent behavior can be established during transformation/bootstrap.
