package com.fuckingdeveloper.lms.profile;

import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Verified coordinate-only source widget hooks on the native pointer lifecycle. */
final class Forge1192WidgetPointerRewriter implements Opcodes {
    static final String ROOT="net/minecraft/client/gui/components/AbstractWidget";
    private static final String EVENT="Lnet/minecraft/client/input/MouseButtonEvent;";
    private static final String BRIDGE="com/fuckingdeveloper/lms/runtime/render/Forge1192WidgetPointerBridge";
    private static final Map<String,String> SOURCE=Map.of("onClick","(DD)V","onRelease","(DD)V","onDrag","(DDDD)V");
    interface Evidence { boolean widget(String owner); String canonical(String name,String desc); ClassNode managed(String owner); }
    private static String operation(String owner,String name,String desc,Evidence e) {
        if(!e.widget(owner))return null;
        String canonical=name.startsWith("m_")?e.canonical(name,desc):name;
        return canonical!=null&&desc.equals(SOURCE.get(canonical))?canonical:null;
    }
    private static String target(String op){return "("+EVENT+(op.equals("onClick")?"Z":op.equals("onDrag")?"DD":"")+")V";}
    private static boolean override(String owner,String op,Evidence e,Set<String> visited){
        if(owner==null||!visited.add(owner))return false;
        var node=e.managed(owner);if(node==null)return false;
        for(var method:node.methods)if((method.access&(ACC_STATIC|ACC_PRIVATE))==0&&op.equals(operation(owner,method.name,method.desc,e)))return true;
        return override(node.superName,op,e,visited);
    }
    static int rewrite(ClassNode node,Evidence e){
        int changed=0;var added=new ArrayList<MethodNode>();
        for(var method:List.copyOf(node.methods)){
            String op=operation(node.name,method.name,method.desc,e);
            if(op!=null&&(method.access&(ACC_STATIC|ACC_PRIVATE))==0){
                String alias="lms$"+op;
                if(node.methods.stream().anyMatch(m->m!=method&&(m.name.equals(alias)&&m.desc.equals(method.desc)||m.name.equals(op)&&m.desc.equals(target(op)))))throw new IllegalStateException("Source/native widget pointer collision: "+node.name+"#"+op);
                method.name=alias;method.access=(method.access&~ACC_PROTECTED)|ACC_PUBLIC;
                var entry=new MethodNode(ACC_PUBLIC|ACC_SYNTHETIC,op,target(op),null,null);
                entry.instructions.add(new VarInsnNode(ALOAD,0));
                coordinates(entry.instructions);
                if(op.equals("onDrag")){entry.instructions.add(new VarInsnNode(DLOAD,2));entry.instructions.add(new VarInsnNode(DLOAD,4));}
                entry.instructions.add(new MethodInsnNode(INVOKEVIRTUAL,node.name,alias,method.desc,false));entry.instructions.add(new InsnNode(RETURN));entry.maxStack=9;entry.maxLocals=op.equals("onDrag")?6:3;added.add(entry);changed++;
            }
            for(var insn:method.instructions.toArray()){
                if(insn instanceof MethodInsnNode call){
                    String action=operation(call.owner,call.name,call.desc,e);if(action==null)continue;
                    if(call.getOpcode()!=INVOKEVIRTUAL&&call.getOpcode()!=INVOKESPECIAL)throw new UnsupportedOperationException("Unproven widget pointer invocation: "+call.owner);
                    if(call.getOpcode()==INVOKESPECIAL&&override(call.owner,action,e,new HashSet<>())){call.name="lms$"+action;changed++;continue;}
                    // Source hook exposes coordinates only. Reconstruct a left-button event
                    // for the verified base implementation; virtual native dispatch still
                    // reaches managed overrides through the entry generated above.
                    int count=action.equals("onDrag")?4:2,base=method.maxLocals;method.maxLocals+=count*2;
                    var args=new InsnList();for(int i=count-1;i>=0;i--)args.add(new VarInsnNode(DSTORE,base+i*2));
                    args.add(new VarInsnNode(DLOAD,base));args.add(new VarInsnNode(DLOAD,base+2));args.add(new MethodInsnNode(INVOKESTATIC,BRIDGE,"left","(DD)"+EVENT,false));
                    if(action.equals("onClick"))args.add(new InsnNode(ICONST_0));
                    if(action.equals("onDrag")){args.add(new VarInsnNode(DLOAD,base+4));args.add(new VarInsnNode(DLOAD,base+6));}
                    method.instructions.insertBefore(call,args);call.name=action;call.desc=target(action);changed++;
                }else if(insn instanceof LdcInsnNode ldc){Object next=constant(node,ldc.cst,e,added);if(next!=ldc.cst){ldc.cst=next;changed++;}}
                else if(insn instanceof InvokeDynamicInsnNode indy)for(int i=0;i<indy.bsmArgs.length;i++){Object next=constant(node,indy.bsmArgs[i],e,added);if(next!=indy.bsmArgs[i]){indy.bsmArgs[i]=next;changed++;}}
            }
        }node.methods.addAll(added);return changed;
    }
    private static void coordinates(InsnList code){for(String axis:List.of("x","y")){code.add(new VarInsnNode(ALOAD,1));code.add(new MethodInsnNode(INVOKEVIRTUAL,"net/minecraft/client/input/MouseButtonEvent",axis,"()D",false));}}
    private static Object constant(ClassNode node,Object value,Evidence e,List<MethodNode> added){
        if(value instanceof Handle h){String op=operation(h.getOwner(),h.getName(),h.getDesc(),e);if(op==null)return value;
            if(h.getTag()!=H_INVOKEVIRTUAL)throw new UnsupportedOperationException("Source widget pointer special handles require lookup-context adaptation");
            String desc="(L"+h.getOwner()+";"+h.getDesc().substring(1),name="lms$pointerHandle$"+added.size();while(true){String n=name;if(node.methods.stream().noneMatch(m->m.name.equals(n))&&added.stream().noneMatch(m->m.name.equals(n)))break;name+="$";}
            var helper=new MethodNode(ACC_PUBLIC|ACC_STATIC|ACC_SYNTHETIC,name,desc,null,null);helper.instructions.add(new VarInsnNode(ALOAD,0));helper.instructions.add(new VarInsnNode(DLOAD,1));helper.instructions.add(new VarInsnNode(DLOAD,3));helper.instructions.add(new MethodInsnNode(INVOKESTATIC,BRIDGE,"left","(DD)"+EVENT,false));
            if(op.equals("onClick"))helper.instructions.add(new InsnNode(ICONST_0));
            if(op.equals("onDrag")){helper.instructions.add(new VarInsnNode(DLOAD,5));helper.instructions.add(new VarInsnNode(DLOAD,7));}
            helper.instructions.add(new MethodInsnNode(INVOKEVIRTUAL,h.getOwner(),op,target(op),false));helper.instructions.add(new InsnNode(RETURN));helper.maxStack=9;helper.maxLocals=op.equals("onDrag")?9:5;added.add(helper);return new Handle(H_INVOKESTATIC,node.name,name,desc,(node.access&ACC_INTERFACE)!=0);
        }
        if(value instanceof ConstantDynamic d){Object[] args=new Object[d.getBootstrapMethodArgumentCount()];boolean changed=false;for(int i=0;i<args.length;i++){Object old=d.getBootstrapMethodArgument(i);args[i]=constant(node,old,e,added);changed|=args[i]!=old;}return changed?new ConstantDynamic(d.getName(),d.getDescriptor(),d.getBootstrapMethod(),args):value;}return value;
    }
}
