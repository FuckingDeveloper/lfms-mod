package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.profile.Forge1192DistributionFilter;
import java.util.*;
import java.util.jar.JarFile;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

final class DistributionFilterCheck implements Opcodes {
    private static final String PREFIX = "Lnet/minecraftforge/api/distmarker/";
    private static AnnotationNode only(String side) {
        var a = new AnnotationNode(PREFIX + "OnlyIn;");
        a.values = new ArrayList<>(List.of("value", new String[]{PREFIX + "Dist;", side}));
        return a;
    }
    private static MethodNode method(String name, int access) {
        return new MethodNode(access, name, "()V", null, null);
    }
    private static void lambda(MethodNode method, String target) {
        method.instructions.add(new InvokeDynamicInsnNode("run", "()Ljava/lang/Runnable;",
                new Handle(H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory",
                        "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;"
                                + "Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;"
                                + "Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false),
                Type.getMethodType("()V"), new Handle(H_INVOKESTATIC,"fixture/Sides",target,"()V",false),
                Type.getMethodType("()V")));
    }
    static void run() throws Exception {
        for (String dist : List.of("CLIENT", "DEDICATED_SERVER")) {
            String other = dist.equals("CLIENT") ? "DEDICATED_SERVER" : "CLIENT";
            var node = new ClassNode(); node.name="fixture/Sides";
            var face = only(other); face.values.add("_interface"); face.values.add(Type.getObjectType("fixture/Opposite"));
            var repeat = new AnnotationNode(PREFIX+"OnlyIns;"); repeat.values=new ArrayList<>(List.of("value",List.of(face)));
            node.visibleAnnotations=new ArrayList<>(List.of(repeat));
            node.interfaces.addAll(List.of("fixture/Opposite","fixture/Kept"));
            var field = new FieldNode(ACC_PUBLIC,"opposite","Ljava/lang/Object;",null,null);
            field.visibleAnnotations=List.of(only(other)); node.fields.add(field);
            var removed=method("opposite",ACC_PUBLIC); removed.visibleAnnotations=List.of(only(other));
            lambda(removed,"nested"); node.methods.add(removed);
            var nested=method("nested",ACC_PRIVATE|ACC_STATIC|ACC_SYNTHETIC); lambda(nested,"leaf"); node.methods.add(nested);
            node.methods.add(method("leaf",ACC_PRIVATE|ACC_STATIC|ACC_SYNTHETIC));
            var kept=method("kept",ACC_PUBLIC); kept.visibleAnnotations=List.of(only(dist)); node.methods.add(kept);
            node.methods.add(method("ordinary",ACC_PRIVATE|ACC_STATIC));
            check(Forge1192DistributionFilter.apply(node,dist)==6,"removal count");
            check(node.fields.isEmpty() && node.interfaces.equals(List.of("fixture/Kept")),"fields/interfaces");
            check(node.methods.stream().map(m->m.name).toList().equals(List.of("kept","ordinary")),"recursive lambdas/kept methods");
            check(node.visibleAnnotations.isEmpty(),"class annotations retained");
            check(Forge1192DistributionFilter.apply(node,dist)==0,"not idempotent");
            var forbidden = new ClassNode(); forbidden.name="fixture/Forbidden";forbidden.visibleAnnotations=List.of(only(other));
            try { Forge1192DistributionFilter.apply(forbidden,dist);throw new AssertionError("wrong-side class allowed"); }
            catch(IllegalStateException expected) { check(expected.getMessage().contains("unavailable"),"wrong rejection"); }
        }
        try(var jar = new JarFile("legacy-mods/CarbonConfig-1.19.2-2.0.2.jar")) {
            var node = new ClassNode();new ClassReader(jar.getInputStream(jar.getJarEntry("carbonconfiglib/CarbonConfig.class")).readAllBytes()).accept(node,0);
            check(node.methods.stream().anyMatch(m->m.name.equals("onKeyPressed")),"missing immutable source callback");
            Forge1192DistributionFilter.apply(node,"DEDICATED_SERVER");
            check(node.methods.stream().noneMatch(m->m.name.equals("onKeyPressed")||m.name.startsWith("lambda$registerKeys$")),"fixture client callback/lambda survived");
            check(node.methods.stream().anyMatch(m->m.name.equals("load")) && node.methods.stream().anyMatch(m->m.name.equals("unload")),"common callbacks removed");
        }
        System.out.println("PASS: source physical-side class/field/method/interface filtering on both sides, recursive synthetic lambdas, idempotence and original artifact common callbacks");
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
