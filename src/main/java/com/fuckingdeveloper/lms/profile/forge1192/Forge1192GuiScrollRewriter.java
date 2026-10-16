package com.fuckingdeveloper.lms.profile.forge1192;

import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Exact source listener ancestry and member identity, independent of widget identity. */
final class Forge1192GuiScrollRewriter {
    static final String ROOT="net/minecraft/client/gui/components/events/GuiEventListener";
    private static final String SOURCE="(DDD)Z",TARGET="(DDDD)Z",CALLBACK="lms$mouseScrolled";
    private static final String BRIDGE="com/fuckingdeveloper/lms/profile/forge1192/runtime/render/Forge1192GuiScrollBridge";
    interface Evidence {
        boolean listener(String owner);
        String canonical(String name,String descriptor);
        ClassNode managed(String owner);
        ClassNode hierarchy(String owner);
        String sourceDeclaration(String owner);
    }
    private static boolean matches(String owner,String name,String descriptor,Evidence e) {
        return descriptor.equals(SOURCE)&&e.listener(owner)&&"mouseScrolled".equals(name.startsWith("m_")?e.canonical(name,descriptor):name);
    }
    private static boolean managedOverride(String owner,Evidence e,Set<String> visited) {
        if(owner==null||!visited.add(owner))return false;
        var source=e.managed(owner);if(source==null)return false;
        for(var method:source.methods)if((method.access&(Opcodes.ACC_STATIC|Opcodes.ACC_PRIVATE))==0&&matches(source.name,method.name,method.desc,e))return true;
        if(managedOverride(source.superName,e,visited))return true;
        for(String parent:source.interfaces)if(managedOverride(parent,e,visited))return true;
        return false;
    }
    private static String declaration(String owner,Evidence e,Set<String> visited) {
        if(owner==null||!visited.add(owner))return null;
        String exact=e.sourceDeclaration(owner);if(exact!=null)return exact;
        var type=e.hierarchy(owner);if(type==null)return null;
        String inherited=declaration(type.superName,e,visited);if(inherited!=null)return inherited;
        String result=null;
        for(String parent:type.interfaces){String candidate=declaration(parent,e,visited);if(candidate!=null){if(result!=null&&!result.equals(candidate))throw new UnsupportedOperationException("Ambiguous source scroll defaults on "+owner);result=candidate;}}
        return result;
    }
    static int rewrite(ClassNode node,Evidence e) {
        int count=0;var added=new ArrayList<MethodNode>();
        for(var method:List.copyOf(node.methods)) {
            if(matches(node.name,method.name,method.desc,e)&&(method.access&(Opcodes.ACC_STATIC|Opcodes.ACC_PRIVATE))==0) {
                if(node.methods.stream().anyMatch(m->m.name.equals("mouseScrolled")&&m.desc.equals(TARGET)))throw new IllegalStateException("Source/native GUI scroll callback collision: "+node.name);
                if(node.methods.stream().anyMatch(m->m!=method&&m.name.equals(CALLBACK)&&m.desc.equals(SOURCE)))throw new IllegalStateException("Source GUI scroll callback collision: "+node.name);
                method.name=CALLBACK;method.access=(method.access&~Opcodes.ACC_PROTECTED)|Opcodes.ACC_PUBLIC;count++;
                var entry=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_SYNTHETIC,"mouseScrolled",TARGET,null,null);
                entry.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));entry.instructions.add(new VarInsnNode(Opcodes.DLOAD,1));entry.instructions.add(new VarInsnNode(Opcodes.DLOAD,3));entry.instructions.add(new VarInsnNode(Opcodes.DLOAD,5));entry.instructions.add(new VarInsnNode(Opcodes.DLOAD,7));
                entry.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,BRIDGE,"verticalAmount","(DD)D",false));
                boolean itf=(node.access&Opcodes.ACC_INTERFACE)!=0;
                entry.instructions.add(new MethodInsnNode(itf?Opcodes.INVOKEINTERFACE:Opcodes.INVOKEVIRTUAL,node.name,CALLBACK,SOURCE,itf));entry.instructions.add(new InsnNode(Opcodes.IRETURN));entry.maxStack=9;entry.maxLocals=9;added.add(entry);
            }
            for(var instruction:method.instructions.toArray()) {
                if(instruction instanceof MethodInsnNode call&&matches(call.owner,call.name,call.desc,e)) {
                    if(call.getOpcode()==Opcodes.INVOKESPECIAL) {
                        if(managedOverride(call.owner,e,new HashSet<>())){call.name=CALLBACK;count++;continue;}
                        if(!Set.of(ROOT,"net/minecraft/client/gui/components/events/ContainerEventHandler").contains(java.util.Objects.toString(declaration(call.owner,e,new HashSet<>()),"")))throw new UnsupportedOperationException("Unproven native source-super scroll implementation: "+call.owner);
                        int amount=method.maxLocals;method.maxLocals+=2;
                        var args=new InsnList();args.add(new VarInsnNode(Opcodes.DSTORE,amount));args.add(new InsnNode(Opcodes.DCONST_0));args.add(new VarInsnNode(Opcodes.DLOAD,amount));method.instructions.insertBefore(call,args);
                        call.name="mouseScrolled";call.desc=TARGET;
                    } else if(call.getOpcode()==Opcodes.INVOKEVIRTUAL||call.getOpcode()==Opcodes.INVOKEINTERFACE) {
                        call.setOpcode(Opcodes.INVOKESTATIC);call.owner=BRIDGE;call.name="mouseScrolled";call.desc="(L"+ROOT+";DDD)Z";call.itf=false;
                    } else continue;
                    count++;
                } else if(instruction instanceof LdcInsnNode ldc){Object next=constant(node,ldc.cst,e,added);if(next!=ldc.cst){ldc.cst=next;count++;}}
                else if(instruction instanceof InvokeDynamicInsnNode indy)for(int i=0;i<indy.bsmArgs.length;i++){Object next=constant(node,indy.bsmArgs[i],e,added);if(next!=indy.bsmArgs[i]){indy.bsmArgs[i]=next;count++;}}
            }
        }
        node.methods.addAll(added);return count;
    }
    private static Object constant(ClassNode node,Object value,Evidence e,List<MethodNode> added) {
        if(value instanceof Handle handle&&matches(handle.getOwner(),handle.getName(),handle.getDesc(),e)) {
            if(handle.getTag()==Opcodes.H_INVOKESPECIAL)throw new UnsupportedOperationException("Source GUI scroll special handles require exact lookup-context adaptation");
            if(handle.getTag()!=Opcodes.H_INVOKEVIRTUAL&&handle.getTag()!=Opcodes.H_INVOKEINTERFACE)return value;
            String descriptor="(L"+handle.getOwner()+";DDD)Z",name="lms$scrollHandle$"+added.size();
            while(true){String candidate=name;if(node.methods.stream().noneMatch(m->m.name.equals(candidate))&&added.stream().noneMatch(m->m.name.equals(candidate)))break;name+="$";}
            var thunk=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC|Opcodes.ACC_SYNTHETIC,name,descriptor,null,null);
            thunk.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));thunk.instructions.add(new VarInsnNode(Opcodes.DLOAD,1));thunk.instructions.add(new VarInsnNode(Opcodes.DLOAD,3));thunk.instructions.add(new VarInsnNode(Opcodes.DLOAD,5));
            thunk.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,BRIDGE,"mouseScrolled","(L"+ROOT+";DDD)Z",false));thunk.instructions.add(new InsnNode(Opcodes.IRETURN));thunk.maxStack=7;thunk.maxLocals=7;added.add(thunk);
            return new Handle(Opcodes.H_INVOKESTATIC,node.name,name,descriptor,(node.access&Opcodes.ACC_INTERFACE)!=0);
        }
        if(value instanceof ConstantDynamic dynamic){Object[] args=new Object[dynamic.getBootstrapMethodArgumentCount()];boolean changed=false;for(int i=0;i<args.length;i++){Object old=dynamic.getBootstrapMethodArgument(i);args[i]=constant(node,old,e,added);changed|=args[i]!=old;}return changed?new ConstantDynamic(dynamic.getName(),dynamic.getDescriptor(),dynamic.getBootstrapMethod(),args):value;}
        return value;
    }
}
