package com.fuckingdeveloper.lms.profile.forge1192.runtime.registry;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.Forge1192LifecycleBridge;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.registry.Forge1192RegistrationContext;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.BusBuilder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;

/** Native deferred registration with source ownership and construction-name scope. */
public final class LegacyDeferredRegister {
    private record Construction(ResourceKey<?> registry,Identifier name){}
    private static final ThreadLocal<Construction> CONSTRUCTION=new ThreadLocal<>();
    private final DeferredRegister<Object> nativeRegister;
    private final String owner;
    private boolean attached;
    private final java.util.List<com.fuckingdeveloper.lms.profile.forge1192.runtime.banner.LegacyBannerPatterns.Entry> banners=new java.util.ArrayList<>();
    private LegacyDeferredRegister(ResourceKey<? extends Registry<Object>> key,String namespace){
        owner=Forge1192LifecycleBridge.requireActive().modId();
        nativeRegister=DeferredRegister.create(key,namespace);
    }
    @SuppressWarnings({"rawtypes","unchecked"})
    public static LegacyDeferredRegister create(Object registry,String namespace){
        if(!(registry instanceof Registry<?> value))throw new IllegalArgumentException("Deferred source registry token is not a native registry");
        return new LegacyDeferredRegister((ResourceKey)value.key(),namespace);
    }
    @SuppressWarnings({"rawtypes","unchecked"})
    public static LegacyDeferredRegister create(ResourceKey<? extends Registry<?>> key,String namespace){return new LegacyDeferredRegister((ResourceKey)key,namespace);}
    public static LegacyDeferredRegister create(Identifier key,String namespace){return create(ResourceKey.createRegistryKey(key),namespace);}
    public LegacyRegistryObject register(String name,Supplier<?> factory){
        Objects.requireNonNull(factory,"source deferred factory");
        Identifier id=Identifier.fromNamespaceAndPath(nativeRegister.getNamespace(),name);
        if(nativeRegister.getRegistryKey().equals(Registries.BANNER_PATTERN)) {
            if(attached)throw new IllegalStateException("Source banner declaration added after register attachment: "+id);
            var entry=com.fuckingdeveloper.lms.profile.forge1192.runtime.banner.LegacyBannerPatterns.declare(owner,id,factory);
            banners.add(entry);return entry.holder();
        }
        var holder=nativeRegister.register(name,()->{
            Construction previous=CONSTRUCTION.get();
            CONSTRUCTION.set(new Construction(nativeRegister.getRegistryKey(),id));
            try{
                Object result=Objects.requireNonNull(factory.get(),"Source deferred factory returned null: "+id);
                if(result instanceof com.fuckingdeveloper.lms.profile.forge1192.runtime.banner.LegacyBannerPatternItemIntent)
                    com.fuckingdeveloper.lms.profile.forge1192.runtime.banner.LegacyBannerPatterns.recordItem(id);
                return result;
            }
            finally{if(previous==null)CONSTRUCTION.remove();else CONSTRUCTION.set(previous);}
        });
        return LegacyRegistryObject.deferred(holder);
    }
    public void register(IEventBus bus){
        if(attached)throw new IllegalStateException("Source DeferredRegister already attached");
        if(Forge1192RegistrationContext.registryPhaseCompleted(owner,nativeRegister.getRegistryKey()))throw new IllegalStateException("Source deferred registration arrived after target phase");
        if(nativeRegister.getRegistryKey().equals(Registries.BANNER_PATTERN)) {
            com.fuckingdeveloper.lms.profile.forge1192.runtime.banner.LegacyBannerPatterns.attach(banners);
            attached=true;return;
        }
        IEventBus scoped=BusBuilder.builder().build();
        nativeRegister.register(scoped);
        bus.addListener((RegisterEvent event)->{
            if(!event.getRegistryKey().equals(nativeRegister.getRegistryKey()))return;
            try(var lifecycle=Forge1192LifecycleBridge.enterCallback(owner);
                var registration=Forge1192RegistrationContext.enter(owner,event)){
                scoped.post(event);
            }catch(RuntimeException|Error failure){throw failure;}
            catch(Exception failure){throw new IllegalStateException("Cannot close source deferred registration scope",failure);}
        });
        attached=true;
    }
    @SuppressWarnings({"rawtypes","unchecked"})
    public net.minecraft.tags.TagKey<?> createTagKey(Identifier id){return net.minecraft.tags.TagKey.create((ResourceKey)getRegistryKey(),id);}
    public net.minecraft.tags.TagKey<?> createTagKey(String name){return createTagKey(Identifier.fromNamespaceAndPath(getNamespace(),name));}
    public String getNamespace(){return nativeRegister.getNamespace();}
    public ResourceKey<? extends Registry<?>> getRegistryKey(){return nativeRegister.getRegistryKey();}
    public static BlockBehaviour.Properties blockProperties(BlockBehaviour.Properties properties){
        Construction scope=CONSTRUCTION.get();
        return scope!=null&&scope.registry.equals(Registries.BLOCK)?properties.setId(ResourceKey.create(Registries.BLOCK,scope.name)):properties;
    }
    public static Item.Properties atItemConstruction(Item.Properties properties){
        Construction scope=CONSTRUCTION.get();
        if(scope==null||!scope.registry.equals(Registries.ITEM))return properties;
        var key=((com.fuckingdeveloper.lms.mixin.Target263ItemPropertiesAccess)(Object)properties).lms$itemId();
        if(key!=null)return properties; // Existing proven explicit-name adapters retain precedence.
        return com.fuckingdeveloper.lms.target.neoforge263.Target263ItemPropertiesSnapshots.withId(properties,ResourceKey.create(Registries.ITEM,scope.name));
    }
    public static Item.Properties itemProperties(){
        var properties=new Item.Properties();Construction scope=CONSTRUCTION.get();
        return scope!=null&&scope.registry.equals(Registries.ITEM)?properties.setId(ResourceKey.create(Registries.ITEM,scope.name)):properties;
    }
}
