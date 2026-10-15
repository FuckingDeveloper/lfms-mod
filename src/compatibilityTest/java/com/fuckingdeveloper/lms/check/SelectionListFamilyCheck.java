package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.runtime.render.*;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.input.*;

final class SelectionListFamilyCheck {
    static final class Row extends Forge1192ContainerSelectionList.Entry<Row> {
        int renders,clicks;int[] location;
        public List<? extends GuiEventListener> children(){return List.of();}
        public List<? extends NarratableEntry> narratables(){return List.of();}
        public void lms$render(PoseStack pose,int index,int top,int left,int width,int height,int x,int y,boolean hover,float partial){renders++;location=new int[]{index,top,left,width,height,x,y,hover?1:0};}
        public boolean lms$mouseClicked(double x,double y,int button){clicks++;return false;}
    }
    static void run() throws Exception {
        sourceHandlesAndHiding();
        var list=new Forge1192ContainerSelectionList<Row>(null,320,240,30,180,20){};
        var first=new Row();var second=new Row();list.lms$children().add(first);list.lms$children().add(second);
        check(list.children().size()==2&&list.children().getFirst()==first,"source/native entries disconnected");
        check(list.lms$getHeight()==240&&list.getHeight()==150,"screen height collapsed into viewport");
        list.lms$renderList(new PoseStack(),100,38,.5F);
        check(first.renders==1&&second.renders==1&&Arrays.equals(first.location,new int[]{0,34,52,220,16,100,38,1}),"row callback coordinates/order/hover changed");
        check(list.lms$getEntryAtPosition(100,38)==first&&list.lms$getEntryAtPosition(100,58)==second,"source hit testing differs from row layout");
        list.mouseClicked(new MouseButtonEvent(100,38,new MouseButtonInfo(0,0)),false);check(first.clicks==1&&second.clicks==0,"native pointer dispatch lost/duplicated source callback");
        list.lms$children().removeFirst();check(list.children().size()==1&&list.children().getFirst()==second,"source mutation did not reach native tracked list");
        for(int i=0;i<20;i++)list.lms$children().add(new Row());
        list.lms$setScrollAmount(9999);check(list.lms$getScrollAmount()==list.lms$getMaxScroll(),"scroll did not clamp using source content height");
        list.lms$setScrollAmount(0);list.mouseScrolled(100,38,0,-1);check(list.lms$getScrollAmount()==10,"source scroll step not half row height");
        check(Arrays.equals(Forge1192TooltipGuiBridge.guiScissor(20,40,80,60,800,600,400,300),new int[]{10,250,50,280}),"framebuffer bottom-left scissor projection lost");
        try{Forge1192TooltipGuiBridge.disableScissor();throw new AssertionError("unscoped scissors accepted");}catch(IllegalStateException expected){}
        System.out.println("PASS: native tracked entry identity/mutation, source viewport/row geometry/hover, once-only pointer callbacks, source scroll clamp/step and framebuffer scissor projection; real extraction requires client");
    }
    private static void sourceHandlesAndHiding() throws Exception {
        String source="net/minecraft/client/gui/components/ContainerObjectSelectionList",name="fixture/SourceSelectionList",hidden="fixture/HiddenSelectionList";
        var node=new org.objectweb.asm.tree.ClassNode();node.version=org.objectweb.asm.Opcodes.V17;node.access=org.objectweb.asm.Opcodes.ACC_PUBLIC;node.name=name;node.superName=source;
        String ctor="(Lnet/minecraft/client/Minecraft;IIIII)V";
        var init=new org.objectweb.asm.tree.MethodNode(org.objectweb.asm.Opcodes.ACC_PUBLIC,"<init>",ctor,null,null);node.methods.add(init);
        init.instructions.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD,0));init.instructions.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD,1));for(int i=2;i<7;i++)init.instructions.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ILOAD,i));init.instructions.add(new org.objectweb.asm.tree.MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESPECIAL,source,"<init>",ctor,false));init.instructions.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.RETURN));
        var child=new org.objectweb.asm.tree.ClassNode();child.version=node.version;child.access=node.access;child.name=hidden;child.superName=name;child.fields.add(new org.objectweb.asm.tree.FieldNode(org.objectweb.asm.Opcodes.ACC_PUBLIC,"f_93388_","I",null,null));
        var ci=new org.objectweb.asm.tree.MethodNode(org.objectweb.asm.Opcodes.ACC_PUBLIC,"<init>",ctor,null,null);child.methods.add(ci);ci.instructions.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD,0));ci.instructions.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD,1));for(int i=2;i<7;i++)ci.instructions.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ILOAD,i));ci.instructions.add(new org.objectweb.asm.tree.MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESPECIAL,name,"<init>",ctor,false));ci.instructions.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.RETURN));
        for(var n:List.of(node,child)){
            var method=new org.objectweb.asm.tree.MethodNode(org.objectweb.asm.Opcodes.ACC_PUBLIC|org.objectweb.asm.Opcodes.ACC_STATIC,"widthHandle","()Ljava/lang/invoke/MethodHandle;",null,null);n.methods.add(method);method.instructions.add(new org.objectweb.asm.tree.LdcInsnNode(new org.objectweb.asm.Handle(org.objectweb.asm.Opcodes.H_GETFIELD,n.name,"f_93388_","I",false)));method.instructions.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.ARETURN));
        }
        var path=java.nio.file.Files.createTempFile("lms-selection-source", ".jar");try(var jar=new java.util.jar.JarOutputStream(java.nio.file.Files.newOutputStream(path))){for(var n:List.of(node,child)){var writer=new org.objectweb.asm.ClassWriter(org.objectweb.asm.ClassWriter.COMPUTE_MAXS);n.accept(writer);jar.putNextEntry(new java.util.jar.JarEntry(n.name+".class"));jar.write(writer.toByteArray());jar.closeEntry();}}
        var original=java.nio.file.Files.readAllBytes(path);
        try(var loader=new com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader(path,SelectionListFamilyCheck.class.getClassLoader(),new com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer())){
            for(String n:List.of(name,hidden)){
                var type=loader.linkOwnedClass(n.replace('/','.'));var value=type.getConstructor(net.minecraft.client.Minecraft.class,int.class,int.class,int.class,int.class,int.class).newInstance(null,320,240,30,180,20);
                if(n.equals(hidden))type.getField("f_93388_").setInt(value,17);
                var handle=(java.lang.invoke.MethodHandle)type.getMethod("widthHandle").invoke(null);
                check(handle.type().equals(java.lang.invoke.MethodType.methodType(int.class,type)),"source field handle receiver type widened");
                try{check((int)handle.invokeWithArguments(value)==(n.equals(hidden)?17:320),"source field hiding or native width ownership changed");}catch(RuntimeException|Error failure){throw failure;}catch(Throwable failure){throw new AssertionError(failure);}
            }
        }
        check(Arrays.equals(original,java.nio.file.Files.readAllBytes(path)),"source list fixture mutated");
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
