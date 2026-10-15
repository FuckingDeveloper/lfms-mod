package com.fuckingdeveloper.lms.runtime;

import java.util.*;
import java.util.function.Supplier;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforgespi.language.IModInfo;
import com.fuckingdeveloper.lms.runtime.render.Forge1192ConfigScreenBridge;

/** Source identity/extension view. Never a member of the native FML container list. */
public class Forge1192ModContainerView {
    private final ModContainer nativeContainer;
    private final Forge1192RuntimeSession session;
    private final IModInfo info;
    private final Map<Class<?>,Supplier<?>> extensions = new IdentityHashMap<>();

    private Forge1192ModContainerView(ModContainer container, Forge1192RuntimeSession session, IModInfo info) {
        this.nativeContainer=container; this.session=session; this.info=Objects.requireNonNull(info);
    }
    public static Forge1192ModContainerView nativeView(ModContainer container) {
        return container instanceof net.neoforged.fml.mclanguageprovider.MinecraftModContainer
                ? new MinecraftContainer(container) : new Forge1192ModContainerView(container,null,container.getModInfo());
    }
    static Forge1192ModContainerView managed(Forge1192RuntimeSession session) {
        return new Forge1192ModContainerView(null,session,Forge1192MetadataView.read(session.artifact(),session.modId()));
    }
    public static final class MinecraftContainer extends Forge1192ModContainerView {
        private MinecraftContainer(ModContainer container) { super(container,null,container.getModInfo()); }
    }
    public String getModId(){return info.getModId();}
    public IModInfo getModInfo(){return info;}
    public Optional<ModContainer> nativeOwner(){return Optional.ofNullable(nativeContainer);}
    Forge1192RuntimeSession session(){return session;}
    public void registerExtensionPoint(Class<?> type,Supplier<?> supplier) {
        Objects.requireNonNull(type);Objects.requireNonNull(supplier);
        if(nativeContainer!=null) Forge1192ConfigScreenBridge.registerExtensionPoint(nativeContainer,type,supplier);
        else synchronized(extensions){extensions.put(type,supplier);}
    }
    public Optional<?> getCustomExtension(Class<?> type) {
        if(nativeContainer!=null)return Forge1192ConfigScreenBridge.getCustomExtension(nativeContainer,type);
        Supplier<?> supplier; synchronized(extensions){supplier=extensions.get(type);}
        return supplier==null?Optional.empty():Optional.ofNullable(supplier.get());
    }
    void clear(){synchronized(extensions){extensions.clear();}}
}
