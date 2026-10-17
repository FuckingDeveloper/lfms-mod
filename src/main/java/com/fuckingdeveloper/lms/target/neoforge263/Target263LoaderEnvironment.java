package com.fuckingdeveloper.lms.target.neoforge263;

import java.nio.file.Path;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLLoader;

/** Genuine active target launch facts; never source membership or guessed metadata. */
public final class Target263LoaderEnvironment {
    private Target263LoaderEnvironment() {}
    public static boolean isProduction() { return FMLLoader.getCurrent().isProduction(); }
    public static Dist getDist() { return FMLLoader.getCurrent().getDist(); }
    public static Path getGamePath() { return FMLLoader.getCurrent().getGameDir(); }
}
