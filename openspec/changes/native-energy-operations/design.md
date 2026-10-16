# Native energy operations

The source block capability bridge currently rejects a present native EnergyHandler
before any operation because source canReceive/canExtract flags have no target
counterpart. Target26.3 EnergyHandler supplies authoritative amount/capacity and
transactional insert/extract. Presence does not establish permanent direction flags;
empty/full state must not be probed to invent those flags.

Expose only proven source operations: nonnegative receive/extract requests map to
target insert/extract in root transactions, aborting simulation and committing real
calls after validating the returned range. Keep matching sided BlockCapabilityCache
and source optional invalidation. Every view operation checks lifetime validity.
Long amounts/capacities that cannot be represented by a source int fail explicitly.
Source canReceive/canExtract still fail explicitly when invoked, including at full
or empty storage; no state probing, guessed booleans or fabricated metadata.
Nested target transactions remain unsupported, as for the existing item adapter.
Source-owned capability overrides still take precedence with their original flags.

This is an operation-level extension of the existing capability policy, not full
native energy compatibility. Current reference EU network is independent from
Forge Energy. Tests use actual target SimpleEnergyHandler and transaction journals;
no compatible native energy provider is installed in the gameplay fixture set, so
successful IC2 EU production cannot prove this native-provider interop.

Full wrapper checks passed in batch-check-11. Actual target SimpleEnergyHandler
checks cover receive/extract limits, aborted simulation with no commit callback,
committed callbacks/counts, zero/negative requests, invalidation for all six source
operations, overflow, bad-provider result rollback, explicit flag failures at
both empty/full storage, and rejection of nested root transactions. No installed
native provider or multiplayer energy-transfer gameplay is inferred.
