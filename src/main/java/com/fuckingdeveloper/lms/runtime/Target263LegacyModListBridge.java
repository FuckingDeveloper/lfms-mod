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
        var visible = sessions.stream().filter(s -> s.state() == Forge1192RuntimeSession.State.INITIALIZED
                || s.state() == Forge1192RuntimeSession.State.BLOCKED).toList();
        if (visible.isEmpty()) return;
        try {
            @SuppressWarnings("unchecked")
            var original = (ImmutableList<ModDisplayInfo>) Access.MODS.get(screen);
            var ids = original.stream().map(ModDisplayInfo::id).collect(java.util.stream.Collectors.toSet());
            var mods = ImmutableList.<ModDisplayInfo>builder().addAll(original);
            int added = 0;
            for (var session : visible) if (ids.add(session.modId())) {
                mods.add(displayInfo(session)); added++;
            }
            if (added == 0) return;
            event.setNewScreen((Screen) Access.CONSTRUCTOR.newInstance(
                    Access.PARENT.get(screen), mods.build(), Access.FOLDER.get(screen),
                    sourceConfigFactory(Access.CONFIG.get(screen)), Access.VERSION.get(screen)));
            com.mojang.logging.LogUtils.getLogger().info("LMS legacy-mod-list entriesAdded={}", added);
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("Cannot project managed legacy mods into target 26.3 mod list", failure);
        }
    }

    /** Wrap the package-private target service, offering only genuine registered source extensions. */
    private static Object sourceConfigFactory(Object nativeFactory) {
        Class<?> service=Access.CONFIG.getType();
        return java.lang.reflect.Proxy.newProxyInstance(service.getClassLoader(),new Class<?>[]{service},(proxy,method,args)-> {
            if(method.getDeclaringClass()==Object.class) return switch(method.getName()) {
                case "equals" -> proxy==args[0];
                case "hashCode" -> System.identityHashCode(proxy);
                case "toString" -> "LMS source/native configuration screen service";
                default -> throw new IllegalStateException("Unexpected Object method: "+method);
            };
            if(!method.getName().equals("create"))throw new IllegalStateException("Changed target configuration service: "+method);
            ModDisplayInfo display=(ModDisplayInfo)args[0];
            var source=Forge1192ModListView.get().getModContainerById(display.id());
            if(source.isPresent()&&source.get().nativeOwner().isEmpty()) {
                var extension=source.get().getCustomExtension(com.fuckingdeveloper.lms.runtime.render.LegacyConfigScreenFactory.class);
                if(extension.isPresent()) {
                    var factory=(com.fuckingdeveloper.lms.runtime.render.LegacyConfigScreenFactory)extension.get();
                    return (java.util.function.UnaryOperator<Screen>)(parent->factory.screenFunction().apply(net.minecraft.client.Minecraft.getInstance(),parent));
                }
            }
            method.setAccessible(true);
            try{return method.invoke(nativeFactory,args);}
            catch(java.lang.reflect.InvocationTargetException failure){throw failure.getCause();}
        });
    }

    public static ModDisplayInfo displayInfo(Forge1192RuntimeSession session) {
        var display = readMetadata(session.artifact(), session.modId());
        return switch (session.state()) {
            case INITIALIZED -> display;
            case BLOCKED -> new BlockedDisplay(display, session.blockReason().orElse("Initialization failed"));
            default -> throw new IllegalStateException("Session is not a loaded or blocked display entry: " + session.state());
        };
    }

    /** Update presentation caches only; FML's native membership remains authoritative. */
    public static void refreshBranding(List<Forge1192RuntimeSession> sessions) {
        var ids = net.neoforged.fml.ModList.get().getMods().stream()
                .map(net.neoforged.neoforgespi.language.IModInfo::getModId)
                .collect(java.util.stream.Collectors.toSet());
        int loaded = ids.size(), blocked = 0;
        for (var session : sessions) if (ids.add(session.modId())) {
            if (session.state() == Forge1192RuntimeSession.State.INITIALIZED) loaded++;
            else if (session.state() == Forge1192RuntimeSession.State.BLOCKED) blocked++;
        }
        var brandings = new java.util.ArrayList<String>();
        net.neoforged.neoforge.internal.BrandingControl.forEachLine(true, false,
                (index, text) -> brandings.add(text));
        if (brandings.size() < 2) throw new IllegalStateException("Unexpected target 26.3 branding layout");
        String count = net.neoforged.fml.i18n.FMLTranslations.parseMessage("fml.menu.branding",
                net.neoforged.neoforge.internal.BrandingControl.BRANDING_NAME + " "
                        + net.neoforged.neoforge.common.NeoForgeVersion.getVersion(), loaded);
        if (blocked > 0) count += " — " + Component.translatable("lms.mods.blocked_count", blocked).getString();
        brandings.set(1, count);
        List<String> immutable = List.copyOf(brandings);
        try {
            BrandingAccess.ALL.set(null, immutable);
            BrandingAccess.NO_MC.set(null, immutable.subList(1, immutable.size()));
        } catch (IllegalAccessException failure) {
            throw new IllegalStateException("Cannot update target mod-count presentation", failure);
        }
    }

    private record BlockedDisplay(ModDisplayInfo original, String reason) implements ModDisplayInfo {
        public String id() { return original.id(); }
        public Component displayName() { return original.displayName().copy().append(" — ")
                .append(Component.translatable("lms.mods.blocked")); }
        public String version() { return original.version(); }
        public Component authors() { return original.authors(); }
        public Component credits() { return original.credits(); }
        public Component description() { return Component.translatable("lms.mods.blocked_description")
                .append("\n" + reason + "\n\n").append(original.description()); }
        public Component license() { return original.license(); }
        public ImageResource banner() { return original.banner(); }
        public ImageResource icon() { return original.icon(); }
        public java.net.URI displayUrl() { return original.displayUrl(); }
        public java.net.URI issuesUrl() { return original.issuesUrl(); }
    }

    private static final class BrandingAccess {
        static final Field ALL = field("brandings"), NO_MC = field("brandingsNoMC");
        private static Field field(String name) {
            try {
                var field = net.neoforged.neoforge.internal.BrandingControl.class.getDeclaredField(name);
                field.setAccessible(true); return field;
            } catch (ReflectiveOperationException failure) { throw new ExceptionInInitializerError(failure); }
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
