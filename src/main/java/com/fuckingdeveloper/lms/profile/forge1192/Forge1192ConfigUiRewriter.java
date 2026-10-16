package com.fuckingdeveloper.lms.profile.forge1192;

import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Verified source control/notification identities; final type projection remains centralized. */
final class Forge1192ConfigUiRewriter {
    static final String CHECK="net/minecraft/client/gui/components/Checkbox",SYS="net/minecraft/client/gui/components/toasts/SystemToast",
            MANAGER="net/minecraft/client/gui/components/toasts/ToastComponent",TOAST="net/minecraft/client/gui/components/toasts/Toast",
            VIEW="com/fuckingdeveloper/lms/profile/forge1192/runtime/render/Forge1192SystemToast",COMPONENT="Lnet/minecraft/network/chat/Component;";
    interface Evidence {boolean subtype(String owner,String root);String canonical(String owner,String name,String descriptor);}
    static String relocation(String owner){return switch(owner){case CHECK->"com/fuckingdeveloper/lms/profile/forge1192/runtime/render/Forge1192Checkbox";case SYS->VIEW;case SYS+"$SystemToastIds"->VIEW+"$Id";case MANAGER->"net/minecraft/client/gui/components/toasts/ToastManager";default->null;};}
    private static String canonical(String root,String name,String desc,Evidence e){return name.startsWith("m_")?e.canonical(root,name,desc):name;}
    private static Handle operation(Handle h,Evidence e){
        if(h.getName().equals("<init>")) {
            String desc=h.getDesc();
            if(h.getOwner().equals(MANAGER))throw new UnsupportedOperationException("Source toast-manager construction requires native options/lifecycle adaptation");
            if(h.getOwner().equals(CHECK)&&!desc.equals("(IIII"+COMPONENT+"Z)V")&&!desc.equals("(IIII"+COMPONENT+"ZZ)V"))throw new UnsupportedOperationException("Unproven source checkbox constructor: "+desc);
            if(h.getOwner().equals(SYS)&&!desc.equals("(L"+SYS+"$SystemToastIds;"+COMPONENT+COMPONENT+")V"))throw new UnsupportedOperationException("Unproven source system-toast constructor: "+desc);
        }
        int tag=h.getTag();if(tag!=Opcodes.H_INVOKESTATIC&&tag!=Opcodes.H_INVOKEVIRTUAL&&tag!=Opcodes.H_INVOKESPECIAL&&tag!=Opcodes.H_INVOKEINTERFACE)return h;
        String owner=h.getOwner(),name=h.getName(),desc=h.getDesc();
        if (owner.equals("net/minecraft/client/Minecraft") && tag==Opcodes.H_INVOKEVIRTUAL
                && ((name.equals("pushGuiLayer") && desc.equals("(Lnet/minecraft/client/gui/screens/Screen;)V"))
                || (name.equals("popGuiLayer") && desc.equals("()V"))))
            return new Handle(Opcodes.H_INVOKESTATIC,"com/fuckingdeveloper/lms/profile/forge1192/runtime/render/Forge1192MinecraftClientBridge",
                    name,"(Lnet/minecraft/client/Minecraft;"+desc.substring(1),false);
        if (owner.equals("net/minecraftforge/client/ForgeHooksClient") && tag==Opcodes.H_INVOKESTATIC) {
            String client="Lnet/minecraft/client/Minecraft;", screen="Lnet/minecraft/client/gui/screens/Screen;";
            if ((name.equals("pushGuiLayer") && desc.equals("("+client+screen+")V"))
                    || (Set.of("popGuiLayer","clearGuiLayers").contains(name) && desc.equals("("+client+")V")))
                return new Handle(tag,"com/fuckingdeveloper/lms/profile/forge1192/runtime/render/Forge1192MinecraftClientBridge",name,desc,false);
        }
        if(owner.equals("net/minecraft/client/Minecraft")&&tag==Opcodes.H_INVOKEVIRTUAL&&desc.equals("()L"+MANAGER+";")&&"getToasts".equals(canonical(owner,name,desc,e)))
            return new Handle(Opcodes.H_INVOKESTATIC,VIEW,"getToasts","(Lnet/minecraft/client/Minecraft;)L"+MANAGER+";",false);
        if(owner.equals("net/minecraft/network/chat/CommonComponents")&&tag==Opcodes.H_INVOKESTATIC&&desc.equals("("+COMPONENT+COMPONENT+")Lnet/minecraft/network/chat/MutableComponent;")&&"joinForNarration".equals(canonical(owner,name,desc,e)))
            return new Handle(tag,"com/fuckingdeveloper/lms/profile/forge1192/runtime/chat/Forge1192TranslationBridge","joinForNarration",desc,false);
        if(e.subtype(owner,CHECK)){
            String operation=canonical(CHECK,name,desc,e);
            if(tag!=Opcodes.H_INVOKESTATIC&&"onPress".equals(operation)&&desc.equals("()V"))return new Handle(tag,owner,"lms$onPress",desc,h.isInterface());
            if(tag!=Opcodes.H_INVOKESTATIC&&"selected".equals(operation)&&desc.equals("()Z"))return new Handle(tag,owner,"selected",desc,h.isInterface());
        }
        if(e.subtype(owner,SYS)) {
            String operation=canonical(SYS,name,desc,e);
            if(Set.of("width","height","reset","getToken","add","addOrUpdate","multiline","onWorldAccessFailure","onWorldDeleteFailure","onPackCopyFailure").contains(Objects.toString(operation,""))
                    && systemDescriptor(operation,desc,tag))return new Handle(tag,owner,operation,desc,h.isInterface());
        }
        if(e.subtype(owner,TOAST)) {
            String operation=canonical(TOAST,name,desc,e);
            if("render".equals(operation)&&desc.equals("(Lcom/mojang/blaze3d/vertex/PoseStack;L"+MANAGER+";J)L"+TOAST+"$Visibility;"))throw new UnsupportedOperationException("Source immediate toast rendering requires extraction adaptation: "+owner);
            if(tag!=Opcodes.H_INVOKESTATIC&&((Set.of("width","height","slotCount").contains(Objects.toString(operation,""))&&desc.equals("()I"))||("getToken".equals(operation)&&desc.equals("()Ljava/lang/Object;"))))
                return new Handle(tag,owner,operation.equals("slotCount")?"occcupiedSlotCount":operation,desc,h.isInterface());
        }
        if(e.subtype(owner,MANAGER)) {
            if(name.equals("<init>"))throw new UnsupportedOperationException("Source toast-manager construction requires native options/lifecycle adaptation");
            String operation=canonical(MANAGER,name,desc,e);
            if("render".equals(operation)&&desc.equals("(Lcom/mojang/blaze3d/vertex/PoseStack;)V"))throw new UnsupportedOperationException("Source toast-manager immediate rendering requires extraction adaptation");
            if(tag!=Opcodes.H_INVOKESTATIC&&(("addToast".equals(operation)&&desc.equals("(L"+TOAST+";)V"))||("getToast".equals(operation)&&desc.equals("(Ljava/lang/Class;Ljava/lang/Object;)L"+TOAST+";"))||("clear".equals(operation)&&desc.equals("()V"))||("getMinecraft".equals(operation)&&desc.equals("()Lnet/minecraft/client/Minecraft;"))))return new Handle(tag,owner,operation,desc,h.isInterface());
        }
        return h;
    }
    private static boolean systemDescriptor(String name,String desc,int tag){
        String id="L"+SYS+"$SystemToastIds;";
        if(tag==Opcodes.H_INVOKESTATIC)return switch(name){case "add","addOrUpdate"->desc.equals("(L"+MANAGER+";"+id+COMPONENT+COMPONENT+")V");case "multiline"->desc.equals("(Lnet/minecraft/client/Minecraft;"+id+COMPONENT+COMPONENT+")L"+SYS+";");case "onWorldAccessFailure","onWorldDeleteFailure","onPackCopyFailure"->desc.equals("(Lnet/minecraft/client/Minecraft;Ljava/lang/String;)V");default->false;};
        return switch(name){case "width","height"->desc.equals("()I");case "reset"->desc.equals("("+COMPONENT+COMPONENT+")V");case "getToken"->desc.equals("()"+id)||desc.equals("()Ljava/lang/Object;");default->false;};
    }
    static int rewrite(ClassNode node,Evidence e){
        int count=0;
        for(var method:node.methods){
            var declaration=operation(new Handle((method.access&Opcodes.ACC_STATIC)!=0?Opcodes.H_INVOKESTATIC:Opcodes.H_INVOKEVIRTUAL,node.name,method.name,method.desc,false),e);
            if(!declaration.getName().equals(method.name)){
                if(node.methods.stream().anyMatch(other->other!=method&&other.name.equals(declaration.getName())&&other.desc.equals(method.desc)))throw new IllegalStateException("Source configuration UI member collision: "+node.name);
                method.name=declaration.getName();count++;
            }
            for(var instruction:method.instructions){
                if(instruction instanceof MethodInsnNode call){int tag=switch(call.getOpcode()){case Opcodes.INVOKESTATIC->Opcodes.H_INVOKESTATIC;case Opcodes.INVOKEVIRTUAL->Opcodes.H_INVOKEVIRTUAL;case Opcodes.INVOKESPECIAL->Opcodes.H_INVOKESPECIAL;case Opcodes.INVOKEINTERFACE->Opcodes.H_INVOKEINTERFACE;default->-1;};if(tag<0)continue;var before=new Handle(tag,call.owner,call.name,call.desc,call.itf);var after=operation(before,e);if(!after.equals(before)){call.owner=after.getOwner();call.name=after.getName();call.desc=after.getDesc();call.itf=after.isInterface();if(after.getTag()==Opcodes.H_INVOKESTATIC)call.setOpcode(Opcodes.INVOKESTATIC);count++;}}
                else if(instruction instanceof LdcInsnNode ldc){Object next=constant(ldc.cst,e);if(next!=ldc.cst){ldc.cst=next;count++;}}
                else if(instruction instanceof InvokeDynamicInsnNode indy)for(int i=0;i<indy.bsmArgs.length;i++){Object next=constant(indy.bsmArgs[i],e);if(next!=indy.bsmArgs[i]){indy.bsmArgs[i]=next;count++;}}
            }
        }return count;
    }
    private static Object constant(Object value,Evidence e){
        if(value instanceof Handle handle){var target=operation(handle,e);return target.equals(handle)?value:target;}
        if(value instanceof ConstantDynamic dynamic){Object[] args=new Object[dynamic.getBootstrapMethodArgumentCount()];boolean changed=false;for(int i=0;i<args.length;i++){Object old=dynamic.getBootstrapMethodArgument(i);args[i]=constant(old,e);changed|=args[i]!=old;}return changed?new ConstantDynamic(dynamic.getName(),dynamic.getDescriptor(),dynamic.getBootstrapMethod(),args):value;}return value;
    }
}
