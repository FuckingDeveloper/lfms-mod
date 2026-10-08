package com.fuckingdevelop.lms;

import com.fuckingdevelop.lms.discovery.LegacyJarScanner;
import com.fuckingdevelop.lms.discovery.LegacyModDescriptor;
import com.fuckingdevelop.lms.profile.Forge1192Profile;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Mod(LmsMod.MOD_ID)
public final class LmsMod {
    public static final String MOD_ID = "lms";
    private static final Logger LOG = LoggerFactory.getLogger(LmsMod.class);

    public LmsMod() {
        // Discovery-only milestone: never load or transform arbitrary legacy classes.
        Path directory = Path.of(System.getProperty("user.dir"), "legacy-mods");
        try {
            Files.createDirectories(directory);
            List<LegacyModDescriptor> mods = new LegacyJarScanner().scan(directory);
            LOG.info("LMS discovery: {} candidate(s) in {}", mods.size(), directory.toAbsolutePath());
            Forge1192Profile profile = new Forge1192Profile();
            for (LegacyModDescriptor mod : mods) {
                LOG.info("LMS candidate={} id={} loader={} minecraft={} profile={}",
                        mod.file().getFileName(), mod.modId(), mod.loader(), mod.minecraftVersion(),
                        profile.supports(mod) ? profile.id() : "UNRESOLVED");
            }
        } catch (IOException e) {
            LOG.error("LMS legacy discovery failed for {}", directory, e);
        }
    }
}
