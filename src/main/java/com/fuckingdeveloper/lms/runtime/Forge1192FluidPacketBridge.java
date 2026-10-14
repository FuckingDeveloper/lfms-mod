package com.fuckingdeveloper.lms.runtime;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
/** Source fluid packets use a named registry/raw id, VarInt amount and source NBT. */
public final class Forge1192FluidPacketBridge {
    private Forge1192FluidPacketBridge() {}
    public static void writeFluidStack(FriendlyByteBuf buffer,FluidStack stack){buffer.writeBoolean(!stack.isEmpty());if(!stack.isEmpty())writeToPacket(stack,buffer);}
    public static FluidStack readFluidStack(FriendlyByteBuf buffer){return buffer.readBoolean()?readFromPacket(buffer):FluidStack.EMPTY;}
    public static void writeToPacket(FluidStack stack,FriendlyByteBuf buffer) {
        if(!stack.getComponentsPatch().isEmpty())throw new UnsupportedOperationException("Source fluid component/NBT packet projection is unproven");
        Forge1192NetworkBridge.writeRegistryId(buffer,BuiltInRegistries.FLUID,stack.getFluid());buffer.writeVarInt(stack.getAmount());Forge1192ItemWireBridge.writeNbt(buffer,null);
    }
    public static FluidStack readFromPacket(FriendlyByteBuf buffer) {
        Object value=Forge1192NetworkBridge.readRegistryId(buffer);if(!(value instanceof Fluid fluid))throw new IllegalArgumentException("Source fluid packet names another registry type");
        int amount=buffer.readVarInt();var tag=Forge1192ItemWireBridge.readNbt(buffer);
        if(tag!=null)throw new UnsupportedOperationException("Source mutable fluid NBT packet projection is unproven");
        return Forge1192EarlyFluidStackBridge.create(fluid,amount);
    }
}
