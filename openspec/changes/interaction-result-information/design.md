# Source InteractionResult enum information over native outcomes

Forge 1.19.2 has five ordered constants: SUCCESS, CONSUME, CONSUME_PARTIAL, PASS,
FAIL. Target 26.3 uses sealed outcome records. A bounded information adapter MAY
retain source values order, cloned values arrays, exact names/valueOf failures,
ordinal branching, sided success and the three source behavioral queries while
keeping actual native outcome objects/payloads.

Native success with a swing is source SUCCESS; no swing with item interaction is
CONSUME; no swing without item interaction is CONSUME_PARTIAL. The latter uses
actual native CONSUME.withoutItem(): consumption is retained while item-stat
eligibility is suppressed. PASS/FAIL retain their native singleton identities.
TRY_WITH_EMPTY_HAND has changed control-flow semantics and MUST fail explicitly
when requested as a source category, rather than be guessed as PASS.

Exact original owner, operation, descriptor and invocation kind select queries
and callable constants. Other enum types MUST remain untouched. Compiler-generated
ordinal switch tables MUST receive a five-entry source view. This MUST NOT be
claimed as Java Enum representation compatibility: EnumMap/EnumSet, arbitrary
Enum-typed consumers, reflective enum metadata and general equality/hash behavior
of noncanonical native payload-bearing success records remain unsupported.
