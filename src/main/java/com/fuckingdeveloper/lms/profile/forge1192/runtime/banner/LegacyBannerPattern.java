package com.fuckingdeveloper.lms.profile.forge1192.runtime.banner;

import java.util.Objects;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/** Source declaration identity; never masquerades as a loaded native registry value. */
public class LegacyBannerPattern {
    private final String hashname;
    public LegacyBannerPattern(String hashname) { this.hashname=Objects.requireNonNull(hashname); }
    public String getHashname() { return hashname; }
    public static Identifier location(ResourceKey<?> key,boolean banner) {
        var id=key.identifier();
        return Identifier.fromNamespaceAndPath(id.getNamespace(),"entity/"+(banner?"banner/":"shield/")+id.getPath());
    }
    public static Holder<?> byHash(String hash) {
        throw new UnsupportedOperationException("Source banner hash-to-registry-holder lookup requires an active world registry projection");
    }
}
