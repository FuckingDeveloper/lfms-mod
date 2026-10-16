package com.fuckingdeveloper.lms.profile.forge1192;

import java.util.*;
import java.util.function.Predicate;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Verified removed source rule operations; native rule registry/world state remain authoritative. */
final class Forge1192GameRulesRewriter {
    private static final String ROOT="net/minecraft/world/level/GameRules", NATIVE="net/minecraft/world/level/gamerules/GameRules",
            VIEW="com/fuckingdeveloper/lms/profile/forge1192/runtime/world/Forge1192GameRuleViews";
    private static final Set<String> NESTED=Set.of("Key","Type","Value","BooleanValue","IntegerValue","Category","GameRuleTypeVisitor");
    interface Names {String method(String owner,String name,String desc);String field(String owner,String name,String desc);}
    static String relocation(String owner){
        if(!owner.startsWith(ROOT+"$"))return null;String nested=owner.substring(ROOT.length()+1);
        return NESTED.contains(nested)?VIEW+"$"+(nested.equals("GameRuleTypeVisitor")?"Visitor":nested):null;
    }
    static String descriptor(String desc){
        for(String nested:NESTED){String from=ROOT+"$"+nested,to=relocation(from);desc=desc.replace("L"+from+";","L"+to+";").replace("L"+from+"<","L"+to+"<");}
        return desc;
    }
    static int rewrite(ClassNode node,Predicate<String> visitor,Names names){
        int changed=0;
        boolean ownVisitor=visitor.test(node.name);
        for(var method:node.methods) {
            if(ownVisitor&&method.desc.equals("(L"+ROOT+"$Key;L"+ROOT+"$Type;)V")) {
                String canonical=names.method(ROOT+"$GameRuleTypeVisitor",method.name,method.desc);
                if(canonical!=null&&Set.of("visit","visitBoolean","visitInteger").contains(canonical)){method.name=canonical;changed++;}
            }
            AbstractInsnNode[] snapshot;
            try {snapshot=method.instructions.toArray();}
            catch(RuntimeException failure){throw new IllegalStateException("Invalid preceding ASM instruction list: "+node.name+"#"+method.name+method.desc,failure);}
            for(var instruction:snapshot) {
                if(instruction instanceof MethodInsnNode call) {
                    if(call.owner.equals(ROOT)&&call.name.equals("<init>")) {
                        if(call.desc.equals("()V")) {
                            method.instructions.insertBefore(call,new FieldInsnNode(Opcodes.GETSTATIC,"net/minecraft/world/flag/FeatureFlags","VANILLA_SET","Lnet/minecraft/world/flag/FeatureFlagSet;"));
                            call.desc="(Lnet/minecraft/world/flag/FeatureFlagSet;)V";changed++;
                        }else if(call.desc.equals("(Lcom/mojang/serialization/DynamicLike;)V")) {
                            method.instructions.insertBefore(call,new MethodInsnNode(Opcodes.INVOKESTATIC,VIEW,"mapFromDynamic","(Lcom/mojang/serialization/DynamicLike;)Lnet/minecraft/world/level/gamerules/GameRuleMap;",false));
                            method.instructions.insertBefore(call,new FieldInsnNode(Opcodes.GETSTATIC,"net/minecraft/world/flag/FeatureFlags","VANILLA_SET","Lnet/minecraft/world/flag/FeatureFlagSet;"));
                            method.instructions.insertBefore(call,new InsnNode(Opcodes.SWAP));call.desc="(Lnet/minecraft/world/flag/FeatureFlagSet;Lnet/minecraft/world/level/gamerules/GameRuleMap;)V";changed++;
                        }
                        continue;
                    }
                    int kind=switch(call.getOpcode()){case Opcodes.INVOKESTATIC->Opcodes.H_INVOKESTATIC;case Opcodes.INVOKEVIRTUAL->Opcodes.H_INVOKEVIRTUAL;case Opcodes.INVOKEINTERFACE->Opcodes.H_INVOKEINTERFACE;default->-1;};if(kind<0)continue;
                    var original=new Handle(kind,call.owner,call.name,call.desc,call.itf);var target=handle(original,visitor,names);
                    if(target.equals(original))continue;call.owner=target.getOwner();call.name=target.getName();call.desc=target.getDesc();call.itf=target.isInterface();
                    if(target.getTag()==Opcodes.H_INVOKESTATIC)call.setOpcode(Opcodes.INVOKESTATIC);changed++;
                }else if(instruction instanceof FieldInsnNode field&&relocation(field.owner)!=null){String canonical=names.field(field.owner,field.name,field.desc);if(canonical!=null&&!canonical.equals(field.name)){field.name=canonical;changed++;}}
                else if(instruction instanceof LdcInsnNode ldc){Object next=constant(ldc.cst,visitor,names);if(next!=ldc.cst){ldc.cst=next;changed++;}}
                else if(instruction instanceof InvokeDynamicInsnNode indy)for(int i=0;i<indy.bsmArgs.length;i++){Object next=constant(indy.bsmArgs[i],visitor,names);if(next!=indy.bsmArgs[i]){indy.bsmArgs[i]=next;changed++;}}
            }
        }
        return changed;
    }
    private static Handle handle(Handle source,Predicate<String> visitor,Names names){
        if(source.getTag()==Opcodes.H_INVOKEVIRTUAL&&source.getDesc().equals("()L"+ROOT+";")
                &&Set.of("net/minecraft/server/MinecraftServer","net/minecraft/server/level/ServerLevel").contains(source.getOwner())
                &&"getGameRules".equals(names.method(source.getOwner(),source.getName(),source.getDesc())))
            return new Handle(source.getTag(),source.getOwner(),"getGameRules",source.getDesc(),source.isInterface());
        if(source.getOwner().equals(ROOT)) {
            if(source.getTag()==Opcodes.H_NEWINVOKESPECIAL) {
                String method=source.getDesc().equals("()V")?"newRules":source.getDesc().equals("(Lcom/mojang/serialization/DynamicLike;)V")?"fromDynamic":null;
                return method==null?source:new Handle(Opcodes.H_INVOKESTATIC,VIEW,method,source.getDesc().replace(")V",")L"+NATIVE+";"),false);
            }
            String name=names.method(ROOT,source.getName(),source.getDesc());if(name==null)return source;
            var supported=Map.of("visitGameRuleTypes","(L"+ROOT+"$GameRuleTypeVisitor;)V","getRule","(L"+ROOT+"$Key;)L"+ROOT+"$Value;",
                    "getBoolean","(L"+ROOT+"$Key;)Z","getInt","(L"+ROOT+"$Key;)I","copy","()L"+ROOT+";",
                    "assignFrom","(L"+ROOT+";Lnet/minecraft/server/MinecraftServer;)V","createTag","()Lnet/minecraft/nbt/CompoundTag;");
            if(!source.getDesc().equals(supported.get(name)))return source;
            boolean staticCall=name.equals("visitGameRuleTypes");
            if(staticCall?source.getTag()!=Opcodes.H_INVOKESTATIC:source.getTag()!=Opcodes.H_INVOKEVIRTUAL)return source;
            String desc=staticCall?source.getDesc():"(L"+ROOT+";"+source.getDesc().substring(1);
            return new Handle(Opcodes.H_INVOKESTATIC,VIEW,name,desc,false);
        }
        if(relocation(source.getOwner())!=null||visitor.test(source.getOwner())) {
            int tag=source.getTag();
            if(tag==Opcodes.H_GETSTATIC||tag==Opcodes.H_GETFIELD||tag==Opcodes.H_PUTSTATIC||tag==Opcodes.H_PUTFIELD) {
                String name=names.field(source.getOwner(),source.getName(),source.getDesc());return name==null?source:new Handle(tag,source.getOwner(),name,source.getDesc(),source.isInterface());
            }
            if(tag!=Opcodes.H_INVOKEVIRTUAL&&tag!=Opcodes.H_INVOKEINTERFACE&&tag!=Opcodes.H_INVOKESTATIC)return source;
            String owner=relocation(source.getOwner())!=null?source.getOwner():ROOT+"$GameRuleTypeVisitor";
            String name=names.method(owner,source.getName(),source.getDesc());return name==null?source:new Handle(tag,source.getOwner(),name,source.getDesc(),source.isInterface());
        }
        return source;
    }
    private static Object constant(Object value,Predicate<String> visitor,Names names){
        if(value instanceof Handle h)return handle(h,visitor,names);
        if(value instanceof ConstantDynamic dynamic){Object[] args=new Object[dynamic.getBootstrapMethodArgumentCount()];boolean changed=false;for(int i=0;i<args.length;i++){Object old=dynamic.getBootstrapMethodArgument(i);args[i]=constant(old,visitor,names);changed|=args[i]!=old;}return changed?new ConstantDynamic(dynamic.getName(),dynamic.getDescriptor(),dynamic.getBootstrapMethod(),args):value;}
        return value;
    }
}
