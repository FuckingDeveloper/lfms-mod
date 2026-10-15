# Source views for native loader configuration

Source configuration viewers inspect actual loader-owned configs. Source ModConfig
and native ModConfig have different type enums, loaded-data ownership and save/event
semantics. Expose source views and verified spec inspection operations; do not rename
these APIs blindly or manufacture managed config ownership.
