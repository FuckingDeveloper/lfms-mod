package com.fuckingdeveloper.lms.check;

import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.*;
import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;

final class WidgetPointerFamilyCheck implements Opcodes {
    static void run() throws Exception {
        String root="net/minecraft/client/gui/components/AbstractWidget",base="fixture/PointerBase",child="fixture/PointerChild";
        var nodes=new ArrayList<ClassNode>();
        for(String name:List.of(base,child)){
            var n=new ClassNode();n.version=V17;n.access=ACC_PUBLIC;n.name=name;n.superName=name.equals(base)?root:base;nodes.add(n);
            if(name.equals(base)){n.fields.add(new FieldNode(ACC_PUBLIC,"calls","I",null,null));n.fields.add(new FieldNode(ACC_PUBLIC,"x","D",null,null));n.fields.add(new FieldNode(ACC_PUBLIC,"dx","D",null,null));}
            for(String[] op:List.of(new String[]{"m_5716_","onClick"},new String[]{"m_7691_","onRelease"},new String[]{"m_7212_","onDrag"})){
                boolean drag=op[1].equals("onDrag");String desc=drag?"(DDDD)V":"(DD)V";
                var m=new MethodNode(ACC_PUBLIC,op[0],desc,null,null);n.methods.add(m);
                m.instructions.add(new VarInsnNode(ALOAD,0));m.instructions.add(new InsnNode(DUP));m.instructions.add(new FieldInsnNode(GETFIELD,base,"calls","I"));m.instructions.add(new IntInsnNode(BIPUSH,name.equals(base)?1:10));m.instructions.add(new InsnNode(IADD));m.instructions.add(new FieldInsnNode(PUTFIELD,base,"calls","I"));
                m.instructions.add(new VarInsnNode(ALOAD,0));m.instructions.add(new VarInsnNode(DLOAD,1));m.instructions.add(new FieldInsnNode(PUTFIELD,base,"x","D"));
                if(drag){m.instructions.add(new VarInsnNode(ALOAD,0));m.instructions.add(new VarInsnNode(DLOAD,5));m.instructions.add(new FieldInsnNode(PUTFIELD,base,"dx","D"));}
                m.instructions.add(new VarInsnNode(ALOAD,0));for(int i=1;i<(drag?9:5);i+=2)m.instructions.add(new VarInsnNode(DLOAD,i));m.instructions.add(new MethodInsnNode(INVOKESPECIAL,n.superName,op[0],desc,false));m.instructions.add(new InsnNode(RETURN));
                var h=new MethodNode(ACC_PUBLIC|ACC_STATIC,op[1]+"Handle","()Ljava/lang/invoke/MethodHandle;",null,null);n.methods.add(h);h.instructions.add(new LdcInsnNode(new Handle(H_INVOKEVIRTUAL,name,op[0],desc,false)));h.instructions.add(new InsnNode(ARETURN));
            }
        }
        var path=Files.createTempFile("lms-pointer-fixture", ".jar");
        try(var jar=new JarOutputStream(Files.newOutputStream(path))){for(var n:nodes){var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);n.accept(w);jar.putNextEntry(new JarEntry(n.name+".class"));jar.write(w.toByteArray());jar.closeEntry();}}
        var field=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");field.setAccessible(true);var unsafe=(sun.misc.Unsafe)field.get(null);
        try(var loader=new ManagedLegacyClassLoader(path,WidgetPointerFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())){
            var type=loader.linkOwnedClass(child.replace('/','.'));var widget=(AbstractWidget)unsafe.allocateInstance(type);var event=new MouseButtonEvent(7.25,13.5,new MouseButtonInfo(1,0));
            widget.onClick(event,true);widget.onRelease(event);type.getMethod("onDrag",MouseButtonEvent.class,double.class,double.class).invoke(widget,event,2.5,3.5);
            check(type.getField("calls").getInt(widget)==33,"native pointer override/super duplicated or bypassed");check(type.getField("x").getDouble(widget)==7.25&&type.getField("dx").getDouble(widget)==2.5,"native pointer coordinates/deltas lost");
            for(String op:List.of("onClick","onRelease","onDrag")){
                var h=(java.lang.invoke.MethodHandle)type.getMethod(op+"Handle").invoke(null);check(h.type().parameterType(0)==type,"pointer handle receiver widened");
                try{if(op.equals("onDrag"))h.invokeWithArguments(widget,20.,30.,4.,5.);else h.invokeWithArguments(widget,20.,30.);}catch(RuntimeException|Error failure){throw failure;}catch(Throwable failure){throw new AssertionError(failure);}
            }
            check(type.getField("calls").getInt(widget)==66,"source pointer callable dispatch lost");
        }
        Path artifact=Path.of("legacy-mods/CarbonConfig-1.19.2-2.0.2.jar");
        if(Files.exists(artifact))try(var loader=new ManagedLegacyClassLoader(artifact,WidgetPointerFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())){
            var type=loader.linkOwnedClass("carbonconfiglib.gui.base.widgets.CarbonSlider");
            type.getDeclaredMethod("onClick",MouseButtonEvent.class,boolean.class);type.getDeclaredMethod("onDrag",MouseButtonEvent.class,double.class,double.class);
        }
        System.out.println("PASS: coordinate-only widget click/release/drag native entry, source-super order and callable receivers; immutable slider entrypoints linked");
    }
    private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
}
