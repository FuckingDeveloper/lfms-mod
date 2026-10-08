# Proposal: Bootstrap Legacy Discovery

## Why

LFMS cannot provide compatibility until it can run natively on the target platform, find legacy artifacts and determine which compatibility profile should handle them.

## Change

Introduce the first executable LFMS foundation:
1. native target bootstrap;
2. non-destructive legacy JAR discovery;
3. normalized legacy mod descriptors;
4. profile resolution infrastructure;
5. Forge 1.19.2 profile identification;
6. structured diagnostics.

Bytecode compatibility itself is intentionally deferred to a subsequent change after discovery/profile selection is observable and testable.

## Success

An original Forge 1.19.2 candidate JAR can be placed in the configured legacy location and LFMS reports it, its evidence, and the selected Forge 1.19.2 profile without altering the JAR.
