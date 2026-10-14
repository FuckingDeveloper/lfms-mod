# Instance-owned immutable reverse symbol indexes

Mapping reverse method and field queries MUST use indexes owned by the immutable
mapping dataset. They MUST NOT hash the full dataset on every query or retain it
in a static object-keyed cache. The four-argument construction and named accessors
remain available. Candidate order, duplicate method removal, overload ambiguity,
owner normalization and missing-result semantics retain their previous contracts.
This is a mapping implementation correction, not a change in symbol identity.
