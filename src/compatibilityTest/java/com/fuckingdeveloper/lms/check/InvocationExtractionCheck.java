package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.transform.SemanticInvocationRewriter;
import java.lang.invoke.*;
import java.util.List;
import java.util.function.IntToLongFunction;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Links independent callable fixtures: no source profile, Minecraft classes or native loader. */
public final class InvocationExtractionCheck implements Opcodes {
    public static class Base {
        public long compute(int value) { return value + 7L; }
        public static long staticCompute(int value) { return value+9L; }
    }
    public static final class Child extends Base {}
    public static long bridge(Base receiver,int value) { return receiver.compute(value) + 100L; }
    public static long staticBridge(int value) { return Base.staticCompute(value)+200L; }
    public static Object constant(MethodHandles.Lookup lookup,String name,Class<?> type,Object value) { return value; }

    static void run() throws Throwable {
        String base=Type.getInternalName(Base.class),child=Type.getInternalName(Child.class);
        String host=Type.getInternalName(InvocationExtractionCheck.class);
        var rewriter=new SemanticInvocationRewriter((candidate,ancestor)->candidate.equals(ancestor)
                || candidate.equals(child)&&ancestor.equals(base));
        var node=new ClassNode();node.version=V17;node.access=ACC_PUBLIC;
        node.name="com/fuckingdeveloper/lms/check/ExtractedInvocationFixture";node.superName="java/lang/Object";
        var direct=new MethodNode(ACC_PUBLIC|ACC_STATIC,"direct","(L"+child+";I)J",null,null);
        direct.instructions.add(new VarInsnNode(ALOAD,0));direct.instructions.add(new VarInsnNode(ILOAD,1));
        direct.instructions.add(new MethodInsnNode(INVOKEVIRTUAL,child,"compute","(I)J",false));direct.instructions.add(new InsnNode(LRETURN));node.methods.add(direct);
        var bound=new MethodNode(ACC_PUBLIC|ACC_STATIC,"bound","(L"+child+";)Ljava/util/function/IntToLongFunction;",null,null);
        bound.instructions.add(new VarInsnNode(ALOAD,0));
        var metafactory=new Handle(H_INVOKESTATIC,"java/lang/invoke/LambdaMetafactory","metafactory",
                "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",false);
        bound.instructions.add(new InvokeDynamicInsnNode("applyAsLong",bound.desc,metafactory,
                Type.getMethodType("(I)J"),new Handle(H_INVOKEVIRTUAL,base,"compute","(I)J",false),Type.getMethodType("(I)J")));
        bound.instructions.add(new InsnNode(ARETURN));node.methods.add(bound);
        var bootstrap=new Handle(H_INVOKESTATIC,host,"constant",
                "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;",false);
        var original=new Handle(H_INVOKEVIRTUAL,child,"compute","(I)J",false);
        for (boolean nested:List.of(false,true)) {
            var method=new MethodNode(ACC_PUBLIC|ACC_STATIC,nested?"nested":"handle","()Ljava/lang/invoke/MethodHandle;",null,null);
            Object value=nested?new ConstantDynamic("outer","Ljava/lang/invoke/MethodHandle;",bootstrap,
                    new ConstantDynamic("inner","Ljava/lang/invoke/MethodHandle;",bootstrap,original)):original;
            method.instructions.add(new LdcInsnNode(value));method.instructions.add(new InsnNode(ARETURN));node.methods.add(method);
        }
        var staticCall=new MethodNode(ACC_PUBLIC|ACC_STATIC,"staticCall","(I)J",null,null);
        staticCall.instructions.add(new VarInsnNode(ILOAD,0));staticCall.instructions.add(new MethodInsnNode(INVOKESTATIC,base,"staticCompute","(I)J",false));staticCall.instructions.add(new InsnNode(LRETURN));node.methods.add(staticCall);
        for(boolean nested:List.of(false,true)) {
            var method=new MethodNode(ACC_PUBLIC|ACC_STATIC,nested?"staticNested":"staticHandle","()Ljava/lang/invoke/MethodHandle;",null,null);
            Object handle=new Handle(H_INVOKESTATIC,base,"staticCompute","(I)J",false);
            if(nested)handle=new ConstantDynamic("staticOuter","Ljava/lang/invoke/MethodHandle;",bootstrap,new ConstantDynamic("staticInner","Ljava/lang/invoke/MethodHandle;",bootstrap,handle));
            method.instructions.add(new LdcInsnNode(handle));method.instructions.add(new InsnNode(ARETURN));node.methods.add(method);
        }
        SemanticInvocationRewriter.MemberMapping members=(opcode,owner,name,descriptor)->
                (opcode==INVOKEVIRTUAL&&(owner.equals(base)||owner.equals(child))&&name.equals("compute")&&descriptor.equals("(I)J"))
                        ?new Handle(H_INVOKESTATIC,host,"bridge","(L"+base+";I)J",false):
                opcode==INVOKESTATIC&&owner.equals(base)&&name.equals("staticCompute")&&descriptor.equals("(I)J")?new Handle(H_INVOKESTATIC,host,"staticBridge",descriptor,false):null;
        check(rewriter.rewrite(node,members)==7,"independent invocation/handle/constant sites lost");
        var writer=new ClassWriter(ClassWriter.COMPUTE_FRAMES|ClassWriter.COMPUTE_MAXS);node.accept(writer);
        class Loader extends ClassLoader {
            Loader(){super(InvocationExtractionCheck.class.getClassLoader());}
            Class<?> define(byte[] bytes){return defineClass(null,bytes,0,bytes.length);}
        }
        var type=new Loader().define(writer.toByteArray());var receiver=new Child();
        check(type.getMethod("direct",Child.class,int.class).invoke(null,receiver,3).equals(110L),"direct receiver/result lost");
        var lambda=(IntToLongFunction)type.getMethod("bound",Child.class).invoke(null,receiver);
        check(lambda.applyAsLong(4)==111L,"exact captured subclass lambda type lost");
        for(var name:List.of("handle","nested")) {
            var handle=(MethodHandle)type.getMethod(name).invoke(null);
            check(handle.type().equals(MethodType.methodType(long.class,Child.class,int.class)),"callable type changed: "+name);
            check((long)handle.invokeExact(receiver,5)==112L,"callable dispatch changed: "+name);
        }
        check(type.getMethod("staticCall",int.class).invoke(null,2).equals(211L),"static direct invocation escaped mapping");
        for(String name:List.of("staticHandle","staticNested")) {
            var handle=(MethodHandle)type.getMethod(name).invoke(null);
            check(handle.type().equals(MethodType.methodType(long.class,int.class))&&(long)handle.invokeExact(3)==212L,"static callable shape or dispatch changed");
        }
        var negative=new ClassNode();negative.name="fixture/Negative";
        var wrong=new MethodNode(ACC_PUBLIC|ACC_STATIC,"wrong","(Ljava/lang/Object;)V",null,null);
        var untouched=new MethodInsnNode(INVOKEVIRTUAL,"fixture/Unrelated","compute","(I)J",false);
        wrong.instructions.add(untouched);negative.methods.add(wrong);
        check(rewriter.rewrite(negative,members)==0&&untouched.owner.equals("fixture/Unrelated"),"descriptor coincidence selected an owner");
        var invalid=new ClassNode();invalid.name="fixture/Invalid";
        var bad=new MethodNode(ACC_PUBLIC|ACC_STATIC,"bad","()V",null,null);
        bad.instructions.add(new MethodInsnNode(INVOKEVIRTUAL,base,"compute","(I)J",false));invalid.methods.add(bad);
        try {
            rewriter.rewrite(invalid,(op,owner,name,desc)->new Handle(H_INVOKESTATIC,host,"bridge","(L"+base+";I)I",false));
            throw new AssertionError("changed return contract accepted");
        } catch(IllegalStateException expected) {}
        var badStatic=new ClassNode();badStatic.name="probe/BadStatic";var invalidStatic=new MethodNode();invalidStatic.instructions.add(new MethodInsnNode(INVOKESTATIC,base,"staticCompute","(I)J",false));badStatic.methods.add(invalidStatic);
        try{rewriter.rewrite(badStatic,(op,o,n,d)->new Handle(H_INVOKESTATIC,host,"staticBridge","(JI)J",false));throw new AssertionError("static bridge invented operands");}catch(IllegalStateException expectedShape){}
        System.out.println("PASS: shared invocation utility links independent direct calls, exact subclass captures, raw handles and nested constants; rejects unrelated owners/changed returns without native APIs");
    }
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}
