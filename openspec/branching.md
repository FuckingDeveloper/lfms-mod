# Branching and Versioning

## Permanent branches

- `master`: stable released code only.
- `develop`: primary integration and development branch.

Normal work branches from `develop` and returns through review.

Recommended prefixes:
- `feature/*`
- `fix/*`
- `spec/*`

## Release flow

When the first working prototype satisfies the 01.000.00 release acceptance criteria:

```text
develop
   |
   +--> release/01.000.00
             |
             +--> stabilization and release blockers only
             |
             +--> master
             |      |
             |      +--> tag 01.000.00
             |
             +--> develop
```

Release fixes MUST be merged back to `develop`.

Urgent released fixes MAY use `hotfix/MM.mmm.pp` from `master` and MUST return to both `master` and `develop`.

## Version format

LFMS uses:

```text
MM.mmm.pp
```

- `MM`: major architecture/API generation.
- `mmm`: feature/minor release.
- `pp`: patch release.

Examples: `01.000.00`, `01.001.00`, `01.001.01`, `02.000.00`.
