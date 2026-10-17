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
    private static final String NAME_TAG="("+ENTITY+"Lnet/minecraft/network/chat/Component;"+POSE+BUFFERS+"I)V";
    private static final String WHITE_ROOT="net/minecraft/client/renderer/entity/TntMinecartRenderer";
    private static final String WHITE="(Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;Lnet/minecraft/world/level/block/state/BlockState;"+POSE+BUFFERS+"IZ)V";
    interface Evidence {boolean subtype(String owner,String root);boolean standalone();String method(String owner,String name,String desc);}
    private static String operation(String owner,String name,String desc,Evidence e) {
        if(!e.subtype(owner,ROOT))return null;
        String canonical=name.startsWith("m_")?e.method(ROOT,name,desc):name;
        return "render".equals(canonical)&&desc.equals(RENDER)?"render":
                "getTextureLocation".equals(canonical)&&desc.equals(TEXTURE)?"texture":
                "renderNameTag".equals(canonical)&&desc.equals(NAME_TAG)?"nameTag":null;
    }
    private static String alias(String operation){return switch(operation){case "render"->"lmsLegacyRender";case "texture"->"lmsLegacyTexture";case "nameTag"->"lmsLegacyNameTag";default->throw new IllegalArgumentException(operation);};}
    static int rewrite(ClassNode node,Evidence e,String adapter,SemanticInvocationRewriter calls) {
        int count=0;
        var suppressedNames=emptyNameTagCallbacks(node,e);
        if(e.subtype(node.name,ROOT)&&e.standalone())for(var method:node.methods) {
            if((method.access&ACC_STATIC)!=0)continue;
            String operation=operation(node.name,method.name,method.desc,e);
            if(operation!=null) {
                if(operation.equals("nameTag")&&!suppressedNames.contains(method))
                    throw new UnsupportedOperationException("Nonempty source name-tag callback requires its own text-render contract: "+node.name+"#"+method.name);
                String alias=alias(operation);
                if(node.methods.stream().anyMatch(m->m!=method&&m.name.equals(alias)&&m.desc.equals(method.desc)))throw new IllegalStateException("Source entity renderer alias collision: "+node.name+"#"+alias);
                method.name=alias;count++;
            } else {
                String canonical=method.name.startsWith("m_")?e.method(ROOT,method.name,method.desc):method.name;
                if(Set.of("getRenderOffset","shouldRender","shouldShowName","renderNameTag").contains(canonical==null?"":canonical)&&!suppressedNames.contains(method))
                    throw new UnsupportedOperationException("Changed source entity renderer override requires its own contract: "+node.name+"#"+method.name+method.desc);
            }
        }
        count+=calls.rewrite(node,(opcode,owner,name,desc)->{
            String op=operation(owner,name,desc,e);if(op==null)return null;
            if(opcode==INVOKESPECIAL&&!owner.equals(ROOT))return null;
            if(opcode==INVOKESPECIAL&&op.equals("texture"))throw new UnsupportedOperationException("Source abstract EntityRenderer texture super call");
            return new Handle(H_INVOKESTATIC,adapter,opcode==INVOKESPECIAL?(op.equals("nameTag")?"baseNameTag":"baseRender"):op,"(L"+ROOT+";"+desc.substring(1),false);
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
    /** Only an actual empty override, or its exact javac erased bridge, proves suppression. */
    private static Set<MethodNode> emptyNameTagCallbacks(ClassNode node,Evidence e) {
        Set<MethodNode> result=new HashSet<>();
        for(var method:node.methods)if("nameTag".equals(operation(node.name,method.name,method.desc,e))) {
            var code=new ArrayList<AbstractInsnNode>();for(var i:method.instructions)if(i.getOpcode()>=0)code.add(i);
            if(code.size()==1&&code.getFirst().getOpcode()==RETURN){result.add(method);continue;}
            if((method.access&(ACC_BRIDGE|ACC_SYNTHETIC))!=(ACC_BRIDGE|ACC_SYNTHETIC)||code.size()!=9)continue;
            int[] locals={0,1,2,3,4,5};int at=0;boolean valid=true;
            for(int i=0;i<locals.length;i++) {
                if(!(code.get(at++) instanceof VarInsnNode load)||load.var!=locals[i]||load.getOpcode()!=(i==5?ILOAD:ALOAD)){valid=false;break;}
                if(i==1){if(!(code.get(at++) instanceof TypeInsnNode cast)||cast.getOpcode()!=CHECKCAST||!e.subtype(cast.desc,"net/minecraft/world/entity/Entity")){valid=false;break;}}
            }
            if(!valid||!(code.get(7) instanceof MethodInsnNode call)||call.getOpcode()!=INVOKEVIRTUAL||!call.owner.equals(node.name)||code.get(8).getOpcode()!=RETURN)continue;
            var cast=(TypeInsnNode)code.get(2);
            if(!call.desc.equals(NAME_TAG.replace(ENTITY,"L"+cast.desc+";")))continue;
            for(var target:node.methods)if(target.name.equals(call.name)&&target.desc.equals(call.desc)&&(target.access&(ACC_STATIC|ACC_ABSTRACT|ACC_NATIVE))==0) {
                var body=new ArrayList<AbstractInsnNode>();for(var i:target.instructions)if(i.getOpcode()>=0)body.add(i);
                if(body.size()==1&&body.getFirst().getOpcode()==RETURN){result.add(method);result.add(target);}
            }
        }
        return result;
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
