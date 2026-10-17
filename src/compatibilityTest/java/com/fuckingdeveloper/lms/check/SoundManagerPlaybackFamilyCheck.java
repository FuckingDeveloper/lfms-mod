package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import java.lang.reflect.Proxy;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import net.minecraft.client.sounds.*;
import net.minecraft.client.resources.sounds.*;
import net.minecraft.sounds.SoundSource;
import static org.objectweb.asm.Opcodes.*;

final class SoundManagerPlaybackFamilyCheck {
    static final String MANAGER="net/minecraft/client/sounds/SoundManager", SOUND="Lnet/minecraft/client/resources/sounds/SoundInstance;";
    static class NativeManager extends SoundManager {
        int plays,pauses; SoundInstance last; SoundEngine.PlayResult result;
        NativeManager(){super(null);}
        @Override public SoundEngine.PlayResult play(SoundInstance instance){plays++;last=instance;return result;}
        @Override public void pauseAllExcept(SoundSource... ignored){check(ignored.length==0,"source pause excluded channels");pauses++;}
        @Override public String getChannelDebugString(){return "native-channel-stats";}
    }
    static class NativeEngine extends SoundEngine {
        int plays,pauses;SoundInstance last;PlayResult result;
        NativeEngine(){super(null,null,null);}
        @Override public PlayResult play(SoundInstance instance){plays++;last=instance;return result;}
        @Override public void pauseAllExcept(SoundSource... ignored){check(ignored.length==0,"base pause excluded channels");pauses++;}
        @Override public String getChannelDebugString(){return "engine-channel-stats";}
    }
    static void run() throws Exception {
        var calls=node("probe/SoundCalls","java/lang/Object");
        var own=node("probe/LegacySoundManager",MANAGER);
        own.fields.add(new FieldNode(ACC_PUBLIC,"calls","I",null,null));own.fields.add(new FieldNode(ACC_PUBLIC,"failure","Ljava/lang/RuntimeException;",null,null));
        var operations=Map.of("play",new String[]{"m_120367_","("+SOUND+")V"},"pause",new String[]{"m_120391_","()V"},"debug",new String[]{"m_120408_","()Ljava/lang/String;"});
        for(var entry:operations.entrySet()) {
            String key=entry.getKey(),name=entry.getValue()[0],desc=entry.getValue()[1];
            var caller=new MethodNode(ACC_PUBLIC|ACC_STATIC,key,"(L"+MANAGER+";"+desc.substring(1),null,null);
            caller.instructions.add(new VarInsnNode(ALOAD,0));if(key.equals("play"))caller.instructions.add(new VarInsnNode(ALOAD,1));caller.instructions.add(new MethodInsnNode(INVOKEVIRTUAL,MANAGER,name,desc,false));caller.instructions.add(new InsnNode(Type.getReturnType(desc).getOpcode(IRETURN)));calls.methods.add(caller);
            var handle=new MethodNode(ACC_PUBLIC|ACC_STATIC,key+"Handle","()Ljava/lang/invoke/MethodHandle;",null,null);handle.instructions.add(new LdcInsnNode(new Handle(H_INVOKEVIRTUAL,MANAGER,name,desc,false)));handle.instructions.add(new InsnNode(ARETURN));calls.methods.add(handle);
            var callback=new MethodNode(ACC_PUBLIC,name,desc,null,null);var ok=new LabelNode();
            callback.instructions.add(new VarInsnNode(ALOAD,0));callback.instructions.add(new FieldInsnNode(GETFIELD,own.name,"failure","Ljava/lang/RuntimeException;"));callback.instructions.add(new JumpInsnNode(IFNULL,ok));callback.instructions.add(new VarInsnNode(ALOAD,0));callback.instructions.add(new FieldInsnNode(GETFIELD,own.name,"failure","Ljava/lang/RuntimeException;"));callback.instructions.add(new InsnNode(ATHROW));callback.instructions.add(ok);
            callback.instructions.add(new VarInsnNode(ALOAD,0));callback.instructions.add(new InsnNode(DUP));callback.instructions.add(new FieldInsnNode(GETFIELD,own.name,"calls","I"));callback.instructions.add(new InsnNode(ICONST_1));callback.instructions.add(new InsnNode(IADD));callback.instructions.add(new FieldInsnNode(PUTFIELD,own.name,"calls","I"));
            callback.instructions.add(new VarInsnNode(ALOAD,0));if(key.equals("play"))callback.instructions.add(new VarInsnNode(ALOAD,1));callback.instructions.add(new MethodInsnNode(INVOKESPECIAL,MANAGER,name,desc,false));callback.instructions.add(new InsnNode(Type.getReturnType(desc).getOpcode(IRETURN)));own.methods.add(callback);
        }
        var f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);var unsafe=(sun.misc.Unsafe)f.get(null);
        var manager=(NativeManager)unsafe.allocateInstance(NativeManager.class);var engine=(NativeEngine)unsafe.allocateInstance(NativeEngine.class);engine.result=SoundEngine.PlayResult.STARTED;
        long engineOffset=unsafe.objectFieldOffset(SoundManager.class.getDeclaredField("soundEngine"));unsafe.putObject(manager,engineOffset,engine);
        var sound=(SoundInstance)Proxy.newProxyInstance(SoundManagerPlaybackFamilyCheck.class.getClassLoader(),new Class<?>[]{SoundInstance.class},(p,m,a)->{throw new AssertionError("Unexpected sound read: "+m);});
        Path path=Files.createTempFile("lms-sound-manager-", ".jar");
        try {
            try(var jar=new JarOutputStream(Files.newOutputStream(path))){for(var n:List.of(calls,own)){jar.putNextEntry(new JarEntry(n.name+".class"));jar.write(bytes(n));jar.closeEntry();}}
            byte[] original=Files.readAllBytes(path);
            try(var loader=new ManagedLegacyClassLoader(path,SoundManagerPlaybackFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
                var type=loader.linkOwnedClass("probe.SoundCalls");int expected=0;
                var play=type.getMethod("play",SoundManager.class,SoundInstance.class);
                var playHandle=(java.lang.invoke.MethodHandle)type.getMethod("playHandle").invoke(null);
                for(var result:SoundEngine.PlayResult.values()) {
                    manager.result=result;play.invoke(null,manager,sound);check(manager.plays==++expected&&manager.last==sound,"native play/result dispatch changed");
                    try{playHandle.invoke(manager,sound);}catch(Throwable failure){throw new AssertionError(failure);}
                    check(manager.plays==++expected,"play handle duplicated/skipped");
                }
                type.getMethod("pause",SoundManager.class).invoke(null,manager);check(manager.pauses==1,"native pause missing");
                check(type.getMethod("debug",SoundManager.class).invoke(null,manager).equals("native-channel-stats"),"native diagnostics fabricated");
                var sourceType=loader.linkOwnedClass("probe.LegacySoundManager");var source=(SoundManager)unsafe.allocateInstance(sourceType);unsafe.putObject(source,engineOffset,engine);
                play.invoke(null,source,sound);type.getMethod("pause",SoundManager.class).invoke(null,source);
                check(type.getMethod("debug",SoundManager.class).invoke(null,source).equals("engine-channel-stats"),"explicit base diagnostics lost");
                check(engine.plays==1&&engine.pauses==1&&engine.last==sound&&sourceType.getField("calls").getInt(source)==3,"source override/base dispatch duplicated/skipped");
                var failure=new IllegalArgumentException("source-audio-probe");sourceType.getField("failure").set(source,failure);
                try{play.invoke(null,source,sound);throw new AssertionError("source sound failure swallowed");}catch(java.lang.reflect.InvocationTargetException e){check(e.getCause()==failure,"source exception identity lost");}
                check(engine.plays==1,"playback continued after source failure");
            }
            check(Arrays.equals(original,Files.readAllBytes(path)),"source sound artifact changed");
        }finally{Files.deleteIfExists(path);}
        SoundManager.class.getMethod("playDelayed",SoundInstance.class,int.class);SoundManager.class.getMethod("stop",SoundInstance.class);SoundManager.class.getMethod("isActive",SoundInstance.class);SoundManager.class.getMethod("queueTickingSound",TickableSoundInstance.class);
        System.out.println("PASS: source sound manager play/pause/debug calls and handles, all native PlayResult values, original sound/once-only dispatch, source callbacks/super and failure identity, pause-all and debug values; actual sound needs runtime");
    }
    private static ClassNode node(String name,String parent){var n=new ClassNode();n.version=V17;n.access=ACC_PUBLIC;n.name=name;n.superName=parent;return n;}
    private static byte[] bytes(ClassNode n){var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);n.accept(w);return w.toByteArray();}
    private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
}
