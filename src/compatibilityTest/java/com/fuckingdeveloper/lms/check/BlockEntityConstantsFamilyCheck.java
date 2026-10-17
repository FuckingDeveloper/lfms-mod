package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import java.lang.invoke.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import java.util.regex.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import static org.objectweb.asm.Opcodes.*;

public final class BlockEntityConstantsFamilyCheck {
    private static final String SOURCE="net/minecraft/world/level/block/entity/BlockEntityType", DESC="L"+SOURCE+";", HOST="fixture/BlockEntityConstants";
    public static Object constant(MethodHandles.Lookup lookup,String name,Class<?> type,Object value){return value;}
    public static void run() throws Exception {
        var official=Files.readString(Path.of("legacy-mappings/1.19.2/client.txt"));
        int start=official.indexOf(SOURCE.replace('/','.')+" ->");var section=official.substring(start,official.indexOf("\nnet.minecraft.",start+1));
        var obfuscated=section.substring(section.indexOf(" -> ")+4,section.indexOf(':'));
        var joined=Files.readString(Path.of("legacy-mappings/1.19.2/joined.tsrg"));
        start=joined.indexOf("\n"+obfuscated+" ")+1;var end=Pattern.compile("(?m)^\\S").matcher(joined);end.find(start+1);var srgSection=joined.substring(start,end.start());
        var names=new LinkedHashMap<String,String>();var fields=Pattern.compile("    net.minecraft.world.level.block.entity.BlockEntityType (\\w+) -> (\\w+)").matcher(section);
        while(fields.find()) {
            var srg=Pattern.compile("(?m)^\\t"+Pattern.quote(fields.group(2))+" (f_\\d+_) ").matcher(srgSection);
            check(srg.find(),"missing pinned source field mapping");names.put(fields.group(1),srg.group(1));
        }
        check(names.size()==36,"pinned source block-entity catalog changed");
        var sourceIds=com.fuckingdeveloper.lms.profile.forge1192.Forge1192BuiltinBlockEntityCatalog.identifiers();
        check(sourceIds.keySet().equals(names.keySet()),"source declaration/registration evidence disagrees");
        var sourceJar=Path.of(System.getProperty("user.home"),".gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.19.2-43.4.0_mapped_official_1.19.2/forge-1.19.2-43.4.0_mapped_official_1.19.2.jar");
        if(Files.isRegularFile(sourceJar))check(sourceRegistryIds(sourceJar).equals(sourceIds),"pinned source snapshot differs from actual source registration bytecode");
        check(!BuiltInRegistries.BLOCK_ENTITY_TYPE.containsKey(Identifier.parse(sourceIds.get("BED"))),"removed native bed type silently returned");
        var supported=new LinkedHashMap<>(names);supported.remove("BED");
        var path=Files.createTempFile("source-block-entity-constants-",".jar");
        try {
            try(var jar=new JarOutputStream(Files.newOutputStream(path))){entry(jar,HOST,fixture(supported));entry(jar,"fixture/InheritedBlockTypes",subclass(false));entry(jar,"fixture/HiddenBlockTypes",subclass(true));}
            var original=Files.readAllBytes(path);
            try(var loader=new ManagedLegacyClassLoader(path,BlockEntityConstantsFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
                var type=loader.linkOwnedClass(HOST.replace('/','.'));
                for(var pair:supported.entrySet()) {
                    var expected=(BlockEntityType<?>)BlockEntityTypes.class.getField(pair.getKey()).get(null);
                    check(Identifier.parse(sourceIds.get(pair.getKey())).equals(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(expected)),"source/native registry identity changed: "+pair.getKey());
                    check(type.getMethod(pair.getKey()).invoke(null)==expected,"source SRG constant: "+pair.getKey());
                    check(type.getMethod("named"+pair.getKey()).invoke(null)==expected,"source named constant: "+pair.getKey());
                    for(boolean nested:List.of(false,true))try {
                        var handle=(MethodHandle)type.getMethod("handle"+pair.getKey()+nested).invoke(null);check(handle.invokeWithArguments()==expected,"source callable field: "+pair.getKey());
                    }catch(Throwable failure){throw new AssertionError(failure);}
                }
                check(type.getMethod("inherited").invoke(null)==BlockEntityTypes.BANNER,"inherited field declaration");
                check(type.getMethod("hidden").invoke(null)==null,"managed hiding field was replaced");
            }
            check(Arrays.equals(original,Files.readAllBytes(path)),"source artifact changed");
            rejectWrite(names.get("BANNER"));
            rejectBed(names.get("BED"));
        } finally {Files.deleteIfExists(path);}
        System.out.println("PASS: 35 surviving of 36 pinned source block-entity constants/SRG/named/direct/callable/nested identity against source registration IDs, inherited resolution, managed shadow preservation, final-write and removed-BED rejection");
    }
    private static Map<String,String> sourceRegistryIds(Path jarPath)throws Exception {
        var node=new ClassNode();try(var jar=new JarFile(jarPath.toFile())){new ClassReader(jar.getInputStream(jar.getJarEntry(SOURCE+".class"))).accept(node,0);}
        var result=new LinkedHashMap<String,String>();String literal=null,pending=null;
        for(var method:node.methods)if(method.name.equals("<clinit>"))for(var insn:method.instructions) {
            if(insn instanceof LdcInsnNode ldc && ldc.cst instanceof String value)literal=value;
            if(insn instanceof MethodInsnNode call && call.owner.equals(SOURCE) && call.name.equals("register"))pending=literal;
            if(insn instanceof FieldInsnNode field && field.getOpcode()==PUTSTATIC && field.owner.equals(SOURCE) && field.desc.equals(DESC)) {
                check(pending!=null,"source registration pattern changed: "+field.name);result.put(field.name,Identifier.parse(pending).toString());pending=null;
            }
        }
        return result;
    }
    private static byte[] fixture(Map<String,String> names) {
        var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);w.visit(V17,ACC_PUBLIC,HOST,null,"java/lang/Object",null);
        var bootstrap=new Handle(H_INVOKESTATIC,Type.getInternalName(BlockEntityConstantsFamilyCheck.class),"constant","(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;",false);
        names.forEach((name,srg)->{
            for(boolean named:List.of(false,true)){var m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,(named?"named":"")+name,"()"+DESC,null,null);m.visitCode();m.visitFieldInsn(GETSTATIC,SOURCE,named?name:srg,DESC);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();}
            var handle=new Handle(H_GETSTATIC,SOURCE,srg,DESC,false);
            for(boolean nested:List.of(false,true)){var m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"handle"+name+nested,"()Ljava/lang/invoke/MethodHandle;",null,null);m.visitCode();m.visitLdcInsn(nested?new ConstantDynamic("outer","Ljava/lang/invoke/MethodHandle;",bootstrap,new ConstantDynamic("inner","Ljava/lang/invoke/MethodHandle;",bootstrap,handle)):handle);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();}
        });
        for(boolean hidden:List.of(false,true)){var m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,hidden?"hidden":"inherited","()"+DESC,null,null);m.visitCode();m.visitFieldInsn(GETSTATIC,hidden?"fixture/HiddenBlockTypes":"fixture/InheritedBlockTypes",hidden?"BANNER":names.get("BANNER"),DESC);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();}
        w.visitEnd();return w.toByteArray();
    }
    private static byte[] subclass(boolean hidden) {
        var w=new ClassWriter(0);w.visit(V17,ACC_PUBLIC|ACC_ABSTRACT,hidden?"fixture/HiddenBlockTypes":"fixture/InheritedBlockTypes",null,SOURCE,null);
        if(hidden)w.visitField(ACC_PUBLIC|ACC_STATIC,"BANNER",DESC,null,null).visitEnd();w.visitEnd();return w.toByteArray();
    }
    private static void rejectWrite(String srg)throws Exception {
        var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);w.visit(V17,ACC_PUBLIC,"fixture/ConstantWrite",null,"java/lang/Object",null);
        var m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"bad","()V",null,null);m.visitCode();m.visitInsn(ACONST_NULL);m.visitFieldInsn(PUTSTATIC,SOURCE,srg,DESC);m.visitInsn(RETURN);m.visitMaxs(0,0);m.visitEnd();w.visitEnd();
        try{new Forge1192LegacyClassTransformer().transform("fixture.ConstantWrite",w.toByteArray());throw new AssertionError("final constant mutation admitted");}catch(UnsupportedOperationException expected){check(expected.getMessage().contains("constants are final"),"wrong constant boundary");}
    }
    private static void rejectBed(String srg)throws Exception {
        var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);w.visit(V17,ACC_PUBLIC,"fixture/RemovedBedType",null,"java/lang/Object",null);
        var m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"bad","()"+DESC,null,null);m.visitCode();m.visitFieldInsn(GETSTATIC,SOURCE,srg,DESC);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();w.visitEnd();
        try{new Forge1192LegacyClassTransformer().transform("fixture.RemovedBedType",w.toByteArray());throw new AssertionError("removed bed type fabricated");}catch(UnsupportedOperationException expected){check(expected.getMessage().contains("unavailable: BED"),"wrong removed bed boundary");}
    }
    private static void entry(JarOutputStream jar,String name,byte[] bytes)throws Exception{jar.putNextEntry(new JarEntry(name+".class"));jar.write(bytes);jar.closeEntry();}
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
