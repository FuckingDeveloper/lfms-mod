package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.target.neoforge263.Target263LegacyLootResources;
import com.fuckingdeveloper.lms.target.neoforge263.Target263LegacyTagResources;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.profiling.Profiler;
import net.neoforged.neoforge.common.loot.*;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

final class LootModifierFamilyCheck {
    record Modifier(int priority) implements IGlobalLootModifier {
        public ObjectArrayList<ItemStack> apply(ObjectArrayList<ItemStack> loot, LootContext context) { return loot; }
        public MapCodec<? extends IGlobalLootModifier> codec() { return MapCodec.unit(this); }
    }
    static void run() throws Exception {
        sourceCallback();
        var a = id("a"); var b = id("b"); var c = id("c");
        var base = new LinkedHashMap<Identifier,String>();
        base.put(Target263LegacyLootResources.LIST, "{\"replace\":false,\"entries\":[\"loot_check:a\",\"loot_check:b\"]}");
        for (var name : List.of("a", "b", "c", "unused")) base.put(file(id(name)), "{}");
        var upper = Map.of(Target263LegacyLootResources.LIST,
                "{\"replace\":true,\"entries\":[\"loot_check:b\",\"loot_check:a\",\"loot_check:b\",\"loot_check:c\"]}");
        var nativeData = Map.of(file(id("native")), "{}");
        try (var manager = new MultiPackResourceManager(PackType.SERVER_DATA,
                List.of(new Target263LegacyTagResources(pack(base)), pack(upper), pack(nativeData)))) {
            var plan = Target263LegacyLootResources.plan(manager);
            check(plan.active() && plan.order().equals(List.of(a,b,c)), "replace/repeated-entry order");
            var listed = plan.resources().listResources("loot_modifiers", i -> true);
            check(listed.size()==4 && !listed.containsKey(Target263LegacyLootResources.LIST)
                    && !listed.containsKey(file(id("unused"))) && listed.containsKey(file(id("native"))), "source selection/native preservation");
            check(plan.resources().getResource(Target263LegacyLootResources.LIST).isEmpty(), "control document leaked");
            check(manager.getResource(Target263LegacyLootResources.LIST).isPresent(), "original manager mutated");
            var ma=new Modifier(1000); var mb=new Modifier(1000); var mc=new Modifier(1000);
            var high=new Modifier(2000); var low=new Modifier(1); var equal=new Modifier(1000);
            var ordered=Target263LegacyLootResources.ordered(Map.of(a,ma,b,mb,c,mc), List.of(high,mc,equal,mb,ma,low),plan.order());
            check(ordered.size()==6 && ordered.get(0)==high && ordered.get(1)==ma
                    && ordered.get(2)==mb && ordered.get(3)==mc && ordered.get(5)==low, "native priority/source order");
            try { Target263LegacyLootResources.ordered(Map.of(a,ma),List.of(),plan.order()); throw new AssertionError("missing decode hidden"); }
            catch (IllegalStateException expected) {}
        }
        // Exact native inherited prepare override + apply hook, no registered codec required for an empty selection.
        try (var resources = new MultiPackResourceManager(PackType.SERVER_DATA,List.of(new Target263LegacyTagResources(pack(
                Map.of(Target263LegacyLootResources.LIST,"{\"replace\":false,\"entries\":[]}",file(a),"{}")))))) {
            var manager = new LootModifierManager();
            var prepare=LootModifierManager.class.getDeclaredMethod("prepare",ResourceManager.class,net.minecraft.util.profiling.ProfilerFiller.class);
            prepare.setAccessible(true);
            var decoded=(Map<?,?>)prepare.invoke(manager,resources,Profiler.get());
            check(decoded.isEmpty(),"actual native prepare enabled unlisted legacy definition");
            var apply=LootModifierManager.class.getDeclaredMethod("apply",Map.class,ResourceManager.class,net.minecraft.util.profiling.ProfilerFiller.class);
            apply.setAccessible(true);apply.invoke(manager,decoded,resources,Profiler.get());
            check(!manager.getSortedModifiers().iterator().hasNext(),"actual native apply selection");
        }
    }
    private static void sourceCallback() throws Exception {
        var name="fixture/LegacyLoot";
        var writer=new org.objectweb.asm.ClassWriter(0);
        writer.visit(61,org.objectweb.asm.Opcodes.ACC_PUBLIC,name,null,"java/lang/Object",
                new String[]{"net/minecraftforge/common/loot/IGlobalLootModifier","java/util/function/Supplier"});
        writer.visitField(9,"CODEC","Lcom/mojang/serialization/Codec;",null,null).visitEnd();
        var constructor=writer.visitMethod(1,"<init>","()V",null,null);constructor.visitCode();
        constructor.visitVarInsn(25,0);constructor.visitMethodInsn(183,"java/lang/Object","<init>","()V",false);constructor.visitInsn(177);constructor.visitMaxs(1,1);constructor.visitEnd();
        var get=writer.visitMethod(1,"get","()Ljava/lang/Object;",null,null);get.visitCode();
        get.visitTypeInsn(187,name);get.visitInsn(89);get.visitMethodInsn(183,name,"<init>","()V",false);get.visitInsn(176);get.visitMaxs(2,1);get.visitEnd();
        var init=writer.visitMethod(8,"<clinit>","()V",null,null);init.visitCode();
        init.visitTypeInsn(187,name);init.visitInsn(89);init.visitMethodInsn(183,name,"<init>","()V",false);
        init.visitMethodInsn(184,"com/mojang/serialization/Codec","unit","(Ljava/util/function/Supplier;)Lcom/mojang/serialization/Codec;",true);
        init.visitFieldInsn(179,name,"CODEC","Lcom/mojang/serialization/Codec;");init.visitInsn(177);init.visitMaxs(2,0);init.visitEnd();
        var codec=writer.visitMethod(1,"codec","()Lcom/mojang/serialization/Codec;",null,null);codec.visitCode();
        codec.visitFieldInsn(178,name,"CODEC","Lcom/mojang/serialization/Codec;");codec.visitInsn(176);codec.visitMaxs(1,1);codec.visitEnd();
        var apply=writer.visitMethod(1,"apply","(Lit/unimi/dsi/fastutil/objects/ObjectArrayList;Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",null,null);
        apply.visitCode();apply.visitVarInsn(25,1);apply.visitInsn(176);apply.visitMaxs(1,3);apply.visitEnd();writer.visitEnd();
        var jar=java.nio.file.Files.createTempFile("lms-source-loot-",".jar");
        try {
            try(var output=new java.util.jar.JarOutputStream(java.nio.file.Files.newOutputStream(jar))){output.putNextEntry(new java.util.jar.JarEntry(name+".class"));output.write(writer.toByteArray());output.closeEntry();}
            try(var loader=new com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader(jar,LootModifierFamilyCheck.class.getClassLoader(),new com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer())) {
                var type=loader.loadClass(name.replace('/','.'));
                var modifier=(IGlobalLootModifier)type.getConstructor().newInstance();
                check(modifier.codec()==type.getField("CODEC").get(null),"source codec producer/callback identity");
                check(modifier.priority()==IGlobalLootModifier.DEFAULT_PRIORITY,"source group native priority");
                var loot=new ObjectArrayList<ItemStack>();check(modifier.apply(loot,null)==loot,"source apply return identity");
            }
        } finally { java.nio.file.Files.delete(jar); }
    }
    private static PackResources pack(Map<Identifier,String> data) {
        return new PackResources() {
            public PackLocationInfo location(){return new PackLocationInfo("loot-check",Component.literal("loot-check"),PackSource.BUILT_IN,Optional.empty());}
            public IoSupplier<InputStream> getRootResource(String... path){return null;}
            public <T> T getMetadataSection(MetadataSectionType<T> type){return null;}
            public Set<String> getNamespaces(PackType type){var names=new HashSet<String>();data.keySet().forEach(i->names.add(i.getNamespace()));return names;}
            public void close(){}
            public IoSupplier<InputStream> getResource(PackType type,Identifier id){return data.containsKey(id)?()->new ByteArrayInputStream(data.get(id).getBytes(StandardCharsets.UTF_8)):null;}
            public void listResources(PackType type,String namespace,String directory,ResourceOutput output){data.forEach((id,json)->{if(id.getNamespace().equals(namespace)&&id.getPath().startsWith(directory+"/"))output.accept(id,getResource(type,id));});}
        };
    }
    private static Identifier id(String name){return Identifier.fromNamespaceAndPath("loot_check",name);}
    private static Identifier file(Identifier id){return Identifier.fromNamespaceAndPath(id.getNamespace(),"loot_modifiers/"+id.getPath()+".json");}
    private static void check(boolean result,String message){if(!result)throw new AssertionError(message);}
}
