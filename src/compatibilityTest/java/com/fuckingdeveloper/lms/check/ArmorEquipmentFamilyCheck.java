package com.fuckingdeveloper.lms.check;
import com.fuckingdeveloper.lms.runtime.item.*;
import com.fuckingdeveloper.lms.runtime.render.Target263ArmorTextureBridge;
import net.minecraft.core.component.*;
import net.minecraft.resources.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.client.resources.model.*;
import net.minecraft.world.item.equipment.*;
import com.google.common.collect.*;

final class ArmorEquipmentFamilyCheck {
    static void run() {
        var uuids=new java.util.HashSet<Identifier>();
        for(var slot:new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET}) {
            var components=DataComponentMap.builder();
            LegacyArmorItem.applyDefaults(components,LegacyArmorMaterials.IRON.m_7366_(slot),9,
                    query->LegacyArmorItem.baseModifiers(slot,query,LegacyArmorMaterials.IRON.m_7365_(slot),0,0));
            check(components.get(DataComponents.MAX_DAMAGE)==LegacyArmorMaterials.IRON.m_7366_(slot),"slot durability");
            var attributes=components.get(DataComponents.ATTRIBUTE_MODIFIERS);
            check(attributes.modifiers().size()==2,"base defense/toughness absent");
            check(attributes.modifiers().stream().allMatch(entry->entry.slot()==EquipmentSlotGroup.bySlot(slot)),"wrong slot group");
            var defense=attributes.modifiers().stream().filter(entry->entry.attribute().equals(Attributes.ARMOR)).findFirst().orElseThrow();
            check(defense.modifier().amount()==LegacyArmorMaterials.IRON.m_7365_(slot),"defense value");
            check(uuids.add(defense.modifier().id()),"slot modifier identities collide");
        }
        var components=DataComponentMap.builder().set(DataComponents.MAX_DAMAGE,77);
        LegacyArmorItem.applyDefaults(components,165,0,query->HashMultimap.create());
        check(components.get(DataComponents.MAX_DAMAGE)==77,"explicit durability replaced");
        check(components.get(DataComponents.ATTRIBUTE_MODIFIERS).modifiers().isEmpty(),"empty source callback replaced by base defense");
        var raw=DataComponentMap.builder();
        LegacyArmorItem.applyDefaults(raw,165,0,slot->{
            var map=HashMultimap.<Object,AttributeModifier>create();
            if(slot==EquipmentSlot.CHEST)map.put(Attributes.ARMOR.value(),new AttributeModifier(Identifier.parse("armor_check:raw"),3,AttributeModifier.Operation.ADD_VALUE));
            return map;
        });
        check(raw.get(DataComponents.ATTRIBUTE_MODIFIERS).modifiers().getFirst().attribute()==Attributes.ARMOR,"registered source attribute value not projected");
        var zero=DataComponentMap.builder();LegacyArmorItem.applyDefaults(zero,0,0,query->HashMultimap.create());
        check(zero.get(DataComponents.MAX_DAMAGE)==null&&zero.get(DataComponents.ENCHANTABLE)==null,"zero source defaults fabricated damage/enchantability");
        var asset=ResourceKey.create(EquipmentAssets.ROOT_ID,Identifier.parse("armor_check:lms_legacy_armor/textures/custom/legs.png"));
        var info=new EquipmentAssetManager().get(asset);
        for(var type:new EquipmentClientInfo.LayerType[]{EquipmentClientInfo.LayerType.HUMANOID,EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS,EquipmentClientInfo.LayerType.HUMANOID_BABY}) {
            check(info.getLayers(type).getFirst().getTextureLocation(type).toString().equals("armor_check:textures/custom/legs.png"),"actual native asset/layer path");
        }
        check(new EquipmentAssetManager().get(ResourceKey.create(EquipmentAssets.ROOT_ID,Identifier.parse("armor_check:unrelated")))==EquipmentAssetManager.MISSING,"foreign asset synthesized");
        // Force exact extraction/repair target classes to apply their mixins in the smoke process.
        net.minecraft.client.renderer.entity.HumanoidMobRenderer.class.getDeclaredMethods();
        net.minecraft.world.item.ItemStack.class.getDeclaredMethods();
        System.out.println("PASS: four-slot source component defaults/UUID identities, explicit/zero durability, empty callbacks and actual native equipment asset paths; constructors/live predicates require runtime registration");
    }
    static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
