package com.fuckingdeveloper.lms.target.neoforge263;

import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.*;

/** Native component boundary; the source absence token is never registered or encoded as a holder. */
public final class Target263PotionContents {
    private static final class SourceAbsence extends Potion { private SourceAbsence() { super("empty"); } }
    public static final Potion EMPTY = new SourceAbsence();
    private Target263PotionContents() {}
    private static net.minecraft.core.component.DataComponentMap sourceComponents(ItemStack stack) {
        if ((Object)stack instanceof com.fuckingdeveloper.lms.profile.forge1192.runtime.itemhandler.LegacyMutableTagCarrier carrier) return carrier.lms$sourceComponents();
        return stack.getComponents();
    }
    public static PotionContents contents(ItemStack stack) {
        var components=sourceComponents(stack);
        var raw=components.get(DataComponents.CUSTOM_DATA);
        if(raw!=null && (raw.tag.contains("Potion")||raw.tag.contains("CustomPotionEffects")||raw.tag.contains("CustomPotionColor")))
            throw new UnsupportedOperationException("Raw source potion tags require schema projection before typed component consumption");
        return components.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
    }
    public static Holder<Potion> holder(Potion potion) {
        Objects.requireNonNull(potion,"potion");
        if(potion==EMPTY)throw new UnsupportedOperationException("Source EMPTY represents absent potion contents, not a native holder");
        return BuiltInRegistries.POTION.getOrThrow(BuiltInRegistries.POTION.getResourceKey(potion)
                .orElseThrow(()->new UnsupportedOperationException("Unregistered source potion has no native holder")));
    }
    public static Potion byName(String name) {
        Identifier id=Identifier.tryParse(name);
        return id==null?EMPTY:BuiltInRegistries.POTION.getOptional(id).orElse(EMPTY);
    }
    public static String name(Potion potion) {
        Objects.requireNonNull(potion,"potion");
        return potion.name()!=null?potion.name():BuiltInRegistries.POTION.getResourceKey(potion)
                .orElseThrow(()->new UnsupportedOperationException("Unnamed unregistered source potion")).identifier().getPath();
    }
    public static Potion getPotion(ItemStack stack) { return contents(stack).potion().map(Holder::value).orElse(EMPTY); }
    public static ItemStack setPotion(ItemStack stack,Potion potion) {
        Objects.requireNonNull(potion,"potion");
        var old=contents(stack);
        var replacement=new PotionContents(potion==EMPTY?Optional.empty():Optional.of(holder(potion)),
                old.customColor(),old.customEffects(),old.customName());
        if(!replacement.equals(old))stack.set(DataComponents.POTION_CONTENTS,replacement);
        return stack;
    }
    public static ItemStack setCustomEffects(ItemStack stack,Collection<MobEffectInstance> effects) {
        if(effects.isEmpty())return stack;
        var old=contents(stack);
        var copy=effects.stream().map(MobEffectInstance::new).toList();
        stack.set(DataComponents.POTION_CONTENTS,new PotionContents(old.potion(),old.customColor(),copy,old.customName()));
        return stack;
    }
    public static List<MobEffectInstance> customEffects(ItemStack stack) { return new ArrayList<>(contents(stack).customEffects()); }
    public static Optional<Integer> customColor(ItemStack stack) { return contents(stack).customColor(); }
    public static void requireRawTagProjection(ItemStack stack) {
        if(sourceComponents(stack).has(DataComponents.POTION_CONTENTS))
            throw new UnsupportedOperationException("Source raw potion NBT projection is not implemented; typed potion contents are supported separately");
    }
}
