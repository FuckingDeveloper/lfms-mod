package com.fuckingdeveloper.lms.target.neoforge263;

import com.fuckingdeveloper.lms.profile.SourceProfiles;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192Profile;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.Forge1192RuntimeBootstrap;
import com.fuckingdeveloper.lms.runtime.RuntimeSession;
import com.fuckingdeveloper.lms.runtime.SourceRuntimeBinding;
import java.util.List;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;

/** Explicit tested source/target composition; selectors and core do not know these classes. */
public final class Target263SourceBindings {
    public static final String TARGET_ID = "minecraft-26.3/neoforge-26.3.0.58-beta/java-25";
    private Target263SourceBindings() {}
    public static List<SourceRuntimeBinding> create(SourceProfiles profiles, IEventBus bus,
            ModContainer host, List<RuntimeSession> sessions, Target263LegacyResourcePackBridge packs) {
        return profiles.installed().stream().map(profile -> {
            if (profile instanceof Forge1192Profile forge)
                return (SourceRuntimeBinding)new Forge1192RuntimeBootstrap(forge,bus,host,sessions,packs);
            throw new UnsupportedOperationException("No NeoForge26.3 runtime binding for " + profile.id());
        }).toList();
    }
}
