package com.fuckingdeveloper.lms.profile.forge1192.runtime.worldgen;
import java.util.List;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
/** Source ore settings; target adapter owns native replacement objects. */
public final class LegacyOreConfiguration implements LegacyFeatureConfiguration {
    public final List<TargetBlockState> targetStates;
    public final int size; public final float discardChanceOnAirExposure;
    public LegacyOreConfiguration(List<TargetBlockState> targets,int size,float discard){this.targetStates=List.copyOf(targets);this.size=size;this.discardChanceOnAirExposure=discard;}
    public LegacyOreConfiguration(List<TargetBlockState> targets,int size){this(targets,size,0);}
    public LegacyOreConfiguration(RuleTest target,BlockState state,int size){this(List.of(new TargetBlockState(target,state)),size,0);}
    public static TargetBlockState target(RuleTest target,BlockState state){return new TargetBlockState(target,state);}
    public static TargetBlockState m_161021_(RuleTest target,BlockState state){return target(target,state);}
    public record TargetBlockState(RuleTest target,BlockState state){public TargetBlockState{java.util.Objects.requireNonNull(target);java.util.Objects.requireNonNull(state);}}
}
