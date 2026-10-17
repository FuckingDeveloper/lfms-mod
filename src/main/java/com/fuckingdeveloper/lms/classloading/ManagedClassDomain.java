package com.fuckingdeveloper.lms.classloading;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Supplier;
import java.util.jar.JarFile;

/** Immutable, pre-definition ownership for one explicitly selected source profile. */
public final class ManagedClassDomain implements AutoCloseable {
    public record Artifact(Path path, boolean library, Supplier<? extends LegacyClassTransformer> transformer) {
        public Artifact { path = path.toAbsolutePath().normalize(); Objects.requireNonNull(transformer); }
    }
    private final Map<Path, ManagedLegacyClassLoader> artifacts = new LinkedHashMap<>();
    private final Map<String, ManagedLegacyClassLoader> classes = new java.util.concurrent.ConcurrentHashMap<>();
    private final Set<ManagedLegacyClassLoader> roots = new LinkedHashSet<>();
    private final Set<ManagedLegacyClassLoader> unavailable = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private List<ManagedLegacyClassLoader> owners = List.of();
    private volatile boolean closed;

    public ManagedClassDomain(List<Artifact> input, ClassLoader parent) throws IOException {
        var paths = new LinkedHashMap<Path, Artifact>();
        for (var artifact : input) {
            var previous = paths.putIfAbsent(artifact.path(), artifact);
            if (previous != null && previous.library() != artifact.library())
                throw new IllegalArgumentException("Artifact is both mod and library: " + artifact.path());
        }
        var canonical = new LinkedHashMap<Path, Path>();
        var libraries = new HashMap<String, Path>();
        var classPaths = new HashMap<String, Path>();
        // Validate all conflicts before constructing/publishing loaders or defining classes.
        for (var artifact : paths.values()) {
            Path path = artifact.path();
            Path owner = artifact.library() ? libraries.putIfAbsent(digest(path), path) : null;
            if (owner != null) { canonical.put(path, owner); continue; }
            canonical.put(path, path);
            try (var jar = new JarFile(path.toFile(), false)) {
                for (var entry : jar.stream().filter(e -> !e.isDirectory() && e.getName().endsWith(".class")).toList()) {
                    String name = entry.getName();
                    if (name.equals("module-info.class")) continue;
                    if (name.startsWith("META-INF/versions/"))
                        throw new IllegalArgumentException("Versioned managed class ownership is not yet supported: " + path + "!" + name);
                    name = name.substring(0, name.length() - 6).replace('/', '.');
                    Path previous = classPaths.putIfAbsent(name, path);
                    if (previous != null) throw new IllegalArgumentException("Conflicting managed class owner: " + name + " in " + previous + " and " + path);
                }
            }
        }
        var created = new LinkedHashMap<Path, ManagedLegacyClassLoader>();
        try {
            for (var artifact : paths.values()) if (canonical.get(artifact.path()).equals(artifact.path())) {
                var loader = new ManagedLegacyClassLoader(artifact.path(), parent, artifact.transformer().get());
                created.put(artifact.path(), loader);
                if (!artifact.library()) roots.add(loader);
            }
            canonical.forEach((path, owner) -> artifacts.put(path, created.get(owner)));
            classPaths.forEach((name, path) -> classes.put(name, created.get(path)));
            owners = List.copyOf(created.values());
            for (var loader : owners) loader.attachDomain(this);
        } catch (IOException | RuntimeException | Error failure) {
            for (var loader : created.values()) try { loader.close(); } catch (IOException close) { failure.addSuppressed(close); }
            throw failure;
        }
    }
    private static String digest(Path path) throws IOException {
        try (var in = Files.newInputStream(path)) {
            var digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[65536]; int count;
            while ((count = in.read(buffer)) != -1) digest.update(buffer, 0, count);
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException impossible) { throw new AssertionError(impossible); }
    }
    public ManagedLegacyClassLoader loader(Path artifact) {
        requireOpen();
        var loader = artifacts.get(artifact.toAbsolutePath().normalize());
        if (loader == null) throw new IllegalArgumentException("Artifact not admitted to source domain: " + artifact);
        return loader;
    }
    ManagedLegacyClassLoader owner(String name) throws ClassNotFoundException {
        if (closed) throw new ClassNotFoundException("Source class domain closed: " + name);
        var owner = classes.get(name);
        if (owner != null && unavailable.contains(owner)) throw new ClassNotFoundException("Source class owner blocked/closed: " + name + " in " + owner.artifact());
        return owner;
    }
    byte[] originalBytes(String name) throws IOException {
        try { var owner = owner(name.replace('/', '.')); return owner == null ? null : owner.localClassBytes(name); }
        catch (ClassNotFoundException failure) { throw new IOException(failure); }
    }
    List<ManagedLegacyClassLoader> resourceOwners(ManagedLegacyClassLoader caller) {
        requireOpen();
        if (unavailable.contains(caller)) throw new IllegalStateException("Source owner blocked/closed: " + caller.artifact());
        var result = new ArrayList<ManagedLegacyClassLoader>(); result.add(caller);
        for (var owner : owners) if (owner != caller && !unavailable.contains(owner)) result.add(owner);
        return result;
    }
    public void block(ManagedLegacyClassLoader owner) { unavailable.add(owner); }
    void released(ManagedLegacyClassLoader owner) throws IOException {
        unavailable.add(owner);
        if (unavailable.containsAll(roots)) close();
    }
    private void requireOpen() { if (closed) throw new IllegalStateException("Source class domain closed"); }
    @Override public synchronized void close() throws IOException {
        if (closed) return;
        closed = true; IOException failure = null;
        for (var owner : owners) try { owner.close(); } catch (IOException e) { if (failure == null) failure = e; else failure.addSuppressed(e); }
        classes.clear(); artifacts.clear(); roots.clear(); unavailable.clear(); owners = List.of();
        if (failure != null) throw failure;
    }
}
