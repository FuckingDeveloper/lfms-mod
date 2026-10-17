package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.target.InstalledTargetAdapters;
import java.lang.invoke.*;
import java.util.*;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.resources.sounds.SoundInstance;

/** Source void playback and public overrides retain their own observable contract. */
public final class Forge1192SoundManagerBridge {
    private Forge1192SoundManagerBridge() {}
    // Resolve the installed client service only when this client bridge is used.
    // The shared composition class therefore has no client-only method signatures.
    private static final MethodHandle PLAY=target("play",MethodType.methodType(void.class,SoundManager.class,SoundInstance.class,boolean.class));
    private static final MethodHandle PAUSE=target("pause",MethodType.methodType(void.class,SoundManager.class,boolean.class));
    private static final MethodHandle DEBUG=target("debug",MethodType.methodType(String.class,SoundManager.class,boolean.class));
    private static MethodHandle target(String name,MethodType type) {
        try {
            Class<?> target=Class.forName(InstalledTargetAdapters.soundManagerPlaybackBridge().replace('/', '.'),false,Forge1192SoundManagerBridge.class.getClassLoader());
            return MethodHandles.lookup().findStatic(target,name,type);
        } catch(ReflectiveOperationException failure) {throw new ExceptionInInitializerError(failure);}
    }
    private static void targetPlay(SoundManager manager,SoundInstance instance,boolean base) {
        try {PLAY.invokeExact(manager,instance,base);}
        catch(RuntimeException|Error failure){throw failure;}
        catch(Throwable failure){throw new IllegalStateException("Bound sound playback failed",failure);}
    }
    private static void targetPause(SoundManager manager,boolean base) {
        try {PAUSE.invokeExact(manager,base);}
        catch(RuntimeException|Error failure){throw failure;}
        catch(Throwable failure){throw new IllegalStateException("Bound sound pause failed",failure);}
    }
    private static String targetDebug(SoundManager manager,boolean base) {
        try {return (String)DEBUG.invokeExact(manager,base);}
        catch(RuntimeException|Error failure){throw failure;}
        catch(Throwable failure){throw new IllegalStateException("Bound sound diagnostics failed",failure);}
    }
    private record Operation(String key,List<String> names,Class<?> result,Class<?>... arguments) {}
    private static final List<Operation> OPERATIONS=List.of(
            new Operation("play",List.of("m_120367_","play"),void.class,SoundInstance.class),
            new Operation("pause",List.of("m_120391_","pause"),void.class),
            new Operation("debug",List.of("m_120408_","getDebugString"),String.class));
    private static final ClassValue<Map<String,MethodHandle>> SOURCE=new ClassValue<>() {
        @Override protected Map<String,MethodHandle> computeValue(Class<?> type) {
            var result=new HashMap<String,MethodHandle>();
            for(var operation:OPERATIONS)for(String name:operation.names) {
                try {
                    var method=type.getMethod(name,operation.arguments);
                    if(method.getReturnType()!=operation.result || java.lang.reflect.Modifier.isStatic(method.getModifiers())
                            || !(method.getDeclaringClass().getClassLoader() instanceof ManagedLegacyClassLoader))continue;
                    var handle=MethodHandles.privateLookupIn(method.getDeclaringClass(),MethodHandles.lookup()).unreflect(method);
                    if(result.putIfAbsent(operation.key,handle)!=null)
                        throw new UnsupportedOperationException("Ambiguous source sound manager aliases: "+type.getName()+"#"+operation.key);
                } catch(NoSuchMethodException absent) {
                    // This exact source callback is not declared; use the target virtual operation.
                } catch(IllegalAccessException failure) {throw new IllegalStateException("Cannot access source sound callback",failure);}
            }
            return Map.copyOf(result);
        }
    };
    private static Object invoke(MethodHandle callback,Object... args) {
        try {return callback.invokeWithArguments(args);}
        catch(RuntimeException|Error failure){throw failure;}
        catch(Throwable failure){throw new IllegalStateException("Source sound manager callback failed",failure);}
    }
    public static void play(SoundManager manager,SoundInstance instance) {
        var callback=SOURCE.get(manager.getClass()).get("play");
        if(callback!=null)invoke(callback,manager,instance);else targetPlay(manager,instance,false);
    }
    public static void basePlay(SoundManager manager,SoundInstance instance) {targetPlay(manager,instance,true);}
    public static void pause(SoundManager manager) {
        var callback=SOURCE.get(manager.getClass()).get("pause");
        if(callback!=null)invoke(callback,manager);else targetPause(manager,false);
    }
    public static void basePause(SoundManager manager) {targetPause(manager,true);}
    public static String debug(SoundManager manager) {
        var callback=SOURCE.get(manager.getClass()).get("debug");
        return callback!=null?(String)invoke(callback,manager):targetDebug(manager,false);
    }
    public static String baseDebug(SoundManager manager) {return targetDebug(manager,true);}
}
