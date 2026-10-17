# Resource graph and scoped context

Resource visibility MUST follow the same admitted artifact/dependency graph as
class visibility. Source resources MUST enumerate owner, declared dependencies
and native parent deterministically, deduplicating identical URLs. Missing resources
MAY use the ordinary ClassLoader null/empty result. Resource reads MUST use the
original immutable JAR bytes and MUST NOT bypass managed class transformation for
class definitions. Closed-loader resource reads MUST fail explicitly. URL handling
SHOULD avoid global JAR caches retaining disposable managed loaders.

Production source initialization MUST record its genuine managed classloader.
Source lifecycle, deferred and cross-owner event scopes MUST install that owner's
thread context loader and restore the previous loader on all exits, including
failure. Same-owner re-entry MUST retain active context identity. Bindings MUST be
released when their owning runtime session closes. Scopes without a managed loader
MUST preserve their incoming context loader. Existing owner/thread/order checks
remain authoritative. No source ServiceLoader call is rewritten and no provider is
fabricated. Out-of-scope threads and projected service descriptor names remain
unverified boundaries.
