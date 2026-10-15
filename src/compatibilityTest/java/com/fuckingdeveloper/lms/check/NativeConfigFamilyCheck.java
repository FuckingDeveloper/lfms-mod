package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.runtime.*;
import com.electronwill.nightconfig.core.CommentedConfig;
import java.lang.reflect.Proxy;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.neoforged.bus.api.*;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.*;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforgespi.language.IModInfo;

final class NativeConfigFamilyCheck {
    static void run() throws Exception {
        var bus=BusBuilder.builder().build();var events=new AtomicInteger();
        bus.addListener((ModConfigEvent.Reloading event)->events.incrementAndGet());bus.start();
        var info=(IModInfo)Proxy.newProxyInstance(NativeConfigFamilyCheck.class.getClassLoader(),new Class<?>[]{IModInfo.class},(p,m,a)->{if(m.getName().equals("getModId"))return "config_probe";throw new AssertionError(m);});
        var host=new ModContainer(info){public IEventBus getEventBus(){return bus;}};
        var builder=new ModConfigSpec.Builder();var value=builder.defineInRange("value",1,0,10);builder.worldRestart().define("world",false);builder.gameRestart().define("game",false);var spec=builder.build();
        var tracker=new ConfigTracker();var config=tracker.registerConfig(ModConfig.Type.LOCAL,spec,host,"probe-local.toml");
        var source=Forge1192ModConfigView.of(config);var owner=Forge1192ModContainerView.nativeView(host);
        check(source==Forge1192ModConfigView.of(config)&&source.getSpec()==spec&&source.getModId().equals("config_probe"),"native config/view/spec ownership changed");
        check(source.getType()==Forge1192ModConfigView.Type.COMMON&&Arrays.equals(Arrays.stream(Forge1192ModConfigView.Type.values()).map(Enum::name).toArray(String[]::new),new String[]{"COMMON","CLIENT","SERVER"}),"source config enum order/names lost");
        check(source.getType().extension().equals("common"),"source filename suffix changed to native kind");
        try{Forge1192ModConfigView.Type.fromNative(ModConfig.Type.STARTUP);throw new AssertionError("unrepresentable startup kind coerced");}catch(UnsupportedOperationException expected){}
        var sets=Forge1192NativeConfigBridge.configSets(tracker);check(sets.get(source.getType()).size()==1&&sets.get(source.getType()).iterator().next()==source,"native config sets lost actual view identity");
        check(source.getConfigData()==null&&source.getFullPath()==null,"unloaded native config fabricated data/path");
        try{source.save();throw new AssertionError("unloaded config saved");}catch(UnsupportedOperationException expected){}
        var data=CommentedConfig.inMemory();check(Forge1192NativeConfigBridge.correct(spec,data)>=1,"native correction count replaced with dummy result");
        var path=Files.createTempDirectory("lms-native-config").resolve("probe.toml");
        var loadedClass=Class.forName("net.neoforged.fml.config.LoadedConfig");var ctor=loadedClass.getDeclaredConstructor(CommentedConfig.class,Path.class,ModConfig.class);ctor.setAccessible(true);
        var loaded=(IConfigSpec.ILoadedConfig)ctor.newInstance(data,path,config);
        var field=ModConfig.class.getDeclaredField("loadedConfig");field.setAccessible(true);field.set(config,loaded);spec.acceptConfig(loaded);
        check(source.getConfigData()==data&&source.getFullPath().equals(path)&&value.get()==1,"native loaded config/current value not exposed");
        data.set("value",7);Forge1192NativeConfigBridge.afterReload(spec);check(value.get()==7,"source reload did not reset native cache");
        source.save();check(Files.readString(path).contains("value = 7")&&events.get()==0,"source file save implicitly dispatched native reload or lost data");
        owner.dispatchConfigEvent(Forge1192NativeConfigBridge.reloading(source));check(events.get()==1,"explicit source reload not dispatched once");
        check(Forge1192NativeConfigBridge.getConfig(Forge1192NativeConfigBridge.reloading(source))==source,"native event returned different source config identity");
        check(Forge1192NativeConfigBridge.get(spec,List.of("value")) instanceof ModConfigSpec.ValueSpec,"removed inherited spec getter lost native definition");
        check(Forge1192NativeConfigBridge.needsWorldRestart((ModConfigSpec.ValueSpec)Forge1192NativeConfigBridge.get(spec,List.of("world"))),"source world restart flag lost");
        try{Forge1192NativeConfigBridge.needsWorldRestart((ModConfigSpec.ValueSpec)Forge1192NativeConfigBridge.get(spec,List.of("game")));throw new AssertionError("game restart weakened to world restart");}catch(UnsupportedOperationException expected){}
        var other=new ModContainer(info){public IEventBus getEventBus(){return bus;}};
        try{Forge1192ModContainerView.nativeView(other).dispatchConfigEvent(Forge1192NativeConfigBridge.reloading(source));throw new AssertionError("cross-owner event accepted");}catch(IllegalArgumentException expected){}
        check(events.get()==1,"rejected event produced side effects");
        sourceHandles(source);
        var resolver=com.fuckingdeveloper.lms.mapping.Forge1192SrgRuntimeResolver.load(Path.of(".")).orElseThrow();
        var pathGetter=resolver.resolve("net/minecraft/server/MinecraftServer","m_129843_","(Lnet/minecraft/world/level/storage/LevelResource;)Ljava/nio/file/Path;");
        check(pathGetter.resolved()&&pathGetter.name().equals("getWorldPath"),"packaged unobfuscated owner failed exact method resolution: "+pathGetter);
        check(!resolver.resolve("net/minecraft/client/Minecraft","m_129843_","(Lnet/minecraft/world/level/storage/LevelResource;)Ljava/nio/file/Path;").resolved(),"unrelated owner accepted by descriptor");
        check(!resolver.resolve("net/minecraft/server/MinecraftServer","m_129843_","()Ljava/nio/file/Path;").resolved(),"wrong descriptor accepted");
        var external=Path.of("legacy-mods/CarbonConfig-1.19.2-2.0.2.jar");
        var dependency=Path.of("lms/dependencies/CarbonConfig-1.19.2-2.0.2.jar/3d6c721d4d977672-Primitive Collections-0.8.9.jar");
        if(Files.exists(external)&&Files.exists(dependency))try(var loader=new com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader(external,List.of(dependency),NativeConfigFamilyCheck.class.getClassLoader(),new com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer())) {
            var guiType=loader.linkOwnedClass("carbonconfiglib.gui.base.helpers.GuiUtils");
            check(guiType.getDeclaredMethods().length>0,"immutable GUI helper verification did not execute");
            var guiNode=new org.objectweb.asm.tree.ClassNode();new org.objectweb.asm.ClassReader(loader.definedClassBytes(guiType.getName())).accept(guiNode,0);int projectedBatches=0;
            for(var method:guiNode.methods){boolean projected=false;for(var instruction:method.instructions)if(instruction instanceof org.objectweb.asm.tree.MethodInsnNode call&&call.owner.equals("com/fuckingdeveloper/lms/runtime/render/LegacyGuiTesselator"))projected=true;
                if(projected){projectedBatches++;for(var instruction:method.instructions)if(instruction instanceof org.objectweb.asm.tree.MethodInsnNode call)check(!call.owner.equals("com/mojang/blaze3d/vertex/BufferBuilder"),"projected GUI batch retained incompatible concrete buffer receiver: "+method.name+"/"+call.name);}
            }
            check(projectedBatches>0,"actual GUI batch projection was not inspected");
            var editType=loader.linkOwnedClass("carbonconfiglib.gui.base.widgets.CarbonEditBox");
            var edit=(net.minecraft.client.gui.components.EditBox)editType.getConstructor(net.minecraft.client.gui.Font.class,int.class,int.class,int.class,int.class).newInstance(null,0,0,120,20);
            check(edit instanceof com.fuckingdeveloper.lms.runtime.render.Forge1192EditBox,"source edit-box constructor did not retain target-native adaptation");
            var editNode=new org.objectweb.asm.tree.ClassNode();new org.objectweb.asm.ClassReader(loader.definedClassBytes(editType.getName())).accept(editNode,0);
            for(var method:editNode.methods)for(var instruction:method.instructions)if(instruction instanceof org.objectweb.asm.tree.MethodInsnNode call&&call.owner.equals(editType.getName().replace('.','/')))check(!call.name.startsWith("m_"),"immutable edit-box retained unresolved self/inherited operation: "+call.name+call.desc);
            edit.setValue("probe");var state=editType.getMethod("getState").invoke(edit);check(state.getClass().getMethod("getValue").invoke(state).equals("probe"),"immutable source text-state responder did not receive native edit");
            var listType=loader.linkOwnedClass("carbonconfiglib.gui.base.widgets.CarbonDynamicList");
            var sourceList=(com.fuckingdeveloper.lms.runtime.render.Forge1192ContainerSelectionList<?>)listType.getConstructor(net.minecraft.client.Minecraft.class,int.class,int.class,int.class,int.class,int.class).newInstance(null,320,240,30,180,20);
            check(sourceList.lms$getHeight()==240&&sourceList.lms$getTop()==30&&sourceList.lms$getBottom()==180,"source screen height and viewport conflated");
            listType.getMethod("setWidth",int.class).invoke(sourceList,280);check(sourceList.lms$getWidth()==280&&sourceList.lms$getRight()==280,"immutable source list geometry writes disconnected");
            check(sourceList.lms$children().isEmpty(),"source list constructed with fabricated entries");
            var listNode=new org.objectweb.asm.tree.ClassNode();new org.objectweb.asm.ClassReader(loader.definedClassBytes(listType.getName())).accept(listNode,0);
            for(var method:listNode.methods)for(var instruction:method.instructions)if(instruction instanceof org.objectweb.asm.tree.MethodInsnNode call&&call.name.startsWith("lms$")) {
                var current=Class.forName(call.owner.replace('/','.'),false,loader);boolean found=false;
                for(var ownerType=current;ownerType!=null;ownerType=ownerType.getSuperclass())for(var candidate:ownerType.getDeclaredMethods())if(candidate.getName().equals(call.name)&&org.objectweb.asm.Type.getMethodDescriptor(candidate).equals(call.desc))found=true;
                check(found,"unlinked source list operation: "+call.owner+"#"+call.name+call.desc);
            }

            var rowType=loader.linkOwnedClass("carbonconfiglib.gui.screens.ConfigListScreen$ConfigEntry");
            var rowNode=new org.objectweb.asm.tree.ClassNode();new org.objectweb.asm.ClassReader(loader.definedClassBytes(rowType.getName())).accept(rowNode,0);int widgets=0;
            for(var method:rowNode.methods)for(var instruction:method.instructions)if(instruction instanceof org.objectweb.asm.tree.MethodInsnNode call){
                check(!call.name.equals("m_6305_"),"source row retained old widget render dispatch");
                if(call.owner.equals("com/fuckingdeveloper/lms/runtime/render/Forge1192WidgetGraphicsBridge")&&call.name.equals("render"))widgets++;
            }
            check(widgets>0,"immutable row's native widget render path was not adapted");
            var type=loader.linkOwnedClass("carbonconfiglib.gui.impl.forge.ForgeConfig");var consumer=type.getConstructor(Forge1192ModConfigView.class).newInstance(source);
            check(type.getMethod("getModId").invoke(consumer).equals("config_probe"),"immutable source config consumer lost actual ownership");
            check(((Enum<?>)type.getMethod("getConfigType").invoke(consumer)).name().equals("SHARED"),"immutable source consumer lost COMMON-to-SHARED projection");
            check(type.getMethod("getRootNode").invoke(consumer)!=null,"actual source config root construction failed");
            try(var jar=new java.util.jar.JarFile(external.toFile())) {
                String name="carbonconfiglib/impl/internal/BackupManager";byte[] bytes;try(var in=jar.getInputStream(jar.getJarEntry(name+".class"))){bytes=in.readAllBytes();}
                var transformed=new org.objectweb.asm.tree.ClassNode();new org.objectweb.asm.ClassReader(new com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer().transform(name.replace('/','.'),bytes).bytes()).accept(transformed,0);
                var names=new java.util.HashSet<String>();
                for(var method:transformed.methods)for(var instruction:method.instructions)if(instruction instanceof org.objectweb.asm.tree.MethodInsnNode call&&java.util.Set.of("net/minecraft/server/MinecraftServer","net/minecraft/client/server/IntegratedServer").contains(call.owner)) {
                    check(!call.name.startsWith("m_"),"immutable backup consumer retained unadapted server call: "+call.name);names.add(call.name);
                }
                check(names.containsAll(java.util.Set.of("getWorldPath","overworld")),"backup consumer world ownership accessors lost");
            }

        }

        System.out.println("PASS: genuine native config/spec/data/path ownership, source enum identity, actual correction count/cache reset, atomic file save separate from exactly-once reload, restart/owner/unloaded boundaries");
    }
    private static void sourceHandles(Forge1192ModConfigView config) throws Exception {
        String owner="fixture/NativeConfigHandles",source="net/minecraftforge/fml/config/ModConfig",event="net/minecraftforge/fml/event/config/ModConfigEvent$Reloading";
        var node=new org.objectweb.asm.tree.ClassNode();node.version=org.objectweb.asm.Opcodes.V17;node.access=org.objectweb.asm.Opcodes.ACC_PUBLIC;node.name=owner;node.superName="java/lang/Object";
        var handles=List.of(new org.objectweb.asm.Handle(org.objectweb.asm.Opcodes.H_NEWINVOKESPECIAL,event,"<init>","(L"+source+";)V",false),new org.objectweb.asm.Handle(org.objectweb.asm.Opcodes.H_INVOKEVIRTUAL,event,"getConfig","()L"+source+";",false));
        for(int i=0;i<handles.size();i++){var m=new org.objectweb.asm.tree.MethodNode(org.objectweb.asm.Opcodes.ACC_PUBLIC|org.objectweb.asm.Opcodes.ACC_STATIC,"handle"+i,"()Ljava/lang/invoke/MethodHandle;",null,null);node.methods.add(m);m.instructions.add(new org.objectweb.asm.tree.LdcInsnNode(handles.get(i)));m.instructions.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.ARETURN));}
        var writer=new org.objectweb.asm.ClassWriter(org.objectweb.asm.ClassWriter.COMPUTE_MAXS);node.accept(writer);var path=Files.createTempFile("lms-config-handles", ".jar");try(var jar=new java.util.jar.JarOutputStream(Files.newOutputStream(path))){jar.putNextEntry(new java.util.jar.JarEntry(owner+".class"));jar.write(writer.toByteArray());jar.closeEntry();}
        try(var loader=new com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader(path,NativeConfigFamilyCheck.class.getClassLoader(),new com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer())) {
            var type=loader.linkOwnedClass(owner.replace('/','.'));var constructor=(java.lang.invoke.MethodHandle)type.getMethod("handle0").invoke(null);var getter=(java.lang.invoke.MethodHandle)type.getMethod("handle1").invoke(null);
            try {
                check(constructor.type().equals(java.lang.invoke.MethodType.methodType(ModConfigEvent.Reloading.class,Forge1192ModConfigView.class)),"source event constructor handle type changed");
                var eventValue=(ModConfigEvent.Reloading)constructor.invokeExact(config);
                var projected=(Forge1192ModConfigView)getter.invokeExact(eventValue);check(projected==config,"source event getter handle lost exact receiver or owner");
            }catch(RuntimeException|Error failure){throw failure;}catch(Throwable failure){throw new AssertionError(failure);}
        }
    }
    private static void check(boolean value,String reason){if(!value)throw new AssertionError(reason);}
}
