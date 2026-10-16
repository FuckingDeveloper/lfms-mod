package com.fuckingdeveloper.lms.profile.forge1192.runtime.item;

/** Source enum identity/order is independent of additions to the target enum. */
public enum LegacyUseAnimation {
    NONE, EAT, DRINK, BLOCK, BOW, SPEAR, CROSSBOW, SPYGLASS, TOOT_HORN;
    public net.minecraft.world.item.ItemUseAnimation target() {
        return net.minecraft.world.item.ItemUseAnimation.valueOf(name());
    }
}
