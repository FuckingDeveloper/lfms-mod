package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.render.LegacyRenderType;
import java.lang.invoke.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.resources.Identifier;
import org.objectweb.asm.*;

/** Real target factory/outline selection through transformed original source ABI. */
public final class TexturedRenderTypeFamilyCheck {
    private record Factory(String name, boolean flag, java.util.function.BiFunction<Identifier,Boolean,RenderType> target) {}
    static void run() throws Exception {
        var factories=List.of(
                new Factory("m_110431_",false,(id,flag)->RenderTypes.armorCutoutNoCull(id)),
                new Factory("m_110452_",false,(id,flag)->RenderTypes.entityCutoutCull(id)),
                new Factory("m_110443_",true,RenderTypes::entityCutout),
                new Factory("m_110458_",false,(id,flag)->RenderTypes.entityCutout(id)),
                new Factory("m_110448_",true,RenderTypes::entityCutoutZOffset),
                new Factory("m_110464_",false,(id,flag)->RenderTypes.entityCutoutZOffset(id)),
                new Factory("m_110454_",true,RenderTypes::entityTranslucent),
                new Factory("m_110473_",false,(id,flag)->RenderTypes.entityTranslucent(id)));
        String owner="net/minecraft/client/renderer/RenderType",location="Lnet/minecraft/resources/ResourceLocation;";
        var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);writer.visit(Opcodes.V17,Opcodes.ACC_PUBLIC,"fixture/TexturedFactories",null,"java/lang/Object",null);
        for(int i=0;i<factories.size();i++) {
            var factory=factories.get(i);String desc="("+location+(factory.flag()?"Z":"")+")L"+owner+";";
            var direct=writer.visitMethod(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"direct"+i,desc,null,null);direct.visitCode();
            direct.visitVarInsn(Opcodes.ALOAD,0);if(factory.flag())direct.visitVarInsn(Opcodes.ILOAD,1);
            direct.visitMethodInsn(Opcodes.INVOKESTATIC,owner,factory.name(),desc,false);direct.visitInsn(Opcodes.ARETURN);direct.visitMaxs(0,0);direct.visitEnd();
            var handle=new Handle(Opcodes.H_INVOKESTATIC,owner,factory.name(),desc,false);
            for(boolean nested:List.of(false,true)) {
                var method=writer.visitMethod(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,(nested?"nested":"handle")+i,"()Ljava/lang/invoke/MethodHandle;",null,null);method.visitCode();
                var bootstrap=new Handle(Opcodes.H_INVOKESTATIC,"com/fuckingdeveloper/lms/check/TexturedRenderTypeFamilyCheck","constant","(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/invoke/MethodHandle;)Ljava/lang/invoke/MethodHandle;",false);
                method.visitLdcInsn(nested?new ConstantDynamic("factory","Ljava/lang/invoke/MethodHandle;",bootstrap,handle):handle);
                method.visitInsn(Opcodes.ARETURN);method.visitMaxs(0,0);method.visitEnd();
            }
        }
        writer.visitEnd();var path=Files.createTempFile("lms-render-factory", ".jar");
        try(var jar=new JarOutputStream(Files.newOutputStream(path))){jar.putNextEntry(new JarEntry("fixture/TexturedFactories.class"));jar.write(writer.toByteArray());jar.closeEntry();}
        try(var loader=new ManagedLegacyClassLoader(path,TexturedRenderTypeFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
            var type=loader.linkOwnedClass("fixture.TexturedFactories");
            for(int i=0;i<factories.size();i++)for(boolean flag:List.of(false,true))for(String texture:List.of("probe:first","probe:second")) {
                var factory=factories.get(i);var id=Identifier.parse(texture);
                var parameters=factory.flag()?new Class<?>[]{Identifier.class,boolean.class}:new Class<?>[]{Identifier.class};
                Object[] args=factory.flag()?new Object[]{id,flag}:new Object[]{id};
                var result=(LegacyRenderType)type.getMethod("direct"+i,parameters).invoke(null,args);
                var expected=factory.target().apply(id,flag);
                check(result.texture()==id&&result.requireTargetRenderType()==expected,"source texture/flags/target changed: "+factory.name());
                check(expected.outline().isPresent()==(!factory.flag()||flag),"outline participation changed: "+factory.name());
                check(expected.hasBlending()==(factory.name().equals("m_110454_")||factory.name().equals("m_110473_")),"unexpected blend state: "+factory.name());
                for(String prefix:List.of("handle","nested"))try{
                    var callable=(MethodHandle)type.getMethod(prefix+i).invoke(null);
                    var other=(LegacyRenderType)callable.invokeWithArguments(args);
                    check(other.requireTargetRenderType()==expected,"callable constant lost factory/flag identity");
                }catch(Throwable failure){throw new AssertionError(failure);}
            }
            try{LegacyRenderType.m_110458_(null);throw new AssertionError("null texture admitted");}catch(NullPointerException expected){}
            try{LegacyRenderType.m_110451_().requireTargetRenderType();throw new AssertionError("chunk pipeline guessed");}catch(UnsupportedOperationException expected){}
        } finally {Files.deleteIfExists(path);}
        System.out.println("PASS: eight original source render factories, direct/handle/nested constants, exact native cull/no-cull/armor/Z-offset/translucent selection, textures and true/false/default outlines; pixels/geometry remain runtime assertions");
    }
    public static MethodHandle constant(MethodHandles.Lookup lookup,String name,Class<?> type,MethodHandle value){return value;}
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}
