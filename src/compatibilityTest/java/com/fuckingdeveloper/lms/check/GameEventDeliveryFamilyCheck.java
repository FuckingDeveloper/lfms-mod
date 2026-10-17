package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import net.minecraft.core.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.*;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import static org.objectweb.asm.Opcodes.*;

public final class GameEventDeliveryFamilyCheck {
    static int count;static Holder<GameEvent> last;static Vec3 position;static GameEvent.Context context;static RuntimeException failure;
    public static void record(Holder<GameEvent> event,Vec3 pos,GameEvent.Context ctx){count++;last=event;position=pos;context=ctx;if(failure!=null)throw failure;}
    public static class NativeEntity extends PrimedTnt {
        NativeEntity(){super(EntityTypes.TNT,null);}
        @Override public void gameEvent(Holder<GameEvent> event,Entity source){record(event,Vec3.ZERO,new GameEvent.Context(source,null));}
    }
    static void run() throws Exception {
        String root="net/minecraft/world/level/LevelAccessor",event="Lnet/minecraft/world/level/gameevent/GameEvent;",actor="Lnet/minecraft/world/entity/Entity;",vector="Lnet/minecraft/world/phys/Vec3;",block="Lnet/minecraft/core/BlockPos;",ctx="Lnet/minecraft/world/level/gameevent/GameEvent$Context;";
        var calls=node("probe/SourceGameEvents","java/lang/Object");
        String[] names={"m_214171_","m_220400_","m_142346_","m_220407_"},descs={"("+event+vector+ctx+")V","("+actor+event+vector+")V","("+actor+event+block+")V","("+event+block+ctx+")V"};
        for(int i=0;i<names.length;i++) {
            var m=new MethodNode(ACC_PUBLIC|ACC_STATIC,"delivery"+i,"(L"+root+";"+descs[i].substring(1),null,null);int slot=0;
            for(var arg:Type.getArgumentTypes(m.desc)){m.instructions.add(new VarInsnNode(arg.getOpcode(ILOAD),slot));slot+=arg.getSize();}
            m.instructions.add(new MethodInsnNode(INVOKEINTERFACE,root,names[i],descs[i],true));m.instructions.add(new InsnNode(RETURN));calls.methods.add(m);
        }
        for(int i=0;i<2;i++) {
            String desc="("+event+(i==0?actor:"")+")V";
            var m=new MethodNode(ACC_PUBLIC|ACC_STATIC,"entity"+i,"("+actor+desc.substring(1),null,null);int slot=0;
            for(var arg:Type.getArgumentTypes(m.desc)){m.instructions.add(new VarInsnNode(arg.getOpcode(ILOAD),slot));slot+=arg.getSize();}
            m.instructions.add(new MethodInsnNode(INVOKEVIRTUAL,"net/minecraft/world/entity/Entity",i==0?"m_146852_":"m_146850_",desc,false));m.instructions.add(new InsnNode(RETURN));calls.methods.add(m);
        }
        var constant=new MethodNode(ACC_PUBLIC|ACC_STATIC,"constant","()"+event,null,null);constant.instructions.add(new FieldInsnNode(GETSTATIC,"net/minecraft/world/level/gameevent/GameEvent","f_157776_",event));constant.instructions.add(new InsnNode(ARETURN));calls.methods.add(constant);
        var handle=new MethodNode(ACC_PUBLIC|ACC_STATIC,"constantHandle","()Ljava/lang/invoke/MethodHandle;",null,null);handle.instructions.add(new LdcInsnNode(new Handle(H_GETSTATIC,"net/minecraft/world/level/gameevent/GameEvent","f_157776_",event,false)));handle.instructions.add(new InsnNode(ARETURN));calls.methods.add(handle);
        var iface=node("probe/InterfaceGameEvent","java/lang/Object");iface.access|=ACC_INTERFACE|ACC_ABSTRACT;
        var interfaceHandle=new MethodNode(ACC_PUBLIC|ACC_STATIC,"handle","()Ljava/lang/invoke/MethodHandle;",null,null);interfaceHandle.instructions.add(new LdcInsnNode(new Handle(H_GETSTATIC,"net/minecraft/world/level/gameevent/GameEvent","f_157776_",event,false)));interfaceHandle.instructions.add(new InsnNode(ARETURN));iface.methods.add(interfaceHandle);
        var own=node("probe/RawEventOverride","net/minecraft/world/entity/item/PrimedTnt");var override=new MethodNode(ACC_PUBLIC,"m_146850_","("+event+")V",null,null);override.instructions.add(new InsnNode(RETURN));own.methods.add(override);
        var levelNode=node("probe/EventLevel","net/minecraft/client/multiplayer/ClientLevel");
        var delivery=new MethodNode(ACC_PUBLIC,"gameEvent","(Lnet/minecraft/core/Holder;"+vector+ctx+")V",null,null);delivery.instructions.add(new VarInsnNode(ALOAD,1));delivery.instructions.add(new VarInsnNode(ALOAD,2));delivery.instructions.add(new VarInsnNode(ALOAD,3));delivery.instructions.add(new MethodInsnNode(INVOKESTATIC,"com/fuckingdeveloper/lms/check/GameEventDeliveryFamilyCheck","record",delivery.desc,false));delivery.instructions.add(new InsnNode(RETURN));levelNode.methods.add(delivery);
        byte[] levelBytes=bytes(levelNode);var levelType=new ClassLoader(GameEventDeliveryFamilyCheck.class.getClassLoader()){Class<?> define(){return defineClass("probe.EventLevel",levelBytes,0,levelBytes.length);}}.define();
        var f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);var unsafe=(sun.misc.Unsafe)f.get(null);
        var level=(LevelAccessor)unsafe.allocateInstance(levelType);var entity=(NativeEntity)unsafe.allocateInstance(NativeEntity.class);var pos=new Vec3(1.25,72.5,-3.75);var blockPos=new BlockPos(2,73,-4);var input=new GameEvent.Context(entity,null);
        Path path=Files.createTempFile("lms-game-events-", ".jar");
        try {
            try(var jar=new JarOutputStream(Files.newOutputStream(path))){for(var n:List.of(calls,own,iface)){jar.putNextEntry(new JarEntry(n.name+".class"));jar.write(bytes(n));jar.closeEntry();}}
            try(var loader=new ManagedLegacyClassLoader(path,GameEventDeliveryFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
                var type=loader.linkOwnedClass("probe.SourceGameEvents");var raw=GameEvent.PRIME_FUSE.value();
                check(type.getMethod("constant").invoke(null)==raw,"native constant value substituted");
                try{check(((java.lang.invoke.MethodHandle)type.getMethod("constantHandle").invoke(null)).invoke()==raw,"constant handle lost identity");}catch(Throwable e){throw new AssertionError(e);}
                var interfaceType=loader.linkOwnedClass("probe.InterfaceGameEvent");
                try{check(((java.lang.invoke.MethodHandle)interfaceType.getMethod("handle").invoke(null)).invoke()==raw,"interface callable adapter reference kind changed");}catch(Throwable e){throw new AssertionError(e);}
                count=0;
                type.getMethod("delivery0",LevelAccessor.class,GameEvent.class,Vec3.class,GameEvent.Context.class).invoke(null,level,raw,pos,input);
                check(count==1&&last==GameEvent.PRIME_FUSE&&position==pos&&context==input,"core event operands/context/holder changed");
                type.getMethod("delivery1",LevelAccessor.class,Entity.class,GameEvent.class,Vec3.class).invoke(null,level,entity,raw,pos);
                check(count==2&&context.sourceEntity()==entity&&position==pos,"actor vector default changed");
                type.getMethod("delivery2",LevelAccessor.class,Entity.class,GameEvent.class,BlockPos.class).invoke(null,level,entity,raw,blockPos);
                check(count==3&&context.sourceEntity()==entity&&position.equals(Vec3.atCenterOf(blockPos)),"actor block-center default changed");
                type.getMethod("delivery3",LevelAccessor.class,GameEvent.class,BlockPos.class,GameEvent.Context.class).invoke(null,level,raw,blockPos,input);
                check(count==4&&context==input,"context block-center default changed");
                type.getMethod("entity0",Entity.class,GameEvent.class,Entity.class).invoke(null,entity,raw,null);check(count==5&&context.sourceEntity()==null,"explicit null actor changed");
                type.getMethod("entity1",Entity.class,GameEvent.class).invoke(null,entity,raw);check(count==6&&context.sourceEntity()==entity,"native self-actor default/override changed");
                failure=new IllegalArgumentException("event-entry-failure");try{type.getMethod("delivery0",LevelAccessor.class,GameEvent.class,Vec3.class,GameEvent.Context.class).invoke(null,level,raw,pos,input);throw new AssertionError("event failure swallowed");}
                catch(java.lang.reflect.InvocationTargetException expected){check(expected.getCause()==failure&&count==7,"event exception/count changed");}finally{failure=null;}
                try{loader.linkOwnedClass("probe.RawEventOverride");throw new AssertionError("source raw override silently bypassed");}
                catch(ClassNotFoundException expected){check(expected.getCause() instanceof UnsupportedOperationException,"wrong override boundary");}
            }
        }finally{Files.deleteIfExists(path);}
        System.out.println("PASS: real registered game-event raw constant/handle -> exact holder, four level and two entity source operations, native defaults/overrides, actor/null/context/center identity and once-only exception propagation; real listeners require world acceptance");
    }
    private static ClassNode node(String name,String parent){var n=new ClassNode();n.version=V17;n.access=ACC_PUBLIC;n.name=name;n.superName=parent;return n;}
    private static byte[] bytes(ClassNode n){var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);n.accept(w);return w.toByteArray();}
    private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
}
