package com.fuckingdeveloper.lms.target.neoforge263;

import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/** Source callback/work execution phase, not a changed native queue or a fabricated event. */
public final class Target263ClientSetupScope {
    private static final ThreadLocal<Integer> DEPTH=new ThreadLocal<>();
    private Target263ClientSetupScope(){}
    public static boolean active(){return DEPTH.get()!=null;}
    public static AutoCloseable enter(Object event) {
        if(!(event instanceof FMLClientSetupEvent))return ()->{};
        Integer previous=DEPTH.get();int current=previous==null?1:previous+1;Thread owner=Thread.currentThread();DEPTH.set(current);
        return ()->{
            if(Thread.currentThread()!=owner||!Integer.valueOf(current).equals(DEPTH.get()))throw new IllegalStateException("Client setup scope closed out of order or on another thread");
            if(previous==null)DEPTH.remove();else DEPTH.set(previous);
        };
    }
}
