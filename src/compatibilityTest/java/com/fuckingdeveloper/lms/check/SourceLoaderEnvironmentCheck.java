package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.*;
import java.nio.file.Files;
import java.util.jar.*;
import java.lang.invoke.MethodHandle;
import net.neoforged.fml.loading.FMLLoader;
import org.objectweb.asm.*;
import static org.objectweb.asm.Opcodes.*;

public final class SourceLoaderEnvironmentCheck {
    public static void run() throws Exception {
        var file=Files.createTempFile("source-launch-environment-",".jar");
        var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);
        writer.visit(V17,ACC_PUBLIC,"fixture/LaunchQueries",null,"java/lang/Object",null);
        for(var rule:Forge1192LoaderEnvironmentRules.entries()) {
            var source=rule.source();var method=writer.visitMethod(ACC_PUBLIC|ACC_STATIC,source.name(),source.descriptor(),null,null);
            method.visitCode();method.visitMethodInsn(INVOKESTATIC,source.owner(),source.name(),source.descriptor(),false);method.visitInsn(Type.getReturnType(source.descriptor()).getOpcode(IRETURN));method.visitMaxs(0,0);method.visitEnd();
            method=writer.visitMethod(ACC_PUBLIC|ACC_STATIC,source.name()+"Handle","()Ljava/lang/invoke/MethodHandle;",null,null);
            method.visitCode();method.visitLdcInsn(new Handle(H_INVOKESTATIC,source.owner(),source.name(),source.descriptor(),false));method.visitInsn(ARETURN);method.visitMaxs(0,0);method.visitEnd();
        }
        writer.visitEnd();
        try {
            try(var jar=new JarOutputStream(Files.newOutputStream(file))){jar.putNextEntry(new JarEntry("fixture/LaunchQueries.class"));jar.write(writer.toByteArray());jar.closeEntry();}
            try(var loader=new ManagedLegacyClassLoader(file,SourceLoaderEnvironmentCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
                var type=loader.linkOwnedClass("fixture.LaunchQueries");
                for(var entry:java.util.Map.<String,Object>of("isProduction",FMLLoader.getCurrent().isProduction(),"getDist",FMLLoader.getCurrent().getDist(),"getGamePath",FMLLoader.getCurrent().getGameDir()).entrySet()) {
                    if(!type.getMethod(entry.getKey()).invoke(null).equals(entry.getValue()))throw new AssertionError("source launch query differs from native: "+entry.getKey());
                    var handle=(MethodHandle)type.getMethod(entry.getKey()+"Handle").invoke(null);
                    try{if(!handle.invokeWithArguments().equals(entry.getValue()))throw new AssertionError("source launch handle differs from native");}catch(Throwable failure){throw new AssertionError(failure);}
                }
            }
        } finally {Files.deleteIfExists(file);}
        System.out.println("PASS: immutable source static launch queries/handles execute through actual native production/side/game-directory context");
    }
}
