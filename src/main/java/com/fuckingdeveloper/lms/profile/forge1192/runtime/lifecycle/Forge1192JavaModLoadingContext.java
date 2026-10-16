package com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle;

import net.neoforged.bus.api.IEventBus;

/** Separate typed source Java-context token; not a native FML container. */
public final class Forge1192JavaModLoadingContext {
    private final Forge1192LifecycleBridge.Context context;
    Forge1192JavaModLoadingContext(Forge1192LifecycleBridge.Context context){this.context=context;}
    public static Forge1192JavaModLoadingContext get(){var c=Forge1192LifecycleBridge.requireActive();return c.container().sourceJavaContext(c);}
    public IEventBus getModEventBus(){return Forge1192LifecycleBridge.getModEventBus(context);}
}
