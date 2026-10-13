package com.fuckingdeveloper.lms.runtime.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;

/**
 * Forge/Minecraft 1.19.2 ArmorItem compatibility superclass.
 *
 * <p>This preserves the removed superclass shape so legacy subclasses can link.
 * Slot/sound and the base equip operation are adapted here. Attributes,
 * source durability/repair/dispenser behavior and worn models remain separate
 * compatibility gaps.</p>
 */
public class LegacyArmorItem extends Item {
    private final LegacyArmorMaterial legacyMaterial;
    private final EquipmentSlot legacySlot;

    public LegacyArmorItem(LegacyArmorMaterial material, EquipmentSlot slot, Item.Properties properties) {
        super(properties.component(net.minecraft.core.component.DataComponents.EQUIPPABLE,
                net.minecraft.world.item.equipment.Equippable.builder(slot)
                        .setEquipSound(net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT
                                .wrapAsHolder(material.m_7344_())).build()));
        this.legacyMaterial = material;
        this.legacySlot = slot;
    }

    protected final LegacyArmorMaterial lmsLegacyMaterial() {
        return legacyMaterial;
    }

    protected final EquipmentSlot lmsLegacyArmorSlot() {
        return legacySlot;
    }

    @Override
    public net.minecraft.world.InteractionResult use(net.minecraft.world.level.Level level,
            net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        var result = equip(level, player, hand);
        return result instanceof net.minecraft.world.InteractionResult.Success success
                ? success.heldItemTransformedTo(player.getItemInHand(hand)) : result;
    }

    public LegacyInteractionResultHolder<net.minecraft.world.item.ItemStack> m_7203_(net.minecraft.world.level.Level level,
            net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        return new LegacyInteractionResultHolder<>(equip(level, player, hand), player.getItemInHand(hand));
    }

    private net.minecraft.world.InteractionResult equip(net.minecraft.world.level.Level level,
            net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (!player.getItemBySlot(legacySlot).isEmpty()) return net.minecraft.world.InteractionResult.FAIL;
        player.setItemSlot(legacySlot, stack.copy());
        if (!level.isClientSide()) player.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this));
        stack.setCount(0);
        var result = level.isClientSide() ? net.minecraft.world.InteractionResult.SUCCESS
                : net.minecraft.world.InteractionResult.CONSUME;
        return result;
    }
}
