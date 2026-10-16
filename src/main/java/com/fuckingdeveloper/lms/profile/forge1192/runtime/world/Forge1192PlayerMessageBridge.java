package com.fuckingdeveloper.lms.profile.forge1192.runtime.world;

import java.lang.invoke.*;
import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;

/** Source boolean selects chat versus action bar; target separates the operations. */
public final class Forge1192PlayerMessageBridge {
    private Forge1192PlayerMessageBridge(){}
    private static final ClassValue<Optional<MethodHandle>> SOURCE=new ClassValue<>() {
        @Override protected Optional<MethodHandle> computeValue(Class<?> type) {
            try {
                var lookup=MethodHandles.privateLookupIn(type,MethodHandles.lookup());MethodHandle found=null;
                for(String name:List.of("m_5661_","displayClientMessage"))try {
                    var method=lookup.findVirtual(type,name,MethodType.methodType(void.class,Component.class,boolean.class));
                    if(found!=null)throw new IllegalStateException("Ambiguous source player message overrides: "+type.getName());found=method;
                }catch(NoSuchMethodException absent){}
                return Optional.ofNullable(found);
            }catch(IllegalAccessException failure){throw new IllegalStateException("Cannot access source player message override",failure);}
        }
    };
    public static void display(Player player,Component message,boolean overlay) {
        Objects.requireNonNull(player);var source=SOURCE.get(player.getClass()).orElse(null);
        if(source!=null) {
            try{source.invoke(player,message,overlay);return;}
            catch(RuntimeException|Error failure){throw failure;}catch(Throwable failure){throw new IllegalStateException("Source player message override failed",failure);}
        }
        if(player instanceof ServerPlayer)serverDisplay(player,message,overlay);
        else if(net.neoforged.fml.loading.FMLEnvironment.getDist()==net.neoforged.api.distmarker.Dist.CLIENT && Client.isLocal(player))clientDisplay(player,message,overlay);
        else baseDisplay(player,message,overlay);
    }
    /** Inspected source Player default has an empty body, on both logical sides. */
    public static void baseDisplay(Player player,Component message,boolean overlay){Objects.requireNonNull(player);}
    public static void serverDisplay(Player player,Component message,boolean overlay) {
        if(!(player instanceof ServerPlayer server))throw new IllegalArgumentException("Source ServerPlayer message has foreign receiver");
        server.sendSystemMessage(message,overlay);
    }
    public static void clientDisplay(Player player,Component message,boolean overlay){Client.display(player,message,overlay);}
    private static final class Client {
        static boolean isLocal(Player player){return player instanceof net.minecraft.client.player.LocalPlayer;}
        static void display(Player player,Component message,boolean overlay) {
            if(!isLocal(player))throw new IllegalArgumentException("Source LocalPlayer message has foreign receiver");
            var listener=net.minecraft.client.Minecraft.getInstance().gui.chatListener();
            if(overlay)listener.handleOverlay(message);else listener.handleSystemMessage(message,false);
        }
    }
}
