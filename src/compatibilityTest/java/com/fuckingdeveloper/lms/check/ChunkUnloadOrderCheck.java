package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.block.Forge1192ChunkUnloadBridge;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.Forge1192EventBusBridge;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.Forge1192LifecycleBridge;


import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.function.Consumer;
import net.minecraft.server.level.*;
import net.minecraft.world.level.chunk.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;

final class ChunkUnloadOrderCheck {
    static void run(IEventBus ownerBus,ModContainer host) throws Exception {
        var f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);var unsafe=(sun.misc.Unsafe)f.get(null);
        var chunk=(LevelChunk)unsafe.allocateInstance(LevelChunk.class);
        var level=LevelChunk.class.getDeclaredField("level");level.setAccessible(true);level.set(chunk,unsafe.allocateInstance(ServerLevel.class));
        var order=new ArrayList<String>();var fail=new AtomicBoolean();var expected=new IllegalArgumentException("unload-probe");
        Consumer<ChunkEvent.Unload> source=event->{check(event.getChunk()==chunk,"chunk replaced");order.add("source");if(fail.get())throw expected;};
        Consumer<ChunkEvent> base=event->order.add("base");
        Consumer<ChunkEvent.Unload> target=event->order.add("native");
        try(var scope=Forge1192LifecycleBridge.enter("native_smoke",ownerBus,host)) {
            Forge1192EventBusBridge.addListener(NeoForge.EVENT_BUS,source,false,ChunkEvent.Unload.class);
            Forge1192EventBusBridge.addListener(NeoForge.EVENT_BUS,EventPriority.HIGH,base,false,ChunkEvent.class);
        }
        NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL,false,ChunkEvent.Unload.class,target);
        try {
            var type=ChunkMap.class;
            var injected=Arrays.stream(type.getDeclaredMethods()).filter(m->m.getName().contains("lms$beforeFinalSave")).findFirst().orElseThrow();injected.setAccessible(true);
            check(Arrays.stream(type.getDeclaredMethods()).anyMatch(m->m.getName().contains("lms$unloadTransaction")),"native transaction injection missing");
            var receiver=unsafe.allocateInstance(type);
            com.llamalad7.mixinextras.injector.wrapoperation.Operation<Boolean> save=args->{
                check(args[0]==receiver && args[1]==chunk,"native save arguments changed");
                check(order.equals(List.of("base","source")),"source unload not before final save or native notified early");
                order.add("save");return false;
            };
            Forge1192ChunkUnloadBridge.transaction(()->{
                try{check(!(boolean)injected.invoke(receiver,receiver,chunk,save),"native save result changed");}
                catch(ReflectiveOperationException failure){throw new AssertionError(failure);}
                Forge1192ChunkUnloadBridge.transaction(()->check(!Forge1192ChunkUnloadBridge.alreadyDelivered(new ChunkEvent.Unload(chunk)),"nested transaction inherited delivery"));
                check(Forge1192ChunkUnloadBridge.alreadyDelivered(new ChunkEvent.Unload(chunk)),"nested transaction lost outer delivery");
                NeoForge.EVENT_BUS.post(new ChunkEvent.Unload(chunk));
            });
            check(order.equals(List.of("base","source","save","native")),"duplicate source or displaced native unload: "+order);
            check(!Forge1192ChunkUnloadBridge.alreadyDelivered(new ChunkEvent.Unload(chunk)),"successful transaction leaked");
            order.clear();NeoForge.EVENT_BUS.post(new ChunkEvent.Unload(chunk));
            check(order.equals(List.of("base","source","native")),"unrelated native unload suppressed");
            fail.set(true);
            try{Forge1192ChunkUnloadBridge.transaction(()->Forge1192ChunkUnloadBridge.beforeSave(chunk));throw new AssertionError("source failure swallowed");}
            catch(IllegalArgumentException actual){check(actual==expected,"source failure replaced");}
            check(!Forge1192ChunkUnloadBridge.alreadyDelivered(new ChunkEvent.Unload(chunk)),"failed transaction leaked");fail.set(false);
            Forge1192EventBusBridge.unregister(NeoForge.EVENT_BUS,source);Forge1192EventBusBridge.unregister(NeoForge.EVENT_BUS,base);
            order.clear();Forge1192ChunkUnloadBridge.transaction(()->Forge1192ChunkUnloadBridge.beforeSave(chunk));check(order.isEmpty(),"source phase subscription survived unregister");
        } finally {
            Forge1192EventBusBridge.unregister(NeoForge.EVENT_BUS,source);Forge1192EventBusBridge.unregister(NeoForge.EVENT_BUS,base);NeoForge.EVENT_BUS.unregister(target);
        }
        System.out.println("PASS: actual target unload save hook, source before native save, native notification isolation, exact/base priority, unrelated native events, nested/failure restoration and unregister");
    }
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}
