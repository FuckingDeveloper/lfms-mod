package com.fuckingdeveloper.lms.profile.forge1192.runtime.chat;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.item.ItemStackTemplate;

import java.net.URI;
import java.util.Objects;

/** Adapts the Forge 1.19.2 Action/String click-event value model to 26.3. */
public final class Forge1192ChatEventBridge {
    private Forge1192ChatEventBridge() {}

    public static ClickEvent click(ClickEvent.Action action, String value) {
        Objects.requireNonNull(action, "click action");
        Objects.requireNonNull(value, "click value");
        if (action == ClickEvent.Action.OPEN_URL) {
            return new ClickEvent.OpenUrl(URI.create(value));
        }
        if (action == ClickEvent.Action.OPEN_FILE) {
            return new ClickEvent.OpenFile(value);
        }
        if (action == ClickEvent.Action.RUN_COMMAND) {
            return new ClickEvent.RunCommand(value);
        }
        if (action == ClickEvent.Action.SUGGEST_COMMAND) {
            return new ClickEvent.SuggestCommand(value);
        }
        if (action == ClickEvent.Action.CHANGE_PAGE) {
            try {
                return new ClickEvent.ChangePage(Integer.parseInt(value));
            } catch (NumberFormatException ignored) {
                // Some source-generation screens deliberately used CHANGE_PAGE
                // as an in-process navigation signal with a non-numeric value.
                return new LegacyStringClickEvent(action, value);
            }
        }
        if (action == ClickEvent.Action.COPY_TO_CLIPBOARD) {
            return new ClickEvent.CopyToClipboard(value);
        }
        throw new IllegalArgumentException(
                "Click action did not exist in Forge 1.19.2: " + action);
    }

    public static ClickEvent.Action action(ClickEvent event) {
        return Objects.requireNonNull(event, "click event").action();
    }

    public static String value(ClickEvent event) {
        Objects.requireNonNull(event, "click event");
        if (event instanceof LegacyStringClickEvent legacy) return legacy.value();
        if (event instanceof ClickEvent.OpenUrl openUrl) return openUrl.uri().toString();
        if (event instanceof ClickEvent.OpenFile openFile) return openFile.path();
        if (event instanceof ClickEvent.RunCommand runCommand) return runCommand.command();
        if (event instanceof ClickEvent.SuggestCommand suggestCommand) {
            return suggestCommand.command();
        }
        if (event instanceof ClickEvent.ChangePage changePage) {
            return Integer.toString(changePage.page());
        }
        if (event instanceof ClickEvent.CopyToClipboard copy) return copy.value();
        throw new IllegalArgumentException(
                "Click event has no Forge 1.19.2 string representation: " + event);
    }

    private record LegacyStringClickEvent(ClickEvent.Action action, String value)
            implements ClickEvent {}

    public static HoverEvent hover(HoverEvent.Action action, Object value) {
        Objects.requireNonNull(action, "hover action");
        Objects.requireNonNull(value, "hover value");
        if (action == HoverEvent.Action.SHOW_TEXT && value instanceof Component component) {
            return new HoverEvent.ShowText(component);
        }
        if (action == HoverEvent.Action.SHOW_ITEM) {
            if (value instanceof LegacyHoverItemStackInfo legacy) {
                return new HoverEvent.ShowItem(legacy.template());
            }
            if (value instanceof ItemStackTemplate template) {
                return new HoverEvent.ShowItem(template);
            }
        }
        if (action == HoverEvent.Action.SHOW_ENTITY
                && value instanceof HoverEvent.EntityTooltipInfo entity) {
            return new HoverEvent.ShowEntity(entity);
        }
        throw new IllegalArgumentException(
                "Invalid Forge 1.19.2 hover value for " + action + ": "
                        + value.getClass().getName());
    }

    public static HoverEvent.Action hoverAction(HoverEvent event) {
        return Objects.requireNonNull(event, "hover event").action();
    }

    public static Object hoverValue(HoverEvent event, HoverEvent.Action expectedAction) {
        Objects.requireNonNull(event, "hover event");
        Objects.requireNonNull(expectedAction, "expected hover action");
        if (event.action() != expectedAction) return null;
        if (event instanceof HoverEvent.ShowText showText) return showText.value();
        if (event instanceof HoverEvent.ShowItem showItem) {
            return new LegacyHoverItemStackInfo(showItem.item());
        }
        if (event instanceof HoverEvent.ShowEntity showEntity) return showEntity.entity();
        throw new IllegalArgumentException(
                "Hover event has no Forge 1.19.2 value representation: " + event);
    }
}
