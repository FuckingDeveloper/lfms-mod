package com.fuckingdeveloper.lms.runtime.advancement;

import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.server.PlayerAdvancements;

/** Listener view used by the source-era hand-written trigger implementation. */
public final class Forge1192LegacyCriterionListener<T extends CriterionTriggerInstance> {
    private final T triggerInstance;
    private final PlayerAdvancements.TriggerInstanceKey key;

    Forge1192LegacyCriterionListener(T triggerInstance, PlayerAdvancements.TriggerInstanceKey key) {
        this.triggerInstance = triggerInstance;
        this.key = key;
    }

    public T getTriggerInstance() {
        return triggerInstance;
    }

    public T m_13685_() {
        return triggerInstance;
    }

    public void run(PlayerAdvancements advancements) {
        advancements.award(key.advancement(), key.criterion());
    }

    public void m_13686_(PlayerAdvancements advancements) {
        run(advancements);
    }
}
