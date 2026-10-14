package com.fuckingdeveloper.lms.profile;

import com.fuckingdeveloper.lms.discovery.LegacyModDescriptor;

import java.util.Arrays;

public final class Forge1192Profile {
    private static final String TARGET_VERSION = "1.19.2";

    public String id() { return "forge-1.19.2"; }

    public boolean supports(LegacyModDescriptor mod) {
        // Fail closed: require Forge metadata and prove that its Minecraft
        // version range actually contains 1.19.2.
        return "FORGE_METADATA".equals(mod.loader())
                && containsVersion(mod.minecraftVersion(), TARGET_VERSION);
    }

    static boolean containsVersion(String range, String version) {
        if (range == null || range.isBlank()) return false;

        String value = range.trim();
        if (!(value.startsWith("[") || value.startsWith("("))
                || !(value.endsWith("]") || value.endsWith(")"))) {
            return value.equals(version);
        }

        boolean includeLower = value.startsWith("[");
        boolean includeUpper = value.endsWith("]");
        String body = value.substring(1, value.length() - 1).trim();
        String[] bounds = body.split(",", -1);

        // [1.19.2] is Maven's exact-version range form.
        if (bounds.length == 1) {
            return includeLower && includeUpper && compareVersions(version, bounds[0].trim()) == 0;
        }
        if (bounds.length != 2) return false;

        String lower = bounds[0].trim();
        String upper = bounds[1].trim();

        if (!lower.isEmpty()) {
            int comparison = compareVersions(version, lower);
            if (comparison < 0 || (comparison == 0 && !includeLower)) return false;
        }
        if (!upper.isEmpty()) {
            int comparison = compareVersions(version, upper);
            if (comparison > 0 || (comparison == 0 && !includeUpper)) return false;
        }
        return true;
    }

    private static int compareVersions(String left, String right) {
        int[] a = numericParts(left);
        int[] b = numericParts(right);
        int length = Math.max(a.length, b.length);
        for (int i = 0; i < length; i++) {
            int av = i < a.length ? a[i] : 0;
            int bv = i < b.length ? b[i] : 0;
            int comparison = Integer.compare(av, bv);
            if (comparison != 0) return comparison;
        }
        return 0;
    }

    private static int[] numericParts(String version) {
        return Arrays.stream(version.trim().split("\\.")) 
                .mapToInt(Forge1192Profile::leadingInteger)
                .toArray();
    }

    private static int leadingInteger(String part) {
        int end = 0;
        while (end < part.length() && Character.isDigit(part.charAt(end))) end++;
        if (end == 0) return 0;
        try {
            return Integer.parseInt(part.substring(0, end));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }
}
