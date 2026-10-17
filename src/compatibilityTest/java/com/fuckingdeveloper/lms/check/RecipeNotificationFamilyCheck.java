package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.Forge1192SourceScan;
import java.lang.invoke.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.jar.*;
import net.neoforged.bus.api.BusBuilder;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.minecraft.world.item.crafting.RecipeMap;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import static org.objectweb.asm.Opcodes.*;

public final class RecipeNotificationFamilyCheck {
    private static final String OLD="net/minecraftforge/client/event/RecipesUpdatedEvent",HOST="fixture/RecipeObserver",EVENT="L"+OLD+";";
    public static Object constant(MethodHandles.Lookup lookup,String name,Class<?> type,Object value){return value;}
    public static void run() throws Exception {
        var file=Files.createTempFile("source-recipe-notification-",".jar");
        try {
            try(var jar=new JarOutputStream(Files.newOutputStream(file))){jar.putNextEntry(new JarEntry(HOST+".class"));jar.write(fixture());jar.closeEntry();}
            try(var loader=new ManagedLegacyClassLoader(file,RecipeNotificationFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
                var type=loader.linkOwnedClass(HOST.replace('/','.'));
                check(type.getMethod("eventType").invoke(null)==RecipesReceivedEvent.class,"registration class token differs");
                check(type.getField("required").get(null)==RecipesReceivedEvent.class,"stored class token differs");
                var listener=(Consumer<RecipesReceivedEvent>)type.getMethod("listener").invoke(null);
                var bus=BusBuilder.builder().build();
                bus.addListener(RecipesReceivedEvent.class,listener);
                var event=new RecipesReceivedEvent(Set.of(),RecipeMap.EMPTY);bus.post(event);
                check(type.getField("last").get(null)==event,"real bus callback/event identity lost");
                check(type.getMethod("matches",Object.class).invoke(null,event).equals(true),"instanceof projection");
                for(var name:List.of("callbackHandle","nestedHandle")) {
                    var handle=(MethodHandle)type.getMethod(name).invoke(null);
                    check(handle.type().equals(MethodType.methodType(void.class,RecipesReceivedEvent.class)),"callback handle type projection");
                    try{handle.invokeWithArguments(event);}catch(Throwable failure){throw new AssertionError(failure);}
                }
                var data=Forge1192SourceScan.read(file,List.of());
                check(data.getAnnotations().stream().anyMatch(a->Type.getType(RecipesReceivedEvent.class).equals(a.annotationData().get("value"))),"source scan/type annotation disagrees");
                check(type.getField("typed").getGenericType().getTypeName().contains(RecipesReceivedEvent.class.getName()),"generic type projection");
                bus.unregister(listener);
            }
            for(int shape=0;shape<5;shape++)reject(shape);
        } finally {Files.deleteIfExists(file);}
        System.out.println("PASS: source recipe notification class/stored tokens, callback/lambda/handle/nested constants, generic/scan identity and real native bus delivery; source payload/construction/subclass negatives");
    }
    private static byte[] fixture() {
        var w=new ClassWriter(ClassWriter.COMPUTE_MAXS|ClassWriter.COMPUTE_FRAMES);w.visit(V17,ACC_PUBLIC,HOST,null,"java/lang/Object",null);
        var a=w.visitAnnotation("Lfixture/Metadata;",false);a.visit("value",Type.getType(EVENT));a.visitEnd();
        w.visitField(ACC_PUBLIC|ACC_STATIC,"required","Ljava/lang/Class;","Ljava/lang/Class<"+EVENT+">;",null).visitEnd();
        w.visitField(ACC_PUBLIC|ACC_STATIC,"typed","Ljava/util/List;","Ljava/util/List<"+EVENT+">;",null).visitEnd();
        w.visitField(ACC_PUBLIC|ACC_STATIC,"last","Ljava/lang/Object;",null,null).visitEnd();
        var m=w.visitMethod(ACC_STATIC,"<clinit>","()V",null,null);m.visitCode();m.visitLdcInsn(Type.getType(EVENT));m.visitFieldInsn(PUTSTATIC,HOST,"required","Ljava/lang/Class;");m.visitInsn(RETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"eventType","()Ljava/lang/Class;",null,null);m.visitCode();m.visitLdcInsn(Type.getType(EVENT));m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"callback","("+EVENT+")V",null,null);m.visitCode();m.visitVarInsn(ALOAD,0);m.visitMethodInsn(INVOKEVIRTUAL,OLD,"getClass","()Ljava/lang/Class;",false);m.visitFieldInsn(GETSTATIC,HOST,"required","Ljava/lang/Class;");var done=new Label();m.visitJumpInsn(IF_ACMPNE,done);m.visitVarInsn(ALOAD,0);m.visitFieldInsn(PUTSTATIC,HOST,"last","Ljava/lang/Object;");m.visitLabel(done);m.visitInsn(RETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"matches","(Ljava/lang/Object;)Z",null,null);m.visitCode();m.visitVarInsn(ALOAD,0);m.visitTypeInsn(INSTANCEOF,OLD);m.visitInsn(IRETURN);m.visitMaxs(0,0);m.visitEnd();
        var callback=new Handle(H_INVOKESTATIC,HOST,"callback","("+EVENT+")V",false);
        m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"listener","()Ljava/util/function/Consumer;",null,null);m.visitCode();
        m.visitInvokeDynamicInsn("accept","()Ljava/util/function/Consumer;",new Handle(H_INVOKESTATIC,"java/lang/invoke/LambdaMetafactory","metafactory","(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",false),Type.getMethodType("(Ljava/lang/Object;)V"),callback,Type.getMethodType("("+EVENT+")V"));m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
        var bootstrap=new Handle(H_INVOKESTATIC,"com/fuckingdeveloper/lms/check/RecipeNotificationFamilyCheck","constant","(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;",false);
        for(boolean nested:List.of(false,true)){m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,nested?"nestedHandle":"callbackHandle","()Ljava/lang/invoke/MethodHandle;",null,null);m.visitCode();m.visitLdcInsn(nested?new ConstantDynamic("outer","Ljava/lang/invoke/MethodHandle;",bootstrap,new ConstantDynamic("inner","Ljava/lang/invoke/MethodHandle;",bootstrap,callback)):callback);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();}
        w.visitEnd();return w.toByteArray();
    }
    private static void reject(int shape) throws Exception {
        var node=new ClassNode();node.version=V17;node.access=ACC_PUBLIC;node.name="fixture/UnsupportedEvent";node.superName=shape==4?OLD:"java/lang/Object";
        var method=new MethodNode(ACC_PUBLIC|ACC_STATIC,"bad","()V",null,null);node.methods.add(method);
        var getter=new Handle(H_INVOKEVIRTUAL,OLD,"getRecipeManager","()Lnet/minecraft/world/item/crafting/RecipeManager;",false);
        if(shape==0)method.instructions.add(new MethodInsnNode(INVOKEVIRTUAL,getter.getOwner(),getter.getName(),getter.getDesc(),false));
        if(shape==1)method.instructions.add(new TypeInsnNode(NEW,OLD));
        if(shape==2)method.instructions.add(new LdcInsnNode(getter));
        if(shape==3)method.instructions.add(new LdcInsnNode(new ConstantDynamic("payload","Ljava/lang/invoke/MethodHandle;",new Handle(H_INVOKESTATIC,"fixture/Bootstrap","get","()V",false),getter)));
        method.instructions.add(new InsnNode(RETURN));var w=new ClassWriter(0);node.accept(w);
        try{new Forge1192LegacyClassTransformer().transform(node.name.replace('/','.'),w.toByteArray());throw new AssertionError("unsupported source event shape accepted: "+shape);}catch(UnsupportedOperationException expected){check(expected.getMessage().contains("Recipe notification"),"wrong source failure");}
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
