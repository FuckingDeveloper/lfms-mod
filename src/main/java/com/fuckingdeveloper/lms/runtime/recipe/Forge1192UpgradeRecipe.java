package com.fuckingdeveloper.lms.runtime.recipe;

import com.fuckingdeveloper.lms.runtime.Forge1192ItemBridge;
import com.fuckingdeveloper.lms.runtime.Forge1192EarlyItemStackBridge;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.RegisterEvent;

/** Source two-slot smithing, distinct from native component-transmutation semantics. */
public class Forge1192UpgradeRecipe extends SimpleSmithingRecipe implements LegacyRecipeResult, LegacyRecipeIngredients {
    public final Ingredient f_44518_, f_44519_;
    public final ItemStack f_44520_;
    private final Identifier f_44521_;
    private static final Identifier SERIALIZER_ID = Identifier.fromNamespaceAndPath("lms", "forge1192_upgrade");
    public static final MapCodec<Forge1192UpgradeRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Identifier.CODEC.fieldOf("legacy_id").forGetter(r -> r.f_44521_),
            Ingredient.CODEC.fieldOf("base").forGetter(r -> r.f_44518_),
            Ingredient.CODEC.fieldOf("addition").forGetter(r -> r.f_44519_),
            ItemStack.CODEC.fieldOf("result").forGetter(r -> r.f_44520_)).apply(i, Forge1192UpgradeRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, Forge1192UpgradeRecipe> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, r -> r.f_44521_,
            Ingredient.CONTENTS_STREAM_CODEC, r -> r.f_44518_,
            Ingredient.CONTENTS_STREAM_CODEC, r -> r.f_44519_,
            ItemStack.STREAM_CODEC, r -> r.f_44520_, Forge1192UpgradeRecipe::new);
    private static final RecipeSerializer<Forge1192UpgradeRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    public static void registerSerializer(RegisterEvent event) {
        event.register(Registries.RECIPE_SERIALIZER, SERIALIZER_ID, () -> SERIALIZER);
    }

    public Forge1192UpgradeRecipe(Identifier id, Ingredient base, Ingredient addition, ItemStack result) {
        super(new Recipe.CommonInfo(true));
        f_44521_ = java.util.Objects.requireNonNull(id);
        f_44518_ = java.util.Objects.requireNonNull(base);
        f_44519_ = java.util.Objects.requireNonNull(addition);
        f_44520_ = java.util.Objects.requireNonNull(result);
    }
    public boolean m_5818_(Container input, Level level) { return f_44518_.test(input.getItem(0)) && f_44519_.test(input.getItem(1)); }
    public ItemStack m_5874_(Container input) {
        ItemStack output = f_44520_.copy();
        var tag = Forge1192ItemBridge.getTag(input.getItem(0));
        if (tag != null) Forge1192ItemBridge.setTag(output, tag.copy());
        return output;
    }
    @Override public boolean matches(SmithingRecipeInput input, Level level) {
        requireNativeBase();
        return input.template().isEmpty() && m_5818_(new SimpleContainer(input.base(), input.addition()), level);
    }
    @Override public ItemStack assemble(SmithingRecipeInput input) { requireNativeBase(); return m_5874_(new SimpleContainer(input.base(), input.addition())); }
    public boolean m_8004_(int width, int height) { return width * height >= 2; }
    public ItemStack m_8043_() { return f_44520_; }
    public Identifier m_6423_() { return f_44521_; }
    public boolean m_44535_(ItemStack item) { return f_44519_.test(item); }
    public ItemStack m_8042_() { return Forge1192EarlyItemStackBridge.create(Items.SMITHING_TABLE); }
    public boolean m_142505_() { return f_44518_.isEmpty() || f_44519_.isEmpty(); }
    public RecipeType<?> m_6671_() { return RecipeType.SMITHING; }
    public LegacyRecipeSerializer<?> m_7707_() { throw new UnsupportedOperationException("Source UpgradeRecipe serializer/network protocol requires explicit Forge 1.19.2 adaptation"); }
    public NonNullList<Ingredient> m_7527_() { return NonNullList.create(); }
    @Override public ItemStack lms$legacyResult() { return m_8043_(); }
    @Override public NonNullList<Ingredient> lms$legacyIngredients() { return m_7527_(); }
    @Override public Optional<Ingredient> templateIngredient() { return Optional.empty(); }
    @Override public Ingredient baseIngredient() { return f_44518_; }
    @Override public Optional<Ingredient> additionIngredient() { return Optional.of(f_44519_); }
    @Override public RecipeSerializer<Forge1192UpgradeRecipe> getSerializer() {
        requireNativeBase();
        return SERIALIZER;
    }
    @Override protected PlacementInfo createPlacementInfo() { return PlacementInfo.createFromOptionals(List.of(Optional.empty(), Optional.of(f_44518_), Optional.of(f_44519_))); }
    private void requireNativeBase() {
        if (!getClass().equals(Forge1192UpgradeRecipe.class)) throw new UnsupportedOperationException(
                "Custom source UpgradeRecipe callbacks require an authoritative mutable source Container; native immutable smithing input cannot prove that contract");
    }
    @Override public List<RecipeDisplay> display() {
        requireNativeBase();
        return List.of(new SmithingRecipeDisplay(Ingredient.optionalIngredientToDisplay(Optional.empty()), f_44518_.display(),
                f_44519_.display(), new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(f_44520_)), new SlotDisplay.ItemSlotDisplay(Items.SMITHING_TABLE)));
    }
}
