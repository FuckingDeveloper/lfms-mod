package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.target.neoforge263.Target263ReloadLookupBridge;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.tags.*;
import net.minecraft.world.damagesource.*;
import java.util.*;
import java.util.stream.Stream;

/** Isolated real fire tag needed by native item initializers; never binds global registries. */
final class NativeDefaultComponentFixture {
    static void run(Runnable checks) {
        var damage=new MappedRegistry<DamageType>(Registries.DAMAGE_TYPE,Lifecycle.stable());
        var fire=damage.register(DamageTypes.IN_FIRE,new DamageType("inFire",0.1F),RegistrationInfo.BUILT_IN);
        damage.freeze();var tags=damage.prepareTagReload(new TagLoader.LoadResult<>(Registries.DAMAGE_TYPE,Map.of(DamageTypeTags.IS_FIRE,List.of(fire))));tags.apply();
        var builtins=RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var provider=HolderLookup.Provider.create(Stream.concat(builtins.listRegistries(),Stream.of(damage)));
        Target263ReloadLookupBridge.run(provider,checks);
    }
}
