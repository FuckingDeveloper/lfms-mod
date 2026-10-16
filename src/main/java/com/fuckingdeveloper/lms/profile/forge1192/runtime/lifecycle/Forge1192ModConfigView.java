package com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle;

import com.electronwill.nightconfig.core.CommentedConfig;
import java.nio.file.Path;
import java.util.*;
import net.neoforged.fml.config.*;

/** Source inspection/save contract over a genuine native loader-owned configuration. */
public final class Forge1192ModConfigView {
    public enum Type {
        COMMON,CLIENT,SERVER;
        public String extension(){return name().toLowerCase(Locale.ROOT);}
        public ModConfig.Type nativeType(){return switch(this){case COMMON->ModConfig.Type.LOCAL;case CLIENT->ModConfig.Type.CLIENT;case SERVER->ModConfig.Type.SYNCED;};}
        public static Type fromNative(ModConfig.Type type){return switch(type){case LOCAL->COMMON;case CLIENT->CLIENT;case SYNCED->SERVER;case STARTUP->throw new UnsupportedOperationException("Native STARTUP has no source config type");};}
    }
    private static final Map<ModConfig,java.lang.ref.WeakReference<Forge1192ModConfigView>> VIEWS=new WeakHashMap<>();
    private final ModConfig config;
    private Forge1192ModConfigView(ModConfig config){this.config=Objects.requireNonNull(config);}
    public static synchronized Forge1192ModConfigView of(ModConfig config){
        var ref=VIEWS.get(config);var view=ref==null?null:ref.get();if(view==null){view=new Forge1192ModConfigView(config);VIEWS.put(config,new java.lang.ref.WeakReference<>(view));}return view;
    }
    public ModConfig nativeConfig(){return config;}
    public Type getType(){return Type.fromNative(config.getType());}
    public String getFileName(){return config.getFileName();}
    public String getModId(){return config.getModId();}
    public IConfigSpec getSpec(){return config.getSpec();}
    public CommentedConfig getConfigData(){var loaded=config.getLoadedConfig();return loaded==null?null:loaded.config();}
    public Path getFullPath(){return config.getFullPath();}
    public void save(){Forge1192NativeConfigBridge.write(this);}
}
