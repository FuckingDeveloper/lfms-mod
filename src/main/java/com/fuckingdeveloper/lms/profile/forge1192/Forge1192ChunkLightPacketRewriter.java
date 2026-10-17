package com.fuckingdeveloper.lms.profile.forge1192;

import java.util.Map;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Original snapshot operands over the explicitly accepted native light solver. */
final class Forge1192ChunkLightPacketRewriter {
    private static final String PREFIX="net/minecraft/network/protocol/game/";
    private static final String CHUNK=PREFIX+"ClientboundLevelChunkWithLightPacket",LIGHT=PREFIX+"ClientboundLightUpdatePacket",DATA=PREFIX+"ClientboundLightUpdatePacketData";
    private static final String TAIL="Lnet/minecraft/world/level/lighting/LevelLightEngine;Ljava/util/BitSet;Ljava/util/BitSet;Z)V";
    private static final Map<String,String> CONSTRUCTORS=Map.of(
            CHUNK,"(Lnet/minecraft/world/level/chunk/LevelChunk;"+TAIL,
            LIGHT,"(Lnet/minecraft/world/level/ChunkPos;"+TAIL,
            DATA,"(Lnet/minecraft/world/level/ChunkPos;"+TAIL);
    private static final Map<String,String> READS=Map.ofEntries(
            Map.entry("getX()I","x"),Map.entry("getZ()I","z"),
            Map.entry("getChunkData()L"+PREFIX+"ClientboundLevelChunkPacketData;","chunkData"),
            Map.entry("getLightData()L"+DATA+";","lightData"),
            Map.entry("getSkyYMask()Ljava/util/BitSet;","skyYMask"),
            Map.entry("getEmptySkyYMask()Ljava/util/BitSet;","emptySkyYMask"),
            Map.entry("getSkyUpdates()Ljava/util/List;","skyUpdates"),
            Map.entry("getBlockYMask()Ljava/util/BitSet;","blockYMask"),
            Map.entry("getEmptyBlockYMask()Ljava/util/BitSet;","emptyBlockYMask"),
            Map.entry("getBlockUpdates()Ljava/util/List;","blockUpdates"));
    interface Evidence { String canonical(String owner,String name,String desc); }
    static int rewrite(ClassNode node,Evidence evidence) {
        if(CONSTRUCTORS.containsKey(node.superName))
            throw new UnsupportedOperationException("Source chunk/light packet subclass cannot extend a native final record: "+node.name);
        int count=0;
        for(var method:node.methods)for(var insn:method.instructions.toArray()) {
            if(insn instanceof MethodInsnNode call) {
                if(call.getOpcode()==Opcodes.INVOKESPECIAL&&call.name.equals("<init>")&&call.desc.equals(CONSTRUCTORS.get(call.owner))) {
                    // The last operand is the source boolean. Keep NEW/DUP and every
                    // original argument evaluation; never touch uninitialized this.
                    method.instructions.insertBefore(call,new InsnNode(Opcodes.POP));
                    call.desc=call.desc.substring(0,call.desc.length()-3)+")V";count++;
                } else if(call.getOpcode()==Opcodes.INVOKEVIRTUAL) {
                    String name=read(call.owner,call.name,call.desc,evidence);
                    if(name!=null){call.name=name;count++;}
                }
            } else if(insn instanceof LdcInsnNode ldc) {
                Object next=constant(ldc.cst,evidence);if(next!=ldc.cst){ldc.cst=next;count++;}
            } else if(insn instanceof InvokeDynamicInsnNode indy) {
                for(int i=0;i<indy.bsmArgs.length;i++){Object next=constant(indy.bsmArgs[i],evidence);if(next!=indy.bsmArgs[i]){indy.bsmArgs[i]=next;count++;}}
            }
        }
        return count;
    }
    private static String read(String owner,String name,String desc,Evidence evidence) {
        if(!CONSTRUCTORS.containsKey(owner))return null;
        String source=name.startsWith("m_")?evidence.canonical(owner,name,desc):name;
        if(source==null)return null;
        // Require the member to belong to this exact packet, not another record.
        if(owner.equals(DATA) ? !(source.contains("Mask")||source.equals("getSkyUpdates")||source.equals("getBlockUpdates"))
                : !(source.equals("getX")||source.equals("getZ")||source.equals("getLightData")||owner.equals(CHUNK)&&source.equals("getChunkData")))return null;
        return READS.get(source+desc);
    }
    private static Object constant(Object value,Evidence evidence) {
        if(value instanceof Handle h) {
            if(h.getTag()==Opcodes.H_NEWINVOKESPECIAL&&h.getName().equals("<init>")&&h.getDesc().equals(CONSTRUCTORS.get(h.getOwner()))) {
                String factory=h.getOwner().equals(CHUNK)?"chunk":h.getOwner().equals(LIGHT)?"light":"data";
                return new Handle(Opcodes.H_INVOKESTATIC,com.fuckingdeveloper.lms.target.InstalledTargetAdapters.chunkLightPacketsBridge(),factory,
                        h.getDesc().substring(0,h.getDesc().length()-1)+"L"+h.getOwner()+";",false);
            }
            if(h.getTag()==Opcodes.H_INVOKEVIRTUAL) {
                String name=read(h.getOwner(),h.getName(),h.getDesc(),evidence);
                if(name!=null)return new Handle(h.getTag(),h.getOwner(),name,h.getDesc(),false);
            }
        }
        if(value instanceof ConstantDynamic d) {
            Object[] args=new Object[d.getBootstrapMethodArgumentCount()];boolean changed=false;
            for(int i=0;i<args.length;i++){Object old=d.getBootstrapMethodArgument(i);args[i]=constant(old,evidence);changed|=old!=args[i];}
            if(changed)return new ConstantDynamic(d.getName(),d.getDescriptor(),d.getBootstrapMethod(),args);
        }
        return value;
    }
}
