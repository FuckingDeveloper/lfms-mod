package com.fuckingdeveloper.lms.profile.forge1192;

import java.util.*;
import java.util.function.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import static org.objectweb.asm.Opcodes.*;

/** Exact removed source atlas-holder callbacks; no model or fixture identities. */
final class Forge1192TextureAtlasHolderRewriter {
    static final String SOURCE="net/minecraft/client/resources/TextureAtlasHolder";
    private static final Map<String,String> METHODS=Map.of(
            "m_7535_()Ljava/util/stream/Stream;","getResourcesToLoad",
            "m_118901_(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;","getSprite",
            "m_5944_(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Lnet/minecraft/client/renderer/texture/TextureAtlas$Preparations;","prepare",
            "m_5944_(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Ljava/lang/Object;","prepare",
            "m_5787_(Lnet/minecraft/client/renderer/texture/TextureAtlas$Preparations;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V","apply",
            "m_5787_(Ljava/lang/Object;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V","apply");
    static int rewrite(ClassNode node,BiPredicate<String,String> subtype){
        int count=0;
        if(subtype.test(node.name,SOURCE))for(var method:node.methods)if((method.access&ACC_STATIC)==0){String name=METHODS.get(method.name+method.desc);if(name!=null){method.name=name;count++;}}
        for(var method:node.methods)for(var instruction:method.instructions){
            if(instruction instanceof MethodInsnNode call&&call.getOpcode()!=INVOKESTATIC&&subtype.test(call.owner,SOURCE)){String name=METHODS.get(call.name+call.desc);if(name!=null){call.name=name;count++;}}
            if(instruction instanceof LdcInsnNode ldc){Object next=constant(ldc.cst,subtype);if(next!=ldc.cst){ldc.cst=next;count++;}}
            if(instruction instanceof InvokeDynamicInsnNode indy)for(int i=0;i<indy.bsmArgs.length;i++){Object next=constant(indy.bsmArgs[i],subtype);if(next!=indy.bsmArgs[i]){indy.bsmArgs[i]=next;count++;}}
        }
        return count;
    }
    private static Object constant(Object value,BiPredicate<String,String> subtype){
        if(value instanceof Handle h&&Set.of(H_INVOKEVIRTUAL,H_INVOKESPECIAL,H_INVOKEINTERFACE).contains(h.getTag())&&subtype.test(h.getOwner(),SOURCE)){
            String name=METHODS.get(h.getName()+h.getDesc());if(name!=null)return new Handle(h.getTag(),h.getOwner(),name,h.getDesc(),h.isInterface());
        }
        if(value instanceof ConstantDynamic c){Object[] args=new Object[c.getBootstrapMethodArgumentCount()];boolean changed=false;for(int i=0;i<args.length;i++){Object old=c.getBootstrapMethodArgument(i);args[i]=constant(old,subtype);changed|=old!=args[i];}if(changed)return new ConstantDynamic(c.getName(),c.getDescriptor(),c.getBootstrapMethod(),args);}
        return value;
    }
}
