package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.render.*;
import java.nio.file.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.jar.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public final class GuiStaticAccessorFamilyCheck {
    private static int initializations;
    public static void recordInitializer(){initializations++;}
    static void run() throws Exception {
        String renderer="net/minecraft/client/renderer/entity/ItemRenderer";
        var root=node("fixture/AccessorScreen","net/minecraft/client/gui/screens/Screen");
        var helper=root;
        var getter=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC|Opcodes.ACC_SYNTHETIC,"read","(L"+root.name+";)L"+renderer+";",null,null);
        getter.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));getter.instructions.add(new FieldInsnNode(Opcodes.GETFIELD,root.name,"f_96542_","L"+renderer+";"));getter.instructions.add(new InsnNode(Opcodes.ARETURN));helper.methods.add(getter);
        var clinit=new MethodNode(Opcodes.ACC_STATIC,"<clinit>","()V",null,null);clinit.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"com/fuckingdeveloper/lms/check/GuiStaticAccessorFamilyCheck","recordInitializer","()V",false));clinit.instructions.add(new InsnNode(Opcodes.RETURN));helper.methods.add(clinit);
        var caller=node("fixture/StaticAccessorConsumer","java/lang/Object");
        var draw=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"draw","(L"+root.name+";Lnet/minecraft/world/item/ItemStack;)V",null,null);
        draw.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));draw.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,helper.name,"read",getter.desc,false));draw.instructions.add(new VarInsnNode(Opcodes.ALOAD,1));draw.instructions.add(new InsnNode(Opcodes.ICONST_0));draw.instructions.add(new InsnNode(Opcodes.ICONST_0));draw.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,renderer,"renderAndDecorateFakeItem","(Lnet/minecraft/world/item/ItemStack;II)V",false));draw.instructions.add(new InsnNode(Opcodes.RETURN));caller.methods.add(draw);
        var transformer=new Forge1192LegacyClassTransformer();
        java.util.function.BooleanSupplier projected=()->{
            transformer.bindManagedClassBytes(name->name.equals(root.name)?bytes(root):name.equals(helper.name)?bytes(helper):null);
            var result=new ClassNode();new ClassReader(transformer.transform(caller.name.replace('/','.'),bytes(caller)).bytes()).accept(result,0);
            return result.methods.stream().flatMap(m->Arrays.stream(m.instructions.toArray())).anyMatch(i->i instanceof MethodInsnNode c&&c.owner.endsWith("Forge1192GuiItemExtractionBridge")&&c.name.equals("screenStaticRenderer"));
        };
        check(projected.getAsBoolean(),"pure static accessor not adapted");
        getter.access|=Opcodes.ACC_SYNCHRONIZED;check(!projected.getAsBoolean(),"accessor monitor semantics bypassed/cache stale");getter.access&=~Opcodes.ACC_SYNCHRONIZED;
        var sideEffect=new InsnNode(Opcodes.NOP);getter.instructions.insert(sideEffect);check(!projected.getAsBoolean(),"unproven accessor body accepted");getter.instructions.remove(sideEffect);
        root.fields.add(new FieldNode(Opcodes.ACC_PUBLIC,"f_96542_","L"+renderer+";",null,null));check(!projected.getAsBoolean(),"source-hidden field treated as native service");root.fields.clear();
        check(projected.getAsBoolean(),"evidence graph rebind failed to restore valid proof");
        var path=Files.createTempFile("lms-screen-accessor", ".jar");
        try(var jar=new JarOutputStream(Files.newOutputStream(path))){for(var source:List.of(root,caller)){jar.putNextEntry(new JarEntry(source.name+".class"));jar.write(bytes(source));jar.closeEntry();}}
        byte[] immutable=Files.readAllBytes(path);initializations=0;
        var field=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");field.setAccessible(true);var unsafe=(sun.misc.Unsafe)field.get(null);
        var graphics=(GuiGraphicsExtractor)unsafe.allocateInstance(GuiGraphicsExtractor.class);
        try(var loader=new ManagedLegacyClassLoader(path,GuiStaticAccessorFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
            var screen=loader.linkOwnedClass(root.name.replace('/','.'));loader.linkOwnedClass(helper.name.replace('/','.'));
            var consumer=loader.linkOwnedClass(caller.name.replace('/','.')).getMethod("draw",screen,ItemStack.class);
            check(initializations==0,"static getter owner initialized during transformation/linking");
            Forge1192WidgetGraphicsBridge.extract((pose,x,y,partial)->{
                try{invoke(consumer,null);throw new AssertionError("null source field receiver accepted");}catch(NullPointerException expected){}
                check(initializations==1,"INVOKESTATIC initialization did not precede source null failure");
            },graphics,0,0,0);
            Object instance=unsafe.allocateInstance(screen);
            Forge1192WidgetGraphicsBridge.extract((pose,x,y,partial)->invoke(consumer,instance),graphics,0,0,0);
            check(initializations==1,"source accessor owner initialized repeatedly");
            check(Arrays.equals(immutable,Files.readAllBytes(path)),"source accessor JAR modified");
        }finally{Files.deleteIfExists(path);}
        try{Forge1192GuiItemExtractionBridge.screenContext(null);throw new AssertionError("null source field became a scope/type error");}catch(NullPointerException expected){}
        System.out.println("PASS: source static/synthetic GUI accessor, exact inherited field/no hidden-field substitution, monitor/body/rebinding negatives, native GUI view, source class initialization before null failure and immutable artifact");
    }
    private static void invoke(java.lang.reflect.Method method,Object receiver){try{method.invoke(null,receiver,ItemStack.EMPTY);}catch(InvocationTargetException failure){if(failure.getCause() instanceof RuntimeException runtime)throw runtime;throw new AssertionError(failure.getCause());}catch(ReflectiveOperationException failure){throw new AssertionError(failure);}}
    private static ClassNode node(String name,String parent){var node=new ClassNode();node.version=Opcodes.V17;node.access=Opcodes.ACC_PUBLIC;node.name=name;node.superName=parent;return node;}
    private static byte[] bytes(ClassNode node){var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);return writer.toByteArray();}
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}
