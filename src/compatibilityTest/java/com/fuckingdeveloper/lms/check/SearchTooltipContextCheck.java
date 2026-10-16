package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.Forge1192SideBridge;
import java.util.concurrent.atomic.*;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.util.thread.*;

/** Exercise the actual server-group provenance which a client search worker can inherit. */
final class SearchTooltipContextCheck {
    static void run() throws Exception {
        var constructor=SidedThreadGroup.class.getDeclaredConstructor(LogicalSide.class);
        constructor.setAccessible(true);
        var group=constructor.newInstance(LogicalSide.SERVER);
        var failure=new AtomicReference<Throwable>();var calls=new AtomicInteger();
        var thread=new Thread(group,()->{
            try {
                check(EffectiveSide.get()==LogicalSide.SERVER && Forge1192SideBridge.getEffectiveSide()==LogicalSide.SERVER,"server baseline changed");
                Object result=new Object();
                check(Forge1192SideBridge.clientTooltip(()->{
                    check(EffectiveSide.get()==LogicalSide.SERVER,"native logical side changed");
                    check(Forge1192SideBridge.getEffectiveSide()==LogicalSide.CLIENT,"source client operation lost");
                    check(Forge1192SideBridge.clientTooltip(()->{calls.incrementAndGet();return result;})==result,"nested result changed");
                    check(Forge1192SideBridge.getEffectiveSide()==LogicalSide.CLIENT,"nested scope not restored");
                    return result;
                })==result,"callback result replaced");
                check(Forge1192SideBridge.getEffectiveSide()==LogicalSide.SERVER,"successful scope leaked");
                var expected=new IllegalArgumentException("probe");
                try {Forge1192SideBridge.clientTooltip(()->{throw expected;});throw new AssertionError("failure swallowed");}
                catch(IllegalArgumentException actual){check(actual==expected,"exception identity changed");}
                check(Forge1192SideBridge.getEffectiveSide()==LogicalSide.SERVER,"failed scope leaked");
            } catch(Throwable error){failure.set(error);}
        },"source-search-context-check");
        thread.start();thread.join();if(failure.get()!=null)throw new AssertionError("worker context",failure.get());
        check(calls.get()==1,"nested callback duplicated");
        var target=net.minecraft.client.multiplayer.SessionSearchTrees.class;
        var injected=java.util.Arrays.stream(target.getDeclaredMethods()).filter(m->m.getName().contains("lms$searchTooltip") && m.getParameterCount()==5).findFirst().orElseThrow();
        injected.setAccessible(true);
        var lines=java.util.List.<net.minecraft.network.chat.Component>of(net.minecraft.network.chat.Component.literal("probe"));
        com.llamalad7.mixinextras.injector.wrapoperation.Operation<java.util.List<net.minecraft.network.chat.Component>> original=args->{
            check(args.length==4 && java.util.Arrays.stream(args).allMatch(java.util.Objects::isNull),"nullable source arguments replaced");
            check(Forge1192SideBridge.getEffectiveSide()==LogicalSide.CLIENT,"actual injection lost client scope");
            calls.incrementAndGet();return lines;
        };
        check(injected.invoke(null,null,null,null,null,original)==lines && calls.get()==2,"native search invocation/result changed");
        System.out.println("PASS: native search injection, inherited server worker, scoped source client side, native side isolation, nested result/exception identity and restoration");
    }
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}
