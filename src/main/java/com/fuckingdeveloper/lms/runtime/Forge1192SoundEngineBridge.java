package com.fuckingdeveloper.lms.runtime;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Objects;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;

/** Exact source Mixin field accessors retained across the 26.3 sound runtime. */
public final class Forge1192SoundEngineBridge {
    private static final VarHandle SOUND_ENGINE = field(SoundManager.class, "soundEngine", SoundEngine.class);
    private static final VarHandle CHANNEL_ACCESS = field(SoundEngine.class, "channelAccess", ChannelAccess.class);
    private static final VarHandle SOUND_BUFFERS = field(SoundEngine.class, "soundBuffers", SoundBufferLibrary.class);

    private Forge1192SoundEngineBridge() {}

    public static SoundEngine soundEngine(SoundManager manager) {
        return (SoundEngine) SOUND_ENGINE.get(Objects.requireNonNull(manager, "sound manager"));
    }

    public static ChannelAccess channelAccess(SoundEngine engine) {
        return (ChannelAccess) CHANNEL_ACCESS.get(Objects.requireNonNull(engine, "sound engine"));
    }

    public static SoundBufferLibrary soundBuffers(SoundEngine engine) {
        return (SoundBufferLibrary) SOUND_BUFFERS.get(Objects.requireNonNull(engine, "sound engine"));
    }

    private static VarHandle field(Class<?> owner, String name, Class<?> type) {
        try {
            return MethodHandles.privateLookupIn(owner, MethodHandles.lookup())
                    .findVarHandle(owner, name, type);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
