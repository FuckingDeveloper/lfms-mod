package com.fuckingdeveloper.lms.profile.forge1192.runtime.registry;

import java.util.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.*;

/** Concrete and interface calls share the same source read policy. */
public final class Forge1192RegistryReadBridge {
    private Forge1192RegistryReadBridge() {}
    private static LegacyForgeRegistryView<?> view(Object token) {
        Objects.requireNonNull(token,"source registry receiver");
        if(token instanceof LegacyForgeRegistryView<?> view)return view;
        if(token instanceof Registry<?> registry) {
            var view=LegacyRegistryManagerView.ACTIVE.getRegistry(registry.key());
            if(view!=null&&view.nativeRegistry()==registry)return view;
        }
        throw new UnsupportedOperationException("No admitted source ACTIVE read policy for registry receiver "+token.getClass().getName());
    }
    public static boolean isRegistry(Object token) {
        if(token instanceof LegacyForgeRegistryView<?>)return true;
        if(token instanceof Registry<?> registry) {
            var view=LegacyRegistryManagerView.ACTIVE.getRegistry(registry.key());
            return view!=null&&view.nativeRegistry()==registry;
        }
        return false;
    }
    public static LegacyForgeRegistryView<?> cast(Object token) {
        if(token==null)return null;
        if(isRegistry(token))return view(token);
        throw new ClassCastException("Not an admitted source ForgeRegistry representation: "+token.getClass().getName());
    }
    public static Object getValue(Object token,Identifier key){return Forge1192RegistrationContext.getValue(token,key);}
    public static Identifier getKey(Object token,Object value){return Forge1192RegistrationContext.getKey(token,value);}
    public static boolean containsKey(Object token,Identifier key){return Forge1192RegistrationContext.containsKey(token,key);}
    public static Iterator<?> iterator(Object token) {
        var delegate=Forge1192RegistrationContext.iterator(token);
        return new Iterator<Object>(){public boolean hasNext(){return delegate.hasNext();}public Object next(){return delegate.hasNext()?delegate.next():null;}};
    }
    public static Spliterator<?> spliterator(Object token){return Spliterators.spliteratorUnknownSize(iterator(token),0);}
    public static void forEach(Object token,java.util.function.Consumer<Object> action){Objects.requireNonNull(action);iterator(token).forEachRemaining(action);}
    public static LegacyTagManager<?> tags(Object token){return Forge1192RegistryTagBridge.tags(token);}
    public static Identifier getRegistryName(Object token){return view(token).getRegistryName();}
    public static ResourceKey<?> getRegistryKey(Object token){return view(token).getRegistryKey();}
    public static Identifier getDefaultKey(Object token){return view(token).getDefaultKey();}
    @SuppressWarnings({"rawtypes","unchecked"})
    public static Optional<?> getResourceKey(Object token,Object value){return ((LegacyForgeRegistryView)view(token)).getResourceKey(value);}
    @SuppressWarnings({"rawtypes","unchecked"})
    public static boolean containsValue(Object token,Object value){return ((LegacyForgeRegistryView)view(token)).containsValue(value);}
    public static boolean isEmpty(Object token){return view(token).isEmpty();}
    public static Set<?> getKeys(Object token){return view(token).getKeys();}
    public static Collection<?> getValues(Object token){return view(token).getValues();}
    public static Set<?> getEntries(Object token){return view(token).getEntries();}
}
