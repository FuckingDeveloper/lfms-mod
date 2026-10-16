# Source analog-output callback

Runtime76 native comparator reports0 beside a full source generator. Pinned
source BlockBehaviour.getAnalogOutputSignal(BlockState,Level,BlockPos) is the
source comparator contract (SRG m_6782_). Target26.3 adds Direction and invokes
only that four-argument entry. Source three-argument overrides are not native
overrides. Source IC2 delegates its callback to a custom helper with null side;
that helper is not an API contract and MUST NOT select production adaptation.

For a verified source Block descendant declaring the exact source override,
generate a native four-argument entry that invokes the original source callback
virtually once with the same state, level and position and returns its result.
The extra native direction MUST NOT alter the original directionless callback.
Preserve original methods, source super dispatch and inherited virtual override
behavior. Static/abstract declarations or a conflicting target entry MUST fail
explicitly. Source direct calls and handles retain the original three-argument
method; this does not declare removed native three-argument superclass APIs
generally supported. Unknown boundaries remain explicit.

No mod-specific helper, comparator formula, inventory or packet policy changes.
Verify native block-state entry dispatch with independent inherited fixtures,
original immutable consumer transformation and live full/partial/empty output.
