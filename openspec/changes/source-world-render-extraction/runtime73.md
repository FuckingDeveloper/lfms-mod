# Runtime72–73 functional follow-up

Source Forge1.19.2, target Minecraft26.3 / NeoForge26.3.0.58-beta / Java25.
Commands use existing MCP Fabric tools. No original JAR or MCP source changed.

## Active-fuel persistence

An isolated generator at(-206,78,12), originally air, receives one coal block
through its original `items` NBT schema. Actual source simulation consumes that
item and reaches fuel3637/storage3630/max_fuel4000/active=true. Native tick freeze
then isolates persistence from ongoing consumption. Move2200 blocks away;
`execute if loaded` becomes false. Return: exact same values, including fuel3637.
Stop the explicitly authorized test JVM after this completed chunk save.

Runtime73 cold native load trace supplies exactly fuel3637/storage3630/
max_fuel4000/active1b. Actual source ticking resumes after startup, reaching
storage4000 with decreasing fuel. This closes the controlled remaining-fuel
unload/reload/cold-start path. It does not promise persistence of every arbitrary
source tick mutation before any source/native dirty notification.

## Actual reader interaction and mechanism Shift route

Temporary reader is placed in hotbar3 after copying original crafting table to
previously empty slot25. Existing MCP control.startUsing/stopUsing, with genuine
render frames between them, performs the real source interaction. Reader writes
pos=-56624848781234, dim=minecraft:overworld, last_time1398633, last_out1586,
last_in0 itself. No command supplied those measurement fields. No causal runtime
exception observed. The known source cube overlay remains active without crash.

An adjacent isolated macerator at(-206,78,13), also originally air, receives
energy800 through the actual original energy network from that generator.
MCP Shift-inserts one copper ore. Native data observes progress155 then381;
later source output is exactly2 copper dust. Actual Shift-take removes both,
leaving the machine empty. This confirms the already adapted common copper tag
path after a cold client startup. It does not prove native-target energy-provider
interoperability or every configurable inventory route.

Initial MCP container.open attempts failed its preflight `opensMenu` test before
any interaction: these source blocks open their menu from use callbacks rather
than returning the native MenuProvider queried by that preflight. This is a
limitation of that MCP operation, not evidence of an LMS menu failure. Existing
MCP held-use controls reach the normal game interaction and work here; no custom
Minecraft control framework or MCP modification was introduced.

## Cleanup and remaining manual acceptance

Restore original crafting table to hotbar3 and clear only temporary slot25/reader.
Remove exactly2 test copper dust; source inventory had no copper dust initially.
Restore both fixture positions to original air and player's pre-probe position/
camera(-203.81094306476245,76,10.794005819486665;30.532179/20.999994).
Native tick query confirms normal20TPS, not frozen, after restart. Health20,
food20, saturation20 and XP1+1/3 are retained; no survival/damage probe performed.

A new isolated generator was subsequently prepared at(-206,78,12) with one coal
for the pending manual comparator/inventory-panel acceptance. Keep this fixture
until that acceptance finishes, then remove it and restore position. MCP cannot
activate those source GUI widgets. Do not mark their configuration/output family
complete from the successful slot/Shift tests. Runtime73 has no fresh ERROR/FATAL/
Caused-by through reader and copper-result cleanup.

## Startup observations

Runtime72 initial-data bootstrap16:10:00/final16:10:10/login16:10:14;
runtime73 bootstrap16:16:33/final16:16:41/login16:16:45. Source CommonSetup and
approved two-phase load are preserved. These are phase measurements, not proof
of a general speedup. Remaining profile work must establish the cause before
changing tag/resource enumeration, source simulations or target model loading.

No independent native energy consumer/provider is installed for a gameplay
interoperability test; the transactional native energy family remains covered
by existing focused tests, without inferring live interoperability from source
EU transmission. The reference artifact has no custom humanoid armor-model
callback, so that callback is not runtime-covered by its accepted worn textures.
Custom dispenser/enchantment/dye paths remain distinct verification tasks.

## Human comparator-panel clarification

Human confirms access-control settings survive reopening, and item-filter mode
highlights slots. This does not yet establish filter contents/transfer behavior.
The reported comparator reset concerns the selected direction. Immutable source
ComparatorComponent initializes selected=6; changeSide only changes that GUI
field and resets its scroll position. It does not send a tile event or persist
selection. Source persistent output rules instead reside in ComparatorManager's
comparator_data lists, keyed per side. Do not change the original mod's ephemeral
panel selection into a persisted block setting.

At this checkpoint native block data contains handler.side49/access3843 but no
comparator_data. No runtime ERROR/FATAL/Caused-by is observed. Human acceptance
for adding the “Буфер энергии” source to a chosen side, reopening and selecting
that same side again is pending; absence of comparator_data alone is not a
network/persistence defect when only the selected direction was changed.
