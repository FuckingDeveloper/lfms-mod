package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.Forge1192LifecycleBridge;
import java.nio.file.*;
import java.util.*;
import java.util.function.Supplier;
import java.util.jar.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforgespi.language.IModInfo;
import org.objectweb.asm.*;
import static org.objectweb.asm.Opcodes.*;

/** Genuine JDK provider discovery over immutable owner/dependency resources and scoped context. */
public final class ManagedServiceResourceCheck {
    static void run() throws Exception {
        var directory=Files.createTempDirectory("lms-managed-services");
        Path owner=directory.resolve("owner space.jar"),dependency=directory.resolve("dependency.jar"),parentJar=directory.resolve("native-parent.jar"),hidden=directory.resolve("hidden.jar");
        write(owner,"Owned","owner");write(dependency,"Dependency","dependency");write(parentJar,"Parent","parent");write(hidden,"Hidden","hidden");
        byte[] original=Files.readAllBytes(owner);ClassLoader previous=Thread.currentThread().getContextClassLoader();
        var bus=BusBuilder.builder().build();bus.start();
        var info=(IModInfo)java.lang.reflect.Proxy.newProxyInstance(ManagedServiceResourceCheck.class.getClassLoader(),new Class[]{IModInfo.class},(p,m,a)->{if(m.getName().equals("getModId"))return "service_host";throw new AssertionError(m);});
        var host=new ModContainer(info){public IEventBus getEventBus(){return bus;}};
        try(var parent=new java.net.URLClassLoader(new java.net.URL[]{parentJar.toUri().toURL()},ManagedServiceResourceCheck.class.getClassLoader());
            var loader=new ManagedLegacyClassLoader(owner,List.of(dependency),parent,(name,bytes)->com.fuckingdeveloper.lms.classloading.LegacyClassTransformer.Result.unchanged(bytes,"independent JDK service fixture"));
            var second=new ManagedLegacyClassLoader(hidden,parent)) {
            var urls=Collections.list(loader.getResources("test/space name.txt"));check(urls.size()==3,"resource graph/order count");
            var values=new ArrayList<String>();for(var url:urls)try(var input=url.openStream()){values.add(new String(input.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8));}
            check(values.equals(List.of("owner","dependency","parent")),"owner/dependency/native-parent ordering");
            try(var input=loader.getResourceAsStream("test/space name.txt")){check(new String(input.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8).equals("owner"),"single resource ownership/escaped name");}
            check(loader.getResource("only/hidden.txt")==null,"undeclared sibling resource exposed");
            try(var scope=Forge1192LifecycleBridge.enter("service_fixture",bus,host,loader)) {
                check(Thread.currentThread().getContextClassLoader()==loader,"source context loader not installed");
                var providers=ServiceLoader.load(Supplier.class).stream().map(p->p.get().get()).toList();
                check(providers.equals(List.of("owner","dependency","parent")),"genuine JDK provider discovery/identity/order");
                var active=Forge1192LifecycleBridge.requireActive();
                try(var same=Forge1192LifecycleBridge.enterRecorded("service_fixture")){check(active==Forge1192LifecycleBridge.requireActive()&&Thread.currentThread().getContextClassLoader()==loader,"same-owner context changed");}
            }
            check(Thread.currentThread().getContextClassLoader()==previous,"initialization context loader leaked");
            try(var scope=Forge1192LifecycleBridge.enter("service_second",bus,host,second)) {
                try(var callback=Forge1192LifecycleBridge.enterCallback("service_fixture")) {
                    check(Thread.currentThread().getContextClassLoader()==loader,"cross-owner callback loader");
                    try(var nested=Forge1192LifecycleBridge.enterRecorded("service_fixture")){throw new IllegalArgumentException("fixture failure");}
                }catch(IllegalArgumentException expected){check(expected.getMessage().equals("fixture failure"),"scope failure changed");}
                check(Thread.currentThread().getContextClassLoader()==second&&Forge1192LifecycleBridge.requireActive().modId().equals("service_second"),"cross-owner failure restoration");
            }
            check(Thread.currentThread().getContextClassLoader()==previous&&Forge1192LifecycleBridge.currentContext().isEmpty(),"outer callback scope leaked");
            Forge1192LifecycleBridge.release("service_fixture",loader);Forge1192LifecycleBridge.release("service_second",second);
            try{Forge1192LifecycleBridge.enterRecorded("service_fixture");throw new AssertionError("disposed binding retained");}catch(IllegalStateException expected){}
            check(Arrays.equals(original,Files.readAllBytes(owner)),"immutable service artifact changed");
            var retained=loader.getResource("test/space name.txt");loader.close();
            try{retained.openStream();throw new AssertionError("closed-loader resource reopened");}catch(java.io.IOException expected){}
            try{loader.getResource("test/space name.txt");throw new AssertionError("closed graph queried");}catch(IllegalStateException expected){}
        }finally{Thread.currentThread().setContextClassLoader(previous);for(var path:List.of(owner,dependency,parentJar,hidden))Files.deleteIfExists(path);Files.deleteIfExists(directory);}
        System.out.println("PASS: genuine JDK ServiceLoader, immutable owner/dependency/native-parent resource graph/order, encoded names, hidden sibling exclusion, same/cross-owner context/failure restoration and disposed binding/resource closure");
    }
    private static void write(Path path,String type,String value)throws Exception{try(var jar=new JarOutputStream(Files.newOutputStream(path))){entry(jar,"probe/"+type+".class",provider(type,value));entry(jar,"META-INF/services/java.util.function.Supplier",("probe."+type+"\n").getBytes(java.nio.charset.StandardCharsets.UTF_8));entry(jar,"test/space name.txt",value.getBytes(java.nio.charset.StandardCharsets.UTF_8));entry(jar,"only/"+value+".txt",value.getBytes(java.nio.charset.StandardCharsets.UTF_8));}}
    private static void entry(JarOutputStream jar,String name,byte[] bytes)throws Exception{jar.putNextEntry(new JarEntry(name));jar.write(bytes);jar.closeEntry();}
    private static byte[] provider(String type,String value){var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);w.visit(V17,ACC_PUBLIC,"probe/"+type,null,"java/lang/Object",new String[]{"java/util/function/Supplier"});var m=w.visitMethod(ACC_PUBLIC,"<init>","()V",null,null);m.visitCode();m.visitVarInsn(ALOAD,0);m.visitMethodInsn(INVOKESPECIAL,"java/lang/Object","<init>","()V",false);m.visitInsn(RETURN);m.visitMaxs(0,0);m.visitEnd();m=w.visitMethod(ACC_PUBLIC,"get","()Ljava/lang/Object;",null,null);m.visitCode();m.visitLdcInsn(value);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();w.visitEnd();return w.toByteArray();}
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
