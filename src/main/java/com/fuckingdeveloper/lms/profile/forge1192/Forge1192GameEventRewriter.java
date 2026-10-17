package com.fuckingdeveloper.lms.profile.forge1192;

import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import com.fuckingdeveloper.lms.transform.SemanticInvocationRewriter;
import static org.objectweb.asm.Opcodes.*;

final class Forge1192GameEventRewriter {
    private static final String EVENT="net/minecraft/world/level/gameevent/GameEvent", LEVEL="net/minecraft/world/level/LevelAccessor", ENTITY="net/minecraft/world/entity/Entity";
    private static final String E="L"+EVENT+";", A="L"+ENTITY+";", V="Lnet/minecraft/world/phys/Vec3;", P="Lnet/minecraft/core/BlockPos;", C="L"+EVENT+"$Context;";
    private static final Set<String> LEVEL_CALLS=Set.of("("+E+V+C+")V","("+A+E+V+")V","("+A+E+P+")V","("+E+P+C+")V"), ENTITY_CALLS=Set.of("("+E+")V","("+E+A+")V");
    interface Evidence {boolean subtype(String owner,String root);String method(String owner,String name,String desc);String field(String name);}
    private static String root(String owner,String name,String desc,Evidence e) {
        String root=LEVEL_CALLS.contains(desc)?LEVEL:ENTITY_CALLS.contains(desc)?ENTITY:null;
        return root!=null&&e.subtype(owner,root)&&"gameEvent".equals(name.startsWith("m_")?e.method(root,name,desc):name)?root:null;
    }
    static int rewrite(ClassNode node,Evidence evidence,Map<String,String> fields,String bridge,SemanticInvocationRewriter calls) {
        for(var method:node.methods)if((method.access&ACC_STATIC)==0&&root(node.name,method.name,method.desc,evidence)!=null)
            throw new UnsupportedOperationException("Custom raw source game-event delivery callback requires an override adapter: "+node.name+"#"+method.name+method.desc);
        int count=calls.rewrite(node,(opcode,owner,name,desc)->{
            if(opcode!=INVOKEVIRTUAL&&opcode!=INVOKEINTERFACE)return null;
            String root=root(owner,name,desc,evidence);return root==null?null:new Handle(H_INVOKESTATIC,bridge,"gameEvent","(L"+root+";"+desc.substring(1),false);
        });
        var adapters=new ArrayList<MethodNode>();
        for(var method:node.methods)for(var insn:method.instructions.toArray()) {
            if(insn instanceof FieldInsnNode field&&field.getOpcode()==GETSTATIC&&field.owner.equals(EVENT)&&field.desc.equals(E)) {
                String name=field.name.startsWith("f_")?evidence.field(field.name):field.name;
                String nativeDesc=name==null?null:fields.get(name);if(nativeDesc==null)continue;
                field.name=name;field.desc=nativeDesc;var tail=new InsnList();tail.add(new MethodInsnNode(INVOKEINTERFACE,"net/minecraft/core/Holder","value","()Ljava/lang/Object;",true));tail.add(new TypeInsnNode(CHECKCAST,EVENT));method.instructions.insert(field,tail);count++;
            }
            if(insn instanceof LdcInsnNode ldc){Object next=constant(node,ldc.cst,evidence,fields,adapters);if(next!=ldc.cst){ldc.cst=next;count++;}}
            if(insn instanceof InvokeDynamicInsnNode indy)for(int i=0;i<indy.bsmArgs.length;i++){Object next=constant(node,indy.bsmArgs[i],evidence,fields,adapters);if(next!=indy.bsmArgs[i]){indy.bsmArgs[i]=next;count++;}}
        }
        node.methods.addAll(adapters);return count;
    }
    private static Object constant(ClassNode node,Object value,Evidence evidence,Map<String,String> fields,List<MethodNode> adapters) {
        if(value instanceof Handle h&&h.getTag()==H_GETSTATIC&&h.getOwner().equals(EVENT)&&h.getDesc().equals(E)) {
            String name=h.getName().startsWith("f_")?evidence.field(h.getName()):h.getName(),desc=name==null?null:fields.get(name);if(desc==null)return value;
            String alias="lms$gameEvent$"+(node.methods.size()+adapters.size());
            while(has(node,adapters,alias))alias+="$";
            var m=new MethodNode(ACC_PUBLIC|ACC_STATIC|ACC_SYNTHETIC,alias,"()"+E,null,null);
            m.instructions.add(new FieldInsnNode(GETSTATIC,EVENT,name,desc));m.instructions.add(new MethodInsnNode(INVOKEINTERFACE,"net/minecraft/core/Holder","value","()Ljava/lang/Object;",true));m.instructions.add(new TypeInsnNode(CHECKCAST,EVENT));m.instructions.add(new InsnNode(ARETURN));adapters.add(m);
            return new Handle(H_INVOKESTATIC,node.name,alias,m.desc,(node.access&ACC_INTERFACE)!=0);
        }
        if(value instanceof ConstantDynamic d){Object[] args=new Object[d.getBootstrapMethodArgumentCount()];boolean changed=false;for(int i=0;i<args.length;i++){Object old=d.getBootstrapMethodArgument(i);args[i]=constant(node,old,evidence,fields,adapters);changed|=args[i]!=old;}if(changed)return new ConstantDynamic(d.getName(),d.getDescriptor(),d.getBootstrapMethod(),args);}
        return value;
    }
    private static boolean has(ClassNode node,List<MethodNode> extra,String name){return node.methods.stream().anyMatch(m->m.name.equals(name))||extra.stream().anyMatch(m->m.name.equals(name));}
}
