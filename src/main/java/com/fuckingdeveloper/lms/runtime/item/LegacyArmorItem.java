package com.fuckingdeveloper.lms.runtime.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;

/**
 * Forge/Minecraft 1.19.2 ArmorItem compatibility superclass.
 *
 * <p>Source slot/default attributes, durability, repair callback and equip operation.
 * Worn extraction is implemented separately by the target client adapter.
 */
public class LegacyArmorItem extends Item {
    private final LegacyArmorMaterial legacyMaterial;
    private final EquipmentSlot legacySlot;

    private static final java.util.UUID[] ARMOR_IDS = java.util.Arrays.stream(new String[]{
            "845db27c-c624-495f-8c9f-6020a9a58b6b", "d8499b04-0e66-4726-ab29-64469d734e0d",
            "9f3d476d-c118-4544-8365-64846904b48e", "2ad3f246-fee1-4e67-b886-69fd380bb150"})
            .map(java.util.UUID::fromString).toArray(java.util.UUID[]::new);
    private final int defense;
    private final float toughness, knockback;

    public LegacyArmorItem(LegacyArmorMaterial material, EquipmentSlot slot, Item.Properties properties) {
        this(new Construction(material, slot, properties));
    }
    private LegacyArmorItem(Construction construction) {
        super(construction.properties);
        legacyMaterial = construction.material;
        legacySlot = construction.slot;
        defense = legacyMaterial.m_7365_(legacySlot);
        toughness = legacyMaterial.m_6651_();
        knockback = legacyMaterial.m_6649_();
        construction.item = this;
    }
    private static final class Construction {
        final LegacyArmorMaterial material;
        final EquipmentSlot slot;
        final Item.Properties properties;
        LegacyArmorItem item;
        Construction(LegacyArmorMaterial material, EquipmentSlot slot, Item.Properties properties) {
            if (slot != EquipmentSlot.HEAD && slot != EquipmentSlot.CHEST
                    && slot != EquipmentSlot.LEGS && slot != EquipmentSlot.FEET)
                throw new IllegalArgumentException("Source ArmorItem requires a humanoid armor slot: " + slot);
            this.material = material; this.slot = slot; this.properties = properties;
            int durability = material.m_7366_(slot);
            properties.component(net.minecraft.core.component.DataComponents.EQUIPPABLE,
                    net.minecraft.world.item.equipment.Equippable.builder(slot)
                            .setEquipSound(net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(material.m_7344_())).build());
            properties.componentInitializer = properties.componentInitializer.andThen((components, lookup, key) -> {
                if (item == null) throw new IllegalStateException("Armor defaults queried before base construction completed");
                applyDefaults(components, durability, material.m_6646_(), item::m_7167_);
            });
        }
    }
    public com.google.common.collect.Multimap<net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute>,
            net.minecraft.world.entity.ai.attributes.AttributeModifier> m_7167_(EquipmentSlot slot) {
        return baseModifiers(legacySlot, slot, defense, toughness, knockback);
    }
    public static com.google.common.collect.Multimap<net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute>,
            net.minecraft.world.entity.ai.attributes.AttributeModifier> baseModifiers(
            EquipmentSlot equipped, EquipmentSlot query, int defense, float toughness, float knockback) {
        var result = com.google.common.collect.HashMultimap.<net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute>,
                net.minecraft.world.entity.ai.attributes.AttributeModifier>create();
        if (query == equipped) {
            var id = ARMOR_IDS[query.getIndex()];
            var operation = net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE;
            result.put(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR,
                    com.fuckingdeveloper.lms.runtime.Forge1192AttributeBridge.modifier(id,"Armor modifier",defense,operation));
            result.put(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS,
                    com.fuckingdeveloper.lms.runtime.Forge1192AttributeBridge.modifier(id,"Armor toughness",toughness,operation));
            if (knockback > 0) result.put(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE,
                    com.fuckingdeveloper.lms.runtime.Forge1192AttributeBridge.modifier(id,"Armor knockback resistance",knockback,operation));
        }
        return result;
    }
    public static void applyDefaults(net.minecraft.core.component.DataComponentMap.Builder components,
            int durability, int enchantability,
            java.util.function.Function<EquipmentSlot,? extends com.google.common.collect.Multimap<?,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier>> query) {
        if (components.getOrDefault(net.minecraft.core.component.DataComponents.MAX_DAMAGE, 0) == 0 && durability > 0) {
            components.set(net.minecraft.core.component.DataComponents.MAX_DAMAGE, durability);
            components.set(net.minecraft.core.component.DataComponents.DAMAGE, 0);
        }
        components.set(net.minecraft.core.component.DataComponents.MAX_STACK_SIZE, 1);
        var attributes = net.minecraft.world.item.component.ItemAttributeModifiers.builder();
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET}) {
            query.apply(slot).forEach((attribute, modifier) -> attributes.add(
                    com.fuckingdeveloper.lms.runtime.Forge1192AttributeBridge.holder(attribute), modifier,
                    net.minecraft.world.entity.EquipmentSlotGroup.bySlot(slot)));
        }
        components.set(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS, attributes.build());
        if (enchantability > 0) components.set(net.minecraft.core.component.DataComponents.ENCHANTABLE,
                new net.minecraft.world.item.enchantment.Enchantable(enchantability));
    }
    public boolean m_6832_(net.minecraft.world.item.ItemStack stack, net.minecraft.world.item.ItemStack candidate) {
        return legacyMaterial.m_6230_().test(candidate);
    }
    public String getArmorTexture(net.minecraft.world.item.ItemStack stack, net.minecraft.world.entity.Entity wearer,
            EquipmentSlot slot, String type) { return null; }
    public String lmsDefaultArmorTexture(EquipmentSlot slot) {
        var name = net.minecraft.resources.Identifier.parse(legacyMaterial.m_6082_());
        return name.getNamespace() + ":textures/models/armor/" + name.getPath() + "_layer_"
                + (slot == EquipmentSlot.LEGS ? "2" : "1") + ".png";
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
