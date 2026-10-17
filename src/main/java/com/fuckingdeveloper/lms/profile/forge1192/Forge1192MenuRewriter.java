package com.fuckingdeveloper.lms.profile.forge1192;

import com.fuckingdeveloper.lms.transform.MethodRuleCatalog;
import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Exact source menu members; facade projection preserves SAM, erased returns and interface identity. */
final class Forge1192MenuRewriter {
    private static final String FACTORY="net/minecraftforge/network/IContainerFactory";
    private static final String MENU="net/minecraftforge/common/extensions/IForgeMenuType";
    private static final Map<String,String> TYPES=Map.of(
            FACTORY,"com/fuckingdeveloper/lms/profile/forge1192/runtime/menu/Forge1192ContainerFactory",
            MENU,"com/fuckingdeveloper/lms/profile/forge1192/runtime/menu/Forge1192MenuType");
    static String relocation(String owner){return TYPES.get(owner);}
    static String descriptor(String desc){for(var e:TYPES.entrySet())desc=desc.replace("L"+e.getKey()+";","L"+e.getValue()+";").replace("L"+e.getKey()+"<","L"+e.getValue()+"<");return desc;}
    static List<MethodRuleCatalog.Rule> entries() {
        var result=new ArrayList<MethodRuleCatalog.Rule>();
        for(String owner:TYPES.keySet()) {
            var node=new ClassNode();new ClassReader(Forge1192SourceHierarchyIndex.methodBytes(owner)).accept(node,ClassReader.SKIP_CODE|ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
            for(var method:node.methods) {
                if(!method.name.equals("create")||(method.access&Opcodes.ACC_PUBLIC)==0)continue;
                boolean isStatic=(method.access&Opcodes.ACC_STATIC)!=0;
                var bridge=new Handle(isStatic?Opcodes.H_INVOKESTATIC:Opcodes.H_INVOKEINTERFACE,relocation(owner),method.name,descriptor(method.desc),true);
                result.add(new MethodRuleCatalog.Rule("source-menu-"+owner+"#"+method.name+method.desc,"MENU_FACTORY",
                        new MethodRuleCatalog.Member(owner,method.name,method.desc),isStatic?Opcodes.INVOKESTATIC:Opcodes.INVOKEINTERFACE,bridge,
                        "Forge43.4.0 IForgeMenuType/IContainerFactory bytecode; NeoForge26.3 native factory/extra-data forwarding and MenuType mixin"));
            }
        }
        return List.copyOf(result);
    }
    static int rewrite(ClassNode node,Forge1192MethodDeclarations declarations) {
        int count=0;
        for(var method:node.methods)for(var insn:method.instructions) {
            if(insn instanceof MethodInsnNode call) {
                var next=handle(new Handle(call.getOpcode()==Opcodes.INVOKESTATIC?Opcodes.H_INVOKESTATIC:call.getOpcode()==Opcodes.INVOKEINTERFACE?Opcodes.H_INVOKEINTERFACE:Opcodes.H_INVOKEVIRTUAL,call.owner,call.name,call.desc,call.itf),declarations);
                if(next!=null){call.owner=next.getOwner();call.desc=next.getDesc();call.itf=true;count++;}
            }else if(insn instanceof LdcInsnNode ldc){Object next=constant(ldc.cst,declarations);if(next!=ldc.cst){ldc.cst=next;count++;}}
            else if(insn instanceof InvokeDynamicInsnNode indy)for(int i=0;i<indy.bsmArgs.length;i++){Object next=constant(indy.bsmArgs[i],declarations);if(next!=indy.bsmArgs[i]){indy.bsmArgs[i]=next;count++;}}
        }
        return count;
    }
    private static Handle handle(Handle source,Forge1192MethodDeclarations declarations) {
        if(!TYPES.containsKey(source.getOwner()))return null;
        int opcode=switch(source.getTag()){case Opcodes.H_INVOKESTATIC->Opcodes.INVOKESTATIC;case Opcodes.H_INVOKEINTERFACE->Opcodes.INVOKEINTERFACE;case Opcodes.H_INVOKEVIRTUAL->Opcodes.INVOKEVIRTUAL;default->-1;};
        var rule=Forge1192MethodRules.catalog().resolve(opcode,source.getOwner(),source.getName(),source.getDesc(),declarations::owner);
        if(rule.isPresent()&&rule.get().family().equals("MENU_FACTORY"))return rule.get().bridge();
        throw new IllegalStateException("Unadapted source menu operation: "+source);
    }
    private static Object constant(Object value,Forge1192MethodDeclarations declarations) {
        if(value instanceof Handle source){var next=handle(source,declarations);return next==null?value:next;}
        if(value instanceof ConstantDynamic dynamic){Object[] args=new Object[dynamic.getBootstrapMethodArgumentCount()];boolean changed=false;for(int i=0;i<args.length;i++){Object old=dynamic.getBootstrapMethodArgument(i);args[i]=constant(old,declarations);changed|=args[i]!=old;}return changed?new ConstantDynamic(dynamic.getName(),dynamic.getDescriptor(),dynamic.getBootstrapMethod(),args):value;}
        return value;
    }
}
