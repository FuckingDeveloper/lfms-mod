package com.fuckingdeveloper.lms.runtime;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.Objects;

/** Source NBT equality is distinct from native component equality. */
public final class Forge1192StackComparisonBridge {
    private Forge1192StackComparisonBridge() {}
    public static boolean sameItem(ItemStack a, ItemStack b) { return !b.isEmpty() && a.is(b.getItem()); }
    public static boolean isSame(ItemStack a, ItemStack b) {
        return a==b || (!a.isEmpty() && !b.isEmpty() && sameItem(a,b));
    }
    public static boolean tagMatches(ItemStack a, ItemStack b) {
        if (a.isEmpty() && b.isEmpty()) return true;
        if (a.isEmpty() || b.isEmpty()) return false;
        if (!Objects.equals(Forge1192ItemBridge.getTag(a), Forge1192ItemBridge.getTag(b))) return false;
        requireSourceTagProjection(a); requireSourceTagProjection(b);
        // Source IForgeItem default provides only named vanilla shulker stacks.
        // Exact base Item instances cannot be those BlockItems. Source stack
        // attachment subjects are currently rejected by the event bridge.
        if (a.getItem().getClass()!=Item.class || b.getItem().getClass()!=Item.class)
            throw new UnsupportedOperationException("Source stack capability equality requires proven provider state: "
                    + a.getItem().getClass().getName() + ", " + b.getItem().getClass().getName());
        return true;
    }
    private static void requireSourceTagProjection(ItemStack stack) {
        if (stack.isDamageableItem()) throw new UnsupportedOperationException("Source comparison requires legacy damage/NBT projection");
        for (var entry : stack.getComponentsPatch().keySet()) {
            if (entry!=net.minecraft.core.component.DataComponents.CUSTOM_DATA)
                throw new UnsupportedOperationException("Source comparison requires native component/NBT projection: " + entry);
        }
    }
    public static boolean isSameItemSameTags(ItemStack a, ItemStack b) { return a.is(b.getItem()) && tagMatches(a,b); }
    public static boolean matches(ItemStack a, ItemStack b) {
        if (a.isEmpty() && b.isEmpty()) return true;
        return !a.isEmpty() && !b.isEmpty() && a.getCount()==b.getCount() && isSameItemSameTags(a,b);
    }
}
