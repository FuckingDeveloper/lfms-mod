package com.fuckingdeveloper.lms.profile.forge1192;

import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import com.fuckingdeveloper.lms.transform.SemanticInvocationRewriter;
import static org.objectweb.asm.Opcodes.*;

/** Proven source root callbacks, not fixture names, select the extracted entity renderer base. */
final class Forge1192EntityRendererRewriter {
    static final String ROOT="net/minecraft/client/renderer/entity/EntityRenderer";
    private static final String ENTITY="Lnet/minecraft/world/entity/Entity;", POSE="Lcom/mojang/blaze3d/vertex/PoseStack;", BUFFERS="Lnet/minecraft/client/renderer/MultiBufferSource;";
    private static final String RENDER="("+ENTITY+"FF"+POSE+BUFFERS+"I)V", TEXTURE="("+ENTITY+")Lnet/minecraft/resources/ResourceLocation;";
    private static final String WHITE_ROOT="net/minecraft/client/renderer/entity/TntMinecartRenderer";
    private static final String WHITE="(Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;Lnet/minecraft/world/level/block/state/BlockState;"+POSE+BUFFERS+"IZ)V";
    interface Evidence {boolean subtype(String owner,String root);boolean standalone();String method(String owner,String name,String desc);}
    private static String operation(String owner,String name,String desc,Evidence e) {
        if(!e.subtype(owner,ROOT))return null;
        String canonical=name.startsWith("m_")?e.method(ROOT,name,desc):name;
        return "render".equals(canonical)&&desc.equals(RENDER)?"render":
                "getTextureLocation".equals(canonical)&&desc.equals(TEXTURE)?"texture":null;
    }
    private static String alias(String operation){return operation.equals("render")?"lmsLegacyRender":"lmsLegacyTexture";}
    static int rewrite(ClassNode node,Evidence e,String adapter,SemanticInvocationRewriter calls) {
        int count=0;
        if(e.subtype(node.name,ROOT)&&e.standalone())for(var method:node.methods) {
            if((method.access&ACC_STATIC)!=0)continue;
            String operation=operation(node.name,method.name,method.desc,e);
            if(operation!=null) {
                String alias=alias(operation);
                if(node.methods.stream().anyMatch(m->m!=method&&m.name.equals(alias)&&m.desc.equals(method.desc)))throw new IllegalStateException("Source entity renderer alias collision: "+node.name+"#"+alias);
                method.name=alias;count++;
            } else {
                String canonical=method.name.startsWith("m_")?e.method(ROOT,method.name,method.desc):method.name;
                if(Set.of("getRenderOffset","shouldRender","shouldShowName","renderNameTag").contains(canonical==null?"":canonical))
                    throw new UnsupportedOperationException("Changed source entity renderer override requires its own contract: "+node.name+"#"+method.name+method.desc);
            }
        }
        count+=calls.rewrite(node,(opcode,owner,name,desc)->{
            String op=operation(owner,name,desc,e);if(op==null)return null;
            if(opcode==INVOKESPECIAL&&!owner.equals(ROOT))return null;
            if(opcode==INVOKESPECIAL&&op.equals("texture"))throw new UnsupportedOperationException("Source abstract EntityRenderer texture super call");
            return new Handle(H_INVOKESTATIC,adapter,opcode==INVOKESPECIAL?"baseRender":op,"(L"+ROOT+";"+desc.substring(1),false);
        });
        for(var method:node.methods)for(var insn:method.instructions) {
            if(insn instanceof MethodInsnNode call) {
                if(call.getOpcode()==INVOKESPECIAL&&call.owner.equals(ROOT)&&call.name.equals("<init>")) {
                    if(!call.desc.equals("(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;)V"))throw new UnsupportedOperationException("Unknown source EntityRenderer constructor: "+call.desc);
                    call.owner=adapter;count++;
                }else if(call.getOpcode()==INVOKESPECIAL&&!call.owner.equals(ROOT)) {
                    String op=operation(call.owner,call.name,call.desc,e);if(op!=null){call.name=alias(op);count++;}
                }else if(call.getOpcode()==INVOKESTATIC&&white(call.owner,call.name,call.desc,e)) {call.owner=adapter;call.name="whiteBlock";call.itf=false;count++;}
            }
            if(insn instanceof LdcInsnNode ldc){Object next=constant(ldc.cst,e,adapter);if(next!=ldc.cst){ldc.cst=next;count++;}}
            if(insn instanceof InvokeDynamicInsnNode indy)for(int i=0;i<indy.bsmArgs.length;i++){Object next=constant(indy.bsmArgs[i],e,adapter);if(next!=indy.bsmArgs[i]){indy.bsmArgs[i]=next;count++;}}
        }
        return count;
    }
    private static boolean white(String owner,String name,String desc,Evidence e) {
        return owner.equals(WHITE_ROOT)&&desc.equals(WHITE)&&"renderWhiteSolidBlock".equals(name.startsWith("m_")?e.method(owner,name,desc):name);
    }
    private static Object constant(Object value,Evidence e,String adapter) {
        if(value instanceof Handle h) {
            if(h.getTag()==H_INVOKESTATIC&&white(h.getOwner(),h.getName(),h.getDesc(),e))return new Handle(H_INVOKESTATIC,adapter,"whiteBlock",h.getDesc(),false);
            if(h.getTag()==H_INVOKESPECIAL&&!h.getOwner().equals(ROOT)) {String op=operation(h.getOwner(),h.getName(),h.getDesc(),e);if(op!=null)return new Handle(h.getTag(),h.getOwner(),alias(op),h.getDesc(),h.isInterface());}
        }
        if(value instanceof ConstantDynamic dynamic){Object[] args=new Object[dynamic.getBootstrapMethodArgumentCount()];boolean changed=false;for(int i=0;i<args.length;i++){Object old=dynamic.getBootstrapMethodArgument(i);args[i]=constant(old,e,adapter);changed|=old!=args[i];}if(changed)return new ConstantDynamic(dynamic.getName(),dynamic.getDescriptor(),dynamic.getBootstrapMethod(),args);}
        return value;
    }
    static int construction(ClassNode node,String adapter) {
        if(!node.superName.equals(ROOT))return 0;
        node.superName=adapter;
        if(node.signature!=null) {
            var writer=new org.objectweb.asm.signature.SignatureWriter(){
                @Override public org.objectweb.asm.signature.SignatureVisitor visitSuperclass(){
                    var parent=super.visitSuperclass();return new org.objectweb.asm.signature.SignatureVisitor(ASM9){
                        @Override public void visitClassType(String name){parent.visitClassType(name.equals(ROOT)?adapter:name);}
                        @Override public void visitInnerClassType(String name){parent.visitInnerClassType(name);}
                        @Override public void visitTypeArgument(){parent.visitTypeArgument();}
                        @Override public org.objectweb.asm.signature.SignatureVisitor visitTypeArgument(char wildcard){return parent.visitTypeArgument(wildcard);}
                        @Override public void visitEnd(){parent.visitEnd();}
                    };
                }
            };new org.objectweb.asm.signature.SignatureReader(node.signature).accept(writer);node.signature=writer.toString();
        }
        return 1;
    }
}
