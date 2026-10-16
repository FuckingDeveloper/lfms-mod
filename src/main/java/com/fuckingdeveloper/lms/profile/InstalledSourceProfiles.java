package com.fuckingdeveloper.lms.profile;

import com.fuckingdeveloper.lms.profile.forge1192.Forge1192Profile;
import java.util.List;

/** Explicit distribution composition; listing another profile requires its implementation/tests. */
public final class InstalledSourceProfiles {
    private InstalledSourceProfiles() {}
    public static SourceProfiles create() {
        return new SourceProfiles(List.of(new Forge1192Profile()));
    }
}
