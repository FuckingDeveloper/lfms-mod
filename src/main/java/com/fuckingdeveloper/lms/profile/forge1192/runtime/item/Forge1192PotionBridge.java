package com.fuckingdeveloper.lms.profile.forge1192.runtime.item;

import com.fuckingdeveloper.lms.target.InstalledTargetAdapters;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;

/** Exact source typed item queries. Raw potion/effect NBT and brewing are separate contracts. */
public final class Forge1192PotionBridge {
    public static final Potion EMPTY=InstalledTargetAdapters.emptySourcePotion();
    private Forge1192PotionBridge() {}
    public static Potion byName(String name) { return InstalledTargetAdapters.sourcePotionByName(name); }
    public static String getName(Potion potion,String prefix) { return prefix+InstalledTargetAdapters.sourcePotionName(potion); }
    public static Potion getPotion(ItemStack stack) { return InstalledTargetAdapters.sourcePotion(stack); }
    public static Potion getPotion(CompoundTag tag) { return tag==null?EMPTY:byName(tag.getStringOr("Potion","")); }
    public static ItemStack setPotion(ItemStack stack,Potion potion) { return InstalledTargetAdapters.setSourcePotion(stack,potion); }
    public static ItemStack setCustomEffects(ItemStack stack,Collection<MobEffectInstance> effects) { return InstalledTargetAdapters.setSourcePotionEffects(stack,effects); }
    public static List<MobEffectInstance> getCustomEffects(ItemStack stack) { return InstalledTargetAdapters.sourcePotionEffects(stack); }
    public static List<MobEffectInstance> getAllEffects(Potion potion,Collection<MobEffectInstance> effects) {
        var all=new ArrayList<>(potion.getEffects());all.addAll(effects);return all;
    }
    public static List<MobEffectInstance> getMobEffects(ItemStack stack) { return getAllEffects(getPotion(stack),getCustomEffects(stack)); }
    public static int getColor(ItemStack stack) {
        var custom=InstalledTargetAdapters.sourcePotionColor(stack);
        return custom.orElseGet(()->getPotion(stack)==EMPTY?16253176:getColor(getMobEffects(stack)));
    }
    public static int getColor(Potion potion) { return potion==EMPTY?16253176:getColor(potion.getEffects()); }
    public static int getColor(Collection<MobEffectInstance> effects) {
        if(effects.isEmpty())return 3694022;
        float red=0,green=0,blue=0;int weight=0;
        for(var effect:effects)if(effect.isVisible()) {
            int color=effect.getEffect().value().getColor(),n=effect.getAmplifier()+1;
            red+=n*((color>>16)&255)/255.0F;green+=n*((color>>8)&255)/255.0F;blue+=n*(color&255)/255.0F;weight+=n;
        }
        if(weight==0)return 0;
        return ((int)(red/weight*255.0F)<<16)|((int)(green/weight*255.0F)<<8)|(int)(blue/weight*255.0F);
    }
}
