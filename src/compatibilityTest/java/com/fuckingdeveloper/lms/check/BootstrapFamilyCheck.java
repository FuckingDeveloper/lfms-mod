package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.runtime.*;
import com.fuckingdeveloper.lms.runtime.registry.*;
import com.fuckingdeveloper.lms.runtime.render.Forge1192KeyboardBridge;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.serialization.Lifecycle;
import java.lang.reflect.Proxy;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.client.input.KeyEvent;
import net.neoforged.bus.api.*;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

final class BootstrapFamilyCheck {
    static void run() throws Exception {
        registryAliases();
        var entityClasses=new java.util.HashMap<String,String>();
        for(var field:net.minecraft.world.entity.EntityTypes.class.getFields())if(field.getType()==net.minecraft.world.entity.EntityType.class&&field.getGenericType() instanceof java.lang.reflect.ParameterizedType generic&&generic.getActualTypeArguments()[0] instanceof Class<?> implementation) {
            var value=(net.minecraft.world.entity.EntityType<?>)field.get(null);
            entityClasses.put(net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(value).toString(),org.objectweb.asm.Type.getInternalName(implementation));
        }
        var entityEvidence=com.fuckingdeveloper.lms.profile.Forge1192EntityRelocations.entries();
        check(entityEvidence.size()>40,"registry-derived entity relocation inventory unexpectedly empty");
        for(var entry:entityEvidence)check(entry.target().equals(entityClasses.get(entry.id())),"entity target identity differs from actual native registry declaration: "+entry.id());
        String entityDescriptor="(Lnet/minecraft/world/entity/animal/Cat;[Lnet/minecraft/world/entity/animal/Wolf;)Lnet/minecraft/world/entity/animal/Cat$UnprovenNested;";
        var projected=com.fuckingdeveloper.lms.profile.Forge1192EntityRelocations.projectDescriptor(entityDescriptor);
        check(projected.contains("/animal/feline/Cat;")&&projected.contains("/animal/wolf/Wolf;")&&projected.contains("/animal/Cat$UnprovenNested;"),"registry entity projection inferred an unverified nested class or lost array identity");
        check(com.fuckingdeveloper.lms.profile.Forge1192EntityRelocations.reverseDescriptor(projected).equals(entityDescriptor),"source entity identity lost for SRG reverse resolution");
        var atlasIds=List.of(net.minecraft.data.AtlasIds.BLOCKS,net.minecraft.data.AtlasIds.CHESTS,id("custom_atlas"));
        var definitions=Target263LegacyAtlasSources.definitions(atlasIds);
        check(definitions.size()==3,"per-atlas source definitions lost native/custom identity");
        for(var atlas:atlasIds) {
            var key=net.minecraft.resources.Identifier.fromNamespaceAndPath(atlas.getNamespace(),"atlases/"+atlas.getPath()+".json");
            var json=com.google.gson.JsonParser.parseString(new String(definitions.get(key),java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
            check(json.getAsJsonArray("sources").get(0).getAsJsonObject().get("atlas").getAsString().equals(atlas.toString()),"source sprite request routed into wrong atlas");
            var value=new LegacyTextureStitchSpriteSource(atlas);
            var codec=LegacyTextureStitchSpriteSource.CODEC.codec();
            check(codec.parse(com.mojang.serialization.JsonOps.INSTANCE,codec.encodeStart(com.mojang.serialization.JsonOps.INSTANCE,value).getOrThrow()).getOrThrow().atlasId().equals(atlas),"native atlas source codec lost identity");
        }
        var sourceCategory=Forge1192KeyMappingBridge.category("fixture.source.category");
        check(sourceCategory.label().getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents translated&&translated.getKey().equals("fixture.source.category"),"source category label lost original translation identity");
        check(Forge1192KeyMappingBridge.sourceCategoryLabel(net.minecraft.client.KeyMapping.Category.MOVEMENT).isEmpty(),"native category label hijacked");
        constructionAndStages();
        var bus=BusBuilder.builder().build();
        var info=(IModInfo)Proxy.newProxyInstance(BootstrapFamilyCheck.class.getClassLoader(),new Class[]{IModInfo.class},(p,m,a)->{if(m.getName().equals("getModId"))return "bootstrap_probe";throw new AssertionError(m);});
        var host=new ModContainer(info){public IEventBus getEventBus(){return bus;}};
        bus.addListener((com.fuckingdeveloper.lms.runtime.event.LegacyDataGenerationEvent event)->{throw new AssertionError("Datagen event emitted during ordinary runtime");});
        var calls=new AtomicInteger();
        try(var scope=Forge1192LifecycleBridge.enter("bootstrap_probe",bus,host)){
            var builder=Forge1192NetworkBridge.LegacyChannelBuilder.named(id("channel"));
            java.util.function.Supplier<String> version=()->{calls.incrementAndGet();return "1";};
            java.util.function.Predicate<String> accept="1"::equals;
            Object token=builder.networkProtocolVersion(version).clientAcceptedVersions(accept).serverAcceptedVersions(accept).simpleChannel();
            var channel=Forge1192NetworkBridge.requireChannel(token);
            check(channel.protocolVersion()==version&&channel.clientAcceptedVersions()==accept&&calls.get()==0,"channel builder lost lazy source callbacks");
            Forge1192NetworkBridge.registerMessage(token,3,String.class,(java.util.function.BiConsumer<String,net.minecraft.network.FriendlyByteBuf>)(m,b)->b.writeUtf(m),(java.util.function.Function<net.minecraft.network.FriendlyByteBuf,String>)net.minecraft.network.FriendlyByteBuf::readUtf,(Object m,Object c)->{},Optional.of(Forge1192NetworkBridge.LegacyNetworkDirection.PLAY_TO_CLIENT));
            check(Forge1192NetworkBridge.messages(channel).getFirst().flow()==Forge1192NetworkBridge.Flow.CLIENTBOUND,"explicit source direction lost");
            try{Forge1192NetworkBridge.registerMessage(token,4,String.class,(Object m,Object b)->{},b->"x",(Object m,Object c)->{},Optional.of(Forge1192NetworkBridge.LegacyNetworkDirection.LOGIN_TO_CLIENT));throw new AssertionError("login admitted");}catch(UnsupportedOperationException expected){}
            check(Forge1192NetworkBridge.messages(channel).size()==1,"failed phase partially registered");
            var deferred=LegacyDeferredRegister.create(Registries.SOUND_EVENT,"bootstrap_probe");
            var made=new AtomicInteger();
            var holder=deferred.register("entry",()->{check(Forge1192RegistrationContext.requireModId().equals("bootstrap_probe"),"source factory owner");made.incrementAndGet();return "value";});
            check(made.get()==0&&holder instanceof java.util.function.Supplier,"source factory eager/holder lost Supplier");
            try{holder.get();throw new AssertionError("early source holder access succeeded");}catch(IllegalStateException|NullPointerException expected){}
            deferred.register(bus);
            try{deferred.register(bus);throw new AssertionError("duplicate bus admitted");}catch(IllegalStateException expected){}
            var registry=new MappedRegistry<Object>((ResourceKey)Registries.SOUND_EVENT,Lifecycle.stable());
            var ctor=RegisterEvent.class.getDeclaredConstructor(ResourceKey.class,Registry.class);ctor.setAccessible(true);
            // Isolated mutable event fixture; no builtin/global registry mutation.
            bus.start();bus.post(ctor.newInstance(Registries.SOUND_EVENT,registry));
            check(made.get()==1&&registry.getValue(id("entry")).equals("value"),"native deferred phase did not invoke/register source factory");
            try{deferred.register("late",()->"late");throw new AssertionError("late entry admitted");}catch(IllegalStateException expected){}
            check(Forge1192LoaderReflectionBridge.sourceConfigs(host).isEmpty(),"no-config native owner is not an actual empty source view");
        }
        var key=Forge1192KeyboardBridge.key(InputConstants.Type.KEYBOARD,335);
        check(key.getName().equals("key.keyboard.keypad.enter")&&key.getValue()==InputConstants.KEY_NUMPADENTER,"source keypad identity mapped to wrong SDL key");
        var event=new InputEvent.Key(new KeyEvent(key.getValue(),0,InputConstants.MOD_SHIFT),InputConstants.PRESS);
        var binding=Forge1192KeyMappingBridge.create("bootstrap.probe",335,"key.categories.misc");
        check(Forge1192KeyboardBridge.matches(binding,335,key.getValue())&&!Forge1192KeyboardBridge.matches(binding,79,key.getValue()),"native key binding matched wrong named source key");
        check(!Forge1192KeyboardBridge.matchesMouse(binding,335),"nonmouse source key admitted as mouse button");
        check(Forge1192KeyboardBridge.getKey(event)==335&&Forge1192KeyboardBridge.getScanCode(event)==key.getValue()&&Forge1192KeyboardBridge.getModifiers(event)==1,"normalized source event identity/modifiers");
        check(Forge1192KeyboardBridge.sourceValue(Forge1192KeyboardBridge.key(InputConstants.Type.KEYBOARD,79))==79,"source O roundtrip");
        try{Forge1192KeyboardBridge.rawScanType();throw new AssertionError("raw GLFW scans admitted");}catch(UnsupportedOperationException expected){}
        for(var artifact:List.of("CarbonConfig-1.19.2-2.0.2.jar","ironchest-1.19.2-14.2.7.jar")){
            Path root=Path.of(System.getProperty("user.dir"));Path jar=root.resolve("legacy-mods").resolve(artifact);if(!Files.exists(jar))jar=root.resolve("run/legacy-mods").resolve(artifact);
            if(!Files.exists(jar))continue;
            String entry=artifact.startsWith("Carbon")?"carbonconfiglib.CarbonConfig":"com.progwml6.ironchest.IronChests";
            var plan=new Forge1192RegistrationPlanner().plan(jar,entry);
            check(plan.complete(),"immutable source bootstrap graph incomplete");
            var assessment=new Forge1192CompatibilitySurface().assess(plan.boundaries());
            if(entry.startsWith("carbon")) {
                check(assessment.executable(),"source container/bootstrap contracts remain gated");
            } else {
                check(assessment.executable(),"source Forge bootstrap still has an unadapted active boundary: "+artifact);
                try(var loader=new com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader(jar,BootstrapFamilyCheck.class.getClassLoader(),new com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer())) {
                    loader.linkOwnedClass(entry);
                    var node=new org.objectweb.asm.tree.ClassNode();new org.objectweb.asm.ClassReader(loader.definedClassBytes(entry)).accept(node,0);
                    var registration=node.methods.stream().filter(m->m.desc.equals("(Lnet/neoforged/neoforge/client/event/RegisterMenuScreensEvent;)V")).findFirst().orElseThrow();
                    check(java.util.stream.StreamSupport.stream(registration.instructions.spliterator(),false).filter(i->i instanceof org.objectweb.asm.tree.MethodInsnNode c&&c.name.equals("registerDuringPhase")).count()==7,"source pure menu callback was not relocated completely");
                    new org.objectweb.asm.tree.analysis.Analyzer<>(new org.objectweb.asm.tree.analysis.BasicVerifier()).analyze(node.name,registration);
                    String renderer="com.progwml6.ironchest.client.render.IronChestRenderer";loader.linkOwnedClass(renderer);
                    var rendererNode=new org.objectweb.asm.tree.ClassNode();new org.objectweb.asm.ClassReader(loader.definedClassBytes(renderer)).accept(rendererNode,0);
                    var renderThunk=rendererNode.methods.stream().filter(m->m.name.equals("lmsLegacyRender")).findFirst().orElseThrow();
                    check(rendererNode.methods.stream().anyMatch(m->m.name.equals("m_6922_")),"direct source erased renderer callback was removed");
                    check(rendererNode.methods.stream().filter(m->m.name.equals("<init>")).anyMatch(m->java.util.stream.StreamSupport.stream(m.instructions.spliterator(),false).anyMatch(i->i instanceof org.objectweb.asm.tree.MethodInsnNode call&&call.owner.equals("com/fuckingdeveloper/lms/runtime/render/Forge1192RendererContextBridge")&&call.name.equals("blockEntityRenderDispatcher"))),"native renderer-context service getter not projected to actual record accessor");
                    new org.objectweb.asm.tree.analysis.Analyzer<>(new org.objectweb.asm.tree.analysis.BasicVerifier()).analyze(rendererNode.name,renderThunk);

                }
                try(var archive=new java.util.jar.JarFile(jar.toFile());var in=archive.getInputStream(archive.getJarEntry(entry.replace('.','/')+".class"))) {
                    var node=new org.objectweb.asm.tree.ClassNode();new org.objectweb.asm.ClassReader(in).accept(node,0);
                    var mixed=node.methods.stream().filter(m->m.desc.equals("(Lnet/minecraftforge/fml/event/lifecycle/FMLClientSetupEvent;)V")).findFirst().orElseThrow();
                    mixed.instructions.insert(new org.objectweb.asm.tree.MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC,"fixture/Effects","touch","()V",false));
                    var rewriter=Class.forName("com.fuckingdeveloper.lms.profile.Forge1192BootstrapRewriter").getDeclaredMethod("menuSetupCallbacks",org.objectweb.asm.tree.ClassNode.class,java.util.function.Predicate.class,java.util.function.Predicate.class);rewriter.setAccessible(true);
                    check((Integer)rewriter.invoke(null,node,(java.util.function.Predicate<String>)(owner->true),(java.util.function.Predicate<String>)(owner->true))==0&&mixed.desc.contains("FMLClientSetupEvent"),"mixed source setup was moved to an earlier target phase");
                }

                var artifactAssessment=new Forge1192CompatibilitySurface().assess(plan.boundaries(),jar);
                check(artifactAssessment.executable()&&artifactAssessment.requirements().stream().anyMatch(r->r.adapter().equals("NATIVE_CONTAINER_DATA")),
                        "native container persistence family absent from preflight inventory");
            }
            if(entry.startsWith("com.progwml6"))check(plan.boundaries().stream().anyMatch(b->b.phase()==Forge1192RegistrationPlanner.ExecutionPhase.DATA_GENERATION),"datagen callback phase not retained");
        }
        var boundary=new Forge1192RegistrationPlanner.Boundary("direct-runtime","net.minecraftforge.data.event.GatherDataEvent","includeClient","()Z");
        check(!new Forge1192CompatibilitySurface().assess(List.of(boundary)).executable(),"direct datagen runtime call hidden");
        var dormant=new Forge1192RegistrationPlanner.Boundary("datagen","net.minecraftforge.data.event.GatherDataEvent","includeClient","()Z",Forge1192RegistrationPlanner.ExecutionPhase.DATA_GENERATION);
        check(!new Forge1192CompatibilitySurface().assess(List.of(dormant,boundary)).executable(),"inactive visit hid runtime reachability");
        System.out.println("PASS: source bootstrap preflight phases, native deferred isolated registration/ownership/early/late access, lazy builder/explicit direction/login failure, named SDL keys and actual empty config view; gameplay not inferred");
    }
    private static void constructionAndStages() throws Exception {
        var calls=new AtomicInteger();
        var builder=com.fuckingdeveloper.lms.runtime.LegacyBlockEntityTypeBuilder.m_155273_((pos,state)->{
            calls.incrementAndGet();return new ContainerPersistenceFamilyCheck.Container();
        },net.minecraft.world.level.block.Blocks.CHEST);
        var blocks=builder.getClass().getDeclaredField("blocks");blocks.setAccessible(true);
        check(calls.get()==0&&blocks.get(builder).equals(java.util.Set.of(net.minecraft.world.level.block.Blocks.CHEST)),"source builder eagerly invoked factory/lost valid-block intent");
        // Never unfreeze the builtin registry after this smoke process has completed bootstrap.
        try{builder.m_58966_(null);throw new AssertionError("construction bypassed frozen native lifecycle");}
        catch(IllegalStateException expected){check(expected.getMessage().contains("frozen"),"unexpected native construction failure");}
        var current=net.neoforged.fml.loading.FMLEnvironment.getDist();
        var other=current==net.neoforged.api.distmarker.Dist.CLIENT?net.neoforged.api.distmarker.Dist.DEDICATED_SERVER:net.neoforged.api.distmarker.Dist.CLIENT;
        check(Forge1192DistExecutorBridge.unsafeCallWhenOn(other,()->{throw new AssertionError("inactive branch obtained");})==null,"inactive source Callable result changed");
        check(Forge1192DistExecutorBridge.unsafeCallWhenOn(current,()->()->"called").equals("called"),"source Callable confused with Supplier");
        var failure=new java.io.IOException("source callable");
        try{Forge1192DistExecutorBridge.unsafeCallWhenOn(current,()->()->{throw failure;});throw new AssertionError("source exception swallowed");}
        catch(RuntimeException expected){check(expected.getCause()==failure,"source Callable exception wrapping changed");}
        var type=Forge1192VanillaConstructionBridge.menuType((id,inventory)->{throw new AssertionError("menu factory invoked eagerly");});
        check(type.requiredFeatures().equals(net.minecraft.world.flag.FeatureFlags.VANILLA_SET),"source MenuType default feature set changed");
        var queue=new com.fuckingdeveloper.lms.runtime.render.Forge1192MenuScreenBridge.Queue();
        net.minecraft.client.gui.screens.MenuScreens.ScreenConstructor factory=(menu,inventory,title)->{throw new AssertionError("screen factory invoked eagerly");};
        queue.add(type,factory,"probe");
        try{queue.add(type,factory,"duplicate");throw new AssertionError("duplicate menu screen admitted");}catch(IllegalStateException expected){}
        var nativeScreens=new java.util.HashMap<net.minecraft.world.inventory.MenuType<?>,net.minecraft.client.gui.screens.MenuScreens.ScreenConstructor<?,?>>();
        nativeScreens.put(type,factory);
        var event=new net.neoforged.neoforge.client.event.RegisterMenuScreensEvent(nativeScreens);
        try{queue.flush(event);throw new AssertionError("native duplicate overwritten");}catch(IllegalStateException expected){}
        check(queue.pendingCount()==1,"failed native screen registration lost pending intent");
        nativeScreens.clear();queue.flush(event);
        check(nativeScreens.get(type)==factory&&queue.pendingCount()==0,"native screen factory identity/flush lost");
        try{queue.add(type,factory,"late");throw new AssertionError("late menu screen admitted");}catch(IllegalStateException expected){}
    }
    private static void registryAliases() throws Exception {
        var node=new org.objectweb.asm.tree.ClassNode();node.name="fixture/RegistryAliases";
        var method=new org.objectweb.asm.tree.MethodNode();node.methods.add(method);
        var aliases=Map.of("SOUND_EVENTS","SOUND_EVENT","POTIONS","POTION","PARTICLE_TYPES","PARTICLE_TYPE",
                "MENU_TYPES","MENU","RECIPE_TYPES","RECIPE_TYPE","ATTRIBUTES","ATTRIBUTE","STAT_TYPES","STAT_TYPE");
        for(var name:aliases.keySet())method.instructions.add(new org.objectweb.asm.tree.FieldInsnNode(
                org.objectweb.asm.Opcodes.GETSTATIC,"net/minecraftforge/registries/ForgeRegistries",name,
                "Lcom/fuckingdeveloper/lms/runtime/registry/LegacyForgeRegistry;"));
        var rewrite=com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer.class
                .getDeclaredMethod("rewriteLegacyForgeRegistries",org.objectweb.asm.tree.ClassNode.class);
        rewrite.setAccessible(true);
        check(((Integer)rewrite.invoke(null,node))==aliases.size(),"static registry alias family incomplete");
        var nativeOwner=net.minecraft.core.registries.BuiltInRegistries.class;
        for(var instruction:method.instructions) {
            var field=(org.objectweb.asm.tree.FieldInsnNode)instruction;
            check(field.owner.equals(org.objectweb.asm.Type.getInternalName(nativeOwner))&&aliases.containsValue(field.name),"wrong native registry identity");
            var nativeField=nativeOwner.getField(field.name);
            check(java.lang.reflect.Modifier.isStatic(nativeField.getModifiers())&&org.objectweb.asm.Type.getDescriptor(nativeField.getType()).equals(field.desc),"native registry ABI mismatch: "+field.name);
        }
    }
    private static Identifier id(String path){return Identifier.fromNamespaceAndPath("bootstrap_probe",path);}
    private static void check(boolean value,String reason){if(!value)throw new AssertionError(reason);}
}
