package com.fuckingdeveloper.lms.profile.forge1192.runtime.registry;

/**
 * Verifier-visible replacement for Forge 1.19.2 RegistryObject.
 *
 * LMS preserves the holder type across transformed producer/consumer boundaries.
 * Executable holder operations are adapted separately by the profile transformer.
 */
public final class LegacyRegistryObject implements java.util.function.Supplier<Object> {
    private net.neoforged.neoforge.registries.DeferredHolder<?,?> deferred;
    private java.util.function.Supplier<?> declaration;
    private java.util.function.BooleanSupplier declarationPresent;
    public static LegacyRegistryObject declaration(net.minecraft.resources.Identifier id,java.util.function.Supplier<?> value,java.util.function.BooleanSupplier present) {
        var result=new LegacyRegistryObject(id.getNamespace(),id.getPath());result.declaration=value;result.declarationPresent=present;return result;
    }
    private final String namespace;
    private final String path;

    private LegacyRegistryObject(String namespace, String path) {
        this.namespace = namespace;
        this.path = path;
    }

    public static LegacyRegistryObject builtin(String namespace, String path) {
        return new LegacyRegistryObject(namespace, path);
    }

    public static LegacyRegistryObject deferred(net.neoforged.neoforge.registries.DeferredHolder<?,?> holder) {
        var id=holder.getId();var result=new LegacyRegistryObject(id.getNamespace(),id.getPath());result.deferred=holder;return result;
    }
    public boolean isDeferred(){return deferred!=null||declaration!=null;}
    public Object deferredValue(){if(declaration!=null)return declaration.get();if(deferred==null)throw new IllegalStateException("Not a deferred source holder");return deferred.get();}
    @Override public Object get(){return com.fuckingdeveloper.lms.profile.forge1192.runtime.registry.Forge1192BuiltinRegistryBridge.get(this);}
    public net.minecraft.resources.Identifier getId(){return net.minecraft.resources.Identifier.fromNamespaceAndPath(namespace,path);}
    public boolean isPresent(){if(declaration!=null)return declarationPresent.getAsBoolean();if(deferred==null)throw new UnsupportedOperationException("Builtin holder presence has no verified source projection");return deferred.isBound();}

    public String namespace() { return namespace; }
    public String path() { return path; }
}
