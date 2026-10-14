package com.fuckingdeveloper.lms.runtime.render;

import java.lang.invoke.*;
import java.util.Objects;
import net.minecraft.client.gui.components.AbstractWidget;

/** Source direct coordinates are raw storage, independent of target getter overrides. */
public final class Forge1192WidgetFieldBridge {
    private static final VarHandle X = field("x"), Y = field("y");
    private Forge1192WidgetFieldBridge() {}
    public static int getX(AbstractWidget widget) { return (int)X.get(Objects.requireNonNull(widget)); }
    public static int getY(AbstractWidget widget) { return (int)Y.get(Objects.requireNonNull(widget)); }
    public static void setX(AbstractWidget widget, int value) { X.set(Objects.requireNonNull(widget), value); }
    public static void setY(AbstractWidget widget, int value) { Y.set(Objects.requireNonNull(widget), value); }
    private static VarHandle field(String name) {
        try { return MethodHandles.privateLookupIn(AbstractWidget.class, MethodHandles.lookup()).findVarHandle(AbstractWidget.class, name, int.class); }
        catch (NoSuchFieldException | IllegalAccessException failure) { throw new ExceptionInInitializerError(failure); }
    }
}
