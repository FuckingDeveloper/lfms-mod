package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.banner.*;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.*;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.registry.*;
import com.fuckingdeveloper.lms.target.neoforge263.Target263BannerPatternPack;
import java.lang.reflect.Proxy;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.neoforged.bus.api.*;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforgespi.language.IModInfo;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;

/** Independent immutable source binary and actual native codec; no game registry mutation. */
final class SourceBannerPatternCheck {
    public static final class Fixture {
        public static LegacyBannerPattern create(){return new LegacyBannerPattern("original_hash");}
        public static String hash(LegacyBannerPattern value){return value.getHashname();}
        public static Identifier texture(ResourceKey<?> key){return LegacyBannerPattern.location(key,true);}
        public static ResourceKey<?> key(){return Registries.BANNER_PATTERN;}
    }
    static void run() throws Exception {
        String name="probe/BannerConsumer",old=Type.getInternalName(Fixture.class);
        byte[] bytes;try(var in=SourceBannerPatternCheck.class.getResourceAsStream("SourceBannerPatternCheck$Fixture.class")){bytes=in.readAllBytes();}
        var writer=new ClassWriter(0);new ClassReader(bytes).accept(new ClassRemapper(writer,new Remapper(){
            public String map(String owner){if(owner.equals(old))return name;if(owner.equals("net/minecraft/resources/Identifier"))return "net/minecraft/resources/ResourceLocation";if(owner.equals(Type.getInternalName(LegacyBannerPattern.class)))return "net/minecraft/world/level/block/entity/BannerPattern";return owner;}
            public String mapMethodName(String owner,String member,String descriptor){if(owner.equals(Type.getInternalName(LegacyBannerPattern.class)))return switch(member){case "getHashname"->"m_58579_";case "location"->"m_222697_";default->member;};return member;}
        }) {
            @Override public MethodVisitor visitMethod(int access,String method,String desc,String signature,String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM9,super.visitMethod(access,method,desc,signature,exceptions)) {
                    @Override public void visitFieldInsn(int opcode,String owner,String field,String descriptor){
                        if(owner.equals("net/minecraft/core/registries/Registries")&&field.equals("BANNER_PATTERN"))super.visitFieldInsn(opcode,"net/minecraft/core/Registry","f_235735_",descriptor);
                        else super.visitFieldInsn(opcode,owner,field,descriptor);
                    }
                };
            }
        },0);
        var path=Files.createTempFile("lms-source-banner", ".jar");
        try {
            try(var jar=new JarOutputStream(Files.newOutputStream(path))){jar.putNextEntry(new JarEntry(name+".class"));jar.write(writer.toByteArray());jar.closeEntry();jar.putNextEntry(new JarEntry("META-INF/mods.toml"));jar.write("modLoader=\"javafml\"\nloaderVersion=\"[43,)\"\nlicense=\"test\"\n[[mods]]\nmodId=\"banner_owner\"\nversion=\"1\"\n".getBytes());jar.closeEntry();}
            byte[] original=Files.readAllBytes(path);
            var bus=BusBuilder.builder().build();bus.start();
            var info=(IModInfo)Proxy.newProxyInstance(SourceBannerPatternCheck.class.getClassLoader(),new Class[]{IModInfo.class},(p,m,a)->{if(m.getName().equals("getModId"))return "banner_host";throw new AssertionError(m);});
            var host=new ModContainer(info){public IEventBus getEventBus(){return bus;}};
            try(var session=new Forge1192RuntimeSession("banner_owner",path,SourceBannerPatternCheck.class.getClassLoader(),List.of(name.replace('/','.')));
                var loader=new ManagedLegacyClassLoader(path,SourceBannerPatternCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
                session.bindModEventBus(bus,host);var type=loader.linkOwnedClass(name.replace('/','.'));
                var value=(LegacyBannerPattern)type.getMethod("create").invoke(null);
                check(type.getMethod("hash",LegacyBannerPattern.class).invoke(null,value).equals("original_hash"),"source hash intent changed");
                check(type.getMethod("key").invoke(null)==Registries.BANNER_PATTERN,"source registry key identity changed");
                var id=Identifier.fromNamespaceAndPath("banner_owner","pattern");
                check(type.getMethod("texture",ResourceKey.class).invoke(null,ResourceKey.create(Registries.BANNER_PATTERN,id)).equals(Identifier.parse("banner_owner:entity/banner/pattern")),"source texture identity changed");
                int[] calls={0},orphan={0};LegacyRegistryObject holder;
                try(var scope=Forge1192LifecycleBridge.enter("banner_owner",bus,host)) {
                    var register=LegacyDeferredRegister.create(Registries.BANNER_PATTERN,"banner_owner");
                    var tag=register.createTagKey("pattern_item/pattern");check(tag.registry()==Registries.BANNER_PATTERN&&tag.location().equals(Identifier.parse("banner_owner:pattern_item/pattern")),"tag identity changed");
                    holder=register.register("pattern",()->{check(Forge1192LifecycleBridge.requireActive().modId().equals("banner_owner"),"factory owner lost");calls[0]++;return value;});
                    check(!holder.isPresent()&&calls[0]==0,"factory ran early");
                    try{holder.get();throw new AssertionError("early source value fabricated");}catch(IllegalStateException expected){}
                    try{register.register("pattern",()->value);throw new AssertionError("duplicate accepted");}catch(IllegalStateException expected){}
                    register.register(bus);
                    var unattached=LegacyDeferredRegister.create(Registries.BANNER_PATTERN,"banner_owner");unattached.register("orphan",()->{orphan[0]++;return value;});
                }
                LegacyBannerPatterns.materialize();check(calls[0]==1&&orphan[0]==0&&holder.isPresent()&&holder.get()==value,"materialization order/identity/attachment changed");
                var resource=Target263BannerPatternPack.resources().get(Identifier.parse("banner_owner:banner_pattern/pattern.json"));
                var json=com.google.gson.JsonParser.parseString(new String(resource,java.nio.charset.StandardCharsets.UTF_8));
                var nativeValue=BannerPattern.DIRECT_CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE,json).getOrThrow();
                check(nativeValue.assetId().equals(id)&&nativeValue.translationKey().equals("block.minecraft.banner.banner_owner.pattern"),"native codec/asset/translation projection changed");
                try{LegacyBannerPatterns.materialize();throw new AssertionError("factory replayed");}catch(IllegalStateException expected){}
                try{LegacyBannerPatterns.declare("banner_owner",Identifier.parse("banner_owner:late"),()->value);throw new AssertionError("late declaration accepted");}catch(IllegalStateException expected){}
                check(Arrays.equals(original,Files.readAllBytes(path)),"original source binary modified");
            }
        }finally{Files.deleteIfExists(path);}
        System.out.println("PASS: immutable source banner constructor/hash/key/texture, lazy attached declaration owner/once/identity, early/duplicate/late/replay rejection, native codec resource; live registry/tag/loom acceptance remains separate");
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
