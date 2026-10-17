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
