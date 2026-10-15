package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.runtime.Forge1192ItemBridge;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;

final class DamageDataFamilyCheck {
    static void run() {
        NativeDefaultComponentFixture.run(() -> {
            ItemStack stack = com.fuckingdeveloper.lms.runtime.Forge1192EarlyItemStackBridge.create(Items.IRON_CHESTPLATE);
            stack.setDamageValue(100);
            check(Forge1192ItemBridge.hasTag(stack), "source hasTag lost native damage");
            var tag = Forge1192ItemBridge.getOrCreateTag(stack);
            check(tag.getIntOr("Damage", -1) == 100, "native damage not exposed in source tag");
            tag.putInt("Damage", 40);
            check(stack.getDamageValue() == 40 && stack.get(DataComponents.DAMAGE) == 40, "source mutation not projected");
            var copy = stack.copy();
            tag.remove("Damage");
            check(stack.getDamageValue() == 0 && copy.getDamageValue() == 40, "source removal/copy isolation lost");
            stack.set(DataComponents.DAMAGE, 27);
            check(stack.getDamageValue() == 27 && tag.getIntOr("Damage", -1) == 27, "later native write lost");
            stack.setDamageValue(-7);
            check(stack.getDamageValue() == 0, "source lower bound lost");
            stack.setDamageValue(9999);
            check(stack.getDamageValue() == 9999 && stack.get(DataComponents.DAMAGE) == stack.getMaxDamage(), "source raw overshoot/native bounded mirror lost");
            Forge1192ItemBridge.setTag(stack, null);
            check(stack.getDamageValue() == 0 && stack.get(DataComponents.DAMAGE) == 0, "source whole-tag removal retained damage");
        });
        System.out.println("PASS: source/native damage defaults, live tag mutation/removal, copies, native replacement, raw overshoot and component mirror; custom setters require runtime");
    }
    private static void check(boolean value,String message) { if(!value)throw new AssertionError(message); }
}
