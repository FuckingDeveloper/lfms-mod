package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import java.lang.invoke.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import org.objectweb.asm.*;

public final class RegistryQueryFamilyCheck {
    private record Query(String name,String descriptor,Class<?>[] parameters,Object[] present,Object[] absent,Object expected,Object missing) {}
    @SuppressWarnings({"rawtypes","unchecked"})
    static void run() throws Exception {
        String id="Lnet/minecraft/resources/ResourceLocation;",key="Lnet/minecraft/resources/ResourceKey;",object="Ljava/lang/Object;",optional="Ljava/util/Optional;";
        var stone=Identifier.parse("minecraft:stone");var unknown=Identifier.parse("probe:absent");var air=Identifier.parse("minecraft:air");
        var stoneKey=ResourceKey.create(Registries.BLOCK,stone);var unknownKey=ResourceKey.create(Registries.BLOCK,unknown);var other=new Object();
        var registry=BuiltInRegistries.BLOCK;
        var queries=List.of(
                new Query("get","("+id+")"+object,new Class[]{Identifier.class},new Object[]{stone},new Object[]{unknown},Blocks.STONE,Blocks.AIR),
                new Query("get","("+key+")"+object,new Class[]{ResourceKey.class},new Object[]{stoneKey},new Object[]{unknownKey},Blocks.STONE,null),
                new Query("getOptional","("+id+")"+optional,new Class[]{Identifier.class},new Object[]{stone},new Object[]{unknown},Optional.of(Blocks.STONE),Optional.empty()),
                new Query("getOptional","("+key+")"+optional,new Class[]{ResourceKey.class},new Object[]{stoneKey},new Object[]{unknownKey},Optional.of(Blocks.STONE),Optional.empty()),
                new Query("getKey","("+object+")"+id,new Class[]{Object.class},new Object[]{Blocks.STONE},new Object[]{other},stone,air),
                new Query("getResourceKey","("+object+")"+optional,new Class[]{Object.class},new Object[]{Blocks.STONE},new Object[]{other},Optional.of(stoneKey),Optional.empty()),
                new Query("getId","("+object+")I",new Class[]{Object.class},new Object[]{Blocks.STONE},new Object[]{other},registry.getId(Blocks.STONE),registry.getId(Blocks.AIR)),
                new Query("byId","(I)"+object,new Class[]{int.class},new Object[]{registry.getId(Blocks.STONE)},new Object[]{-1},Blocks.STONE,Blocks.AIR),
                new Query("getDefaultKey","()"+id,new Class[]{},new Object[]{},new Object[]{},air,air),
                new Query("getRandom","(Lnet/minecraft/util/RandomSource;)"+optional,new Class[]{RandomSource.class},new Object[]{},new Object[]{},null,null),
                new Query("containsKey","("+id+")Z",new Class[]{Identifier.class},new Object[]{stone},new Object[]{unknown},true,false),
                new Query("containsKey","("+key+")Z",new Class[]{ResourceKey.class},new Object[]{stoneKey},new Object[]{unknownKey},true,false));
        String[] owners={"net/minecraft/core/Registry","net/minecraft/core/DefaultedRegistry","net/minecraft/core/MappedRegistry"};
        var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);writer.visit(Opcodes.V17,Opcodes.ACC_PUBLIC,"fixture/RegistryQueries",null,"java/lang/Object",null);
        for(int root=0;root<owners.length;root++)for(int i=0;i<queries.size();i++) {
            var query=queries.get(i);if(query.name().equals("getDefaultKey")&&root!=1)continue;
            emit(writer,owners[root],query.name(),query.descriptor(),root+"_"+i);
        }
        emit(writer,owners[1],"m_7745_",queries.getFirst().descriptor(),"Srg");
        writer.visitEnd();var path=Files.createTempFile("lms-registry-queries", ".jar");
        try(var jar=new JarOutputStream(Files.newOutputStream(path))){jar.putNextEntry(new JarEntry("fixture/RegistryQueries.class"));jar.write(writer.toByteArray());jar.closeEntry();}
        byte[] original=Files.readAllBytes(path);
        try(var loader=new ManagedLegacyClassLoader(path,RegistryQueryFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
            var type=loader.linkOwnedClass("fixture.RegistryQueries");
            for(int root=0;root<owners.length;root++)for(int i=0;i<queries.size();i++) {
                var query=queries.get(i);if(query.name().equals("getDefaultKey")&&root!=1)continue;
                for(boolean missing:List.of(false,true)) {
                    Object[] args=missing?query.absent():query.present();Object expected=missing?query.missing():query.expected();
                    if(query.name().equals("getRandom")){args=new Object[]{RandomSource.create(37)};expected=registry.getRandom(RandomSource.create(37));}
                    var parameters=new ArrayList<Class<?>>();parameters.add(Class.forName(owners[root].replace('/','.')));parameters.addAll(List.of(query.parameters()));
                    var values=new ArrayList<Object>();values.add(registry);values.addAll(List.of(args));
                    Object result=type.getMethod("direct"+root+"_"+i,parameters.toArray(Class[]::new)).invoke(null,values.toArray());
                    check(Objects.equals(result,expected),"query/default semantics changed: "+root+query.name()+query.descriptor());
                    for(String prefix:List.of("handle","nested"))try{
                        var handle=(MethodHandle)type.getMethod(prefix+root+"_"+i).invoke(null);
                        if(query.name().equals("getRandom"))values.set(1,RandomSource.create(37));
                        check(Objects.equals(handle.invokeWithArguments(values),expected),"query callable identity/default changed");
                    }catch(Throwable failure){throw new AssertionError(failure);}
                }
            }
            check(type.getMethod("directSrg",DefaultedRegistry.class,Identifier.class).invoke(null,registry,unknown)==Blocks.AIR,"SRG defaulted query not mapped");
            var plain=BuiltInRegistries.SOUND_EVENT;
            check(type.getMethod("direct0_0",Registry.class,Identifier.class).invoke(null,plain,unknown)==null,"ordinary registry gained a default");
            check(type.getMethod("direct0_2",Registry.class,Identifier.class).invoke(null,plain,unknown).equals(Optional.empty()),"ordinary missing optional changed");
            check(Arrays.equals(original,Files.readAllBytes(path)),"source artifact mutated");
        }finally{Files.deleteIfExists(path);}
        System.out.println("PASS: twelve source registry queries through three native receiver types, direct/handle/nested/SRG calls, default-vs-optional/key absence, reverse/numeric identity and seeded random; no registry mutation");
    }
    private static void emit(ClassWriter writer,String owner,String name,String desc,String suffix) {
        var direct=writer.visitMethod(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"direct"+suffix,"(L"+owner+";"+desc.substring(1),null,null);direct.visitCode();direct.visitVarInsn(Opcodes.ALOAD,0);
        int local=1;for(Type argument:Type.getArgumentTypes(desc)){direct.visitVarInsn(argument.getOpcode(Opcodes.ILOAD),local);local+=argument.getSize();}
        direct.visitMethodInsn(Opcodes.INVOKEVIRTUAL,owner,name,desc,false);direct.visitInsn(Type.getReturnType(desc).getOpcode(Opcodes.IRETURN));direct.visitMaxs(0,0);direct.visitEnd();
        var getter=new Handle(Opcodes.H_INVOKEVIRTUAL,owner,name,desc,false);
        for(boolean nested:List.of(false,true)) {
            var method=writer.visitMethod(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,(nested?"nested":"handle")+suffix,"()Ljava/lang/invoke/MethodHandle;",null,null);method.visitCode();
            var bootstrap=new Handle(Opcodes.H_INVOKESTATIC,"com/fuckingdeveloper/lms/check/RegistryQueryFamilyCheck","constant","(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/invoke/MethodHandle;)Ljava/lang/invoke/MethodHandle;",false);
            method.visitLdcInsn(nested?new ConstantDynamic("query","Ljava/lang/invoke/MethodHandle;",bootstrap,getter):getter);method.visitInsn(Opcodes.ARETURN);method.visitMaxs(0,0);method.visitEnd();
        }
    }
    public static MethodHandle constant(MethodHandles.Lookup lookup,String name,Class<?> type,MethodHandle handle){return handle;}
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}
