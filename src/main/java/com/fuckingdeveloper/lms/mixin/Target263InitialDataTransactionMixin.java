package com.fuckingdeveloper.lms.mixin;

import com.fuckingdeveloper.lms.target.neoforge263.Target263InitialDataTransaction;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.server.*;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Only initial WorldLoader entry; ordinary server reload is untouched. */
@Mixin(WorldLoader.class)
abstract class Target263InitialDataTransactionMixin {
    @Redirect(method = "*", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/server/ReloadableServerResources;loadResources(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/core/LayeredRegistryAccess;Ljava/util/List;Lnet/minecraft/world/flag/FeatureFlagSet;Lnet/minecraft/commands/Commands$CommandSelection;Lnet/minecraft/server/permissions/PermissionSet;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"), require = 1, allow = 1)
    private static CompletableFuture<ReloadableServerResources> lms$initialData(
            ResourceManager resources, LayeredRegistryAccess<RegistryLayer> layers,
            List<Registry.PendingTags<?>> tags, FeatureFlagSet features,
            Commands.CommandSelection commands, PermissionSet permissions,
            Executor background, Executor main) {
        return Target263InitialDataTransaction.load(resources, layers, tags,
                features, commands, permissions, background, main);
    }
}
