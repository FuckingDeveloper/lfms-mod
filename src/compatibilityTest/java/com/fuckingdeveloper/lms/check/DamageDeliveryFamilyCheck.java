package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import java.nio.file.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import static org.objectweb.asm.Opcodes.*;

final class DamageDeliveryFamilyCheck {
    static final String ENTITY="net/minecraft/world/entity/Entity", DAMAGE="Lnet/minecraft/world/damagesource/DamageSource;";
    static final class NativeLoader extends ClassLoader {
        NativeLoader(){super(DamageDeliveryFamilyCheck.class.getClassLoader());}
        Class<?> define(ClassNode node){byte[] b=bytes(node);return defineClass(node.name.replace('/','.'),b,0,b.length);}
    }
    static void run() throws Exception {
        var caller=node("probe/DamageDelivery","java/lang/Object");
        var method=new MethodNode(ACC_PUBLIC|ACC_STATIC,"hurt","(L"+ENTITY+";"+DAMAGE+"F)Z",null,null);
        method.instructions.add(new VarInsnNode(ALOAD,0));method.instructions.add(new VarInsnNode(ALOAD,1));method.instructions.add(new VarInsnNode(FLOAD,2));
        method.instructions.add(new MethodInsnNode(INVOKEVIRTUAL,ENTITY,"m_6469_","("+DAMAGE+"F)Z",false));method.instructions.add(new InsnNode(IRETURN));caller.methods.add(method);
        for (var flag : java.util.Map.of("marked", "f_19864_", "impulse", "f_19812_").entrySet()) {
            var set=new MethodNode(ACC_PUBLIC|ACC_STATIC,flag.getKey(),"(L"+ENTITY+";Z)Z",null,null);
            set.instructions.add(new VarInsnNode(ALOAD,0));set.instructions.add(new VarInsnNode(ILOAD,1));set.instructions.add(new FieldInsnNode(PUTFIELD,ENTITY,flag.getValue(),"Z"));
            set.instructions.add(new VarInsnNode(ALOAD,0));set.instructions.add(new FieldInsnNode(GETFIELD,ENTITY,flag.getValue(),"Z"));set.instructions.add(new InsnNode(IRETURN));caller.methods.add(set);
            var getter=new MethodNode(ACC_PUBLIC|ACC_STATIC,flag.getKey()+"Handle","()Ljava/lang/invoke/MethodHandle;",null,null);
            getter.instructions.add(new LdcInsnNode(new Handle(H_GETFIELD,ENTITY,flag.getValue(),"Z",false)));getter.instructions.add(new InsnNode(ARETURN));caller.methods.add(getter);
        }
        var source=node("probe/SourceDamageVictim",ENTITY); record(source,"m_6469_","("+DAMAGE+"F)Z",1,2,false);
        var nativeNode=node("probe/NativeDamageVictim",ENTITY);
        record(nativeNode,"hurtServer","(Lnet/minecraft/server/level/ServerLevel;"+DAMAGE+"F)Z",2,3,true);
        record(nativeNode,"hurtClient","("+DAMAGE+")Z",1,-1,false);
        var field=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");field.setAccessible(true);var unsafe=(sun.misc.Unsafe)field.get(null);
        var worldField=Entity.class.getDeclaredField("level");long offset=unsafe.objectFieldOffset(worldField);
        var nativeType=new NativeLoader().define(nativeNode);var nativeVictim=(Entity)unsafe.allocateInstance(nativeType);
        var sourceDamage=new DamageSource(net.minecraft.core.Holder.direct(new DamageType("probe",0.1f)));
        Path archive=Files.createTempFile("lms-damage-delivery-", ".jar");
        try {
            try(var jar=new JarOutputStream(Files.newOutputStream(archive))) {
                for(var n:java.util.List.of(caller,source)){jar.putNextEntry(new JarEntry(n.name+".class"));jar.write(bytes(n));jar.closeEntry();}
            }
            try(var loader=new ManagedLegacyClassLoader(archive,DamageDeliveryFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
                var consumerType=loader.linkOwnedClass("probe.DamageDelivery");
                var consumer=consumerType.getMethod("hurt",Entity.class,DamageSource.class,float.class);
                for(String flag:java.util.List.of("marked","impulse")) {
                    var setter=consumerType.getMethod(flag,Entity.class,boolean.class);
                    check((boolean)setter.invoke(null,nativeVictim,true),"live source tracker set/read lost");
                    check(flag.equals("marked")?nativeVictim.syncVelocity:nativeVictim.needsSync,"detached source tracker flag");
                    var getter=(java.lang.invoke.MethodHandle)consumerType.getMethod(flag+"Handle").invoke(null);
                    try {check((boolean)getter.invoke(nativeVictim),"tracker field handle lost");}catch(Throwable failure){throw new AssertionError(failure);}
                    check(!(boolean)setter.invoke(null,nativeVictim,false),"live tracker reset lost");
                }
                unsafe.putObject(nativeVictim,offset,unsafe.allocateInstance(ServerLevel.class));
                check((boolean)consumer.invoke(null,nativeVictim,sourceDamage,7.25f),"native server result lost");
                check(nativeType.getField("calls").getInt(nativeVictim)==1&&nativeType.getField("amount").getFloat(nativeVictim)==7.25f&&nativeType.getField("source").get(nativeVictim)==sourceDamage,"server operands/callback count changed");
                unsafe.putObject(nativeVictim,offset,null); // Native side selector takes its client branch for non-ServerLevel.
                check(!(boolean)consumer.invoke(null,nativeVictim,sourceDamage,4f),"native client result lost");
                check(nativeType.getField("calls").getInt(nativeVictim)==2,"client callback skipped/duplicated");
                var sourceType=loader.linkOwnedClass("probe.SourceDamageVictim");var sourceVictim=(Entity)unsafe.allocateInstance(sourceType);
                check(!(boolean)consumer.invoke(null,sourceVictim,sourceDamage,2.5f),"source callback result lost");
                check(sourceType.getField("calls").getInt(sourceVictim)==1&&sourceType.getField("amount").getFloat(sourceVictim)==2.5f,"source callback skipped/duplicated");
            }
        } finally {Files.deleteIfExists(archive);}
        System.out.println("PASS: source virtual damage delivery, native server/client selector, original damage/amount/results, exactly-once managed callback; real damage events require runtime");
    }
    private static void record(ClassNode n,String name,String desc,int damage,int amount,boolean result) {
        if(n.fields.isEmpty()) {n.fields.add(new FieldNode(ACC_PUBLIC,"calls","I",null,null));n.fields.add(new FieldNode(ACC_PUBLIC,"amount","F",null,null));n.fields.add(new FieldNode(ACC_PUBLIC,"source",DAMAGE,null,null));}
        var m=new MethodNode(ACC_PUBLIC,name,desc,null,null);
        m.instructions.add(new VarInsnNode(ALOAD,0));m.instructions.add(new InsnNode(DUP));m.instructions.add(new FieldInsnNode(GETFIELD,n.name,"calls","I"));m.instructions.add(new InsnNode(ICONST_1));m.instructions.add(new InsnNode(IADD));m.instructions.add(new FieldInsnNode(PUTFIELD,n.name,"calls","I"));
        m.instructions.add(new VarInsnNode(ALOAD,0));m.instructions.add(new VarInsnNode(ALOAD,damage));m.instructions.add(new FieldInsnNode(PUTFIELD,n.name,"source",DAMAGE));
        if(amount>=0){m.instructions.add(new VarInsnNode(ALOAD,0));m.instructions.add(new VarInsnNode(FLOAD,amount));m.instructions.add(new FieldInsnNode(PUTFIELD,n.name,"amount","F"));}
        m.instructions.add(new InsnNode(result?ICONST_1:ICONST_0));m.instructions.add(new InsnNode(IRETURN));n.methods.add(m);
    }
    private static ClassNode node(String name,String parent){var n=new ClassNode();n.version=V17;n.access=ACC_PUBLIC;n.name=name;n.superName=parent;return n;}
    private static byte[] bytes(ClassNode n){var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);n.accept(w);return w.toByteArray();}
    private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
}
