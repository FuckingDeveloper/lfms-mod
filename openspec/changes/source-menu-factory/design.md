# Source menu factory contract

Forge1.19.2 IContainerFactory accepts a nullable FriendlyByteBuf. Its vanilla
MenuSupplier entry passes null; IForgeMenuType.create retains the factory in a
MenuType. Source construction retains the existing VANILLA_SET feature policy
through the shared source constructor adapter. Native NeoForge26.3 uses
RegistryFriendlyByteBuf and its own default feature flags; the native menu-open path selects IContainerFactory and forwards its
registry-aware buffer. The registry-aware buffer is also a FriendlyByteBuf.

A source-shaped functional interface MUST retain the original three-argument
SAM and implement the native SAM by forwarding the exact same buffer, id and
inventory once. The two-argument source entry MUST pass null. Constructor lambdas,
explicit implementations, direct invocations and callable constants MUST retain
these types. LMS MUST NOT wrap, copy, consume, release or manufacture a buffer.

Source IForgeMenuType remains an interface for source implementations and casts.
The target MenuType integration MUST expose that source interface, retaining its
actual native factory. Source calls may use an ordinary FriendlyByteBuf with a
source factory; native-only registry-aware factories require the actual registry
buffer and MUST fail if source callers supply an unbound ordinary buffer. Vanilla
two-argument factories ignore extra data as in the source. A native-only call
continues to use native behavior. No menu registration or native registry is
mutated outside its normal lifecycle.

Static construction rules and source SAM members MUST be shared by admission and
transformation. Native opening-data packet delivery and screen rendering are
separate assertions; a factory test MUST NOT claim either from menu creation alone.

## Approved client-setup timing

Accepted by the user on 2026-10-17. This amends the former prohibition in
source-container-and-native-container-families/design.md for verified client
setup execution only.

Native MenuScreens.init posts RegisterMenuScreensEvent before source FMLClientSetup
deferred work runs. Source MenuScreens.register was valid in that work. Target
26.3 retains the exact private register/getConstructor methods and a mutable
screen map; there is no frozen-registry lifecycle behind this operation. Source
callbacks/work MUST retain their original native queue timing. During a verified
FMLClientSetup direct or deferred callback, later screen registrations MAY invoke
the retained native operation. Source provenance, exception/future identity and
nested thread-scope restoration MUST survive. Arbitrary post-setup registration
MUST still fail. A duplicate MUST preserve the original target factory. Earlier
intent still stages through the ordinary target event, including partial-failure
retention. No native event is reopened or fabricated; no registry is thawed.
