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

public final class SourceContainerFamilyCheck {
    public static final class Probe extends Event {}
    private static final List<String> AUTOMATIC=new ArrayList<>();
    public static void record(String id){
        check(Forge1192LifecycleBridge.requireActive().modId().equals(id),"automatic subscriber lost source ownership");AUTOMATIC.add(id);
    }
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
            AUTOMATIC.clear();bus.post(new Probe());
            check(AUTOMATIC.equals(List.of("source_view_b","source_view_a")),"automatic source subscriptions missing or on wrong bus");
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
        AUTOMATIC.clear();bus.post(new Probe());check(AUTOMATIC.isEmpty(),"closed automatic subscribers retained");
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
            for(var entry:Map.of("fixture/Automatic",autoClass(id,true,false),"fixture/WrongSide",autoClass(id,false,false),"fixture/WrongOwner",autoClass(id,true,true)).entrySet()) {
                output.putNextEntry(new JarEntry(entry.getKey()+".class"));output.write(entry.getValue());output.closeEntry();
            }
            output.putNextEntry(new JarEntry("META-INF/mods.toml"));
            String toml="modLoader='javafml'\nloaderVersion='[43,)'\nlicense='Test'\n[[mods]]\nmodId='"+id+"'\nversion='1.0'\ndisplayName='Source "+id+"'\n";
            if(dependency!=null)toml+="[[dependencies."+id+"]]\nmodId='"+dependency+"'\nmandatory=true\nversionRange='[1,)'\n";
            output.write(toml.getBytes(java.nio.charset.StandardCharsets.UTF_8));output.closeEntry();
        }
        return jar;
    }
    private static byte[] autoClass(String id,boolean correctSide,boolean wrongOwner) {
        String name=wrongOwner?"fixture/WrongOwner":correctSide?"fixture/Automatic":"fixture/WrongSide";
        var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);
        writer.visit(Opcodes.V17,Opcodes.ACC_PUBLIC,name,null,correctSide&&!wrongOwner?"java/lang/Object":"fixture/MustNeverLoad",null);
        var annotation=writer.visitAnnotation("Lnet/minecraftforge/fml/common/Mod$EventBusSubscriber;",true);
        annotation.visit("modid",wrongOwner?"unrelated_owner":id);
        annotation.visitEnum("bus","Lnet/minecraftforge/fml/common/Mod$EventBusSubscriber$Bus;","MOD");
        var sides=annotation.visitArray("value");
        String side=net.neoforged.fml.loading.FMLEnvironment.getDist().name();
        if(!correctSide)side=side.equals("CLIENT")?"DEDICATED_SERVER":"CLIENT";
        sides.visitEnum(null,"Lnet/minecraftforge/api/distmarker/Dist;",side);sides.visitEnd();annotation.visitEnd();
        var method=writer.visitMethod(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"onProbe","(Lcom/fuckingdeveloper/lms/check/SourceContainerFamilyCheck$Probe;)V",null,null);
        method.visitAnnotation("Lnet/minecraftforge/eventbus/api/SubscribeEvent;",true).visitEnd();method.visitCode();
        method.visitLdcInsn(id);method.visitMethodInsn(Opcodes.INVOKESTATIC,"com/fuckingdeveloper/lms/check/SourceContainerFamilyCheck","record","(Ljava/lang/String;)V",false);
        method.visitInsn(Opcodes.RETURN);method.visitMaxs(0,0);method.visitEnd();writer.visitEnd();return writer.toByteArray();
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
