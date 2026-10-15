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
                var gaps=assessment.requirements().stream().filter(r->r.state()==Forge1192CompatibilitySurface.State.ADAPTER_REQUIRED).toList();
                check(!assessment.executable()&&gaps.size()==1&&gaps.getFirst().target().startsWith("net.minecraftforge.fml.ModList#forEachModInOrder"),"source managed iteration must remain gated until a real source container view exists");
            } else {
                check(assessment.executable(),"source Forge bootstrap still has an unadapted active boundary: "+artifact);
                var artifactAssessment=new Forge1192CompatibilitySurface().assess(plan.boundaries(),jar);
                check(!artifactAssessment.executable()&&artifactAssessment.requirements().stream().anyMatch(r->r.adapter().equals("NATIVE_CONTAINER_DATA")),
                        "unsupported native container persistence must reject before partial constructor execution");
            }
            if(entry.startsWith("com.progwml6"))check(plan.boundaries().stream().anyMatch(b->b.phase()==Forge1192RegistrationPlanner.ExecutionPhase.DATA_GENERATION),"datagen callback phase not retained");
        }
        var boundary=new Forge1192RegistrationPlanner.Boundary("direct-runtime","net.minecraftforge.data.event.GatherDataEvent","includeClient","()Z");
        check(!new Forge1192CompatibilitySurface().assess(List.of(boundary)).executable(),"direct datagen runtime call hidden");
        var dormant=new Forge1192RegistrationPlanner.Boundary("datagen","net.minecraftforge.data.event.GatherDataEvent","includeClient","()Z",Forge1192RegistrationPlanner.ExecutionPhase.DATA_GENERATION);
        check(!new Forge1192CompatibilitySurface().assess(List.of(dormant,boundary)).executable(),"inactive visit hid runtime reachability");
        System.out.println("PASS: source bootstrap preflight phases, native deferred isolated registration/ownership/early/late access, lazy builder/explicit direction/login failure, named SDL keys and actual empty config view; gameplay not inferred");
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
