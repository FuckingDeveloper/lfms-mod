package com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle;

import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarFile;
import net.neoforged.fml.loading.modscan.ModAnnotation;
import net.neoforged.fml.loading.modscan.ModClassVisitor;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

/** Non-executing source metadata projected with the transformer's exact namespace rule. */
public final class Forge1192SourceScan {
    private Forge1192SourceScan() {}
    public static ModFileScanData read(Path artifact, List<IModFileInfo> info) throws IOException {
        var data = new ModFileScanData();
        var remapper = new Remapper(Opcodes.ASM9) {
            @Override public String map(String name) { return Forge1192LegacyClassTransformer.projectInternalName(name); }
        };
        try (var jar = new JarFile(artifact.toFile(), false)) {
            for (var entry : jar.stream().filter(e -> !e.isDirectory() && e.getName().endsWith(".class")).sorted(Comparator.comparing(java.util.jar.JarEntry::getName)).toList()) {
                if (entry.getName().equals("module-info.class")) continue;
                try (var in = jar.getInputStream(entry)) {
                    var visitor = new ModClassVisitor();
                    new ClassReader(in).accept(new ClassRemapper(visitor, remapper), ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                    visitor.buildData(data.getClasses(), data.getAnnotations());
                } catch (IllegalArgumentException failure) { throw new IOException("Invalid source scan: " + artifact + "!" + entry.getName(), failure); }
            }
        }
        var classes = new LinkedHashSet<ModFileScanData.ClassData>();
        for (var value : data.getClasses()) classes.add(new ModFileScanData.ClassData(value.clazz(), value.parent(), Set.copyOf(value.interfaces())));
        var annotations = new LinkedHashSet<ModFileScanData.AnnotationData>();
        for (var value : data.getAnnotations()) annotations.add(new ModFileScanData.AnnotationData(value.annotationType(), value.targetType(), value.clazz(), value.memberName(), freezeMap(value.annotationData())));
        return new Snapshot(Collections.unmodifiableSet(classes), Collections.unmodifiableSet(annotations), List.copyOf(info));
    }
    private static Map<String,Object> freezeMap(Map<String,Object> input) {
        var output = new LinkedHashMap<String,Object>(); input.forEach((key,value) -> output.put(key, freeze(value))); return Collections.unmodifiableMap(output);
    }
    private static Object freeze(Object value) {
        if (value instanceof Map<?,?> map) { var result = new LinkedHashMap<Object,Object>(); map.forEach((k,v) -> result.put(k,freeze(v))); return Collections.unmodifiableMap(result); }
        if (value instanceof List<?> list) return list.stream().map(Forge1192SourceScan::freeze).toList();
        if (value != null && value.getClass().isArray()) { var result = java.lang.reflect.Array.newInstance(value.getClass().getComponentType(),java.lang.reflect.Array.getLength(value)); System.arraycopy(value,0,result,0,java.lang.reflect.Array.getLength(value)); return result; }
        if (value == null || value instanceof String || value instanceof Number || value instanceof Boolean || value instanceof Character || value instanceof Type || value instanceof ModAnnotation.EnumHolder) return value;
        throw new IllegalArgumentException("Unsupported source scan value: " + value.getClass());
    }
    private static final class Snapshot extends ModFileScanData {
        private final Set<ClassData> classes;
        private final Set<AnnotationData> annotations;
        private final List<IModFileInfo> info;
        Snapshot(Set<ClassData> classes, Set<AnnotationData> annotations, List<IModFileInfo> info) { this.classes=classes; this.annotations=annotations; this.info=info; }
        @Override public Set<ClassData> getClasses() { return classes; }
        @Override public Set<AnnotationData> getAnnotations() {
            var copy = new LinkedHashSet<AnnotationData>();
            for(var value : annotations) copy.add(new AnnotationData(value.annotationType(),value.targetType(),value.clazz(),value.memberName(),freezeMap(value.annotationData())));
            return Collections.unmodifiableSet(copy);
        }
        @Override public List<IModFileInfo> getIModInfoData() { return info; }
        @Override public void addModFileInfo(IModFileInfo ignored) { throw new UnsupportedOperationException("Immutable source scan"); }
    }
}
