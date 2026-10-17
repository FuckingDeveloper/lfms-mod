package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.Forge1192LifecycleBridge;
import com.fuckingdeveloper.lms.target.neoforge263.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.jar.*;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.*;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforgespi.language.IModInfo;
import org.objectweb.asm.*;
import static org.objectweb.asm.Opcodes.*;

/** Native packet operands and original source callbacks; no registered entity or loaded-world claim. */
public final class SourceClientEntityFactoryCheck {
    private static Packet<?> packet;
    public static Packet<?> packet(){return packet;}
    static void run() throws Exception {
        var field=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");field.setAccessible(true);var unsafe=(sun.misc.Unsafe)field.get(null);
        @SuppressWarnings("unchecked") var type=(EntityType<Entity>)unsafe.allocateInstance(EntityType.class);var fallbackType=(EntityType<?>)unsafe.allocateInstance(EntityType.class);
        var level=(Level)unsafe.allocateInstance(net.minecraft.client.multiplayer.ClientLevel.class);
        var original=(Entity)unsafe.allocateInstance(PrimedTnt.class);
        var uuid=UUID.randomUUID();packet=new ClientboundAddEntityPacket(17,uuid,2,3,4,45,90,type,0,new Vec3(0.25,0.5,-0.25),135);
        var view=new Target263SourceSpawnEntity((ClientboundAddEntityPacket)packet);
        check(view.getEntity()==null&&view.getEntityId()==17&&view.getUuid()==uuid&&view.getPosX()==2&&view.getPosY()==3&&view.getPosZ()==4&&view.getPitch()==32&&view.getYaw()==64&&view.getHeadYaw()==96&&view.getVelX()==2000,"actual native packet view changed source operands");
        try{view.getAdditionalData();throw new AssertionError("missing additional spawn bytes fabricated");}catch(UnsupportedOperationException expected){}
        var path=Files.createTempFile("lms-source-client-factory", ".jar");
        try {
            try(var jar=new JarOutputStream(Files.newOutputStream(path))){jar.putNextEntry(new JarEntry("probe/ClientFactoryEntity.class"));jar.write(fixture());jar.closeEntry();}
            byte[] immutable=Files.readAllBytes(path);
            var bus=BusBuilder.builder().build();bus.start();
            var info=(IModInfo)java.lang.reflect.Proxy.newProxyInstance(SourceClientEntityFactoryCheck.class.getClassLoader(),new Class[]{IModInfo.class},(p,m,a)->{if(m.getName().equals("getModId"))return "client_factory_host";throw new AssertionError(m);});
            var host=new ModContainer(info){public IEventBus getEventBus(){return bus;}};
            try(var loader=new ManagedLegacyClassLoader(path,SourceClientEntityFactoryCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
                var source=loader.linkOwnedClass("probe.ClientFactoryEntity");var builder=EntityType.Builder.of((t,l)->original,MobCategory.MISC);
                int[] called={0};boolean[] returnNull={false};RuntimeException[] failure={null};
                BiFunction<Target263SourceSpawnEntity,Level,Entity> factory=(actual,world)->{
                    check(Forge1192LifecycleBridge.requireActive().modId().equals("client_factory_owner")&&world==level&&actual.getEntityId()==17,"source factory scope/packet/level");called[0]++;
                    if(failure[0]!=null)throw failure[0];return returnNull[0]?null:original;
                };
                try(var scope=Forge1192LifecycleBridge.enter("client_factory_owner",bus,host)) {
                    check(source.getMethod("set",EntityType.Builder.class,BiFunction.class).invoke(null,builder,factory)==builder&&called[0]==0,"source client factory eagerly evaluated/fluent identity");
                    Target263SourceEntityFactories.bind(builder,type);
                }
                check(Target263SourceEntityFactories.create(type,level,(ClientboundAddEntityPacket)packet,()->{throw new AssertionError("custom source factory replaced by native fallback");})==original&&called[0]==1,"custom callback/result");
                returnNull[0]=true;check(Target263SourceEntityFactories.create(type,level,(ClientboundAddEntityPacket)packet,()->original)==null,"genuine source null result replaced");
                failure[0]=new IllegalArgumentException("source-factory-failure");
                try{Target263SourceEntityFactories.create(type,level,(ClientboundAddEntityPacket)packet,()->original);throw new AssertionError("source factory failure swallowed");}catch(IllegalArgumentException expected){check(expected==failure[0],"source exception changed");}
                check(Forge1192LifecycleBridge.currentContext().isEmpty(),"source factory scope leaked");
                check(Target263SourceEntityFactories.create(fallbackType,level,(ClientboundAddEntityPacket)packet,()->original)==original,"native-only factory changed");
                var entity=(Entity)unsafe.allocateInstance(source);var tracker=(ServerEntity)unsafe.allocateInstance(ServerEntity.class);
                check(entity.getAddEntityPacket(tracker)==packet,"native callback did not invoke original source packet method");
                check(source.getMethod("read",Target263SourceSpawnEntity.class).invoke(null,view).equals(17),"source view descriptor/member projection");
                try{Target263SourceEntityFactories.getEntitySpawningPacket(entity);throw new AssertionError("tracker context escaped callback");}catch(UnsupportedOperationException expected){}
                try{source.getMethod("hook",Entity.class).invoke(null,entity);throw new AssertionError("source static spawning hook bypassed tracker gate");}
                catch(java.lang.reflect.InvocationTargetException expectedHook){check(expectedHook.getCause() instanceof UnsupportedOperationException&&expectedHook.getCause().getMessage().contains("tracker"),"source spawning hook was not rewritten to native tracker bridge");}
                var handle=(java.lang.invoke.MethodHandle)source.getMethod("hookHandle").invoke(null);
                try{handle.invoke(entity);throw new AssertionError("static source hook handle bypassed tracker gate");}catch(UnsupportedOperationException expectedHook){}catch(Throwable failedHook){throw new AssertionError(failedHook);}
                check(Arrays.equals(immutable,Files.readAllBytes(path)),"original source JAR changed");
            }
        }finally{Files.deleteIfExists(path);packet=null;}
        System.out.println("PASS: native spawn packet view, source builder callback capture/bind and native-only fallback, exact type/level/result/null/failure/owner, source packet override/native tracker-scope cleanup and immutable source binary; registered entity spawning requires client");
    }
    private static byte[] fixture() {
        String name="probe/ClientFactoryEntity",builder="net/minecraft/world/entity/EntityType$Builder";
        var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);w.visit(V17,ACC_PUBLIC,name,null,"net/minecraft/world/entity/item/PrimedTnt",null);
        var m=w.visitMethod(ACC_PUBLIC,"m_5654_","()Lnet/minecraft/network/protocol/Packet;",null,null);m.visitCode();m.visitMethodInsn(INVOKESTATIC,"com/fuckingdeveloper/lms/check/SourceClientEntityFactoryCheck","packet","()Lnet/minecraft/network/protocol/Packet;",false);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"set","(L"+builder+";Ljava/util/function/BiFunction;)L"+builder+";",null,null);m.visitCode();m.visitVarInsn(ALOAD,0);m.visitVarInsn(ALOAD,1);m.visitMethodInsn(INVOKEVIRTUAL,builder,"setCustomClientFactory","(Ljava/util/function/BiFunction;)L"+builder+";",false);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"read","(Lnet/minecraftforge/network/PlayMessages$SpawnEntity;)I",null,null);m.visitCode();m.visitVarInsn(ALOAD,0);m.visitMethodInsn(INVOKEVIRTUAL,"net/minecraftforge/network/PlayMessages$SpawnEntity","getEntityId","()I",false);m.visitInsn(IRETURN);m.visitMaxs(0,0);m.visitEnd();
        String hookDesc="(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/network/protocol/Packet;";
        m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"hook",hookDesc,null,null);m.visitCode();m.visitVarInsn(ALOAD,0);m.visitMethodInsn(INVOKESTATIC,"net/minecraftforge/network/NetworkHooks","getEntitySpawningPacket",hookDesc,false);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"hookHandle","()Ljava/lang/invoke/MethodHandle;",null,null);m.visitCode();m.visitLdcInsn(new Handle(H_INVOKESTATIC,"net/minecraftforge/network/NetworkHooks","getEntitySpawningPacket",hookDesc,false));m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();w.visitEnd();return w.toByteArray();
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
