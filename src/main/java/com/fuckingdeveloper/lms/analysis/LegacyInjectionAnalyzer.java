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
    public record CoremodMethodTarget(String owner, String method, String descriptor) {}
    public record CoremodHookCall(String owner, String method, String descriptor, String invocationType) {}
    public record CoremodOperation(String kind, String detail) {}
    public record CoremodTransform(String name, CoremodMethodTarget target, List<String> mappedMethods,
                                   List<CoremodHookCall> hookCalls, List<CoremodOperation> operations) {}
    public record Coremod(String path, List<String> declaredTargets, List<String> referencedClasses,
                          List<String> asmApiCalls, List<String> mappedMethods,
                          List<String> builtMethodCalls, List<String> transformKinds,
                          List<CoremodMethodTarget> methodTargets, List<CoremodHookCall> hookCalls,
                          List<CoremodTransform> transforms) {}
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
    private static final Pattern TRANSFORMER_TYPE = Pattern.compile(
            "['\\\"]target['\\\"]\\s*:\\s*\\{[^}]*['\\\"]type['\\\"]\\s*:\\s*['\\\"]([^'\\\"]+)['\\\"]",
            Pattern.DOTALL);
    private static final Pattern METHOD_TARGET_BLOCK = Pattern.compile(
            "['\\\"]target['\\\"]\\s*:\\s*\\{(.*?)\\}", Pattern.DOTALL);
    private static final Pattern JS_PROPERTY = Pattern.compile(
            "['\\\"]?(class|className|methodName|method|name|methodDesc|descriptor|desc)['\\\"]?\\s*:\\s*(?:ASMAPI\\.mapMethod\\s*\\(\\s*)?['\\\"]([^'\\\"]+)['\\\"]",
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
                    coremods.add(new Coremod(path, List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of()));
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
                List<String> builtCalls = extractFunctionCalls(source, "ASMAPI.buildMethodCall");
                Set<String> transformKinds = matches(TRANSFORMER_TYPE, source, 1);
                List<CoremodMethodTarget> methodTargets = extractMethodTargets(source);
                List<CoremodHookCall> hookCalls = extractHookCalls(builtCalls);
                List<CoremodTransform> transforms = extractTransforms(source);
                coremods.add(new Coremod(path, List.copyOf(targets), List.copyOf(classes),
                        List.copyOf(calls), List.copyOf(mappedMethods), builtCalls,
                        List.copyOf(transformKinds), methodTargets, hookCalls, transforms));
            }
        }
        return new Report(List.copyOf(mixins), List.copyOf(coremods));
    }

    /**
     * Extracts JavaScript call arguments without executing the script.
     * Regex cannot safely parse descriptors because buildMethodCall arguments may contain
     * nested parentheses; this scanner tracks strings and balanced delimiters instead.
     */
    private static List<CoremodTransform> extractTransforms(String source) {
        List<MatcherSnapshot> matches = new ArrayList<>();
        Matcher matcher = METHOD_TARGET_BLOCK.matcher(source);
        while (matcher.find()) {
            CoremodMethodTarget target = parseMethodTargetBlock(matcher.group(1));
            if (target != null) matches.add(new MatcherSnapshot(matcher.start(), target));
        }
        List<CoremodTransform> transforms = new ArrayList<>();
        for (int i = 0; i < matches.size(); i++) {
            MatcherSnapshot current = matches.get(i);
            int segmentEnd = i + 1 < matches.size() ? matches.get(i + 1).start() : source.length();
            String segment = source.substring(current.start(), segmentEnd);
            List<String> mappedMethods = List.copyOf(matches(MAP_METHOD, segment, 1));
            List<CoremodHookCall> hooks = extractHookCalls(extractFunctionCalls(segment, "ASMAPI.buildMethodCall"));
            List<CoremodOperation> operations = extractTransformOperations(segment);
            transforms.add(new CoremodTransform("transform-" + (i + 1), current.target(), mappedMethods, hooks, operations));
        }
        return List.copyOf(transforms);
    }

    private static List<CoremodOperation> extractTransformOperations(String source) {
        List<CoremodOperation> operations = new ArrayList<>();
        Pattern operation = Pattern.compile(
                "\\.(insertBefore|insert|add|remove|setOpcode)\\s*\\(|\\b(opcode|owner|name|desc)\\s*=");
        Matcher matcher = operation.matcher(source);
        while (matcher.find()) {
            String kind = matcher.group(1) != null ? matcher.group(1) : "instruction-" + matcher.group(2);
            int end = Math.min(source.length(), matcher.start() + 220);
            String detail = source.substring(matcher.start(), end)
                    .replaceAll("\\s+", " ").trim();
            operations.add(new CoremodOperation(kind, detail));
        }
        return List.copyOf(operations);
    }

    private record MatcherSnapshot(int start, CoremodMethodTarget target) {}

    private static CoremodMethodTarget parseMethodTargetBlock(String block) {
        if (!block.matches("(?s).*['\\\"]?type['\\\"]?\\s*:\\s*['\\\"]METHOD['\\\"].*")) return null;
        String owner = "", method = "", descriptor = "";
        Matcher properties = JS_PROPERTY.matcher(block);
        while (properties.find()) {
            switch (properties.group(1)) {
                case "class", "className" -> owner = properties.group(2);
                case "methodName", "method", "name" -> method = properties.group(2);
                case "methodDesc", "descriptor", "desc" -> descriptor = properties.group(2);
            }
        }
        return new CoremodMethodTarget(owner, method, descriptor);
    }

    private static List<CoremodHookCall> extractHookCalls(List<String> calls) {
        List<CoremodHookCall> hooks = new ArrayList<>();
        Pattern stringLiteral = Pattern.compile("['\\\"]([^'\\\"]*)['\\\"]");
        Pattern methodType = Pattern.compile("ASMAPI\\.MethodType\\.([A-Za-z0-9_]+)");
        for (String call : calls) {
            Matcher strings = stringLiteral.matcher(call);
            List<String> values = new ArrayList<>(3);
            while (strings.find() && values.size() < 3) values.add(strings.group(1));
            if (values.size() < 3) continue;
            Matcher type = methodType.matcher(call);
            hooks.add(new CoremodHookCall(values.get(0).replace('/', '.'), values.get(1), values.get(2),
                    type.find() ? type.group(1) : ""));
        }
        return List.copyOf(hooks);
    }

    private static List<String> extractFunctionCalls(String source, String function) {
        List<String> calls = new ArrayList<>();
        int from = 0;
        while (from < source.length()) {
            int start = source.indexOf(function, from);
            if (start < 0) break;
            int open = start + function.length();
            while (open < source.length() && Character.isWhitespace(source.charAt(open))) open++;
            if (open >= source.length() || source.charAt(open) != '(') {
                from = start + function.length();
                continue;
            }

            int depth = 1;
            char quote = 0;
            boolean escaped = false;
            int i = open + 1;
            for (; i < source.length() && depth > 0; i++) {
                char ch = source.charAt(i);
                if (quote != 0) {
                    if (escaped) {
                        escaped = false;
                    } else if (ch == '\\') {
                        escaped = true;
                    } else if (ch == quote) {
                        quote = 0;
                    }
                    continue;
                }
                if (ch == '\'' || ch == '"' || ch == '`') {
                    quote = ch;
                } else if (ch == '(') {
                    depth++;
                } else if (ch == ')') {
                    depth--;
                }
            }

            if (depth == 0) {
                String args = source.substring(open + 1, i - 1).replaceAll("\\s+", " ").trim();
                if (!args.isEmpty()) calls.add(args);
                from = i;
            } else {
                // Malformed/unclosed call: keep analysis non-fatal and stop at EOF.
                break;
            }
        }
        return List.copyOf(calls);
    }

    private static List<CoremodMethodTarget> extractMethodTargets(String source) {
        List<CoremodMethodTarget> targets = new ArrayList<>();
        Matcher blocks = METHOD_TARGET_BLOCK.matcher(source);
        while (blocks.find()) {
            CoremodMethodTarget target = parseMethodTargetBlock(blocks.group(1));
            if (target != null) targets.add(target);
        }
        return List.copyOf(targets);
    }

    private static Set<String> matches(Pattern pattern, String source, int group) {
        Set<String> values = new LinkedHashSet<>();
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            String value = matcher.group(group).replaceAll("\\s+", " ").trim();
            if (!value.isEmpty()) values.add(value);
        }
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
