package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.runtime.worldgen.*;
import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.stateproviders.*;
import net.minecraft.world.level.levelgen.feature.featuresize.*;
import net.minecraft.world.level.levelgen.feature.foliageplacers.*;
import net.minecraft.world.level.levelgen.feature.trunkplacers.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.neoforged.neoforge.registries.RegisterEvent;
import java.lang.reflect.Proxy;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.function.*;

/** Native codecs and actual engine callback dispatch, no window or fabricated registry. */
public final class WorldgenFamilyCheck {
    private static final class Stop extends RuntimeException {}
    private static final class Trunk extends LegacyTrunkPlacer {
        LegacyTreeConfiguration seen; RandomSource random; LevelSimulatedReader world;boolean stop;
        Trunk(){super(1,0,0);}
        protected TrunkPlacerType<?> lms$trunkType(){return TrunkPlacerType.STRAIGHT_TRUNK_PLACER;}
        public List<FoliagePlacer.FoliageAttachment> lms$placeTrunk(LevelSimulatedReader world,BiConsumer<BlockPos,BlockState> setter,RandomSource random,int height,BlockPos pos,LegacyTreeConfiguration config){
            this.seen=config;this.random=random;this.world=world;
            check(Target263WorldgenAdapter.state(config.trunkProvider,random,pos).is(Blocks.OAK_LOG),"provider did not receive active native world");
            if(stop){setter.accept(pos,Blocks.OAK_LOG.defaultBlockState());throw new Stop();}
            return List.of(new FoliagePlacer.FoliageAttachment(pos,0,false));
        }
    }
    private static final class Foliage extends LegacyFoliagePlacer {
        LegacyTreeConfiguration seen;boolean stop;
        Foliage(){super(ConstantInt.of(0),ConstantInt.of(0));}
        protected FoliagePlacerType<?> lms$foliageType(){return FoliagePlacerType.BLOB_FOLIAGE_PLACER;}
        public int lms$foliageHeight(RandomSource random,int height,LegacyTreeConfiguration config){seen=config;return 0;}
        protected boolean lms$skipLocation(RandomSource r,int dx,int y,int dz,int radius,boolean doubleTrunk){return false;}
        protected void lms$createFoliage(LevelSimulatedReader world,BiConsumer<BlockPos,BlockState> setter,RandomSource random,LegacyTreeConfiguration config,int height,FoliageAttachment attachment,int foliageHeight,int radius,int offset){seen=config;if(stop){setter.accept(attachment.pos(),Blocks.OAK_LEAVES.defaultBlockState());throw new Stop();}}
    }
    public static void run()throws Exception {
        var trunk=new Trunk();var foliage=new Foliage();var config=new LegacyTreeConfiguration.TreeConfigurationBuilder(BlockStateProvider.of(Blocks.OAK_LOG),trunk,BlockStateProvider.of(Blocks.OAK_LEAVES),foliage,new TwoLayersFeatureSize(1,0,1)).forceDirt().ignoreVines().build();
        Map<BlockPos,BlockState> writes=new HashMap<>();
        WorldGenLevel world=(WorldGenLevel)Proxy.newProxyInstance(WorldgenFamilyCheck.class.getClassLoader(),new Class<?>[]{WorldGenLevel.class},(proxy,method,args)->switch(method.getName()){
            case "getMinY"->-64;case "getMaxY"->320;case "getHeight"->384;
            case "isStateAtPosition"->((Predicate<BlockState>)args[1]).test(Blocks.AIR.defaultBlockState());
            case "getBlockState"->writes.getOrDefault(args[0],Blocks.AIR.defaultBlockState());
            case "setBlock"->{writes.put(((BlockPos)args[0]).immutable(),(BlockState)args[1]);yield true;}
            case "toString"->"worldgen-check";case "hashCode"->System.identityHashCode(proxy);case "equals"->proxy==args[0];
            default->throw new AssertionError("Unhandled actual native world operation: "+method);
        });
        var random=RandomSource.create(12);var origin=new BlockPos(2,80,3);var configured=new LegacyConfiguredFeature<>(LegacyFeature.TREE,config);
        check(!configured.place(world,null,random,origin),"empty source setters fabricated native placement success");
        check(trunk.seen==config&&foliage.seen==config&&trunk.random==random&&trunk.world==world,"native engine lost exact source callback context");
        for(boolean trunkFailure:new boolean[]{true,false}){
            trunk.stop=trunkFailure;foliage.stop=!trunkFailure;
            try{configured.place(world,null,random,origin);throw new AssertionError("callback failure swallowed");}catch(Stop expected){}
            check(writes.get(origin).is(trunkFailure?Blocks.OAK_LOG:Blocks.OAK_LEAVES),"source setter did not reach native world");
            try{Target263WorldgenAdapter.tree(config);throw new AssertionError("placement scope leaked");}catch(IllegalStateException expected){}
        }
        var vanilla=new LegacyTreeConfiguration.TreeConfigurationBuilder(BlockStateProvider.of(Blocks.BIRCH_LOG),new StraightTrunkPlacer(4,2,1),BlockStateProvider.of(Blocks.BIRCH_LEAVES),new BlobFoliagePlacer(ConstantInt.of(2),ConstantInt.of(0),3),new TwoLayersFeatureSize(1,0,1)).forceDirt().ignoreVines().build();
        var intent=(Target263WorldgenAdapter.TreeIntent)new LegacyConfiguredFeature<>(LegacyFeature.TREE,vanilla).target();
        var json=Target263WorldgenAdapter.TreeIntent.CODEC.codec().encodeStart(JsonOps.INSTANCE,intent).getOrThrow();
        var decoded=Target263WorldgenAdapter.TreeIntent.CODEC.codec().parse(JsonOps.INSTANCE,json).getOrThrow();
        check(decoded.source().forceDirt&&decoded.source().ignoreVines&&((SimpleStateProvider)decoded.source().trunkProvider).state().is(Blocks.BIRCH_LOG),"tree codec lost source flags/providers");
        var ore=new LegacyOreConfiguration(List.of(new LegacyOreConfiguration.TargetBlockState(Target263WorldgenAdapter.stone(),Blocks.GOLD_ORE.defaultBlockState()),new LegacyOreConfiguration.TargetBlockState(Target263WorldgenAdapter.deepslate(),Blocks.DEEPSLATE_GOLD_ORE.defaultBlockState())),9,0.25F);
        var nativeOre=(OreFeature)new LegacyConfiguredFeature<>(LegacyFeature.ORE,ore).target();
        var oreJson=Feature.DIRECT_CODEC.encodeStart(JsonOps.INSTANCE,nativeOre).getOrThrow().getAsJsonObject();
        check(oreJson.get("size").getAsInt()==9&&oreJson.get("discard_chance_on_air_exposure").getAsFloat()==0.25F&&oreJson.getAsJsonArray("targets").size()==2,"ore projection lost size/discard/replacements");
        var ctor=RegisterEvent.class.getDeclaredConstructor(ResourceKey.class,Registry.class);ctor.setAccessible(true);
        var id=Identifier.fromNamespaceAndPath("worldgen_check","ore");var sourceOre=new LegacyConfiguredFeature<>(LegacyFeature.ORE,ore);
        var sourcePlaced=new LegacyPlacedFeature(Holder.direct(sourceOre),List.of(net.minecraft.world.level.levelgen.placement.CountPlacement.of(3)));
        var placedId=Identifier.fromNamespaceAndPath("worldgen_check","placed_ore");
        try(var scope=com.fuckingdeveloper.lms.runtime.Forge1192RegistrationContext.enter("worldgen_check",ctor.newInstance(Registries.FEATURE_TYPE,BuiltInRegistries.FEATURE_TYPE))){
            check(LegacyWorldgenDefinitions.register(LegacyWorldgenDefinitions.CONFIGURED,id,sourceOre)==sourceOre,"definition registration replaced source identity");
            LegacyWorldgenDefinitions.register(LegacyWorldgenDefinitions.PLACED,placedId,sourcePlaced);
            try{LegacyWorldgenDefinitions.register(LegacyWorldgenDefinitions.CONFIGURED,id,sourceOre);throw new AssertionError("duplicate declaration swallowed");}catch(IllegalStateException expected){}
        }
        var resources=LegacyWorldgenDefinitions.resources();check(resources.containsKey(Identifier.fromNamespaceAndPath("worldgen_check","worldgen/feature/ore.json")),"source definition not published as native feature data");
        for(int iteration=0;iteration<2;iteration++){
            var registry=new MappedRegistry<net.minecraft.world.level.levelgen.placement.PlacedFeature>(Registries.PLACED_FEATURE,com.mojang.serialization.Lifecycle.stable());
            var overridden=new net.minecraft.world.level.levelgen.placement.PlacedFeature(Holder.direct(nativeOre),List.of());
            var canonical=Registry.registerForHolder(registry,ResourceKey.create(Registries.PLACED_FEATURE,placedId),overridden);registry.freeze();
            RegistryAccess access=new RegistryAccess.ImmutableRegistryAccess(List.of(registry));
            var builder=new net.minecraft.world.level.biome.BiomeGenerationSettings.PlainBuilder();
            Target263BiomeModifierBridge.modify((biome,phase,ignored)->Target263BiomeModifierBridge.addFeature(builder,net.minecraft.world.level.levelgen.GenerationStep.Decoration.UNDERGROUND_ORES,Holder.direct(sourcePlaced)),access,null,net.neoforged.neoforge.common.world.BiomeModifier.Phase.ADD,null);
            check(builder.build().features().get(net.minecraft.world.level.levelgen.GenerationStep.Decoration.UNDERGROUND_ORES.ordinal()).get(0)==canonical,"source biome holder bypassed native override/reload identity");
            try{Target263BiomeModifierBridge.addFeature(builder,net.minecraft.world.level.levelgen.GenerationStep.Decoration.UNDERGROUND_ORES,Holder.direct(sourcePlaced));throw new AssertionError("biome registry scope leaked");}catch(IllegalStateException expected){}
        }
        var thresholdField=net.minecraft.data.worldgen.placement.VegetationPlacements.class.getDeclaredField("TREE_THRESHOLD");thresholdField.setAccessible(true);
        check(Target263WorldgenAdapter.treeThreshold()==thresholdField.get(null),"source placement default lost exact native singleton identity");
        sourceFactories();
        var fixture=Path.of("legacy-mods/IC2Classic-1.19.2-2.1.3.4.jar");
        if(!Files.exists(fixture))fixture=Path.of("run/legacy-mods/IC2Classic-1.19.2-2.1.3.4.jar");
        if(Files.exists(fixture)){
            byte[] before=MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(fixture));
            try(var loader=new ManagedLegacyClassLoader(fixture,WorldgenFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())){
                var generator=loader.linkOwnedClass("ic2.core.platform.events.WorldGenerator");
                nativeAbi(loader.definedClassBytes(generator.getName()));
                var sourceTrunk=loader.linkOwnedClass("ic2.core.block.resource.rubber.RubberTrunkPlacer");
                check(sourceTrunk.getSuperclass()==LegacyTrunkPlacer.class,"external trunk did not use source-dispatch base");sourceTrunk.getConstructor(int.class,int.class,int.class).newInstance(5,2,0);
                var sourceLeaves=loader.linkOwnedClass("ic2.core.block.resource.rubber.RubberFoliagePlacer");
                check(sourceLeaves.getSuperclass()==LegacyFoliagePlacer.class,"external foliage did not use source-dispatch base");sourceLeaves.getConstructor().newInstance();
                var modifier=(net.neoforged.neoforge.common.world.BiomeModifier)loader.linkOwnedClass("ic2.core.platform.events.WorldGenerator$IC2Modifier").getConstructor().newInstance();
                check(modifier.codec()!=null,"source modifier did not expose native map codec");
                modifier.modify(RegistryAccess.EMPTY,null,net.neoforged.neoforge.common.world.BiomeModifier.Phase.REMOVE,null);
            }
            check(Arrays.equals(before,MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(fixture))),"legacy artifact modified");
        }
        System.out.println("PASS: native tree engine source callbacks/context/setters/failure restoration, tree flags/provider codec roundtrip, native ore size/discard/replacements, staged definition data/duplicate rejection, canonical biome override/reload identity, resource bytes/native precedence and immutable external placer/modifier linkage");
    }
    private static void nativeAbi(byte[] bytes)throws Exception {
        var node=new org.objectweb.asm.tree.ClassNode();new org.objectweb.asm.ClassReader(bytes).accept(node,org.objectweb.asm.ClassReader.SKIP_DEBUG|org.objectweb.asm.ClassReader.SKIP_FRAMES);
        int checked=0;
        for(var method:node.methods)for(var insn:method.instructions){
            String owner=insn instanceof org.objectweb.asm.tree.MethodInsnNode call?call.owner:insn instanceof org.objectweb.asm.tree.FieldInsnNode field?field.owner:null;
            if(owner==null||!(owner.startsWith("net/minecraft/")||owner.startsWith("net/neoforged/")||owner.startsWith("com/fuckingdeveloper/lms/runtime/")))continue;
            var type=Class.forName(owner.replace('/','.'),false,WorldgenFamilyCheck.class.getClassLoader());
            java.lang.reflect.Member found=null;
            if(insn instanceof org.objectweb.asm.tree.MethodInsnNode call){
                if(call.name.equals("<init>")){for(var candidate:type.getDeclaredConstructors())if(org.objectweb.asm.Type.getConstructorDescriptor(candidate).equals(call.desc))found=candidate;}
                else {var pending=new ArrayDeque<Class<?>>();pending.add(type);var visited=new HashSet<Class<?>>();while(!pending.isEmpty()&&found==null){var current=pending.remove();if(!visited.add(current))continue;for(var candidate:current.getDeclaredMethods())if(candidate.getName().equals(call.name)&&org.objectweb.asm.Type.getMethodDescriptor(candidate).equals(call.desc)){found=candidate;break;}if(current.getSuperclass()!=null)pending.add(current.getSuperclass());pending.addAll(List.of(current.getInterfaces()));}}
                check(call.itf==type.isInterface(),"wrong native class/interface invocation: "+owner+"#"+call.name+call.desc);
            }else if(insn instanceof org.objectweb.asm.tree.FieldInsnNode field){
                for(Class<?> current=type;current!=null&&found==null;current=current.getSuperclass())for(var candidate:current.getDeclaredFields())if(candidate.getName().equals(field.name)&&org.objectweb.asm.Type.getDescriptor(candidate.getType()).equals(field.desc)){found=candidate;break;}
            }
            check(found!=null,"unresolved transformed worldgen native member: "+owner+" / "+insn);
            check(java.lang.reflect.Modifier.isPublic(found.getModifiers()),"inaccessible native worldgen member: "+found);checked++;
        }
        check(checked>30,"worldgen native ABI inventory unexpectedly empty");
        System.out.println("Worldgen native call/field ABI inventory checked="+checked);
    }
    private static void sourceFactories()throws Exception {
        var node=new org.objectweb.asm.tree.ClassNode();node.name="fixture/WorldgenCalls";node.superName="java/lang/Object";node.version=org.objectweb.asm.Opcodes.V17;node.access=org.objectweb.asm.Opcodes.ACC_PUBLIC;
        String provider="net/minecraft/world/level/levelgen/feature/stateproviders/BlockStateProvider",state="Lnet/minecraft/world/level/block/state/BlockState;",simple="Lnet/minecraft/world/level/levelgen/feature/stateproviders/SimpleStateProvider;";
        for(String name:List.of("provider","handle")){
            var method=new org.objectweb.asm.tree.MethodNode(org.objectweb.asm.Opcodes.ACC_PUBLIC|org.objectweb.asm.Opcodes.ACC_STATIC,name,name.equals("provider")?"("+state+")"+simple:"()Ljava/lang/invoke/MethodHandle;",null,null);
            if(name.equals("provider")){method.instructions.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD,0));method.instructions.add(new org.objectweb.asm.tree.MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC,provider,"m_191384_","("+state+")"+simple,false));}
            else method.instructions.add(new org.objectweb.asm.tree.LdcInsnNode(new org.objectweb.asm.Handle(org.objectweb.asm.Opcodes.H_INVOKESTATIC,provider,"m_191384_","("+state+")"+simple,false)));
            method.instructions.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.ARETURN));node.methods.add(method);
        }
        var key=new org.objectweb.asm.tree.MethodNode(org.objectweb.asm.Opcodes.ACC_PUBLIC|org.objectweb.asm.Opcodes.ACC_STATIC,"key","()Lnet/minecraft/resources/ResourceKey;",null,null);
        key.instructions.add(new org.objectweb.asm.tree.FieldInsnNode(org.objectweb.asm.Opcodes.GETSTATIC,"net/minecraftforge/registries/ForgeRegistries$Keys","FEATURES","Lnet/minecraft/resources/ResourceKey;"));key.instructions.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.ARETURN));node.methods.add(key);
        String treeSource="net/minecraft/world/level/levelgen/feature/configurations/TreeConfiguration",builder=treeSource+"$TreeConfigurationBuilder";
        String builderArgs="(L"+provider+";Lnet/minecraft/world/level/levelgen/feature/trunkplacers/TrunkPlacer;L"+provider+";Lnet/minecraft/world/level/levelgen/feature/foliageplacers/FoliagePlacer;Lnet/minecraft/world/level/levelgen/feature/featuresize/FeatureSize;)";
        var build=new org.objectweb.asm.tree.MethodNode(org.objectweb.asm.Opcodes.ACC_PUBLIC|org.objectweb.asm.Opcodes.ACC_STATIC,"tree",builderArgs+"L"+treeSource+";",null,null);
        build.instructions.add(new org.objectweb.asm.tree.TypeInsnNode(org.objectweb.asm.Opcodes.NEW,builder));build.instructions.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.DUP));
        for(int i=0;i<5;i++)build.instructions.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD,i));
        build.instructions.add(new org.objectweb.asm.tree.MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESPECIAL,builder,"<init>",builderArgs+"V",false));build.instructions.add(new org.objectweb.asm.tree.MethodInsnNode(org.objectweb.asm.Opcodes.INVOKEVIRTUAL,builder,"m_68251_","()L"+treeSource+";",false));build.instructions.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.ARETURN));node.methods.add(build);
        var writer=new org.objectweb.asm.ClassWriter(org.objectweb.asm.ClassWriter.COMPUTE_MAXS);node.accept(writer);
        var path=Files.createTempFile("lms-worldgen-calls",".jar");try(var jar=new java.util.jar.JarOutputStream(Files.newOutputStream(path))){jar.putNextEntry(new java.util.jar.JarEntry(node.name+".class"));jar.write(writer.toByteArray());jar.closeEntry();
            for(var resource:Map.of("data/probe/forge/biome_modifier/source.json","source-bytes","data/probe/forge/biome_modifier/both.json","old-bytes","data/probe/neoforge/biome_modifier/both.json","native-bytes").entrySet()){
                jar.putNextEntry(new java.util.jar.JarEntry(resource.getKey()));jar.write(resource.getValue().getBytes(java.nio.charset.StandardCharsets.UTF_8));jar.closeEntry();
            }
        }
        var aliasPack=Target263WorldgenPack.aliases(path);check(aliasPack!=null,"source biome resources not published");
        try(var packs=aliasPack.open()){
            var resources=packs.findFirst().orElseThrow();
            try(var input=resources.getResource(net.minecraft.server.packs.PackType.SERVER_DATA,Identifier.fromNamespaceAndPath("probe","neoforge/biome_modifier/source.json")).get()){
                check(new String(input.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8).equals("source-bytes"),"source biome resource bytes changed");
            }
            check(resources.getResource(net.minecraft.server.packs.PackType.SERVER_DATA,Identifier.fromNamespaceAndPath("probe","neoforge/biome_modifier/both.json"))==null,"source alias overrides native entry in same artifact");
            resources.close();
        }
        try(var loader=new ManagedLegacyClassLoader(path,WorldgenFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())){
            var type=loader.linkOwnedClass(node.name.replace('/','.'));var source=Blocks.BIRCH_LOG.defaultBlockState();
            check(((SimpleStateProvider)type.getMethod("provider",BlockState.class).invoke(null,source)).state()==source,"source provider factory lost state");
            var handle=(java.lang.invoke.MethodHandle)type.getMethod("handle").invoke(null);try{check(((SimpleStateProvider)handle.invoke(source)).state()==source,"source factory handle lost state");}catch(Throwable failure){throw new AssertionError(failure);}
            var built=(LegacyTreeConfiguration)type.getMethod("tree",BlockStateProvider.class,TrunkPlacer.class,BlockStateProvider.class,FoliagePlacer.class,FeatureSize.class).invoke(null,BlockStateProvider.of(Blocks.OAK_LOG),new StraightTrunkPlacer(1,0,0),BlockStateProvider.of(Blocks.OAK_LEAVES),new BlobFoliagePlacer(ConstantInt.of(0),ConstantInt.of(0),1),new TwoLayersFeatureSize(1,0,1));
            check(built.trunkPlacer instanceof StraightTrunkPlacer,"source builder stopped accepting native vanilla placers");
            check(type.getMethod("key").invoke(null)==Registries.FEATURE_TYPE,"source FEATURES still selects data registry instead of implementation phase");
        }
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
