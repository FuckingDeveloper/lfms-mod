package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.target.neoforge263.Target263InitialDataTransaction;
import java.util.*;
import java.util.concurrent.*;

final class InitialDataTransactionCheck {
    static void run() {
        // Loading the real target class must apply the exact one-call redirect.
        check(Arrays.stream(net.minecraft.server.WorldLoader.class.getDeclaredMethods())
                .anyMatch(m->m.getName().contains("lms$initialData")),"native WorldLoader redirect missing");
        var main=new ArrayDeque<Runnable>();var background=new ArrayDeque<Runnable>();
        var trace=new ArrayList<String>();var first=new CompletableFuture<String>();
        var last=new CompletableFuture<String>();
        var result=Target263InitialDataTransaction.bootstrapThenFinal(()->first,value->{
            check(Target263InitialDataTransaction.bootstrapActive(),"bootstrap scope absent");
            trace.add("commit:"+value);
        },()->{
            check(!Target263InitialDataTransaction.bootstrapActive(),"scope leaked into final data loading");
            trace.add("final");return last;
        },background::add,main::add);
        check(!result.isDone()&&main.isEmpty()&&background.isEmpty(),"data published before preparation");
        first.complete("bootstrap");check(main.size()==1&&background.isEmpty(),"commit executor/order");
        main.remove().run();check(trace.equals(List.of("commit:bootstrap"))&&background.size()==1,"final started before native commit");
        check(!Target263InitialDataTransaction.bootstrapActive(),"bootstrap scope leaked");
        background.remove().run();check(!result.isDone(),"bootstrap result published as final result");
        last.complete("final-data");check(result.join().equals("final-data")&&trace.equals(List.of("commit:bootstrap","final")),"final identity/order");
        var failure=new IllegalStateException("native-failure");
        for(int stage=0;stage<3;stage++) {
            int failingStage=stage;int[] commits={0}, finals={0};
            var failed=Target263InitialDataTransaction.bootstrapThenFinal(
                    ()->failingStage==0?CompletableFuture.failedFuture(failure):CompletableFuture.completedFuture("first"),
                    value->{commits[0]++;if(failingStage==1)throw failure;},
                    ()->{finals[0]++;return CompletableFuture.failedFuture(failure);},Runnable::run,Runnable::run);
            try{failed.join();throw new AssertionError("phase failure swallowed");}
            catch(CompletionException expected){check(expected.getCause()==failure,"causal failure replaced");}
            check(commits[0]==(stage==0?0:1)&&finals[0]==(stage==2?1:0),"failed phase continued or repeated");
            check(!Target263InitialDataTransaction.bootstrapActive(),"failure leaked bootstrap scope");
        }
        System.out.println("PASS: native WorldLoader redirect, async executor/commit/final ordering, no bootstrap publication and three failure boundaries");
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
