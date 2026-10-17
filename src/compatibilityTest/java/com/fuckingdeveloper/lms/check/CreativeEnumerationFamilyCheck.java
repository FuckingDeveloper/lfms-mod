package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.item.Forge1192CreativeTabBridge;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.Forge1192LifecycleBridge;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforgespi.language.IModInfo;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import static org.objectweb.asm.Opcodes.*;

public final class CreativeEnumerationFamilyCheck {
    private static final String TAB="net/minecraft/world/item/CreativeModeTab", T="L"+TAB+";", ARRAY="["+T;
    private static final String HOST="fixture/CreativeEnumeration", LIST="Lnet/minecraft/core/NonNullList;";
    private static int calls;
    private static boolean invalidCount;
    public static void append(NonNullList<ItemStack> list) {
        check(Forge1192LifecycleBridge.requireActive().modId().equals("creative_enumeration_fixture"),"tab callback ownership");
        calls++;
        var stack = Items.STONE.getDefaultInstance();
        if (invalidCount) stack.setCount(2);
        list.add(stack); list.add(stack.copy());
    }
    public static void run() throws Exception {
        var transformer=new Forge1192LegacyClassTransformer();
        byte[] source=iteration(0);
        byte[] rewritten=transformer.transform(HOST.replace('/','.'),source).bytes();
        var node=new ClassNode();new ClassReader(rewritten).accept(node,0);
        check(node.methods.stream().flatMap(m->Arrays.stream(m.instructions.toArray())).anyMatch(i->i instanceof MethodInsnNode m&&m.name.equals("tabs")),"enhanced-for was not migrated");
        for(int shape=1;shape<=8;shape++) {
            try{transformer.transform(HOST.replace('/','.'),iteration(shape));throw new AssertionError("unsupported array shape accepted "+shape);}
            catch(UnsupportedOperationException expected){check(expected.getMessage().contains("Creative tab enumeration"),"wrong negative boundary");}
        }
        var file=Files.createTempFile("creative-enumeration-", ".jar");
        var bus=BusBuilder.builder().build();
        var info=(IModInfo)java.lang.reflect.Proxy.newProxyInstance(CreativeEnumerationFamilyCheck.class.getClassLoader(),new Class[]{IModInfo.class},(p,m,a)->{if(m.getName().equals("getModId"))return "creative_enumeration_fixture";throw new AssertionError(m);});
        var host=new ModContainer(info){public IEventBus getEventBus(){return bus;}};
        try {
            try(var jar=new JarOutputStream(Files.newOutputStream(file))) {
                jar.putNextEntry(new JarEntry(HOST+".class"));jar.write(source);jar.closeEntry();
                jar.putNextEntry(new JarEntry("fixture/EnumerationTab.class"));jar.write(tab());jar.closeEntry();
            }
            byte[] original=Files.readAllBytes(file);
            try(var loader=new ManagedLegacyClassLoader(file,CreativeEnumerationFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
                var type=loader.linkOwnedClass(HOST.replace('/','.'));
                for(String token:List.of("search","hotbar","inventory")) {
                    Object actual=type.getMethod(token).invoke(null);
                    var expected=net.minecraft.core.registries.BuiltInRegistries.CREATIVE_MODE_TAB.getValueOrThrow(switch(token){case "search"->CreativeModeTabs.SEARCH;case "hotbar"->CreativeModeTabs.HOTBAR;default->CreativeModeTabs.INVENTORY;});
                    check(actual==expected,"special native tab identity "+token);
                }
                for (String name : List.of("tokenHandle","nestedToken")) {
                    var handle=(java.lang.invoke.MethodHandle)type.getMethod(name).invoke(null);
                    try { check(handle.invokeWithArguments()==Forge1192CreativeTabBridge.hotbarTab(),"callable/nested special identity"); }
                    catch (Throwable failure) { throw new AssertionError(failure); }
                }
                var fill=(java.lang.invoke.MethodHandle)type.getMethod("fillHandle").invoke(null);
                check(fill.type().equals(java.lang.invoke.MethodType.methodType(void.class,CreativeModeTab.class,NonNullList.class)),"virtual handle receiver projection");
                try{type.getMethod("iterate").invoke(null);throw new AssertionError("missing native player context accepted");}
                catch(java.lang.reflect.InvocationTargetException failure){check(failure.getCause() instanceof IllegalStateException&&failure.getCause().getMessage().contains("ready local client"),"wrong readiness failure");}
                CreativeModeTab tab;
                try(var scope=Forge1192LifecycleBridge.enter("creative_enumeration_fixture",bus,host,loader)) {
                    tab=(CreativeModeTab)loader.linkOwnedClass("fixture.EnumerationTab").getConstructor(String.class).newInstance("enumeration_fixture");
                }
                // The real native builder retains LMS's population generator. Exercise
                // it without thawing the already-frozen tab registry in native smoke.
                var field=CreativeModeTab.class.getDeclaredField("displayItemsGenerator");field.setAccessible(true);
                var generator=(CreativeModeTab.DisplayItemsGenerator)field.get(tab);
                for (int i=0;i<3;i++) {
                var output=new ArrayList<ItemStack>();
                generator.accept(new CreativeModeTab.ItemDisplayParameters(net.minecraft.world.flag.FeatureFlags.VANILLA_SET,false,net.minecraft.core.RegistryAccess.EMPTY),(stack,visibility)->output.add(stack));
                check(output.size()==1&&output.getFirst().is(Items.STONE)&&calls==i+1,"native custom-tab population lost tab-level override/source super dispatch");
                }
                invalidCount = true;
                try {
                    generator.accept(new CreativeModeTab.ItemDisplayParameters(net.minecraft.world.flag.FeatureFlags.VANILLA_SET,false,net.minecraft.core.RegistryAccess.EMPTY),(stack,visibility)->{});
                    throw new AssertionError("invalid source stack count accepted");
                } catch (IllegalStateException expected) {
                    check(expected.getMessage().contains("count is not representable"), "wrong source invariant failure");
                } finally { invalidCount = false; }
                check(Forge1192LifecycleBridge.currentContext().isEmpty(),"source callback context leaked");
                check(Arrays.equals(original,Files.readAllBytes(file)),"legacy artifact modified");
                Forge1192LifecycleBridge.release("creative_enumeration_fixture",loader);
            }
        }finally{Files.deleteIfExists(file);}
        System.out.println("PASS: creative enhanced-for admission, mutation/escape/index/handle negatives, real special-tab identity, readiness guard, virtual/source-super and installed native generator dispatch, callback ownership and immutable artifact");
    }
    private static byte[] iteration(int shape) {
        var w=new ClassWriter(ClassWriter.COMPUTE_MAXS|ClassWriter.COMPUTE_FRAMES);w.visit(V17,ACC_PUBLIC,HOST,null,"java/lang/Object",null);
        var m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"iterate","()V",null,null);m.visitCode();
        if(shape==5||shape==6) {
            var h=new Handle(H_GETSTATIC,TAB,"f_40748_",ARRAY,false);
            m.visitLdcInsn(shape==5?h:new ConstantDynamic("array","Ljava/lang/invoke/MethodHandle;",new Handle(H_INVOKESTATIC,"fixture/Bootstrap","get","()Ljava/lang/Object;",false),h));m.visitInsn(POP);
        } else if(shape==4) {m.visitInsn(ACONST_NULL);m.visitFieldInsn(PUTSTATIC,TAB,"f_40748_",ARRAY);}
        else {
            m.visitFieldInsn(GETSTATIC,TAB,"f_40748_",ARRAY);
            if(shape==1){m.visitInsn(ICONST_0);m.visitInsn(ACONST_NULL);m.visitInsn(AASTORE);}
            else if(shape==2){m.visitMethodInsn(INVOKESTATIC,"fixture/Sink","escape","(Ljava/lang/Object;)V",false);}
            else if(shape==3){m.visitInsn(ICONST_0);m.visitInsn(AALOAD);m.visitInsn(POP);}
            else {
                m.visitVarInsn(ASTORE,0);m.visitVarInsn(ALOAD,0);m.visitInsn(ARRAYLENGTH);m.visitVarInsn(ISTORE,1);m.visitInsn(ICONST_0);m.visitVarInsn(ISTORE,2);
                var condition=new Label();var done=new Label();m.visitLabel(condition);m.visitVarInsn(ILOAD,2);m.visitVarInsn(ILOAD,1);m.visitJumpInsn(IF_ICMPGE,done);
                m.visitVarInsn(ALOAD,0);m.visitVarInsn(ILOAD,2);m.visitInsn(AALOAD);m.visitVarInsn(ASTORE,3);
                if(shape==8){m.visitVarInsn(ILOAD,2);m.visitMethodInsn(INVOKESTATIC,"fixture/Sink","index","(I)V",false);}
                m.visitIincInsn(2,shape==7?2:1);m.visitJumpInsn(GOTO,condition);m.visitLabel(done);
            }
        }
        m.visitInsn(RETURN);m.visitMaxs(0,0);m.visitEnd();
        var bootstrap = new Handle(H_INVOKESTATIC,"com/fuckingdeveloper/lms/check/CreativeEnumerationFamilyCheck","constant","(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;",false);
        for (boolean nested : List.of(false,true)) {
            m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,nested?"nestedToken":"tokenHandle","()Ljava/lang/invoke/MethodHandle;",null,null);m.visitCode();
            var h=new Handle(H_GETSTATIC,TAB,"f_40760_",T,false);
            m.visitLdcInsn(nested?new ConstantDynamic("token","Ljava/lang/invoke/MethodHandle;",bootstrap,h):h);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
        }
        m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"fillHandle","()Ljava/lang/invoke/MethodHandle;",null,null);m.visitCode();m.visitLdcInsn(new Handle(H_INVOKEVIRTUAL,TAB,"m_6151_","("+LIST+")V",false));m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
        int i=0;for(String token:List.of("search","hotbar","inventory")) {
            m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,token,"()"+T,null,null);m.visitCode();m.visitFieldInsn(GETSTATIC,TAB,new String[]{"f_40754_","f_40760_","f_40761_"}[i++],T);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
        }
        w.visitEnd();return w.toByteArray();
    }
    private static byte[] tab() {
        var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);w.visit(V17,ACC_PUBLIC,"fixture/EnumerationTab",null,TAB,null);
        var m=w.visitMethod(ACC_PUBLIC,"<init>","(Ljava/lang/String;)V",null,null);m.visitCode();m.visitVarInsn(ALOAD,0);m.visitVarInsn(ALOAD,1);m.visitMethodInsn(INVOKESPECIAL,TAB,"<init>","(Ljava/lang/String;)V",false);m.visitInsn(RETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC,"m_6151_","("+LIST+")V",null,null);m.visitCode();m.visitVarInsn(ALOAD,0);m.visitVarInsn(ALOAD,1);m.visitMethodInsn(INVOKESPECIAL,TAB,"m_6151_","("+LIST+")V",false);
        m.visitVarInsn(ALOAD,1);m.visitMethodInsn(INVOKESTATIC,"com/fuckingdeveloper/lms/check/CreativeEnumerationFamilyCheck","append","("+LIST+")V",false);m.visitInsn(RETURN);m.visitMaxs(0,0);m.visitEnd();w.visitEnd();return w.toByteArray();
    }
    public static Object constant(java.lang.invoke.MethodHandles.Lookup lookup, String name, Class<?> type, Object value) { return value; }
    private static void check(boolean value,String reason){if(!value)throw new AssertionError(reason);}
}
