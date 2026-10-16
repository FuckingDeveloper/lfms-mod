package com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle;

import java.lang.reflect.Field;
import java.util.*;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.ModConfig;

/** Verified loader-field migrations, without guessing renamed Minecraft fields. */
public final class Forge1192LoaderReflectionBridge {
    private Forge1192LoaderReflectionBridge(){}
    public static Object getPrivateValue(Class<?> owner,Object receiver,String name){
        Objects.requireNonNull(owner);Objects.requireNonNull(name);
        if(owner==Forge1192ModContainerView.class&&name.equals("configs")) {
            var view=(Forge1192ModContainerView)receiver;
            return view.nativeOwner().<Map<Forge1192ModConfigView.Type,Forge1192ModConfigView>>map(Forge1192LoaderReflectionBridge::sourceConfigs)
                    .orElseGet(()->com.fuckingdeveloper.lms.target.neoforge263.Target263SourceConfigAdapter.sourceConfigs(view.getModId()));
        }
        if(owner==ModContainer.class&&name.equals("configs"))return sourceConfigs((ModContainer)receiver);
        // Named loader/library fields retain their exact actual declaration. SRG
        // Minecraft reflection requires a separate verified mapping operation.
        if(name.matches("[fm]_\\d+_"))throw new UnsupportedOperationException("Unadapted source reflection symbol "+owner.getName()+"#"+name);
        try{Field field=owner.getDeclaredField(name);field.setAccessible(true);return field.get(receiver);}
        catch(ReflectiveOperationException failure){throw new IllegalStateException("Unadapted source field "+owner.getName()+"#"+name,failure);}
    }
    @SuppressWarnings("unchecked")
    public static EnumMap<Forge1192ModConfigView.Type,Forge1192ModConfigView> sourceConfigs(ModContainer container){
        try{
            Field field=ConfigTracker.class.getDeclaredField("configsByMod");
            if(field.getType()!=java.util.concurrent.ConcurrentHashMap.class)throw new IllegalStateException("Target config tracker ABI changed");
            field.setAccessible(true);
            var tracked=(Map<String,List<ModConfig>>)field.get(ConfigTracker.INSTANCE);
            var result=new EnumMap<Forge1192ModConfigView.Type,Forge1192ModConfigView>(Forge1192ModConfigView.Type.class);
            for(var config:tracked.getOrDefault(container.getModId(),List.of())){
                // Backend hosting does not make managed configs belong to the native host's source view.
                if(com.fuckingdeveloper.lms.target.neoforge263.Target263SourceConfigAdapter.owner(config).isPresent())continue;
                if(config.getType()==ModConfig.Type.STARTUP)throw new UnsupportedOperationException("Native STARTUP configs have no source 1.19.2 config type");
                if(result.putIfAbsent(Forge1192ModConfigView.Type.fromNative(config.getType()),Forge1192ModConfigView.of(config))!=null)throw new UnsupportedOperationException("Multiple native configs of one type cannot project to source EnumMap: "+container.getModId());
            }
            return result;
        }catch(ReflectiveOperationException failure){throw new IllegalStateException("Cannot read verified target config ownership",failure);}
    }
}
