package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.runtime.render.*;
import java.util.*;
import java.util.function.Predicate;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import com.mojang.blaze3d.platform.InputConstants;

final class EditBoxFamilyCheck {
    static final class Probe extends Forge1192EditBox {
        int keys,lastKey,lastModifiers;
        Probe(){super(null,0,0,100,20,Component.empty());}
        // State-only input fixture; native window/focus lifecycle is checked in the dev client.
        @Override public boolean isFocused(){return true;}
        @Override public boolean lms$keyPressed(int key,int scan,int mods){keys++;lastKey=key;lastModifiers=mods;return super.lms$keyPressed(key,scan,mods);}
    }
    static void run() throws Exception {
        var box=new Probe();var observed=new ArrayList<String>();var candidates=new ArrayList<String>();box.setResponder(observed::add);box.setMaxLength(3);
        box.lms$setFilter(value->{candidates.add(value);return value.matches("[0-9]*");});box.setValue("1234");
        check(box.getValue().equals("123")&&observed.equals(List.of("123","1234"))&&candidates.equals(List.of("1234")),"setValue predicate/truncation/source responder order lost");
        observed.clear();candidates.clear();box.setHighlightPos(0);String highlighted=box.getHighlighted();int cursor=box.getCursorPosition();box.insertText("x");
        check(box.getValue().equals("123")&&box.getHighlighted().equals(highlighted)&&box.getCursorPosition()==cursor&&observed.isEmpty(),"rejected replacement changed value/cursor/selection or notified");
        box.insertText("4");check(box.getValue().equals("4")&&box.getHighlighted().isEmpty()&&observed.equals(List.of("4")),"selected replacement or once-only insertion notification lost");
        observed.clear();box.lms$setFilter(value->{throw new IllegalArgumentException("fixture predicate failure");});
        try{box.setValue("5");throw new AssertionError("predicate exception swallowed");}catch(IllegalArgumentException expected){}
        check(box.getValue().equals("4")&&observed.isEmpty(),"predicate failure changed native state");
        box.lms$setFilter(value->!value.isEmpty());box.deleteChars(-1);check(box.getValue().equals("4")&&observed.isEmpty(),"deletion bypassed filter");
        box.lms$setFilter(value->true);box.deleteChars(-1);check(box.getValue().isEmpty()&&observed.equals(List.of("")),"deletion responder duplicated or lost");
        box.setValue("123");observed.clear();box.insertText("4");check(box.getValue().equals("123")&&observed.equals(List.of("123")),"source full-capacity insertion notification lost");
        observed.clear();int left=Forge1192KeyboardBridge.keyboardCode(263);box.keyPressed(new KeyEvent(left,org.lwjgl.sdl.SDLKeycode.SDLK_LEFT,InputConstants.MOD_SHIFT));
        check(box.keys==1&&box.lastKey==263&&box.lastModifiers==1&&box.getCursorPosition()==2&&box.getHighlighted().equals("3")&&observed.equals(List.of("123")),"native named-key/source override/selection/cursor notification lost: keys="+box.keys+", key="+box.lastKey+", mods="+box.lastModifiers+", cursor="+box.getCursorPosition()+", highlighted="+box.getHighlighted()+", events="+observed);
        var formatText=EditBox.class.getDeclaredMethod("applyFormat",String.class,int.class);formatText.setAccessible(true);
        var sequence=net.minecraft.util.FormattedCharSequence.forward("fixture",net.minecraft.network.chat.Style.EMPTY);var formatted=new ArrayList<String>();
        box.lms$setFormatter((text,offset)->{formatted.add(text+":"+offset);return sequence;});
        check(formatText.invoke(box,"123",2)==sequence&&formatted.equals(List.of("123:2")),"source formatter lost text/offset/native result identity");
        box.lms$setFormatter((text,offset)->net.minecraft.util.FormattedCharSequence.EMPTY);formatted.clear();
        check(formatText.invoke(box,"123",2)==net.minecraft.util.FormattedCharSequence.EMPTY&&formatted.isEmpty(),"source formatter replacement accumulated callbacks");
        for(int bits=0;bits<64;bits++)check(Forge1192KeyboardBridge.sourceModifiers(Forge1192KeyboardBridge.nativeModifiers(bits))==bits,"modifier meaning changed");
        try{Forge1192EditBox.setFilter(new EditBox(null,0,0,100,20,Component.empty()),value->true);throw new AssertionError("native-only box silently opted into source semantics");}catch(UnsupportedOperationException expected){}
        sourceHandles();
        System.out.println("PASS: native text/cursor/selection identity, pre-mutation predicate rejection/failure, truncation/replacement/deletion/source notification counts, named SDL keys/modifiers and exact source constructor/filter handles");
    }
    private static void sourceHandles() throws Exception {
        String root="net/minecraft/client/gui/components/EditBox",name="fixture/SourceEditBox",ctor="(Lnet/minecraft/client/gui/Font;IIIILnet/minecraft/network/chat/Component;)V";
        var node=new org.objectweb.asm.tree.ClassNode();node.version=org.objectweb.asm.Opcodes.V17;node.access=org.objectweb.asm.Opcodes.ACC_PUBLIC;node.name=name;node.superName=root;
        var init=new org.objectweb.asm.tree.MethodNode(org.objectweb.asm.Opcodes.ACC_PUBLIC,"<init>",ctor,null,null);node.methods.add(init);init.instructions.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD,0));init.instructions.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD,1));for(int i=2;i<6;i++)init.instructions.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ILOAD,i));init.instructions.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD,6));init.instructions.add(new org.objectweb.asm.tree.MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESPECIAL,root,"<init>",ctor,false));init.instructions.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.RETURN));
        var handles=List.of(new org.objectweb.asm.Handle(org.objectweb.asm.Opcodes.H_NEWINVOKESPECIAL,root,"<init>",ctor,false),new org.objectweb.asm.Handle(org.objectweb.asm.Opcodes.H_INVOKEVIRTUAL,name,"m_94153_","(Ljava/util/function/Predicate;)V",false));
        for(int i=0;i<handles.size();i++){var method=new org.objectweb.asm.tree.MethodNode(org.objectweb.asm.Opcodes.ACC_PUBLIC|org.objectweb.asm.Opcodes.ACC_STATIC,"handle"+i,"()Ljava/lang/invoke/MethodHandle;",null,null);node.methods.add(method);method.instructions.add(new org.objectweb.asm.tree.LdcInsnNode(handles.get(i)));method.instructions.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.ARETURN));}
        var path=java.nio.file.Files.createTempFile("lms-source-edit", ".jar");var writer=new org.objectweb.asm.ClassWriter(org.objectweb.asm.ClassWriter.COMPUTE_MAXS);node.accept(writer);try(var jar=new java.util.jar.JarOutputStream(java.nio.file.Files.newOutputStream(path))){jar.putNextEntry(new java.util.jar.JarEntry(name+".class"));jar.write(writer.toByteArray());jar.closeEntry();}var original=java.nio.file.Files.readAllBytes(path);
        try(var loader=new com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader(path,EditBoxFamilyCheck.class.getClassLoader(),new com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer())){
            var type=loader.linkOwnedClass(name.replace('/','.'));var instance=(EditBox)type.getConstructor(net.minecraft.client.gui.Font.class,int.class,int.class,int.class,int.class,Component.class).newInstance(null,0,0,100,20,Component.empty());
            var constructor=(java.lang.invoke.MethodHandle)type.getMethod("handle0").invoke(null);var filter=(java.lang.invoke.MethodHandle)type.getMethod("handle1").invoke(null);
            check(constructor.type().returnType()==EditBox.class&&filter.type().parameterType(0)==type,"source handle return/receiver type changed");
            try{var created=constructor.invokeWithArguments(null,0,0,100,20,Component.empty());check(created instanceof Forge1192EditBox,"source constructor handle built unadapted native text field");filter.invokeWithArguments(instance,(Predicate<String>)value->false);}catch(RuntimeException|Error failure){throw failure;}catch(Throwable failure){throw new AssertionError(failure);}
            instance.setValue("rejected");check(instance.getValue().isEmpty(),"source filter method handle disconnected from native mutation");
        }
        check(Arrays.equals(original,java.nio.file.Files.readAllBytes(path)),"source edit-box artifact modified");
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
