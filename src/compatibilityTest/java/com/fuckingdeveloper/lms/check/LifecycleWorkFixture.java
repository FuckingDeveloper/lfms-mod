package com.fuckingdeveloper.lms.check;
import java.util.List;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
public final class LifecycleWorkFixture implements Opcodes {
    private static final String ROOT="net/minecraftforge/fml/event/lifecycle/ParallelDispatchEvent";
    private static final String EVENT="net/minecraftforge/fml/event/lifecycle/FMLCommonSetupEvent";
    private static final String DESC="(Ljava/lang/Runnable;)Ljava/util/concurrent/CompletableFuture;";
    public static Object constant(java.lang.invoke.MethodHandles.Lookup lookup,String name,Class<?> type,Object value) { return value; }
    public static byte[] bytes() {
        var node = new ClassNode(); node.version=V17;node.access=ACC_PUBLIC;node.name="fixture/WorkCalls";node.superName="java/lang/Object";
        var method = new MethodNode(ACC_PUBLIC|ACC_STATIC,"direct","(L"+EVENT+";Ljava/lang/Runnable;)Ljava/util/concurrent/CompletableFuture;",null,null);
        method.instructions.add(new VarInsnNode(ALOAD,0));method.instructions.add(new VarInsnNode(ALOAD,1));
        method.instructions.add(new MethodInsnNode(INVOKEVIRTUAL,EVENT,"enqueueWork",DESC,false));method.instructions.add(new InsnNode(ARETURN));node.methods.add(method);
        var function = new MethodNode(ACC_PUBLIC|ACC_STATIC,"bound","(L"+EVENT+";)Ljava/util/function/Function;",null,null);
        function.instructions.add(new VarInsnNode(ALOAD,0));
        function.instructions.add(new InvokeDynamicInsnNode("apply","(L"+EVENT+";)Ljava/util/function/Function;",
                new Handle(H_INVOKESTATIC,"java/lang/invoke/LambdaMetafactory","metafactory","(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",false),
                Type.getMethodType("(Ljava/lang/Object;)Ljava/lang/Object;"),new Handle(H_INVOKEVIRTUAL,ROOT,"enqueueWork",DESC,false),Type.getMethodType(DESC)));
        function.instructions.add(new InsnNode(ARETURN));node.methods.add(function);
        var bootstrap = new Handle(H_INVOKESTATIC,"com/fuckingdeveloper/lms/check/LifecycleWorkFixture","constant",
                "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;",false);
        for (boolean nested : List.of(false,true)) {
            var reference=new MethodNode(ACC_PUBLIC|ACC_STATIC,nested?"nested":"handle","()Ljava/lang/invoke/MethodHandle;",null,null);
            var original=new Handle(H_INVOKEVIRTUAL,EVENT,"enqueueWork",DESC,false);
            reference.instructions.add(new LdcInsnNode(nested?new ConstantDynamic("outer","Ljava/lang/invoke/MethodHandle;",bootstrap,
                    new ConstantDynamic("inner","Ljava/lang/invoke/MethodHandle;",bootstrap,original)):original));
            reference.instructions.add(new InsnNode(ARETURN));node.methods.add(reference);
        }
        var constructor=new MethodNode(ACC_PUBLIC,"<init>","()V",null,null);
        constructor.instructions.add(new VarInsnNode(ALOAD,0));constructor.instructions.add(new MethodInsnNode(INVOKESPECIAL,"java/lang/Object","<init>","()V",false));
        for(String name:List.of("handle","nested")) {
            constructor.instructions.add(new MethodInsnNode(INVOKESTATIC,node.name,name,"()Ljava/lang/invoke/MethodHandle;",false));constructor.instructions.add(new InsnNode(POP));
        }
        constructor.instructions.add(new InsnNode(RETURN));node.methods.add(constructor);
        var writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);return writer.toByteArray();
    }
}
