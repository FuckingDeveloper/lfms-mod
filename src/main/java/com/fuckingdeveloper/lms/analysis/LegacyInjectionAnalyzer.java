package com.fuckingdeveloper.lms.analysis;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Static inspection of Mixin annotations and legacy JavaScript coremods. Nothing is executed. */
public final class LegacyInjectionAnalyzer {
    public record Injection(String method, String annotation, List<String> selectors, List<String> at) {}
    public record Mixin(String source, List<String> targets, List<String> mechanisms,
                        List<Injection> injections, String error) {}
    public record Coremod(String path, List<String> declaredTargets, List<String> referencedClasses,
                          List<String> asmApiCalls, List<String> mappedMethods,
                          List<String> builtMethodCalls, List<String> transformKinds) {}
    public record Report(List<Mixin> mixins, List<Coremod> coremods) {}

    private static final String MIXIN = "Lorg/spongepowered/asm/mixin/Mixin;";
    private static final String MIXIN_PREFIX = "Lorg/spongepowered/asm/mixin/";
    private static final String INJECTION_PREFIX = "Lorg/spongepowered/asm/mixin/injection/";
    private static final Pattern JS_CLASS_LITERAL = Pattern.compile(
            "['\\\"]((?:net\\.minecraft|net\\.minecraftforge|ic2)[A-Za-z0-9_.$]+)['\\\"]");
    private static final Pattern JS_TARGET = Pattern.compile(
            "['\\\"](?:class|className)['\\\"]\\s*:\\s*['\\\"]([^'\\\"]+)['\\\"]");
    private static final Pattern ASM_API_CALL = Pattern.compile(
            "ASMAPI\\.([A-Za-z0-9_]+)\\s*\\(");
    private static final Pattern MAP_METHOD = Pattern.compile(
            "ASMAPI\\.mapMethod\\s*\\(\\s*['\\\"]([^'\\\"]+)['\\\"]\\s*\\)");
    private static final Pattern BUILD_METHOD_CALL = Pattern.compile(
            "ASMAPI\\.buildMethodCall\\s*\\(([^;\\n]+)");
    private static final Pattern TRANSFORMER_TYPE = Pattern.compile(
            "['\\\"]target['\\\"]\\s*:\\s*\\{[^}]*['\\\"]type['\\\"]\\s*:\\s*['\\\"]([^'\\\"]+)['\\\"]",
            Pattern.DOTALL);

    public Report analyze(Path file, LegacyMetadataAnalyzer.Report metadata) throws IOException {
        List<Mixin> mixins = new ArrayList<>();
        List<Coremod> coremods = new ArrayList<>();
        try (ZipFile jar = new ZipFile(file.toFile())) {
            for (String name : metadata.mixinClasses()) {
                ZipEntry entry = jar.getEntry(name.replace('.', '/') + ".class");
                if (entry == null) {
                    mixins.add(new Mixin(name, List.of(), List.of(), List.of(), "Class missing from JAR"));
                    continue;
                }
                try (var in = jar.getInputStream(entry)) {
                    mixins.add(readMixin(name, in.readNBytes(4 * 1024 * 1024)));
                } catch (RuntimeException ex) {
                    mixins.add(new Mixin(name, List.of(), List.of(), List.of(), ex.toString()));
                }
            }
            for (String path : metadata.coremodScripts()) {
                ZipEntry entry = jar.getEntry(path);
                if (entry == null) {
                    coremods.add(new Coremod(path, List.of(), List.of(), List.of(), List.of(), List.of(), List.of()));
                    continue;
                }
                String source;
                try (var in = jar.getInputStream(entry)) {
                    source = new String(in.readNBytes(1024 * 1024), StandardCharsets.UTF_8);
                }
                Set<String> targets = matches(JS_TARGET, source, 1);
                Set<String> classes = matches(JS_CLASS_LITERAL, source, 1);
                Set<String> calls = matches(ASM_API_CALL, source, 1);
                Set<String> mappedMethods = matches(MAP_METHOD, source, 1);
                Set<String> builtCalls = matches(BUILD_METHOD_CALL, source, 1);
                Set<String> transformKinds = matches(TRANSFORMER_TYPE, source, 1);
                coremods.add(new Coremod(path, List.copyOf(targets), List.copyOf(classes),
                        List.copyOf(calls), List.copyOf(mappedMethods), List.copyOf(builtCalls),
                        List.copyOf(transformKinds)));
            }
        }
        return new Report(List.copyOf(mixins), List.copyOf(coremods));
    }

    private static Set<String> matches(Pattern pattern, String source, int group) {
        Set<String> values = new LinkedHashSet<>();
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) values.add(matcher.group(group));
        return values;
    }

    private static Mixin readMixin(String name, byte[] bytes) {
        List<String> targets = new ArrayList<>();
        Set<String> mechanisms = new LinkedHashSet<>();
        List<Injection> injections = new ArrayList<>();
        new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                if (!MIXIN.equals(descriptor)) {
                    recordMixinMechanism(descriptor, mechanisms);
                    return null;
                }
                mechanisms.add("Mixin");
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
            public FieldVisitor visitField(int access, String fieldName, String descriptor,
                                           String signature, Object value) {
                return new FieldVisitor(Opcodes.ASM9) {
                    @Override
                    public AnnotationVisitor visitAnnotation(String annotation, boolean visible) {
                        recordMixinMechanism(annotation, mechanisms);
                        return null;
                    }
                };
            }

            @Override
            public MethodVisitor visitMethod(int access, String methodName, String descriptor,
                                             String signature, String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    public AnnotationVisitor visitAnnotation(String annotation, boolean visible) {
                        recordMixinMechanism(annotation, mechanisms);
                        if (!annotation.startsWith(INJECTION_PREFIX)) return null;
                        String shortName = shortAnnotation(annotation);
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
        return new Mixin(name, List.copyOf(targets), List.copyOf(mechanisms), List.copyOf(injections), "");
    }

    private static void recordMixinMechanism(String descriptor, Set<String> mechanisms) {
        if (descriptor.startsWith(MIXIN_PREFIX)) mechanisms.add(shortAnnotation(descriptor));
    }

    private static String shortAnnotation(String descriptor) {
        int slash = descriptor.lastIndexOf('/');
        int end = descriptor.endsWith(";") ? descriptor.length() - 1 : descriptor.length();
        return descriptor.substring(slash + 1, end);
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
