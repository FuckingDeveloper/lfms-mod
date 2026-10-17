package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.mapping.Forge1192MappingLayer;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.objectweb.asm.*;

/** Deterministic parallel and reentrant resource inspections against one actual mapping cache. */
public final class MappingCacheConcurrencyCheck {
    public static void run() throws Exception {
        var previous=Thread.currentThread().getContextClassLoader();
        var layer=new AtomicReference<Forge1192MappingLayer>();
        var barrier=new CyclicBarrier(8);var opens=new AtomicInteger();
        var loader=new ClassLoader(previous){
            @Override public InputStream getResourceAsStream(String resource) {
                if(resource.startsWith("fixture/ParallelMapping")) {
                    opens.incrementAndGet();
                    try{barrier.await(10,TimeUnit.SECONDS);}catch(Exception failure){throw new IllegalStateException(failure);}
                    return new ByteArrayInputStream(metadata(resource));
                }
                if(resource.equals("fixture/ReentrantMapping.class")) {
                    layer.get().classifyMethod("fixture.MissingNestedMapping","read","()I");
                    return new ByteArrayInputStream(metadata(resource));
                }
                if(resource.startsWith("fixture/"))return null;
                return super.getResourceAsStream(resource);
            }
        };
        Thread.currentThread().setContextClassLoader(loader);
        try {layer.set(new Forge1192MappingLayer());}finally{Thread.currentThread().setContextClassLoader(previous);}
        try(var workers=Executors.newFixedThreadPool(8)) {
            var results=new ArrayList<Future<Forge1192MappingLayer.Mapping>>();
            for(int i=0;i<8;i++){int index=i;results.add(workers.submit(()->layer.get().classifyMethod("fixture.ParallelMapping"+index,"read","()I")));}
            for(int i=0;i<8;i++) {
                var result=results.get(i).get(15,TimeUnit.SECONDS);
                check(result.status()==Forge1192MappingLayer.Status.VERIFIED_IDENTITY,"parallel classification lost evidence");
                check(layer.get().classifyMethod("fixture.ParallelMapping"+i,"read","()I")==result,"published cache identity changed");
            }
        }
        check(opens.get()==8,"cached queries repeated resource inspection");
        var reentrant=layer.get().classifyMethod("fixture.ReentrantMapping","read","()I");
        check(reentrant.status()==Forge1192MappingLayer.Status.VERIFIED_IDENTITY,"reentrant classification failed");
        check(layer.get().find("method:fixture.MissingNestedMapping#read()I").orElseThrow().status()==Forge1192MappingLayer.Status.OWNER_MISSING,"nested negative evidence lost");
        check(layer.get().classifyMethod("fixture.ReentrantMapping","read","()I")==reentrant,"reentrant cache identity changed");
        System.out.println("PASS: parallel and reentrant mapping inspection, atomic cache publication, canonical identity, negative evidence and no repeat resource reads");
    }
    private static byte[] metadata(String resource){var writer=new ClassWriter(0);writer.visit(Opcodes.V17,Opcodes.ACC_PUBLIC|Opcodes.ACC_ABSTRACT,resource.substring(0,resource.length()-6),null,"java/lang/Object",null);writer.visitMethod(Opcodes.ACC_PUBLIC|Opcodes.ACC_ABSTRACT,"read","()I",null,null).visitEnd();writer.visitEnd();return writer.toByteArray();}
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
