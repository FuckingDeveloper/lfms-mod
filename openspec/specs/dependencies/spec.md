# Legacy Dependency Specification

## Requirements

LMS MUST model dependencies between managed legacy artifacts.

A dependency MAY be required or optional and SHOULD retain available version constraints.

Before executing a managed legacy mod, LMS MUST identify missing required dependencies that can be determined from artifact metadata.

Optional missing dependencies MUST NOT fail the entire compatibility pipeline unless the legacy mod itself requires that integration at runtime.

LMS MUST detect dependency cycles where ordering cannot be resolved safely and report the involved artifacts.

The dependency graph MUST be available to profile resolution, transformation planning, classloading and diagnostics.

Target-native dependencies are not automatically legacy-managed artifacts and MUST be distinguished from dependencies expected to be transformed by LMS.
