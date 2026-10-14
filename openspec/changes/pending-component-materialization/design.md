# Isolated per-holder component materialization

Early source construction must not bind frozen/global holders prematurely. The
actual target DataComponentInitializers builds each registered value from its
ordered initializer entries, then applies native item component modifiers. An
element with no initializer receives the actual target empty-component policy.

The target adapter MAY materialize one holder by executing its matching entries
in their native order with the authoritative provider and a fresh builder. It
MUST verify that provider resolves the same registered value. It MUST NOT run
initializers for unrelated holders to construct one stack. It MUST NOT publish
this isolated component map to the global holder. Initializer entries must be
read from the exact target ABI; private target access failures are explicit.
No new cache may retain source loaders or disposable lookup providers.

The source early ItemStack path keeps its existing source-properties handling
and active binding snapshot. Its outside-binding native prototype path now uses
this target service. The verified two-argument Forge FluidStack constructor MAY
use the same target service and actual isolated native constructor. Bound holders
still use ordinary native constructors. Unknown constructor shapes/control-flow
allocation flows remain unsupported. Nested NEW/DUP/constructor pairs MUST be
preserved and the traversal must resume from attached argument nodes.

This change does not claim all old FluidStack metadata, native FluidResource
ownership, third-party fluid serializers or source custom subclasses are migrated.
