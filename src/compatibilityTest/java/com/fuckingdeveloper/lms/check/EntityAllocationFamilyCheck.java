package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import java.nio.file.*;
import java.util.jar.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import static org.objectweb.asm.Opcodes.*;

final class EntityAllocationFamilyCheck {
    static void run() throws Exception {
        String root="net/minecraft/world/entity/EntityType",desc="(Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/entity/Entity;";
        var n=new ClassNode();n.version=V17;n.access=ACC_PUBLIC;n.name="probe/SourceAllocation";n.superName="java/lang/Object";
        var m=new MethodNode(ACC_PUBLIC|ACC_STATIC,"create","(L"+root+";"+desc.substring(1),null,null);
        m.instructions.add(new VarInsnNode(ALOAD,0));m.instructions.add(new VarInsnNode(ALOAD,1));m.instructions.add(new MethodInsnNode(INVOKEVIRTUAL,root,"m_20615_",desc,false));m.instructions.add(new InsnNode(ARETURN));n.methods.add(m);
        var h=new MethodNode(ACC_PUBLIC|ACC_STATIC,"handle","()Ljava/lang/invoke/MethodHandle;",null,null);h.instructions.add(new LdcInsnNode(new Handle(H_INVOKEVIRTUAL,root,"m_20615_",desc,false)));h.instructions.add(new InsnNode(ARETURN));n.methods.add(h);
        var f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);var unsafe=(sun.misc.Unsafe)f.get(null);
        var type=(EntityType<?>)unsafe.allocateInstance(EntityType.class);var level=(Level)unsafe.allocateInstance(net.minecraft.client.multiplayer.ClientLevel.class);
        var entity=(Entity)unsafe.allocateInstance(PrimedTnt.class);int[] calls={0};boolean[] returnNull={false};RuntimeException[] failure={null};
        EntityType.EntityFactory<Entity> factory=(actualType,actualLevel)->{
            if(actualType!=type||actualLevel!=level)throw new AssertionError("factory operands changed");calls[0]++;
            if(failure[0]!=null)throw failure[0];return returnNull[0]?null:entity;
        };
        unsafe.putObject(type,unsafe.objectFieldOffset(EntityType.class.getDeclaredField("factory")),factory);
        var p=Files.createTempFile("lms-entity-allocation-", ".jar");var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);n.accept(w);
        try {
            try(var jar=new JarOutputStream(Files.newOutputStream(p))){jar.putNextEntry(new JarEntry(n.name+".class"));jar.write(w.toByteArray());jar.closeEntry();}
            try(var loader=new ManagedLegacyClassLoader(p,EntityAllocationFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
                var source=loader.linkOwnedClass("probe.SourceAllocation");var create=source.getMethod("create",EntityType.class,Level.class);
                if(create.invoke(null,type,level)!=entity||calls[0]!=1)throw new AssertionError("raw allocation checked/spawned/substituted instead of factory");
                var handle=(java.lang.invoke.MethodHandle)source.getMethod("handle").invoke(null);
                try{if(handle.invoke(type,level)!=entity||calls[0]!=2)throw new AssertionError("callable factory count/identity");}catch(Throwable e){throw new AssertionError(e);}
                returnNull[0]=true;if(create.invoke(null,type,level)!=null||calls[0]!=3)throw new AssertionError("genuine null factory result lost");
                failure[0]=new IllegalArgumentException("factory-failure");try{create.invoke(null,type,level);throw new AssertionError("factory failure swallowed");}
                catch(java.lang.reflect.InvocationTargetException e){if(e.getCause()!=failure[0]||calls[0]!=4)throw new AssertionError("factory failure changed",e);}
            }
        }finally{Files.deleteIfExists(p);}
        System.out.println("PASS: raw source EntityType allocation through actual native ignoreChecks path, exact factory/type/level/result/null and exception identity, direct/callable counts; no spawning/finalization/world insertion claimed");
    }
}
