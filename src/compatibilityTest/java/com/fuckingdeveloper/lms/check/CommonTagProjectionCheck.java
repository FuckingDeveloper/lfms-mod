package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.runtime.Target263CommonTagProjection;
import java.io.*;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.*;
import net.minecraft.tags.TagKey;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

final class CommonTagProjectionCheck implements Opcodes {
    static void run() throws Exception {
        var copper=Identifier.parse("forge:ores/copper");
        var item=net.neoforged.neoforge.common.Tags.Items.ORES_COPPER;
        check(Target263CommonTagProjection.create(Registries.ITEM,copper)==item,"native copper tag identity");
        check(Target263CommonTagProjection.create(Registries.BLOCK,copper)==net.neoforged.neoforge.common.Tags.Blocks.ORES_COPPER,"block/item conflated");
        check(Target263CommonTagProjection.create(Registries.FLUID,copper).location().equals(copper),"wrong registry migrated");
        for(String name:List.of("forge:private_custom_tag","other:ores/copper"))
            check(Target263CommonTagProjection.create(Registries.ITEM,Identifier.parse(name)).location().toString().equals(name),"arbitrary namespace/tag migrated");
        check(Target263CommonTagProjection.create(Registries.ITEM,Identifier.parse("forge:ingots/brick"))==net.neoforged.neoforge.common.Tags.Items.BRICKS_NORMAL,"verified path/member rename missing");
        var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);String name="fixture/SourceCommonTag";
        w.visit(V17,ACC_PUBLIC,name,null,"java/lang/Object",null);
        String desc="(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/tags/TagKey;";
        var m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"create",desc,null,null);m.visitCode();m.visitVarInsn(ALOAD,0);m.visitVarInsn(ALOAD,1);
        m.visitMethodInsn(INVOKESTATIC,"net/minecraft/tags/TagKey","m_203882_",desc,false);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"construct",desc,null,null);m.visitCode();
        m.visitTypeInsn(NEW,"net/minecraft/tags/TagKey");m.visitInsn(DUP);m.visitVarInsn(ALOAD,0);m.visitVarInsn(ALOAD,1);
        m.visitMethodInsn(INVOKESPECIAL,"net/minecraft/tags/TagKey","<init>",desc.substring(0,desc.indexOf(')')+1)+"V",false);
        m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"nestedHandle","()Ljava/lang/invoke/MethodHandle;",null,null);m.visitCode();
        var bootstrap=new Handle(H_INVOKESTATIC,Type.getInternalName(CommonTagFamilyCheck.class),"constant","(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;",false);
        m.visitLdcInsn(new ConstantDynamic("outer","Ljava/lang/invoke/MethodHandle;",bootstrap,new ConstantDynamic("inner","Ljava/lang/invoke/MethodHandle;",bootstrap,new Handle(H_NEWINVOKESPECIAL,"net/minecraft/tags/TagKey","<init>",desc.substring(0,desc.indexOf(')')+1)+"V",false))));
        m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"handle","()Ljava/lang/invoke/MethodHandle;",null,null);m.visitCode();
        m.visitLdcInsn(new Handle(H_INVOKESTATIC,"net/minecraft/tags/TagKey","m_203882_",desc,false));m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();w.visitEnd();
        var transformer=new Forge1192LegacyClassTransformer();
        byte[] bytes=transformer.transform(name.replace('/','.'),w.toByteArray()).bytes();
        var type=new AttributeOperationFamilyCheck.Loader().define(name.replace('/','.'),bytes);
        check(type.getMethod("create",net.minecraft.resources.ResourceKey.class,Identifier.class).invoke(null,Registries.ITEM,copper)==item,"executed source factory not projected");
        try {check(((MethodHandle)type.getMethod("handle").invoke(null)).invoke(Registries.ITEM,copper)==item,"source factory handle not projected");}
        catch(Throwable failure){throw new AssertionError(failure);}
        var fresh=(TagKey<?>)type.getMethod("construct",net.minecraft.resources.ResourceKey.class,Identifier.class).invoke(null,Registries.ITEM,copper);
        check(fresh!=item && fresh.equals(item),"source constructor lost fresh identity or canonical key");
        try {var nested=(TagKey<?>)((MethodHandle)type.getMethod("nestedHandle").invoke(null)).invoke(Registries.ITEM,copper);check(nested!=item&&nested.equals(item),"nested constructor handle identity/projection");}
        catch(Throwable failure){throw new AssertionError(failure);}
        var codec=Target263CommonTagProjection.codec(Registries.ITEM);
        check(codec.parse(com.mojang.serialization.JsonOps.INSTANCE,new com.google.gson.JsonPrimitive("forge:ores/copper")).getOrThrow()==item,"flat codec decode");
        check(codec.encodeStart(com.mojang.serialization.JsonOps.INSTANCE,item).getOrThrow().getAsString().equals("c:ores/copper"),"canonical codec encode");
        var hashed=Target263CommonTagProjection.hashedCodec(Registries.ITEM);
        check(hashed.parse(com.mojang.serialization.JsonOps.INSTANCE,new com.google.gson.JsonPrimitive("#forge:ores/copper")).getOrThrow()==item,"hashed codec decode");
        check(hashed.parse(com.mojang.serialization.JsonOps.INSTANCE,new com.google.gson.JsonPrimitive("forge:ores/copper")).error().isPresent(),"invalid unhashed value accepted");
        resources();
        Path artifact=Path.of("legacy-mods/IC2Classic-1.19.2-2.1.3.4.jar");
        if(Files.exists(artifact))try(var jar=new JarFile(artifact.toFile())) {
            String owner="ic2/core/platform/registries/IC2Tags";
            byte[] original;try(var in=jar.getInputStream(jar.getJarEntry(owner+".class"))){original=in.readAllBytes();}
            var node=new ClassNode();new ClassReader(transformer.transform(owner.replace('/','.'),original).bytes()).accept(node,0);
            int factories=0;
            for(var method:node.methods)for(var insn:method.instructions)if(insn instanceof MethodInsnNode call){
                check(!(call.owner.equals("net/minecraft/tags/TagKey")&&call.name.equals("create")),"source tag factory escaped");
                if(call.owner.equals("com/fuckingdeveloper/lms/runtime/Target263CommonTagProjection"))factories++;
            }
            check(factories>0,"immutable tag consumer not covered");
        }
        System.out.println("PASS: canonical common tag native identities, dynamic source factory/handle and original consumer, registry/unknown boundaries, resource paths/references/native precedence and live relisting");
    }
    private static void resources() throws Exception {
        var data=new LinkedHashMap<Identifier,String>();
        data.put(Identifier.parse("forge:tags/items/ores/copper.json"),"{\"replace\":false,\"values\":[\"minecraft:copper_ore\",\"#forge:ores/iron\",{\"id\":\"#forge:ores/gold\",\"required\":false},\"#forge:custom\"]}");
        data.put(Identifier.parse("forge:tags/items/ingots/brick.json"),"{\"values\":[\"minecraft:brick\"]}");
        data.put(Identifier.parse("forge:tags/items/custom.json"),"{\"values\":[\"minecraft:stick\"]}");
        PackResources source=new PackResources(){
            public PackLocationInfo location(){return new PackLocationInfo("common-probe",net.minecraft.network.chat.Component.literal("probe"),net.minecraft.server.packs.repository.PackSource.BUILT_IN,Optional.empty());}
            public net.minecraft.server.packs.resources.IoSupplier<InputStream> getRootResource(String... path){return null;}
            public <T>T getMetadataSection(net.minecraft.server.packs.metadata.MetadataSectionType<T> type){return null;}
            public Set<String>getNamespaces(PackType type){return Set.of("forge");}
            public void close(){}
            public net.minecraft.server.packs.resources.IoSupplier<InputStream> getResource(PackType type,Identifier id){if(!data.containsKey(id))throw new AssertionError("unlisted source lookup");return ()->new ByteArrayInputStream(data.get(id).getBytes(StandardCharsets.UTF_8));}
            public void listResources(PackType type,String ns,String dir,ResourceOutput out){data.forEach((id,text)->{if(id.getNamespace().equals(ns)&&id.getPath().startsWith(dir))out.accept(id,getResource(type,id));});}
        };
        var pack=new com.fuckingdeveloper.lms.runtime.Target263LegacyTagResources(source);
        var key=Identifier.parse("c:tags/item/ores/copper.json");
        check(pack.getNamespaces(PackType.SERVER_DATA).contains("c"),"canonical namespace not discovered");
        String text=read(pack.getResource(PackType.SERVER_DATA,key));
        check(text.contains("#c:ores/iron")&&text.contains("#c:ores/gold")&&text.contains("#forge:custom")&&text.contains("\"required\":false")&&text.contains("\"replace\":false"),"nested refs/flags changed");
        check(pack.getResource(PackType.SERVER_DATA,Identifier.parse("c:tags/item/bricks/normal.json"))!=null,"cross-directory rename direct lookup");
        check(pack.getResource(PackType.SERVER_DATA,Identifier.parse("forge:tags/item/custom.json"))!=null,"custom definition lost");
        data.put(key,"{\"values\":[\"minecraft:gold_ingot\"]}");
        check(read(pack.getResource(PackType.SERVER_DATA,key)).equals(data.get(key)),"explicit native resource did not win");
        data.remove(key);data.put(Identifier.parse("forge:tags/items/ores/copper.json"),"{\"values\":[\"minecraft:iron_ingot\"]}");
        check(read(pack.getResource(PackType.SERVER_DATA,key)).contains("minecraft:iron_ingot"),"source declarations cached across data phases");
        check(pack.getResource(PackType.SERVER_DATA,Identifier.parse("c:tags/item/absent.json"))==null,"membership fabricated");
    }
    private static String read(net.minecraft.server.packs.resources.IoSupplier<InputStream> supplier)throws Exception{check(supplier!=null,"missing resource");try(var input=supplier.get()){return new String(input.readAllBytes(),StandardCharsets.UTF_8);}}
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}
