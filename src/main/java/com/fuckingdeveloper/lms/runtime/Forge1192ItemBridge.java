package com.fuckingdeveloper.lms.runtime;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.tags.ItemTags;

/** Target adapter for item semantics that moved into default components. */
public final class Forge1192ItemBridge {
    private Forge1192ItemBridge() {}

    private static final ClassValue<java.util.Optional<java.lang.invoke.MethodHandle>>
            LEGACY_CORRECT_TOOL = new ClassValue<>() {
                @Override
                protected java.util.Optional<java.lang.invoke.MethodHandle> computeValue(Class<?> type) {
                    try {
                        var method = type.getMethod("m_8096_", BlockState.class);
                        if (method.getReturnType() != boolean.class)
                            throw new IllegalStateException("Invalid source correct-tool return type: " + type.getName());
                        return java.util.Optional.of(java.lang.invoke.MethodHandles
                                .privateLookupIn(method.getDeclaringClass(), java.lang.invoke.MethodHandles.lookup())
                                .unreflect(method));
                    } catch (NoSuchMethodException absent) {
                        return java.util.Optional.empty();
                    } catch (IllegalAccessException inaccessible) {
                        throw new IllegalStateException("Cannot access source correct-tool override: "
                                + type.getName(), inaccessible);
                    }
                }
            };

    /** Source Item-only calls have no stack state; native target calls use exact defaults. */
    public static boolean isCorrectToolForDrops(Item item, BlockState state) {
        var legacy = LEGACY_CORRECT_TOOL.get(item.getClass());
        if (legacy.isPresent()) {
            try {
                return (boolean) legacy.get().invoke(item, state);
            } catch (Throwable error) {
                throw new IllegalStateException("Source correct-tool predicate failed for "
                        + item.getClass().getName(), error);
            }
        }
        Class<?> base = item.getClass();
        boolean managed = false;
        while (base.getClassLoader() instanceof com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader) {
            managed = true;
            base = base.getSuperclass();
        }
        if (managed) {
            // Verified 1.19.2 Item default: return false. Other source bases
            // require their own proven predicate rather than empty components.
            if (base == Item.class) return false;
            throw new IllegalStateException("Source tool-base predicate is not adapted: " + base.getName());
        }
        if (item instanceof com.fuckingdeveloper.lms.runtime.item.LegacyAxeItem
                || item instanceof com.fuckingdeveloper.lms.runtime.item.LegacyHoeItem
                || item instanceof com.fuckingdeveloper.lms.runtime.item.LegacyPickaxeItem
                || item instanceof com.fuckingdeveloper.lms.runtime.item.LegacyShovelItem
                || item instanceof com.fuckingdeveloper.lms.runtime.item.LegacySwordItem) {
            throw new IllegalStateException("Source tool-base predicate is not adapted: " + item.getClass().getName());
        }
        return item.isCorrectToolForDrops(Forge1192EarlyItemStackBridge.create(item), state);
    }

    /** Exact source Item super implementation, deliberately without virtual dispatch. */
    public static boolean baseCorrectToolForDrops(Item item, BlockState state) {
        return false;
    }

    private static final ClassValue<java.util.Optional<java.lang.invoke.MethodHandle>>
            LEGACY_DESCRIPTION_ID = new ClassValue<>() {
                @Override
                protected java.util.Optional<java.lang.invoke.MethodHandle> computeValue(Class<?> type) {
                    try {
                        return java.util.Optional.of(java.lang.invoke.MethodHandles.publicLookup()
                                .findVirtual(type, "m_5671_", java.lang.invoke.MethodType.methodType(
                                        String.class, ItemStack.class)));
                    } catch (NoSuchMethodException | IllegalAccessException ignored) {
                        return java.util.Optional.empty();
                    }
                }
            };

    /** Forge 1.19.2 Item#isEdible was equivalent to having food properties. */
    public static boolean isEdible(Item item) {
        var override = LEGACY_EDIBLE.get(item.getClass());
        if (override.isPresent()) {
            try { return (boolean) override.get().invoke(item); }
            catch (Throwable error) { throw new IllegalStateException("Legacy edible predicate failed", error); }
        }
        if (item.builtInRegistryHolder().areComponentsBound())
            return item.components().has(DataComponents.FOOD);
        return Forge1192EarlyItemStackBridge.create(item)
                .getComponents().has(DataComponents.FOOD);
    }

    private static final ClassValue<java.util.Optional<java.lang.invoke.MethodHandle>> LEGACY_EDIBLE = new ClassValue<>() {
        @Override protected java.util.Optional<java.lang.invoke.MethodHandle> computeValue(Class<?> type) {
            try {
                return java.util.Optional.of(java.lang.invoke.MethodHandles.publicLookup().findVirtual(
                        type, "m_41472_", java.lang.invoke.MethodType.methodType(boolean.class)));
            } catch (NoSuchMethodException | IllegalAccessException ignored) {
                return java.util.Optional.empty();
            }
        }
    };

    /** Source Item#getName creates a fresh component and dispatches stack description virtually. */
    public static net.minecraft.network.chat.Component baseName(Item item, ItemStack stack) {
        return net.minecraft.network.chat.Component.translatable(getDescriptionId(item, stack));
    }

    public static net.minecraft.world.InteractionResult useResult(
            com.fuckingdeveloper.lms.runtime.item.LegacyInteractionResultHolder<?> holder,
            net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        java.util.Objects.requireNonNull(holder, "legacy use result");
        if (!(holder.m_19095_() instanceof ItemStack stack))
            throw new IllegalStateException("Source Item.use returned a non-stack value");
        var result = holder.m_19089_();
        if (result instanceof net.minecraft.world.InteractionResult.Success success)
            return success.heldItemTransformedTo(stack);
        if (stack != player.getItemInHand(hand))
            throw new IllegalStateException("Source non-success use result replaces the held stack; target migration is not established");
        return result;
    }

    public static com.fuckingdeveloper.lms.runtime.item.LegacyInteractionResultHolder<ItemStack> baseUse(
            Item item, net.minecraft.world.level.Level level, net.minecraft.world.entity.player.Player player,
            net.minecraft.world.InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        var consumable = stack.get(DataComponents.CONSUMABLE);
        if (consumable == null) {
            if (stack.has(DataComponents.FOOD)) throw new IllegalStateException("Source food has no target consumption intent");
            return com.fuckingdeveloper.lms.runtime.item.LegacyInteractionResultHolder.m_19098_(stack);
        }
        return new com.fuckingdeveloper.lms.runtime.item.LegacyInteractionResultHolder<>(
                consumable.startConsuming(player, stack, hand), stack);
    }

    /** Preserve the source DiggerItem type family after target class removal. */
    public static boolean isDiggerItem(Object value) {
        if (!(value instanceof Item item)) return false;
        if (item instanceof com.fuckingdeveloper.lms.runtime.item.LegacyAxeItem
                || item instanceof com.fuckingdeveloper.lms.runtime.item.LegacyHoeItem
                || item instanceof com.fuckingdeveloper.lms.runtime.item.LegacyPickaxeItem
                || item instanceof com.fuckingdeveloper.lms.runtime.item.LegacyShovelItem) {
            return true;
        }
        var holder = item.builtInRegistryHolder();
        return holder.is(ItemTags.AXES) || holder.is(ItemTags.HOES)
                || holder.is(ItemTags.PICKAXES) || holder.is(ItemTags.SHOVELS);
    }

    /** Forge 1.19.2 ItemStack#is(Item) was an exact item-identity comparison. */
    public static boolean is(ItemStack stack, Item item) {
        return stack.getItem() == item;
    }

    /**
     * Forge 1.19.2 ItemStack#getDescriptionId delegated virtually to
     * Item#getDescriptionId(ItemStack). The target removed that overload, so
     * retain managed legacy overrides and otherwise use the exact source
     * default of the item's ordinary description id.
     */
    public static String getDescriptionId(ItemStack stack) {
        return getDescriptionId(stack.getItem(), stack);
    }

    public static String getDescriptionId(Item item, ItemStack stack) {
        var legacy = LEGACY_DESCRIPTION_ID.get(item.getClass());
        if (legacy.isEmpty()) {
            var stackless = LEGACY_STACKLESS_DESCRIPTION.get(item.getClass());
            if (stackless.isEmpty()) return item.getDescriptionId();
            try { return (String) stackless.get().invoke(item); }
            catch (Throwable error) { throw new IllegalStateException("Legacy stackless description failed", error); }
        }
        try {
            return (String) legacy.get().invoke(item, stack);
        } catch (Throwable error) {
            throw new IllegalStateException("Legacy Item#getDescriptionId(ItemStack) failed for "
                    + item.getClass().getName(), error);
        }
    }

    private static final ClassValue<java.util.Optional<java.lang.invoke.MethodHandle>> LEGACY_STACKLESS_DESCRIPTION = new ClassValue<>() {
        @Override protected java.util.Optional<java.lang.invoke.MethodHandle> computeValue(Class<?> type) {
            for (Class<?> cursor = type; cursor != null; cursor = cursor.getSuperclass()) {
                if (!(cursor.getClassLoader() instanceof com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader)) break;
                for (String name : new String[]{"m_41466_", "m_41467_"}) {
                    try {
                        return java.util.Optional.of(java.lang.invoke.MethodHandles.privateLookupIn(cursor,
                                java.lang.invoke.MethodHandles.lookup()).findVirtual(cursor, name,
                                java.lang.invoke.MethodType.methodType(String.class)));
                    } catch (NoSuchMethodException ignored) {
                        // Continue only when this exact source override is absent.
                    } catch (IllegalAccessException error) {
                        throw new IllegalStateException("Cannot access legacy description override", error);
                    }
                }
            }
            return java.util.Optional.empty();
        }
    };

    /**
     * Forge 1.19.2 tested specifically for the stack's {@code display.Name}
     * value. The target CUSTOM_NAME component is that same explicit naming
     * intent; {@link ItemStack#getCustomName()} is deliberately broader for
     * written-book titles and is therefore not equivalent here.
     */
    public static boolean hasCustomHoverName(ItemStack stack) {
        return stack.has(DataComponents.CUSTOM_NAME);
    }

    /** Store a source-era arbitrary stack tag in the target custom-data component. */
    public static void addTagElement(ItemStack stack, String key, Tag value) {
        getOrCreateTag(stack).put(key, value);
    }

    public static boolean hasTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && !data.tag.isEmpty();
    }

    /** Return the live source-era tag view, not a detached target copy. */
    public static net.minecraft.nbt.CompoundTag getTag(ItemStack stack) {
        return mutableTags(stack).acquire(stack, false);
    }

    public static net.minecraft.nbt.CompoundTag getOrCreateTag(ItemStack stack) {
        return mutableTags(stack).acquire(stack, true);
    }

    private static com.fuckingdeveloper.lms.runtime.itemhandler.LegacyMutableTagState mutableTags(ItemStack stack) {
        if (!((Object)stack instanceof com.fuckingdeveloper.lms.runtime.itemhandler.LegacyMutableTagCarrier carrier))
            throw new IllegalStateException("Source mutable stack-tag lifecycle hook is missing");
        return carrier.lms$mutableTags();
    }

    public static net.minecraft.nbt.CompoundTag getOrCreateTagElement(
            ItemStack stack, String key) {
        var root = getOrCreateTag(stack);
        Tag current = root.get(key);
        if (current instanceof net.minecraft.nbt.CompoundTag compound) return compound;
        var compound = new net.minecraft.nbt.CompoundTag();
        root.put(key, compound);
        return compound;
    }

    public static net.minecraft.nbt.CompoundTag getTagElement(ItemStack stack, String key) {
        var root = getTag(stack);
        if (root == null) return null;
        Tag current = root.get(key);
        return current instanceof net.minecraft.nbt.CompoundTag compound ? compound : null;
    }

    public static void removeTagKey(ItemStack stack, String key) {
        var root = getTag(stack);
        if (root == null) return;
        root.remove(key);
        if (root.isEmpty()) mutableTags(stack).assign(stack, null);
    }

    public static void setTag(ItemStack stack, net.minecraft.nbt.CompoundTag tag) {
        mutableTags(stack).assign(stack, tag);
    }

    /** Preserve the exact Forge 1.19.2 ItemStack NBT schema. */
    public static CompoundTag save(ItemStack stack, CompoundTag destination) {
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        destination.putString("id", id == null ? "minecraft:air" : id.toString());
        destination.putByte("Count", (byte) stack.getCount());
        CompoundTag legacyTag = getTag(stack);
        if (legacyTag != null) destination.put("tag", legacyTag.copy());
        return destination;
    }
}
