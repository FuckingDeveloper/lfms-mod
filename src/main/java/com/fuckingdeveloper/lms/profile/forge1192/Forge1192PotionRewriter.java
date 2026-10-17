package com.fuckingdeveloper.lms.profile.forge1192;

import com.fuckingdeveloper.lms.transform.SemanticInvocationRewriter;
import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import static org.objectweb.asm.Opcodes.*;

/** Verified source constants and typed potion-item members, with one callable traversal. */
final class Forge1192PotionRewriter {
    interface Evidence { String field(String name); String method(String owner,String name,String desc); boolean targetField(String name); }
    private static final String P="net/minecraft/world/item/alchemy/Potion", CONSTANTS=P+"s", UTILS=P+"Utils";
    private static final String PD="L"+P+";", STACK="Lnet/minecraft/world/item/ItemStack;", TAG="Lnet/minecraft/nbt/CompoundTag;", LIST="Ljava/util/List;", COLLECTION="Ljava/util/Collection;";
    // Complete pinned source Potions declarations; target-only fields are not source members.
    private static final Set<String> SOURCE_CONSTANTS=Set.of("EMPTY","WATER","MUNDANE","THICK","AWKWARD","NIGHT_VISION","LONG_NIGHT_VISION","INVISIBILITY","LONG_INVISIBILITY","LEAPING","LONG_LEAPING","STRONG_LEAPING","FIRE_RESISTANCE","LONG_FIRE_RESISTANCE","SWIFTNESS","LONG_SWIFTNESS","STRONG_SWIFTNESS","SLOWNESS","LONG_SLOWNESS","STRONG_SLOWNESS","TURTLE_MASTER","LONG_TURTLE_MASTER","STRONG_TURTLE_MASTER","WATER_BREATHING","LONG_WATER_BREATHING","HEALING","STRONG_HEALING","HARMING","STRONG_HARMING","POISON","LONG_POISON","STRONG_POISON","REGENERATION","LONG_REGENERATION","STRONG_REGENERATION","STRENGTH","LONG_STRENGTH","STRONG_STRENGTH","WEAKNESS","LONG_WEAKNESS","LUCK","SLOW_FALLING","LONG_SLOW_FALLING");
    private static final Set<String> MEMBERS=Set.of(
            "getMobEffects("+STACK+")"+LIST,"getCustomEffects("+STACK+")"+LIST,
            "getAllEffects("+PD+COLLECTION+")"+LIST,
            "getPotion("+STACK+")"+PD,"getPotion("+TAG+")"+PD,
            "setPotion("+STACK+PD+")"+STACK,"setCustomEffects("+STACK+COLLECTION+")"+STACK,
            "getColor("+STACK+")I","getColor("+PD+")I","getColor("+COLLECTION+")I");
    static int rewrite(ClassNode node,Evidence evidence,SemanticInvocationRewriter calls) {
        if(P.equals(node.superName))throw new UnsupportedOperationException("Source custom Potion subclasses require a separate adapter");
        String bridge=com.fuckingdeveloper.lms.target.InstalledTargetAdapters.potionContentsBridge();
        var getters=new LinkedHashMap<String,MethodNode>();int count=0;
        for(var method:List.copyOf(node.methods))for(var instruction:method.instructions.toArray()) {
            if(instruction instanceof TypeInsnNode type&&type.getOpcode()==NEW&&type.desc.equals(P))
                throw new UnsupportedOperationException("Source Potion construction is outside typed contents adaptation");
            if(instruction instanceof FieldInsnNode field&&field.owner.equals(CONSTANTS)&&field.desc.equals(PD)) {
                if(field.getOpcode()!=GETSTATIC)throw new UnsupportedOperationException("Source potion constant mutation");
                String name=constantName(field.name,evidence);
                method.instructions.set(field,new MethodInsnNode(INVOKESTATIC,node.name,getter(node,getters,name,bridge).name,"()"+PD,(node.access&ACC_INTERFACE)!=0));count++;
            } else if(instruction instanceof LdcInsnNode ldc) {
                Object value=constant(ldc.cst,node,getters,evidence,bridge);if(!Objects.equals(value,ldc.cst)){ldc.cst=value;count++;}
            } else if(instruction instanceof InvokeDynamicInsnNode indy) {
                Handle bootstrap=(Handle)constant(indy.bsm,node,getters,evidence,bridge);if(!bootstrap.equals(indy.bsm)){indy.bsm=bootstrap;count++;}
                for(int i=0;i<indy.bsmArgs.length;i++){Object value=constant(indy.bsmArgs[i],node,getters,evidence,bridge);if(!Objects.equals(value,indy.bsmArgs[i])){indy.bsmArgs[i]=value;count++;}}
            }
        }
        node.methods.addAll(getters.values());
        return count+calls.rewrite(node,(opcode,owner,name,desc)->{
            String named=name.startsWith("m_")?evidence.method(owner,name,desc):name;
            if(opcode==INVOKESTATIC&&owner.equals(UTILS)&&MEMBERS.contains(named+desc))
                return new Handle(H_INVOKESTATIC,bridge,named,desc,false);
            if(opcode==INVOKESTATIC&&owner.equals(P)&&"byName".equals(named)&&desc.equals("(Ljava/lang/String;)"+PD))
                return new Handle(H_INVOKESTATIC,bridge,"byName",desc,false);
            if(opcode==INVOKEVIRTUAL&&owner.equals(P)&&"getName".equals(named)&&desc.equals("(Ljava/lang/String;)Ljava/lang/String;"))
                return new Handle(H_INVOKESTATIC,bridge,"getName","("+PD+"Ljava/lang/String;)Ljava/lang/String;",false);
            return null;
        });
    }
    private static String constantName(String name,Evidence evidence) {
        String named=name.startsWith("f_")?evidence.field(name):name;
        if(named==null||!SOURCE_CONSTANTS.contains(named)||!named.equals("EMPTY")&&!evidence.targetField(named))
            throw new UnsupportedOperationException("Unproven source potion constant: "+name);
        return named;
    }
    private static MethodNode getter(ClassNode node,Map<String,MethodNode> getters,String name,String bridge) {
        return getters.computeIfAbsent(name,ignored->{
            String methodName="lms$potionConstant$"+name;
            if(node.methods.stream().anyMatch(m->m.name.equals(methodName)&&m.desc.equals("()"+PD)))
                throw new IllegalStateException("Source potion getter adapter collision: "+methodName);
            var method=new MethodNode(((node.access&ACC_INTERFACE)!=0 && node.version<V9?ACC_PUBLIC:ACC_PRIVATE)|ACC_STATIC|ACC_SYNTHETIC,methodName,"()"+PD,null,null);
            if(name.equals("EMPTY"))method.instructions.add(new FieldInsnNode(GETSTATIC,bridge,"EMPTY",PD));
            else {
                method.instructions.add(new FieldInsnNode(GETSTATIC,CONSTANTS,name,"Lnet/minecraft/core/Holder;"));
                method.instructions.add(new MethodInsnNode(INVOKEINTERFACE,"net/minecraft/core/Holder","value","()Ljava/lang/Object;",true));
                method.instructions.add(new TypeInsnNode(CHECKCAST,P));
            }
            method.instructions.add(new InsnNode(ARETURN));method.maxStack=1;return method;
        });
    }
    private static Object constant(Object value,ClassNode node,Map<String,MethodNode> getters,Evidence evidence,String bridge) {
        if(value instanceof Handle handle&&handle.getOwner().equals(CONSTANTS)&&handle.getDesc().equals(PD)) {
            if(handle.getTag()!=H_GETSTATIC)throw new UnsupportedOperationException("Callable source potion constant mutation");
            String name=constantName(handle.getName(),evidence);
            return new Handle(H_INVOKESTATIC,node.name,getter(node,getters,name,bridge).name,"()"+PD,(node.access&ACC_INTERFACE)!=0);
        }
        if(value instanceof ConstantDynamic dynamic) {
            Object[] args=new Object[dynamic.getBootstrapMethodArgumentCount()];for(int i=0;i<args.length;i++)args[i]=constant(dynamic.getBootstrapMethodArgument(i),node,getters,evidence,bridge);
            return new ConstantDynamic(dynamic.getName(),dynamic.getDescriptor(),(Handle)constant(dynamic.getBootstrapMethod(),node,getters,evidence,bridge),args);
        }
        return value;
    }
}
