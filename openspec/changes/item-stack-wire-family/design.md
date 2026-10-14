# Paired legacy item-stack and NBT buffer operations

The source Forge 1.19.2 buffer item protocol is presence boolean, synchronized
registry raw-id VarInt, signed count byte and nullable compound NBT. Its NBT has
an empty root name (type/name/payload), unlike target network NBT's type/payload
format. Both source directions MUST preserve source framing, including null and
empty sentinels. The native component stream codec MUST NOT replace this protocol.
This is for peers executing the source protocol against the target synchronized
registries; it is not native item codec interoperability or a historical remote
Minecraft client's registry-id compatibility claim.

Direct source calls, handles and nested constants use exact pinned source
owner/member/descriptors. Source writeItem delegates to limited writeItemStack.
The source virtual share-tag, read-share-tag, multiplayer override and damageable
callbacks MUST retain source ownership and return values when declared. Source
byte indexes and precise MethodHandles identify these callbacks without eager
reflection over unrelated member signatures. ClassValue metadata follows the
class lifetime, never a new permanent loader cache.

Default source tags project proven native damage, enchantments, stored enchantments,
custom name, repair cost, unbreakable and source custom data. Decode restores those
actual native components and retains source tag data. Unknown component patches
MUST fail before writing. Custom share callbacks combined with native-component
projection remain an explicit unsupported boundary, not discarded callbacks.
Enchantment decode requires authoritative registry holders. Source root NBT
uses actual target NbtIo named-root operations and a bounded native accounter,
not a fabricated registry or a newly invented serialization framework.

Broader capability/share-tag policies, native/source custom buffer overrides,
all metadata schemas and third-party serialization are not inferred from this
bounded paired protocol. Original legacy JARs remain immutable.

## Explicit registry tokens and fluid packets

Source full registry ids prepend the actual registry identifier to the raw-id
VarInt. Both full and unsafe pairs MUST be selected before source IForgeRegistry
is projected to its verifier marker; descriptor projection must not make a
verified adapter unreachable. Read MUST resolve an actual registered registry,
never a placeholder. Primitive fluid packets MAY preserve registry id, amount
VarInt and nullable source NBT for untagged stacks. Unknown fluid component patches
or nonnull source mutable fluid metadata MUST fail explicitly. Native fluid
component codec replacement is forbidden. This bounded protocol does not claim
native fluid metadata/transfer ownership migration.

The inherited IForgeFriendlyByteBuf writeFluidStack/readFluidStack wrappers are
not identical to raw FluidStack.writeToPacket/readFromPacket: they prefix a
presence boolean and use the actual EMPTY singleton when false. That discriminator
MUST be preserved in both directions. Evidence is the pinned source default
interface body, not the consuming fixture's method names. Source interface-typed
casts/default-super invocations remain a separate unimplemented surface.
