package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryList;

/** Source-profile bridge for client debug options whose storage model changed. */
public final class Forge1192DebugBridge {
    private Forge1192DebugBridge() {}

    /**
     * Preserves Forge 1.19.2 DebugRenderer#switchRenderChunkborder semantics.
     * The source method toggled the value and returned its new state; the target
     * stores that same option as a named debug-screen entry.
     */
    public static boolean toggleChunkBorders(DebugScreenEntryList entries) {
        return entries.toggleStatus(DebugScreenEntries.CHUNK_BORDERS);
    }
}
