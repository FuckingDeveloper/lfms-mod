package com.fuckingdeveloper.lms.runtime.worldgen;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.stateproviders.*;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.neoforged.neoforge.registries.RegisterEvent;

/** Target-native algorithms; source settings and virtual callback identity survive. */
public final class Target263WorldgenAdapter {
    private Target263WorldgenAdapter() {}
    private record Placement(WorldGenLevel world,TreeFeature tree,LegacyTreeConfiguration source) {}
    private static final ThreadLocal<Placement> ACTIVE=new ThreadLocal<>();
    public static void register(RegisterEvent event){
        event.register(Registries.FEATURE_TYPE,Identifier.fromNamespaceAndPath("lms","legacy_tree_intent"),()->TreeIntent.CODEC);
    }
    public static Feature bind(LegacyFeature<?> feature,LegacyFeatureConfiguration config){
        return switch(feature.kind){
            case TREE -> {if(!(config instanceof LegacyTreeConfiguration tree))throw new IllegalArgumentException("TREE needs tree configuration");yield new TreeIntent(tree);}
            case ORE -> {if(!(config instanceof LegacyOreConfiguration ore))throw new IllegalArgumentException("ORE needs ore configuration");yield new OreFeature(ore.targetStates.stream().map(t->new BlockReplacement(t.target(),t.state())).toList(),ore.size,ore.discardChanceOnAirExposure);}
        };
    }
    public static WorldGenLevel world(LevelSimulatedReader level){
        if(!(level instanceof WorldGenLevel world))throw new UnsupportedOperationException("Source generation requires native WorldGenLevel");return world;
    }
    public static LegacyTreeConfiguration configuration(TreeFeature tree){
        var placement=ACTIVE.get();if(placement==null||placement.tree!=tree)throw new IllegalStateException("Source tree callback has no matching native placement scope");return placement.source;
    }
    public static TreeFeature tree(LegacyTreeConfiguration config){
        var placement=ACTIVE.get();if(placement==null||placement.source!=config)throw new IllegalStateException("Source tree settings escaped placement scope");return placement.tree;
    }
    public static BlockState state(BlockStateProvider provider,RandomSource random,BlockPos pos){
        var placement=ACTIVE.get();if(placement==null)throw new IllegalStateException("Source state-provider lookup needs placement context");return provider.getState(placement.world,random,pos);
    }
    public static net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate survival(BlockState state,Vec3i offset){
        return new net.minecraft.world.level.levelgen.blockpredicates.WouldSurvivePredicate(offset,state);
    }
    public static RuleTest stone(){return RuleTest.either(new TagMatchTest(BlockTags.HEIGHT_SPECIFIC_ORE_REPLACEABLES),HeightMatchTest.min(0),new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES));}
    public static RuleTest deepslate(){return RuleTest.either(new TagMatchTest(BlockTags.HEIGHT_SPECIFIC_ORE_REPLACEABLES),HeightMatchTest.max(8),new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES));}
    public static RuleTest naturalStone(){return new TagMatchTest(BlockTags.BASE_STONE_OVERWORLD);}
    public static RuleTest nether(){return new TagMatchTest(BlockTags.BASE_STONE_NETHER);}
    public static RuleTest netherrack(){return new BlockMatchTest(net.minecraft.world.level.block.Blocks.NETHERRACK);}
    public static <P> MapCodec<P> recordCodec(Codec<P> codec){
        // RecordCodecBuilder.create returns this exact wrapper: retain its map layout and keys.
        if(codec instanceof MapCodec.MapCodecCodec<P> record)return record.codec();
        throw new UnsupportedOperationException("Source placer codec is not a proven map codec: "+codec);
    }
    public static <P extends net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer> TrunkPlacerType<P> registerTrunk(String id,Codec<P> codec){
        var type=new TrunkPlacerType<>(recordCodec(codec));
        com.fuckingdeveloper.lms.runtime.Forge1192RegistrationContext.register(
            BuiltInRegistries.TRUNK_PLACER_TYPE,Identifier.parse(id),type);
        return type;
    }
    /** Codec retains source-only flags while the actual engine stays native TreeFeature. */
    public static final class TreeIntent implements Feature {
        public static final MapCodec<TreeIntent> CODEC=RecordCodecBuilder.mapCodec(i->i.group(
            TreeFeature.CODEC.fieldOf("tree").forGetter(t->t.settings),Codec.BOOL.fieldOf("force_dirt").forGetter(t->t.source.forceDirt)
        ).apply(i,TreeIntent::new));
        private final TreeFeature settings;
        private final LegacyTreeConfiguration source;
        public TreeIntent(LegacyTreeConfiguration source){
            this.source=source;this.settings=new TreeFeature(Holder.direct(source.trunkProvider),source.trunkPlacer,Holder.direct(source.foliageProvider),source.foliagePlacer,source.rootPlacer,source.minimumSize,source.decorators,source.ignoreVines,Holder.direct(source.dirtProvider));
        }
        private TreeIntent(TreeFeature settings,boolean force){
            this.settings=settings;this.source=new LegacyTreeConfiguration(settings.trunkProvider().value(),settings.trunkPlacer(),settings.foliageProvider().value(),settings.foliagePlacer(),settings.rootPlacer(),settings.belowTrunkProvider().value(),settings.minimumSize(),settings.decorators(),settings.ignoreVines(),force);
        }
        public LegacyTreeConfiguration source(){return source;}
        public TreeFeature settings(){return settings;}
        @Override public MapCodec<TreeIntent> codec(){return CODEC;}
        @Override public boolean place(WorldGenLevel level,ChunkGenerator generator,RandomSource random,BlockPos pos){
            var below=source.forceDirt?source.dirtProvider:RuleBasedStateProvider.ifTrueThenProvide(BlockPredicate.not(BlockPredicate.matchesTag(BlockTags.CANNOT_REPLACE_BELOW_TREE_TRUNK)),source.dirtProvider);
            var effective=new TreeFeature(settings.trunkProvider(),settings.trunkPlacer(),settings.foliageProvider(),settings.foliagePlacer(),settings.rootPlacer(),settings.minimumSize(),settings.decorators(),settings.ignoreVines(),Holder.direct(below));
            var previous=ACTIVE.get();ACTIVE.set(new Placement(level,effective,source));
            try{return effective.place(level,generator,random,pos);}finally{if(previous==null)ACTIVE.remove();else ACTIVE.set(previous);}
        }
    }
}
