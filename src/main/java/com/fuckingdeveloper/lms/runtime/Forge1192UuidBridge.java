package com.fuckingdeveloper.lms.runtime;

import java.util.UUID;

/**
 * Executable compatibility boundary for the legacy Mojang UUIDTypeAdapter API.
 *
 * <p>Forge 1.19.2 mods can call UUIDTypeAdapter.fromString directly. That helper
 * is an implementation detail of Authlib and its executable ABI is not stable
 * across target runtimes. Keep the legacy parsing contract in LMS instead of
 * requiring the target Authlib class to retain the old static method.</p>
 */
public final class Forge1192UuidBridge {
    private Forge1192UuidBridge() {}

    public static UUID fromString(String value) {
        if (value == null) throw new NullPointerException("value");

        // Legacy UUIDTypeAdapter accepted both canonical dashed UUIDs and the
        // compact 32-hex representation used by Mojang profile data.
        if (value.length() == 32
                && value.indexOf('-') < 0
                && isHex(value)) {
            value = value.substring(0, 8) + "-"
                    + value.substring(8, 12) + "-"
                    + value.substring(12, 16) + "-"
                    + value.substring(16, 20) + "-"
                    + value.substring(20);
        }
        return UUID.fromString(value);
    }

    private static boolean isHex(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            boolean digit = c >= '0' && c <= '9';
            boolean lower = c >= 'a' && c <= 'f';
            boolean upper = c >= 'A' && c <= 'F';
            if (!digit && !lower && !upper) return false;
        }
        return true;
    }
}
