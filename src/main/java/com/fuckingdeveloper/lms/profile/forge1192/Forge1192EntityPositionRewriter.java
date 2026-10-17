package com.fuckingdeveloper.lms.profile.forge1192;

import java.util.*;
import java.util.function.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import static org.objectweb.asm.Opcodes.*;

/** Exact source moveTo snap operations, preserving source overrides and native virtual/super dispatch. */
final class Forge1192EntityPositionRewriter {
    private static final String ROOT="net/minecraft/world/entity/Entity";
    private static final Set<String> DESCRIPTORS=Set.of("(DDDFF)V","(DDD)V","(Lnet/minecraft/world/phys/Vec3;)V","(Lnet/minecraft/core/BlockPos;FF)V");
    private static boolean member(String owner,String name,String desc,BiPredicate<String,String> subtype,BiFunction<String,String,String> canonical) {
        return DESCRIPTORS.contains(desc)&&subtype.test(owner,ROOT)&&"moveTo".equals(name.startsWith("m_")?canonical.apply(name,desc):name);
    }
    static int rewrite(ClassNode node,BiPredicate<String,String> subtype,BiFunction<String,String,String> canonical,String target) {
        int count=0;
        for(var method:node.methods)if((method.access&(ACC_STATIC|ACC_PRIVATE))==0&&member(node.name,method.name,method.desc,subtype,canonical)) {
            if(node.methods.stream().anyMatch(m->m!=method&&m.name.equals(target)&&m.desc.equals(method.desc)))throw new IllegalStateException("Source entity position override collision: "+node.name+method.desc);
            method.name=target;count++;
        }
        for(var method:node.methods)for(var insn:method.instructions) {
            if(insn instanceof MethodInsnNode call&&call.getOpcode()!=INVOKESTATIC&&member(call.owner,call.name,call.desc,subtype,canonical)) {call.name=target;count++;}
            if(insn instanceof LdcInsnNode ldc){Object next=constant(ldc.cst,subtype,canonical,target);if(next!=ldc.cst){ldc.cst=next;count++;}}
            if(insn instanceof InvokeDynamicInsnNode indy)for(int i=0;i<indy.bsmArgs.length;i++){Object next=constant(indy.bsmArgs[i],subtype,canonical,target);if(next!=indy.bsmArgs[i]){indy.bsmArgs[i]=next;count++;}}
        }
        return count;
    }
    private static Object constant(Object value,BiPredicate<String,String> subtype,BiFunction<String,String,String> canonical,String target) {
        if(value instanceof Handle h&&Set.of(H_INVOKEVIRTUAL,H_INVOKESPECIAL,H_INVOKEINTERFACE).contains(h.getTag())&&member(h.getOwner(),h.getName(),h.getDesc(),subtype,canonical))
            return new Handle(h.getTag(),h.getOwner(),target,h.getDesc(),h.isInterface());
        if(value instanceof ConstantDynamic dynamic){Object[] args=new Object[dynamic.getBootstrapMethodArgumentCount()];boolean changed=false;for(int i=0;i<args.length;i++){Object old=dynamic.getBootstrapMethodArgument(i);args[i]=constant(old,subtype,canonical,target);changed|=old!=args[i];}if(changed)return new ConstantDynamic(dynamic.getName(),dynamic.getDescriptor(),dynamic.getBootstrapMethod(),args);}
        return value;
    }
}
