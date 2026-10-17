package com.fuckingdeveloper.lms.profile.forge1192.runtime.language;

import java.util.Locale;

/** Pinned source value; the target record no longer carries language-code identity. */
public class LegacyLanguageInfo implements Comparable<LegacyLanguageInfo> {
    public static final LegacyLanguageInfo DEFAULT = new LegacyLanguageInfo("en_us", "US", "English", false);
    private final String code;
    private final String region;
    private final String name;
    private final boolean bidirectional;
    private final Locale javaLocale;

    @SuppressWarnings("deprecation")
    public LegacyLanguageInfo(String code, String region, String name, boolean bidirectional) {
        this.code = code;
        this.region = region;
        this.name = name;
        this.bidirectional = bidirectional;
        String[] parts = code.split("_", 2);
        this.javaLocale = parts.length == 1 ? new Locale(code) : new Locale(parts[0], parts[1]);
    }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getRegion() { return region; }
    public boolean isBidirectional() { return bidirectional; }
    public Locale getJavaLocale() { return javaLocale; }
    @Override public String toString() { return String.format(Locale.ROOT, "%s (%s)", name, region); }
    @Override public boolean equals(Object other) {
        return this == other || other instanceof LegacyLanguageInfo info && code.equals(info.code);
    }
    @Override public int hashCode() { return code.hashCode(); }
    @Override public int compareTo(LegacyLanguageInfo other) { return code.compareTo(other.code); }
}
