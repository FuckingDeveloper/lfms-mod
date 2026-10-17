package com.fuckingdeveloper.lms.mixin;

import com.fuckingdeveloper.lms.target.neoforge263.Target263SourceEntityFactories;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.*;

@Mixin({EntityType.class,EntityType.Builder.class})
abstract class Target263SourceEntityFactoryMixin implements Target263SourceEntityFactories.Access {
    @Unique private Target263SourceEntityFactories.Intent lms$factory;
    public Target263SourceEntityFactories.Intent lms$sourceClientFactory(){return lms$factory;}
    public void lms$sourceClientFactory(Target263SourceEntityFactories.Intent intent){lms$factory=intent;}
}
