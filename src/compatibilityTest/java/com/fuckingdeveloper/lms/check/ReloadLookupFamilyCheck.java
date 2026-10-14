package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.runtime.Target263DataComponentBindingBridge;
import com.fuckingdeveloper.lms.runtime.Target263ReloadLookupBridge;
import com.mojang.serialization.Lifecycle;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.item.Item;

/** Actual delayed fire-resistance consumes the reload's damage tag, without global mutation. */
public final class ReloadLookupFamilyCheck {
    public static void run() throws Exception {
        var damages = new MappedRegistry<DamageType>(Registries.DAMAGE_TYPE, Lifecycle.stable());
        var fire = damages.register(DamageTypes.IN_FIRE, new DamageType("inFire", 0.1F), RegistrationInfo.BUILT_IN);
        damages.freeze();
        var pending = damages.prepareTagReload(new TagLoader.LoadResult<>(Registries.DAMAGE_TYPE,
                Map.of(DamageTypeTags.IS_FIRE, List.of(fire))));
        HolderLookup.Provider raw = HolderLookup.Provider.create(java.util.stream.Stream.of(damages));
        HolderLookup.Provider lookup = HolderLookup.Provider.create(java.util.stream.Stream.of(pending.lookup()));
        HolderLookup.Provider nested = RegistryAccess.EMPTY;
        var properties = new Item.Properties().fireResistant();
        var key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fixture", "fireproof"));
        try {
            properties.componentInitializer.run(DataComponentMap.builder(), raw, key);
            throw new AssertionError("raw unbound reload registry unexpectedly resolved fire tag");
        } catch (IllegalStateException expected) {
            check(expected.getMessage().startsWith("Missing tag"), "wrong unbound-registry failure");
        }
        var captured = new java.util.concurrent.atomic.AtomicReference<net.minecraft.world.item.component.DamageResistant>();
        Runnable consume = () -> {
            check(Target263DataComponentBindingBridge.currentRegistries().orElseThrow() == lookup, "reload provider identity changed");
            var builder = DataComponentMap.builder();
            properties.componentInitializer.run(builder, Target263DataComponentBindingBridge.currentRegistries().orElseThrow(), key);
            var resistant = builder.build().get(DataComponents.DAMAGE_RESISTANT);
            check(resistant.types() == pending.lookup().getOrThrow(DamageTypeTags.IS_FIRE), "initializer lost pending native tag identity");
            captured.set(resistant);
        };
        check(Target263DataComponentBindingBridge.currentRegistries().isEmpty(), "unexpected pre-existing reload scope");
        Target263ReloadLookupBridge.run(lookup, () -> {
            consume.run();
            try {
                Target263ReloadLookupBridge.run(nested, () -> {
                    check(Target263DataComponentBindingBridge.currentRegistries().orElseThrow() == nested, "nested lookup not active");
                    throw new IllegalStateException("expected nested failure");
                });
                throw new AssertionError("nested failure swallowed");
            } catch (IllegalStateException expected) { check(expected.getMessage().equals("expected nested failure"), "unexpected nested failure"); }
            consume.run();
        });
        check(Target263DataComponentBindingBridge.currentRegistries().isEmpty(), "synchronous scope leaked");
        check(damages.get(DamageTypeTags.IS_FIRE).isEmpty(), "scope prematurely applied pending registry tags");
        pending.apply();
        check(captured.get().types().contains(fire), "captured fire resistance did not follow native pending-tag binding");
        // Verify event delivery itself uses the target updated-tag lookup before source callback work.
        try (var input = com.fuckingdeveloper.lms.runtime.Forge1192EventBusBridge.class.getResourceAsStream("Forge1192EventBusBridge.class")) {
            var node = new org.objectweb.asm.tree.ClassNode(); new org.objectweb.asm.ClassReader(input.readAllBytes()).accept(node, 0);
            var method = node.methods.stream().filter(m -> m.name.equals("invokeScoped")).findFirst().orElseThrow();
            boolean getter = false, scope = false;
            for (var instruction : method.instructions) if (instruction instanceof org.objectweb.asm.tree.MethodInsnNode call) {
                if (call.owner.equals("net/minecraft/server/ReloadableServerResources") && call.name.equals("getRegistryLookup")) getter = true;
                if (call.owner.endsWith("Target263ReloadLookupBridge") && call.name.equals("run")) { check(getter, "callback scope precedes authoritative lookup"); scope = true; }
            }
            check(scope, "synchronous reload event delivery not scoped");
        }
        System.out.println("PASS: actual native fire-resistance initializer/tag membership, synchronous reload lookup, nested failure restoration and thread cleanup; full datapack reload unverified");
    }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
