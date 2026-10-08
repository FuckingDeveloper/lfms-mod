# Design: Bootstrap Legacy Discovery

## Components

- `bootstrap`: target-native LMS initialization.
- `discovery`: artifact enumeration and metadata inspection.
- `model`: immutable normalized descriptors/evidence.
- `profile`: resolver and profile contracts.
- `profiles/forge1192`: first version-profile implementation.
- `diagnostics`: structured compatibility reporting.

## Discovery boundary

Discovery reads candidate JAR metadata and selected class/resource markers. It does not rewrite classes and does not load legacy entrypoints into the target classloader.

## Profile resolution

Profiles evaluate normalized evidence rather than directly probing arbitrary JARs. Resolution returns a supported profile only when evidence satisfies that profile's declared rules.

## Future transformer boundary

The later transformer consumes a descriptor plus a resolved profile. This keeps JAR detection independent from bytecode rewriting and allows discovery tests without launching transformed legacy code.
