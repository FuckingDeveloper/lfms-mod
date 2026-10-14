package com.fuckingdeveloper.lms.mixin;

import com.fuckingdeveloper.lms.runtime.capability.*;
import com.fuckingdeveloper.lms.runtime.event.LegacyAttachCapabilitiesEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Exact source gather/removal/NBT timing on the native target base entity. */
@Mixin(BlockEntity.class)
abstract class Target263BlockCapabilityMixin implements LegacyBlockCapabilityCarrier {
    @Unique private LegacyBlockCapabilityDispatcher lms$capabilities;
    @Override public LegacyBlockCapabilityDispatcher lms$sourceCapabilities() {
        if (lms$capabilities == null) throw new IllegalStateException("Source block capability constructor hook has not run");
        return lms$capabilities;
    }
    @Inject(method = "<init>", at = @At("RETURN"))
    private void lms$gather(BlockEntityType<?> type, BlockPos position, BlockState state, CallbackInfo callback) {
        lms$capabilities = new LegacyBlockCapabilityDispatcher();
        var event = new LegacyAttachCapabilitiesEvent<>(BlockEntity.class, (BlockEntity)(Object)this);
        NeoForge.EVENT_BUS.post(event);
        lms$capabilities.complete(event);
    }
    @Inject(method = "setRemoved", at = @At("TAIL"))
    private void lms$invalidate(CallbackInfo callback) { invalidateCaps(); }
    public void invalidateCaps() { lms$sourceCapabilities().invalidate(); }
    public void reviveCaps() { lms$sourceCapabilities().revive(); }
    public void onChunkUnloaded() { invalidateCaps(); }
    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void lms$load(ValueInput input, CallbackInfo callback) {
        input.read("ForgeCaps", CompoundTag.CODEC).ifPresent(lms$sourceCapabilities()::deserialize);
    }
    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void lms$save(ValueOutput output, CallbackInfo callback) {
        CompoundTag data = lms$sourceCapabilities().serialize();
        if (!data.isEmpty()) output.store("ForgeCaps", CompoundTag.CODEC, data);
    }
}
