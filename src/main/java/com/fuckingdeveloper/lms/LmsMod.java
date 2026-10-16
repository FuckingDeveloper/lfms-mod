package com.fuckingdeveloper.lms;

import com.fuckingdeveloper.lms.profile.InstalledSourceProfiles;
import com.fuckingdeveloper.lms.runtime.RuntimeBootstrap;
import com.fuckingdeveloper.lms.runtime.RuntimeSession;
import com.fuckingdeveloper.lms.target.neoforge263.Target263SourceBindings;

import com.fuckingdeveloper.lms.target.neoforge263.Target263LegacyResourcePackBridge;
import com.fuckingdeveloper.lms.target.neoforge263.Target263ClientRuntimeBootstrap;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Mod(LmsMod.MOD_ID)
public final class LmsMod {
    public static final String MOD_ID = "lms";
    private static final Logger LOG = LoggerFactory.getLogger(LmsMod.class);
    private final java.util.List<RuntimeSession> runtimeSessions = new java.util.concurrent.CopyOnWriteArrayList<>();
    private final Target263LegacyResourcePackBridge legacyResourcePacks = new Target263LegacyResourcePackBridge();

    public LmsMod(IEventBus modBus, ModContainer hostContainer) {
        modBus.addListener(com.fuckingdeveloper.lms.target.neoforge263.Target263SourceNameLock::register);
        modBus.addListener(com.fuckingdeveloper.lms.target.neoforge263.worldgen.Target263WorldgenAdapter::register);
        modBus.addListener(com.fuckingdeveloper.lms.target.neoforge263.worldgen.Target263WorldgenPack::addPackFinders);
        modBus.addListener(AddPackFindersEvent.class, legacyResourcePacks::addPackFinders);
        modBus.addListener(net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent.class, event -> {
            for (var session : runtimeSessions) {
                var transformer = session.transformer();
                LOG.info("LMS startup-timing id={} state={} transformedClasses={} transformationTotalMs={}",
                        session.modId(), session.state(), transformer.transformedClasses(),
                        transformer.transformationMillis());
            }
        });
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            Target263ClientRuntimeBootstrap.register(modBus, () -> java.util.List.copyOf(runtimeSessions));
        }
        var profiles = InstalledSourceProfiles.create();
        var bindings = Target263SourceBindings.create(
                profiles, modBus, hostContainer, runtimeSessions, legacyResourcePacks);
        Path directory = Path.of(System.getProperty("user.dir"), "legacy-mods");
        try {
            RuntimeBootstrap.start(directory, profiles, bindings);
        } catch (IOException failure) {
            LOG.error("LMS legacy discovery failed for {}", directory, failure);
        }
    }
}
