package com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle;

import net.neoforged.fml.config.IConfigSpec;

/** Typed source token, preserving helper/field descriptors and the existing owner-scope checks. */
public final class Forge1192ModLoadingContext {
    private final Forge1192LifecycleBridge.Context context;
    Forge1192ModLoadingContext(Forge1192LifecycleBridge.Context context){this.context=context;}
    public static Forge1192ModLoadingContext get(){var c=Forge1192LifecycleBridge.requireActive();return c.container().sourceContext(c);}
    public Forge1192ModContainerView getActiveContainer(){return Forge1192LifecycleBridge.getSourceActiveContainer(context);}
    public void setActiveContainer(Forge1192ModContainerView container){Forge1192LifecycleBridge.setActiveContainer(context,container);}
    public String getActiveNamespace(){return Forge1192LifecycleBridge.getActiveNamespace(context);}
    public void registerExtensionPoint(Class<?> type,java.util.function.Supplier<?> supplier){getActiveContainer().registerExtensionPoint(type,supplier);}
    public void registerDisplayTest(LegacyExtensionPoint.DisplayTest test){registerDisplayTest(() -> test);}
    public void registerDisplayTest(java.util.function.Supplier<LegacyExtensionPoint.DisplayTest> test){registerExtensionPoint(LegacyExtensionPoint.DisplayTest.class,test);}
    public void registerDisplayTest(String version,java.util.function.BiPredicate<String,Boolean> test){registerDisplayTest(new LegacyExtensionPoint.DisplayTest(version,test));}
    public void registerDisplayTest(java.util.function.Supplier<String> version,java.util.function.BiPredicate<String,Boolean> test){registerDisplayTest(new LegacyExtensionPoint.DisplayTest(version,test));}
    public void registerConfig(Forge1192ModConfigView.Type type,IConfigSpec spec){com.fuckingdeveloper.lms.target.neoforge263.Target263SourceConfigAdapter.register(context,type,spec);}
    public void registerConfig(Forge1192ModConfigView.Type type,IConfigSpec spec,String filename){com.fuckingdeveloper.lms.target.neoforge263.Target263SourceConfigAdapter.register(context,type,spec,filename);}
}
