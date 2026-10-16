package com.fuckingdeveloper.lms.profile.forge1192.runtime.registry;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.Forge1192LifecycleBridge;
import com.fuckingdeveloper.lms.target.neoforge263.Target263ReloadLookupBridge;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * Resolves Forge 1.19.2 registry globals that became data-pack/world scoped.
 * The returned object is always a real target Registry from the active
 * registration, server, or client world; LMS never caches a counterfeit
 * process-global registry.
 */
public final class Forge1192DynamicRegistryBridge {
    private static final ThreadLocal<RegistryAccess> ACTIVE_ACCESS = new ThreadLocal<>();
    private static final Map<Object, Set<String>> DEFERRED =
            Collections.synchronizedMap(new IdentityHashMap<>());

    private Forge1192DynamicRegistryBridge() {}

    public static Registry<?> enchantments() {
        return resolve(Registries.ENCHANTMENT);
    }

    public static void enchant(
            ItemStack stack, ResourceKey<Enchantment> enchantment, int level) {
        HolderLookup.RegistryLookup<Enchantment> registry = readLookup(Registries.ENCHANTMENT);
        stack.enchant(registry.getOrThrow(enchantment), level);
    }

    public static int enchantmentMaxLevel(ResourceKey<Enchantment> enchantment) {
        HolderLookup.RegistryLookup<Enchantment> registry = readLookup(Registries.ENCHANTMENT);
        return registry.getOrThrow(enchantment).value().getMaxLevel();
    }

    public static int itemEnchantmentLevel(
            ResourceKey<Enchantment> enchantment, ItemStack stack) {
        HolderLookup.RegistryLookup<Enchantment> registry = readLookup(Registries.ENCHANTMENT);
        return EnchantmentHelper.getItemEnchantmentLevel(
                registry.getOrThrow(enchantment), stack);
    }

    public static int stackEnchantmentLevel(
            ItemStack stack, ResourceKey<Enchantment> enchantment) {
        return itemEnchantmentLevel(enchantment, stack);
    }

    public static Map.Entry<EquipmentSlot, ItemStack> randomItemWith(
            ResourceKey<Enchantment> enchantment, LivingEntity entity) {
        return randomItemWith(enchantment, entity, stack -> true);
    }

    public static Map.Entry<EquipmentSlot, ItemStack> randomItemWith(
            ResourceKey<Enchantment> enchantment, LivingEntity entity,
            Predicate<ItemStack> predicate) {
        HolderLookup.RegistryLookup<Enchantment> registry = readLookup(Registries.ENCHANTMENT);
        var holder = registry.getOrThrow(enchantment);
        var matches = new ArrayList<Map.Entry<EquipmentSlot, ItemStack>>();
        for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (!stack.isEmpty()
                    && EnchantmentHelper.getItemEnchantmentLevel(holder, stack) > 0
                    && holder.value().matchingSlot(slot)
                    && predicate.test(stack)) {
                matches.add(Map.entry(slot, stack));
            }
        }
        return matches.isEmpty() ? null
                : matches.get(entity.getRandom().nextInt(matches.size()));
    }

    public static Registry<?> paintingVariants() {
        return resolve(Registries.PAINTING_VARIANT);
    }

    public static void populateSimpleTreeBlockMapping(Map<Block, Block> mapping) {
        mapping.clear();
        for (Object value : resolve(Registries.FEATURE)) {
            TreeFeature tree;
            if (value instanceof TreeFeature nativeTree) tree = nativeTree;
            else if (value instanceof com.fuckingdeveloper.lms.target.neoforge263.worldgen.Target263WorldgenAdapter.TreeIntent intent) tree = intent.settings();
            else continue;
            if (!(tree.trunkProvider().value() instanceof SimpleStateProvider trunk)) continue;
            if (!(tree.foliageProvider().value() instanceof SimpleStateProvider foliage)) continue;
            mapping.put(trunk.state().getBlock(), foliage.state().getBlock());
        }
    }

    public static void deferConfiguredFeatureInitialization(
            Object receiver, Class<?> owner, String methodName) {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            invokeScoped(Forge1192LifecycleBridge.requireActive().modId(), receiver,
                    owner, methodName, server.registryAccess());
            return;
        }
        String key = owner.getName() + '#' + methodName;
        synchronized (DEFERRED) {
            if (!DEFERRED.computeIfAbsent(receiver, ignored -> new HashSet<>()).add(key)) return;
        }
        String modId = Forge1192LifecycleBridge.requireActive().modId();
        NeoForge.EVENT_BUS.addListener((ServerAboutToStartEvent event) ->
                invokeScoped(modId, receiver, owner, methodName, event.getServer().registryAccess()));
    }

    private static void invokeScoped(
            String modId, Object receiver, Class<?> owner, String methodName,
            RegistryAccess registryAccess) {
        RegistryAccess previous = ACTIVE_ACCESS.get();
        ACTIVE_ACCESS.set(registryAccess);
        try (var lifecycle = Forge1192LifecycleBridge.enterRecorded(modId);
             var ignored = Forge1192RegistrationContext.enterMod(modId)) {
            Method method = owner.getDeclaredMethod(methodName);
            if (!method.canAccess(receiver)) method.setAccessible(true);
            method.invoke(receiver);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtime) throw runtime;
            if (cause instanceof Error error) throw error;
            throw new IllegalStateException("Deferred configured-feature initialization failed", cause);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot invoke configured-feature initializer "
                    + owner.getName() + '#' + methodName, e);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot close configured-feature initializer scope", e);
        } finally {
            if (previous == null) ACTIVE_ACCESS.remove();
            else ACTIVE_ACCESS.set(previous);
        }
    }

    private static Registry<?> resolve(ResourceKey<? extends Registry<?>> key) {
        RegistryAccess active = ACTIVE_ACCESS.get();
        if (active != null) return active.lookupOrThrow(key);
        Registry<?> registration = Forge1192RegistrationContext.activeRegistry(key);
        if (registration != null) return registration;

        var server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) return server.registryAccess().lookupOrThrow(key);

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) return minecraft.level.registryAccess().lookupOrThrow(key);

        throw new IllegalStateException("Legacy access to data-driven registry "
                + key.identifier() + " requires an active registration or world context");
    }

    /** A target reload lookup can expose live data without being a mutable Registry. */
    private static <T> HolderLookup.RegistryLookup<T> readLookup(ResourceKey<Registry<T>> key) {
        var reload = Target263ReloadLookupBridge.currentRegistries();
        if (reload.isPresent()) return reload.get().lookupOrThrow(key);
        return resolveTyped(key);
    }

    @SuppressWarnings("unchecked")
    private static <T> Registry<T> resolveTyped(ResourceKey<Registry<T>> key) {
        return (Registry<T>) resolve(key);
    }
}
