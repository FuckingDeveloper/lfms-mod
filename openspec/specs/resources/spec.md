# Resource and Data Compatibility Specification

Legacy compatibility includes non-class content where required: metadata, language resources, models, textures, recipes, tags or legacy tag-like systems, data files and other loader/game resources.

Resource transformation MUST be separated conceptually from arbitrary bytecode transformation. Original resource bytes MUST remain unchanged in the source artifact.

A profile MUST be able to declare resource/data transformations required by its legacy generation. Unsupported resource formats that affect declared functionality MUST be represented in diagnostics/capabilities.

Resource transformations MUST participate in transformation cache identity.
