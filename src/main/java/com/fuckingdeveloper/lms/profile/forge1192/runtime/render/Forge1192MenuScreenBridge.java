package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.Forge1192LifecycleBridge;
import java.util.*;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** Source client-setup intent staged until the target's dedicated screen-registration event. */
public final class Forge1192MenuScreenBridge {
    private static final Queue GLOBAL=new Queue();
    private Forge1192MenuScreenBridge(){}
    public static void register(MenuType<?> type,MenuScreens.ScreenConstructor<?,?> factory){GLOBAL.add(type,factory,Forge1192LifecycleBridge.requireActive().modId());}
    @SuppressWarnings({"rawtypes","unchecked"})
    public static void registerDuringPhase(MenuType<?> type,MenuScreens.ScreenConstructor<?,?> factory,RegisterMenuScreensEvent event) {
        event.register((MenuType)type,(MenuScreens.ScreenConstructor)factory);
    }
    public static void flush(RegisterMenuScreensEvent event){GLOBAL.flush(event);}
    public static final class Queue {
        private record Entry(MenuScreens.ScreenConstructor<?,?> factory,String owner){}
        private final Map<MenuType<?>,Entry> pending=new LinkedHashMap<>();
        private boolean complete;
        public synchronized void add(MenuType<?> type,MenuScreens.ScreenConstructor<?,?> factory,String owner) {
            if(complete)throw new IllegalStateException("Source menu screen registered after native phase: "+owner);
            if(pending.putIfAbsent(Objects.requireNonNull(type),new Entry(Objects.requireNonNull(factory),Objects.requireNonNull(owner)))!=null)
                throw new IllegalStateException("Duplicate source menu screen registration: "+owner);
        }
        @SuppressWarnings({"rawtypes","unchecked"})
        public synchronized void flush(RegisterMenuScreensEvent event) {
            if(complete)throw new IllegalStateException("Native menu-screen registration phase repeated");
            for(var iterator=pending.entrySet().iterator();iterator.hasNext();) {
                var entry=iterator.next();event.register((MenuType)entry.getKey(),(MenuScreens.ScreenConstructor)entry.getValue().factory());iterator.remove();
            }
            complete=true;
        }
        public synchronized int pendingCount(){return pending.size();}
    }
}
