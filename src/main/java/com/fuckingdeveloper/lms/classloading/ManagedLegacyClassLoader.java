package com.fuckingdeveloper.lms.classloading;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * Controlled loader for classes owned by one managed legacy artifact.
 *
 * <p>The source JAR is never modified. Platform/JDK classes are always delegated
 * to the target loader. Legacy-owned classes are defined from the managed JAR
 * without class initialization. This is the classloading boundary on which
 * profile transformations and dependency visibility will be layered.</p>
 */
public final class ManagedLegacyClassLoader extends ClassLoader implements AutoCloseable {
    private static final Set<String> PARENT_FIRST = Set.of(
            "java.", "javax.", "jdk.", "sun.", "net.minecraft.",
            "net.neoforged.", "org.slf4j.", "org.objectweb.asm.");

    private final Path artifact;
    private final JarFile jar;
    private final LegacyClassTransformer transformer;

    public ManagedLegacyClassLoader(Path artifact, ClassLoader targetLoader) throws IOException {
        this(artifact, targetLoader, (name, bytes) ->
                LegacyClassTransformer.Result.unchanged(bytes, "identity profile transformer"));
    }

    public ManagedLegacyClassLoader(Path artifact, ClassLoader targetLoader,
                                    LegacyClassTransformer transformer) throws IOException {
        super(Objects.requireNonNull(targetLoader, "targetLoader"));
        this.artifact = artifact.toAbsolutePath().normalize();
        this.jar = new JarFile(this.artifact.toFile(), false);
        this.transformer = Objects.requireNonNull(transformer, "transformer");
    }

    public Path artifact() {
        return artifact;
    }

    public boolean owns(String binaryName) {
        return jar.getJarEntry(binaryName.replace('.', '/') + ".class") != null;
    }

    /**
     * Links a legacy-owned class without running its static initializer.
     * Linkage failures are intentionally surfaced to the caller as evidence.
     */
    public Class<?> linkOwnedClass(String binaryName) throws ClassNotFoundException {
        if (!owns(binaryName)) {
            throw new ClassNotFoundException(binaryName + " is not owned by " + artifact);
        }
        return Class.forName(binaryName, false, this);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> loaded = findLoadedClass(name);
            if (loaded == null) {
                // Legacy Forge names may survive in constant-pool class
                // references even when all executable call sites are migrated.
                // Resolve the class identity against the current NeoForge namespace
                // without defining fake classes in either framework namespace.
                if (name.startsWith("net.minecraftforge.") && !owns(name)) {
                    String migrated = migrateLegacyFrameworkName(name);
                    if (!migrated.equals(name)) {
                        try {
                            loaded = getParent().loadClass(migrated);
                        } catch (ClassNotFoundException ignored) {
                            // Preserve the original lookup/error when this is a
                            // removed API that requires a semantic adapter.
                        }
                    }
                }
                if (loaded == null && (parentFirst(name) || !owns(name))) {
                    loaded = getParent().loadClass(name);
                } else if (loaded == null) {
                    try {
                        loaded = findClass(name);
                    } catch (ClassNotFoundException e) {
                        loaded = getParent().loadClass(name);
                    }
                }
            }
            if (resolve) resolveClass(loaded);
            return loaded;
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        var entry = jar.getJarEntry(name.replace('.', '/') + ".class");
        if (entry == null) throw new ClassNotFoundException(name);
        try (InputStream in = jar.getInputStream(entry)) {
            byte[] original = in.readAllBytes();
            LegacyClassTransformer.Result result = transformer.transform(name, original);
            byte[] bytes = result.bytes();
            return defineClass(name, bytes, 0, bytes.length);
        } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
        } catch (Exception e) {
            throw new ClassNotFoundException("Profile transformation failed for " + name, e);
        }
    }

    private static String migrateLegacyFrameworkName(String value) {
        // Binary-name form of the exact namespace rules used by the bytecode
        // transformer. Keep these segment-aware: eventbus.api.IEventBus moved
        // to bus.api.IEventBus, not bus.api.api.IEventBus.
        if (value.startsWith("net.minecraftforge.eventbus."))
            return "net.neoforged.bus." + value.substring("net.minecraftforge.eventbus.".length());
        if (value.startsWith("net.minecraftforge.fml."))
            return "net.neoforged.fml." + value.substring("net.minecraftforge.fml.".length());
        if (value.startsWith("net.minecraftforge.forgespi."))
            return "net.neoforged.neoforgespi." + value.substring("net.minecraftforge.forgespi.".length());
        if (value.startsWith("net.minecraftforge."))
            return "net.neoforged.neoforge." + value.substring("net.minecraftforge.".length());
        return value;
    }

    private static boolean parentFirst(String name) {
        for (String prefix : PARENT_FIRST) {
            if (name.startsWith(prefix)) return true;
        }
        return false;
    }

    @Override
    public void close() throws IOException {
        jar.close();
    }
}
