package com.fuckingdeveloper.lms.runtime;

import com.fuckingdeveloper.lms.runtime.item.LegacyFuelItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.common.NeoForge;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Source fuel queries never read event-materialized target CookingFuel components. */
public final class Forge1192FuelBridge {
    private record Rule(Identifier item, TagKey<Item> tag, int ticks) {
        boolean matches(ItemStack stack) {
            return tag != null ? stack.is(tag)
                    : stack.getItem().builtInRegistryHolder().key().identifier().equals(item);
        }
    }
    private static final TagKey<Item> NEVER_FUEL = TagKey.create(Registries.ITEM, Identifier.parse("minecraft:non_flammable_wood"));
    private static final List<Rule> RULES = loadRules();
    private Forge1192FuelBridge() {}

    public static int itemBurnTime(Item item, ItemStack stack, RecipeType<?> type) {
        if (item instanceof LegacyFuelItem legacy) return legacy.lms$burnTime(stack, type);
        if (item instanceof com.fuckingdeveloper.lms.runtime.item.LegacyIForgeItem legacy) return legacy.getBurnTime(stack,type);
        return -1;
    }
    public static int stackBurnTime(ItemStack stack, RecipeType<?> type) {
        return itemBurnTime(stack.getItem(), stack, type);
    }
    /** Verified IForgeItem default, including explicit source-super dispatch. */
    public static int defaultBurnTime(Item item, ItemStack stack, RecipeType<?> type) { return -1; }

    public static int vanillaBurnTime(ItemStack stack) {
        if (stack.isEmpty() || stack.is(NEVER_FUEL)) return 0;
        int ticks = 0;
        // Source LinkedHashMap additions overwrite earlier matching rules.
        for (Rule rule : RULES) if (rule.matches(stack)) ticks = rule.ticks();
        return ticks;
    }
    public static int getBurnTime(ItemStack stack, RecipeType<?> type) {
        if (stack.isEmpty()) return 0;
        int ticks = stackBurnTime(stack, type);
        return getItemBurnTime(stack, ticks == -1 ? vanillaBurnTime(stack) : ticks, type);
    }
    public static int getItemBurnTime(ItemStack stack, int ticks, RecipeType<?> type) {
        var event = new LegacyFurnaceFuelBurnTimeEvent(stack, ticks, type);
        NeoForge.EVENT_BUS.post(event);
        return event.getBurnTime();
    }
    private static List<Rule> loadRules() {
        try (var input = Forge1192FuelBridge.class.getResourceAsStream("/lms/profile/forge1192/vanilla-fuels.tsv")) {
            if (input == null) throw new IllegalStateException("Pinned source fuel rules missing");
            var rules = new ArrayList<Rule>();
            for (String line : new String(input.readAllBytes(), StandardCharsets.UTF_8).split("\n")) {
                if (line.isBlank() || line.startsWith("#")) continue;
                String[] row = line.split("\t");
                if (row.length != 3) throw new IllegalStateException("Malformed source fuel rule");
                var id = Identifier.parse(row[1]); int ticks = Integer.parseInt(row[2]);
                if (ticks < 0) throw new IllegalStateException("Negative source vanilla fuel duration");
                switch (row[0]) {
                    case "item" -> rules.add(new Rule(id, null, ticks));
                    case "tag" -> rules.add(new Rule(null, TagKey.create(Registries.ITEM, id), ticks));
                    default -> throw new IllegalStateException("Unknown source fuel rule kind");
                }
            }
            return List.copyOf(rules);
        } catch (IOException failure) { throw new UncheckedIOException(failure); }
    }
}
