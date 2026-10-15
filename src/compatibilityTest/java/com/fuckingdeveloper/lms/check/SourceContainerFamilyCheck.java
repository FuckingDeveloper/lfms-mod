package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.runtime.*;
import com.fuckingdeveloper.lms.runtime.render.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.neoforged.bus.api.*;
import net.neoforged.fml.ModContainer;
import org.objectweb.asm.*;

final class SourceContainerFamilyCheck {
    static void run(ModContainer host) throws Exception {
        var first=fixture("source_view_a","source_view_b");var second=fixture("source_view_b",null);
        var aDescriptor=new com.fuckingdeveloper.lms.discovery.LegacyModDescriptor(first,"source_view_a","FORGE_METADATA","[1.19.2]","fixture");
        var bDescriptor=new com.fuckingdeveloper.lms.discovery.LegacyModDescriptor(second,"source_view_b","FORGE_METADATA","[1.19.2]","fixture");
        check(Forge1192SessionOrder.order(List.of(aDescriptor,bDescriptor),new com.fuckingdeveloper.lms.profile.Forge1192Profile()).equals(List.of(bDescriptor,aDescriptor)),"source admission depends on filename/input order");
        var bus=BusBuilder.builder().build();
        var a=new Forge1192RuntimeSession("source_view_a",first,SourceContainerFamilyCheck.class.getClassLoader(),List.of("fixture.Owner"));
        var b=new Forge1192RuntimeSession("source_view_b",second,SourceContainerFamilyCheck.class.getClassLoader(),List.of("fixture.Owner"));
        try(a;b) {
            a.bindModEventBus(bus,host);b.bindModEventBus(bus,host);
            check(!Forge1192ModListView.get().isLoaded(a.modId()),"linked session advertised loaded");
            var av=Forge1192ModListView.sourceOwner(a.modId());var bv=Forge1192ModListView.sourceOwner(b.modId());
            check(av.getModInfo().getDisplayName().equals("Source source_view_a")&&av.nativeOwner().isEmpty(),"managed metadata/owner is host identity");
            check(Forge1192ModListView.ordered(List.of(av,bv)).equals(List.of(bv,av)),"source dependency order lost");
            var count=new AtomicInteger();var source=new LegacyConfigScreenFactory((client,parent)->parent);
            av.registerExtensionPoint(LegacyConfigScreenFactory.class,()->{count.incrementAndGet();return source;});
            check(count.get()==0&&bv.getCustomExtension(LegacyConfigScreenFactory.class).isEmpty(),"source extension eager or crossed owner");
            check(av.getCustomExtension(LegacyConfigScreenFactory.class).orElseThrow()==source&&count.get()==1,"source extension identity lost");
            b.initialize();a.initialize();
            try(var consumer=new com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader(first,List.of(second),
                    SourceContainerFamilyCheck.class.getClassLoader(),new com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer(),
                    Forge1192ModListView.sharedDependencies(List.of(second)))) {
                // Primary classes retain priority even if the dependency exports the same binary name.
                check(consumer.linkOwnedClass("fixture.Owner").getClassLoader()==consumer,"dependency overrode primary artifact");
                check(consumer.linkOwnedClass("fixture.Shared")==Class.forName("fixture.Shared",false,Forge1192ModListView.sharedDependencies(List.of(second)).get(second)),"dependency defined duplicate Class identity");
            }

            check(Forge1192ModListView.get().getModContainerById(a.modId()).orElseThrow()==av,"source lookup identity unstable");
            var wrapped=Forge1192ModListView.wrap(host);
            check(wrapped.nativeOwner().orElseThrow()==host&&wrapped.getModInfo()==host.getModInfo(),"native owner/metadata replaced");
            check(Forge1192ModListView.merge(List.of(wrapped),List.of(av,bv)).stream().filter(v->v==av||v==bv).toList().equals(List.of(bv,av)),"actual source enumeration order");
        } finally {Files.deleteIfExists(first);Files.deleteIfExists(second);}
        try {Forge1192ModListView.sourceOwner("source_view_a");throw new AssertionError("closed source container retained");}
        catch(IllegalStateException expected){}
        System.out.println("PASS: source metadata/identity, dependency order, scoped lazy extensions, native owner, actual source lifecycle/lambda view and close cleanup");
    }
    private static Path fixture(String id,String dependency) throws Exception {
        var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);
        writer.visit(Opcodes.V17,Opcodes.ACC_PUBLIC,"fixture/Owner",null,"java/lang/Object",null);
        var ctor=writer.visitMethod(Opcodes.ACC_PUBLIC,"<init>","()V",null,null);ctor.visitCode();
        ctor.visitVarInsn(Opcodes.ALOAD,0);ctor.visitMethodInsn(Opcodes.INVOKESPECIAL,"java/lang/Object","<init>","()V",false);
        ctor.visitMethodInsn(Opcodes.INVOKESTATIC,"net/minecraftforge/fml/ModLoadingContext","get","()Lnet/minecraftforge/fml/ModLoadingContext;",false);
        ctor.visitMethodInsn(Opcodes.INVOKEVIRTUAL,"net/minecraftforge/fml/ModLoadingContext","getActiveContainer","()Lnet/minecraftforge/fml/ModContainer;",false);
        ctor.visitMethodInsn(Opcodes.INVOKEVIRTUAL,"net/minecraftforge/fml/ModContainer","getModId","()Ljava/lang/String;",false);
        ctor.visitLdcInsn(id);ctor.visitMethodInsn(Opcodes.INVOKEVIRTUAL,"java/lang/String","equals","(Ljava/lang/Object;)Z",false);
        var okay=new Label();ctor.visitJumpInsn(Opcodes.IFNE,okay);
        ctor.visitTypeInsn(Opcodes.NEW,"java/lang/AssertionError");ctor.visitInsn(Opcodes.DUP);ctor.visitMethodInsn(Opcodes.INVOKESPECIAL,"java/lang/AssertionError","<init>","()V",false);ctor.visitInsn(Opcodes.ATHROW);
        ctor.visitLabel(okay);ctor.visitFrame(Opcodes.F_FULL,1,new Object[]{"fixture/Owner"},0,new Object[]{});ctor.visitInsn(Opcodes.RETURN);ctor.visitMaxs(0,0);ctor.visitEnd();writer.visitEnd();
        Path jar=Files.createTempFile("lms-source-container-",".jar");
        try(var output=new JarOutputStream(Files.newOutputStream(jar))) {
            output.putNextEntry(new JarEntry("fixture/Owner.class"));output.write(writer.toByteArray());output.closeEntry();
            if(dependency==null) {
                var shared=new ClassWriter(0);shared.visit(Opcodes.V17,Opcodes.ACC_PUBLIC,"fixture/Shared",null,"java/lang/Object",null);shared.visitEnd();
                output.putNextEntry(new JarEntry("fixture/Shared.class"));output.write(shared.toByteArray());output.closeEntry();
            }
            output.putNextEntry(new JarEntry("META-INF/mods.toml"));
            String toml="modLoader='javafml'\nloaderVersion='[43,)'\nlicense='Test'\n[[mods]]\nmodId='"+id+"'\nversion='1.0'\ndisplayName='Source "+id+"'\n";
            if(dependency!=null)toml+="[[dependencies."+id+"]]\nmodId='"+dependency+"'\nmandatory=true\nversionRange='[1,)'\n";
            output.write(toml.getBytes(java.nio.charset.StandardCharsets.UTF_8));output.closeEntry();
        }
        return jar;
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
