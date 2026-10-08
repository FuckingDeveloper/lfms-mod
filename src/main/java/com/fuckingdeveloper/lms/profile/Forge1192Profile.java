package com.fuckingdeveloper.lms.profile;

import com.fuckingdeveloper.lms.discovery.LegacyModDescriptor;

public final class Forge1192Profile {
    public String id() { return "forge-1.19.2"; }

    public boolean supports(LegacyModDescriptor mod) {
        // Fail closed: do not infer generation from filenames.
        return "FORGE_METADATA".equals(mod.loader())
                && mod.minecraftVersion().contains("1.19.2");
    }
}
