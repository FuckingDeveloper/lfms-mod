package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import net.minecraft.client.renderer.texture.AbstractTexture;
import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;

/** Exact source getId/bind pair projected to the real native texture, without invented GPU IDs. */
public final class LegacyGuiTextureHandles {
    private LegacyGuiTextureHandles() {}
    public static void setShaderTexture(int unit,AbstractTexture texture){
        if(unit!=0)throw new UnsupportedOperationException("Source GUI texture unit "+unit);
        java.util.Objects.requireNonNull(texture,"texture");
        if(texture.getClass().getClassLoader() instanceof ManagedLegacyClassLoader)
            throw new UnsupportedOperationException("Source custom texture ID/binding semantics are not adapted: "+texture.getClass().getName());
        var scope=Forge1192TooltipGuiBridge.scope();scope.textureObject=texture;scope.texture=null;
    }
}
