package com.fuckingdeveloper.lms.profile.forge1192.runtime.banner;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.Forge1192LifecycleBridge;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.registry.LegacyRegistryObject;
import java.util.*;
import java.util.function.Supplier;
import net.minecraft.resources.Identifier;

/** Source-owned declarations materialized at the approved pre-setup boundary. */
public final class LegacyBannerPatterns {
    private static final Map<Identifier,Entry> ENTRIES=new LinkedHashMap<>();
    private static final Set<Identifier> ITEMS=new LinkedHashSet<>();
    private static boolean started,prepared;
    private LegacyBannerPatterns() {}
    public static final class Entry {
        private final String owner;
        private final Identifier id;
        private final Supplier<?> factory;
        private LegacyBannerPattern value;
        private boolean attached;
        private Entry(String owner,Identifier id,Supplier<?> factory) {this.owner=owner;this.id=id;this.factory=Objects.requireNonNull(factory);}
        public Object value() {
            if(value==null)throw new IllegalStateException("Source banner declaration not materialized at the approved pre-setup boundary: "+id);
            return value;
        }
        public LegacyRegistryObject holder() {return LegacyRegistryObject.declaration(id,this::value,()->value!=null);}
    }
    public static synchronized Entry declare(String owner,Identifier id,Supplier<?> factory) {
        if(started)throw new IllegalStateException("Late source banner declaration: "+id);
        var entry=new Entry(owner,id,factory);
        if(ENTRIES.putIfAbsent(id,entry)!=null)throw new IllegalStateException("Duplicate source banner declaration: "+id);
        return entry;
    }
    public static synchronized void attach(List<Entry> entries) {
        if(started)throw new IllegalStateException("Source banner register attached after materialization began");
        for(var entry:entries)entry.attached=true;
    }
    public static synchronized void recordItem(Identifier id) {
        if(prepared)throw new IllegalStateException("Source banner item registered after pre-setup materialization: "+id);
        ITEMS.add(id);
    }
    public static synchronized void materialize() {
        if(started)throw new IllegalStateException("Source banner materialization repeated");
        started=true;
        for(var entry:ENTRIES.values())if(entry.attached) {
            try(var ignored=Forge1192LifecycleBridge.enterCallback(entry.owner);
                var mod=com.fuckingdeveloper.lms.profile.forge1192.runtime.registry.Forge1192RegistrationContext.enterCallbackMod(entry.owner)) {
                Object value=Objects.requireNonNull(entry.factory.get(),"Source banner factory returned null: "+entry.id);
                if(value.getClass()!=LegacyBannerPattern.class)throw new UnsupportedOperationException("Unproven source banner declaration subclass: "+entry.id);
                entry.value=(LegacyBannerPattern)value;
            }catch(RuntimeException|Error failure){throw failure;}
            catch(Exception failure){throw new IllegalStateException("Cannot close source banner declaration scope",failure);}
        }
        prepared=true;
    }
    public static synchronized Map<Identifier,LegacyBannerPattern> definitions() {
        if(!prepared)throw new IllegalStateException("Native data loading began before source banner declaration materialization completed");
        Map<Identifier,LegacyBannerPattern> result=new LinkedHashMap<>();
        ENTRIES.forEach((id,entry)->{if(entry.attached)result.put(id,entry.value);});
        return Collections.unmodifiableMap(result);
    }
    public static synchronized Set<Identifier> items() {
        if(!prepared)throw new IllegalStateException("Source banner item declarations are not complete");
        return Set.copyOf(ITEMS);
    }
}
