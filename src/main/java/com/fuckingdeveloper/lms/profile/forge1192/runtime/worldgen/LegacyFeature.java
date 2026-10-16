package com.fuckingdeveloper.lms.profile.forge1192.runtime.worldgen;

import com.fuckingdeveloper.lms.target.neoforge263.worldgen.Target263WorldgenAdapter;
import java.util.Objects;
/** Source feature kind; configuration binding is a target-adapter operation. */
public final class LegacyFeature<C extends LegacyFeatureConfiguration> {
    public enum Kind { TREE, ORE }
    public final Kind kind;
    private LegacyFeature(Kind kind){this.kind=kind;}
    public static final LegacyFeature<LegacyTreeConfiguration> TREE=new LegacyFeature<>(Kind.TREE);
    public static final LegacyFeature<LegacyOreConfiguration> ORE=new LegacyFeature<>(Kind.ORE);
    public static final LegacyFeature<LegacyTreeConfiguration> f_65760_=TREE;
    public static final LegacyFeature<LegacyOreConfiguration> f_65731_=ORE;
    public net.minecraft.world.level.levelgen.feature.Feature bind(C config){
        return Target263WorldgenAdapter.bind(this,Objects.requireNonNull(config));
    }
}
