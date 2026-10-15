package com.fuckingdeveloper.lms.runtime.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.*;
import net.minecraft.network.chat.Component;
import java.util.Objects;

/** Source notification identity/intent consumed by the real native scheduler and renderer. */
public class Forge1192SystemToast implements Toast {
    public enum Id {
        TUTORIAL_HINT, NARRATOR_TOGGLE, WORLD_BACKUP, WORLD_GEN_SETTINGS_TRANSFER,
        PACK_LOAD_FAILURE, WORLD_ACCESS_FAILURE, PACK_COPY_FAILURE, PERIODIC_NOTIFICATION,
        CHAT_PREVIEW_WARNING(10000), UNSECURE_SERVER_WARNING(10000);
        public final long displayTime;
        private final SystemToast.SystemToastId nativeId;
        Id(){this(5000);}
        Id(long displayTime){this.displayTime=displayTime;this.nativeId=new SystemToast.SystemToastId(displayTime);}
    }
    private final Id id;
    private Component title,message;
    private SystemToast delegate;
    private boolean resetPending;
    public Forge1192SystemToast(Id id,Component title,Component message){this.id=Objects.requireNonNull(id);this.title=Objects.requireNonNull(title);this.message=message;}
    private SystemToast nativeToast(){if(delegate==null){delegate=new SystemToast(id.nativeId,title,message);if(resetPending){delegate.reset(title,message);resetPending=false;}}return delegate;}
    @Override public Id getToken(){return id;}
    @Override public int width(){return nativeToast().width();}
    @Override public int height(){return nativeToast().height();}
    @Override public Visibility getWantedVisibility(){return nativeToast().getWantedVisibility();}
    @Override public void update(ToastManager manager,long elapsed){nativeToast().update(manager,elapsed);}
    @Override public void extractRenderState(GuiGraphicsExtractor graphics,Font font,long elapsed){nativeToast().extractRenderState(graphics,font,elapsed);}
    public void reset(Component title,Component message){this.title=Objects.requireNonNull(title);this.message=message;if(delegate!=null)delegate.reset(title,message);else resetPending=true;}
    public static void add(ToastManager manager,Id id,Component title,Component message){manager.addToast(new Forge1192SystemToast(id,title,message));}
    public static void addOrUpdate(ToastManager manager,Id id,Component title,Component message){var toast=manager.getToast(Forge1192SystemToast.class,id);if(toast==null)add(manager,id,title,message);else toast.reset(title,message);}
    public static Forge1192SystemToast multiline(Minecraft client,Id id,Component title,Component message){Objects.requireNonNull(client);Objects.requireNonNull(message);if(client!=Minecraft.getInstance())throw new UnsupportedOperationException("Source toast requires the authoritative native client");return new Forge1192SystemToast(id,title,message);}
    public static ToastManager getToasts(Minecraft client){return Objects.requireNonNull(client).gui.toastManager();}
    public static void onWorldAccessFailure(Minecraft client,String world){add(getToasts(client),Id.WORLD_ACCESS_FAILURE,Component.translatable("selectWorld.access_failure"),Component.literal(world));}
    public static void onWorldDeleteFailure(Minecraft client,String world){add(getToasts(client),Id.WORLD_ACCESS_FAILURE,Component.translatable("selectWorld.delete_failure"),Component.literal(world));}
    public static void onPackCopyFailure(Minecraft client,String name){add(getToasts(client),Id.PACK_COPY_FAILURE,Component.translatable("pack.copyFailure"),Component.literal(name));}
}
