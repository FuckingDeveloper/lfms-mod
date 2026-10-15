- [x] Inspect source custom item extension, renderer, dispatcher and native GUI submission.
- [x] Specify the GUI-only complete-override subset and unsupported boundaries.
- [x] Preserve item-owned extension intent and original renderer geometry.
- [x] Derive native item/base definitions and register the target model codec.
- [x] Verify regression suite including rejection boundaries (`compatibilityTest nativeCompatibilitySmoke compileLaunchJava`).
- [x] Launch runtime32 via Gradle into `Новый мир`; MCP confirms actual loaded IC2/Iron Chests inventory, no new runtime error and persisted chainsaw charge 950 EU.
- [x] Diagnose runtime32 transparent-renderer camera failure; adapt the exact native camera-position observation.
- [x] Runtime33 MCP opens all 14 source GUI callbacks without a crash; identifies shared obsidian image as native GUI cache aliasing.
- [x] Runtime34 MCP repeats all 14 source callbacks; visual inspection confirms distinct source material icons with no new error. Test barrel is emptied/removed and original inventory retained.
- [ ] User creative-tab re-entry acceptance; held/world contexts remain open.

Runtime38 follow-up (deferred-custom-item-contexts) extends the original GUI-only
subset to seven proven native submission contexts. Actual first-person right-hand
and mixed five-item container rendering now pass; ground/shelf/bounds/outline and
initial contents synchronization remain open. Earlier GUI-only limitations above
are historical and must not be treated as completion of all world contexts.
