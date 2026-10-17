package com.fuckingdeveloper.lms.profile.forge1192;

import com.fuckingdeveloper.lms.transform.SemanticInvocationRewriter;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import static org.objectweb.asm.Opcodes.*;

/** Verified Forge client-factory and source packet callbacks; native packet data is never fabricated. */
final class Forge1192EntitySpawnRewriter {
    private static final String ENTITY="net/minecraft/world/entity/Entity",BUILDER="net/minecraft/world/entity/EntityType$Builder";
    private static final String PACKET="()Lnet/minecraft/network/protocol/Packet;";
    interface Evidence {boolean entity(String owner);String method(String name,String descriptor);}
    static int rewrite(ClassNode node,Evidence evidence,String bridge,String callbackInterface,SemanticInvocationRewriter invocations) {
        int count=0;
        if(evidence.entity(node.name))for(var method:java.util.List.copyOf(node.methods)) {
            if((method.access&ACC_STATIC)!=0||!packet(method.name,method.desc,evidence))continue;
            if(node.methods.stream().anyMatch(m->m!=method&&m.name.equals("lms$sourceSpawnPacket")&&m.desc.equals(PACKET)))throw new IllegalStateException("Source spawn packet callback collision");
            method.name="lms$sourceSpawnPacket";
            if(!node.interfaces.contains(callbackInterface))node.interfaces.add(callbackInterface);
            var nativeMethod=new MethodNode(ACC_PUBLIC|ACC_SYNTHETIC,"getAddEntityPacket","(Lnet/minecraft/server/level/ServerEntity;)Lnet/minecraft/network/protocol/Packet;",null,null);
            nativeMethod.instructions.add(new VarInsnNode(ALOAD,0));nativeMethod.instructions.add(new VarInsnNode(ALOAD,1));
            nativeMethod.instructions.add(new MethodInsnNode(INVOKESTATIC,bridge,"spawnPacket","(L"+ENTITY+";Lnet/minecraft/server/level/ServerEntity;)Lnet/minecraft/network/protocol/Packet;",false));
            nativeMethod.instructions.add(new InsnNode(ARETURN));nativeMethod.maxStack=2;nativeMethod.maxLocals=2;node.methods.add(nativeMethod);count++;
        }
        for(var method:node.methods)for(var insn:method.instructions)if(insn instanceof MethodInsnNode call && call.getOpcode()==INVOKESPECIAL && !call.owner.equals(ENTITY)
                && evidence.entity(call.owner)&&packet(call.name,call.desc,evidence)){call.name="lms$sourceSpawnPacket";count++;}
        count+=invocations.rewrite(node,(opcode,owner,name,desc)->{
            if(opcode==INVOKEVIRTUAL&&owner.equals(BUILDER)&&name.equals("setCustomClientFactory")&&desc.equals("(Ljava/util/function/BiFunction;)L"+BUILDER+";"))
                return new Handle(H_INVOKESTATIC,bridge,name,"(L"+BUILDER+";Ljava/util/function/BiFunction;)L"+BUILDER+";",false);
            if(opcode==INVOKESTATIC&&owner.equals("net/minecraftforge/network/NetworkHooks")&&name.equals("getEntitySpawningPacket")&&desc.equals("(L"+ENTITY+";)Lnet/minecraft/network/protocol/Packet;"))
                return new Handle(H_INVOKESTATIC,bridge,name,desc,false);
            if(opcode==INVOKEVIRTUAL&&evidence.entity(owner)&&packet(name,desc,evidence))
                return new Handle(H_INVOKESTATIC,bridge,"sourcePacket","(L"+ENTITY+";)Lnet/minecraft/network/protocol/Packet;",false);
            return null;
        });
        return count;
    }
    private static boolean packet(String name,String descriptor,Evidence evidence){return descriptor.equals(PACKET)&&"getAddEntityPacket".equals(name.startsWith("m_")?evidence.method(name,descriptor):name);}
}
