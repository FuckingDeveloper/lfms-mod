package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.world.Forge1192ExplosionBlockInteraction;
import com.fuckingdeveloper.lms.target.neoforge263.Target263SourceExplosions;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import static org.objectweb.asm.Opcodes.*;

/** Actual patched native execution path canceled at its normal event; no world or packets simulated. */
final class ExplosionExecutionFamilyCheck {
    private static final String LEVEL="net/minecraft/world/level/Level", MODE="Lnet/minecraft/world/level/Explosion$BlockInteraction;";
    private static final String DESC="(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/level/ExplosionDamageCalculator;DDDFZ"+MODE+")Lnet/minecraft/world/level/Explosion;";
    static void run() throws Exception {
        var node=new ClassNode();node.version=V17;node.access=ACC_PUBLIC;node.name="probe/SourceExplosions";node.superName="java/lang/Object";
        var m=new MethodNode(ACC_PUBLIC|ACC_STATIC,"call","(L"+LEVEL+";"+DESC.substring(1),null,null);
        int slot=0;for(var arg:Type.getArgumentTypes(m.desc)){m.instructions.add(new VarInsnNode(arg.getOpcode(ILOAD),slot));slot+=arg.getSize();}
        m.instructions.add(new MethodInsnNode(INVOKEVIRTUAL,"net/minecraft/server/level/ServerLevel","m_7703_",DESC,false));m.instructions.add(new InsnNode(ARETURN));node.methods.add(m);
        var h=new MethodNode(ACC_PUBLIC|ACC_STATIC,"handle","()Ljava/lang/invoke/MethodHandle;",null,null);h.instructions.add(new LdcInsnNode(new Handle(H_INVOKEVIRTUAL,LEVEL,"m_7703_",DESC,false)));h.instructions.add(new InsnNode(ARETURN));node.methods.add(h);
        for(String mode:List.of("NONE","BREAK","DESTROY")) {
            var getter=new MethodNode(ACC_PUBLIC|ACC_STATIC,mode,"()"+MODE,null,null);getter.instructions.add(new FieldInsnNode(GETSTATIC,"net/minecraft/world/level/Explosion$BlockInteraction",mode,MODE));getter.instructions.add(new InsnNode(ARETURN));node.methods.add(getter);
        }
        var querySymbols=Map.of("damage",new String[]{"m_46077_","Lnet/minecraft/world/damagesource/DamageSource;"},
                "players",new String[]{"m_46078_","Ljava/util/Map;"},"owner",new String[]{"m_46079_","Lnet/minecraft/world/entity/LivingEntity;"});
        for(var entry:querySymbols.entrySet()) {
            var query=new MethodNode(ACC_PUBLIC|ACC_STATIC,entry.getKey(),"(Lnet/minecraft/world/level/Explosion;)"+entry.getValue()[1],null,null);
            query.instructions.add(new VarInsnNode(ALOAD,0));query.instructions.add(new MethodInsnNode(INVOKEVIRTUAL,"net/minecraft/world/level/Explosion",entry.getValue()[0],"()"+entry.getValue()[1],false));query.instructions.add(new InsnNode(ARETURN));node.methods.add(query);
        }
        var f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);var unsafe=(sun.misc.Unsafe)f.get(null);
        var level=(ServerLevel)unsafe.allocateInstance(ServerLevel.class);var damage=(DamageSource)unsafe.allocateInstance(DamageSource.class);
        var path=Files.createTempFile("lms-explosions-", ".jar");var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);
        try {
            try(var jar=new JarOutputStream(Files.newOutputStream(path))){jar.putNextEntry(new JarEntry(node.name+".class"));jar.write(writer.toByteArray());jar.closeEntry();}
            try(var loader=new ManagedLegacyClassLoader(path,ExplosionExecutionFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
                var type=loader.linkOwnedClass("probe.SourceExplosions");
                var call=type.getMethod("call",Level.class,net.minecraft.world.entity.Entity.class,DamageSource.class,ExplosionDamageCalculator.class,double.class,double.class,double.class,float.class,boolean.class,Forge1192ExplosionBlockInteraction.class);
                var handle=(java.lang.invoke.MethodHandle)type.getMethod("handle").invoke(null);
                var observed=new ArrayList<ServerExplosion>();
                Consumer<ExplosionEvent.Start> listener=event->{observed.add(event.getExplosion());event.setCanceled(true);};
                NeoForge.EVENT_BUS.addListener(listener);
                try {
                    for(var mode:Forge1192ExplosionBlockInteraction.values()) {
                        check(type.getMethod(mode.name()).invoke(null)==mode,"source enum identity changed");
                        var explosion=(Explosion)call.invoke(null,level,null,damage,null,3.0,70.5,-2.0,4.5F,true,mode);
                        check(observed.size()==mode.ordinal()+1&&observed.getLast()==explosion,"real native result/event once-only lost");
                        check(explosion.getBlockInteraction()==Target263SourceExplosions.mode(mode),"source drop/destruction mode changed");
                        check(type.getMethod("damage",Explosion.class).invoke(null,explosion)==damage,"source damage query lost native identity");
                        check(type.getMethod("players",Explosion.class).invoke(null,explosion)==((ServerExplosion)explosion).getHitPlayers(),"source hit-player map copied/substituted");
                        check(type.getMethod("owner",Explosion.class).invoke(null,explosion)==null,"source owner query changed null owner");
                        check(explosion.radius()==4.5F&&explosion.center().equals(new net.minecraft.world.phys.Vec3(3,70.5,-2)),"source operands changed");
                    }
                    try{var result=(Explosion)handle.invokeWithArguments(level,null,damage,null,1.0,2.0,3.0,2F,false,Forge1192ExplosionBlockInteraction.NONE);check(result==observed.getLast(),"callable actual result lost");}
                    catch(Throwable failure){throw new AssertionError(failure);}
                    // With no source scope, a normal native explosion keeps its own mode.
                    level.explode(null,damage,null,1,2,3,1,false,Level.ExplosionInteraction.NONE);
                    check(observed.getLast().getBlockInteraction()==Explosion.BlockInteraction.KEEP,"source scope leaked into native call");
                } finally {NeoForge.EVENT_BUS.unregister(listener);}
                var nesting=new ArrayList<ServerExplosion>();var nested=new boolean[1];
                Consumer<ExplosionEvent.Start> nestingListener=event->{
                    nesting.add(event.getExplosion());event.setCanceled(true);
                    if(!nested[0]) {nested[0]=true;
                        var inner=Target263SourceExplosions.explode(level,null,damage,null,9,8,7,2,false,Forge1192ExplosionBlockInteraction.DESTROY);
                        check(inner==nesting.getLast()&&inner.getBlockInteraction()==Explosion.BlockInteraction.DESTROY_WITH_DECAY,"nested source result/mode lost");
                    }
                };
                NeoForge.EVENT_BUS.addListener(nestingListener);
                try {
                    var outer=Target263SourceExplosions.explode(level,null,damage,null,1,2,3,1,false,Forge1192ExplosionBlockInteraction.BREAK);
                    check(nesting.size()==2&&outer==nesting.getFirst()&&outer.getBlockInteraction()==Explosion.BlockInteraction.DESTROY,"nested context replaced outer result/mode");
                }finally{NeoForge.EVENT_BUS.unregister(nestingListener);}
                var failure=new IllegalArgumentException("native-start-failure");Consumer<ExplosionEvent.Start> throwing=event->{throw failure;};NeoForge.EVENT_BUS.addListener(throwing);
                try{Target263SourceExplosions.explode(level,null,damage,null,1,2,3,1,false,Forge1192ExplosionBlockInteraction.BREAK);throw new AssertionError("native event failure swallowed");}
                catch(IllegalArgumentException actual){check(actual==failure,"native failure identity lost");}
                finally{NeoForge.EVENT_BUS.unregister(throwing);}
                NeoForge.EVENT_BUS.addListener(listener);
                try {level.explode(null,damage,null,1,2,3,1,false,Level.ExplosionInteraction.NONE);check(observed.getLast().getBlockInteraction()==Explosion.BlockInteraction.KEEP,"failed source scope leaked");}
                finally{NeoForge.EVENT_BUS.unregister(listener);}
            }
        }finally{Files.deleteIfExists(path);}
        System.out.println("PASS: original source explosion enum identity and actual native canceled explosion result/event/mode/operands, source inherited call/handle, unrelated native scope and failure identity; real blast/packets need runtime");
    }
    private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
}
