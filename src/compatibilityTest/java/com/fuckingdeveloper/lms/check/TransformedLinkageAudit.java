package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer;
import java.nio.file.*;
import java.util.*;
import java.util.jar.JarFile;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Offline inventory: no legacy class definition/initialization and no compatibility claim. */
public final class TransformedLinkageAudit {
    record Gap(String kind, String symbol, String site) {}
    private final Map<String, ClassNode> owned = new HashMap<>();
    private final Map<String, Optional<ClassNode>> target = new HashMap<>();
    private final List<Gap> gaps = new ArrayList<>();
    private final Map<String, Boolean> resolutions = new HashMap<>();
    public static void main(String[] args) throws Exception {
        Path directory = Path.of(args.length == 0 ? "legacy-mods" : args[0]);
        Path output = Path.of(args.length < 2 ? "/tmp/lms-compatibility-audit.tsv" : args[1]);
        var originals = new LinkedHashMap<String, byte[]>();
        var origins = new HashMap<String, String>();
        try (var files = Files.list(directory)) {
            for (var path : files.filter(p -> p.toString().endsWith(".jar")).sorted().toList()) {
                try (var jar = new JarFile(path.toFile())) {
                    for (var entry : jar.stream().filter(e -> e.getName().endsWith(".class") && !e.getName().startsWith("META-INF/")).toList()) {
                        try (var stream = jar.getInputStream(entry)) {
                            String name = entry.getName().substring(0, entry.getName().length() - 6);
                            byte[] bytes = stream.readAllBytes();
                            if (originals.putIfAbsent(name, bytes) != null)
                                throw new IllegalStateException("Ambiguous audit class graph: " + name);
                            origins.put(name, path.getFileName().toString());
                        }
                    }
                }
            }
        }
        var audit = new TransformedLinkageAudit();
        var transformer = new Forge1192LegacyClassTransformer(); transformer.bindManagedClassBytes(originals::get);
        int attempted = 0;
        for (var entry : originals.entrySet()) {
            try {
                var result = transformer.transform(entry.getKey().replace('/', '.'), entry.getValue());
                var node = new ClassNode(); new ClassReader(result.bytes()).accept(node, 0); audit.owned.put(node.name, node);
            } catch (Exception | LinkageError failure) {
                // Every failure is recorded; diagnostic traversal is not functional support.
                audit.gaps.add(new Gap("TRANSFORM_FAILURE", entry.getKey(), origins.get(entry.getKey()) + ": " + failure));
                var failed = new ClassNode(); failed.name = entry.getKey(); audit.owned.put(entry.getKey(), failed);
            }
            if (++attempted % 250 == 0) System.out.println("AUDIT progress=" + attempted + "/" + originals.size());
        }
        for (var node : audit.owned.values()) audit.inspect(node);
        var grouped = new TreeMap<String, List<String>>();
        for (var gap : audit.gaps) grouped.computeIfAbsent(gap.kind + "\t" + gap.symbol, k -> new ArrayList<>()).add(gap.site);
        var lines = new ArrayList<String>(); lines.add("kind\tsymbol\tsites\texample");
        grouped.forEach((symbol, sites) -> lines.add(symbol + "\t" + sites.size() + "\t" + sites.getFirst().replace('\n', ' ').replace('\t', ' ')));
        Files.write(output, lines);
        System.out.println("AUDIT inventory only: classes=" + originals.size() + " groupedGaps=" + grouped.size()
                + " sites=" + audit.gaps.size() + " transformationMs=" + transformer.transformationMillis() + " report=" + output);
    }
    private void inspect(ClassNode node) {
        if (node.superName != null) type(node.superName, node.name + " SUPER");
        for (String iface : node.interfaces) type(iface, node.name + " INTERFACE");
        for (var method : node.methods) {
            String site = node.name + "#" + method.name + method.desc;
            for (var instruction : method.instructions) {
                if (instruction instanceof MethodInsnNode call) member(call.owner, call.name, call.desc, false, call.getOpcode() == Opcodes.INVOKESTATIC, site);
                else if (instruction instanceof FieldInsnNode field) member(field.owner, field.name, field.desc, true,
                        field.getOpcode() == Opcodes.GETSTATIC || field.getOpcode() == Opcodes.PUTSTATIC, site);
                else if (instruction instanceof TypeInsnNode typed) type(typed.desc, site);
                else if (instruction instanceof LdcInsnNode constant) constant(constant.cst, site);
                else if (instruction instanceof InvokeDynamicInsnNode dynamic) {
                    constant(dynamic.bsm, site); for (Object arg : dynamic.bsmArgs) constant(arg, site);
                }
            }
        }
    }
    private void constant(Object value, String site) {
        if (value instanceof Handle handle) {
            int tag = handle.getTag(); boolean field = tag <= Opcodes.H_PUTSTATIC;
            member(handle.getOwner(), handle.getName(), handle.getDesc(), field,
                    tag == Opcodes.H_GETSTATIC || tag == Opcodes.H_PUTSTATIC || tag == Opcodes.H_INVOKESTATIC, site);
        } else if (value instanceof ConstantDynamic dynamic) {
            constant(dynamic.getBootstrapMethod(), site);
            for (int i = 0; i < dynamic.getBootstrapMethodArgumentCount(); i++) constant(dynamic.getBootstrapMethodArgument(i), site);
        } else if (value instanceof Type typed && typed.getSort() == Type.OBJECT) type(typed.getInternalName(), site);
    }
    private void type(String name, String site) {
        if (!name.startsWith("[") && lookup(name) == null) gaps.add(new Gap("CLASS_MISSING", name, site));
    }
    private void member(String owner, String name, String descriptor, boolean field, boolean isStatic, String site) {
        if (owner.startsWith("[")) return; // JVM array clone/member rules are separate.
        String symbol = owner + "#" + name + descriptor;
        String key = (field ? "F" : "M") + isStatic + symbol;
        boolean found = resolutions.computeIfAbsent(key, k -> resolves(owner, name, descriptor, field, isStatic, new HashSet<>()));
        if (!found) gaps.add(new Gap(lookup(owner) == null ? "OWNER_MISSING" : field ? "FIELD_MISSING" : "METHOD_MISSING", symbol, site));
    }
    private boolean resolves(String owner, String name, String descriptor, boolean field, boolean isStatic, Set<String> seen) {
        if (!seen.add(owner)) return false;
        var node = lookup(owner); if (node == null) return false;
        if (field) {
            for (var member : node.fields) if (member.name.equals(name) && member.desc.equals(descriptor)
                    && ((member.access & Opcodes.ACC_STATIC) != 0) == isStatic) return true;
        } else {
            for (var member : node.methods) {
                boolean polymorphic = (owner.equals("java/lang/invoke/MethodHandle") || owner.equals("java/lang/invoke/VarHandle"))
                        && (member.access & (Opcodes.ACC_NATIVE | Opcodes.ACC_VARARGS)) == (Opcodes.ACC_NATIVE | Opcodes.ACC_VARARGS);
                if (member.name.equals(name) && (member.desc.equals(descriptor) || polymorphic)
                        && ((member.access & Opcodes.ACC_STATIC) != 0) == isStatic) return true;
            }
            if (name.equals("<init>")) return false;
        }
        if (node.superName != null && resolves(node.superName, name, descriptor, field, isStatic, seen)) return true;
        for (String iface : node.interfaces) if (resolves(iface, name, descriptor, field, isStatic, seen)) return true;
        return false;
    }
    private ClassNode lookup(String name) {
        if (owned.containsKey(name)) return owned.get(name);
        return target.computeIfAbsent(name, key -> {
            try (var stream = TransformedLinkageAudit.class.getClassLoader().getResourceAsStream(key + ".class")) {
                if (stream == null) return Optional.empty();
                var node = new ClassNode(); new ClassReader(stream).accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                return Optional.of(node);
            } catch (java.io.IOException failure) { throw new java.io.UncheckedIOException(failure); }
        }).orElse(null);
    }
}
