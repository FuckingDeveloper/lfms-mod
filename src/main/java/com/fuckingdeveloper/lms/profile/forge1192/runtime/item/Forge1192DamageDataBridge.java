package com.fuckingdeveloper.lms.profile.forge1192.runtime.item;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.item.LegacyArmorItem;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.itemhandler.LegacyMutableTagCarrier;
import net.minecraft.world.item.ItemStack;

/** Source IForgeItem damage defaults and the native component mirror, without replacing overrides. */
public final class Forge1192DamageDataBridge {
    private Forge1192DamageDataBridge() {}
    public static boolean source(ItemStack stack) {
        return stack.getItem().getClass().getClassLoader() instanceof ManagedLegacyClassLoader
                || stack.getItem() instanceof LegacyArmorItem
                || ((LegacyMutableTagCarrier)(Object)stack).lms$existingMutableTags() != null;
    }
    public static int getDefault(ItemStack stack) {
        var tag = Forge1192ItemBridge.getTag(stack);
        int damage = tag == null ? 0 : tag.getIntOr("Damage", 0);
        mirror(stack, damage);
        return damage;
    }
    public static void setDefault(ItemStack stack, int damage) {
        int value = Math.max(0, damage);
        Forge1192ItemBridge.getOrCreateTag(stack).putInt("Damage", value);
        mirror(stack, value);
    }
    public static void mirror(ItemStack stack, int damage) {
        ((LegacyMutableTagCarrier)(Object)stack).lms$mutableTags().mirrorDamage(stack, damage);
    }
}
