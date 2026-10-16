package com.fuckingdeveloper.lms.target.neoforge263;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.predicates.DataComponentMatchers;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.LockCode;
import net.neoforged.neoforge.registries.RegisterEvent;

/** The source plain-text custom-name lock expressed as a serializable native predicate. */
public record Target263SourceNameLock(String name) implements DataComponentPredicate {
    public static final Codec<Target263SourceNameLock> CODEC=Codec.STRING.xmap(Target263SourceNameLock::new,Target263SourceNameLock::name);
    public static final DataComponentPredicate.Type<Target263SourceNameLock> TYPE=new DataComponentPredicate.ConcreteType<>(CODEC);
    public static void register(RegisterEvent event) {
        event.register(Registries.DATA_COMPONENT_PREDICATE_TYPE,Identifier.fromNamespaceAndPath("lms","source_plain_name_lock"),()->TYPE);
    }
    public boolean matches(DataComponentGetter components) {
        var customName=components.get(DataComponents.CUSTOM_NAME);
        return customName!=null&&name.equals(customName.getString());
    }
    public static LockCode lock(String name) {
        if(name.isEmpty())return LockCode.NO_LOCK;
        var matcher=DataComponentMatchers.Builder.components().partial(TYPE,new Target263SourceNameLock(name)).build();
        // The old lock explicitly rejects an empty stack as well as absent custom names.
        return new LockCode(ItemPredicate.Builder.item().withCount(MinMaxBounds.Ints.atLeast(1)).withComponents(matcher).build());
    }
    public static String sourceName(LockCode lock) {
        if(lock.equals(LockCode.NO_LOCK))return "";
        var predicate=lock.predicate();
        if(predicate.items().isEmpty()&&predicate.count().equals(MinMaxBounds.Ints.atLeast(1))
                &&predicate.components().exact().isEmpty()&&predicate.components().partial().size()==1
                &&predicate.components().partial().get(TYPE) instanceof Target263SourceNameLock source)return source.name();
        throw new UnsupportedOperationException("Native container lock cannot be represented as a source plain-name lock");
    }
}
