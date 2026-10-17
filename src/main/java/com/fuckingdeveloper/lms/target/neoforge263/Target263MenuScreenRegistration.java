package com.fuckingdeveloper.lms.target.neoforge263;

import java.lang.invoke.*;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;

/** Retained native screen operation during valid source client setup, before consumers can open screens. */
public final class Target263MenuScreenRegistration {
    private static final MethodHandle GET=find("getConstructor",MethodType.methodType(MenuScreens.ScreenConstructor.class,MenuType.class));
    private static final MethodHandle REGISTER=find("register",MethodType.methodType(void.class,MenuType.class,MenuScreens.ScreenConstructor.class));
    private Target263MenuScreenRegistration(){}
    private static MethodHandle find(String name,MethodType type) {
        try{return MethodHandles.privateLookupIn(MenuScreens.class,MethodHandles.lookup()).findStatic(MenuScreens.class,name,type);}
        catch(ReflectiveOperationException failure){throw new ExceptionInInitializerError(failure);}
    }
    public static synchronized void register(MenuType<?> type,MenuScreens.ScreenConstructor<?,?> factory,String owner) {
        if(!Target263ClientSetupScope.active())throw new IllegalStateException("Source menu screen registered outside client setup: "+owner);
        try {
            // Native legacy register writes before checking duplicates; validate first to retain the existing factory on failure.
            if((MenuScreens.ScreenConstructor<?,?>)GET.invokeExact(type)!=null)throw new IllegalStateException("Duplicate source/native menu screen registration: "+owner);
            REGISTER.invokeExact(type,factory);
        }catch(RuntimeException|Error failure){throw failure;}
        catch(Throwable failure){throw new IllegalStateException("Native source screen registration failed: "+owner,failure);}
    }
}
