# Source configuration registration family

## Problem

Immutable source mods use ForgeConfigSpec construction and ModLoadingContext
registration. Inspection bridges alone neither register their files nor deliver
owner-correct load/reload/unload callbacks. Descriptor projection cannot preserve
source value-cache semantics or the changed SERVER storage policy.

## Change

Use the existing method catalog for proven source builder/value/context members
and both registration overloads. The installed target adapter uses the native
ConfigTracker backend, preserves source provenance and isolates source callbacks.
The accepted SERVER decision adopts native SYNCED storage/override/synchronization.
No legacy JAR changes or reference-mod selectors are permitted.

## Verification

Pure shared-admission checks, source-shaped executable binaries and the real
native file loader verify values/files/events/ownership. A fresh Gradle client and
MCP verify actual initialization and content separately; unresolved neighboring
families stay explicit blockers.
