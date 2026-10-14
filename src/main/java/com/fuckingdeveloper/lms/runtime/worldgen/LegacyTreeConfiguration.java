package com.fuckingdeveloper.lms.runtime.worldgen;
import java.util.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.rootplacers.RootPlacer;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSize;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
/** Source settings retained across direct placement and native codec roundtrips. */
public final class LegacyTreeConfiguration implements LegacyFeatureConfiguration {
    public final BlockStateProvider trunkProvider,foliageProvider,dirtProvider;
    public final TrunkPlacer trunkPlacer; public final FoliagePlacer foliagePlacer;
    public final Optional<RootPlacer> rootPlacer; public final FeatureSize minimumSize;
    public final List<TreeDecorator> decorators; public final boolean ignoreVines,forceDirt;
    // Verified source field aliases consumed by source virtual callbacks.
    public final BlockStateProvider f_68185_,f_161213_; public final boolean f_161215_;
    public LegacyTreeConfiguration(BlockStateProvider trunk,TrunkPlacer placer,BlockStateProvider foliage,FoliagePlacer leaves,Optional<RootPlacer> roots,BlockStateProvider dirt,FeatureSize size,List<TreeDecorator> decorators,boolean vines,boolean force){
        this.trunkProvider=Objects.requireNonNull(trunk);this.trunkPlacer=Objects.requireNonNull(placer);this.foliageProvider=Objects.requireNonNull(foliage);this.foliagePlacer=Objects.requireNonNull(leaves);this.rootPlacer=Objects.requireNonNull(roots);this.dirtProvider=Objects.requireNonNull(dirt);this.minimumSize=Objects.requireNonNull(size);this.decorators=List.copyOf(decorators);this.ignoreVines=vines;this.forceDirt=force;f_68185_=trunk;f_161213_=foliage;f_161215_=force;
    }
    public static final class TreeConfigurationBuilder {
        private final BlockStateProvider trunk,foliage; private final TrunkPlacer placer;private final FoliagePlacer leaves;private final Optional<RootPlacer> roots;private final FeatureSize size;
        private BlockStateProvider dirt=BlockStateProvider.of(Blocks.DIRT);private List<TreeDecorator> decorators=List.of();private boolean vines,force;
        public TreeConfigurationBuilder(BlockStateProvider trunk,TrunkPlacer placer,BlockStateProvider foliage,FoliagePlacer leaves,FeatureSize size){this(trunk,placer,foliage,leaves,Optional.empty(),size);}
        public TreeConfigurationBuilder(BlockStateProvider trunk,TrunkPlacer placer,BlockStateProvider foliage,FoliagePlacer leaves,Optional<RootPlacer> roots,FeatureSize size){this.trunk=trunk;this.placer=placer;this.foliage=foliage;this.leaves=leaves;this.roots=roots;this.size=size;}
        public TreeConfigurationBuilder ignoreVines(){vines=true;return this;}public TreeConfigurationBuilder forceDirt(){force=true;return this;}
        public TreeConfigurationBuilder dirt(BlockStateProvider provider){dirt=Objects.requireNonNull(provider);return this;}public TreeConfigurationBuilder decorators(List<TreeDecorator> value){decorators=List.copyOf(value);return this;}
        public LegacyTreeConfiguration build(){return new LegacyTreeConfiguration(trunk,placer,foliage,leaves,roots,dirt,size,decorators,vines,force);}
        public TreeConfigurationBuilder m_68249_(List<TreeDecorator> decorators){return decorators(decorators);}
        public TreeConfigurationBuilder m_161260_(BlockStateProvider dirt){return dirt(dirt);}
        public TreeConfigurationBuilder m_68244_(){return ignoreVines();}public TreeConfigurationBuilder m_161262_(){return forceDirt();}public LegacyTreeConfiguration m_68251_(){return build();}
    }
}
