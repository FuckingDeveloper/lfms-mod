package com.fuckingdeveloper.lms.target.neoforge263;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.*;
import java.util.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.*;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Native loader storage/events; managed source ownership is separate from host FML membership. */
public final class Target263SourceConfigAdapter {
    private record Registration(String owner, ModConfigSpec spec, ModConfig config) {}
    private static final Map<ModConfig,Registration> CONFIGS = new IdentityHashMap<>();
    private static final Map<ModConfigSpec,Registration> SPECS = new IdentityHashMap<>();
    private static final Map<String,IEventBus> BUSES = new HashMap<>();
    private static final Set<IEventBus> HOSTS = Collections.newSetFromMap(new IdentityHashMap<>());
    private Target263SourceConfigAdapter() {}
    public static synchronized IEventBus sourceBus(String owner) {
        return BUSES.computeIfAbsent(owner, ignored -> BusBuilder.builder().build());
    }
    public static synchronized Optional<String> owner(ModConfig config) {
        var registration=CONFIGS.get(config);return registration==null?Optional.empty():Optional.of(registration.owner());
    }
    public static synchronized IConfigSpec sourceSpec(ModConfig config) {
        var registration=CONFIGS.get(config);return registration==null?config.getSpec():registration.spec();
    }
    public static synchronized Map<Forge1192ModConfigView.Type,Forge1192ModConfigView> sourceConfigs(String owner) {
        var result=new EnumMap<Forge1192ModConfigView.Type,Forge1192ModConfigView>(Forge1192ModConfigView.Type.class);
        for(var r:CONFIGS.values())if(r.owner().equals(owner))result.put(Forge1192ModConfigView.Type.fromNative(r.config().getType()),Forge1192ModConfigView.of(r.config()));
        return result;
    }
    public static synchronized ModConfig config(ModConfigSpec spec) {
        var registration=SPECS.get(spec);
        if(registration==null)throw new IllegalStateException("No source configuration registered for spec");
        return registration.config();
    }
    public static void register(Object token,Forge1192ModConfigView.Type type,IConfigSpec spec) {
        var context=Forge1192LifecycleBridge.requireActive();
        register(token,type,spec,context.modId()+"-"+type.extension()+".toml");
    }
    public static synchronized void register(Object token,Forge1192ModConfigView.Type type,IConfigSpec spec,String filename) {
        var context=Forge1192LifecycleBridge.requireActive();
        Forge1192LifecycleBridge.requireConfigOwner(token);
        if(!(spec instanceof ModConfigSpec nativeSpec) || spec.getClass()!=ModConfigSpec.class)throw new UnsupportedOperationException("Custom source IConfigSpec registration is not adapted: "+spec.getClass().getName());
        if(nativeSpec.isEmpty())return; // Both source ModContainer and target skip empty specs.
        if(SPECS.containsKey(nativeSpec))throw new IllegalStateException("Source spec is already registered");
        if(sourceConfigs(context.modId()).containsKey(type))throw new IllegalStateException("Duplicate source config type for "+context.modId()+": "+type);
        if(HOSTS.add(context.modEventBus()))context.modEventBus().addListener(EventPriority.NORMAL,false,ModConfigEvent.class,event -> {
            var owner=owner(event.getConfig());owner.ifPresent(id->sourceBus(id).post(event));
        });
        var config=ConfigTracker.INSTANCE.registerConfig(type.nativeType(),new SourceSpec(nativeSpec),context.hostContainer(),Objects.requireNonNull(filename));
        var registration=new Registration(context.modId(),nativeSpec,config);
        CONFIGS.put(config,registration);SPECS.put(nativeSpec,registration);
    }
    /** Target loader owns loaded configs, corrections, watches, synchronization and file lifecycle. */
    private record SourceSpec(ModConfigSpec delegate) implements IConfigSpec {
        public boolean isEmpty(){return delegate.isEmpty();}
        public void validateSpec(ModConfig config){delegate.validateSpec(config);}
        public boolean isCorrect(com.electronwill.nightconfig.core.UnmodifiableCommentedConfig config){return delegate.isCorrect(config);}
        public void correct(com.electronwill.nightconfig.core.CommentedConfig config){delegate.correct(config);}
        public void acceptConfig(ILoadedConfig config){delegate.acceptConfig(config);Target263SourceConfigValues.afterReload(delegate);}
    }
}
