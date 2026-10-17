package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.*;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.Forge1192SourceScan;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import static org.objectweb.asm.Opcodes.*;

/** Independent immutable artifacts: reverse reflection, identity, conflicts and disposal. */
public final class SourceClassDomainCheck {
    private static ManagedClassDomain.Artifact artifact(Path path, boolean library) {
        return new ManagedClassDomain.Artifact(path,library,()->(name,bytes)->LegacyClassTransformer.Result.unchanged(bytes,"fixture"));
    }
    public static void run() throws Exception {
        Path dir=Files.createTempDirectory("source-domain-");
        Path host=dir.resolve("host.jar"), plugin=dir.resolve("plugin.jar"), api=dir.resolve("api.jar"), copy=dir.resolve("api-copy.jar"), conflict=dir.resolve("conflict.jar");
        write(host,Map.of("domain/Host.class",host(),"host.txt",new byte[]{1}));
        write(plugin,Map.of("domain/Plugin.class",plugin(),"META-INF/services/domain.Api","domain.Plugin\n".getBytes(),"plugin.txt",new byte[]{2}));
        write(api,Map.of("domain/Api.class",api()));Files.copy(api,copy);
        write(conflict,Map.of("domain/Api.class",api()));
        byte[] original=Files.readAllBytes(plugin);
        try {
            var inputs=List.of(artifact(host,false),artifact(plugin,false),artifact(api,true),artifact(copy,true));
            try(var domain=new ManagedClassDomain(inputs,SourceClassDomainCheck.class.getClassLoader())) {
                var h=domain.loader(host);var p=domain.loader(plugin);var a=domain.loader(api);
                check(a==domain.loader(copy),"whole library identity not deduplicated");
                Class<?> type=Class.forName("domain.Api",false,h);
                check(type==Class.forName("domain.Api",false,p)&&type.getClassLoader()==a,"shared API identity");
                Class<?> found=(Class<?>)h.linkOwnedClass("domain.Host").getMethod("discover").invoke(null);
                check(found==p.linkOwnedClass("domain.Plugin")&&type.isAssignableFrom(found),"ordinary caller-loader Class.forName or assignability");
                check(type.isInstance(found.getConstructor().newInstance()),"provider construction identity");
                var resources=Collections.list(h.getResources("META-INF/services/domain.Api"));
                check(resources.size()==1&&new String(resources.getFirst().openStream().readAllBytes()).contains("domain.Plugin"),"domain service resource");
                @SuppressWarnings({"rawtypes","unchecked"}) var providers=ServiceLoader.load((Class)type,h).stream().toList();
                check(providers.size()==1,"genuine Java cross-owner service lookup");
                var pool=Executors.newFixedThreadPool(4);
                try { var work=new ArrayList<Callable<Class<?>>>();for(int i=0;i<100;i++)work.add(()->Class.forName("domain.Plugin",true,h));
                    for(var result:pool.invokeAll(work,10,TimeUnit.SECONDS))check(result.get()==found,"concurrent identity");
                } finally { pool.shutdownNow(); }
                var scan=Forge1192SourceScan.read(plugin,List.of());
                var annotation=scan.getAnnotations().stream().filter(v->v.annotationType().getClassName().equals("net.neoforged.fml.common.Mod")).findFirst().orElseThrow();
                check(annotation.memberName().equals("domain.Plugin")&&annotation.annotationData().get("value").equals("fixture"),"projected annotation/class member data");
                try{scan.getAnnotations().clear();throw new AssertionError("mutable scan");}catch(UnsupportedOperationException expected){}
                var retained=p.getResource("plugin.txt");domain.block(p);
                try{h.loadClass("domain.Plugin");throw new AssertionError("blocked owner resolved");}catch(ClassNotFoundException expected){}
                p.close();try{retained.openStream();throw new AssertionError("closed resource reopened");}catch(java.io.IOException expected){}
                h.close();try{domain.loader(api);throw new AssertionError("domain retained after root disposal");}catch(IllegalStateException expected){}
            }
            for(var conflicts:List.of(List.of(artifact(api,false),artifact(conflict,false)),List.of(artifact(api,true),artifact(conflict,false)))) {
                try(var ignored=new ManagedClassDomain(conflicts,SourceClassDomainCheck.class.getClassLoader())){throw new AssertionError("conflicting top-level class accepted");}
                catch(IllegalArgumentException expected){check(expected.getMessage().contains("Conflicting managed class owner"),"wrong conflict diagnostic");}
            }
            try(var separate=new ManagedClassDomain(List.of(artifact(host,false)),SourceClassDomainCheck.class.getClassLoader())) {
                try{separate.loader(host).loadClass("domain.Plugin");throw new AssertionError("domain leaked sibling");}catch(ClassNotFoundException expected){}
            }
            check(Arrays.equals(original,Files.readAllBytes(plugin)),"legacy JAR changed");
        } finally { try(var paths=Files.walk(dir)){for(var path:paths.sorted(Comparator.reverseOrder()).toList())Files.delete(path);} }
        System.out.println("PASS: prepared source domain, reverse reflection/shared API identity, real ServiceLoader/resources, concurrent lookups, full library dedup/conflict rejection, immutable projected scan, blocked owner and domain disposal");
    }
    private static byte[] api(){var w=new ClassWriter(0);w.visit(V17,ACC_PUBLIC|ACC_ABSTRACT|ACC_INTERFACE,"domain/Api",null,"java/lang/Object",null);w.visitEnd();return w.toByteArray();}
    private static byte[] plugin(){var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);w.visit(V17,ACC_PUBLIC,"domain/Plugin",null,"java/lang/Object",new String[]{"domain/Api"});var a=w.visitAnnotation("Lnet/minecraftforge/fml/common/Mod;",true);a.visit("value","fixture");a.visitEnd();var m=w.visitMethod(ACC_PUBLIC,"<init>","()V",null,null);m.visitCode();m.visitVarInsn(ALOAD,0);m.visitMethodInsn(INVOKESPECIAL,"java/lang/Object","<init>","()V",false);m.visitInsn(RETURN);m.visitMaxs(0,0);m.visitEnd();w.visitEnd();return w.toByteArray();}
    private static byte[] host(){var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);w.visit(V17,ACC_PUBLIC,"domain/Host",null,"java/lang/Object",null);var m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"discover","()Ljava/lang/Class;",null,new String[]{"java/lang/ClassNotFoundException"});m.visitCode();m.visitLdcInsn("domain.Plugin");m.visitMethodInsn(INVOKESTATIC,"java/lang/Class","forName","(Ljava/lang/String;)Ljava/lang/Class;",false);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();w.visitEnd();return w.toByteArray();}
    private static void write(Path path,Map<String,byte[]> entries)throws Exception{try(var jar=new JarOutputStream(Files.newOutputStream(path))){for(var entry:entries.entrySet()){jar.putNextEntry(new JarEntry(entry.getKey()));jar.write(entry.getValue());jar.closeEntry();}}}
    private static void check(boolean result,String message){if(!result)throw new AssertionError(message);}
}
