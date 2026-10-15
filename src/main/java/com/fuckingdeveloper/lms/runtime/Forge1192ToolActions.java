package com.fuckingdeveloper.lms.runtime;

import java.util.*;
import net.neoforged.neoforge.common.ItemAbility;

/** Constants and mutable identity sets verified from Forge 1.19.2 bytecode. */
public final class Forge1192ToolActions {
    public Forge1192ToolActions() {}
    public static final ItemAbility AXE_DIG = ItemAbility.get("axe_dig");
    public static final ItemAbility PICKAXE_DIG = ItemAbility.get("pickaxe_dig");
    public static final ItemAbility SHOVEL_DIG = ItemAbility.get("shovel_dig");
    public static final ItemAbility HOE_DIG = ItemAbility.get("hoe_dig");
    public static final ItemAbility SWORD_DIG = ItemAbility.get("sword_dig");
    public static final ItemAbility SHEARS_DIG = ItemAbility.get("shears_dig");
    public static final ItemAbility AXE_STRIP = ItemAbility.get("axe_strip");
    public static final ItemAbility AXE_SCRAPE = ItemAbility.get("axe_scrape");
    public static final ItemAbility AXE_WAX_OFF = ItemAbility.get("axe_wax_off");
    public static final ItemAbility SHOVEL_FLATTEN = ItemAbility.get("shovel_flatten");
    public static final ItemAbility SWORD_SWEEP = ItemAbility.get("sword_sweep");
    public static final ItemAbility SHEARS_HARVEST = ItemAbility.get("shears_harvest");
    public static final ItemAbility SHEARS_CARVE = ItemAbility.get("shears_carve");
    public static final ItemAbility SHEARS_DISARM = ItemAbility.get("shears_disarm");
    public static final ItemAbility HOE_TILL = ItemAbility.get("till");
    public static final ItemAbility SHIELD_BLOCK = ItemAbility.get("shield_block");
    public static final ItemAbility FISHING_ROD_CAST = ItemAbility.get("fishing_rod_cast");
    public static final Set<ItemAbility> DEFAULT_AXE_ACTIONS = of(AXE_DIG, AXE_STRIP, AXE_SCRAPE, AXE_WAX_OFF);
    public static final Set<ItemAbility> DEFAULT_HOE_ACTIONS = of(HOE_DIG, HOE_TILL);
    public static final Set<ItemAbility> DEFAULT_SHOVEL_ACTIONS = of(SHOVEL_DIG, SHOVEL_FLATTEN);
    public static final Set<ItemAbility> DEFAULT_PICKAXE_ACTIONS = of(PICKAXE_DIG);
    public static final Set<ItemAbility> DEFAULT_SWORD_ACTIONS = of(SWORD_DIG, SWORD_SWEEP);
    public static final Set<ItemAbility> DEFAULT_SHEARS_ACTIONS = of(SHEARS_DIG, SHEARS_HARVEST, SHEARS_CARVE, SHEARS_DISARM);
    public static final Set<ItemAbility> DEFAULT_SHIELD_ACTIONS = of(SHIELD_BLOCK);
    public static final Set<ItemAbility> DEFAULT_FISHING_ROD_ACTIONS = of(FISHING_ROD_CAST);
    private static Set<ItemAbility> of(ItemAbility... actions) {
        Set<ItemAbility> result = Collections.newSetFromMap(new IdentityHashMap<>());
        Collections.addAll(result, actions);
        return result;
    }
}
