package com.fuckingdeveloper.lms.target.neoforge263;

import java.util.BitSet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;

/** Accepted policy: native lighting owns edge consistency; source snapshots stay intact. */
public final class Target263SourceChunkLightPackets {
    private Target263SourceChunkLightPackets() {}
    public static ClientboundLevelChunkWithLightPacket chunk(LevelChunk chunk,LevelLightEngine engine,BitSet sky,BitSet block,boolean sourceTrustEdges) {
        return new ClientboundLevelChunkWithLightPacket(chunk,engine,sky,block);
    }
    public static ClientboundLightUpdatePacket light(ChunkPos pos,LevelLightEngine engine,BitSet sky,BitSet block,boolean sourceTrustEdges) {
        return new ClientboundLightUpdatePacket(pos,engine,sky,block);
    }
    public static ClientboundLightUpdatePacketData data(ChunkPos pos,LevelLightEngine engine,BitSet sky,BitSet block,boolean sourceTrustEdges) {
        return new ClientboundLightUpdatePacketData(pos,engine,sky,block);
    }
}
