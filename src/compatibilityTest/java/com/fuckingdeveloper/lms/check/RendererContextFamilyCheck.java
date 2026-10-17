package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.render.LegacyBlockRenderDispatcher;
import java.lang.invoke.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.objectweb.asm.*;

public final class RendererContextFamilyCheck {
    static void run() throws Exception {
        var first=new BlockModelResolver(null);var second=new BlockModelResolver(null);
        var entity=context(first);
        var block=new BlockEntityRendererProvider.Context(null,second,null,null,null,null,null,null);
        Object[] contexts={entity,block};
        String[] owners={"net/minecraft/client/renderer/entity/EntityRendererProvider$Context","net/minecraft/client/renderer/blockentity/BlockEntityRendererProvider$Context"};
        String[] names={"m_234597_","m_173584_"};
        String returns="Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;";
        var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);writer.visit(Opcodes.V17,Opcodes.ACC_PUBLIC,"fixture/RendererContexts",null,"java/lang/Object",null);
        for(int i=0;i<owners.length;i++)for(String name:List.of(names[i],"getBlockRenderDispatcher")) {
            String suffix=i+(name.equals(names[i])?"Srg":"Named");
            var direct=writer.visitMethod(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"direct"+suffix,"(L"+owners[i]+";)"+returns,null,null);direct.visitCode();
            direct.visitVarInsn(Opcodes.ALOAD,0);direct.visitMethodInsn(Opcodes.INVOKEVIRTUAL,owners[i],name,"()"+returns,false);direct.visitInsn(Opcodes.ARETURN);direct.visitMaxs(0,0);direct.visitEnd();
            var getter=new Handle(Opcodes.H_INVOKEVIRTUAL,owners[i],name,"()"+returns,false);
            for(boolean nested:List.of(false,true)) {
                var method=writer.visitMethod(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,(nested?"nested":"handle")+suffix,"()Ljava/lang/invoke/MethodHandle;",null,null);method.visitCode();
                var bootstrap=new Handle(Opcodes.H_INVOKESTATIC,"com/fuckingdeveloper/lms/check/RendererContextFamilyCheck","constant","(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/invoke/MethodHandle;)Ljava/lang/invoke/MethodHandle;",false);
                method.visitLdcInsn(nested?new ConstantDynamic("context","Ljava/lang/invoke/MethodHandle;",bootstrap,getter):getter);
                method.visitInsn(Opcodes.ARETURN);method.visitMaxs(0,0);method.visitEnd();
            }
        }
        writer.visitEnd();var path=Files.createTempFile("lms-render-context", ".jar");
        try(var jar=new JarOutputStream(Files.newOutputStream(path))){jar.putNextEntry(new JarEntry("fixture/RendererContexts.class"));jar.write(writer.toByteArray());jar.closeEntry();}
        byte[] original=Files.readAllBytes(path);
        var resolver=LegacyBlockRenderDispatcher.class.getDeclaredMethod("resolver");resolver.setAccessible(true);
        try(var loader=new ManagedLegacyClassLoader(path,RendererContextFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
            var type=loader.linkOwnedClass("fixture.RendererContexts");
            for(int i=0;i<contexts.length;i++)for(String mode:List.of("Srg","Named")) {
                var expected=i==0?first:second;String suffix=i+mode;
                var value=(LegacyBlockRenderDispatcher)type.getMethod("direct"+suffix,contexts[i].getClass()).invoke(null,contexts[i]);
                check(resolver.invoke(value)==expected,"dispatcher lost exact context resolver");
                for(String prefix:List.of("handle","nested"))try{
                    var handle=(MethodHandle)type.getMethod(prefix+suffix).invoke(null);
                    check(resolver.invoke(handle.invoke(contexts[i]))==expected,"callable getter replaced context service");
                }catch(Throwable failure){throw new AssertionError(failure);}
                try{value.m_110910_(null);throw new AssertionError("unsupported block model fabricated");}catch(UnsupportedOperationException expectedFailure){}
            }
            check(Arrays.equals(original,Files.readAllBytes(path)),"source JAR modified");
        } finally {Files.deleteIfExists(path);}
        try{LegacyBlockRenderDispatcher.from(context(null));throw new AssertionError("missing resolver replaced by dummy");}catch(NullPointerException expected){}
        var subclass=new EntityRendererProvider.Context(null,first,null,null,null,null,null,null,null,null,null) {};
        try{LegacyBlockRenderDispatcher.from(subclass);throw new AssertionError("unproven virtual override bypassed");}catch(UnsupportedOperationException expected){}
        System.out.println("PASS: both native renderer contexts through source named/SRG/direct/handle/nested getters, exact resolver identity, immutable artifact, absent resolver/subclass rejection and unsupported immediate model boundary");
    }
    private static EntityRendererProvider.Context context(BlockModelResolver resolver) {
        return new EntityRendererProvider.Context(null,resolver,null,null,null,null,null,null,null,null,null);
    }
    public static MethodHandle constant(MethodHandles.Lookup lookup,String name,Class<?> type,MethodHandle handle){return handle;}
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}
