# Height family verification

- [x] Inspect runtime102 first causal getter failure and all related source queries.
- [x] Compare pinned source interface bytecode/mappings with exact native interface.
- [x] Implement eleven query overloads plus create factory; retain exclusive source
  maxima and native dimensions; inherited owners and callable forms are covered.
- [x] Keep custom source height callbacks explicitly unsupported at consumption.
- [x] Native probe covers six ranges including negative/unaligned minY, zero height
  and integer overflow, endpoints/indices, inherited ServerLevel owner, foreign
  owner, custom callback guard, source artifact immutability and callable forms.
- [x] `compatibilityTest nativeCompatibilitySmoke compileLaunchJava` passed
  (`/tmp/lms-height-query-check.log`).
- [x] Selected 4323-class transformed-linkage audit passed
  (`/tmp/lms-height-query-audit.log` and `.tsv`); the prior height findings are absent.
- [x] Runtime103 loaded the explicit test world and MCP client/server gates passed.
  No saved projectile/job remained, so loading alone did not exercise terrain.
  Original player position/camera was restored before safe client shutdown.
- [x] Runtime104: original TNT x5 Dynamite used through MCP on an isolated stone
  block at (-157,245,28). Original projectile collision/terrain job changed stone
  to air; native light returned sky=15; former height errors stayed absent.
  Inventory/equipment summaries, position, camera and selected slot restored;
  temporary tagged item removed and fixture air read back.
- [x] Original workbench opened/closed/reopened (13 slots); user accepted block
  icons, large preview and variant selection. Owned workbench removed afterward.
  Later click-sound linkage errors are tracked separately; no silent clean-log
  claim is made for all runtime104 GUI operations.

Custom source world implementations, source super/default callbacks, old packet
codecs and full TNT behavior remain separate gaps. The pending native lighting
choice is documented in source-chunk-light-packets; no obsolete flag is silently
removed by this height family.
