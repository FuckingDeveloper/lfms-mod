package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.runtime.render.Forge1192ScreenUtilsBridge;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.util.*;

/** Removed callbacks and tiled geometry are independent of any reference mod. */
public final class WidgetExtractionFamilyCheck {
    private static final String POSE="Lcom/mojang/blaze3d/vertex/PoseStack;";
    public static void run() {
        blitFamily();
        var parent=fixture("fixture/SourceButton","net/minecraft/client/gui/components/AbstractButton");
        var child=fixture("fixture/ChildButton",parent.name);
        var transformer=new Forge1192LegacyClassTransformer();
        transformer.bindManagedClassBytes(name->name.equals(parent.name)?bytes(parent):null);
        var root=transformed(transformer,parent);var nested=transformed(transformer,child);
        check(root.methods.stream().anyMatch(m->m.name.equals("extractContents")),"button content callback missing");
        check(root.methods.stream().noneMatch(m->m.name.equals("extractWidgetRenderState")||m.name.equals("extractRenderState")),"native final extractor overridden");
        check(root.fields.stream().anyMatch(f->f.name.equals("lms$widgetDepth")),"mutable source depth absent");
        check(nested.fields.stream().noneMatch(f->f.name.equals("lms$widgetDepth")),"subclass split inherited depth storage");
        for(var node:List.of(root,nested)) {
            var method=node.methods.stream().filter(m->m.name.equals("primitives")).findFirst().orElseThrow();
            check(((FieldInsnNode)method.instructions.getFirst()).owner.equals("com/fuckingdeveloper/lms/runtime/Target263LegacyWidgetAssets"),"inherited widget image not projected");
            check(java.util.stream.StreamSupport.stream(method.instructions.spliterator(),false).filter(i->i instanceof MethodInsnNode).allMatch(i->((MethodInsnNode)i).owner.startsWith("com/fuckingdeveloper/lms/runtime/")),"source primitive left unresolved: " + java.util.stream.StreamSupport.stream(method.instructions.spliterator(),false).filter(i->i instanceof MethodInsnNode).map(i->{var c=(MethodInsnNode)i;return c.owner+"#"+c.name+c.desc;}).toList());
        }
        var shadow=fixture("fixture/ShadowButton",parent.name);
        shadow.fields.add(new FieldNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"f_93617_","Lnet/minecraft/resources/ResourceLocation;",null,null));
        var hidden=transformed(transformer,shadow);
        var field=(FieldInsnNode)hidden.methods.stream().filter(m->m.name.equals("primitives")).findFirst().orElseThrow().instructions.getFirst();
        check(field.owner.equals(shadow.name),"managed hidden texture field redirected");
        var tiles=Forge1192ScreenUtilsBridge.tiles(10,20,0,46,23,11,20,20,2,2,2,2);
        int area=tiles.stream().mapToInt(t->t.width()*t.height()).sum();
        check(area==23*11,"nine-slice has overlap or missing remainder");
        check(tiles.stream().anyMatch(t->t.width()==3 && t.height()==7 && t.u()==2 && t.v()==48),"remainder tile stretches texture");
        for(var tile:tiles)check(tile.x()>=10&&tile.y()>=20&&tile.x()+tile.width()<=33&&tile.y()+tile.height()<=31,"tile escapes source destination");
        System.out.println("PASS: source widget extraction, final-native boundary, inherited depth/texture hiding and tiled remainder geometry");
    }
    private static void blitFamily() {
        var signatures=List.of("("+POSE+"IIIFFIIII)V","("+POSE+"IIIIFFIIII)V","("+POSE+"IIFFIIII)V","("+POSE+"IIIII)V","("+POSE+"IIIIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V");
        var names=List.of("m_93143_","m_93160_","m_93133_","m_93172_","m_93200_");
        for(String base:List.of("net/minecraft/client/gui/screens/Screen","net/minecraft/client/gui/components/AbstractWidget","java/lang/Object")) {
            var node=new ClassNode();node.version=Opcodes.V17;node.access=Opcodes.ACC_PUBLIC|Opcodes.ACC_ABSTRACT;node.name="fixture/BlitOwner";node.superName=base;
            var method=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"references","()V",null,null);node.methods.add(method);
            for(int i=0;i<names.size();i++) {
                var handle=new Handle(Opcodes.H_INVOKESTATIC,node.name,names.get(i),signatures.get(i),false);
                var boot=new Handle(Opcodes.H_INVOKESTATIC,"fixture/Bootstrap","constant","(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;",false);
                method.instructions.add(new LdcInsnNode(handle));method.instructions.add(new InsnNode(Opcodes.POP));
                method.instructions.add(new LdcInsnNode(new ConstantDynamic("nested","Ljava/lang/Object;",boot,handle)));method.instructions.add(new InsnNode(Opcodes.POP));
            }
            method.instructions.add(new InsnNode(Opcodes.RETURN));
            var mapped=transformed(new Forge1192LegacyClassTransformer(),node);
            int adapted=0;
            for(var instruction:mapped.methods.getFirst().instructions)if(instruction instanceof LdcInsnNode ldc) {
                var h=ldc.cst instanceof Handle handle?handle:(Handle)((ConstantDynamic)ldc.cst).getBootstrapMethodArgument(0);
                if(h.getOwner().endsWith("Forge1192TooltipGuiBridge")) {check(Set.of("blit","fill").contains(h.getName())&&signatures.contains(h.getDesc()),"blit overload ABI changed");adapted++;}
            }
            check(adapted==(base.equals("java/lang/Object")?0:10),"inherited static blit or unrelated owner resolved incorrectly: "+base);
        }
    }
    private static ClassNode fixture(String name,String parent) {
        var node=new ClassNode();node.version=Opcodes.V17;node.access=Opcodes.ACC_PUBLIC|Opcodes.ACC_ABSTRACT;node.name=name;node.superName=parent;
        var callback=new MethodNode(Opcodes.ACC_PROTECTED,"m_6303_","("+POSE+"IIF)V",null,null);callback.instructions.add(new InsnNode(Opcodes.RETURN));node.methods.add(callback);
        var primitives=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"primitives","("+POSE+"Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;)V",null,null);
        primitives.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC,name,"f_93617_","Lnet/minecraft/resources/ResourceLocation;"));primitives.instructions.add(new InsnNode(Opcodes.POP));
        primitives.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));primitives.instructions.add(new VarInsnNode(Opcodes.ALOAD,1));primitives.instructions.add(new VarInsnNode(Opcodes.ALOAD,2));for(int i=0;i<3;i++)primitives.instructions.add(new InsnNode(Opcodes.ICONST_0));
        primitives.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,name,"m_93215_","("+POSE+"Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V",false));
        primitives.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));for(int i=0;i<9;i++)primitives.instructions.add(new InsnNode(Opcodes.ICONST_0));primitives.instructions.add(new InsnNode(Opcodes.FCONST_0));primitives.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"net/minecraftforge/client/gui/ScreenUtils","blitWithBorder","("+POSE+"IIIIIIIIIF)V",false));primitives.instructions.add(new InsnNode(Opcodes.ICONST_0));primitives.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC,name,"f_93617_","Lnet/minecraft/resources/ResourceLocation;"));primitives.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"com/mojang/blaze3d/systems/RenderSystem","m_157456_","(ILnet/minecraft/resources/ResourceLocation;)V",false));
        primitives.instructions.add(new LdcInsnNode(new Handle(Opcodes.H_INVOKESTATIC,"com/mojang/blaze3d/systems/RenderSystem","m_157429_","(FFFF)V",false)));primitives.instructions.add(new InsnNode(Opcodes.POP));
        primitives.instructions.add(new InsnNode(Opcodes.RETURN));node.methods.add(primitives);return node;
    }
    private static byte[] bytes(ClassNode node){var out=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(out);return out.toByteArray();}
    private static ClassNode transformed(Forge1192LegacyClassTransformer transformer,ClassNode node){var out=new ClassNode();new ClassReader(transformer.transform(node.name.replace('/','.'),bytes(node)).bytes()).accept(out,0);return out;}
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}
