package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

final class ScreenLayerFamilyCheck implements Opcodes {
    static void run() throws Exception {
        String root="net/minecraftforge/client/ForgeHooksClient", bridge="com/fuckingdeveloper/lms/profile/forge1192/runtime/render/Forge1192MinecraftClientBridge";
        var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);w.visit(V17,ACC_PUBLIC,"fixture/LayeredScreens",null,"java/lang/Object",null);
        for(String operation:List.of("pushGuiLayer","popGuiLayer","clearGuiLayers")) {
            String desc="(Lnet/minecraft/client/Minecraft;"+(operation.equals("pushGuiLayer")?"Lnet/minecraft/client/gui/screens/Screen;":"")+")V";
            var m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,operation,desc,null,null);m.visitCode();m.visitVarInsn(ALOAD,0);if(operation.equals("pushGuiLayer"))m.visitVarInsn(ALOAD,1);
            m.visitMethodInsn(INVOKESTATIC,root,operation,desc,false);m.visitInsn(RETURN);m.visitMaxs(0,0);m.visitEnd();
            m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,operation+"Handle","()Ljava/lang/invoke/MethodHandle;",null,null);m.visitCode();m.visitLdcInsn(new Handle(H_INVOKESTATIC,root,operation,desc,false));m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
        }
        for(String operation:List.of("pushGuiLayer","popGuiLayer")) {
            String args=operation.equals("pushGuiLayer")?"Lnet/minecraft/client/gui/screens/Screen;":"";
            String desc="(Lnet/minecraft/client/Minecraft;"+args+")V";
            var m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"instance"+operation,desc,null,null);m.visitCode();m.visitVarInsn(ALOAD,0);if(!args.isEmpty())m.visitVarInsn(ALOAD,1);
            m.visitMethodInsn(INVOKEVIRTUAL,"net/minecraft/client/Minecraft",operation,"("+args+")V",false);m.visitInsn(RETURN);m.visitMaxs(0,0);m.visitEnd();
            m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"instance"+operation+"Handle","()Ljava/lang/invoke/MethodHandle;",null,null);m.visitCode();
            m.visitLdcInsn(new Handle(H_INVOKEVIRTUAL,"net/minecraft/client/Minecraft",operation,"("+args+")V",false));m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
        }
        w.visitEnd();var transformer=new Forge1192LegacyClassTransformer();var node=new ClassNode();
        new ClassReader(transformer.transform("fixture.LayeredScreens",w.toByteArray()).bytes()).accept(node,0);
        int calls=0,handles=0;
        for(var method:node.methods)for(var insn:method.instructions) {
            if(insn instanceof MethodInsnNode call){check(call.owner.equals(bridge),"layer call escaped");calls++;}
            if(insn instanceof LdcInsnNode ldc&&ldc.cst instanceof Handle handle){check(handle.getOwner().equals(bridge),"layer handle escaped");handles++;}
        }
        check(calls==5&&handles==5,"whole layer transition family not covered");
        for(String method:List.of("popScreenLayer","clearScreenLayers"))net.minecraft.client.gui.Gui.class.getMethod(method);
        net.minecraft.client.gui.Gui.class.getMethod("pushScreenLayer",net.minecraft.client.gui.screens.Screen.class);
        Path artifact=Path.of("legacy-mods/CarbonConfig-1.19.2-2.0.2.jar");
        if(Files.exists(artifact))try(var jar=new JarFile(artifact.toFile())) {
            String owner="carbonconfiglib/gui/base/screen/BaseCarbonScreen";byte[] original;
            try(var input=jar.getInputStream(jar.getJarEntry(owner+".class"))){original=input.readAllBytes();}
            var actual=new ClassNode();new ClassReader(transformer.transform(owner.replace('/','.'),original).bytes()).accept(actual,0);
            int count=0;for(var method:actual.methods)for(var insn:method.instructions)if(insn instanceof MethodInsnNode call&&call.name.equals("pushGuiLayer")){check(call.owner.equals(bridge),"original source layer call escaped");count++;}
            check(count==2,"original pushExternalScreen/pushScreen paths not covered");
        }
        backgroundFrames();
        check(com.fuckingdeveloper.lms.mixin.Target263GuiLayerAccess.class.isAssignableFrom(net.minecraft.client.gui.Gui.class),"native layer accessor not applied");
        check(Arrays.stream(net.minecraft.client.gui.screens.Screen.class.getDeclaredMethods()).anyMatch(method->method.getName().contains("lms$sourceOverlayBackground")),"native Screen background injection not applied");
        if(Files.exists(artifact))try(var jar=new JarFile(artifact.toFile())) {
            String owner="carbonconfiglib/gui/base/widgets/DropDownMenu$DropDownScreen";byte[] original;
            try(var input=jar.getInputStream(jar.getJarEntry(owner+".class"))){original=input.readAllBytes();}
            var actual=new ClassNode();new ClassReader(transformer.transform(owner.replace('/','.'),original).bytes()).accept(actual,0);
            int pops=0;for(var method:actual.methods)for(var insn:method.instructions)if(insn instanceof MethodInsnNode call&&call.name.equals("popGuiLayer")){check(call.owner.equals(bridge)&&call.getOpcode()==INVOKESTATIC,"original source instance pop escaped");pops++;}
            check(pops==3,"original dropdown resize/reopen and selection/cancel pops not covered: "+pops);
        }
        System.out.println("PASS: all source static/instance layer calls and handles, original editor/resize consumers, actual native layer accessor/background injection and multi-layer native blur frames; interactive editors require client acceptance");
    }
    private static void backgroundFrames() {
        var state=new net.minecraft.client.renderer.state.gui.GuiRenderState();
        Object root=new Object(), middle=new Object(), top=new Object(), unrelated=new Object();
        var layers=List.of(root,middle);
        for(int frame=0;frame<3;frame++) {
            for(Object screen:List.of(root,middle,top)) {
                state.nextStratum();
                if(!com.fuckingdeveloper.lms.target.neoforge263.render.Target263SourceScreenBackground.aboveBottom(screen,top,layers))state.blurBeforeThisStratum();
            }
            try {state.blurBeforeThisStratum();throw new AssertionError("native blur invariant was disabled");}
            catch(IllegalStateException expected){check(expected.getMessage().contains("once per frame"),"wrong native blur failure");}
            state.reset(); // The next frame must still apply the root blur normally.
        }
        check(!com.fuckingdeveloper.lms.target.neoforge263.render.Target263SourceScreenBackground.aboveBottom(root,root,List.of()),"empty stack changed");
        check(!com.fuckingdeveloper.lms.target.neoforge263.render.Target263SourceScreenBackground.aboveBottom(unrelated,top,layers),"off-stack screen changed");
        Object equalButDistinct=new Object(){public boolean equals(Object other){return true;}};
        check(!com.fuckingdeveloper.lms.target.neoforge263.render.Target263SourceScreenBackground.aboveBottom(equalButDistinct,top,layers),"screen equality substituted for identity");
    }
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}
