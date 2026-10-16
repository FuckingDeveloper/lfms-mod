package com.fuckingdeveloper.lms.profile.forge1192.runtime.event;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.render.Forge1192ModelBakingBridge;
import net.neoforged.bus.api.Event;

import java.util.Map;

/** Source-shaped view of Forge 1.19.2's mutable baked-model registry. */
public final class LegacyModelBakingCompletedEvent extends Event {
    private final Forge1192ModelBakingBridge.Context context;

    public LegacyModelBakingCompletedEvent(Forge1192ModelBakingBridge.Context context) {
        this.context = context;
    }

    public Map<Object, Object> getModels() {
        return context.models();
    }

    public Forge1192ModelBakingBridge.Context context() {
        return context;
    }
}
