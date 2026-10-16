package com.fuckingdeveloper.lms.profile.forge1192;

import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Forge 1.19.2 RuntimeDistCleaner semantics before managed class definition. */
public final class Forge1192DistributionFilter {
    private static final String PREFIX = "Lnet/minecraftforge/api/distmarker/";
    private static final Handle METAFACTORY = new Handle(Opcodes.H_INVOKESTATIC,
            "java/lang/invoke/LambdaMetafactory", "metafactory",
            "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;"
                    + "Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;"
                    + "Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false);
    private Forge1192DistributionFilter() {}

    public static int apply(ClassNode node, String distribution) {
        if (!Set.of("CLIENT", "DEDICATED_SERVER").contains(distribution))
            throw new IllegalArgumentException("Unknown physical distribution: " + distribution);
        if (wrongSide(node.visibleAnnotations, distribution))
            throw new IllegalStateException("Source class " + node.name + " is unavailable on " + distribution);
        int changes = 0;
        for (var annotation : unpack(node.visibleAnnotations)) {
            Object target = value(annotation, "_interface");
            if (target instanceof Type type && !side(annotation).equals(distribution)
                    && node.interfaces.remove(type.getInternalName())) changes++;
        }
        if (node.visibleAnnotations != null) {
            int before = node.visibleAnnotations.size();
            node.visibleAnnotations.removeIf(a -> a.desc.equals(PREFIX + "OnlyIn;")
                    || a.desc.equals(PREFIX + "OnlyIns;"));
            changes += before - node.visibleAnnotations.size();
        }
        int fields = node.fields.size();
        node.fields.removeIf(field -> wrongSide(field.visibleAnnotations, distribution));
        changes += fields - node.fields.size();
        var lambdaTargets = new HashSet<String>();
        for (var it = node.methods.iterator(); it.hasNext();) {
            var method = it.next();
            if (!wrongSide(method.visibleAnnotations, distribution)) continue;
            gather(node.name, method, lambdaTargets);
            it.remove(); changes++;
        }
        // Source removal is recursive, and only compiler-synthetic methods qualify.
        boolean removed;
        do {
            removed = false;
            for (var it = node.methods.iterator(); it.hasNext();) {
                var method = it.next();
                if ((method.access & Opcodes.ACC_SYNTHETIC) == 0
                        || !lambdaTargets.contains(method.name + method.desc)) continue;
                gather(node.name, method, lambdaTargets);
                it.remove(); changes++; removed = true;
            }
        } while (removed);
        return changes;
    }

    private static void gather(String owner, MethodNode method, Set<String> targets) {
        for (var instruction : method.instructions) {
            if (instruction instanceof InvokeDynamicInsnNode call && call.bsm.equals(METAFACTORY)
                    && call.bsmArgs.length > 1 && call.bsmArgs[1] instanceof Handle impl
                    && impl.getOwner().equals(owner)) targets.add(impl.getName() + impl.getDesc());
        }
    }

    private static boolean wrongSide(List<AnnotationNode> annotations, String distribution) {
        return unpack(annotations).stream().anyMatch(a -> value(a, "_interface") == null
                && !side(a).equals(distribution));
    }
    private static String side(AnnotationNode annotation) {
        Object encoded = value(annotation, "value");
        if (!(encoded instanceof String[] values) || values.length != 2
                || !values[0].equals(PREFIX + "Dist;")
                || !(values[1].equals("CLIENT") || values[1].equals("DEDICATED_SERVER")))
            throw new IllegalStateException("Invalid source OnlyIn distribution");
        return values[1];
    }
    private static Object value(AnnotationNode annotation, String key) {
        if (annotation.values != null) for (int i = 0; i < annotation.values.size(); i += 2)
            if (annotation.values.get(i).equals(key)) return annotation.values.get(i + 1);
        return null;
    }
    private static List<AnnotationNode> unpack(List<AnnotationNode> annotations) {
        var result = new ArrayList<AnnotationNode>();
        if (annotations != null) for (var annotation : annotations) {
            if (annotation.desc.equals(PREFIX + "OnlyIn;")) result.add(annotation);
            else if (annotation.desc.equals(PREFIX + "OnlyIns;")) {
                if (!(value(annotation, "value") instanceof List<?> nested))
                    throw new IllegalStateException("Invalid source OnlyIns container");
                for (var child : nested) {
                    if (!(child instanceof AnnotationNode entry) || !entry.desc.equals(PREFIX + "OnlyIn;"))
                        throw new IllegalStateException("Invalid source OnlyIns entry");
                    result.add(entry);
                }
            }
        }
        return result;
    }
}
