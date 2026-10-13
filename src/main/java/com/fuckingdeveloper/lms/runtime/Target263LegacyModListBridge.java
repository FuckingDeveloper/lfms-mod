package com.fuckingdeveloper.lms.runtime;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.toml.TomlParser;
import com.google.common.collect.ImmutableList;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipFile;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.modlist.ImageResource;
import net.neoforged.neoforge.client.gui.modlist.ModDisplayInfo;
import net.neoforged.neoforge.client.gui.modlist.ModListScreen;

/** Target-owned screen projection, without manufacturing FML loader containers. */
public final class Target263LegacyModListBridge {
    private Target263LegacyModListBridge() {}

    public static void opening(ScreenEvent.Opening event, List<Forge1192RuntimeSession> sessions) {
        if (!(event.getNewScreen() instanceof ModListScreen screen)) return;
        var loaded = sessions.stream().filter(s -> s.state() == Forge1192RuntimeSession.State.INITIALIZED).toList();
        if (loaded.isEmpty()) return;
        try {
            @SuppressWarnings("unchecked")
            var original = (ImmutableList<ModDisplayInfo>) Access.MODS.get(screen);
            var ids = original.stream().map(ModDisplayInfo::id).collect(java.util.stream.Collectors.toSet());
            var mods = ImmutableList.<ModDisplayInfo>builder().addAll(original);
            int added = 0;
            for (var session : loaded) if (ids.add(session.modId())) {
                mods.add(readMetadata(session.artifact(), session.modId())); added++;
            }
            if (added == 0) return;
            event.setNewScreen((Screen) Access.CONSTRUCTOR.newInstance(
                    Access.PARENT.get(screen), mods.build(), Access.FOLDER.get(screen),
                    Access.CONFIG.get(screen), Access.VERSION.get(screen)));
            com.mojang.logging.LogUtils.getLogger().info("LMS legacy-mod-list entriesAdded={}", added);
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("Cannot project managed legacy mods into target 26.3 mod list", failure);
        }
    }

    public static ModDisplayInfo readMetadata(Path artifact, String modId) {
        try (var archive = new ZipFile(artifact.toFile())) {
            var entry = archive.getEntry("META-INF/mods.toml");
            if (entry == null) throw new IllegalStateException("Missing admitted Forge metadata: " + artifact);
            UnmodifiableConfig document;
            try (var reader = new InputStreamReader(archive.getInputStream(entry), StandardCharsets.UTF_8)) {
                document = new TomlParser().parse(reader);
            }
            List<? extends UnmodifiableConfig> mods = document.get("mods");
            var mod = mods.stream().filter(c -> modId.equals(c.get("modId"))).findFirst()
                    .orElseThrow(() -> new IllegalStateException("Missing admitted mod id " + modId));
            String version = text(mod, "version", "");
            if (version.equals("${file.jarVersion}")) {
                var manifestEntry = archive.getEntry("META-INF/MANIFEST.MF");
                if (manifestEntry == null) throw new IllegalStateException("Missing jarVersion manifest for " + modId);
                try (var input = archive.getInputStream(manifestEntry)) {
                    version = new java.util.jar.Manifest(input).getMainAttributes().getValue("Implementation-Version");
                }
                if (version == null) throw new IllegalStateException("Missing Implementation-Version for " + modId);
            }
            return new Display(modId, text(mod, "displayName", modId), version,
                    text(mod, "authors", ""), text(mod, "credits", ""),
                    text(mod, "description", ""), text(document, "license", ""));
        } catch (java.io.IOException failure) {
            throw new IllegalStateException("Cannot read admitted legacy display metadata: " + artifact, failure);
        }
    }

    private static String text(UnmodifiableConfig config, String key, String fallback) {
        Object value = config.get(key);
        return value == null ? fallback : value.toString();
    }

    private record Display(String id, String name, String version, String authorText,
                           String creditText, String descriptionText, String licenseText) implements ModDisplayInfo {
        public Component displayName() { return Component.literal(name); }
        public Component authors() { return Component.literal(authorText); }
        public Component credits() { return Component.literal(creditText); }
        public Component description() { return Component.literal(descriptionText); }
        public Component license() { return Component.literal(licenseText); }
        public ImageResource banner() { return null; }
        public ImageResource icon() { return null; }
        public java.net.URI displayUrl() { return null; }
        public java.net.URI issuesUrl() { return null; }
    }

    /** Exact inspected 26.3 screen ABI; no final-field mutation or loader-list modification. */
    private static final class Access {
        static final Field MODS = field("mods"), PARENT = field("lastScreen"), FOLDER = field("modsFolder"),
                CONFIG = field("configFactory"), VERSION = field("versionCheck");
        static final Constructor<?> CONSTRUCTOR = constructor();
        private static Field field(String name) {
            try { var field = ModListScreen.class.getDeclaredField(name); field.setAccessible(true); return field; }
            catch (ReflectiveOperationException failure) { throw new ExceptionInInitializerError(failure); }
        }
        private static Constructor<?> constructor() {
            try {
                var constructor = ModListScreen.class.getDeclaredConstructor(Screen.class, ImmutableList.class,
                        Path.class, CONFIG.getType(), VERSION.getType());
                constructor.setAccessible(true); return constructor;
            } catch (ReflectiveOperationException failure) { throw new ExceptionInInitializerError(failure); }
        }
    }
}
