package com.fuckingdeveloper.lms.target.neoforge263.render;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.item.LegacyArmorItem;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Live source callback only during extraction; submitted state contains plain identifiers. */
public final class Target263ArmorTextureBridge {
    private static final String PREFIX = "lms_legacy_armor/";
    private Target263ArmorTextureBridge() {}
    public static ItemStack extract(LivingEntity wearer, EquipmentSlot slot) {
        return snapshot(wearer.getItemBySlot(slot), wearer, slot);
    }
    public static ItemStack snapshot(ItemStack input, LivingEntity wearer, EquipmentSlot slot) {
        if (!(input.getItem() instanceof LegacyArmorItem armor))
            throw new IllegalArgumentException("Source armor extraction requires a source ArmorItem");
        var equip = input.get(DataComponents.EQUIPPABLE);
        if (equip == null || equip.slot() != slot) return ItemStack.EMPTY;
        if (equip.assetId().isPresent()) return input.copy();
        rejectCustomModel(armor.getClass());
        String selected = armor.getArmorTexture(input, wearer, slot, null);
        ItemStack copy = input.copy();
        Identifier texture = Identifier.parse(selected == null ? armor.lmsDefaultArmorTexture(slot) : selected);
        ResourceKey<EquipmentAsset> asset = ResourceKey.create(EquipmentAssets.ROOT_ID,
                texture.withPath(path -> PREFIX + path));
        copy.set(DataComponents.EQUIPPABLE, new Equippable(equip.slot(), equip.equipSound(), Optional.of(asset),
                equip.cameraOverlay(), equip.allowedEntities(), equip.dispensable(), equip.swappable(),
                equip.damageOnHurt(), equip.equipOnInteract(), equip.canBeSheared(), equip.shearingSound()));
        return copy;
    }
    private static final ClassValue<Boolean> CUSTOM_MODEL = new ClassValue<>() {
        protected Boolean computeValue(Class<?> type) {
            for (var current = type; current != null; current = current.getSuperclass()) {
                if (!(current.getClassLoader() instanceof com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader loader)) continue;
                try {
                    byte[] bytes = loader.originalClassBytes(current.getName());
                    if (bytes == null) throw new IllegalStateException("No source armor metadata: " + current.getName());
                    var found = new boolean[1];
                    new org.objectweb.asm.ClassReader(bytes).accept(new org.objectweb.asm.ClassVisitor(org.objectweb.asm.Opcodes.ASM9) {
                        @Override public org.objectweb.asm.MethodVisitor visitMethod(int access,String name,String desc,String sig,String[] exceptions) {
                            if (name.equals("initializeClient") && desc.equals("(Ljava/util/function/Consumer;)V")) found[0] = true;
                            return null;
                        }
                    }, org.objectweb.asm.ClassReader.SKIP_CODE);
                    if (found[0]) return true;
                } catch (java.io.IOException failure) { throw new IllegalStateException("Cannot inspect source armor model", failure); }
            }
            return false;
        }
    };
    private static void rejectCustomModel(Class<?> type) {
        if (CUSTOM_MODEL.get(type)) throw new UnsupportedOperationException("Source custom armor model initialization is not adapted: " + type.getName());
    }
    public static EquipmentClientInfo asset(ResourceKey<EquipmentAsset> key) {
        if (!key.identifier().getPath().startsWith(PREFIX)) return null;
        var layer = new EquipmentClientInfo.Layer(key.identifier());
        return new EquipmentClientInfo(Map.of(EquipmentClientInfo.LayerType.HUMANOID,List.of(layer),
                EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS,List.of(layer),
                EquipmentClientInfo.LayerType.HUMANOID_BABY,List.of(layer)), List.of());
    }
    public static Identifier texture(Identifier encoded) {
        return encoded.getPath().startsWith(PREFIX) ? encoded.withPath(path -> path.substring(PREFIX.length())) : null;
    }
}
