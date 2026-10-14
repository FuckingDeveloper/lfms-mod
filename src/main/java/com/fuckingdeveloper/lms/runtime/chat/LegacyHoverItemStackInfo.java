package com.fuckingdeveloper.lms.runtime.chat;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

import java.util.Objects;

/** Source-shaped carrier for the removed 1.19.2 hover item-stack value. */
public final class LegacyHoverItemStackInfo {
    private final ItemStackTemplate template;

    public LegacyHoverItemStackInfo(ItemStack stack) {
        this(ItemStackTemplate.fromStack(Objects.requireNonNull(stack, "stack")));
    }

    LegacyHoverItemStackInfo(ItemStackTemplate template) {
        this.template = Objects.requireNonNull(template, "template");
    }

    public ItemStack m_130898_() {
        return template.create();
    }

    ItemStackTemplate template() {
        return template;
    }
}
