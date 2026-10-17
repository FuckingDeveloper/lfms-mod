# Chunk/light snapshot verification

- [x] Inspect exact source trustEdges wire/storage path and native packet/solver
  sources; obtain user acceptance of native 26.3 lighting (2026-10-17).
- [x] Adapt three original snapshot constructors and constructor handles plus
  thirteen proven getter/handle operations; retain unknown flag/raw-codec gaps.
- [x] Native probes verify actual target snapshot construction, one-time boolean
  operand effects for both values, selected/empty layer masks, copied light
  bytes, getter identity, three target codec roundtrips and immutable input.
  The chunk probe uses a bounded empty-section fixture; it is not a full world.
- [x] `compatibilityTest nativeCompatibilitySmoke compileLaunchJava` passed
  (`/tmp/lms-chunk-light-check.log`).
- [x] Selected artifact audit including original terrain job: 4323 classes,
  1146 grouped findings / 5748 sites; former height and chunk constructor
  findings absent (`/tmp/lms-height-light-audit.log` and `.tsv`).
- [x] Runtime104 actual use of source TNT x5 Dynamite destroyed the isolated
  stone fixture at (-157,245,28), exercising original terrain modification and
  normal client update processing without chunk/height/packet exceptions.
  No projectile remained near the fixture. Returned block is air, sky light 15.
- [x] Test item cleared by its unique custom-data marker; inventory/equipment
  summary matched the full baseline. Camera/position/vitals/selected slot restored.
  Prior owned workbench removed and air confirmed after visual acceptance.

Runtime104 later reported a source SoundManager.play(SoundInstance):void linkage
error during human GUI clicks, without client termination. It is independent of
the accepted packet path and is the next causal GUI blocker.

Legacy raw FriendlyByteBuf codecs, packet subclassing and reads of removed
trustEdges remain unsupported. Source SimpleChannel wire compatibility is not
inferred. No source artifact or MCP Fabric implementation is modified.
