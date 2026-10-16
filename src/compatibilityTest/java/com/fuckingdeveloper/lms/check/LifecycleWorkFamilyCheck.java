package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.Forge1192LifecycleBridge;
import com.fuckingdeveloper.lms.target.neoforge263.Target263Forge1192LifecycleWorkBridge;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.*;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforgespi.language.IModInfo;

public final class LifecycleWorkFamilyCheck {
    private static ModContainer host(String id, IEventBus bus) {
        var info=(IModInfo)Proxy.newProxyInstance(LifecycleWorkFamilyCheck.class.getClassLoader(),new Class[]{IModInfo.class},(p,m,a)->{
            if(m.getName().equals("getModId"))return id;throw new AssertionError(m);
        });
        return new ModContainer(info){public IEventBus getEventBus(){return bus;}};
    }
    public static void run() throws Exception {
        var bus=BusBuilder.builder().build();var host=host("work_owner",bus);
        var queue=new DeferredWorkQueue("lms-catalog-check");var event=new FMLCommonSetupEvent(host,queue);
        var calls=new ArrayList<String>();var thread=Thread.currentThread();
        var bytes=new Forge1192LegacyClassTransformer().transform("fixture.WorkCalls",LifecycleWorkFixture.bytes()).bytes();
        var type=new ClassLoader(LifecycleWorkFamilyCheck.class.getClassLoader()) { Class<?> define(){return defineClass("fixture.WorkCalls",bytes,0,bytes.length);} }.define();
        CompletableFuture<?> first; CompletableFuture<Integer> value; CompletableFuture<?> bound;
        try(var scope=Forge1192LifecycleBridge.enter("work_owner",bus,host)) {
            Runnable work=()->{check(Thread.currentThread()==thread,"native queue thread changed");check(Forge1192LifecycleBridge.requireActive().modId().equals("work_owner"),"source owner lost");calls.add("direct");};
            first=(CompletableFuture<?>)type.getMethod("direct",FMLCommonSetupEvent.class,Runnable.class).invoke(null,event,work);
            @SuppressWarnings("unchecked") var function=(Function<Runnable,CompletableFuture<?>>)type.getMethod("bound",FMLCommonSetupEvent.class).invoke(null,event);
            bound=function.apply(()->calls.add("bound"));
            value=Target263Forge1192LifecycleWorkBridge.enqueueWork(event,()->{calls.add("value");return 37;});
            check(calls.isEmpty()&&!first.isDone()&&!value.isDone(),"work executed before native queue dispatch");
        }
        for(String method : List.of("handle","nested")) {
            try(var scope=Forge1192LifecycleBridge.enter("work_owner",bus,host)) {
                var handle=(java.lang.invoke.MethodHandle)type.getMethod(method).invoke(null);
                try { handle.invoke(event,(Runnable)()->calls.add(method)); }
                catch(Throwable failure){throw new AssertionError("catalogued handle linkage failed",failure);}
            }
        }
        var other=host("work_other",bus);
        try(var scope=Forge1192LifecycleBridge.enter("work_other",bus,other)) {
            var previous=Forge1192LifecycleBridge.requireActive();queue.runTasks();
            check(Forge1192LifecycleBridge.requireActive()==previous,"native deferred callback leaked source ownership");
        }
        check(first.isDone()&&bound.isDone()&&value.join()==37&&calls.equals(List.of("direct","bound","value","handle","nested")),"future/order/value changed");
        var failingQueue=new DeferredWorkQueue("lms-failure-check");var failingEvent=new FMLCommonSetupEvent(host,failingQueue);
        var failure=new IllegalArgumentException("expected-work-failure");CompletableFuture<?> failed;
        try(var scope=Forge1192LifecycleBridge.enter("work_owner",bus,host)) { failed=Target263Forge1192LifecycleWorkBridge.enqueueWork(failingEvent,(Runnable)()->{throw failure;}); }
        try { failingQueue.runTasks();throw new AssertionError("native aggregate failure swallowed"); }
        catch(RuntimeException expected) { check(expected.getSuppressed().length==1&&expected.getSuppressed()[0]==failure,"native error aggregation changed"); }
        try { failed.join();throw new AssertionError("future failure swallowed"); }
        catch(CompletionException expected) { check(expected.getCause()==failure,"source failure replaced"); }
        check(Forge1192LifecycleBridge.currentContext().isEmpty(),"failed callback scope leaked");
        System.out.println("PASS: catalogued native work direct/inherited bound lambda linkage, deferred queue timing/order/thread/future/result/failure and source owner restoration");
    }
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}
