package com.fuckingdeveloper.lms.profile.forge1192.runtime.item;

import java.util.UUID;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/** Semantic bridge for the UUID-keyed Forge/Minecraft 1.19.2 attribute ABI. */
public final class Forge1192AttributeBridge {
    private static final String LEGACY_NAMESPACE = "lms_legacy";

    private Forge1192AttributeBridge() {}

    public static AttributeModifier modifier(UUID uuid, String ignoredLegacyName, double amount,
                                             AttributeModifier.Operation operation) {
        return new AttributeModifier(identifier(uuid), amount, operation);
    }

    public static AttributeModifier getModifier(AttributeInstance instance, UUID uuid) {
        return instance.getModifier(identifier(uuid));
    }

    public static void removeModifier(AttributeInstance instance, UUID uuid) {
        instance.removeModifier(identifier(uuid));
    }

    /** Source maps can contain registered attribute values or projected native holders. */
    @SuppressWarnings("unchecked")
    public static net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> holder(Object value) {
        if (value instanceof net.minecraft.core.Holder<?> holder
                && holder.value() instanceof net.minecraft.world.entity.ai.attributes.Attribute)
            return (net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute>) holder;
        if (value instanceof net.minecraft.world.entity.ai.attributes.Attribute attribute) {
            var registry = net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE;
            var key = registry.getResourceKey(attribute).orElseThrow(() ->
                    new IllegalStateException("Source attribute is not registered: " + attribute));
            return registry.getOrThrow(key);
        }
        throw new IllegalArgumentException("Source modifier key is not an attribute: " + value);
    }

    private static Identifier identifier(UUID uuid) {
        if (uuid == null) throw new IllegalArgumentException("Legacy attribute modifier UUID is null");
        return Identifier.fromNamespaceAndPath(LEGACY_NAMESPACE, uuid.toString());
    }
}
