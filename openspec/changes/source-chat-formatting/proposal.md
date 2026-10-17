# Source formatting metadata family

Runtime123 passes the previous original JEI empty-plugin blocker and IC2 logs its
JEI plugin. Original JEI configuration serialization then fails at source
ChatFormatting.isColor (`m_126664_`). The native enum retains code/string/native
lookup but removed source metadata predicates, names, IDs and color values.
Restore the proven public query/lookup family with explicit source member rules
and native named TextColor values. No JEI-specific selector or source input edit.
