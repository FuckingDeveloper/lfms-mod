- [x] Inspect source ordered setup, isolated defaults and native binding order.
- [x] Compile and execute focused/native regression checks (passed).
- [x] Runtime47 rejected early setup: original inventory scan needs unavailable native trim_material/lapis. Experiment removed; first-load fix remains OPEN.
- [ ] Verify fresh tree leaf persistence with native ticks.

User decisions: old saved cables are retired/closed. Transparent chest initial
contents remain an open source behavior limitation; only general adapter rules
are authorized. Original crystal chest load fills its private inventory directly,
without its dirty-marking accessors; no initial packet callback was found.

Source tooltip evidence: original Minecraft search also supplies null player;
original Forge forwards it and the fixture tooltip handler dereferences it.
No fake player, skipped listener or suppressed exception is introduced.
