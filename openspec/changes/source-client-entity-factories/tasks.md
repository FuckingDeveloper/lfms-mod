- [x] Inspect source builder/SpawnEntity/NetworkHooks and actual native packet creation/publication.
- [x] Preserve factory intent on actual native builder/type objects without global loader-retaining maps.
- [x] Invoke original source factory at native packet construction with real packet/level.
- [x] Preserve original source spawn-packet override under real ServerEntity scope.
- [x] Verify native packet view, source-shaped factory capture/bind, result/null/failure/owner and native fallback; no fabricated extra-data buffer.
- [x] Compile and pass compatibilityTest/nativeCompatibilitySmoke/compileLaunchJava.
- [ ] Inspect transformed audit and fresh runtime registration/world/client factory acceptance.
- [ ] Verify actual source seating interaction/publication; source entity persistence is separate.

Runtime117 ordinary floor-chair use reached actual source entity creation and
tracking, then failed because the shared invocation walker skipped the source
static NetworkHooks.getEntitySpawningPacket call. Static/handle/constant traversal
is now covered by both the shared utility fixture and exact source-hook fixture.
Checks passed in /tmp/lms-static-invocation-check.log. Runtime118 passed the
previous packet-hook blocker and reached source startRiding(Entity,boolean),
which requires the separate approved riding-family adaptation. Actual seating
and client tracking remain unverified until that path completes.
