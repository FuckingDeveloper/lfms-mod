package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import static org.objectweb.asm.Opcodes.*;

final class DamageFactoryFamilyCheck {
    static class Sources extends DamageSources {
        ResourceKey<DamageType> last;
        Sources() { super(RegistryAccess.EMPTY); }
        @Override public DamageSource source(ResourceKey<DamageType> key, Entity direct, Entity cause) {
            last=key;return new DamageSource(Holder.direct(new DamageType("probe",0.1f)),direct,cause);
        }
    }
    static final class NativeLoader extends ClassLoader {
        NativeLoader(){super(DamageFactoryFamilyCheck.class.getClassLoader());}
        Class<?> define(String name,byte[] bytes){return defineClass(name,bytes,0,bytes.length);}
    }
    static void run() throws Exception {
        String root="net/minecraft/world/damagesource/DamageSource", living="Lnet/minecraft/world/entity/LivingEntity;";
        String level="Lnet/minecraft/world/level/Level;", result="L"+root+";", sourceDesc="("+living+")"+result;
        var caller=node("probe/DamageFactories","java/lang/Object");
        for(boolean ambiguous:List.of(false,true)) {
            var method=new MethodNode(ACC_PUBLIC|ACC_STATIC,ambiguous?"ambiguous":"context","("+level+(ambiguous?level:"")+")"+result,null,null);
            // Source local reuse must not replace the captured formal-world evidence.
            method.instructions.add(new InsnNode(ACONST_NULL));method.instructions.add(new VarInsnNode(ASTORE,0));
            method.instructions.add(new InsnNode(ACONST_NULL));
            method.instructions.add(new MethodInsnNode(INVOKESTATIC,root,"m_19373_",sourceDesc,false));
            method.instructions.add(new InsnNode(ARETURN));caller.methods.add(method);
        }
        var handle=new MethodNode(ACC_PUBLIC|ACC_STATIC,"handle","()Ljava/lang/invoke/MethodHandle;",null,null);
        handle.instructions.add(new LdcInsnNode(new Handle(H_INVOKESTATIC,root,"m_19373_",sourceDesc,false)));handle.instructions.add(new InsnNode(ARETURN));caller.methods.add(handle);
        var nativeLevel=node("probe/DamageLevel","net/minecraft/world/level/Level");
        nativeLevel.fields.add(new FieldNode(ACC_PUBLIC,"sources","Lnet/minecraft/world/damagesource/DamageSources;",null,null));
        var sources=new MethodNode(ACC_PUBLIC,"damageSources","()Lnet/minecraft/world/damagesource/DamageSources;",null,null);
        sources.instructions.add(new VarInsnNode(ALOAD,0));sources.instructions.add(new FieldInsnNode(GETFIELD,nativeLevel.name,"sources","Lnet/minecraft/world/damagesource/DamageSources;"));sources.instructions.add(new InsnNode(ARETURN));nativeLevel.methods.add(sources);
        var field=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");field.setAccessible(true);var unsafe=(sun.misc.Unsafe)field.get(null);
        var worldType=new NativeLoader().define("probe.DamageLevel",bytes(nativeLevel));
        var world=(Level)unsafe.allocateInstance(worldType);var damage=(Sources)unsafe.allocateInstance(Sources.class);worldType.getField("sources").set(world,damage);
        Path archive=Files.createTempFile("lms-damage-factory-", ".jar");
        try {
            try(var jar=new JarOutputStream(Files.newOutputStream(archive))) {jar.putNextEntry(new JarEntry(caller.name+".class"));jar.write(bytes(caller));jar.closeEntry();}
            byte[] original=Files.readAllBytes(archive);
            try(var loader=new ManagedLegacyClassLoader(archive,DamageFactoryFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
                var type=loader.linkOwnedClass("probe.DamageFactories");
                var source=(DamageSource)type.getMethod("context",Level.class).invoke(null,world);
                check(damage.last.equals(DamageTypes.EXPLOSION)&&source.getEntity()==null&&source.getDirectEntity()==null,"native explosion key/context/attribution lost");
                try {type.getMethod("ambiguous",Level.class,Level.class).invoke(null,world,world);throw new AssertionError("ambiguous world guessed");}
                catch(java.lang.reflect.InvocationTargetException failure) {check(failure.getCause() instanceof UnsupportedOperationException,"wrong ambiguous failure");}
                var callable=(java.lang.invoke.MethodHandle)type.getMethod("handle").invoke(null);
                try {callable.invoke((LivingEntity)null);throw new AssertionError("ownerless handle guessed world");}
                catch(UnsupportedOperationException expected){} catch(Throwable failure){throw new AssertionError(failure);}
            }
            check(Arrays.equals(original,Files.readAllBytes(archive)),"damage input modified");
        } finally {Files.deleteIfExists(archive);}
        System.out.println("PASS: source damage factory explicit world capture despite local reuse, native explosion dispatch/key/attribution, ambiguous/null-handle boundaries and immutable artifact; full damage/registry roundtrip requires runtime");
    }
    private static ClassNode node(String name,String parent){var n=new ClassNode();n.version=V17;n.access=ACC_PUBLIC;n.name=name;n.superName=parent;return n;}
    private static byte[] bytes(ClassNode node){var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(w);return w.toByteArray();}
    private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
}
