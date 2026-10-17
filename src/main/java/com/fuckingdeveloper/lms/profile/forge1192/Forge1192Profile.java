package com.fuckingdeveloper.lms.profile.forge1192;

import com.fuckingdeveloper.lms.profile.SessionRequest;
import com.fuckingdeveloper.lms.profile.SourceEvidencePaths;
import com.fuckingdeveloper.lms.profile.SourceProfile;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.Forge1192RuntimeSession;
import com.fuckingdeveloper.lms.runtime.CompatibilityContract;

import com.fuckingdeveloper.lms.discovery.LegacyModDescriptor;

import java.util.Arrays;
import com.fuckingdeveloper.lms.runtime.blockdata.BlockDataTargetLayout;
import com.fuckingdeveloper.lms.target.InstalledTargetAdapters;

public final class Forge1192Profile implements SourceProfile<Forge1192RuntimeSession> {
    private static final String SOURCE_VERSION = "1.19.2";
    private final BlockDataTargetLayout blockDataTarget;

    public Forge1192Profile() { this(InstalledTargetAdapters.blockDataCallbacks()); }
    public Forge1192Profile(BlockDataTargetLayout blockDataTarget) {
        this.blockDataTarget = java.util.Objects.requireNonNull(blockDataTarget);
    }
    public BlockDataTargetLayout blockDataTarget() { return blockDataTarget; }

    public String id() { return "forge-1.19.2"; }

    public boolean supports(LegacyModDescriptor mod) {
        if (!"FORGE_METADATA".equals(mod.loader())) return false;
        // An explicit Minecraft contract wins, even when the loader range includes us.
        if (mod.minecraftVersion() != null && !mod.minecraftVersion().isBlank())
            return containsVersion(mod.minecraftVersion(), SOURCE_VERSION);
        // This declares compatibility with JavaFML 43, not the exact compiler version.
        // Other installed profiles must independently match; the catalog rejects ambiguity.
        return "javafml".equals(mod.languageLoader())
                && numericLoaderRange(mod.languageLoaderVersion())
                && containsVersion(mod.languageLoaderVersion(), "43");
    }

    @Override public SourceEvidencePaths evidence(java.nio.file.Path run) {
        return new SourceEvidencePaths(
                run.resolve("legacy-mappings/1.19.2/joined.tsrg"),
                run.resolve("legacy-mappings/1.19.2/client.txt"),
                run.resolve("legacy-runtime/1.19.2/client.jar"));
    }

    @Override public Forge1192LegacyClassTransformer createTransformer(String distribution) {
        return new Forge1192LegacyClassTransformer(distribution, blockDataTarget);
    }

    @Override public Forge1192RuntimeSession createSession(
            SessionRequest request) throws java.io.IOException, ClassNotFoundException {
        return new Forge1192RuntimeSession(
                request.modId(), request.artifact(), request.dependencies(), request.parent(),
                request.entrypoints(), request.physicalDistribution(), createTransformer(request.physicalDistribution()));
    }

    @Override public java.util.Set<CompatibilityContract> requiredContracts() {
        return java.util.Set.of(CompatibilityContract.PHASE_STAGED_REGISTRATION_V1);
    }

    private Forge1192LaunchPlanner launchPlanner;
    private java.nio.file.Path launchRoot;

    @Override public synchronized java.util.List<com.fuckingdeveloper.lms.transform.TransformationSpec> planLaunch(
            java.nio.file.Path root, LegacyModDescriptor descriptor) throws java.io.IOException {
        var normalized = root.toAbsolutePath().normalize();
        if (launchPlanner == null || !normalized.equals(launchRoot)) {
            launchPlanner = new Forge1192LaunchPlanner(evidence(normalized));
            launchRoot = normalized;
        }
        return launchPlanner.plan(descriptor);
    }

    private static boolean numericLoaderRange(String range) {
        if (range == null || range.isBlank()) return false;
        String value = range.trim();
        if (value.matches("[0-9]+(?:\\.[0-9]+)*")) return true;
        if (value.length() < 3 || (value.charAt(0) != '[' && value.charAt(0) != '(')
                || (value.charAt(value.length()-1) != ']' && value.charAt(value.length()-1) != ')')) return false;
        String[] parts = value.substring(1,value.length()-1).split(",",-1);
        if (parts.length == 1) return value.startsWith("[") && value.endsWith("]")
                && parts[0].trim().matches("[0-9]+(?:\\.[0-9]+)*");
        if (parts.length != 2 || parts[0].isBlank() && parts[1].isBlank()) return false;
        for (String part : parts) if (!part.isBlank() && !part.trim().matches("[0-9]+(?:\\.[0-9]+)*")) return false;
        return true;
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
