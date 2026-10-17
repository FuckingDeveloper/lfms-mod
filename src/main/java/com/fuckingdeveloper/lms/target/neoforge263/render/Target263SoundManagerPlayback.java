package com.fuckingdeveloper.lms.target.neoforge263.render;

import java.lang.invoke.*;
import net.minecraft.client.sounds.*;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;

/** Actual native dispatch; only the new result is unobservable to source void callers. */
public final class Target263SoundManagerPlayback {
    private Target263SoundManagerPlayback() {}
    private static final SoundSource[] ALL=new SoundSource[0];
    private static final MethodHandle PLAY=base("play",MethodType.methodType(SoundEngine.PlayResult.class,SoundInstance.class));
    private static final MethodHandle PAUSE=base("pauseAllExcept",MethodType.methodType(void.class,SoundSource[].class));
    private static final MethodHandle DEBUG=base("getChannelDebugString",MethodType.methodType(String.class));
    private static MethodHandle base(String name,MethodType type) {
        try {return MethodHandles.privateLookupIn(SoundManager.class,MethodHandles.lookup()).findSpecial(SoundManager.class,name,type,SoundManager.class).asFixedArity();}
        catch(ReflectiveOperationException failure){throw new ExceptionInInitializerError(failure);}
    }
    public static void play(SoundManager manager,SoundInstance instance,boolean base) {
        if(!base){manager.play(instance);return;}
        try {var ignored=(SoundEngine.PlayResult)PLAY.invokeExact(manager,instance);}
        catch(RuntimeException|Error failure){throw failure;}
        catch(Throwable failure){throw new IllegalStateException("Native base sound playback failed",failure);}
    }
    public static void pause(SoundManager manager,boolean base) {
        if(!base){manager.pauseAllExcept(ALL);return;}
        try {PAUSE.invokeExact(manager,ALL);}
        catch(RuntimeException|Error failure){throw failure;}
        catch(Throwable failure){throw new IllegalStateException("Native base sound pause failed",failure);}
    }
    public static String debug(SoundManager manager,boolean base) {
        if(!base)return manager.getChannelDebugString();
        try {return (String)DEBUG.invokeExact(manager);}
        catch(RuntimeException|Error failure){throw failure;}
        catch(Throwable failure){throw new IllegalStateException("Native base sound diagnostics failed",failure);}
    }
}
