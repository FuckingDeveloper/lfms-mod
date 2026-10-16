package com.fuckingdeveloper.lms.runtime;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/** The source sound codec stores a flat location, independently from target registry holders. */
public final class Forge1192SoundEventValues {
    private Forge1192SoundEventValues() {}
    public static final Codec<SoundEvent> CODEC = Identifier.CODEC.xmap(
            SoundEvent::createVariableRangeEvent, SoundEvent::location);
}
