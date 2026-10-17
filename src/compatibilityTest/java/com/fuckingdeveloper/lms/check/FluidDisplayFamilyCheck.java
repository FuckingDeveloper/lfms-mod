package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.fluid.Forge1192EarlyFluidStackBridge;
import com.fuckingdeveloper.lms.target.neoforge263.Target263FluidDisplayQueries;
import java.lang.invoke.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import net.minecraft.core.*;
import net.minecraft.core.component.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.*;
import net.neoforged.neoforge.fluids.*;
import org.objectweb.asm.*;
import static org.objectweb.asm.Opcodes.*;

public final class FluidDisplayFamilyCheck {
    private static final String SOURCE="net/minecraftforge/fluids/FluidStack", STACK="L"+SOURCE+";", HOST="fixture/FluidDisplay";
    private static FluidStack last;private static int callbacks;
    private static final FluidType TYPE=new FluidType(FluidType.Properties.create()) {
        @Override public Component getDescription(FluidStack stack){last=stack;callbacks++;return stack.get(DataComponents.CUSTOM_NAME);}
        @Override public String getDescriptionId(FluidStack stack){last=stack;callbacks++;return "fixture.amount."+stack.getAmount();}
    };
    private static class ProbeFluid extends EmptyFluid { @Override public FluidType getFluidType(){return TYPE;} }
    public static Object constant(MethodHandles.Lookup lookup,String name,Class<?> type,Object value){return value;}
    public static void run() throws Exception {
        NativeDefaultComponentFixture.run(()->{try{checks();}catch(Exception failure){throw new AssertionError(failure);}});
    }
    private static void checks() throws Exception {
        // Isolated probe value: do not construct an intrusive fluid in a frozen registry.
        var unsafeField=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");unsafeField.setAccessible(true);
        var unsafe=(sun.misc.Unsafe)unsafeField.get(null);var fluid=(Fluid)unsafe.allocateInstance(ProbeFluid.class);
        var constructor=FluidStack.class.getDeclaredConstructor(Holder.class,int.class,PatchedDataComponentMap.class);constructor.setAccessible(true);
        var text=Component.literal("Stack-sensitive fluid");
        var stack=constructor.newInstance(Holder.direct(fluid),321,new PatchedDataComponentMap(DataComponentMap.builder().set(DataComponents.CUSTOM_NAME,text).build()));
        callbacks=0;
        check(Target263FluidDisplayQueries.getDisplayName(stack)==text && last==stack && callbacks==1,"native callback component/receiver identity");
        check(Target263FluidDisplayQueries.getTranslationKey(stack).equals("fixture.amount.321") && last==stack && callbacks==2,"stack-sensitive description-ID callback");
        var path=Files.createTempFile("source-fluid-display-",".jar");
        try {
            try(var jar=new JarOutputStream(Files.newOutputStream(path))){jar.putNextEntry(new JarEntry(HOST+".class"));jar.write(fixture());jar.closeEntry();}
            var original=Files.readAllBytes(path);
            try(var loader=new ManagedLegacyClassLoader(path,FluidDisplayFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
                var type=loader.linkOwnedClass(HOST.replace('/','.'));
                for(String name:List.of("getDisplayName","getTranslationKey")) {
                    for(String shape:List.of("direct","handle","nested")) {
                        int before=callbacks;Object result;
                        if(shape.equals("direct"))result=type.getMethod(name,FluidStack.class).invoke(null,stack);
                        else try{result=((MethodHandle)type.getMethod(shape+name).invoke(null)).invokeWithArguments(stack);}catch(Throwable failure){throw new AssertionError(failure);}
                        check(name.equals("getDisplayName")?result==text:result.equals("fixture.amount.321"),"source query result: "+name+shape);
                        check(callbacks==before+1 && last==stack,"source callback dispatch count/receiver: "+shape);
                    }
                }
                for(var empty:List.of(FluidStack.EMPTY,Forge1192EarlyFluidStackBridge.create(Fluids.WATER,0))) {
                    check(empty.getFluid()==Fluids.EMPTY,"empty source/native fluid identity");
                    String expected=null;IllegalStateException unavailable=null;
                    try{expected=empty.getDescriptionId();}catch(IllegalStateException failure) {
                        if(!failure.getMessage().startsWith("Registry not present for DeferredHolder{ResourceKey[neoforge:fluid_type / minecraft:empty]}"))throw failure;
                        unavailable=failure;
                    }
                    if(unavailable==null)check(type.getMethod("getTranslationKey",FluidStack.class).invoke(null,empty).equals(expected),"empty fluid display callback");
                    else {
                        try{type.getMethod("getTranslationKey",FluidStack.class).invoke(null,empty);throw new AssertionError("missing native fluid-type registry was hidden");}
                        catch(java.lang.reflect.InvocationTargetException failure){check(failure.getCause().getClass()==unavailable.getClass() && failure.getCause().getMessage().equals(unavailable.getMessage()),"native registry failure changed");}
                        System.out.println("UNVERIFIED: empty-fluid description text needs full NeoForge fluid-type registration; selection and exact native failure propagation verified");
                    }
                }
            }
            check(Arrays.equals(original,Files.readAllBytes(path)),"source artifact changed");
        } finally {Files.deleteIfExists(path);}
        var jei=Path.of("legacy-mods/jei-1.19.2-forge-11.8.1.1032.jar");
        if(Files.isRegularFile(jei))try(var loader=new ManagedLegacyClassLoader(jei,FluidDisplayFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
            var type=loader.linkOwnedClass("mezz.jei.forge.platform.FluidHelper");
            var receiver=type.getConstructor().newInstance();
            check(type.getMethod("getDisplayName",FluidStack.class).invoke(receiver,stack)==text,"original JEI fluid display consumer");
        }
        System.out.println("PASS: both source fluid display queries/direct/callable/nested forms, exact receiver/component identity and once-only stack-sensitive FluidType callbacks, empty/zero fluid selection/failure boundary and original JEI display consumer");
    }
    private static byte[] fixture() {
        var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);w.visit(V17,ACC_PUBLIC,HOST,null,"java/lang/Object",null);
        var bootstrap=new Handle(H_INVOKESTATIC,Type.getInternalName(FluidDisplayFamilyCheck.class),"constant","(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;",false);
        for(String name:List.of("getDisplayName","getTranslationKey")) {
            String result=name.equals("getDisplayName")?"Lnet/minecraft/network/chat/Component;":"Ljava/lang/String;";
            var m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,name,"("+STACK+")"+result,null,null);m.visitCode();m.visitVarInsn(ALOAD,0);m.visitMethodInsn(INVOKEVIRTUAL,SOURCE,name,"()"+result,false);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
            var handle=new Handle(H_INVOKEVIRTUAL,SOURCE,name,"()"+result,false);
            for(String shape:List.of("handle","nested")){m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,shape+name,"()Ljava/lang/invoke/MethodHandle;",null,null);m.visitCode();m.visitLdcInsn(shape.equals("nested")?new ConstantDynamic("outer","Ljava/lang/invoke/MethodHandle;",bootstrap,new ConstantDynamic("inner","Ljava/lang/invoke/MethodHandle;",bootstrap,handle)):handle);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();}
        }
        w.visitEnd();return w.toByteArray();
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
