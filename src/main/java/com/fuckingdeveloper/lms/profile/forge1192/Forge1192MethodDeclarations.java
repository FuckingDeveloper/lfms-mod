package com.fuckingdeveloper.lms.profile.forge1192;

import java.util.*;
import java.util.function.Function;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

/** Source-only class declaration lookup. Instances belong to one fixed raw evidence graph. */
public final class Forge1192MethodDeclarations {
    private record Header(String parent, Set<String> methods) {}
    private record Query(String owner, String name, String descriptor) {}
    private final Function<String, byte[]> managed;
    private final Map<String, Optional<Header>> headers = new HashMap<>();
    private final Map<Query, Optional<String>> declarations = new HashMap<>();
    public Forge1192MethodDeclarations(Function<String, byte[]> managed) { this.managed = Objects.requireNonNull(managed); }
    public synchronized String owner(String owner, String name, String descriptor) {
        return declarations.computeIfAbsent(new Query(owner,name,descriptor), key -> Optional.ofNullable(resolve(key))).orElse(null);
    }
    private String resolve(Query query) {
        String current = query.owner(); var seen = new HashSet<String>();
        while (current != null && seen.add(current)) {
            var header = header(current);
            if (header == null) return null;
            if (header.methods().contains(query.name() + query.descriptor())) return current;
            current = header.parent();
        }
        return null;
    }
    public synchronized boolean subtype(String owner, String root) {
        var seen = new HashSet<String>();
        while (owner != null && seen.add(owner)) {
            if (owner.equals(root)) return true;
            var header = header(owner); if (header == null) return false; owner = header.parent();
        }
        return false;
    }
    private Header header(String owner) {
        return headers.computeIfAbsent(owner, name -> {
                byte[] bytes = managed.apply(name);
                if (bytes == null) bytes = Forge1192SourceHierarchyIndex.methodBytes(name);
                if (bytes == null) return Optional.empty();
                var node = new ClassNode(); new ClassReader(bytes).accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                var methods = new HashSet<String>();
                node.methods.forEach(method -> methods.add(method.name + method.desc));
                return Optional.of(new Header(node.superName, Set.copyOf(methods)));
            }).orElse(null);
    }
    public synchronized void clear() { headers.clear(); declarations.clear(); }
}
