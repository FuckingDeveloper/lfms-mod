package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import java.lang.invoke.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import static org.objectweb.asm.Opcodes.*;

public final class ResourceKeyFamilyCheck {
    private static final String HOST="fixture/ResourceKeys", ROOT="net/minecraft/resources/ResourceKey", K="L"+ROOT+";", ID="Lnet/minecraft/resources/ResourceLocation;";
    private static final String[][] METHODS={{"m_135782_","()"+ID},{"m_211136_","()"+ID},{"m_135783_","("+K+")Z"},{"m_195975_","("+K+")Ljava/util/Optional;"},{"m_135785_","("+K+ID+")"+K},{"m_135788_","("+ID+")"+K},{"m_195966_","("+K+")Lcom/mojang/serialization/Codec;"}};
    public static Object constant(MethodHandles.Lookup lookup,String name,Class<?> type,Object value){return value;}
    @SuppressWarnings({"rawtypes","unchecked"})
    public static void run() throws Exception {
        var file=Files.createTempFile("source-resource-keys-", ".jar");
        try {
            try(var jar=new JarOutputStream(Files.newOutputStream(file))){jar.putNextEntry(new JarEntry(HOST+".class"));jar.write(fixture());jar.closeEntry();}
            byte[] original=Files.readAllBytes(file);
            try(var loader=new ManagedLegacyClassLoader(file,ResourceKeyFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
                var type=loader.linkOwnedClass(HOST.replace('/','.'));var stone=Identifier.parse("minecraft:stone");var key=ResourceKey.create(Registries.BLOCK,stone);
                Object[][] inputs={{key},{key},{key,Registries.BLOCK},{key,Registries.BLOCK},{Registries.BLOCK,stone},{Identifier.parse("minecraft:block")},{Registries.BLOCK}};
                Object[] expected={stone,Registries.BLOCK.identifier(),true,Optional.of(key),key,Registries.BLOCK,null};
                for(int i=0;i<METHODS.length;i++) {
                    var method=type.getDeclaredMethod("direct"+i,Arrays.stream(inputs[i]).map(v->v instanceof ResourceKey?ResourceKey.class:Identifier.class).toArray(Class<?>[]::new));
                    Object actual=method.invoke(null,inputs[i]);verify(i,actual,expected[i],key);
                    for(String prefix:List.of("handle","nested")) {
                        var handle=(MethodHandle)type.getMethod(prefix+i).invoke(null);
                        try{verify(i,handle.invokeWithArguments(inputs[i]),expected[i],key);}catch(Throwable failure){throw new AssertionError(failure);}
                    }
                }
                var fn=(java.util.function.Function<ResourceKey<?>,Identifier>)type.getMethod("function").invoke(null);
                check(fn.apply(key)==stone||fn.apply(key).equals(stone),"unbound lambda key identifier");
                try{fn.apply(null);throw new AssertionError("null source receiver accepted");}catch(NullPointerException expectedNull){}
                check(type.getMethod("direct2",ResourceKey.class,ResourceKey.class).invoke(null,key,Registries.ITEM).equals(false),"isFor wrong registry");
                check(type.getMethod("direct3",ResourceKey.class,ResourceKey.class).invoke(null,key,Registries.ITEM).equals(Optional.empty()),"cast wrong registry");
                check(Arrays.equals(original,Files.readAllBytes(file)),"source key JAR changed");
            }
            var node=new ClassNode();node.version=V17;node.access=ACC_PUBLIC;node.name="fixture/OtherKeyMethods";node.superName="java/lang/Object";
            var method=new MethodNode(ACC_PUBLIC|ACC_STATIC,"probe","()V",null,null);node.methods.add(method);
            method.instructions.add(new InsnNode(ACONST_NULL));method.instructions.add(new MethodInsnNode(INVOKEVIRTUAL,"fixture/OtherKey","m_135782_","()"+ID,false));method.instructions.add(new InsnNode(POP));method.instructions.add(new InsnNode(RETURN));method.maxStack=1;
            var writer=new ClassWriter(0);node.accept(writer);var result=new ClassNode();new ClassReader(new Forge1192LegacyClassTransformer().transform(node.name.replace('/','.'),writer.toByteArray()).bytes()).accept(result,0);
            check(Arrays.stream(result.methods.getFirst().instructions.toArray()).anyMatch(i->i instanceof MethodInsnNode m&&m.owner.equals("fixture/OtherKey")&&m.name.equals("m_135782_")),"unrelated descriptor coincidence migrated");
        }finally{Files.deleteIfExists(file);}
        System.out.println("PASS: seven pinned source ResourceKey operations, direct/handle/nested/unbound lambda, native interned identity/registry/cast/codec semantics, null receiver and negative owner; immutable artifact");
    }
    @SuppressWarnings({"rawtypes","unchecked"})
    private static void verify(int i,Object actual,Object expected,ResourceKey<?> key) {
        if(i==6){var codec=(com.mojang.serialization.Codec)actual;var json=codec.encodeStart(com.mojang.serialization.JsonOps.INSTANCE,key).getOrThrow();check(json.equals(new com.google.gson.JsonPrimitive("minecraft:stone")),"codec encoding");check(codec.parse(com.mojang.serialization.JsonOps.INSTANCE,json).getOrThrow()==key,"codec interned decode");}
        else if(i==4||i==5)check(actual==expected,"interned factory identity");else check(Objects.equals(actual,expected),"key read semantics "+i);
    }
    private static byte[] fixture() {
        var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);w.visit(V17,ACC_PUBLIC,HOST,null,"java/lang/Object",null);
        var bootstrap=new Handle(H_INVOKESTATIC,"com/fuckingdeveloper/lms/check/ResourceKeyFamilyCheck","constant","(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;",false);
        for(int i=0;i<METHODS.length;i++) {
            var operation=METHODS[i];boolean stat=i>=4;String desc=stat?operation[1]:"("+K+operation[1].substring(1);
            var m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"direct"+i,desc,null,null);m.visitCode();int local=0;for(Type arg:Type.getArgumentTypes(desc)){m.visitVarInsn(arg.getOpcode(ILOAD),local);local+=arg.getSize();}m.visitMethodInsn(stat?INVOKESTATIC:INVOKEVIRTUAL,ROOT,operation[0],operation[1],false);m.visitInsn(Type.getReturnType(desc).getOpcode(IRETURN));m.visitMaxs(0,0);m.visitEnd();
            var h=new Handle(stat?H_INVOKESTATIC:H_INVOKEVIRTUAL,ROOT,operation[0],operation[1],false);
            for(boolean nested:List.of(false,true)){m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,(nested?"nested":"handle")+i,"()Ljava/lang/invoke/MethodHandle;",null,null);m.visitCode();m.visitLdcInsn(nested?new ConstantDynamic("key","Ljava/lang/invoke/MethodHandle;",bootstrap,h):h);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();}
        }
        var m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"function","()Ljava/util/function/Function;",null,null);m.visitCode();m.visitInvokeDynamicInsn("apply","()Ljava/util/function/Function;",new Handle(H_INVOKESTATIC,"java/lang/invoke/LambdaMetafactory","metafactory","(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",false),Type.getMethodType("(Ljava/lang/Object;)Ljava/lang/Object;"),new Handle(H_INVOKEVIRTUAL,ROOT,"m_135782_","()"+ID,false),Type.getMethodType("("+K+")"+ID));m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();w.visitEnd();return w.toByteArray();
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
