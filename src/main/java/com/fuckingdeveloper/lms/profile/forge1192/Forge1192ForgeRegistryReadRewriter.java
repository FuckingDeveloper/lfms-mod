package com.fuckingdeveloper.lms.profile.forge1192;

import java.util.*;
import java.util.function.Predicate;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import static org.objectweb.asm.Opcodes.*;

/** Admit the proven source read ABI before central stored/callable type projection. */
final class Forge1192ForgeRegistryReadRewriter {
    static final String MANAGER="net/minecraftforge/registries/RegistryManager", REGISTRY="net/minecraftforge/registries/ForgeRegistry";
    private static final String API="net/minecraftforge/registries/IForgeRegistry", ID="Lnet/minecraft/resources/ResourceLocation;", KEY="Lnet/minecraft/resources/ResourceKey;", OBJ="Ljava/lang/Object;";
    private static final String BRIDGE="com/fuckingdeveloper/lms/profile/forge1192/runtime/registry/Forge1192RegistryReadBridge";
    private static final Set<String> READS=Set.of("getRegistryName()"+ID,"getRegistryKey()"+KEY,"getDefaultKey()"+ID,
            "getValue("+ID+")"+OBJ,"getRaw("+ID+")"+OBJ,"tags()Lnet/minecraftforge/registries/tags/ITagManager;","getValue(I)"+OBJ,"getKey("+OBJ+")"+ID,"getKey(I)"+KEY,
            "getResourceKey("+OBJ+")Ljava/util/Optional;","getID("+OBJ+")I","getID("+ID+")I",
            "containsKey("+ID+")Z","containsValue("+OBJ+")Z","isEmpty()Z","iterator()Ljava/util/Iterator;",
            "spliterator()Ljava/util/Spliterator;","forEach(Ljava/util/function/Consumer;)V",
            "getKeys()Ljava/util/Set;","getValues()Ljava/util/Collection;","getEntries()Ljava/util/Set;");
    private static final Set<String> INTERFACE_READS=Set.of("getRegistryName()"+ID,"getRegistryKey()"+KEY,"getDefaultKey()"+ID,
            "getResourceKey("+OBJ+")Ljava/util/Optional;","containsValue("+OBJ+")Z","isEmpty()Z",
            "getKeys()Ljava/util/Set;","getValues()Ljava/util/Collection;","getEntries()Ljava/util/Set;");
    private static final Set<String> IDENTITY=Set.of("getClass()Ljava/lang/Class;","equals("+OBJ+")Z","hashCode()I","toString()Ljava/lang/String;");
    static int rewrite(ClassNode node, Predicate<String> registrySubtype, Predicate<String> managerSubtype) {
        if (registrySubtype.test(node.name) || managerSubtype.test(node.name)) throw unsupported(node,"source registry/manager subclass");
        int count=0;
        for(var method:node.methods) for(var insn:method.instructions.toArray()) {
            if(insn instanceof FieldInsnNode f) field(node,f.owner,f.name,f.desc,f.getOpcode());
            if(insn instanceof MethodInsnNode call) {
                member(node,call.owner,call.name,call.desc,call.getOpcode());
                if(call.owner.equals(API)&&INTERFACE_READS.contains(call.name+call.desc)) {
                    if(call.getOpcode()!=INVOKEINTERFACE)throw unsupported(node,"interface read invocation kind");
                    call.setOpcode(INVOKESTATIC);call.owner=BRIDGE;call.desc="("+OBJ+call.desc.substring(1);call.itf=false;count++;
                }
            }
            if(insn instanceof TypeInsnNode t && t.getOpcode()==NEW && (t.desc.equals(MANAGER)||t.desc.equals(REGISTRY)))throw unsupported(node,"construction");
            if(insn instanceof LdcInsnNode ldc) {Object c=constant(node,ldc.cst);if(!Objects.equals(c,ldc.cst)){ldc.cst=c;count++;}}
            if(insn instanceof InvokeDynamicInsnNode indy) {
                indy.bsm=(Handle)constant(node,indy.bsm);
                for(int i=0;i<indy.bsmArgs.length;i++){Object c=constant(node,indy.bsmArgs[i]);if(!Objects.equals(c,indy.bsmArgs[i])){indy.bsmArgs[i]=c;count++;}}
            }
        }
        return count;
    }
    private static void field(ClassNode node,String owner,String name,String desc,int opcode) {
        if(owner.equals(MANAGER)) {
            if(opcode!=GETSTATIC||!name.equals("ACTIVE")||!desc.equals("L"+MANAGER+";"))throw unsupported(node,"manager field "+name);
        } else if(owner.equals(REGISTRY))throw unsupported(node,"registry internal field "+name);
    }
    private static void member(ClassNode node,String owner,String name,String desc,int opcode) {
        if(!owner.equals(MANAGER)&&!owner.equals(REGISTRY))return;
        boolean virtual=opcode==INVOKEVIRTUAL;
        boolean allowed=IDENTITY.contains(name+desc);
        if(owner.equals(REGISTRY))allowed|=READS.contains(name+desc);
        else allowed|=Set.of("getName()Ljava/lang/String;","getName(L"+API+";)"+ID,
                "getRegistry("+ID+")L"+REGISTRY+";","getRegistry("+KEY+")L"+REGISTRY+";").contains(name+desc);
        if(!virtual||!allowed)throw unsupported(node,"member "+owner+"#"+name+desc);
    }
    private static Object constant(ClassNode node,Object value) {
        if(value instanceof Handle h) {
            int op=switch(h.getTag()){case H_GETSTATIC->GETSTATIC;case H_PUTSTATIC->PUTSTATIC;case H_GETFIELD->GETFIELD;case H_PUTFIELD->PUTFIELD;case H_INVOKEVIRTUAL->INVOKEVIRTUAL;default->-1;};
            if(h.getTag()<=H_PUTSTATIC)field(node,h.getOwner(),h.getName(),h.getDesc(),op);else member(node,h.getOwner(),h.getName(),h.getDesc(),op);
            if(h.getOwner().equals(API)&&INTERFACE_READS.contains(h.getName()+h.getDesc())) {
                if(h.getTag()!=H_INVOKEINTERFACE)throw unsupported(node,"interface read handle kind");
                return new Handle(H_INVOKESTATIC,BRIDGE,h.getName(),"("+OBJ+h.getDesc().substring(1),false);
            }
        } else if(value instanceof ConstantDynamic d) {
            Object[] args=new Object[d.getBootstrapMethodArgumentCount()];for(int i=0;i<args.length;i++)args[i]=constant(node,d.getBootstrapMethodArgument(i));
            return new ConstantDynamic(d.getName(),d.getDescriptor(),(Handle)constant(node,d.getBootstrapMethod()),args);
        }
        return value;
    }
    private static UnsupportedOperationException unsupported(ClassNode node,String reason){return new UnsupportedOperationException("Source Forge registry read boundary: "+node.name+": "+reason);}
}
