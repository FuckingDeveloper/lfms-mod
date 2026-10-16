package com.fuckingdeveloper.lms.profile.forge1192;

import java.util.*;
import java.util.function.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Source list operations are selected by verified ancestry, names and descriptors. */
final class Forge1192SelectionListRewriter {
    static final String ROOT="net/minecraft/client/gui/components/AbstractSelectionList",CONTAINER="net/minecraft/client/gui/components/ContainerObjectSelectionList",
        VIEW="com/fuckingdeveloper/lms/profile/forge1192/runtime/render/Forge1192ContainerSelectionList",POSE="Lcom/mojang/blaze3d/vertex/PoseStack;";
    private static final Map<String,String> CHANGED=Map.ofEntries(
        Map.entry("render","("+POSE+"IIF)V"),Map.entry("renderList","("+POSE+"IIF)V"),Map.entry("renderItem","("+POSE+"IIFIIIII)V"),
        Map.entry("renderDecorations","("+POSE+"II)V"),Map.entry("renderBackground","("+POSE+")V"),Map.entry("renderSelection","("+POSE+"IIIII)V"),
        Map.entry("children","()Ljava/util/List;"),Map.entry("getEntry","(I)L"+ROOT+"$Entry;"),
        Map.entry("getWidth","()I"),Map.entry("getHeight","()I"),Map.entry("getTop","()I"),Map.entry("getBottom","()I"),Map.entry("getLeft","()I"),Map.entry("getRight","()I"),
        Map.entry("getMaxPosition","()I"),Map.entry("getScrollAmount","()D"),Map.entry("setScrollAmount","(D)V"),Map.entry("getMaxScroll","()I"),Map.entry("getScrollBottom","()I"),
        Map.entry("getScrollbarPosition","()I"),Map.entry("getRowTop","(I)I"),Map.entry("getRowBottom","(I)I"),Map.entry("getEntryAtPosition","(DD)L"+ROOT+"$Entry;"),
        Map.entry("isFocused","()Z"),Map.entry("isSelectedItem","(I)Z"),Map.entry("updateSize","(IIII)V"),Map.entry("setLeftPos","(I)V"),Map.entry("scroll","(I)V"),
        Map.entry("setRenderSelection","(Z)V"),Map.entry("setRenderBackground","(Z)V"),Map.entry("setRenderTopAndBottom","(Z)V"),Map.entry("setRenderHeader","(ZI)V"),
        Map.entry("clickedHeader","(II)V"),Map.entry("updateScrollingState","(DDI)V"),
        Map.entry("mouseClicked","(DDI)Z"),Map.entry("mouseReleased","(DDI)Z"),Map.entry("mouseDragged","(DDIDD)Z"),Map.entry("mouseScrolled","(DDD)Z"),Map.entry("keyPressed","(III)Z"));
    interface Evidence {boolean subtype(String owner,String root);String method(String owner,String name,String desc);String fieldOwner(String owner,String name,String desc);String field(String owner,String name,String desc);}
    static String relocation(String owner){return owner.equals(CONTAINER)?VIEW:owner.equals(CONTAINER+"$Entry")?VIEW+"$Entry":null;}
    static String descriptor(String desc){return desc.replace("L"+CONTAINER+";","L"+VIEW+";").replace("L"+CONTAINER+"<","L"+VIEW+"<").replace("L"+CONTAINER+"$Entry;","L"+VIEW+"$Entry;").replace("L"+CONTAINER+"$Entry<","L"+VIEW+"$Entry<");}
    private static String canonical(Evidence e,String owner,String name,String desc,boolean entry){
        if(!name.startsWith("m_"))return name;
        for(String root:entry?List.of(CONTAINER+"$Entry",ROOT+"$Entry","net/minecraft/client/gui/components/events/GuiEventListener","net/minecraft/client/gui/components/events/ContainerEventHandler"):
                List.of(CONTAINER,ROOT,"net/minecraft/client/gui/components/Widget","net/minecraft/client/gui/components/events/GuiEventListener","net/minecraft/client/gui/components/events/ContainerEventHandler")){
            String candidate=e.method(root,name,desc);if(candidate!=null)return candidate;
        }return null;
    }
    private static Handle handle(Handle source,Evidence e){
        boolean entry=e.subtype(source.getOwner(),CONTAINER+"$Entry"),list=e.subtype(source.getOwner(),CONTAINER);
        if(!list&&!entry)return source;
        int tag=source.getTag();
        if(tag==Opcodes.H_GETFIELD||tag==Opcodes.H_PUTFIELD){
            if(!ROOT.equals(e.fieldOwner(source.getOwner(),source.getName(),source.getDesc())))return source;
            String name=source.getName().startsWith("f_")?e.field(ROOT,source.getName(),source.getDesc()):source.getName();
            if(list&&name!=null&&Set.of("width","height","y0","y1","x0","x1","itemHeight","headerHeight").contains(name))return new Handle(tag,VIEW,"lms$"+name,source.getDesc(),false);
            return source;
        }
        if(tag!=Opcodes.H_INVOKEVIRTUAL&&tag!=Opcodes.H_INVOKESPECIAL&&tag!=Opcodes.H_INVOKEINTERFACE)return source;
        String canonical=canonical(e,source.getOwner(),source.getName(),source.getDesc(),entry);if(canonical==null)return source;
        if(entry&&canonical.equals("render")&&source.getDesc().equals("("+POSE+"IIIIIIIZF)V"))return new Handle(tag,source.getOwner(),"lms$render",source.getDesc(),source.isInterface());
        String required=CHANGED.get(canonical);
        if(source.getDesc().equals(required)&&(!entry||Set.of("mouseClicked","mouseReleased","mouseDragged","mouseScrolled").contains(canonical)))return new Handle(tag,source.getOwner(),"lms$"+canonical,source.getDesc(),source.isInterface());
        // Surviving source declarations retain native names. The regular resolver
        // cannot recover their source owner after semantic class projection.
        if(source.getName().startsWith("m_")&&Set.of("getRowWidth","getRowLeft","getRowRight","getSelected","setSelected","getFocused","setFocused","isDragging","setDragging","isMouseOver","narrationPriority","clearEntries","replaceEntries","addEntry","addEntryToTop","getItemCount","getHovered","narratables").contains(canonical))return new Handle(tag,source.getOwner(),canonical,source.getDesc(),source.isInterface());
        return source;
    }
    static int rewrite(ClassNode node,Evidence evidence){
        int changed=0;var helpers=new ArrayList<MethodNode>();
        for(var method:List.copyOf(node.methods)){
            var identity=handle(new Handle(Opcodes.H_INVOKEVIRTUAL,node.name,method.name,method.desc,false),evidence);
            if(!identity.getName().equals(method.name)){method.name=identity.getName();method.access=(method.access&~(Opcodes.ACC_PRIVATE|Opcodes.ACC_PROTECTED))|Opcodes.ACC_PUBLIC;changed++;}
            for(var instruction:method.instructions.toArray()){
                if(instruction instanceof MethodInsnNode call){int tag=switch(call.getOpcode()){case Opcodes.INVOKEVIRTUAL->Opcodes.H_INVOKEVIRTUAL;case Opcodes.INVOKESPECIAL->Opcodes.H_INVOKESPECIAL;case Opcodes.INVOKEINTERFACE->Opcodes.H_INVOKEINTERFACE;default->-1;};if(tag<0)continue;var target=handle(new Handle(tag,call.owner,call.name,call.desc,call.itf),evidence);if(!target.getName().equals(call.name)){call.name=target.getName();changed++;}}
                else if(instruction instanceof FieldInsnNode field){int tag=field.getOpcode()==Opcodes.GETFIELD?Opcodes.H_GETFIELD:field.getOpcode()==Opcodes.PUTFIELD?Opcodes.H_PUTFIELD:-1;if(tag<0)continue;var target=handle(new Handle(tag,field.owner,field.name,field.desc,false),evidence);if(!target.getName().equals(field.name)){field.owner=target.getOwner();field.name=target.getName();changed++;}}
                else if(instruction instanceof LdcInsnNode ldc){Object next=constant(node,ldc.cst,evidence,helpers);if(next!=ldc.cst){ldc.cst=next;changed++;}}
                else if(instruction instanceof InvokeDynamicInsnNode indy)for(int i=0;i<indy.bsmArgs.length;i++){Object next=constant(node,indy.bsmArgs[i],evidence,helpers);if(next!=indy.bsmArgs[i]){indy.bsmArgs[i]=next;changed++;}}
            }
        }node.methods.addAll(helpers);return changed;
    }
    private static Object constant(ClassNode node,Object value,Evidence e,List<MethodNode> helpers){
        if(value instanceof Handle source){
            var target=handle(source,e);if(target.equals(source))return value;
            int tag=source.getTag();
            if(tag==Opcodes.H_INVOKESPECIAL)throw new UnsupportedOperationException("Source selection-list special method handles require exact lookup-context adaptation");
            String desc=tag==Opcodes.H_GETFIELD?"(L"+source.getOwner()+";)"+source.getDesc():tag==Opcodes.H_PUTFIELD?"(L"+source.getOwner()+";"+source.getDesc()+")V":"(L"+source.getOwner()+";"+source.getDesc().substring(1);
            String name="lms$listHandle$"+helpers.size();while(true){String candidate=name;if(node.methods.stream().noneMatch(m->m.name.equals(candidate))&&helpers.stream().noneMatch(m->m.name.equals(candidate)))break;name+="$";}
            var helper=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC|Opcodes.ACC_SYNTHETIC,name,desc,null,null);int local=0;
            for(Type type:Type.getArgumentTypes(desc)){helper.instructions.add(new VarInsnNode(type.getOpcode(Opcodes.ILOAD),local));local+=type.getSize();}
            if(tag==Opcodes.H_GETFIELD||tag==Opcodes.H_PUTFIELD)helper.instructions.add(new FieldInsnNode(tag==Opcodes.H_GETFIELD?Opcodes.GETFIELD:Opcodes.PUTFIELD,target.getOwner(),target.getName(),target.getDesc()));
            else helper.instructions.add(new MethodInsnNode(tag==Opcodes.H_INVOKEINTERFACE?Opcodes.INVOKEINTERFACE:Opcodes.INVOKEVIRTUAL,target.getOwner(),target.getName(),target.getDesc(),target.isInterface()));
            helper.instructions.add(new InsnNode(Type.getReturnType(desc).getOpcode(Opcodes.IRETURN)));helper.maxStack=local;helper.maxLocals=local;helpers.add(helper);
            return new Handle(Opcodes.H_INVOKESTATIC,node.name,name,desc,(node.access&Opcodes.ACC_INTERFACE)!=0);
        }
        if(value instanceof ConstantDynamic d){Object[] args=new Object[d.getBootstrapMethodArgumentCount()];boolean changed=false;for(int i=0;i<args.length;i++){Object old=d.getBootstrapMethodArgument(i);args[i]=constant(node,old,e,helpers);changed|=args[i]!=old;}return changed?new ConstantDynamic(d.getName(),d.getDescriptor(),d.getBootstrapMethod(),args):value;}return value;
    }
}
