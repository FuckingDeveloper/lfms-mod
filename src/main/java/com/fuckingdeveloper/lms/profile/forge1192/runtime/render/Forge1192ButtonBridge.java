package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Objects;
import net.minecraft.client.gui.components.Button;

/** Exact source default callback; native input is not part of source activation. */
public final class Forge1192ButtonBridge {
    private static final VarHandle ON_PRESS = onPressField();
    private Forge1192ButtonBridge() {}
    public static Button create(int x,int y,int width,int height,net.minecraft.network.chat.Component title,Button.OnPress press) {
        return Button.builder(title,press).bounds(x,y,width,height).build();
    }
    public static void press(Button receiver) {
        Objects.requireNonNull(receiver, "button");
        if (receiver instanceof LegacyButtonPressCallbacks source) source.lms$sourceButtonPress();
        else basePress(receiver);
    }
    public static void basePress(Button receiver) {
        ((Button.OnPress)ON_PRESS.get(Objects.requireNonNull(receiver, "button"))).onPress(receiver);
    }
    private static VarHandle onPressField() {
        try {
            return MethodHandles.privateLookupIn(Button.class, MethodHandles.lookup()).findVarHandle(Button.class, "onPress", Button.OnPress.class);
        } catch (NoSuchFieldException | IllegalAccessException failure) { throw new ExceptionInInitializerError(failure); }
    }
}
