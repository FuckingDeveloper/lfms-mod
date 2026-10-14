# Native component JSON information

The removed source Component.Serializer static JSON information calls MAY project
to the actual target ComponentSerialization.CODEC for native component objects.
Serialization MUST retain component content/style/siblings using authoritative
registry ops when a reload context exists; context-free values MAY use JsonOps.
No dummy registry provider is supplied. Deserialization of this canonical target
JSON MUST produce a real mutable component preserving the same display semantics.
Null source information results remain null; malformed/unsupported codec values
MUST fail. Direct calls and handles share exact source owner/name/descriptor
selection. The native serializer is not blindly namespace-renamed.

Canonical target JSON may differ structurally from source JSON (including plain
string collapse). This bounded information contract does not promise source wire
format, byte-for-byte source JSON, legacy rich-event schemas or third-party Gson
serializer subclass/constructor semantics. Old clickEvent/hoverEvent input MUST
fail explicitly rather than lose these semantics through ignored fields. Lenient
and Brigadier reader parsing remain unsupported. Stable JSON uses the verified
native GsonHelper key-ordering operation. No original legacy artifact is edited.

Pinned source Serializer uses String.valueOf for non-component translation
arguments. Both encoding and decoding MUST normalize primitive component/with
arguments to their textual values, preserving `7` rather than a codec-induced
`7.0`. Input JSON trees MUST NOT be mutated by this projection.
