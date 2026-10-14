package com.fuckingdeveloper.lms.runtime.item;

import net.minecraft.world.InteractionResult;

/** Source action/value pair; never substituted with a target result alone. */
public final class LegacyInteractionResultHolder<T> {
    private final InteractionResult result;
    private final T object;
    public LegacyInteractionResultHolder(InteractionResult result, T object) {
        this.result = java.util.Objects.requireNonNull(result, "interaction result"); this.object = object;
    }
    public InteractionResult m_19089_() { return result; }
    public T m_19095_() { return object; }
    public static <T> LegacyInteractionResultHolder<T> m_19090_(T object) { return new LegacyInteractionResultHolder<>(InteractionResult.SUCCESS, object); }
    public static <T> LegacyInteractionResultHolder<T> m_19092_(T object, boolean client) { return new LegacyInteractionResultHolder<>(client ? InteractionResult.SUCCESS : InteractionResult.CONSUME, object); }
    public static <T> LegacyInteractionResultHolder<T> m_19096_(T object) { return new LegacyInteractionResultHolder<>(InteractionResult.CONSUME, object); }
    public static <T> LegacyInteractionResultHolder<T> m_19098_(T object) { return new LegacyInteractionResultHolder<>(InteractionResult.PASS, object); }
    public static <T> LegacyInteractionResultHolder<T> m_19100_(T object) { return new LegacyInteractionResultHolder<>(InteractionResult.FAIL, object); }
}
