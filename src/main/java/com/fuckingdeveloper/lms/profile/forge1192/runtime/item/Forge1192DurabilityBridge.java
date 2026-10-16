package com.fuckingdeveloper.lms.profile.forge1192.runtime.item;

import java.lang.invoke.*;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;

/** Source notification order and item-hook dispatch, independent of native stack callbacks. */
public final class Forge1192DurabilityBridge {
    private Forge1192DurabilityBridge() {}
    private static final ClassValue<Optional<MethodHandle>> SOURCE_DAMAGE = new ClassValue<>() {
        @Override protected Optional<MethodHandle> computeValue(Class<?> type) {
            try {
                var method = type.getMethod("lms$sourceDamageItem", ItemStack.class, int.class, LivingEntity.class, Consumer.class);
                return Optional.of(MethodHandles.privateLookupIn(method.getDeclaringClass(), MethodHandles.lookup()).unreflect(method));
            } catch (NoSuchMethodException absent) { return Optional.empty(); }
            catch (IllegalAccessException failure) { throw new IllegalStateException("Cannot access source damage hook", failure); }
        }
    };
    public static <T extends LivingEntity> int damageItem(Item item, ItemStack stack, int amount, T entity, Consumer<T> onBreak) {
        var source = SOURCE_DAMAGE.get(item.getClass());
        if (source.isPresent()) {
            try { return (int)source.get().invoke(item, stack, amount, entity, onBreak); }
            catch (RuntimeException | Error failure) { throw failure; }
            catch (Throwable failure) { throw new IllegalStateException("Source damage hook failed", failure); }
        }
        return item.damageItem(stack, amount, entity, ignored -> onBreak.accept(entity));
    }
    /** Native game calls retain the source hook's entity callback, exposing a stack snapshot to the target caller. */
    public static int targetDamageItem(Item item, ItemStack stack, int amount, LivingEntity entity, Consumer<ItemStack> onBreak) {
        return damageItem(item, stack, amount, entity, ignored -> onBreak.accept(stack.copy()));
    }
    public static <T extends LivingEntity> void hurtAndBreak(ItemStack stack, int amount, T entity, Consumer<T> onBreak) {
        Objects.requireNonNull(stack); Objects.requireNonNull(entity); Objects.requireNonNull(onBreak);
        if (entity.level().isClientSide() || entity instanceof Player player && player.getAbilities().instabuild || !stack.isDamageableItem()) return;
        int adjusted = damageItem(stack.getItem(), stack, amount, entity, onBreak);
        if (hurt(stack, adjusted, entity.getRandom(), entity instanceof ServerPlayer player ? player : null)) {
            completeBreak(stack, () -> onBreak.accept(entity), item -> {
                if (entity instanceof Player player) player.awardStat(Stats.ITEM_BROKEN.get(item));
            });
        }
    }
    private static void completeBreak(ItemStack stack, Runnable notify, Consumer<Item> statistic) {
        notify.run(); // Source observers see the stack before it is consumed.
        Item item = stack.getItem(); stack.shrink(1); statistic.accept(item); stack.setDamageValue(0);
    }
    public static boolean hurt(ItemStack stack, int amount, RandomSource random, ServerPlayer player) {
        Objects.requireNonNull(stack); Objects.requireNonNull(random);
        if (!stack.isDamageableItem()) return false;
        if (amount > 0 && !stack.getEnchantments().isEmpty())
            throw new IllegalStateException("Source durability enchantment/random contract is not adapted");
        int damage = stack.getDamageValue() + amount;
        if (player != null && amount != 0) CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger(player, stack, damage);
        stack.setDamageValue(damage);
        return damage >= stack.getMaxDamage();
    }
}
