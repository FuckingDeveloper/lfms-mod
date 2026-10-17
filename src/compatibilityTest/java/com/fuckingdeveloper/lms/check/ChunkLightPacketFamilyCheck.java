package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import java.lang.reflect.Proxy;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import net.minecraft.core.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.*;
import static org.objectweb.asm.Opcodes.*;

final class ChunkLightPacketFamilyCheck {
    static final String PREFIX="net/minecraft/network/protocol/game/", CHUNK=PREFIX+"ClientboundLevelChunkWithLightPacket", LIGHT=PREFIX+"ClientboundLightUpdatePacket", DATA=PREFIX+"ClientboundLightUpdatePacketData";
    static final String TAIL="Lnet/minecraft/world/level/lighting/LevelLightEngine;Ljava/util/BitSet;Ljava/util/BitSet;Z)V";
    static class ProbeChunk extends LevelChunk {
        int snapshots;
        ProbeChunk(){super(null,new ChunkPos(-2,3));}
        @Override public ChunkPos getPos(){return new ChunkPos(-2,3);}
        @Override public Collection<Map.Entry<Heightmap.Types,Heightmap>> getHeightmaps(){snapshots++;return List.of();}
        @Override public LevelChunkSection[] getSections(){return new LevelChunkSection[0];}
        @Override public Map<BlockPos,BlockEntity> getBlockEntities(){return Map.of();}
    }
    static class ProbeLight extends LevelLightEngine {
        DataLayer sky,block;
        ProbeLight(){super(null,false,false);}
        @Override public int getLightSectionCount(){return 3;}
        @Override public int getMinLightSection(){return -1;}
        @Override public LayerLightEventListener getLayerListener(LightLayer layer){
            return (LayerLightEventListener)Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{LayerLightEventListener.class},(p,m,a)->{
                if(!m.getName().equals("getDataLayerData"))throw new AssertionError("Unexpected light callback: "+m);
                int y=((SectionPos)a[0]).y();
                return layer==LightLayer.SKY?(y==-1?sky:y==0?new DataLayer():null):(y==0?block:null);
            });
        }
    }
    record Getter(String owner,String srg,String desc,String target) {}
    static final List<Getter> GETTERS=List.of(
            new Getter(CHUNK,"m_195717_","()I","x"),new Getter(CHUNK,"m_195718_","()I","z"),
            new Getter(CHUNK,"m_195719_","()L"+PREFIX+"ClientboundLevelChunkPacketData;","chunkData"),new Getter(CHUNK,"m_195720_","()L"+DATA+";","lightData"),
            new Getter(LIGHT,"m_132349_","()I","x"),new Getter(LIGHT,"m_132352_","()I","z"),new Getter(LIGHT,"m_195722_","()L"+DATA+";","lightData"),
            new Getter(DATA,"m_195740_","()Ljava/util/BitSet;","skyYMask"),new Getter(DATA,"m_195751_","()Ljava/util/BitSet;","emptySkyYMask"),new Getter(DATA,"m_195754_","()Ljava/util/List;","skyUpdates"),
            new Getter(DATA,"m_195757_","()Ljava/util/BitSet;","blockYMask"),new Getter(DATA,"m_195758_","()Ljava/util/BitSet;","emptyBlockYMask"),new Getter(DATA,"m_195759_","()Ljava/util/List;","blockUpdates"));
    static void run() throws Exception {
        var caller=new ClassNode();caller.version=V17;caller.access=ACC_PUBLIC;caller.name="probe/ChunkLightCalls";caller.superName="java/lang/Object";
        caller.fields.add(new FieldNode(ACC_PUBLIC|ACC_STATIC,"evaluations","I",null,null));
        var trust=new MethodNode(ACC_PUBLIC|ACC_STATIC,"trust","(Z)Z",null,null);
        trust.instructions.add(new FieldInsnNode(GETSTATIC,caller.name,"evaluations","I"));trust.instructions.add(new InsnNode(ICONST_1));trust.instructions.add(new InsnNode(IADD));trust.instructions.add(new FieldInsnNode(PUTSTATIC,caller.name,"evaluations","I"));trust.instructions.add(new VarInsnNode(ILOAD,0));trust.instructions.add(new InsnNode(IRETURN));caller.methods.add(trust);
        for(String owner:List.of(CHUNK,LIGHT,DATA)) {
            String first=owner.equals(CHUNK)?"Lnet/minecraft/world/level/chunk/LevelChunk;":"Lnet/minecraft/world/level/ChunkPos;", ctor="("+first+TAIL;
            String name=owner.substring(owner.lastIndexOf('/')+1),desc=ctor.substring(0,ctor.length()-1)+"L"+owner+";";
            var make=new MethodNode(ACC_PUBLIC|ACC_STATIC,name,desc,null,null);
            make.instructions.add(new TypeInsnNode(NEW,owner));make.instructions.add(new InsnNode(DUP));
            for(int i=0;i<4;i++)make.instructions.add(new VarInsnNode(ALOAD,i));make.instructions.add(new VarInsnNode(ILOAD,4));
            make.instructions.add(new MethodInsnNode(INVOKESTATIC,caller.name,"trust","(Z)Z",false));
            make.instructions.add(new MethodInsnNode(INVOKESPECIAL,owner,"<init>",ctor,false));make.instructions.add(new InsnNode(ARETURN));caller.methods.add(make);
            var handle=new MethodNode(ACC_PUBLIC|ACC_STATIC,name+"Handle","()Ljava/lang/invoke/MethodHandle;",null,null);
            handle.instructions.add(new LdcInsnNode(new Handle(H_NEWINVOKESPECIAL,owner,"<init>",ctor,false)));handle.instructions.add(new InsnNode(ARETURN));caller.methods.add(handle);
        }
        for(int i=0;i<GETTERS.size();i++) {
            var g=GETTERS.get(i);var read=new MethodNode(ACC_PUBLIC|ACC_STATIC,"read"+i,"(L"+g.owner+";"+g.desc.substring(1),null,null);
            read.instructions.add(new VarInsnNode(ALOAD,0));read.instructions.add(new MethodInsnNode(INVOKEVIRTUAL,g.owner,g.srg,g.desc,false));read.instructions.add(new InsnNode(Type.getReturnType(g.desc).getOpcode(IRETURN)));caller.methods.add(read);
            var handle=new MethodNode(ACC_PUBLIC|ACC_STATIC,"readHandle"+i,"()Ljava/lang/invoke/MethodHandle;",null,null);
            handle.instructions.add(new LdcInsnNode(new Handle(H_INVOKEVIRTUAL,g.owner,g.srg,g.desc,false)));handle.instructions.add(new InsnNode(ARETURN));caller.methods.add(handle);
        }
        var removed=new MethodNode(ACC_PUBLIC|ACC_STATIC,"unproven","(L"+DATA+";)Z",null,null);
        removed.instructions.add(new VarInsnNode(ALOAD,0));removed.instructions.add(new MethodInsnNode(INVOKEVIRTUAL,DATA,"m_195760_","()Z",false));removed.instructions.add(new InsnNode(IRETURN));caller.methods.add(removed);
        var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);caller.accept(writer);byte[] bytes=writer.toByteArray();
        var transformed=new ClassNode();new ClassReader(new Forge1192LegacyClassTransformer().transform("probe.ChunkLightCalls",bytes).bytes()).accept(transformed,0);
        check(transformed.methods.stream().filter(m->m.name.equals("unproven")).flatMap(m->Arrays.stream(m.instructions.toArray())).anyMatch(x->x instanceof MethodInsnNode c&&c.name.equals("m_195760_")),"removed flag fabricated");
        var f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);var unsafe=(sun.misc.Unsafe)f.get(null);
        var chunk=(ProbeChunk)unsafe.allocateInstance(ProbeChunk.class);var engine=(ProbeLight)unsafe.allocateInstance(ProbeLight.class);
        var sky=new BitSet();sky.set(0);var block=new BitSet();block.set(1);
        Path path=Files.createTempFile("lms-chunk-light-", ".jar");
        try {
            try(var jar=new JarOutputStream(Files.newOutputStream(path))){jar.putNextEntry(new JarEntry(caller.name+".class"));jar.write(bytes);jar.closeEntry();}
            byte[] original=Files.readAllBytes(path);
            try(var loader=new ManagedLegacyClassLoader(path,ChunkLightPacketFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
                var type=loader.linkOwnedClass("probe.ChunkLightCalls");int expectedEvaluations=0;
                for(boolean edges:List.of(true,false))for(String owner:List.of(CHUNK,LIGHT,DATA)) {
                    engine.sky=new DataLayer(new byte[2048]);engine.sky.getData()[0]=0x12;
                    engine.block=new DataLayer(new byte[2048]);engine.block.getData()[0]=0x34;
                    String name=owner.substring(owner.lastIndexOf('/')+1);Class<?> first=owner.equals(CHUNK)?LevelChunk.class:ChunkPos.class;
                    Object arg=owner.equals(CHUNK)?chunk:new ChunkPos(-2,3);
                    Object packet=type.getMethod(name,first,LevelLightEngine.class,BitSet.class,BitSet.class,boolean.class).invoke(null,arg,engine,sky,block,edges);
                    check(type.getField("evaluations").getInt(null)==++expectedEvaluations,"boolean operand skipped/duplicated");
                    var data=packet instanceof ClientboundLevelChunkWithLightPacket c?c.lightData():packet instanceof ClientboundLightUpdatePacket l?l.lightData():(ClientboundLightUpdatePacketData)packet;
                    check(data.skyYMask().equals(sky)&&data.blockYMask().equals(block)&&data.skyUpdates().size()==1&&data.blockUpdates().size()==1,"native filters/layers lost");
                    engine.sky.getData()[0]=0;engine.block.getData()[0]=0;
                    check(data.skyUpdates().getFirst()[0]==0x12&&data.blockUpdates().getFirst()[0]==0x34,"native light snapshot aliases source data");
                    for(int i=0;i<GETTERS.size();i++)if(GETTERS.get(i).owner.equals(owner)) {
                        var g=GETTERS.get(i);Class<?> ownerType=Class.forName(owner.replace('/','.'));Object want=ownerType.getMethod(g.target).invoke(packet);
                        check(Objects.equals(type.getMethod("read"+i,ownerType).invoke(null,packet),want),"source getter changed");
                        var h=(java.lang.invoke.MethodHandle)type.getMethod("readHandle"+i).invoke(null);
                        try{check(Objects.equals(h.invoke(packet),want),"source getter handle changed");}catch(Throwable e){throw new AssertionError(e);}
                    }
                    var ctor=(java.lang.invoke.MethodHandle)type.getMethod(name+"Handle").invoke(null);
                    try{check(ctor.invokeWithArguments(arg,engine,sky,block,edges).getClass()==packet.getClass(),"constructor reference type lost");}catch(Throwable e){throw new AssertionError(e);}
                    var buffer=io.netty.buffer.Unpooled.buffer();
                    try {
                        if(packet instanceof ClientboundLevelChunkWithLightPacket c) {
                            var registryBuffer=new RegistryFriendlyByteBuf(buffer,RegistryAccess.EMPTY);
                            ClientboundLevelChunkWithLightPacket.STREAM_CODEC.encode(registryBuffer,c);
                            var decoded=ClientboundLevelChunkWithLightPacket.STREAM_CODEC.decode(registryBuffer);
                            check(decoded.x()==-2&&decoded.z()==3&&decoded.lightData().skyYMask().equals(sky),"native chunk codec snapshot lost");
                        } else if(packet instanceof ClientboundLightUpdatePacket l) {
                            ClientboundLightUpdatePacket.STREAM_CODEC.encode(buffer,l);var decoded=ClientboundLightUpdatePacket.STREAM_CODEC.decode(buffer);
                            check(decoded.x()==-2&&decoded.z()==3&&decoded.lightData().blockYMask().equals(block),"native light codec lost");
                        } else {
                            ClientboundLightUpdatePacketData.STREAM_CODEC.encode(buffer,data);var decoded=ClientboundLightUpdatePacketData.STREAM_CODEC.decode(buffer);
                            check(decoded.skyUpdates().getFirst()[0]==0x12&&decoded.blockUpdates().getFirst()[0]==0x34,"native layer codec lost");
                        }
                    } finally {buffer.release();}
                }
                check(chunk.snapshots==4,"native chunk snapshot repeated/skipped");
                var all=com.fuckingdeveloper.lms.target.neoforge263.Target263SourceChunkLightPackets.data(new ChunkPos(-2,3),engine,null,null,true);
                check(all.emptySkyYMask().get(1),"native empty-light mask lost");
            }
            check(Arrays.equals(original,Files.readAllBytes(path)),"source packet artifact changed");
        } finally {Files.deleteIfExists(path);}
        System.out.println("PASS: three source snapshot constructors/references, thirteen getters/handles, true/false operand effects, native masks/byte isolation/empty layers and target codec roundtrips; real chunk refresh requires runtime");
    }
    private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
}
