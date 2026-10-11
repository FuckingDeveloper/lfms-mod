package com.fuckingdeveloper.lms.runtime.block;

import com.fuckingdeveloper.lms.runtime.Forge1192VanillaConstructionBridge;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;

/** Minecraft 1.19.2 PressurePlateBlock construction shape on the 26.3 runtime. */
public class LegacyPressurePlateBlock extends PressurePlateBlock {
    public LegacyPressurePlateBlock(
            BlockSetType.PressurePlateSensitivity sensitivity,
            BlockBehaviour.Properties properties) {
        super(Forge1192VanillaConstructionBridge.legacyPressurePlateType(properties, sensitivity),
                properties);
    }
}
