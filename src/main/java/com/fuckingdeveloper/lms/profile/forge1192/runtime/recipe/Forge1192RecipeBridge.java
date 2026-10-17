package com.fuckingdeveloper.lms.profile.forge1192.runtime.recipe;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.recipe.LegacyRecipeIngredients;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.recipe.LegacyRecipeResult;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

import java.util.Objects;
import java.util.Collection;

/** Source-era full client recipe access backed by a co-resident server. */
public final class Forge1192RecipeBridge {
    private Forge1192RecipeBridge() {}

    private static final java.lang.ref.ReferenceQueue<Recipe<?>> ID_QUEUE = new java.lang.ref.ReferenceQueue<>();
    private static final java.util.Map<RecipeReference, RecipeIdentity> NATIVE_IDS = new java.util.HashMap<>();
    private record RecipeIdentity(net.minecraft.resources.Identifier id, boolean ambiguous) {}
    private static final class RecipeReference extends java.lang.ref.WeakReference<Recipe<?>> {
        private final int hash;
        RecipeReference(Recipe<?> recipe, java.lang.ref.ReferenceQueue<Recipe<?>> queue) { super(recipe,queue); hash=System.identityHashCode(recipe); }
        @Override public int hashCode() { return hash; }
        @Override public boolean equals(Object other) {
            return this==other || (other instanceof RecipeReference ref && get()!=null && get()==ref.get());
        }
    }
    private static void cleanIdentities() {
        java.lang.ref.Reference<? extends Recipe<?>> ref;
        while ((ref=ID_QUEUE.poll())!=null) NATIVE_IDS.remove(ref);
    }
    public static synchronized void recordRecipeIdentity(RecipeHolder<?> holder) {
        cleanIdentities();
        var key=new RecipeReference(holder.value(),ID_QUEUE);
        var id=holder.id().identifier();
        var previous=NATIVE_IDS.putIfAbsent(key,new RecipeIdentity(id,false));
        if (previous!=null && (previous.ambiguous() || !previous.id().equals(id))) {
            NATIVE_IDS.put(key,new RecipeIdentity(previous.id(),true));
            throw new IllegalStateException("Native recipe has ambiguous holder identifiers: " + previous.id() + " / " + id);
        }
    }
    private static synchronized net.minecraft.resources.Identifier nativeId(Recipe<?> recipe) {
        cleanIdentities();
        var id=NATIVE_IDS.get(new RecipeReference(recipe,null));
        if (id==null) throw new UnsupportedOperationException("Source identifier requires source ownership or an enumerated authoritative RecipeHolder: " + recipe.getClass().getName());
        if (id.ambiguous()) throw new IllegalStateException("Native recipe holder identity is ambiguous");
        return id.id();
    }
    public static RecipeManager serverRecipeManager(net.minecraft.server.MinecraftServer server) {
        return Objects.requireNonNull(server,"server").getRecipeManager();
    }
    public static int cookingTime(net.minecraft.world.item.crafting.AbstractCookingRecipe recipe) { return recipe instanceof com.fuckingdeveloper.lms.profile.forge1192.runtime.recipe.LegacyCookingTime source ? source.lms$cookingTime() : recipe.cookingTime(); }
    public static float cookingExperience(net.minecraft.world.item.crafting.AbstractCookingRecipe recipe) { return recipe instanceof com.fuckingdeveloper.lms.profile.forge1192.runtime.recipe.LegacyCookingExperience source ? source.lms$cookingExperience() : recipe.experience(); }


    public static RecipeManager worldRecipeManager(net.minecraft.world.level.Level level) {
        Objects.requireNonNull(level, "level");
        if (!level.isClientSide()) {
            if (level.recipeAccess() instanceof RecipeManager manager) return manager;
            throw new UnsupportedOperationException("Source world has no authoritative full recipe manager");
        }
        var client = net.minecraft.client.Minecraft.getInstance();
        if (client.level != level) throw new IllegalStateException("Source recipe query uses a stale client world");
        return clientRecipeManager(Objects.requireNonNull(client.getConnection(), "Active client connection"));
    }

    public static RecipeManager clientRecipeManager(ClientPacketListener connection) {
        Objects.requireNonNull(connection, "connection");
        var client = net.minecraft.client.Minecraft.getInstance();
        if (client.getConnection() != connection)
            throw new IllegalStateException("Source recipe query uses a stale client connection");
        var server = client.getSingleplayerServer();
        if (server == null) throw new UnsupportedOperationException(
                "Forge 1.19.2 full client recipe access requires a co-resident integrated server; "
                        + "Minecraft 26.3 only synchronizes a reduced RecipeAccess view");
        return server.getRecipeManager();
    }

    public static Collection<Recipe<?>> legacyRecipes(RecipeManager manager) {
        Objects.requireNonNull(manager, "manager");
        java.util.ArrayList<Recipe<?>> recipes = new java.util.ArrayList<>();
        for (RecipeHolder<?> holder : manager.getRecipes()) { recordRecipeIdentity(holder); recipes.add(holder.value()); }
        return java.util.List.copyOf(recipes);
    }

    public static net.minecraft.resources.Identifier legacyId(Recipe<?> recipe) {
        Objects.requireNonNull(recipe, "recipe");
        if (recipe instanceof com.fuckingdeveloper.lms.profile.forge1192.runtime.recipe.LegacyRecipeIdentity legacy)
            return Objects.requireNonNull(legacy.lms$legacyId(), "Source recipe returned null identifier");
        return nativeId(recipe);
    }

    public static ItemStack legacyResult(Recipe<?> recipe) {
        Objects.requireNonNull(recipe, "recipe");
        if (recipe instanceof LegacyRecipeResult legacy) return legacy.lms$legacyResult();
        if (recipe instanceof net.minecraft.world.item.crafting.SingleItemRecipe) {
            if (!(recipe instanceof com.fuckingdeveloper.lms.mixin.Target263SingleRecipeResultAccess access))
                throw new IllegalStateException("Native single-input result accessor is not installed");
            return access.lms$nativeResult().create();
        }
        var server = Objects.requireNonNull(ServerLifecycleHooks.getCurrentServer(),
                "Cannot resolve a target recipe display without a co-resident server");
        var context = SlotDisplayContext.fromLevel(server.overworld());
        return recipe.display().stream()
                .map(display -> display.result().resolveForFirstStack(context))
                .filter(stack -> !stack.isEmpty())
                .findFirst().orElse(ItemStack.EMPTY);
    }

    public static NonNullList<LegacyIngredient> legacyIngredients(Recipe<?> recipe) {
        Objects.requireNonNull(recipe, "recipe");
        if (recipe instanceof LegacyRecipeIngredients legacy) return legacy.lms$legacyIngredients();
        NonNullList<LegacyIngredient> result = NonNullList.create();
        recipe.placementInfo().ingredients().forEach(ingredient->result.add(com.fuckingdeveloper.lms.target.neoforge263.Target263SourceIngredients.wrap(ingredient)));
        return result;
    }
}
