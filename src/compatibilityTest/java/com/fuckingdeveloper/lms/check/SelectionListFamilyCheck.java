package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.render.Forge1192ContainerSelectionList;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.render.Forge1192GuiScrollBridge;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.render.Forge1192TooltipGuiBridge;


import com.mojang.blaze3d.vertex.PoseStack;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.util.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.input.*;

final class SelectionListFamilyCheck {
    static final class Row extends Forge1192ContainerSelectionList.Entry<Row> {
        int renders,clicks,lastButton;int[] location;
        public List<? extends GuiEventListener> children(){return List.of();}
        public List<? extends NarratableEntry> narratables(){return List.of();}
        public void lms$render(PoseStack pose,int index,int top,int left,int width,int height,int x,int y,boolean hover,float partial){renders++;location=new int[]{index,top,left,width,height,x,y,hover?1:0};}
        public boolean lms$mouseClicked(double x,double y,int button){clicks++;lastButton=button;return false;}
    }
    static final class FocusList extends Forge1192ContainerSelectionList<FocusRow> {
        int changes;
        FocusList(){super(null,320,240,30,180,20);}
        @Override public void setSelected(FocusRow value){super.setSelected(value);changes++;}
    }
    static final class FocusRow extends Forge1192ContainerSelectionList.Entry<FocusRow> {
        final FocusList owner;int clicks,focusCallbacks;
        FocusRow(FocusList owner){this.owner=owner;}
        public List<? extends GuiEventListener> children(){return List.of();}
        public List<? extends NarratableEntry> narratables(){return List.of();}
        public void lms$render(PoseStack p,int i,int t,int l,int w,int h,int x,int y,boolean hover,float partial){}
        @Override public boolean lms$mouseClicked(double x,double y,int button){clicks++;owner.setSelected(this);return true;}
        @Override public void setFocused(boolean value){focusCallbacks++;super.setFocused(value);}
    }
    private static void focusOwnership(){
        var list=new FocusList();var first=new FocusRow(list);var second=new FocusRow(list);
        list.lms$children().add(first);list.lms$children().add(second);
        // Native dispatch: source row selects, then its source list focuses the row.
        // Target list focus must not synthesize a second selection callback/pop.
        var click=new MouseButtonEvent(100,38,new MouseButtonInfo(1,0));
        check(list.mouseClicked(click,false),"source row activation not consumed");
        check(first.clicks==1&&list.changes==1&&list.getSelected()==first&&list.getFocused()==first,"focus duplicated source selection notification");
        check(list.mouseClicked(click,true)&&list.changes==2,"explicit repeated selection was silently deduplicated");
        list.setFocused(second);check(list.getFocused()==second&&list.getSelected()==first&&list.changes==2,"focus changed independent selection");
        list.setFocused(null);check(list.getFocused()==null&&list.getSelected()==first&&list.changes==2,"null focus cleared source selection");
        list.setSelected(second);check(list.getSelected()==second&&list.getFocused()==null&&list.changes==3,"source selection mutated focus or native storage differs");
        list.setSelected(null);check(list.getSelected()==null&&list.changes==4,"explicit selection clear lost");
        check(first.focusCallbacks==0&&second.focusCallbacks==0,"source list focus synthesized child callbacks");
    }
    static void run() throws Exception {
        focusOwnership();
        scrollDispatch();
        sourceHandlesAndHiding();
        var list=new Forge1192ContainerSelectionList<Row>(null,320,240,30,180,20){};
        var first=new Row();var second=new Row();list.lms$children().add(first);list.lms$children().add(second);
        check(list.children().size()==2&&list.children().getFirst()==first,"source/native entries disconnected");
        check(list.lms$getHeight()==240&&list.getHeight()==150,"screen height collapsed into viewport");
        list.lms$renderList(new PoseStack(),100,38,.5F);
        check(first.renders==1&&second.renders==1&&Arrays.equals(first.location,new int[]{0,34,52,220,16,100,38,1}),"row callback coordinates/order/hover changed");
        check(list.lms$getEntryAtPosition(100,38)==first&&list.lms$getEntryAtPosition(100,58)==second,"source hit testing differs from row layout");
        list.mouseClicked(new MouseButtonEvent(100,38,new MouseButtonInfo(1,0)),false);check(first.clicks==1&&first.lastButton==0&&second.clicks==0,"native pointer dispatch lost/duplicated source callback");
        list.lms$children().removeFirst();check(list.children().size()==1&&list.children().getFirst()==second,"source mutation did not reach native tracked list");
        for(int i=0;i<20;i++)list.lms$children().add(new Row());
        list.lms$setScrollAmount(9999);check(list.lms$getScrollAmount()==list.lms$getMaxScroll(),"scroll did not clamp using source content height");
        list.lms$setScrollAmount(0);list.mouseScrolled(100,38,0,-1);check(list.lms$getScrollAmount()==10,"source scroll step not half row height");
        check(Arrays.equals(Forge1192TooltipGuiBridge.guiScissor(20,40,80,60,800,600,400,300),new int[]{10,250,50,280}),"framebuffer bottom-left scissor projection lost");
        try{Forge1192TooltipGuiBridge.disableScissor();throw new AssertionError("unscoped scissors accepted");}catch(IllegalStateException expected){}
        System.out.println("PASS: native tracked entry identity/mutation, source viewport/row geometry/hover, once-only pointer callbacks, source scroll clamp/step and framebuffer scissor projection; real extraction requires client");
    }
    private static void scrollDispatch() throws Exception {
        check(Forge1192GuiScrollBridge.sourceAmount(true,3,0)==3&&Forge1192GuiScrollBridge.sourceAmount(true,3,-2)==-2&&Forge1192GuiScrollBridge.sourceAmount(false,3,0)==0&&Forge1192GuiScrollBridge.sourceAmount(false,3,-2)==-2,"source macOS axis normalization changed");
        String listener="net/minecraft/client/gui/components/events/GuiEventListener",base="fixture/ScrollBase",child="fixture/ScrollChild";
        var nodes=new ArrayList<ClassNode>();
        for(String name:List.of(base,child)) {
            var node=new ClassNode();node.version=Opcodes.V17;node.access=Opcodes.ACC_PUBLIC;node.name=name;node.superName=name.equals(base)?"java/lang/Object":base;nodes.add(node);
            if(name.equals(base)){
                node.interfaces.add(listener);node.fields.add(new FieldNode(Opcodes.ACC_PUBLIC,"calls","I",null,null));
                var focus=new MethodNode(Opcodes.ACC_PUBLIC,"isFocused","()Z",null,null);focus.instructions.add(new InsnNode(Opcodes.ICONST_0));focus.instructions.add(new InsnNode(Opcodes.IRETURN));node.methods.add(focus);
                var set=new MethodNode(Opcodes.ACC_PUBLIC,"setFocused","(Z)V",null,null);set.instructions.add(new InsnNode(Opcodes.RETURN));node.methods.add(set);
                var defaults=new MethodNode(Opcodes.ACC_PUBLIC,"baseDefault","(DDD)Z",null,null);node.methods.add(defaults);scrollArguments(defaults);defaults.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,listener,"m_6050_","(DDD)Z",true));defaults.instructions.add(new InsnNode(Opcodes.IRETURN));
            }
            var init=new MethodNode(Opcodes.ACC_PUBLIC,"<init>","()V",null,null);node.methods.add(init);init.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));init.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,node.superName,"<init>","()V",false));init.instructions.add(new InsnNode(Opcodes.RETURN));
            var callback=new MethodNode(Opcodes.ACC_PUBLIC,"m_6050_","(DDD)Z",null,null);node.methods.add(callback);
            callback.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));callback.instructions.add(new InsnNode(Opcodes.DUP));callback.instructions.add(new FieldInsnNode(Opcodes.GETFIELD,base,"calls","I"));callback.instructions.add(new IntInsnNode(Opcodes.BIPUSH,name.equals(base)?1:10));callback.instructions.add(new InsnNode(Opcodes.IADD));callback.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD,base,"calls","I"));
            if(name.equals(base))callback.instructions.add(new InsnNode(Opcodes.ICONST_1));else{scrollArguments(callback);callback.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,base,"m_6050_","(DDD)Z",false));}
            callback.instructions.add(new InsnNode(Opcodes.IRETURN));
            for(boolean nested:new boolean[]{false,true}) {
                var getter=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,nested?"nestedHandle":"handle","()Ljava/lang/invoke/MethodHandle;",null,null);node.methods.add(getter);
                var handle=new Handle(name.equals(base)?Opcodes.H_INVOKEINTERFACE:Opcodes.H_INVOKEVIRTUAL,name.equals(base)?listener:name,"m_6050_","(DDD)Z",name.equals(base));
                Object value=nested?new ConstantDynamic("scroll","Ljava/lang/invoke/MethodHandle;",new Handle(Opcodes.H_INVOKESTATIC,"java/lang/invoke/ConstantBootstraps","explicitCast","(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;",false),handle):handle;
                getter.instructions.add(new LdcInsnNode(value));getter.instructions.add(new InsnNode(Opcodes.ARETURN));
            }
            var lambda=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"lambda","(L"+listener+";DD)Ljava/util/function/DoublePredicate;",null,null);node.methods.add(lambda);
            lambda.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));lambda.instructions.add(new VarInsnNode(Opcodes.DLOAD,1));lambda.instructions.add(new VarInsnNode(Opcodes.DLOAD,3));
            lambda.instructions.add(new InvokeDynamicInsnNode("test",lambda.desc,new Handle(Opcodes.H_INVOKESTATIC,"java/lang/invoke/LambdaMetafactory","metafactory","(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",false),Type.getMethodType("(D)Z"),new Handle(Opcodes.H_INVOKEINTERFACE,listener,"m_6050_","(DDD)Z",true),Type.getMethodType("(D)Z")));lambda.instructions.add(new InsnNode(Opcodes.ARETURN));
        }
        var inherited=new ClassNode();inherited.version=Opcodes.V17;inherited.access=Opcodes.ACC_PUBLIC;inherited.name="fixture/ScrollInherited";inherited.superName=child;var init=new MethodNode(Opcodes.ACC_PUBLIC,"<init>","()V",null,null);inherited.methods.add(init);init.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));init.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,child,"<init>","()V",false));init.instructions.add(new InsnNode(Opcodes.RETURN));nodes.add(inherited);
        var unrelated=new ClassNode();unrelated.version=Opcodes.V17;unrelated.access=Opcodes.ACC_PUBLIC;unrelated.name="fixture/UnrelatedScroll";unrelated.superName="java/lang/Object";
        var same=new MethodNode(Opcodes.ACC_PUBLIC,"m_6050_","(DDD)Z",null,null);same.instructions.add(new InsnNode(Opcodes.ICONST_1));same.instructions.add(new InsnNode(Opcodes.IRETURN));unrelated.methods.add(same);
        var nativeHandle=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"nativeHandle","()Ljava/lang/invoke/MethodHandle;",null,null);unrelated.methods.add(nativeHandle);nativeHandle.instructions.add(new LdcInsnNode(new Handle(Opcodes.H_INVOKEINTERFACE,listener,"mouseScrolled","(DDDD)Z",true)));nativeHandle.instructions.add(new InsnNode(Opcodes.ARETURN));
        var all=new ArrayList<ClassNode>(nodes);all.add(unrelated);
        var path=java.nio.file.Files.createTempFile("lms-scroll-dispatch", ".jar");try(var jar=new java.util.jar.JarOutputStream(java.nio.file.Files.newOutputStream(path))){for(var node:all){var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);jar.putNextEntry(new java.util.jar.JarEntry(node.name+".class"));jar.write(writer.toByteArray());jar.closeEntry();}}
        try(var loader=new com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader(path,SelectionListFamilyCheck.class.getClassLoader(),new com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer())) {
            var unrelatedType=loader.linkOwnedClass(unrelated.name.replace('/','.'));check(unrelatedType.getDeclaredMethod("m_6050_",double.class,double.class,double.class)!=null,"descriptor coincidence rewrote unrelated scroll member");
            var unchanged=(java.lang.invoke.MethodHandle)unrelatedType.getMethod("nativeHandle").invoke(null);check(unchanged.type().equals(java.lang.invoke.MethodType.methodType(boolean.class,GuiEventListener.class,double.class,double.class,double.class,double.class)),"native four-argument descriptor rewritten as source scroll");
            for(var node:nodes){var type=loader.linkOwnedClass(node.name.replace('/','.'));var value=(GuiEventListener)type.getConstructor().newInstance();int increment=node.name.equals(base)?1:11;
                check(Forge1192GuiScrollBridge.mouseScrolled(value,100,38,-1)&&type.getField("calls").getInt(value)==increment,"native scroll lost inherited/source-super dispatch");
                check(!(boolean)type.getMethod("baseDefault",double.class,double.class,double.class).invoke(value,100.,38.,-1.),"source interface default was redispatched virtually");
                for(String name:List.of("handle","nestedHandle")){var handle=(java.lang.invoke.MethodHandle)type.getMethod(name).invoke(null);check(handle.type().parameterType(0)==(node.name.equals(base)?GuiEventListener.class:loader.linkOwnedClass(child.replace('/','.'))),"source scroll handle receiver widened");try{check((boolean)handle.invokeWithArguments(value,100.,38.,-1.),"scroll handle returned wrong consumption");}catch(RuntimeException|Error failure){throw failure;}catch(Throwable failure){throw new AssertionError(failure);}}
                var lambda=(java.util.function.DoublePredicate)type.getMethod("lambda",GuiEventListener.class,double.class,double.class).invoke(null,value,100.,38.);check(lambda.test(-1.),"real source scroll lambda did not link/dispatch");
                check(type.getField("calls").getInt(value)==4*increment,"source input callback duplicated");
                check(value.mouseScrolled(100,38,1,-1)&&type.getField("calls").getInt(value)==5*increment,"diagonal native scroll did not retain source vertical dispatch");
            }
        }
    }
    private static void scrollArguments(MethodNode method){method.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));method.instructions.add(new VarInsnNode(Opcodes.DLOAD,1));method.instructions.add(new VarInsnNode(Opcodes.DLOAD,3));method.instructions.add(new VarInsnNode(Opcodes.DLOAD,5));}
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
        var focus=new org.objectweb.asm.tree.MethodNode(org.objectweb.asm.Opcodes.ACC_PROTECTED,"m_5694_","()Z",null,null);
        child.methods.add(focus);focus.instructions.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.ICONST_1));focus.instructions.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.IRETURN));
        var baseFocus=new org.objectweb.asm.tree.MethodNode(org.objectweb.asm.Opcodes.ACC_PUBLIC,"baseFocus","()Z",null,null);
        child.methods.add(baseFocus);baseFocus.instructions.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD,0));baseFocus.instructions.add(new org.objectweb.asm.tree.MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESPECIAL,name,"m_5694_","()Z",false));baseFocus.instructions.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.IRETURN));
        for(var n:List.of(node,child)){
            var method=new org.objectweb.asm.tree.MethodNode(org.objectweb.asm.Opcodes.ACC_PUBLIC|org.objectweb.asm.Opcodes.ACC_STATIC,"focusHandle","()Ljava/lang/invoke/MethodHandle;",null,null);
            n.methods.add(method);method.instructions.add(new org.objectweb.asm.tree.LdcInsnNode(new org.objectweb.asm.Handle(org.objectweb.asm.Opcodes.H_INVOKEVIRTUAL,n.name,"m_5694_","()Z",false)));method.instructions.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.ARETURN));
        }
        var path=java.nio.file.Files.createTempFile("lms-selection-source", ".jar");try(var jar=new java.util.jar.JarOutputStream(java.nio.file.Files.newOutputStream(path))){for(var n:List.of(node,child)){var writer=new org.objectweb.asm.ClassWriter(org.objectweb.asm.ClassWriter.COMPUTE_MAXS);n.accept(writer);jar.putNextEntry(new java.util.jar.JarEntry(n.name+".class"));jar.write(writer.toByteArray());jar.closeEntry();}}
        var original=java.nio.file.Files.readAllBytes(path);
        try(var loader=new com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader(path,SelectionListFamilyCheck.class.getClassLoader(),new com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer())){
            for(String n:List.of(name,hidden)){
                var type=loader.linkOwnedClass(n.replace('/','.'));var value=type.getConstructor(net.minecraft.client.Minecraft.class,int.class,int.class,int.class,int.class,int.class).newInstance(null,320,240,30,180,20);
                check(((Forge1192ContainerSelectionList<?>)value).lms$isFocused()==n.equals(hidden),"source focus override dispatch lost");
                if(n.equals(hidden))check(!(boolean)type.getMethod("baseFocus").invoke(value),"source focus super call redispatched override");
                var focusHandle=(java.lang.invoke.MethodHandle)type.getMethod("focusHandle").invoke(null);
                check(focusHandle.type().equals(java.lang.invoke.MethodType.methodType(boolean.class,type)),"source focus handle receiver widened");
                try{check((boolean)focusHandle.invokeWithArguments(value)==n.equals(hidden),"source focus handle dispatch lost");}catch(RuntimeException|Error failure){throw failure;}catch(Throwable failure){throw new AssertionError(failure);}
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
