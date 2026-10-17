package com.fuckingdeveloper.lms.classloading;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
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
            "net.neoforged.", "com.fuckingdeveloper.lms.", "org.slf4j.", "org.objectweb.asm.");

    private volatile boolean closed;
    private final Path artifact;
    private final JarFile jar;
    private final List<JarFile> dependencyJars;
    private final LegacyClassTransformer transformer;
    private final java.util.Map<Path,ManagedLegacyClassLoader> dependencyOwners;
    private final java.util.Map<String, byte[]> definedClassBytes = new java.util.concurrent.ConcurrentHashMap<>();

    public ManagedLegacyClassLoader(Path artifact, ClassLoader targetLoader) throws IOException {
        this(artifact, List.of(), targetLoader, (name, bytes) ->
                LegacyClassTransformer.Result.unchanged(bytes, "identity profile transformer"));
    }

    public ManagedLegacyClassLoader(Path artifact, ClassLoader targetLoader,
                                    LegacyClassTransformer transformer) throws IOException {
        this(artifact, List.of(), targetLoader, transformer);
    }

    public ManagedLegacyClassLoader(Path artifact, List<Path> dependencies, ClassLoader targetLoader,
                                    LegacyClassTransformer transformer) throws IOException {
        this(artifact,dependencies,targetLoader,transformer,java.util.Map.of());
    }

    public ManagedLegacyClassLoader(Path artifact,List<Path> dependencies,ClassLoader targetLoader,
            LegacyClassTransformer transformer,java.util.Map<Path,ManagedLegacyClassLoader> owners) throws IOException {
        super(Objects.requireNonNull(targetLoader, "targetLoader"));
        this.dependencyOwners = new java.util.HashMap<>();
        for(var entry:owners.entrySet()) {
            Path normalized=entry.getKey().toAbsolutePath().normalize();
            if(dependencies.stream().noneMatch(path->path.toAbsolutePath().normalize().equals(normalized)))
                throw new IllegalArgumentException("Class owner is not an admitted dependency: "+normalized);
            this.dependencyOwners.put(normalized,Objects.requireNonNull(entry.getValue()));
        }
        this.artifact = artifact.toAbsolutePath().normalize();
        this.jar = new JarFile(this.artifact.toFile(), false);
        this.dependencyJars = new ArrayList<>();
        try {
            for (Path dependency : dependencies) {
                this.dependencyJars.add(new JarFile(dependency.toAbsolutePath().normalize().toFile(), false));
            }
        } catch (IOException failure) {
            for (JarFile dependencyJar : dependencyJars) try { dependencyJar.close(); } catch (IOException ignored) {}
            jar.close();
            throw failure;
        }
        this.transformer = Objects.requireNonNull(transformer, "transformer");
        this.transformer.bindManagedClassBytes(this::readOwnedClassBytes);
    }

    public Path artifact() {
        return artifact;
    }

    private byte[] readOwnedClassBytes(String internalName) throws IOException {
        String entryName = internalName + ".class";
        var entry = jar.getJarEntry(entryName);
        if (entry != null) {
            try (InputStream in = jar.getInputStream(entry)) {
                return in.readAllBytes();
            }
        }
        for (JarFile dependencyJar : dependencyJars) {
            entry = dependencyJar.getJarEntry(entryName);
            if (entry != null) {
                try (InputStream in = dependencyJar.getInputStream(entry)) {
                    return in.readAllBytes();
                }
            }
        }
        return null;
    }

    /** Standard resources retain the same admitted graph as managed class ownership. */
    @Override public java.net.URL getResource(String name) {
        Objects.requireNonNull(name); requireOpen();
        var own=findResource(name); return own==null?getParent().getResource(name):own;
    }
    @Override public java.util.Enumeration<java.net.URL> getResources(String name) throws IOException {
        Objects.requireNonNull(name); requireOpen();
        var resources=new java.util.LinkedHashMap<String,java.net.URL>();
        for(var items= findResources(name);items.hasMoreElements();){var url=items.nextElement();resources.putIfAbsent(url.toExternalForm(),url);}
        for(var items=getParent().getResources(name);items.hasMoreElements();){var url=items.nextElement();resources.putIfAbsent(url.toExternalForm(),url);}
        return java.util.Collections.enumeration(resources.values());
    }
    @Override protected java.net.URL findResource(String name) {
        requireOpen();
        if(jar.getJarEntry(name)!=null)return resourceUrl(jar,name);
        for(var dependency:dependencyJars)if(dependency.getJarEntry(name)!=null)return resourceUrl(dependency,name);
        return null;
    }
    @Override protected java.util.Enumeration<java.net.URL> findResources(String name) {
        requireOpen();var resources=new java.util.ArrayList<java.net.URL>();
        if(jar.getJarEntry(name)!=null)resources.add(resourceUrl(jar,name));
        for(var dependency:dependencyJars)if(dependency.getJarEntry(name)!=null)resources.add(resourceUrl(dependency,name));
        return java.util.Collections.enumeration(resources);
    }
    private void requireOpen(){if(closed)throw new IllegalStateException("Managed resource loader is closed: "+artifact);}
    private java.net.URL resourceUrl(JarFile source,String name) {
        try {
            String entry=new java.net.URI(null,null,"/"+name,null).getRawPath().substring(1);
            String location="jar:"+Path.of(source.getName()).toUri().toASCIIString()+"!/"+entry;
            return new java.net.URL(null,location,new java.net.URLStreamHandler(){
                protected java.net.URLConnection openConnection(java.net.URL url){return new java.net.URLConnection(url){
                    public void connect() throws IOException {if(closed)throw new IOException("Managed resource loader closed: "+artifact);connected=true;}
                    public InputStream getInputStream() throws IOException {connect();var entry=source.getJarEntry(name);if(entry==null)throw new java.io.FileNotFoundException(name);return source.getInputStream(entry);}
                };}
            });
        }catch(java.net.MalformedURLException|java.net.URISyntaxException failure){throw new IllegalArgumentException("Invalid managed resource name: "+name,failure);}
    }

    /** Immutable pre-transformation evidence from this loader's admitted dependency graph. */
    public byte[] originalClassBytes(String binaryName) throws IOException {
        return readOwnedClassBytes(binaryName.replace('.', '/'));
    }

    /** Authoritative metadata for a class already defined by this exact loader. */
    public byte[] definedClassBytes(String binaryName) {
        byte[] bytes = definedClassBytes.get(binaryName);
        if (bytes == null) throw new IllegalStateException("Class has not been defined by this managed loader: " + binaryName);
        return bytes.clone();
    }

    public boolean owns(String binaryName) {
        String entry = binaryName.replace('.', '/') + ".class";
        if (jar.getJarEntry(entry) != null) return true;
        for (JarFile dependencyJar : dependencyJars) if (dependencyJar.getJarEntry(entry) != null) return true;
        return false;
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
                    // A ClassLoader cannot satisfy a request for binary name A
                    // by returning a Class whose binary name is B. Namespace
                    // migration must therefore be completed in transformed
                    // bytecode, never aliased here.
                    throw new ClassNotFoundException(
                            "Residual legacy Forge type escaped transformation: " + name);
                }
                if (loaded == null && (parentFirst(name) || !owns(name))) {
                    loaded = getParent().loadClass(name);
                } else if (loaded == null) {
                    // Owned classes must never silently fall back to the parent.
                    // A CNFE here can represent a transformation/linkage failure
                    // wrapped by findClass; parent fallback would erase the
                    // evidence and misreport the owned class as simply missing.
                    ManagedLegacyClassLoader dependencyOwner = dependencyOwner(name);
                    loaded = dependencyOwner == null ? findClass(name) : dependencyOwner.loadClass(name);
                }
            }
            if (resolve) resolveClass(loaded);
            return loaded;
        }
    }

    public boolean providesArtifact(Path candidate) {
        Path normalized=candidate.toAbsolutePath().normalize();
        return artifact.equals(normalized)||dependencyJars.stream().anyMatch(jar->Path.of(jar.getName()).equals(normalized));
    }
    private ManagedLegacyClassLoader dependencyOwner(String binaryName) {
        String entry=binaryName.replace('.','/')+".class";
        if(jar.getJarEntry(entry)!=null)return null;
        for(JarFile dependency:dependencyJars)if(dependency.getJarEntry(entry)!=null)
            return dependencyOwners.get(Path.of(dependency.getName()));
        return null;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String entryName = name.replace('.', '/') + ".class";
        JarFile source = jar;
        var entry = source.getJarEntry(entryName);
        if (entry == null) {
            source = null;
            for (JarFile dependencyJar : dependencyJars) {
                var candidate = dependencyJar.getJarEntry(entryName);
                if (candidate != null) { source = dependencyJar; entry = candidate; break; }
            }
        }
        if (entry == null || source == null) throw new ClassNotFoundException(name);
        try (InputStream in = source.getInputStream(entry)) {
            byte[] original = in.readAllBytes();
            LegacyClassTransformer.Result result = transformer.transform(name, original);
            byte[] bytes = result.bytes();
            Class<?> defined = defineClass(name, bytes, 0, bytes.length);
            definedClassBytes.put(name, bytes.clone());
            return defined;
        } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
        } catch (Exception e) {
            throw new ClassNotFoundException(
                    "Profile transformation failed for owned class " + name
                            + " from " + artifact + ": " + e.getMessage(), e);
        }
    }

    private static boolean parentFirst(String name) {
        for (String prefix : PARENT_FIRST) {
            if (name.startsWith(prefix)) return true;
        }
        return false;
    }

    @Override
    public void close() throws IOException {
        closed=true;
        dependencyOwners.clear();
        IOException failure = null;
        for (JarFile dependencyJar : dependencyJars) try { dependencyJar.close(); } catch (IOException e) { failure = e; }
        try { jar.close(); } catch (IOException e) { failure = e; }
        if (failure != null) throw failure;
    }
}
