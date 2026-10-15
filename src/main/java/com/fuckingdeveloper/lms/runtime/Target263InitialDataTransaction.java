package com.fuckingdeveloper.lms.runtime;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.List;
import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.flag.FeatureFlagSet;

/** Bounded bootstrap/final native data phases, only while source setup is outstanding. */
public final class Target263InitialDataTransaction {
    private static final AtomicBoolean REQUESTED = new AtomicBoolean();
    private static final ThreadLocal<Boolean> BOOTSTRAP = new ThreadLocal<>();
    private static final org.slf4j.Logger LOG = com.mojang.logging.LogUtils.getLogger();
    private Target263InitialDataTransaction() {}

    public static void sourceSetupCaptured() { REQUESTED.set(true); }
    public static boolean bootstrapActive() { return Boolean.TRUE.equals(BOOTSTRAP.get()); }

    public static CompletableFuture<ReloadableServerResources> load(
            ResourceManager resources, LayeredRegistryAccess<RegistryLayer> layers,
            List<Registry.PendingTags<?>> tags, FeatureFlagSet features,
            Commands.CommandSelection commands, PermissionSet permissions,
            Executor background, Executor main) {
        Supplier<CompletableFuture<ReloadableServerResources>> first = () ->
                ReloadableServerResources.loadResources(resources, layers, tags, features,
                        commands, permissions, background, main);
        if (!REQUESTED.get()) return first.get();
        LOG.info("LMS initial-data transaction phase=bootstrap");
        return bootstrapThenFinal(first,
                ReloadableServerResources::updateComponentsAndStaticRegistryTags,
                () -> {
                    LOG.info("LMS initial-data transaction phase=final");
                    var finalTags = TagLoader.loadTagsForExistingRegistries(
                            resources, layers.getLayer(RegistryLayer.STATIC));
                    return ReloadableServerResources.loadResources(resources, layers, finalTags,
                            features, commands, permissions, background, main);
                }, background, main).thenApply(result -> {
                    REQUESTED.set(false);
                    LOG.info("LMS initial-data transaction final-prepared=true");
                    return result;
                });
    }

    /** The scope ends before asynchronous final preparation, even when bootstrap commit throws. */
    public static <T> CompletableFuture<T> bootstrapThenFinal(
            Supplier<CompletableFuture<T>> bootstrap, Consumer<T> commit,
            Supplier<CompletableFuture<T>> finalLoad, Executor background, Executor main) {
        return bootstrap.get().thenAcceptAsync(result -> {
            if (bootstrapActive()) throw new IllegalStateException("Nested initial data bootstrap");
            BOOTSTRAP.set(true);
            try { commit.accept(result); }
            finally { BOOTSTRAP.remove(); }
        }, main).thenComposeAsync(ignored -> finalLoad.get(), background);
    }
}
