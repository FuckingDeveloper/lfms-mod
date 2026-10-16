package com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarFile;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Source FML auto-subscription, indexed before linking any subscriber parameter types. */
final class Forge1192AutomaticSubscribers {
    private record Installed(IEventBus bus,Class<?> type){}
    private final List<Installed> installed=new ArrayList<>();
    void install(Path artifact,String owner,ManagedLegacyClassLoader loader,IEventBus modBus) throws IOException,ClassNotFoundException {
        try(var archive=new JarFile(artifact.toFile(),false)) {
            var entries=Collections.list(archive.entries()).stream().filter(e->e.getName().endsWith(".class")).sorted(Comparator.comparing(java.util.jar.JarEntry::getName)).toList();
            for(var entry:entries)try(var stream=archive.getInputStream(entry)) {
                var node=new ClassNode();new ClassReader(stream).accept(node,ClassReader.SKIP_CODE|ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
                if(node.visibleAnnotations==null)continue;
                var declaration=node.visibleAnnotations.stream().filter(a->a.desc.equals("Lnet/minecraftforge/fml/common/Mod$EventBusSubscriber;")).findFirst();
                if(declaration.isEmpty())continue;
                Map<String,Object> values=values(declaration.get());
                String defaultOwner=owner;
                for(var annotation:node.visibleAnnotations)if(annotation.desc.equals("Lnet/minecraftforge/fml/common/Mod;"))defaultOwner=(String)values(annotation).get("value");
                String declaredOwner=(String)values.getOrDefault("modid",defaultOwner);
                if(!owner.equals(declaredOwner))continue;
                var sides=EnumSet.allOf(Dist.class);
                if(values.containsKey("value")) {
                    sides.clear();
                    for(Object value:(List<?>)values.get("value"))sides.add(Dist.valueOf(enumValue(value,"Lnet/minecraftforge/api/distmarker/Dist;")));
                }
                if(!sides.contains(FMLEnvironment.getDist()))continue;
                String bus=values.containsKey("bus")?enumValue(values.get("bus"),"Lnet/minecraftforge/fml/common/Mod$EventBusSubscriber$Bus;"):"FORGE";
                IEventBus target=switch(bus){case "MOD"->modBus;case "FORGE"->NeoForge.EVENT_BUS;default->throw new IllegalStateException("Unknown source subscriber bus "+bus);};
                Class<?> type=Class.forName(node.name.replace('/','.'),true,loader);
                Forge1192EventBusBridge.register(target,type);installed.add(new Installed(target,type));
            }
        } catch(IOException|ClassNotFoundException|RuntimeException|Error failure) {
            try{close();}catch(RuntimeException|Error cleanup){failure.addSuppressed(cleanup);}throw failure;
        }
    }
    void close() {
        RuntimeException failure=null;
        for(var entry:installed.reversed())try{Forge1192EventBusBridge.unregister(entry.bus(),entry.type());}
        catch(RuntimeException error){if(failure==null)failure=error;else failure.addSuppressed(error);}
        installed.clear();if(failure!=null)throw failure;
    }
    private static String enumValue(Object value,String descriptor){
        if(!(value instanceof String[] data)||data.length!=2||!data[0].equals(descriptor))throw new IllegalStateException("Invalid source subscriber enum identity");
        return data[1];
    }
    private static Map<String,Object> values(AnnotationNode annotation){
        var result=new HashMap<String,Object>();
        if(annotation.values!=null)for(int i=0;i<annotation.values.size();i+=2)result.put((String)annotation.values.get(i),annotation.values.get(i+1));
        return result;
    }
}
