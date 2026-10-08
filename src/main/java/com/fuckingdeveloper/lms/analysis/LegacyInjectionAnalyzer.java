package com.fuckingdeveloper.lms.analysis;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Reads Mixin annotations and legacy coremod source without loading or executing them. */
public final class LegacyInjectionAnalyzer {
    public record Injection(String method, String annotation, List<String> selectors, List<String> at) {}
    public record Mixin(String source, List<String> targets, List<Injection> injections, String error) {}
    public record Coremod(String path, List<String> declaredTargets, String sourcePreview) {}
    public record Report(List<Mixin> mixins, List<Coremod> coremods) {}

    private static final String MIXIN = "Lorg/spongepowered/asm/mixin/Mixin;";
    private static final Pattern JS_TARGET = Pattern.compile(
            "['\\\"](?:class|className)['\\\"]\\s*:\\s*['\\\"]([^'\\\"]+)['\\\"]");

    public Report analyze(Path file, LegacyMetadataAnalyzer.Report metadata) throws IOException {
        List<Mixin> mixins = new ArrayList<>();
        List<Coremod> coremods = new ArrayList<>();
        try (ZipFile jar = new ZipFile(file.toFile())) {
            for (String name : metadata.mixinClasses()) {
                ZipEntry entry = jar.getEntry(name.replace('.', '/') + ".class");
                if (entry == null) {
                    mixins.add(new Mixin(name, List.of(), List.of(), "Class missing from JAR"));
                    continue;
                }
                try (var in = jar.getInputStream(entry)) {
                    mixins.add(readMixin(name, in.readNBytes(4 * 1024 * 1024)));
                } catch (RuntimeException ex) {
                    mixins.add(new Mixin(name, List.of(), List.of(), ex.toString()));
                }
            }
            for (String path : metadata.coremodScripts()) {
                ZipEntry entry = jar.getEntry(path);
                if (entry == null) {
                    coremods.add(new Coremod(path, List.of(), "MISSING"));
                    continue;
                }
                String source;
                try (var in = jar.getInputStream(entry)) {
                    source = new String(in.readNBytes(1024 * 1024), StandardCharsets.UTF_8);
                }
                List<String> targets = new ArrayList<>();
                Matcher matcher = JS_TARGET.matcher(source);
                while (matcher.find()) targets.add(matcher.group(1));
                // Preview is diagnostic evidence, not executable JavaScript.
                coremods.add(new Coremod(path, List.copyOf(targets),
                        source.substring(0, Math.min(1500, source.length())).replace('\n', ' ')));
            }
        }
        return new Report(List.copyOf(mixins), List.copyOf(coremods));
    }

    private static Mixin readMixin(String name, byte[] bytes) {
        List<String> targets = new ArrayList<>();
        List<Injection> injections = new ArrayList<>();
        new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                if (!MIXIN.equals(descriptor)) return null;
                return new AnnotationVisitor(Opcodes.ASM9) {
                    @Override
                    public AnnotationVisitor visitArray(String key) {
                        if (!"value".equals(key) && !"targets".equals(key)) return null;
                        return new AnnotationVisitor(Opcodes.ASM9) {
                            @Override
                            public void visit(String ignored, Object value) {
                                if (value instanceof Type type) targets.add(type.getClassName());
                                else if (value instanceof String text) targets.add(text);
                            }
                        };
                    }
                    @Override
                    public void visit(String key, Object value) {
                        if ("value".equals(key) && value instanceof Type type) targets.add(type.getClassName());
                        if ("targets".equals(key) && value instanceof String text) targets.add(text);
                    }
                };
            }

            @Override
            public MethodVisitor visitMethod(int access, String methodName, String descriptor,
                                             String signature, String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    public AnnotationVisitor visitAnnotation(String annotation, boolean visible) {
                        String shortName = annotation.substring(annotation.lastIndexOf('/') + 1,
                                annotation.length() - 1);
                        if (!annotation.startsWith("Lorg/spongepowered/asm/mixin/injection/")) return null;
                        List<String> selectors = new ArrayList<>();
                        List<String> at = new ArrayList<>();
                        injections.add(new Injection(methodName + descriptor, shortName, selectors, at));
                        return new AnnotationVisitor(Opcodes.ASM9) {
                            @Override
                            public AnnotationVisitor visitArray(String key) {
                                if (!"method".equals(key) && !"at".equals(key)) return null;
                                return new AnnotationVisitor(Opcodes.ASM9) {
                                    @Override
                                    public void visit(String ignored, Object value) {
                                        if ("method".equals(key)) selectors.add(String.valueOf(value));
                                    }
                                    @Override
                                    public AnnotationVisitor visitAnnotation(String ignored, String desc) {
                                        return "at".equals(key) ? atVisitor(at) : null;
                                    }
                                };
                            }
                            @Override
                            public void visit(String key, Object value) {
                                if ("method".equals(key)) selectors.add(String.valueOf(value));
                            }
                            @Override
                            public AnnotationVisitor visitAnnotation(String key, String desc) {
                                return "at".equals(key) ? atVisitor(at) : null;
                            }
                        };
                    }
                };
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return new Mixin(name, List.copyOf(targets), List.copyOf(injections), "");
    }

    private static AnnotationVisitor atVisitor(List<String> at) {
        return new AnnotationVisitor(Opcodes.ASM9) {
            String value = "";
            String target = "";
            @Override
            public void visit(String key, Object object) {
                if ("value".equals(key)) value = String.valueOf(object);
                if ("target".equals(key)) target = String.valueOf(object);
            }
            @Override
            public void visitEnd() {
                at.add(value + (target.isEmpty() ? "" : ":" + target));
            }
        };
    }
}
