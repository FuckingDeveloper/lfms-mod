package com.fuckingdeveloper.lms.profile.forge1192;

import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import com.fuckingdeveloper.lms.transform.SemanticInvocationRewriter;

/** Source-root membership and mapped declarations select adapters; a moved class name alone is insufficient. */
final class Forge1192ProjectileRewriter {
    static final String SOURCE="net/minecraft/world/entity/projectile/ThrowableItemProjectile";
    private static final String THROWABLE="net/minecraft/world/entity/projectile/ThrowableProjectile",PROJECTILE="net/minecraft/world/entity/projectile/Projectile";
    interface Evidence {
        boolean subtype(String owner,String base);
        String canonical(String owner,String name,String descriptor);
    }
    static String target(){return com.fuckingdeveloper.lms.target.InstalledTargetAdapters.throwableItemBase();}
    static String adapter(){return com.fuckingdeveloper.lms.target.InstalledTargetAdapters.throwableItemAdapter();}
    private static String canonical(Evidence evidence,String name,String desc) {
        if(!name.startsWith("m_"))return name;
        for(String owner:List.of(SOURCE,THROWABLE,PROJECTILE,"net/minecraft/world/entity/Entity")) {String mapped=evidence.canonical(owner,name,desc);if(mapped!=null)return mapped;}
        return null;
    }
    private static String alias(String name,String desc) {
        if(name==null)return null;
        if("getGravity".equals(name)&&desc.equals("()F"))return "lms$sourceGravity";
        if(Set.of("getDefaultItem","getItem","getItemRaw").contains(name)&&desc.equals(name.equals("getDefaultItem")?"()Lnet/minecraft/world/item/Item;":"()Lnet/minecraft/world/item/ItemStack;"))return name;
        if("setItem".equals(name)&&desc.equals("(Lnet/minecraft/world/item/ItemStack;)V"))return name;
        if("onHit".equals(name)&&desc.equals("(Lnet/minecraft/world/phys/HitResult;)V"))return name;
        return null;
    }
    static int rewrite(ClassNode node,Evidence evidence,SemanticInvocationRewriter invocations) {
        int count=0;boolean source=evidence.subtype(node.name,SOURCE);
        if(source)for(var method:node.methods) {
            String name=canonical(evidence,method.name,method.desc);
            if(("defineSynchedData".equals(name)&&method.desc.equals("()V"))
                    || Set.of("addAdditionalSaveData","readAdditionalSaveData").contains(name==null?"":name)&&method.desc.equals("(Lnet/minecraft/nbt/CompoundTag;)V"))
                throw new UnsupportedOperationException("Source custom projectile entity-data callbacks require the entity-data adapter: "+node.name+"#"+method.name+method.desc);
            if((method.access&Opcodes.ACC_STATIC)!=0)continue;
            String alias=alias(name,method.desc);
            if(alias!=null&&!method.name.equals(alias)){method.name=alias;count++;}
        }
        for(var method:node.methods)for(var insn:method.instructions) {
            if(insn instanceof LdcInsnNode ldc){Object next=constant(ldc.cst,evidence);if(next!=ldc.cst){ldc.cst=next;count++;}continue;}
            if(insn instanceof InvokeDynamicInsnNode indy){for(int i=0;i<indy.bsmArgs.length;i++){Object next=constant(indy.bsmArgs[i],evidence);if(next!=indy.bsmArgs[i]){indy.bsmArgs[i]=next;count++;}}continue;}
            if(!(insn instanceof MethodInsnNode call))continue;
            if(call.getOpcode()==Opcodes.INVOKESPECIAL&&call.owner.equals(SOURCE)&&call.name.equals("<init>")){call.owner=adapter();count++;continue;}
            if(!evidence.subtype(call.owner,SOURCE))continue;
            String name=canonical(evidence,call.name,call.desc),alias=alias(name,call.desc);
            if(alias==null)continue;
            if(call.getOpcode()==Opcodes.INVOKESPECIAL&&call.owner.equals(SOURCE))call.owner=adapter();
            if(name.equals("getItemRaw")&&call.getOpcode()!=Opcodes.INVOKESPECIAL || name.equals("getGravity")&&call.getOpcode()!=Opcodes.INVOKESPECIAL)continue;
            call.name=alias;count++;
        }
        count+=invocations.rewrite(node,(opcode,owner,name,desc)->{
            if(!evidence.subtype(owner,SOURCE))return null;
            String canonical=canonical(evidence,name,desc);
            String method="getItemRaw".equals(canonical)&&desc.equals("()Lnet/minecraft/world/item/ItemStack;")?"raw":
                    "getGravity".equals(canonical)&&desc.equals("()F")?"gravity":null;
            if(method==null||opcode==Opcodes.INVOKESPECIAL)return null;
            return new Handle(Opcodes.H_INVOKESTATIC,adapter(),method,"(L"+SOURCE+";"+desc.substring(1),false);
        });
        return count;
    }
    private static Object constant(Object value,Evidence evidence) {
        if(value instanceof Handle handle) {
            if(!evidence.subtype(handle.getOwner(),SOURCE))return value;
            String name=canonical(evidence,handle.getName(),handle.getDesc()),alias=alias(name,handle.getDesc());
            if(alias==null)return value;
            if(("getItemRaw".equals(name)||"getGravity".equals(name))&&handle.getTag()!=Opcodes.H_INVOKESPECIAL)return value;
            String owner=handle.getTag()==Opcodes.H_INVOKESPECIAL&&handle.getOwner().equals(SOURCE)?adapter():handle.getOwner();
            return new Handle(handle.getTag(),owner,alias,handle.getDesc(),handle.isInterface());
        }
        if(value instanceof ConstantDynamic dynamic){Object[] args=new Object[dynamic.getBootstrapMethodArgumentCount()];boolean changed=false;for(int i=0;i<args.length;i++){Object old=dynamic.getBootstrapMethodArgument(i);args[i]=constant(old,evidence);changed|=args[i]!=old;}return changed?new ConstantDynamic(dynamic.getName(),dynamic.getDescriptor(),dynamic.getBootstrapMethod(),args):value;}
        return value;
    }
    static int construction(ClassNode node){
        if(!target().equals(node.superName))return 0;
        node.superName=adapter();
        if(node.signature!=null) {
            var writer=new org.objectweb.asm.signature.SignatureWriter(){
                @Override public org.objectweb.asm.signature.SignatureVisitor visitSuperclass(){
                    var parent=super.visitSuperclass();
                    return new org.objectweb.asm.signature.SignatureVisitor(Opcodes.ASM9){
                        @Override public void visitClassType(String name){parent.visitClassType(name.equals(target())?adapter():name);}
                        @Override public void visitInnerClassType(String name){parent.visitInnerClassType(name);}
                        @Override public void visitTypeArgument(){parent.visitTypeArgument();}
                        @Override public org.objectweb.asm.signature.SignatureVisitor visitTypeArgument(char wildcard){return parent.visitTypeArgument(wildcard);}
                        @Override public void visitEnd(){parent.visitEnd();}
                    };
                }
            };
            new org.objectweb.asm.signature.SignatureReader(node.signature).accept(writer);
            node.signature=writer.toString();
        }
        return 1;
    }
}
