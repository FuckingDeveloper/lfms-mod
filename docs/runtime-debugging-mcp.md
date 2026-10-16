# Runtime debugging with MCP Fabric

Use the existing `minecraft` MCP server and the MCP Fabric NeoForge build as
external development instrumentation. LMS remains a binary-first Forge 1.19.2
compatibility engine. Installing this tool does not add Fabric mod support.
The current target is Minecraft 26.3 / NeoForge 26.3.0.58-beta / Java 25.

Use [the API-family coverage matrix](compatibility-api-coverage.md) for current
bounded acceptance and priorities. Historical change checklists are chronological
evidence and may be superseded by later accepted runtime results.

Do not add an LMS game-control service, custom automation framework or dependency
on MCP Fabric. Do not patch the reference mod or MCP Fabric to advance a test.
Read the relevant OpenSpec contract before changing compatibility semantics.

## Build and explicit world launch

Preserve the failing client's logs before another process writes to the run
directory. Keep per-run evidence outside Git, for example under `/tmp/`.
Stop the previous test client safely before launching a replacement or running
the native FML smoke probe in the same run directory. Do not terminate arbitrary
Java processes or interrupt a world save. Do not launch concurrent clients
against the same world/bridge/log destination.
Do not compile or regenerate `build/classes`/`build/resources` while a dev client
is using those directories. Complete all build checks first, then launch a fresh
client; a live module classloader is not an incremental hot-reload boundary.

Use the repository wrapper and the existing verification tasks appropriate to
the compatibility family. For the current development slice:

```bash
./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava > /tmp/lms-preflight.log 2>&1
```

Run `compatibilityAudit` when changes affect transformation or unresolved-symbol
inventory. It is an inventory, not a gameplay acceptance test. Compile/focused
checks must pass before exercising a fixed client.

The tested target's `Main` parser accepts `quickPlaySingleplayer`; native
`QuickPlay` opens an existing save through ordinary `WorldOpenFlows`. Use an
explicit directory identifier under `run/saves/`, including spaces as necessary:

```bash
./gradlew runClient -PlmsTestWorld='<test-world-directory>' > /tmp/lms-run.log 2>&1
```

`lmsTestWorld` appends the two native arguments through NeoForge's standard run
DSL. Do not use JavaExec `--args` here: it replaces DevLaunch's main-class argument
and generated program-argument file instead of appending game options. An empty
explicit property fails in Gradle; omitting the property preserves the ordinary
menu launch.

For an ordinary menu launch:

```bash
./gradlew runClient > /tmp/lms-run.log 2>&1
```

Quick Play bypasses menu selection, not datapack validation, world locks, data
migration or compatibility failures. It does not create a new test world. A
missing/invalid identifier is a failed launch gate; do not silently select the
latest world or another user's save. The `minecraft` tool interface has no
world-open/restart tool; native launcher arguments cover opening an existing
world without implementing a replacement framework.

## Connection and readiness gates

1. Discover the actual exposed `minecraft` tools and their schemas. Refresh after
   reconnecting/upgrading; never invent an operation from a low-level method name.
2. Call `get_status`, then `list_capabilities`. Verify Minecraft version, loader,
   client side and available groups. Distinguish advertised methods from usable
   capabilities. Record bridge errors/disconnection instead of treating them as
   successful empty results.
3. Correlate the response with the Gradle dev process, its run directory and the
   current launch log. Verify exact NeoForge and Java identities from runtime
   evidence, not from the bridge's Minecraft version alone. Do not expose JVM
   arguments/environment values that may contain authentication tokens.
4. Confirm `serverPresent` and, for a local test, `integratedServer`; obtain
   `agent_brief`/`get_self`, `list_dimensions` and the intended player/world
   identity. A true `player_local` capability alone does not prove a local player
   exists. An `agent_brief` error such as `no_client_player` blocks gameplay.
5. Verify LMS lifecycle progress independently in logs. `INITIALIZED` proves
   entrypoint progress only. Check actual content before assigning a higher
   compatibility milestone.

If world/data loading fails, inspect the first causal log exception before any
item/block tests. Leave the dependent tests BLOCKED. Do not recover by disabling
legacy content, selecting safe-mode datapacks or bypassing a meaningful callback.

## Structured operations and assertions

Check current capability groups and tool schemas before each scenario. Use a
selected test world and a bounded fixture area, and retain before/after player,
inventory and block observations. Avoid unrelated existing builds or saves.

| Purpose | Existing MCP tools | Required evidence |
| --- | --- | --- |
| Client/world | `get_status`, `list_capabilities`, `agent_brief`, `get_self`, `list_dimensions`, `list_players` | Correct target, intended world/player/dimension and usable capabilities |
| Exact content identities | `give_item`, `get_inventory`, `get_block`, `get_blocks_region`, `find_blocks`, `run_command` | Exact item ID accepted and observed in inventory; exact block ID accepted and read back at a known coordinate |
| Normal placement/removal | `select_hotbar_slot`, `place_block`, `break_block`, `get_block`, `get_inventory`, `poll_events` | Actual server placement/break result and expected count/block changes, not just client prediction |
| Machine menus | `open_container`, `container_transfer`, `close_container`, `get_inventory` | Actual open-menu/slot response; valid input/output transfer; close and reopen; callback failure-free logs |
| Food/effects | `get_self`, `get_inventory`, `get_status_effects`, `start_using_item`, `stop_using_item` | Consumption completed, count/container return, expected hunger/effect change and server stability |
| Equipment | `get_equipment`, `get_inventory`, `swap_slots`, `run_command` | Correct equipped item and observable attributes/data, with worn rendering verified separately |
| Recipes | `get_recipes`, `plan_craft`, `craft_item`, `run_command` | Exact recipe/input/output behavior supported by the tool; do not equate a recipe list with successful crafting |
| Energy/data | `run_command` with supported native `data get` commands; inventory/menu/block observations | Actual charge/input/output changes under appropriate game mode; inspect real serialized data when accessible |
| Error correlation | `poll_events` plus launch/latest/crash logs | Fresh event cursor and first causal exception with scenario/coordinates/content ID |

`set_block`/`give_item`/native commands can prepare fixtures and prove exact
registry-key acceptance. They do not prove ordinary placement, crafting,
consumption, right-click delivery or energy transfer. Use the corresponding
client interaction afterward. For `place_block`, `result` is client prediction;
`placed` is integrated-server confirmation and may be null on a remote server.

Commands passed to `run_command` must omit the leading slash. Capture feedback,
errors and observed state; do not treat command invocation alone as success.
Use commands only in the agreed test world. Do not erase or broadly rebuild a
world to make a fixture convenient.

Observe each action before continuing. Stop a batch at a causal crash/world-loss
boundary and mark later checks BLOCKED rather than cascading commands against a
missing player. Use `job_status` for existing background actions and do not issue
manual movement while a job owns movement. Energy tests must distinguish creative
mode exceptions, real charge deltas, transfer direction and persistence.

## Limits of the exposed interface

- No direct registry-enumeration tool is exposed. Exact IDs can be verified via
  supported game commands and inventory/block readback; this is not an exhaustive
  registry dump. Do not infer registration from source JAR strings or static gaps.
- No direct LMS-session/mod-list query tool is exposed. Use current LMS logs and
  actual content observations, and distinguish independently blocked dependencies.
- Inventory summaries report IDs/counts/durability; arbitrary component/NBT or
  energy state is not guaranteed. Native `data get` is a supported command path
  only where the target actually serializes the required state. If it cannot
  expose that state, leave the assertion UNVERIFIED.
- Low-level `container.state`/`container.click` may be advertised by the bridge
  without a corresponding exposed MCP tool. Use the actual `open_container` and
  transfer tools; do not invent direct bridge calls or a custom socket client.
- Custom mod crafting and old GUI rendering/input may exceed available operations.
  Neither successful opening nor slot data proves visual correctness.
- Blockstate/neighbor data cannot alone prove cable meshes, worn textures, HUD or
  GPU rendering. Use `describe_scene` where sufficient; request a deliberate
  visual/manual check or a screenshot only for an unresolved visual assertion.
- No Java-crash/stacktrace tool or world-create/quit/restart tool is exposed. Use
  existing runtime logs and normal process/launcher operations. Record tool gaps;
  agree before implementing any custom replacement.

Capabilities must be rechecked after world load/loss, reconnect or restart.
These interface limits do not justify weakening any LMS fail-fast contract.

## Log collection and result handling

For an initial block-model synchronization investigation, opt into the existing
LMS boundary trace for one explicit position:

```bash
LMS_TRACE_MODEL_POS='x,y,z' ./gradlew runClient -PlmsTestWorld='<test-world-directory>' > /tmp/lms-model-sync.log 2>&1
rg -n 'model-data trace|/ERROR\]|/FATAL\]|^Caused by:' /tmp/lms-model-sync.log
```

Replace `x,y,z` with actual integer coordinates. This logs persistent input,
initial tag output/input and the positioned model-data snapshot; it does not query
live block entities from rendering workers or change refresh timing. Distinguish
missing saved block-entity records from lost initial callbacks before changing
compatibility behavior. Inspecting a block through a native command can itself
trigger Minecraft's lazy creation of a missing entity, so capture initial traces
and saved data before such queries. The trace is disabled when the variable is absent.

Use structured MCP responses for observations and the following exact filter for
causal Java/runtime evidence:

```bash
rg -n -A30 -B5 'LMS lifecycle|LMS runtime-session|LMS managed-transform|ERROR|FATAL|Exception|Caused by:|NoClassDefFoundError|ClassNotFoundException|VerifyError|NoSuchMethodError|NoSuchFieldError|AbstractMethodError|IllegalAccessError' \
  /tmp/lms-run.log run/logs/latest.log > /tmp/lms-runtime-evidence.log
```

Retain the full log and newest relevant crash report too. Capture per-scenario
before/after state, event cursor, target/build/source artifact identities, exact
operations, acceptance assertions and first causal blocker. Keep reports and
screenshots outside Git. Never commit access tokens or local MCP connection
configuration; do not copy such values into documentation or command output.

Report PASS/PARTIAL/BLOCKED/UNVERIFIED for each scenario and the highest observed
LMS milestone. Reaching the menu is level 5; successful world load is level 6;
basic legacy behavior must be observed before claiming level 7. A historical
world-load success does not establish success for a later build/test run.

## Exposed tool reference

The integration uses the following installed interface names as a reference;
refresh discovery rather than assuming future versions preserve every tool.

```text
agent_brief, apply_effect, attack_entity, break_block, close_container,
collect_blocks, container_transfer, craft_item, describe_scene, drop_held_item,
drop_slot, explore, explore_next, fill_blocks, find_blocks, get_block,
get_blocks_region, get_entity, get_equipment, get_inventory, get_player,
get_recent_chat, get_recipes, get_self, get_status, get_status_effects,
get_time_and_weather, give_item, goal_add, goal_list, goal_update, job_cancel,
job_status, jump, kick_player, list_capabilities, list_dimensions, list_players,
look, look_at, map_view, memory_update, memory_verify, message_player,
navigate_to, navigation_status, observe, open_container, place_block,
plan_craft, poll_events, query_entities, raycast, recall, remember,
remove_entity, run_command, screenshot, select_hotbar_slot, send_chat,
set_block, set_gamemode, set_movement, set_time, set_weather, start_using_item,
stop_movement, stop_navigation, stop_using_item, summon_entity, swap_slots,
teleport_player, travel_to, use_entity, use_item
```

## Dedicated-server checks

`./gradlew --console=plain runServer` uses the existing native console; Gradle
forwards standard input. Use a persistent terminal for commands and `stop` so the
server saves and the Gradle process exits normally. MCP Fabric's client bridge is
not evidence of dedicated-server readiness. Check the real DEDICATED_SERVER log,
source entrypoint states, data transaction and world readiness; then execute
bounded native commands to verify actual registered content and callbacks.
Keep the dedicated `run/world` save separate from the agreed singleplayer save.
Do not run another build, native smoke or client against these dev classes while
the server is alive. Restore temporary fixtures/tickets, issue `stop`, and confirm
save logs and process exit before starting the client.
