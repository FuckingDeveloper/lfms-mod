package com.fuckingdeveloper.lms.target.neoforge263;

import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.*;

/** Actual paired target declarations, checked against exact source identifiers. */
public final class Target263VanillaTagBridge {
    private record Projection(String member,String identifier,boolean item) {}
    private static final ConcurrentHashMap<Projection,TagKey<?>> CACHE=new ConcurrentHashMap<>();
    private Target263VanillaTagBridge() {}
    public static TagKey<?> item(String member,String identifier) { return resolve(new Projection(member,identifier,true)); }
    public static TagKey<?> block(String member,String identifier) { return resolve(new Projection(member,identifier,false)); }
    private static TagKey<?> resolve(Projection key) {
        return CACHE.computeIfAbsent(key,k -> {
            try {
                var field=BlockItemTags.class.getField(k.member());
                if(field.getType()!=BlockItemTagId.class) throw new IllegalStateException("Target field is not a block/item tag pair: "+k.member());
                var pair=(BlockItemTagId)field.get(null);
                TagKey<?> tag=k.item()?pair.item():pair.block();
                if(!tag.location().toString().equals(k.identifier())||!tag.registry().equals(k.item()?Registries.ITEM:Registries.BLOCK))
                    throw new IllegalStateException("Source/target paired tag identity differs: "+k+" -> "+tag);
                return tag;
            } catch(ReflectiveOperationException failure) { throw new IllegalStateException("Target paired tag declaration unavailable: "+k.member(),failure); }
        });
    }
}
