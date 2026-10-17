package com.fuckingdeveloper.lms.profile.forge1192;

import com.fuckingdeveloper.lms.transform.MethodRuleCatalog;
import com.fuckingdeveloper.lms.target.InstalledTargetAdapters;
import java.util.*;
import org.objectweb.asm.*;

/** Exact source static declarations; lifecycle-owned target launch facts. */
public final class Forge1192LoaderEnvironmentRules {
    public static final String SOURCE="net/minecraftforge/fml/loading/FMLLoader";
    private Forge1192LoaderEnvironmentRules() {}
    public static List<MethodRuleCatalog.Rule> entries() {
        var rules=new ArrayList<MethodRuleCatalog.Rule>();
        for(var entry:Map.of("isProduction","()Z","getDist","()Lnet/minecraftforge/api/distmarker/Dist;","getGamePath","()Ljava/nio/file/Path;").entrySet())
            rules.add(new MethodRuleCatalog.Rule("loader-environment-"+entry.getKey(),"LOADER_ENVIRONMENT",
                    new MethodRuleCatalog.Member(SOURCE,entry.getKey(),entry.getValue()),Opcodes.INVOKESTATIC,
                    new Handle(Opcodes.H_INVOKESTATIC,InstalledTargetAdapters.loaderEnvironmentBridge(),entry.getKey(),entry.getValue(),false),
                    "Pinned Forge43.4.0 FMLLoader static launch fields; FML12.0.9 active loader dist/production/gameDir fields and getters"));
        return List.copyOf(rules);
    }
}
