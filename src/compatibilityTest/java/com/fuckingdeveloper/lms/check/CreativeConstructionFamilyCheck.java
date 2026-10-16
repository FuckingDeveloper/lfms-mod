package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.item.Forge1192VanillaConstructionBridge;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

final class CreativeConstructionFamilyCheck {
    static void run() {
        var first = Identifier.fromNamespaceAndPath("creative_probe", "first");
        var second = Identifier.fromNamespaceAndPath("creative_probe", "second");
        var third = Identifier.fromNamespaceAndPath("creative_probe", "third");
        var tab = CreativeModeTabs.searchTab();
        var properties = Forge1192VanillaConstructionBridge.recordLegacyCreativeTab(new Item.Properties(), tab);
        // Exercise the FML-applied properties accessor without mutating frozen registries.
        check(java.util.Arrays.stream(Item.class.getDeclaredMethods()).anyMatch(m -> m.getName().contains("lms$captureCreativeIntent")), "native constructor hook absent");
        Forge1192VanillaConstructionBridge.captureCreativeItem(properties.setId(ResourceKey.create(Registries.ITEM, first)));
        Forge1192VanillaConstructionBridge.captureCreativeItem(properties.setId(ResourceKey.create(Registries.ITEM, second)));
        check(Forge1192VanillaConstructionBridge.legacyCreativeTabIntent(first).orElseThrow() == tab,
                "native constructor lost first source membership");
        check(Forge1192VanillaConstructionBridge.legacyCreativeTabIntent(second).orElseThrow() == tab,
                "source properties reuse consumed membership");
        var other = net.minecraft.world.item.CreativeModeTab.builder()
                .title(net.minecraft.network.chat.Component.literal("other"))
                .icon(() -> net.minecraft.world.item.ItemStack.EMPTY).displayItems((p, o) -> {}).build();
        Forge1192VanillaConstructionBridge.recordLegacyCreativeTab(properties, other);
        Forge1192VanillaConstructionBridge.captureCreativeItem(properties.setId(ResourceKey.create(Registries.ITEM, third)));
        check(Forge1192VanillaConstructionBridge.legacyCreativeTabIntent(first).orElseThrow() == tab,
                "later property mutation changed constructed item membership");
        check(Forge1192VanillaConstructionBridge.legacyCreativeTabIntent(third).orElseThrow() == other,
                "later construction lost updated membership");
        var nativeId = Identifier.fromNamespaceAndPath("creative_probe", "native");
        Forge1192VanillaConstructionBridge.captureCreativeItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, nativeId)));
        check(Forge1192VanillaConstructionBridge.legacyCreativeTabIntent(nativeId).isEmpty(),
                "native item acquired source membership");
        System.out.println("PASS: FML-applied constructor hook and keyed source tab intent across keyed construction, properties reuse and later mutation");
    }
    private static void check(boolean value, String reason) { if (!value) throw new AssertionError(reason); }
}
