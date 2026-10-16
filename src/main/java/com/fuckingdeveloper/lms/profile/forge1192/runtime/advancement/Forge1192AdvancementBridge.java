package com.fuckingdeveloper.lms.profile.forge1192.runtime.advancement;

import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

import java.util.Optional;

/** Target construction adapters for the Forge 1.19.2 advancement value model. */
public final class Forge1192AdvancementBridge {
    private Forge1192AdvancementBridge() {}

    public static DisplayInfo displayInfo(
            ItemStack icon,
            Component title,
            Component description,
            Identifier background,
            AdvancementType type,
            boolean showToast,
            boolean announceToChat,
            boolean hidden) {
        Optional<ClientAsset.ResourceTexture> targetBackground = background == null
                ? Optional.empty()
                : Optional.of(new ClientAsset.ResourceTexture(background));
        return new DisplayInfo(
                ItemStackTemplate.fromNonEmptyStack(icon),
                title,
                description,
                targetBackground,
                type,
                showToast,
                announceToChat,
                hidden);
    }
}
