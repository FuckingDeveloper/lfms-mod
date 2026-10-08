# Mod-Specific Compatibility Specification

## Purpose

Some legacy mods may require behavior that cannot be generalized safely. LMS permits optional mod-specific compatibility modules without contaminating core or version profiles.

## Requirements

- Mod-specific modules MUST be separate from LMS core.
- They MUST identify the exact mod/mod-version range they target.
- They MUST declare which generic limitation necessitates the module.
- Generic fixes SHOULD be preferred whenever behavior is reusable.
- A mod-specific rule MUST NOT silently affect unrelated artifacts.
- Diagnostics MUST identify when a mod-specific compatibility module or rule was applied.

A conceptual location is `compat/mods/<mod-id>/`; exact source layout may be refined by an implementation change.
