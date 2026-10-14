package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.runtime.Forge1192InteractionResultBridge;
import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer;
import net.minecraft.world.InteractionResult;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.nio.file.*;
import java.util.jar.*;

public final class InteractionResultInformationCheck {
    private static void check(boolean ok,String message) {if(!ok)throw new AssertionError(message);}
    public static void run() throws Exception {
        var values=Forge1192InteractionResultBridge.values();check(values.length==5,"source enum cardinality");
        String[] names={"SUCCESS","CONSUME","CONSUME_PARTIAL","PASS","FAIL"};
        for(int i=0;i<5;i++) {
            check(Forge1192InteractionResultBridge.ordinal(values[i])==i && Forge1192InteractionResultBridge.name(values[i]).equals(names[i]),"source order/names");
            check(Forge1192InteractionResultBridge.valueOf(names[i])==values[i],"canonical outcome identity");
            check(Forge1192InteractionResultBridge.consumesAction(values[i])==(i<3),"source consume predicate");
            check(Forge1192InteractionResultBridge.shouldSwing(values[i])==(i==0),"source swing predicate");
            check(Forge1192InteractionResultBridge.shouldAwardStats(values[i])==(i<2),"source item-stat predicate");
        }
        values[0]=InteractionResult.FAIL;check(Forge1192InteractionResultBridge.values()[0]==InteractionResult.SUCCESS,"values array sharing");
        try{Forge1192InteractionResultBridge.valueOf("unknown");throw new AssertionError("invalid name accepted");}catch(IllegalArgumentException expected){}
        try{Forge1192InteractionResultBridge.ordinal(InteractionResult.TRY_WITH_EMPTY_HAND);throw new AssertionError("new native control outcome guessed");}catch(UnsupportedOperationException expected){}
        check(Forge1192InteractionResultBridge.sidedSuccess(true)==InteractionResult.SUCCESS && Forge1192InteractionResultBridge.sidedSuccess(false)==InteractionResult.CONSUME,"side semantics");
        String owner="net/minecraft/world/InteractionResult",result="L"+owner+";";
        var node=new ClassNode();node.name="fixture/InteractionEnumInformation";node.superName="java/lang/Object";node.version=Opcodes.V17;node.access=Opcodes.ACC_PUBLIC;
        var size=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"size","()I",null,null);
        size.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,owner,"values","()["+result,false));size.instructions.add(new InsnNode(Opcodes.ARRAYLENGTH));size.instructions.add(new InsnNode(Opcodes.IRETURN));node.methods.add(size);
        var branch=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"branch","("+result+")I",null,null);
        branch.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));branch.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,owner,"ordinal","()I",false));
        var labels=new LabelNode[5];for(int i=0;i<5;i++)labels[i]=new LabelNode();var fallback=new LabelNode();
        branch.instructions.add(new TableSwitchInsnNode(0,4,fallback,labels));
        for(int i=0;i<5;i++){branch.instructions.add(labels[i]);branch.instructions.add(new IntInsnNode(Opcodes.SIPUSH,100+i));branch.instructions.add(new InsnNode(Opcodes.IRETURN));}
        branch.instructions.add(fallback);branch.instructions.add(new InsnNode(Opcodes.ICONST_M1));branch.instructions.add(new InsnNode(Opcodes.IRETURN));node.methods.add(branch);
        var handle=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"handle","()Ljava/lang/invoke/MethodHandle;",null,null);
        handle.instructions.add(new LdcInsnNode(new Handle(Opcodes.H_INVOKEVIRTUAL,owner,"name","()Ljava/lang/String;",false)));handle.instructions.add(new InsnNode(Opcodes.ARETURN));node.methods.add(handle);
        var partial=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"partial","()"+result,null,null);
        partial.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC,owner,"CONSUME_PARTIAL",result));partial.instructions.add(new InsnNode(Opcodes.ARETURN));node.methods.add(partial);
        var writer=new ClassWriter(ClassWriter.COMPUTE_FRAMES|ClassWriter.COMPUTE_MAXS);node.accept(writer);var path=Files.createTempFile("lms-interaction-enum", ".jar");
        try(var jar=new JarOutputStream(Files.newOutputStream(path))){jar.putNextEntry(new JarEntry(node.name+".class"));jar.write(writer.toByteArray());jar.closeEntry();}
        var original=Files.readAllBytes(path);
        try(var loader=new ManagedLegacyClassLoader(path,InteractionResultInformationCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
            var clazz=loader.linkOwnedClass(node.name.replace('/','.'));check((int)clazz.getMethod("size").invoke(null)==5,"source compiler switch array shape");
            values=Forge1192InteractionResultBridge.values();
            for(int i=0;i<5;i++)check((int)clazz.getMethod("branch",InteractionResult.class).invoke(null,values[i])==100+i,"source switch branch");
            check(clazz.getMethod("partial").invoke(null)==values[2],"source partial constant");
            var getter=(java.lang.invoke.MethodHandle)clazz.getMethod("handle").invoke(null);
            try{check(getter.invoke(values[3]).equals("PASS"),"source name handle");}catch(Throwable failure){throw new IllegalStateException("source interaction handle failed",failure);}
        }
        check(java.util.Arrays.equals(original,Files.readAllBytes(path)),"immutable source switch fixture changed");
        System.out.println("PASS: source five-value order/clones/names/failures, side/consume/swing/stats, native partial semantics, transformed compiler switch and handle; full Java Enum representation not inferred");
    }
}
