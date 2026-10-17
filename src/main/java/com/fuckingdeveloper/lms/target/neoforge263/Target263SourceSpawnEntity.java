package com.fuckingdeveloper.lms.target.neoforge263;

import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;

/** Client-side source view of an actual native spawn packet; no legacy extra-data bytes are fabricated. */
public final class Target263SourceSpawnEntity {
    private final ClientboundAddEntityPacket packet;
    public Target263SourceSpawnEntity(ClientboundAddEntityPacket packet){this.packet=Objects.requireNonNull(packet);}
    public Entity getEntity(){return null;} // Source decoded client packets carry no originating server entity.
    public int getTypeId(){return BuiltInRegistries.ENTITY_TYPE.getId(packet.getType());}
    public int getEntityId(){return packet.getId();}
    public UUID getUuid(){return packet.getUUID();}
    public double getPosX(){return packet.getX();}
    public double getPosY(){return packet.getY();}
    public double getPosZ(){return packet.getZ();}
    public byte getPitch(){return (byte)(packet.getXRot()*256F/360F);}
    public byte getYaw(){return (byte)(packet.getYRot()*256F/360F);}
    public byte getHeadYaw(){return (byte)(packet.getYHeadRot()*256F/360F);}
    public int getVelX(){return velocity(packet.getMovement().x());}
    public int getVelY(){return velocity(packet.getMovement().y());}
    public int getVelZ(){return velocity(packet.getMovement().z());}
    private static int velocity(double value){return (int)(net.minecraft.util.Mth.clamp(value,-3.9D,3.9D)*8000D);}
    public FriendlyByteBuf getAdditionalData(){throw new UnsupportedOperationException("Source entity additional spawn data needs its own native payload contract");}
}
