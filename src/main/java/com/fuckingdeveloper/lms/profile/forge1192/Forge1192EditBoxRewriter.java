package com.fuckingdeveloper.lms.profile.forge1192;

import java.util.*;
import java.util.function.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Source construction and removed operations, independent of consumer identity. */
final class Forge1192EditBoxRewriter {
    private static final String ROOT="net/minecraft/client/gui/components/EditBox",VIEW="com/fuckingdeveloper/lms/profile/forge1192/runtime/render/Forge1192EditBox";
    private static final Map<String,String> OPERATIONS=Map.ofEntries(
        Map.entry("setFilter","(Ljava/util/function/Predicate;)V"),Map.entry("setFormatter","(Ljava/util/function/BiFunction;)V"),
        Map.entry("tick","()V"),Map.entry("setFocus","(Z)V"),Map.entry("moveCursor","(I)V"),Map.entry("moveCursorTo","(I)V"),
        Map.entry("moveCursorToStart","()V"),Map.entry("moveCursorToEnd","()V"),Map.entry("keyPressed","(III)Z"),Map.entry("charTyped","(CI)Z"),Map.entry("mouseClicked","(DDI)Z"));
    interface Names {String canonical(String name,String desc);}
    private static String operation(String name,String desc,Names names){String canonical=name.startsWith("m_")?names.canonical(name,desc):name;return canonical!=null&&desc.equals(OPERATIONS.get(canonical))?canonical:null;}
    static int rewrite(ClassNode node,Predicate<String> subtype,Names names){
        int changed=0;var helpers=new ArrayList<MethodNode>();
        boolean own=subtype.test(node.name);
        for(var method:List.copyOf(node.methods)){
            String name=own?operation(method.name,method.desc,names):null;
            if(name!=null){method.name="lms$"+name;method.access=(method.access&~(Opcodes.ACC_PROTECTED|Opcodes.ACC_PRIVATE))|Opcodes.ACC_PUBLIC;changed++;}
            for(var instruction:method.instructions.toArray()){
                if(instruction instanceof TypeInsnNode type&&type.getOpcode()==Opcodes.NEW&&type.desc.equals(ROOT)){type.desc=VIEW;changed++;}
                else if(instruction instanceof MethodInsnNode call){
                    if(call.getOpcode()==Opcodes.INVOKESPECIAL&&call.owner.equals(ROOT)&&call.name.equals("<init>")){call.owner=VIEW;changed++;continue;}
                    if((call.getOpcode()!=Opcodes.INVOKEVIRTUAL&&call.getOpcode()!=Opcodes.INVOKESPECIAL)||!subtype.test(call.owner))continue;
                    String operation=operation(call.name,call.desc,names);if(operation==null)continue;
                    if(call.getOpcode()==Opcodes.INVOKESPECIAL){if(call.owner.equals(ROOT))call.owner=VIEW;call.name="lms$"+operation;}
                    else{call.setOpcode(Opcodes.INVOKESTATIC);call.owner=VIEW;call.name=operation;call.desc="(L"+ROOT+";"+call.desc.substring(1);call.itf=false;}
                    changed++;
                }else if(instruction instanceof LdcInsnNode ldc){Object next=constant(node,ldc.cst,subtype,names,helpers);if(next!=ldc.cst){ldc.cst=next;changed++;}}
                else if(instruction instanceof InvokeDynamicInsnNode indy)for(int i=0;i<indy.bsmArgs.length;i++){Object next=constant(node,indy.bsmArgs[i],subtype,names,helpers);if(next!=indy.bsmArgs[i]){indy.bsmArgs[i]=next;changed++;}}
            }
        }
        if(ROOT.equals(node.superName)){node.superName=VIEW;changed++;}
        node.methods.addAll(helpers);return changed;
    }
    private static Object constant(ClassNode node,Object value,Predicate<String> subtype,Names names,List<MethodNode> helpers){
        if(value instanceof Handle source){
            boolean constructor=source.getTag()==Opcodes.H_NEWINVOKESPECIAL&&source.getOwner().equals(ROOT)&&source.getName().equals("<init>");
            String operation=subtype.test(source.getOwner())?operation(source.getName(),source.getDesc(),names):null;
            if(!constructor&&operation==null)return value;
            if(!constructor&&source.getTag()!=Opcodes.H_INVOKEVIRTUAL)throw new UnsupportedOperationException("Source edit-box special handles require exact lookup-context adaptation");
            String desc=constructor?source.getDesc().replace(")V",")L"+ROOT+";"):"(L"+source.getOwner()+";"+source.getDesc().substring(1);
            String name="lms$editHandle$"+helpers.size();while(true){String candidate=name;if(node.methods.stream().noneMatch(m->m.name.equals(candidate))&&helpers.stream().noneMatch(m->m.name.equals(candidate)))break;name+="$";}
            var method=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC|Opcodes.ACC_SYNTHETIC,name,desc,null,null);int local=0;
            if(constructor){method.instructions.add(new TypeInsnNode(Opcodes.NEW,VIEW));method.instructions.add(new InsnNode(Opcodes.DUP));}
            for(Type type:Type.getArgumentTypes(desc)){method.instructions.add(new VarInsnNode(type.getOpcode(Opcodes.ILOAD),local));local+=type.getSize();}
            if(constructor)method.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,VIEW,"<init>",source.getDesc(),false));
            else method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,VIEW,operation,"(L"+ROOT+";"+source.getDesc().substring(1),false));
            method.instructions.add(new InsnNode(Type.getReturnType(desc).getOpcode(Opcodes.IRETURN)));method.maxStack=local+2;method.maxLocals=local;helpers.add(method);
            return new Handle(Opcodes.H_INVOKESTATIC,node.name,name,desc,(node.access&Opcodes.ACC_INTERFACE)!=0);
        }
        if(value instanceof ConstantDynamic dynamic){Object[] args=new Object[dynamic.getBootstrapMethodArgumentCount()];boolean changed=false;for(int i=0;i<args.length;i++){Object old=dynamic.getBootstrapMethodArgument(i);args[i]=constant(node,old,subtype,names,helpers);changed|=args[i]!=old;}return changed?new ConstantDynamic(dynamic.getName(),dynamic.getDescriptor(),dynamic.getBootstrapMethod(),args):value;}
        return value;
    }
}
