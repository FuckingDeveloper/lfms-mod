package com.fuckingdeveloper.lms.runtime.render;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;

/** Source listener intent, with native-only gesture fields scoped to a dispatch. */
public final class Forge1192GuiPointerBridge {
    public interface Click { boolean lms$mouseClicked(double x, double y, int button); }
    public interface Release { boolean lms$mouseReleased(double x, double y, int button); }
    public interface Drag { boolean lms$mouseDragged(double x, double y, int button, double dx, double dy); }
    private record Context(MouseButtonEvent event, boolean twice) {}
    private static final ThreadLocal<Context> ACTIVE = new ThreadLocal<>();
    private Forge1192GuiPointerBridge() {}

    private static int sourceButton(MouseButtonEvent event) {
        int button = Forge1192KeyboardBridge.sourceValue(InputConstants.Type.MOUSE.getOrCreate(event.button()));
        if (button < 0) throw new UnsupportedOperationException("Unmapped native pointer button: " + event.button());
        return button;
    }
    private static boolean scoped(MouseButtonEvent event, boolean twice, java.util.function.BooleanSupplier action) {
        var previous = ACTIVE.get();
        ACTIVE.set(new Context(event, twice));
        try { return action.getAsBoolean(); }
        finally { if (previous == null) ACTIVE.remove(); else ACTIVE.set(previous); }
    }
    public static boolean clicked(Click receiver, MouseButtonEvent event, boolean twice) {
        return observed("click", receiver, event, () -> scoped(event, twice, () -> receiver.lms$mouseClicked(event.x(), event.y(), sourceButton(event))));
    }
    public static boolean released(Release receiver, MouseButtonEvent event) {
        return observed("release", receiver, event, () -> scoped(event, false, () -> receiver.lms$mouseReleased(event.x(), event.y(), sourceButton(event))));
    }
    public static boolean dragged(Drag receiver, MouseButtonEvent event, double dx, double dy) {
        return observed("drag", receiver, event, () -> scoped(event, false, () -> receiver.lms$mouseDragged(event.x(), event.y(), sourceButton(event), dx, dy)));
    }
    private static boolean observed(String operation, Object receiver, MouseButtonEvent event, java.util.function.BooleanSupplier action) {
        boolean result = action.getAsBoolean();
        if (Boolean.getBoolean("lms.traceGuiInput")) org.slf4j.LoggerFactory.getLogger(Forge1192GuiPointerBridge.class).info(
                "LMS GUI pointer op={} receiver={} x={} y={} button={} consumed={}", operation, receiver.getClass().getName(), event.x(), event.y(), event.button(), result);
        return result;
    }
    /** Reuse exact native input where unchanged; source-forwarded operands take precedence. */
    public static MouseButtonEvent event(double x, double y, int button) {
        int nativeButton = Forge1192KeyboardBridge.mouseCode(button);
        var context = ACTIVE.get();
        if (context != null && context.event.x() == x && context.event.y() == y && context.event.button() == nativeButton)
            return context.event;
        return new MouseButtonEvent(x, y, new MouseButtonInfo(nativeButton, context == null ? 0 : context.event.modifiers()));
    }
    public static boolean twice() { var context = ACTIVE.get(); return context != null && context.twice; }
    public static boolean mouseClicked(GuiEventListener receiver, double x, double y, int button) {
        if (receiver instanceof Click source) return source.lms$mouseClicked(x, y, button);
        return receiver.mouseClicked(event(x, y, button), twice());
    }
    public static boolean mouseReleased(GuiEventListener receiver, double x, double y, int button) {
        if (receiver instanceof Release source) return source.lms$mouseReleased(x, y, button);
        return receiver.mouseReleased(event(x, y, button));
    }
    public static boolean mouseDragged(GuiEventListener receiver, double x, double y, int button, double dx, double dy) {
        if (receiver instanceof Drag source) return source.lms$mouseDragged(x, y, button, dx, dy);
        return receiver.mouseDragged(event(x, y, button), dx, dy);
    }
}
